package com.hanainplan.domain.banking.service;

import com.hanainplan.domain.banking.client.HanaBankClient;
import com.hanainplan.domain.banking.entity.BankingAccount;
import com.hanainplan.domain.banking.entity.IrpAccount;
import com.hanainplan.domain.banking.entity.Transaction;
import com.hanainplan.domain.banking.repository.AccountRepository;
import com.hanainplan.domain.banking.repository.IrpAccountRepository;
import com.hanainplan.domain.banking.repository.TransactionRepository;
import com.hanainplan.domain.user.entity.User;
import com.hanainplan.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 계좌 동기화 서비스
 * - 하나은행 서버와 하나인플랜 백엔드 간 계좌 및 거래내역 동기화
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountSyncService {

    private final HanaBankClient hanaBankClient;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final IrpAccountRepository irpAccountRepository;
    private final UserRepository userRepository;

    /**
     * 사용자의 모든 계좌를 하나은행 서버와 동기화
     */
    @Transactional
    public void syncUserAccounts(Long userId) {
        log.info("계좌 동기화 시작 - 사용자 ID: {}", userId);

        try {
            // 1. 사용자 정보 조회
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                log.warn("사용자를 찾을 수 없습니다 - 사용자 ID: {}", userId);
                return;
            }

            User user = userOpt.get();
            String customerCi = user.getCi();

            if (customerCi == null || customerCi.isEmpty()) {
                log.warn("사용자 CI 정보가 없습니다 - 사용자 ID: {}", userId);
                return;
            }

            // 2. 하나은행 서버에서 계좌 정보 조회
            Map<String, Object> bankResponse = hanaBankClient.getCustomerAccountsByCi(customerCi);
            
            if (bankResponse == null) {
                log.warn("하나은행 서버 응답이 null입니다 - CI: {}", customerCi);
                return;
            }

            // 3. 계좌 목록 파싱
            Object accountsObj = bankResponse.get("accounts");
            if (!(accountsObj instanceof List)) {
                log.warn("계좌 목록 형식이 올바르지 않습니다 - CI: {}", customerCi);
                return;
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> bankAccounts = (List<Map<String, Object>>) accountsObj;
            
            log.info("하나은행에서 조회된 계좌 수: {} - CI: {}", bankAccounts.size(), customerCi);

            // 4. 각 계좌를 동기화
            for (Map<String, Object> bankAccount : bankAccounts) {
                syncSingleAccount(userId, customerCi, bankAccount);
            }

            log.info("계좌 동기화 완료 - 사용자 ID: {}, 동기화된 계좌 수: {}", userId, bankAccounts.size());
            
            // 5. IRP 계좌 동기화
            syncIrpAccount(userId, customerCi);

        } catch (Exception e) {
            log.error("계좌 동기화 실패 - 사용자 ID: {}, 오류: {}", userId, e.getMessage(), e);
            // 동기화 실패해도 예외를 던지지 않음 (기존 데이터는 유지)
        }
    }

    /**
     * IRP 계좌 동기화
     * - 하나은행에서 IRP 계좌 정보를 가져와서 irp_account 테이블에 복사/업데이트
     */
    private void syncIrpAccount(Long userId, String customerCi) {
        try {
            log.info("IRP 계좌 동기화 시작 - 사용자 ID: {}", userId);
            
            // 하나은행에서 계좌 정보 조회
            Map<String, Object> bankResponse = hanaBankClient.getCustomerAccountsByCi(customerCi);
            
            if (bankResponse == null) {
                log.warn("하나은행 서버 응답이 null입니다 - CI: {}", customerCi);
                return;
            }
            
            Object accountsObj = bankResponse.get("accounts");
            if (!(accountsObj instanceof List)) {
                log.warn("계좌 목록 형식이 올바르지 않습니다 - CI: {}", customerCi);
                return;
            }
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> bankAccounts = (List<Map<String, Object>>) accountsObj;
            
            // IRP 계좌 찾기 (accountType = 6 또는 accountName에 IRP 포함)
            Map<String, Object> irpBankAccount = null;
            for (Map<String, Object> bankAccount : bankAccounts) {
                Integer accountType = parseInteger(bankAccount.get("accountType"));
                String accountName = (String) bankAccount.get("accountName");
                
                // accountType이 6이거나 accountName에 "IRP"가 포함되어 있으면 IRP 계좌
                if ((accountType != null && accountType == 6) || 
                    (accountName != null && accountName.contains("IRP"))) {
                    irpBankAccount = bankAccount;
                    break;
                }
            }
            
            if (irpBankAccount == null) {
                log.info("하나은행에 IRP 계좌가 없습니다 - 사용자 ID: {}", userId);
                return;
            }
            
            // 하나은행 IRP 계좌번호
            String irpAccountNumber = (String) irpBankAccount.get("accountNumber");
            BigDecimal balance = parseBigDecimal(irpBankAccount.get("balance"));
            
            if (irpAccountNumber == null || irpAccountNumber.isEmpty()) {
                log.warn("IRP 계좌번호가 없습니다 - CI: {}", customerCi);
                return;
            }
            
            log.info("하나은행 IRP 계좌 발견 - 계좌번호: {}, 잔액: {}", irpAccountNumber, balance);
            
            // 하나인플랜 IRP 계좌 조회
            List<IrpAccount> existingIrpAccounts = irpAccountRepository.findByCustomerCiOrderByCreatedDateDesc(customerCi);
            
            if (existingIrpAccounts.isEmpty()) {
                // 하나인플랜에 IRP 계좌가 없으면 새로 생성 (하나은행에서 복사)
                log.info("하나인플랜에 IRP 계좌가 없습니다. 하나은행 데이터를 복사합니다 - 계좌번호: {}", irpAccountNumber);
                
                IrpAccount newIrpAccount = IrpAccount.builder()
                        .customerId(userId)
                        .customerCi(customerCi)
                        .bankCode("HANA")
                        .accountNumber(irpAccountNumber)
                        .accountStatus("ACTIVE")
                        .initialDeposit(balance)
                        .currentBalance(balance)
                        .totalContribution(balance)
                        .monthlyDeposit(BigDecimal.ZERO)
                        .isAutoDeposit(false)
                        .investmentStyle("CONSERVATIVE")
                        .productCode("IRP001")
                        .productName("하나은행 IRP")
                        .openDate(java.time.LocalDate.now())
                        .lastContributionDate(java.time.LocalDate.now())
                        .syncStatus("SUCCESS")
                        .externalAccountId(irpAccountNumber)
                        .externalLastUpdated(java.time.LocalDateTime.now())
                        .build();
                
                irpAccountRepository.save(newIrpAccount);
                log.info("하나은행 IRP 계좌를 irp_account 테이블에 복사 완료 - 계좌번호: {}, 잔액: {}", 
                        irpAccountNumber, balance);
                
            } else {
                // 이미 존재하면 잔액만 업데이트
                IrpAccount existingIrpAccount = existingIrpAccounts.get(0);
                
                if (balance != null && balance.compareTo(existingIrpAccount.getCurrentBalance()) != 0) {
                    log.info("IRP 계좌 잔액 업데이트 - 계좌번호: {}, 이전 잔액: {}, 새 잔액: {}", 
                            irpAccountNumber, existingIrpAccount.getCurrentBalance(), balance);
                    existingIrpAccount.setCurrentBalance(balance);
                    existingIrpAccount.setExternalLastUpdated(java.time.LocalDateTime.now());
                    irpAccountRepository.save(existingIrpAccount);
                }
            }
            
            // IRP 계좌의 거래내역도 동기화
            syncAccountTransactions(irpAccountNumber);
            
            log.info("IRP 계좌 동기화 완료 - 사용자 ID: {}, 계좌번호: {}", userId, irpAccountNumber);
            
        } catch (Exception e) {
            log.error("IRP 계좌 동기화 실패 - 사용자 ID: {}, 오류: {}", userId, e.getMessage(), e);
        }
    }
    
    /**
     * 단일 계좌 동기화
     */
    private void syncSingleAccount(Long userId, String customerCi, Map<String, Object> bankAccount) {
        try {
            String accountNumber = (String) bankAccount.get("accountNumber");
            if (accountNumber == null || accountNumber.isEmpty()) {
                log.warn("계좌번호가 없습니다 - 은행 응답: {}", bankAccount);
                return;
            }

            // 1. 하나인플랜 DB에서 계좌 조회
            Optional<BankingAccount> existingAccountOpt = accountRepository.findByAccountNumber(accountNumber);

            if (existingAccountOpt.isPresent()) {
                // 계좌가 존재하면 잔액만 업데이트
                BankingAccount existingAccount = existingAccountOpt.get();
                BigDecimal newBalance = parseBigDecimal(bankAccount.get("balance"));
                
                if (newBalance != null && newBalance.compareTo(existingAccount.getBalance()) != 0) {
                    log.info("계좌 잔액 업데이트 - 계좌번호: {}, 이전 잔액: {}, 새 잔액: {}", 
                            accountNumber, existingAccount.getBalance(), newBalance);
                    existingAccount.setBalance(newBalance);
                    accountRepository.save(existingAccount);
                }
            } else {
                // 계좌가 없으면 새로 생성
                log.info("새 계좌 생성 - 계좌번호: {}", accountNumber);
                createNewAccount(userId, customerCi, bankAccount);
            }

            // 2. 거래내역 동기화
            syncAccountTransactions(accountNumber);

        } catch (Exception e) {
            log.error("계좌 동기화 실패 - 계좌번호: {}, 오류: {}", 
                    bankAccount.get("accountNumber"), e.getMessage(), e);
        }
    }

    /**
     * 새 계좌 생성
     */
    private void createNewAccount(Long userId, String customerCi, Map<String, Object> bankAccount) {
        String accountNumber = (String) bankAccount.get("accountNumber");
        String accountName = (String) bankAccount.getOrDefault("accountName", "입출금통장");
        Integer accountType = parseInteger(bankAccount.getOrDefault("accountType", 1)); // 기본값: 입출금(1)
        BigDecimal balance = parseBigDecimal(bankAccount.get("balance"));

        BankingAccount newAccount = BankingAccount.builder()
                .userId(userId)
                .customerCi(customerCi)
                .accountNumber(accountNumber)
                .accountName(accountName)
                .accountType(accountType)
                .accountStatus(BankingAccount.AccountStatus.ACTIVE)
                .balance(balance != null ? balance : BigDecimal.ZERO)
                .currencyCode("KRW")
                .openedDate(LocalDateTime.now())
                .description("하나은행 계좌 (자동 동기화)")
                .build();

        accountRepository.save(newAccount);
        log.info("새 계좌 생성 완료 - 계좌번호: {}, 잔액: {}", accountNumber, balance);
    }

    /**
     * 거래내역 동기화
     */
    private void syncAccountTransactions(String accountNumber) {
        try {
            log.info("거래내역 동기화 시작 - 계좌번호: {}", accountNumber);

            // 1. 하나인플랜 DB에서 계좌 조회 (일반 계좌)
            Optional<BankingAccount> accountOpt = accountRepository.findByAccountNumber(accountNumber);
            if (accountOpt.isEmpty()) {
                log.warn("일반 계좌를 찾을 수 없습니다 - 계좌번호: {} (IRP 계좌일 수 있음)", accountNumber);
                // IRP 계좌일 수 있으므로 계속 진행
            }

            BankingAccount account = accountOpt.orElse(null);

            // 2. 하나은행 서버에서 거래내역 조회
            List<HanaBankClient.BankTransactionDto> bankTransactions = 
                    hanaBankClient.getTransactionsByAccountNumber(accountNumber);

            if (bankTransactions == null || bankTransactions.isEmpty()) {
                log.info("동기화할 거래내역이 없습니다 - 계좌번호: {}", accountNumber);
                return;
            }

            // 3. 각 거래내역을 동기화
            int syncedCount = 0;
            for (HanaBankClient.BankTransactionDto bankTx : bankTransactions) {
                if (syncSingleTransaction(account, accountNumber, bankTx)) {
                    syncedCount++;
                }
            }

            log.info("거래내역 동기화 완료 - 계좌번호: {}, 조회된 거래: {}, 동기화된 거래: {}", 
                    accountNumber, bankTransactions.size(), syncedCount);

        } catch (Exception e) {
            log.error("거래내역 동기화 실패 - 계좌번호: {}, 오류: {}", accountNumber, e.getMessage(), e);
        }
    }

    /**
     * 단일 거래내역 동기화
     * @param account 계좌 엔티티 (null일 수 있음 - IRP 계좌인 경우)
     * @param accountNumber 계좌번호
     * @return 새로 생성된 거래내역이면 true, 이미 존재하면 false
     */
    private boolean syncSingleTransaction(BankingAccount account, String accountNumber, HanaBankClient.BankTransactionDto bankTx) {
        try {
            // transactionNumber가 없으면 건너뜀
            String transactionNumber = bankTx.getTransactionNumber();
            if (transactionNumber == null || transactionNumber.isEmpty()) {
                log.warn("거래번호가 없습니다 - 건너뜁니다");
                return false;
            }

            // 이미 존재하는 거래인지 확인
            Optional<Transaction> existingTx = transactionRepository.findByTransactionNumber(transactionNumber);
            if (existingTx.isPresent()) {
                return false; // 이미 존재하는 거래
            }

            // 새 거래내역 생성
            Transaction.TransactionType txType = parseTransactionType(bankTx.getTransactionType());
            Transaction.TransactionDirection txDirection = parseTransactionDirection(bankTx.getTransactionDirection());

            // 계좌 ID 설정 (account가 null이면 계좌번호만 저장)
            Long accountId = (account != null) ? account.getAccountId() : null;

            Transaction newTransaction = Transaction.builder()
                    .transactionNumber(transactionNumber)
                    .fromAccountId(txDirection == Transaction.TransactionDirection.DEBIT ? accountId : null)
                    .toAccountId(txDirection == Transaction.TransactionDirection.CREDIT ? accountId : null)
                    .fromAccountNumber(txDirection == Transaction.TransactionDirection.DEBIT ? accountNumber : null)
                    .toAccountNumber(txDirection == Transaction.TransactionDirection.CREDIT ? accountNumber : null)
                    .transactionType(txType)
                    .transactionCategory(Transaction.TransactionCategory.OTHER)
                    .amount(bankTx.getAmount())
                    .balanceAfter(bankTx.getBalanceAfter())
                    .transactionDirection(txDirection)
                    .description(bankTx.getDescription())
                    .transactionStatus(parseTransactionStatus(bankTx.getTransactionStatus()))
                    .transactionDate(parseLocalDateTime(bankTx.getTransactionDatetime()))
                    .processedDate(parseLocalDateTime(bankTx.getProcessedDate()))
                    .referenceNumber(bankTx.getReferenceNumber())
                    .memo(bankTx.getMemo())
                    .build();

            transactionRepository.save(newTransaction);
            log.debug("거래내역 생성 완료 - 거래번호: {}, 계좌: {}, 금액: {}, 잔액: {}", 
                    transactionNumber, accountNumber, bankTx.getAmount(), bankTx.getBalanceAfter());

            return true;

        } catch (Exception e) {
            log.error("거래내역 동기화 실패 - 거래번호: {}, 계좌: {}, 오류: {}", 
                    bankTx.getTransactionNumber(), accountNumber, e.getMessage(), e);
            return false;
        }
    }

    // ===== 유틸리티 메서드 =====

    private BigDecimal parseBigDecimal(Object value) {
        if (value == null) return null;
        if (value instanceof BigDecimal) return (BigDecimal) value;
        if (value instanceof Number) return BigDecimal.valueOf(((Number) value).doubleValue());
        try {
            return new BigDecimal(value.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private Integer parseInteger(Object value) {
        if (value == null) return null;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Number) return ((Number) value).intValue();
        try {
            return Integer.parseInt(value.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private Transaction.TransactionType parseTransactionType(String type) {
        if (type == null) return Transaction.TransactionType.TRANSFER;
        
        switch (type.toUpperCase()) {
            case "DEPOSIT": return Transaction.TransactionType.DEPOSIT;
            case "WITHDRAWAL": return Transaction.TransactionType.WITHDRAWAL;
            case "TRANSFER": return Transaction.TransactionType.TRANSFER;
            case "AUTO_TRANSFER": return Transaction.TransactionType.AUTO_TRANSFER;
            case "INTEREST": return Transaction.TransactionType.INTEREST;
            case "FEE": return Transaction.TransactionType.FEE;
            case "REFUND": return Transaction.TransactionType.REFUND;
            case "REVERSAL": return Transaction.TransactionType.REVERSAL;
            default: return Transaction.TransactionType.TRANSFER;
        }
    }

    private Transaction.TransactionDirection parseTransactionDirection(String direction) {
        if (direction == null) return Transaction.TransactionDirection.DEBIT;
        return direction.equalsIgnoreCase("CREDIT") ? 
                Transaction.TransactionDirection.CREDIT : Transaction.TransactionDirection.DEBIT;
    }

    private Transaction.TransactionStatus parseTransactionStatus(String status) {
        if (status == null) return Transaction.TransactionStatus.COMPLETED;
        
        switch (status.toUpperCase()) {
            case "PENDING": return Transaction.TransactionStatus.PENDING;
            case "PROCESSING": return Transaction.TransactionStatus.PROCESSING;
            case "COMPLETED": return Transaction.TransactionStatus.COMPLETED;
            case "FAILED": return Transaction.TransactionStatus.FAILED;
            case "CANCELLED": return Transaction.TransactionStatus.CANCELLED;
            case "REVERSED": return Transaction.TransactionStatus.REVERSED;
            default: return Transaction.TransactionStatus.COMPLETED;
        }
    }

    private LocalDateTime parseLocalDateTime(Object value) {
        if (value == null) return LocalDateTime.now();
        if (value instanceof LocalDateTime) return (LocalDateTime) value;
        
        try {
            // ISO 형식 파싱 시도
            return LocalDateTime.parse(value.toString(), DateTimeFormatter.ISO_DATE_TIME);
        } catch (Exception e1) {
            try {
                // 다른 형식 파싱 시도
                return LocalDateTime.parse(value.toString(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            } catch (Exception e2) {
                log.warn("날짜 파싱 실패, 현재 시간 사용 - 값: {}", value);
                return LocalDateTime.now();
            }
        }
    }
}

