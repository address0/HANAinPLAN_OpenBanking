package com.hanainplan.hana.product.service;

import com.hanainplan.hana.account.entity.Account;
import com.hanainplan.hana.account.entity.Transaction;
import com.hanainplan.hana.account.repository.AccountRepository;
import com.hanainplan.hana.account.repository.TransactionRepository;
import com.hanainplan.hana.account.service.AccountService;
import com.hanainplan.hana.product.dto.DepositSubscriptionRequest;
import com.hanainplan.hana.product.dto.DepositSubscriptionResponse;
import com.hanainplan.hana.product.entity.DepositSubscription;
import com.hanainplan.hana.product.repository.DepositSubscriptionRepository;
import com.hanainplan.hana.product.util.InterestRateCalculator;
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
import java.util.List;
import java.util.stream.Collectors;

/**
 * 하나은행 정기예금 가입 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DepositSubscriptionService {

    private final DepositSubscriptionRepository depositSubscriptionRepository;
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final IrpAccountRepository irpAccountRepository;
    private final TransactionRepository transactionRepository;
    private final AccountService accountService;

    /**
     * 정기예금 가입 처리
     */
    @Transactional
    public DepositSubscriptionResponse subscribe(DepositSubscriptionRequest request) {
        log.info("정기예금 가입 요청: customerCi={}, productType={}, period={}{}", 
                request.getCustomerCi(), request.getProductType(), 
                request.getContractPeriod(), request.getContractPeriodUnit());

        // 1. 요청 검증
        request.validateContractPeriod();

        // 2. 고객 확인
        Customer customer = customerRepository.findByCi(request.getCustomerCi())
                .orElseThrow(() -> new RuntimeException("고객을 찾을 수 없습니다: " + request.getCustomerCi()));

        // 3. IRP 계좌 확인
        IrpAccount irpAccount = irpAccountRepository.findByCustomerCiAndAccountNumber(
                request.getCustomerCi(), request.getIrpAccountNumber())
                .orElseThrow(() -> new RuntimeException("IRP 계좌를 찾을 수 없습니다: " + request.getIrpAccountNumber()));

        if (!"ACTIVE".equals(irpAccount.getAccountStatus())) {
            throw new RuntimeException("활성화된 IRP 계좌가 아닙니다: " + request.getIrpAccountNumber());
        }

        // 4. 연결 주계좌 확인 및 잔액 확인
        Account linkedAccount = accountRepository.findByAccountNumber(request.getLinkedAccountNumber())
                .orElseThrow(() -> new RuntimeException("연결 계좌를 찾을 수 없습니다: " + request.getLinkedAccountNumber()));

        if (!linkedAccount.getCustomerCi().equals(request.getCustomerCi())) {
            throw new RuntimeException("계좌 소유자가 일치하지 않습니다");
        }

        if (linkedAccount.getBalance().compareTo(request.getSubscriptionAmount()) < 0) {
            throw new RuntimeException("계좌 잔액이 부족합니다. 현재 잔액: " + linkedAccount.getBalance() + "원");
        }

        // 5. IRP 계좌번호를 예금 계좌번호로 사용 (정기예금은 IRP 계좌에 가입)
        String depositAccountNumber = request.getIrpAccountNumber();

        // 6. 적용 금리 결정
        BigDecimal appliedRate = InterestRateCalculator.getBaseRate(
                request.getProductType(), 
                request.getContractPeriod());

        // 7. 만기일 계산
        LocalDate subscriptionDate = LocalDate.now();
        LocalDate maturityDate = InterestRateCalculator.calculateMaturityDate(
                subscriptionDate, 
                request.getProductType(), 
                request.getContractPeriod());

        // 8. 만기 예상 이자 계산 (개월로 환산)
        int months = request.getProductType() == 2 
                ? (int) Math.ceil(request.getContractPeriod() / 30.0) 
                : request.getContractPeriod();
        BigDecimal expectedInterest = InterestRateCalculator.calculateMaturityInterest(
                request.getSubscriptionAmount(), 
                appliedRate, 
                months);

        log.info("만기 예상 이자: {}원 (원금: {}원, 금리: {}%, 기간: {}{})", 
                expectedInterest, request.getSubscriptionAmount(), 
                appliedRate.multiply(BigDecimal.valueOf(100)), 
                request.getContractPeriod(), request.getContractPeriodUnit());

        // 9. 예금 가입 정보 생성 및 저장
        DepositSubscription subscription = DepositSubscription.builder()
                .customerCi(request.getCustomerCi())
                .accountNumber(depositAccountNumber)
                .status("ACTIVE")
                .subscriptionDate(subscriptionDate)
                .maturityDate(maturityDate)
                .contractPeriod(request.getContractPeriod())
                .productType(request.getProductType())
                .depositCode(request.getDepositCode())
                .rate(appliedRate)
                .currentBalance(request.getSubscriptionAmount())
                .unpaidInterest(BigDecimal.ZERO)
                .build();

        DepositSubscription savedSubscription = depositSubscriptionRepository.save(subscription);
        
        log.info("정기예금 가입 완료: subscriptionId={}, accountNumber={}", 
                savedSubscription.getSubscriptionId(), depositAccountNumber);

        // 10. IRP 계좌 잔액 차감 (정기예금으로 이동)
        try {
            BigDecimal newBalance = irpAccount.getCurrentBalance().subtract(request.getSubscriptionAmount());
            if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                throw new RuntimeException("IRP 계좌 잔액이 부족합니다");
            }
            irpAccount.setCurrentBalance(newBalance);
            irpAccountRepository.save(irpAccount);
            
            log.info("IRP 계좌 잔액 차감: accountNumber={}, 차감액={}원, 남은 잔액={}원", 
                    request.getIrpAccountNumber(), request.getSubscriptionAmount(), newBalance);
        } catch (Exception e) {
            log.error("IRP 계좌 잔액 차감 실패: {}", e.getMessage());
            throw new RuntimeException("IRP 계좌 잔액 차감에 실패했습니다: " + e.getMessage());
        }

        // 11. IRP 계좌의 거래내역 저장 (hana_accounts와 연결된 account를 찾아서 저장)
        String transactionId = null;
        try {
            // IRP 계좌번호로 Account 조회
            Account irpAccountEntity = accountRepository.findByAccountNumber(request.getIrpAccountNumber())
                    .orElseThrow(() -> new RuntimeException("IRP 계좌를 찾을 수 없습니다: " + request.getIrpAccountNumber()));
            
            transactionId = java.util.UUID.randomUUID().toString();
            Transaction transaction = Transaction.builder()
                    .transactionId(transactionId)
                    .account(irpAccountEntity)
                    .transactionType("DEPOSIT_SUB")
                    .amount(request.getSubscriptionAmount().negate()) // 차감이므로 음수
                    .balanceAfter(irpAccount.getCurrentBalance())
                    .transactionDatetime(LocalDateTime.now())
                    .description("정기예금 가입: " + request.getDepositCode())
                    .referenceNumber(depositAccountNumber)
                    .build();
            
            transactionRepository.save(transaction);
            
            log.info("IRP 거래내역 저장 완료: transactionType=DEPOSIT_SUBSCRIPTION, amount={}원", 
                    request.getSubscriptionAmount());
        } catch (Exception e) {
            log.error("IRP 거래내역 저장 실패: {}", e.getMessage(), e);
            // 거래내역 저장 실패는 치명적이지 않으므로 계속 진행
        }

        // 13. 응답 생성
        DepositSubscriptionResponse response = DepositSubscriptionResponse.fromEntity(savedSubscription);
        response.setIrpAccountNumber(request.getIrpAccountNumber());
        response.setPrincipalAmount(request.getSubscriptionAmount());
        response.setExpectedInterest(expectedInterest);
        response.setExpectedMaturityAmount(request.getSubscriptionAmount().add(expectedInterest));

        return response;
    }


    /**
     * 고객의 정기예금 가입 내역 조회
     */
    @Transactional(readOnly = true)
    public List<DepositSubscriptionResponse> getCustomerSubscriptions(String customerCi) {
        log.info("고객 정기예금 가입내역 조회: customerCi={}", customerCi);
        
        List<DepositSubscription> subscriptions = depositSubscriptionRepository
                .findByCustomerCi(customerCi);
        
        return subscriptions.stream()
                .map(DepositSubscriptionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 계좌번호로 가입 내역 조회
     */
    @Transactional(readOnly = true)
    public DepositSubscriptionResponse getSubscriptionByAccountNumber(String accountNumber) {
        log.info("계좌번호로 가입내역 조회: accountNumber={}", accountNumber);
        
        DepositSubscription subscription = depositSubscriptionRepository
                .findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("가입내역을 찾을 수 없습니다: " + accountNumber));
        
        return DepositSubscriptionResponse.fromEntity(subscription);
    }

    /**
     * 중도해지 처리
     */
    @Transactional
    public DepositSubscriptionResponse terminateEarly(String accountNumber) {
        log.info("정기예금 중도해지 요청: accountNumber={}", accountNumber);
        
        DepositSubscription subscription = depositSubscriptionRepository
                .findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("가입내역을 찾을 수 없습니다: " + accountNumber));

        if (!"ACTIVE".equals(subscription.getStatus())) {
            throw new RuntimeException("활성 상태의 예금만 해지할 수 있습니다");
        }

        // 경과 일수 및 계약 일수 계산
        long elapsedDays = InterestRateCalculator.calculateElapsedDays(
                subscription.getSubscriptionDate(), LocalDate.now());
        long contractDays = InterestRateCalculator.calculateContractDays(
                subscription.getSubscriptionDate(), subscription.getMaturityDate());

        // 중도해지 이자율 계산
        BigDecimal earlyRate = InterestRateCalculator.calculateEarlyTerminationRate(
                subscription.getProductType(),
                subscription.getRate(),
                elapsedDays,
                contractDays);

        // 중도해지 이자 계산
        BigDecimal earlyInterest = InterestRateCalculator.calculateEarlyTerminationInterest(
                subscription.getCurrentBalance(),
                earlyRate,
                elapsedDays);

        log.info("중도해지 이자 계산: 원금={}, 중도해지율={}%, 경과일={}, 이자={}원",
                subscription.getCurrentBalance(), earlyRate.multiply(BigDecimal.valueOf(100)),
                elapsedDays, earlyInterest);

        // 상태 변경 및 이자 적용
        subscription.setStatus("CLOSED");
        subscription.calculateInterest(earlyInterest);

        DepositSubscription updatedSubscription = depositSubscriptionRepository.save(subscription);

        log.info("중도해지 처리 완료: accountNumber={}, 중도해지 이자={}원", 
                accountNumber, earlyInterest);

        return DepositSubscriptionResponse.fromEntity(updatedSubscription);
    }

    /**
     * 만기 처리
     */
    @Transactional
    public void processMaturity(String accountNumber) {
        log.info("정기예금 만기 처리: accountNumber={}", accountNumber);
        
        DepositSubscription subscription = depositSubscriptionRepository
                .findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("가입내역을 찾을 수 없습니다: " + accountNumber));

        if (!"ACTIVE".equals(subscription.getStatus())) {
            throw new RuntimeException("활성 상태의 예금만 만기 처리할 수 있습니다");
        }

        // 만기 이자 계산
        int months = subscription.getProductType() == 2 
                ? (int) Math.ceil(subscription.getContractPeriod() / 30.0) 
                : subscription.getContractPeriod();
        
        BigDecimal maturityInterest = InterestRateCalculator.calculateMaturityInterest(
                subscription.getCurrentBalance(),
                subscription.getRate(),
                months);

        log.info("만기 이자 계산: 원금={}, 금리={}%, 기간={}{}, 이자={}원",
                subscription.getCurrentBalance(), 
                subscription.getRate().multiply(BigDecimal.valueOf(100)),
                subscription.getContractPeriod(),
                subscription.getProductType() == 2 ? "일" : "개월",
                maturityInterest);

        // 만기 처리
        subscription.processInterestPayment(maturityInterest);
        subscription.setStatus("MATURED");

        depositSubscriptionRepository.save(subscription);

        log.info("만기 처리 완료: accountNumber={}, 만기 이자={}원, 만기 금액={}원", 
                accountNumber, maturityInterest, subscription.getCurrentBalance());

        // TODO: 자동 재예치 로직 (디폴트옵션의 경우 펀드 포트폴리오 고려)
    }
}

