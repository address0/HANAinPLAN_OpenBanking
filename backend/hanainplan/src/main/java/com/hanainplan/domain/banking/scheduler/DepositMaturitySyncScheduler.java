package com.hanainplan.domain.banking.scheduler;

import com.hanainplan.domain.banking.entity.BankingAccount;
import com.hanainplan.domain.banking.entity.DepositSubscription;
import com.hanainplan.domain.banking.entity.Transaction;
import com.hanainplan.domain.banking.repository.AccountRepository;
import com.hanainplan.domain.banking.repository.DepositSubscriptionRepository;
import com.hanainplan.domain.banking.repository.TransactionRepository;
import com.hanainplan.domain.banking.service.TaxCalculationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DepositMaturitySyncScheduler {

    private final DepositSubscriptionRepository depositSubscriptionRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TaxCalculationService taxCalculationService;

    @Scheduled(cron = "0 30 1 * * ?")
    @Transactional
    public void syncMaturityTransactions() {
        log.info("=== HANAinPLAN 정기예금 만기 거래내역 동기화 시작 ===");

        LocalDate today = LocalDate.now();

        List<DepositSubscription> maturedDeposits = depositSubscriptionRepository
                .findByMaturityDateAndStatus(today, "ACTIVE");

        log.info("만기 거래내역 동기화 대상: {}건", maturedDeposits.size());

        int successCount = 0;
        int failCount = 0;

        for (DepositSubscription deposit : maturedDeposits) {
            try {
                createMaturityTransactions(deposit);
                successCount++;
            } catch (Exception e) {
                failCount++;
                log.error("만기 거래내역 동기화 실패 - subscriptionId: {}", 
                        deposit.getSubscriptionId(), e);
            }
        }

        log.info("=== 만기 거래내역 동기화 완료 - 성공: {}건, 실패: {}건 ===", 
                successCount, failCount);
    }

    private void createMaturityTransactions(DepositSubscription deposit) {
        BankingAccount account = accountRepository.findByAccountNumber(deposit.getAccountNumber())
                .orElseThrow(() -> new RuntimeException("계좌를 찾을 수 없습니다: " + deposit.getAccountNumber()));

        String baseTransactionNumber = Transaction.generateTransactionNumber();
        LocalDateTime now = LocalDateTime.now();

        BigDecimal grossInterest = deposit.getGrossInterest() != null 
                ? deposit.getGrossInterest() : deposit.getUnpaidInterest();
        BigDecimal taxAmount = deposit.getTaxAmount() != null 
                ? deposit.getTaxAmount() : BigDecimal.ZERO;
        BigDecimal netInterest = deposit.getNetInterest() != null 
                ? deposit.getNetInterest() : grossInterest;

        if (grossInterest != null && grossInterest.compareTo(BigDecimal.ZERO) > 0) {
            if (taxAmount.compareTo(BigDecimal.ZERO) > 0) {
                Transaction interestTransaction = Transaction.builder()
                        .transactionNumber(baseTransactionNumber + "-INT")
                        .toAccountId(account.getAccountId())
                        .toAccountNumber(account.getAccountNumber())
                        .transactionType(Transaction.TransactionType.DEPOSIT)
                        .transactionCategory(Transaction.TransactionCategory.INTEREST)
                        .transactionDirection(Transaction.TransactionDirection.CREDIT)
                        .amount(grossInterest)
                        .balanceAfter(account.getBalance())
                        .description(String.format("정기예금 만기 이자 (세전) - %s", deposit.getDepositCode()))
                        .transactionStatus(Transaction.TransactionStatus.COMPLETED)
                        .transactionDate(now)
                        .processedDate(now)
                        .referenceNumber(baseTransactionNumber)
                        .build();

                transactionRepository.save(interestTransaction);

                Transaction taxTransaction = Transaction.builder()
                        .transactionNumber(baseTransactionNumber + "-TAX")
                        .fromAccountId(account.getAccountId())
                        .fromAccountNumber(account.getAccountNumber())
                        .transactionType(Transaction.TransactionType.WITHDRAWAL)
                        .transactionCategory(Transaction.TransactionCategory.TAX)
                        .transactionDirection(Transaction.TransactionDirection.DEBIT)
                        .amount(taxAmount)
                        .balanceAfter(account.getBalance())
                        .description("이자소득세 원천징수 (15.4%)")
                        .transactionStatus(Transaction.TransactionStatus.COMPLETED)
                        .transactionDate(now)
                        .processedDate(now)
                        .referenceNumber(baseTransactionNumber)
                        .memo(String.format("소득세: %s원, 지방세: %s원", 
                                grossInterest.multiply(new BigDecimal("0.14")),
                                grossInterest.multiply(new BigDecimal("0.014"))))
                        .build();

            transactionRepository.save(taxTransaction);

            account.setBalance(account.getBalance().add(netInterest));
            accountRepository.save(account);

            log.info("만기 거래내역 생성 완료 - 계좌: {}, 이자: {}원, 세금: {}원, 실수령: {}원, 새 잔액: {}원",
                    deposit.getAccountNumber(), grossInterest, taxAmount, netInterest, account.getBalance());
            } else {
                Transaction interestTransaction = Transaction.builder()
                        .transactionNumber(baseTransactionNumber + "-INT")
                        .toAccountId(account.getAccountId())
                        .toAccountNumber(account.getAccountNumber())
                        .transactionType(Transaction.TransactionType.DEPOSIT)
                        .transactionCategory(Transaction.TransactionCategory.INTEREST)
                        .transactionDirection(Transaction.TransactionDirection.CREDIT)
                        .amount(netInterest)
                        .balanceAfter(account.getBalance())
                        .description(String.format("정기예금 만기 이자 - %s", deposit.getDepositCode()))
                        .transactionStatus(Transaction.TransactionStatus.COMPLETED)
                        .transactionDate(now)
                        .processedDate(now)
                        .referenceNumber(baseTransactionNumber)
                        .build();

            transactionRepository.save(interestTransaction);

            account.setBalance(account.getBalance().add(netInterest));
            accountRepository.save(account);

            log.info("만기 거래내역 생성 완료 - 계좌: {}, 이자: {}원, 새 잔액: {}원",
                    deposit.getAccountNumber(), netInterest, account.getBalance());
            }
        }
    }
}

