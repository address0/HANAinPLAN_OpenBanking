package com.hanainplan.shinhan.account.service;

import com.hanainplan.shinhan.account.dto.AccountRequestDto;
import com.hanainplan.shinhan.account.dto.AccountResponseDto;
import com.hanainplan.shinhan.account.entity.Account;
import com.hanainplan.shinhan.account.entity.Transaction;
import com.hanainplan.shinhan.account.repository.AccountRepository;
import com.hanainplan.shinhan.account.repository.TransactionRepository;
import com.hanainplan.shinhan.user.repository.CustomerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private TransactionRepository transactionRepository;

    // 신한은행 계좌번호 패턴 (앞자리 3자리)
    private static final String[] SHINHAN_PATTERNS = {
        "456", "457", "458", "459"
    };

    /**
     * 계좌번호 자동 생성
     */
    private String generateAccountNumber() {
        Random random = new Random();
        String pattern = SHINHAN_PATTERNS[random.nextInt(SHINHAN_PATTERNS.length)];
        
        // 나머지 13자리 랜덤 생성
        StringBuilder accountNumber = new StringBuilder(pattern);
        for (int i = 0; i < 13; i++) {
            accountNumber.append(random.nextInt(10));
        }
        
        String generatedNumber = accountNumber.toString();
        
        // 중복 확인
        if (accountRepository.existsById(generatedNumber)) {
            return generateAccountNumber(); // 재귀 호출로 다시 생성
        }
        
        return generatedNumber;
    }

    /**
     * 계좌 생성
     */
    public AccountResponseDto createAccount(AccountRequestDto request) {
        // 고객 CI 존재 확인
        if (!customerRepository.existsByCi(request.getCustomerCi())) {
            throw new IllegalArgumentException("존재하지 않는 고객 CI입니다: " + request.getCustomerCi());
        }

        // 계좌번호 자동 생성
        String accountNumber = generateAccountNumber();

        Account account = Account.builder()
                .accountNumber(accountNumber)
                .accountType(request.getAccountType())
                .balance(request.getBalance())
                .openingDate(request.getOpeningDate())
                .customerCi(request.getCustomerCi())
                .build();

        Account savedAccount = accountRepository.save(account);
        return AccountResponseDto.from(savedAccount);
    }

    /**
     * 계좌 조회 (계좌번호)
     */
    @Transactional(readOnly = true)
    public Optional<AccountResponseDto> getAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .map(AccountResponseDto::from);
    }

    /**
     * CI로 계좌 조회
     */
    @Transactional(readOnly = true)
    public List<AccountResponseDto> getAccountsByCi(String ci) {
        List<Account> accounts = accountRepository.findByCustomerCi(ci);
        return accounts.stream()
                .map(AccountResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 모든 계좌 조회
     */
    @Transactional(readOnly = true)
    public List<AccountResponseDto> getAllAccounts() {
        List<Account> accounts = accountRepository.findAll();
        return accounts.stream()
                .map(AccountResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 계좌 수정
     */
    public AccountResponseDto updateAccount(String accountNumber, AccountRequestDto request) {
        Account account = accountRepository.findById(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("계좌를 찾을 수 없습니다: " + accountNumber));

        // 고객 CI 존재 확인 (CI가 변경된 경우)
        if (!account.getCustomerCi().equals(request.getCustomerCi()) && 
            !customerRepository.existsByCi(request.getCustomerCi())) {
            throw new IllegalArgumentException("존재하지 않는 고객 CI입니다: " + request.getCustomerCi());
        }

        account.setAccountType(request.getAccountType());
        account.setBalance(request.getBalance());
        account.setOpeningDate(request.getOpeningDate());
        account.setCustomerCi(request.getCustomerCi());

        Account updatedAccount = accountRepository.save(account);
        return AccountResponseDto.from(updatedAccount);
    }

    /**
     * 계좌 삭제
     */
    public void deleteAccount(String accountNumber) {
        if (!accountRepository.existsById(accountNumber)) {
            throw new IllegalArgumentException("계좌를 찾을 수 없습니다: " + accountNumber);
        }
        accountRepository.deleteById(accountNumber);
    }

    /**
     * 계좌 잔액 업데이트
     */
    public AccountResponseDto updateBalance(String accountNumber, java.math.BigDecimal newBalance) {
        Account account = accountRepository.findById(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("계좌를 찾을 수 없습니다: " + accountNumber));

        account.setBalance(newBalance);
        Account updatedAccount = accountRepository.save(account);
        return AccountResponseDto.from(updatedAccount);
    }
    
    /**
     * 계좌 출금 처리 (타행 요청용)
     */
    public String processWithdrawal(String accountNumber, BigDecimal amount, String description) throws Exception {
        log.info("신한은행 계좌 출금 처리 시작 - 계좌번호: {}, 금액: {}원", accountNumber, amount);
        
        // 1. 계좌 조회
        Account account = accountRepository.findById(accountNumber)
                .orElseThrow(() -> new Exception("신한은행 계좌를 찾을 수 없습니다: " + accountNumber));
        
        // 2. 잔액 확인
        if (account.getBalance() == null || account.getBalance().compareTo(amount) < 0) {
            throw new Exception("신한은행 계좌 잔액이 부족합니다. 현재 잔액: " + 
                    (account.getBalance() != null ? account.getBalance() : BigDecimal.ZERO) + "원, 요청 금액: " + amount + "원");
        }
        
        // 3. 계좌 잔액 차감
        BigDecimal newBalance = account.getBalance().subtract(amount);
        account.setBalance(newBalance);
        accountRepository.save(account);
        
        // 4. 거래내역 저장
        String transactionId = saveWithdrawalTransaction(account, amount, newBalance, description);
        
        log.info("신한은행 계좌 출금 처리 완료 - 거래ID: {}, 계좌번호: {}, 새 잔액: {}원", 
                transactionId, accountNumber, newBalance);
        
        return transactionId;
    }
    
    /**
     * 출금 거래내역을 신한은행 DB에 저장
     */
    private String saveWithdrawalTransaction(Account account, BigDecimal amount, BigDecimal balanceAfter, String description) {
        try {
            // 거래 ID 생성 (SH-WD-{timestamp}-{random})
            String transactionId = "SH-WD-" + System.currentTimeMillis() + "-" + 
                    String.format("%04d", (int)(Math.random() * 10000));

            // 거래내역 생성
            Transaction transaction = Transaction.builder()
                    .transactionId(transactionId)
                    .transactionDatetime(LocalDateTime.now())
                    .transactionType("출금")
                    .transactionCategory("기타")
                    .amount(amount)
                    .balanceAfter(balanceAfter)
                    .branchName("신한은행 본점")
                    .account(account)
                    .build();

            transactionRepository.save(transaction);
            
            log.info("신한은행 출금 거래내역 저장 완료 - 거래ID: {}, 계좌번호: {}, 금액: {}원", 
                    transactionId, account.getAccountNumber(), amount);
            
            return transactionId;
            
        } catch (Exception e) {
            log.error("신한은행 출금 거래내역 저장 실패 - 계좌번호: {}, 오류: {}", account.getAccountNumber(), e.getMessage());
            throw new RuntimeException("거래내역 저장 실패: " + e.getMessage());
        }
    }
}
