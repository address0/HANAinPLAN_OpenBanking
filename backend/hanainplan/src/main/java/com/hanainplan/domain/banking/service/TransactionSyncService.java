package com.hanainplan.domain.banking.service;

import com.hanainplan.domain.banking.client.HanaBankClient;
import com.hanainplan.domain.banking.entity.BankingAccount;
import com.hanainplan.domain.banking.entity.Transaction;
import com.hanainplan.domain.banking.repository.AccountRepository;
import com.hanainplan.domain.banking.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TransactionSyncService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final HanaBankClient hanaBankClient;

    public int syncTransactionsByAccount(String accountNumber) {
        log.info("계좌 거래내역 동기화 시작: accountNumber={}", accountNumber);

        try {
            Optional<BankingAccount> accountOpt = accountRepository.findByAccountNumber(accountNumber);
            if (accountOpt.isEmpty()) {
                log.warn("계좌를 찾을 수 없음: {}", accountNumber);
                return 0;
            }

            BankingAccount account = accountOpt.get();

            String bankCode = getBankCodeFromAccountNumber(accountNumber);

            List<HanaBankClient.BankTransactionDto> bankTransactions;
            switch (bankCode) {
                case "081":
                    bankTransactions = hanaBankClient.getTransactionsByAccount(accountNumber);
                    break;
                default:
                    log.warn("지원하지 않는 은행: bankCode={}", bankCode);
                    return 0;
            }

            if (bankTransactions == null || bankTransactions.isEmpty()) {
                log.info("조회된 거래내역 없음");
                return 0;
            }

            log.info("은행에서 조회된 거래내역 수: {}", bankTransactions.size());

            List<Transaction> existingTransactions = transactionRepository
                    .findTop1ByAccountAccountNumberOrderByTransactionDateDesc(accountNumber);

            LocalDateTime lastTransactionDate = null;
            if (!existingTransactions.isEmpty()) {
                lastTransactionDate = existingTransactions.get(0).getTransactionDate();
                log.info("마지막 동기화된 거래 시각: {}", lastTransactionDate);
            }

            int syncedCount = 0;
            LocalDateTime finalLastTransactionDate = lastTransactionDate;

            for (HanaBankClient.BankTransactionDto bankTx : bankTransactions) {
                if (finalLastTransactionDate == null || 
                    bankTx.getTransactionDatetime().isAfter(finalLastTransactionDate)) {

                    Long fromAccountId = null;
                    Long toAccountId = null;

                    if ("DEBIT".equals(bankTx.getTransactionDirection())) {
                        fromAccountId = account.getAccountId();
                    } else if ("CREDIT".equals(bankTx.getTransactionDirection())) {
                        toAccountId = account.getAccountId();
                    }

                    Transaction transaction = Transaction.builder()
                            .fromAccountId(fromAccountId)
                            .toAccountId(toAccountId)
                            .transactionNumber(bankTx.getTransactionNumber())
                            .transactionType(convertTransactionType(bankTx.getTransactionType()))
                            .transactionDirection(convertTransactionDirection(bankTx.getTransactionDirection()))
                            .amount(bankTx.getAmount())
                            .balanceAfter(bankTx.getBalanceAfter())
                            .transactionDate(bankTx.getTransactionDatetime())
                            .processedDate(bankTx.getProcessedDate())
                            .transactionStatus(convertTransactionStatus(bankTx.getTransactionStatus()))
                            .transactionCategory(convertTransactionCategory(bankTx.getTransactionCategory()))
                            .description(bankTx.getDescription())
                            .memo(bankTx.getMemo())
                            .referenceNumber(bankTx.getReferenceNumber())
                            .build();

                    transactionRepository.save(transaction);
                    syncedCount++;
                }
            }

            log.info("거래내역 동기화 완료: accountNumber={}, syncedCount={}", accountNumber, syncedCount);
            return syncedCount;

        } catch (Exception e) {
            log.error("거래내역 동기화 실패: accountNumber={}, error={}", accountNumber, e.getMessage(), e);
            throw new RuntimeException("거래내역 동기화 실패: " + e.getMessage());
        }
    }

    public int syncAllTransactionsByUser(Long userId) {
        log.info("사용자 전체 계좌 거래내역 동기화 시작: userId={}", userId);

        List<BankingAccount> accounts = accountRepository.findByUserIdOrderByCreatedAtDesc(userId);
        int totalSyncedCount = 0;

        for (BankingAccount account : accounts) {
            try {
                int syncedCount = syncTransactionsByAccount(account.getAccountNumber());
                totalSyncedCount += syncedCount;
            } catch (Exception e) {
                log.error("계좌 거래내역 동기화 실패: accountNumber={}, error={}", 
                        account.getAccountNumber(), e.getMessage());
            }
        }

        log.info("사용자 전체 계좌 거래내역 동기화 완료: userId={}, totalSyncedCount={}", userId, totalSyncedCount);
        return totalSyncedCount;
    }

    private String getBankCodeFromAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 3) {
            return null;
        }

        String cleanAccountNumber = accountNumber.replace("-", "");
        String prefix = cleanAccountNumber.substring(0, 3);

        switch (prefix) {
            case "081":
            case "117":
                return "HANA";
            case "088":
                return "SHINHAN";
            case "004":
                return "KOOKMIN";
            default:
                log.warn("알 수 없는 은행코드: prefix={}", prefix);
                return null;
        }
    }

    private Transaction.TransactionType convertTransactionType(String type) {
        try {
            return Transaction.TransactionType.valueOf(type);
        } catch (Exception e) {
            log.warn("Unknown transaction type: {}", type);
            return Transaction.TransactionType.TRANSFER;
        }
    }

    private Transaction.TransactionDirection convertTransactionDirection(String direction) {
        try {
            return Transaction.TransactionDirection.valueOf(direction);
        } catch (Exception e) {
            log.warn("Unknown transaction direction: {}", direction);
            return Transaction.TransactionDirection.CREDIT;
        }
    }

    private Transaction.TransactionStatus convertTransactionStatus(String status) {
        try {
            return Transaction.TransactionStatus.valueOf(status);
        } catch (Exception e) {
            log.warn("Unknown transaction status: {}", status);
            return Transaction.TransactionStatus.COMPLETED;
        }
    }

    private Transaction.TransactionCategory convertTransactionCategory(String category) {
        try {
            return Transaction.TransactionCategory.valueOf(category);
        } catch (Exception e) {
            log.warn("Unknown transaction category: {}", category);
            return Transaction.TransactionCategory.OTHER;
        }
    }

}