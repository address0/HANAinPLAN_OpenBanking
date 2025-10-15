package com.hanainplan.hana.product.service;

import com.hanainplan.hana.product.entity.DepositSubscription;
import com.hanainplan.hana.product.repository.DepositSubscriptionRepository;
import com.hanainplan.hana.product.util.InterestRateCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepositSchedulerService {

    private final DepositSubscriptionRepository depositSubscriptionRepository;
    private final com.hanainplan.hana.account.repository.AccountRepository accountRepository;
    private final com.hanainplan.hana.account.repository.TransactionRepository transactionRepository;
    
    @Autowired(required = false)
    private com.hanainplan.domain.banking.service.TaxCalculationService taxCalculationService;

    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    public void processMaturityDeposits() {
        log.info("=== 정기예금 만기 처리 스케줄러 시작 ===");

        LocalDate today = LocalDate.now();

        List<DepositSubscription> maturingDeposits = depositSubscriptionRepository
                .findByMaturityDateAndStatus(today, "ACTIVE");

        log.info("만기 처리 대상: {}건", maturingDeposits.size());

        int successCount = 0;
        int failCount = 0;

        for (DepositSubscription deposit : maturingDeposits) {
            try {
                processMaturity(deposit);
                successCount++;

                log.info("만기 처리 완료 - 계좌: {}, 원금: {}, 이자: {}", 
                        deposit.getAccountNumber(), 
                        deposit.getCurrentBalance().subtract(deposit.getUnpaidInterest()),
                        deposit.getUnpaidInterest());

            } catch (Exception e) {
                failCount++;
                log.error("만기 처리 실패 - 계좌: {}, 오류: {}", 
                        deposit.getAccountNumber(), e.getMessage(), e);
            }
        }

        log.info("=== 정기예금 만기 처리 완료 - 성공: {}건, 실패: {}건 ===", successCount, failCount);
    }

    @Scheduled(cron = "0 0 2 1 * ?")
    @Transactional
    public void processMonthlyInterestPayments() {
        log.info("=== 정기예금 월간 이자 지급 스케줄러 시작 ===");

        List<DepositSubscription> activeDeposits = depositSubscriptionRepository
                .findByStatus("ACTIVE");

        log.info("이자 계산 대상: {}건", activeDeposits.size());

        int successCount = 0;
        int failCount = 0;

        for (DepositSubscription deposit : activeDeposits) {
            try {
                LocalDate lastCalculationDate = deposit.getLastInterestCalculationDate();
                if (lastCalculationDate == null) {
                    lastCalculationDate = deposit.getSubscriptionDate();
                }

                long elapsedDays = InterestRateCalculator.calculateElapsedDays(
                        lastCalculationDate, LocalDate.now());

                if (elapsedDays >= 30) {
                    calculateAndRecordInterest(deposit, elapsedDays);
                    successCount++;

                    log.debug("이자 계산 완료 - 계좌: {}, 경과일: {}, 누적이자: {}", 
                            deposit.getAccountNumber(), elapsedDays, deposit.getUnpaidInterest());
                }

            } catch (Exception e) {
                failCount++;
                log.error("이자 계산 실패 - 계좌: {}, 오류: {}", 
                        deposit.getAccountNumber(), e.getMessage(), e);
            }
        }

        log.info("=== 정기예금 월간 이자 지급 완료 - 성공: {}건, 실패: {}건 ===", successCount, failCount);
    }

    private void processMaturity(DepositSubscription deposit) {
        int months = deposit.getProductType() == 2 
                ? (int) Math.ceil(deposit.getContractPeriod() / 30.0) 
                : deposit.getContractPeriod();

        BigDecimal principal = deposit.getCurrentBalance().subtract(
                deposit.getUnpaidInterest() != null ? deposit.getUnpaidInterest() : BigDecimal.ZERO);

        BigDecimal maturityInterest = InterestRateCalculator.calculateMaturityInterest(
                principal,
                deposit.getRate(),
                months);

        log.info("만기 이자 계산 - 계좌: {}, 원금: {}, 금리: {}%, 기간: {}{}, 이자: {}", 
                deposit.getAccountNumber(), principal, 
                deposit.getRate().multiply(BigDecimal.valueOf(100)),
                deposit.getContractPeriod(),
                deposit.getProductType() == 2 ? "일" : "개월",
                maturityInterest);

        if (taxCalculationService != null) {
            com.hanainplan.domain.banking.dto.TaxResult taxResult = 
                    taxCalculationService.calculateInterestTax(maturityInterest);
            
            log.info("세금 계산 - 세전: {}, 세액: {}, 세후: {}", 
                    taxResult.getGrossAmount(), taxResult.getTotalTax(), taxResult.getNetAmount());
            
            deposit.processInterestPaymentWithTax(
                    taxResult.getGrossAmount(),
                    taxResult.getTotalTax(),
                    taxResult.getNetAmount()
            );
            
            createMaturityTransactions(deposit, taxResult);
        } else {
            deposit.processInterestPayment(maturityInterest);
            createSimpleInterestTransaction(deposit, maturityInterest);
        }

        deposit.setStatus("MATURED");

        depositSubscriptionRepository.save(deposit);

        if (deposit.getProductType() == 1) {
            log.info("디폴트옵션 상품 만기 - 자동 재예치 대상: {}", deposit.getAccountNumber());
        }
    }

    private void calculateAndRecordInterest(DepositSubscription deposit, long elapsedDays) {
        BigDecimal principal = deposit.getCurrentBalance().subtract(
                deposit.getUnpaidInterest() != null ? deposit.getUnpaidInterest() : BigDecimal.ZERO);

        BigDecimal dailyInterest = principal
                .multiply(deposit.getRate())
                .multiply(BigDecimal.valueOf(elapsedDays))
                .divide(BigDecimal.valueOf(365), 2, java.math.RoundingMode.DOWN);

        deposit.calculateInterest(dailyInterest);
        deposit.setLastInterestCalculationDate(LocalDate.now());

        depositSubscriptionRepository.save(deposit);
    }

    @Transactional
    public void processMaturityByAccountNumber(String accountNumber) {
        log.info("수동 만기 처리 요청: {}", accountNumber);

        DepositSubscription deposit = depositSubscriptionRepository
                .findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("예금 계좌를 찾을 수 없습니다: " + accountNumber));

        if (!"ACTIVE".equals(deposit.getStatus())) {
            throw new RuntimeException("활성 상태의 예금만 만기 처리할 수 있습니다");
        }

        processMaturity(deposit);

        log.info("수동 만기 처리 완료: {}", accountNumber);
    }

    @Transactional
    public void processDailyInterestTest() {
        log.info("=== [테스트] 일일 이자 지급 스케줄러 시작 ===");

        List<DepositSubscription> activeDeposits = depositSubscriptionRepository
                .findByStatus("ACTIVE");

        for (DepositSubscription deposit : activeDeposits) {
            try {
                LocalDate lastCalculationDate = deposit.getLastInterestCalculationDate();
                if (lastCalculationDate == null) {
                    lastCalculationDate = deposit.getSubscriptionDate();
                }

                long elapsedDays = InterestRateCalculator.calculateElapsedDays(
                        lastCalculationDate, LocalDate.now());

                if (elapsedDays > 0) {
                    calculateAndRecordInterest(deposit, elapsedDays);
                }
            } catch (Exception e) {
                log.error("일일 이자 계산 실패: {}", deposit.getAccountNumber(), e);
            }
        }

        log.info("=== [테스트] 일일 이자 지급 완료 ===");
    }

    private void createMaturityTransactions(DepositSubscription deposit, 
                                           com.hanainplan.domain.banking.dto.TaxResult taxResult) {
        try {
            com.hanainplan.hana.account.entity.Account account = accountRepository
                    .findByAccountNumber(deposit.getAccountNumber())
                    .orElse(null);
            
            if (account == null) {
                log.warn("계좌를 찾을 수 없어 거래내역 생성 생략 - 계좌번호: {}", deposit.getAccountNumber());
                return;
            }

            String baseTransactionId = "MATURITY-" + System.currentTimeMillis();
            java.time.LocalDateTime now = java.time.LocalDateTime.now();

            com.hanainplan.hana.account.entity.Transaction interestDeposit = 
                    com.hanainplan.hana.account.entity.Transaction.builder()
                    .transactionId(baseTransactionId + "-INTEREST")
                    .accountNumber(deposit.getAccountNumber())
                    .transactionDatetime(now)
                    .transactionType("입금")
                    .transactionCategory("이자")
                    .transactionStatus("COMPLETED")
                    .transactionDirection("CREDIT")
                    .amount(taxResult.getGrossAmount())
                    .balanceAfter(account.getBalance().add(taxResult.getNetAmount()))
                    .description("정기예금 만기 이자 (세전)")
                    .branchName("하나은행 본점")
                    .referenceNumber(baseTransactionId)
                    .account(account)
                    .build();

            transactionRepository.save(interestDeposit);
            log.info("이자 입금 거래내역 생성 완료 - 금액: {}원", taxResult.getGrossAmount());

            com.hanainplan.hana.account.entity.Transaction taxWithdrawal = 
                    com.hanainplan.hana.account.entity.Transaction.builder()
                    .transactionId(baseTransactionId + "-TAX")
                    .accountNumber(deposit.getAccountNumber())
                    .transactionDatetime(now)
                    .transactionType("출금")
                    .transactionCategory("세금")
                    .transactionStatus("COMPLETED")
                    .transactionDirection("DEBIT")
                    .amount(taxResult.getTotalTax())
                    .balanceAfter(account.getBalance().add(taxResult.getNetAmount()))
                    .description(String.format("이자소득세 원천징수 (소득세: %s원, 지방세: %s원)", 
                            taxResult.getIncomeTax(), taxResult.getLocalTax()))
                    .branchName("하나은행 본점")
                    .referenceNumber(baseTransactionId)
                    .account(account)
                    .build();

            transactionRepository.save(taxWithdrawal);
            log.info("세금 출금 거래내역 생성 완료 - 금액: {}원 (소득세: {}, 지방세: {})", 
                    taxResult.getTotalTax(), taxResult.getIncomeTax(), taxResult.getLocalTax());

            account.setBalance(account.getBalance().add(taxResult.getNetAmount()));
            accountRepository.save(account);

        } catch (Exception e) {
            log.error("만기 거래내역 생성 실패 - 계좌: {}", deposit.getAccountNumber(), e);
        }
    }

    private void createSimpleInterestTransaction(DepositSubscription deposit, BigDecimal interestAmount) {
        try {
            com.hanainplan.hana.account.entity.Account account = accountRepository
                    .findByAccountNumber(deposit.getAccountNumber())
                    .orElse(null);
            
            if (account == null) {
                log.warn("계좌를 찾을 수 없어 거래내역 생성 생략 - 계좌번호: {}", deposit.getAccountNumber());
                return;
            }

            String transactionId = "MATURITY-" + System.currentTimeMillis() + "-INTEREST";

            com.hanainplan.hana.account.entity.Transaction transaction = 
                    com.hanainplan.hana.account.entity.Transaction.builder()
                    .transactionId(transactionId)
                    .accountNumber(deposit.getAccountNumber())
                    .transactionDatetime(java.time.LocalDateTime.now())
                    .transactionType("입금")
                    .transactionCategory("이자")
                    .transactionStatus("COMPLETED")
                    .transactionDirection("CREDIT")
                    .amount(interestAmount)
                    .balanceAfter(account.getBalance().add(interestAmount))
                    .description("정기예금 만기 이자")
                    .branchName("하나은행 본점")
                    .referenceNumber(transactionId)
                    .account(account)
                    .build();

            transactionRepository.save(transaction);
            log.info("이자 입금 거래내역 생성 완료 - 금액: {}원", interestAmount);

            account.setBalance(account.getBalance().add(interestAmount));
            accountRepository.save(account);

        } catch (Exception e) {
            log.error("거래내역 생성 실패 - 계좌: {}", deposit.getAccountNumber(), e);
        }
    }
}