package com.hanainplan.kookmin.account.service;

import com.hanainplan.kookmin.account.dto.AccountRequestDto;
import com.hanainplan.kookmin.account.dto.AccountResponseDto;
import com.hanainplan.kookmin.account.entity.Account;
import com.hanainplan.kookmin.account.repository.AccountRepository;
import com.hanainplan.kookmin.user.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@Transactional
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    // 국민은행 계좌번호 패턴 (앞자리 3자리)
    private static final String[] KOOKMIN_PATTERNS = {
        "123", "124", "125", "126", "127", "128", "129"
    };

    /**
     * 계좌번호 자동 생성
     */
    private String generateAccountNumber() {
        Random random = new Random();
        String pattern = KOOKMIN_PATTERNS[random.nextInt(KOOKMIN_PATTERNS.length)];
        
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
        // 1. 계좌 조회
        Account account = accountRepository.findById(accountNumber)
                .orElseThrow(() -> new Exception("국민은행 계좌를 찾을 수 없습니다: " + accountNumber));
        
        // 2. 잔액 확인
        if (account.getBalance() == null || account.getBalance().compareTo(amount) < 0) {
            throw new Exception("국민은행 계좌 잔액이 부족합니다. 현재 잔액: " + 
                    (account.getBalance() != null ? account.getBalance() : BigDecimal.ZERO) + "원");
        }
        
        // 3. 출금 처리
        BigDecimal newBalance = account.getBalance().subtract(amount);
        account.setBalance(newBalance);
        accountRepository.save(account);
        
        // 4. 거래 ID 생성 (간단한 형태)
        String transactionId = "KB-WD-" + System.currentTimeMillis() + "-" + 
                String.format("%04d", (int)(Math.random() * 10000));
        
        // TODO: 실제로는 거래내역 테이블에 저장해야 함
        // 지금은 로그만 남김
        System.out.println("국민은행 출금 거래 - 거래ID: " + transactionId + 
                ", 계좌번호: " + accountNumber + 
                ", 금액: " + amount + "원" +
                ", 새 잔액: " + newBalance + "원" +
                ", 설명: " + description);
        
        return transactionId;
    }
}
