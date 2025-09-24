package com.hanainplan.hana.account.service;

import com.hanainplan.hana.account.dto.AccountRequestDto;
import com.hanainplan.hana.account.dto.AccountResponseDto;
import com.hanainplan.hana.account.entity.Account;
import com.hanainplan.hana.account.repository.AccountRepository;
import com.hanainplan.hana.user.repository.CustomerRepository;
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

    // 하나은행 계좌번호 패턴 (앞자리 3자리)
    private static final String[] HANA_PATTERNS = {
        "110", "111", "112", "113", "114", "115", "116", "117", "118", "119"
    };

    /**
     * 계좌번호 자동 생성
     */
    private String generateAccountNumber() {
        Random random = new Random();
        String pattern = HANA_PATTERNS[random.nextInt(HANA_PATTERNS.length)];
        
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
            .balance(request.getBalance() != null ? request.getBalance() : BigDecimal.ZERO)
            .openingDate(request.getOpeningDate() != null ? request.getOpeningDate() : LocalDate.now())
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
     * 계좌 잔액 업데이트
     */
    public AccountResponseDto updateBalance(String accountNumber, BigDecimal newBalance) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new IllegalArgumentException("계좌를 찾을 수 없습니다: " + accountNumber));

        account.setBalance(newBalance);
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
}
