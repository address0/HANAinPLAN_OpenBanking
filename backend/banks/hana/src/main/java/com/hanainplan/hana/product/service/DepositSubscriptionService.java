package com.hanainplan.hana.product.service;

import com.hanainplan.hana.account.entity.Account;
import com.hanainplan.hana.account.entity.Transaction;
import com.hanainplan.hana.account.repository.AccountRepository;
import com.hanainplan.hana.account.repository.TransactionRepository;
import com.hanainplan.hana.product.dto.DepositSubscriptionRequest;
import com.hanainplan.hana.product.dto.ProductSubscriptionResponseDto;
import com.hanainplan.hana.product.entity.InterestRate;
import com.hanainplan.hana.product.entity.ProductSubscription;
import com.hanainplan.hana.product.repository.InterestRateRepository;
import com.hanainplan.hana.product.repository.ProductSubscriptionRepository;
import com.hanainplan.hana.user.entity.Customer;
import com.hanainplan.hana.user.entity.IrpAccount;
import com.hanainplan.hana.user.repository.CustomerRepository;
import com.hanainplan.hana.user.repository.IrpAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 정기예금 가입 서비스 (IRP 계좌 출금 포함)
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class DepositSubscriptionService {

    private final ProductSubscriptionRepository productSubscriptionRepository;
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final InterestRateRepository interestRateRepository;
    private final IrpAccountRepository irpAccountRepository;
    private final TransactionRepository transactionRepository;

    /**
     * IRP 계좌에서 출금 후 정기예금 가입
     */
    public ProductSubscriptionResponseDto subscribeDepositFromIrp(DepositSubscriptionRequest request) throws Exception {
        log.info("정기예금 가입 처리 시작 - 상품코드: {}, 금액: {}원", 
                request.getProductCode(), request.getContractPrincipal());

        // 1. 고객 정보 확인 및 자동 생성
        Customer customer = ensureCustomerExists(request);

        // 2. 금융상품 존재 확인 (생략 - 상품코드로 직접 조회)
        log.info("정기예금 상품 코드: {}", request.getProductCode());

        // 3. IRP 계좌 조회
        IrpAccount irpAccount = irpAccountRepository.findByAccountNumber(request.getIrpAccountNumber())
                .orElseThrow(() -> new IllegalArgumentException("IRP 계좌를 찾을 수 없습니다: " + request.getIrpAccountNumber()));

        // 4. IRP 계좌 소유자 확인
        if (!irpAccount.getCustomerCi().equals(request.getCustomerCi())) {
            throw new IllegalArgumentException("IRP 계좌 소유자가 일치하지 않습니다.");
        }

        // 5. IRP 계좌 잔액 확인
        if (irpAccount.getCurrentBalance().compareTo(request.getContractPrincipal()) < 0) {
            throw new IllegalArgumentException("IRP 계좌 잔액이 부족합니다. 현재 잔액: " + 
                    irpAccount.getCurrentBalance() + "원, 요청 금액: " + request.getContractPrincipal() + "원");
        }

        // 6. IRP 계좌에서 출금 처리
        BigDecimal newIrpBalance = irpAccount.getCurrentBalance().subtract(request.getContractPrincipal());
        irpAccount.setCurrentBalance(newIrpBalance);
        irpAccountRepository.save(irpAccount);
        
        log.info("IRP 계좌 출금 완료 - 계좌번호: {}, 출금액: {}원, 남은 잔액: {}원", 
                request.getIrpAccountNumber(), request.getContractPrincipal(), newIrpBalance);

        // 7. IRP 계좌 출금 거래내역 저장
        saveIrpWithdrawalTransaction(irpAccount, request.getContractPrincipal(), newIrpBalance, 
                "정기예금 가입 - " + request.getProductCode());

        // 8. 금리 정보 조회
        InterestRate interestRate = interestRateRepository
                .findLatestBasicRateByProductCodeAndMaturityPeriod(request.getProductCode(), request.getMaturityPeriod())
                .orElseThrow(() -> new IllegalArgumentException(
                        "해당 상품의 금리 정보를 찾을 수 없습니다: " + request.getProductCode() + ", " + request.getMaturityPeriod()));

        // 9. 우대금리 조회 (선택사항)
        InterestRate preferentialRate = interestRateRepository
                .findLatestPreferentialRateByProductCodeAndMaturityPeriod(request.getProductCode(), request.getMaturityPeriod())
                .orElse(null);

        // 10. 최종 적용금리 계산
        BigDecimal finalRate = (preferentialRate != null) ? preferentialRate.getInterestRate() : interestRate.getInterestRate();

        // 11. 정기예금 가입 생성
        ProductSubscription subscription = ProductSubscription.builder()
                .customerCi(request.getCustomerCi())
                .productCode(request.getProductCode())
                .accountNumber(request.getAccountNumber())
                .status(request.getStatus())
                .subscriptionDate(LocalDate.parse(request.getSubscriptionDate()))
                .maturityDate(LocalDate.parse(request.getMaturityDate()))
                .contractPeriod(request.getContractPeriod())
                .maturityPeriod(request.getMaturityPeriod())
                .rateType(request.getRateType())
                .baseRate(interestRate.getInterestRate())
                .preferentialRate(preferentialRate != null ? preferentialRate.getInterestRate() : null)
                .finalAppliedRate(finalRate)
                .interestCalculationBasis(request.getInterestCalculationBasis())
                .interestPaymentMethod(request.getInterestPaymentMethod())
                .contractPrincipal(request.getContractPrincipal())
                .currentBalance(request.getCurrentBalance())
                .branchName(request.getBranchName())
                .build();

        ProductSubscription savedSubscription = productSubscriptionRepository.save(subscription);
        
        log.info("정기예금 가입 완료 - 가입ID: {}, 상품코드: {}, 금액: {}원", 
                savedSubscription.getSubscriptionId(), savedSubscription.getProductCode(), 
                savedSubscription.getContractPrincipal());

        return ProductSubscriptionResponseDto.from(savedSubscription);
    }

    /**
     * 고객 정보 확인 및 자동 생성
     */
    private Customer ensureCustomerExists(DepositSubscriptionRequest request) {
        return customerRepository.findByCi(request.getCustomerCi())
                .orElseGet(() -> {
                    log.info("하나은행에 고객 정보가 없어 자동 생성 - CI: {}, 이름: {}", 
                            request.getCustomerCi(), request.getCustomerName());
                    
                    Customer newCustomer = Customer.builder()
                            .ci(request.getCustomerCi())
                            .name(request.getCustomerName())
                            .birthDate(request.getBirthDate())
                            .gender(request.getGender())
                            .phone(request.getPhone())
                            .build();
                    
                    return customerRepository.save(newCustomer);
                });
    }

    /**
     * IRP 계좌 출금 거래내역 저장
     */
    private void saveIrpWithdrawalTransaction(IrpAccount irpAccount, BigDecimal amount, 
                                               BigDecimal balanceAfter, String description) {
        try {
            // IRP 계좌의 일반 계좌 정보 조회
            Account account = accountRepository.findByAccountNumber(irpAccount.getAccountNumber())
                    .orElseThrow(() -> new RuntimeException("IRP 일반 계좌를 찾을 수 없습니다: " + irpAccount.getAccountNumber()));
            
            // 일반 계좌의 잔액도 업데이트
            account.setBalance(balanceAfter);
            accountRepository.save(account);

            // 거래 ID 생성
            String transactionId = "HANA-IRP-WD-" + System.currentTimeMillis() + "-" +
                    String.format("%04d", (int)(Math.random() * 10000));

            // 거래내역 생성
            Transaction transaction = Transaction.builder()
                    .transactionId(transactionId)
                    .transactionDatetime(LocalDateTime.now())
                    .transactionType("출금")
                    .transactionCategory("정기예금 가입")
                    .amount(amount)
                    .balanceAfter(balanceAfter)
                    .description(description)
                    .branchName("하나은행 본점")
                    .account(account)
                    .build();

            transactionRepository.save(transaction);
            
            log.info("IRP 계좌 출금 거래내역 저장 완료 - 거래ID: {}, 계좌번호: {}, 금액: {}원", 
                    transactionId, irpAccount.getAccountNumber(), amount);

        } catch (Exception e) {
            log.error("IRP 계좌 출금 거래내역 저장 실패 - 계좌번호: {}, 오류: {}", 
                    irpAccount.getAccountNumber(), e.getMessage());
            throw new RuntimeException("거래내역 저장 실패: " + e.getMessage());
        }
    }
}
