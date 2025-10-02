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

/**
 * 거래내역 동기화 서비스
 * 은행 서버로부터 거래내역을 조회하여 하나인플랜 DB에 동기화
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TransactionSyncService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final HanaBankClient hanaBankClient;

    /**
     * 특정 계좌의 거래내역 동기화
     */
    public int syncTransactionsByAccount(String accountNumber) {
        log.info("계좌 거래내역 동기화 시작: accountNumber={}", accountNumber);

        try {
            // 1. 계좌 정보 조회
            Optional<BankingAccount> accountOpt = accountRepository.findByAccountNumber(accountNumber);
            if (accountOpt.isEmpty()) {
                log.warn("계좌를 찾을 수 없음: {}", accountNumber);
                return 0;
            }

            BankingAccount account = accountOpt.get();

            // 2. 은행 식별
            String bankCode = getBankCodeFromAccountNumber(accountNumber);

            // 3. 은행별 Feign Client 호출하여 거래내역 조회 (OpenFeign 사용)
            List<HanaBankClient.BankTransactionDto> bankTransactions;
            switch (bankCode) {
                case "081": // 하나은행
                    bankTransactions = hanaBankClient.getTransactionsByAccount(accountNumber);
                    break;
                // 추후 다른 은행 Feign Client 추가 가능
                default:
                    log.warn("지원하지 않는 은행: bankCode={}", bankCode);
                    return 0;
            }

            if (bankTransactions == null || bankTransactions.isEmpty()) {
                log.info("조회된 거래내역 없음");
                return 0;
            }

            log.info("은행에서 조회된 거래내역 수: {}", bankTransactions.size());

            // 4. 하나인플랜 DB에서 마지막 거래내역 조회
            List<Transaction> existingTransactions = transactionRepository
                    .findTop1ByAccountAccountNumberOrderByTransactionDateDesc(accountNumber);

            LocalDateTime lastTransactionDate = null;
            if (!existingTransactions.isEmpty()) {
                lastTransactionDate = existingTransactions.get(0).getTransactionDate();
                log.info("마지막 동기화된 거래 시각: {}", lastTransactionDate);
            }

            // 5. 새로운 거래내역만 필터링하여 저장
            int syncedCount = 0;
            LocalDateTime finalLastTransactionDate = lastTransactionDate;

            for (HanaBankClient.BankTransactionDto bankTx : bankTransactions) {
                // 마지막 거래 이후의 거래만 동기화
                if (finalLastTransactionDate == null || 
                    bankTx.getTransactionDatetime().isAfter(finalLastTransactionDate)) {
                    
                    // 거래 방향에 따라 fromAccountId, toAccountId 설정
                    Long fromAccountId = null;
                    Long toAccountId = null;
                    
                    if ("DEBIT".equals(bankTx.getTransactionDirection())) {
                        // 출금인 경우
                        fromAccountId = account.getAccountId();
                    } else if ("CREDIT".equals(bankTx.getTransactionDirection())) {
                        // 입금인 경우
                        toAccountId = account.getAccountId();
                    }
                    
                    // Transaction 엔터티로 변환하여 저장
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

    /**
     * 사용자의 모든 계좌 거래내역 동기화
     */
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
                // 한 계좌 실패해도 계속 진행
            }
        }

        log.info("사용자 전체 계좌 거래내역 동기화 완료: userId={}, totalSyncedCount={}", userId, totalSyncedCount);
        return totalSyncedCount;
    }

    /**
     * 계좌번호에서 은행코드 추출
     */
    private String getBankCodeFromAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 3) {
            return null;
        }

        String cleanAccountNumber = accountNumber.replace("-", "");
        String prefix = cleanAccountNumber.substring(0, 3);

        // 은행코드 매핑
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

    /**
     * 거래 유형 변환 (String -> Enum)
     */
    private Transaction.TransactionType convertTransactionType(String type) {
        try {
            return Transaction.TransactionType.valueOf(type);
        } catch (Exception e) {
            log.warn("Unknown transaction type: {}", type);
            return Transaction.TransactionType.TRANSFER; // 기본값: 이체
        }
    }

    /**
     * 거래 방향 변환 (String -> Enum)
     */
    private Transaction.TransactionDirection convertTransactionDirection(String direction) {
        try {
            return Transaction.TransactionDirection.valueOf(direction);
        } catch (Exception e) {
            log.warn("Unknown transaction direction: {}", direction);
            return Transaction.TransactionDirection.CREDIT;
        }
    }

    /**
     * 거래 상태 변환 (String -> Enum)
     */
    private Transaction.TransactionStatus convertTransactionStatus(String status) {
        try {
            return Transaction.TransactionStatus.valueOf(status);
        } catch (Exception e) {
            log.warn("Unknown transaction status: {}", status);
            return Transaction.TransactionStatus.COMPLETED;
        }
    }

    /**
     * 거래 분류 변환 (String -> Enum)
     */
    private Transaction.TransactionCategory convertTransactionCategory(String category) {
        try {
            return Transaction.TransactionCategory.valueOf(category);
        } catch (Exception e) {
            log.warn("Unknown transaction category: {}", category);
            return Transaction.TransactionCategory.OTHER; // 기본값: 기타
        }
    }

}

