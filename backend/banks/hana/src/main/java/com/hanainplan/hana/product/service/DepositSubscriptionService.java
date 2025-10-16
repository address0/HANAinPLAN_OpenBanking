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

    public ProductSubscriptionResponseDto subscribeDepositFromIrp(DepositSubscriptionRequest request) throws Exception {
        log.info("정기예금 가입 처리 시작 - 상품코드: {}, 금액: {}원", 
                request.getProductCode(), request.getContractPrincipal());

        Customer customer = ensureCustomerExists(request);

        log.info("정기예금 상품 코드: {}", request.getProductCode());

        IrpAccount irpAccount = irpAccountRepository.findByAccountNumber(request.getIrpAccountNumber())
                .orElseThrow(() -> new IllegalArgumentException("IRP 계좌를 찾을 수 없습니다: " + request.getIrpAccountNumber()));

        if (!irpAccount.getCustomerCi().equals(request.getCustomerCi())) {
            throw new IllegalArgumentException("IRP 계좌 소유자가 일치하지 않습니다.");
        }

        if (irpAccount.getCurrentBalance().compareTo(request.getContractPrincipal()) < 0) {
            throw new IllegalArgumentException("IRP 계좌 잔액이 부족합니다. 현재 잔액: " + 
                    irpAccount.getCurrentBalance() + "원, 요청 금액: " + request.getContractPrincipal() + "원");
        }

        BigDecimal newIrpBalance = irpAccount.getCurrentBalance().subtract(request.getContractPrincipal());
        irpAccount.setCurrentBalance(newIrpBalance);
        irpAccountRepository.save(irpAccount);

        log.info("IRP 계좌 출금 완료 - 계좌번호: {}, 출금액: {}원, 남은 잔액: {}원", 
                request.getIrpAccountNumber(), request.getContractPrincipal(), newIrpBalance);

        saveIrpWithdrawalTransaction(irpAccount, request.getContractPrincipal(), newIrpBalance, 
                "정기예금 가입 - " + request.getProductCode());

        BigDecimal baseRate = request.getBaseRate();
        BigDecimal preferentialRate = request.getPreferentialRate();
        BigDecimal finalRate = request.getFinalAppliedRate();

        log.info("HANAinPLAN에서 전달받은 금리 사용 - 기본금리: {}%, 우대금리: {}%, 최종금리: {}%", 
                baseRate, preferentialRate, finalRate);

        ProductSubscription subscription = ProductSubscription.builder()
                .customerCi(request.getCustomerCi())
                .productCode(request.getProductCode())
                .accountNumber(request.getIrpAccountNumber()) // IRP 계좌번호 사용
                .status(request.getStatus())
                .subscriptionDate(LocalDate.parse(request.getSubscriptionDate()))
                .maturityDate(LocalDate.parse(request.getMaturityDate()))
                .contractPeriod(request.getContractPeriod())
                .maturityPeriod(request.getMaturityPeriod())
                .rateType(request.getRateType())
                .baseRate(baseRate)
                .preferentialRate(preferentialRate)
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

    private void saveIrpWithdrawalTransaction(IrpAccount irpAccount, BigDecimal amount, 
                                               BigDecimal balanceAfter, String description) {
        try {
            Account account = accountRepository.findByAccountNumber(irpAccount.getAccountNumber())
                    .orElseThrow(() -> new RuntimeException("IRP 일반 계좌를 찾을 수 없습니다: " + irpAccount.getAccountNumber()));

            account.setBalance(balanceAfter);
            accountRepository.save(account);

            String transactionId = "HANA-IRP-WD-" + System.currentTimeMillis() + "-" +
                    String.format("%04d", (int)(Math.random() * 10000));

            Transaction transaction = Transaction.builder()
                    .transactionId(transactionId)
                    .accountNumber(irpAccount.getAccountNumber())
                    .transactionDatetime(LocalDateTime.now())
                    .transactionType("출금")
                    .transactionCategory("정기예금 가입")
                    .amount(amount)
                    .balanceAfter(balanceAfter)
                    .description(description)
                    .branchName("하나은행 본점")
                    .transactionStatus("COMPLETED")
                    .transactionDirection("DEBIT")
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