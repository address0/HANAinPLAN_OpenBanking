package com.hanainplan.domain.banking.service;

import com.hanainplan.domain.banking.dto.InternalTransferRequestDto;
import com.hanainplan.domain.banking.dto.TransactionResponseDto;
import com.hanainplan.domain.banking.entity.BankingAccount;
import com.hanainplan.domain.banking.entity.Transaction;
import com.hanainplan.domain.banking.repository.AccountRepository;
import com.hanainplan.domain.banking.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 계좌 간 송금 통합 서비스
 * - DepositSubscriptionService 패턴 참고
 * - 출금 → 입금 → DB 동기화 → 거래 기록
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TransferIntegrationService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final BankWithdrawalService bankWithdrawalService;
    private final BankDepositService bankDepositService;

    /**
     * 계좌 간 송금 처리
     */
    public TransactionResponseDto transferBetweenAccounts(InternalTransferRequestDto request) {
        log.info("계좌 간 송금 요청 - 출금: {}, 입금: {}, 금액: {}원", 
                request.getFromAccountId(), request.getToAccountId(), request.getAmount());
        
        try {
            // 1. 출금 계좌 조회
            BankingAccount fromAccount = accountRepository.findById(request.getFromAccountId())
                    .orElseThrow(() -> new RuntimeException("출금 계좌를 찾을 수 없습니다: " + request.getFromAccountId()));
            
            // 2. 입금 계좌 조회
            BankingAccount toAccount = accountRepository.findById(request.getToAccountId())
                    .orElseThrow(() -> new RuntimeException("입금 계좌를 찾을 수 없습니다: " + request.getToAccountId()));
            
            // 3. 계좌 상태 확인
            if (fromAccount.getAccountStatus() != BankingAccount.AccountStatus.ACTIVE) {
                return TransactionResponseDto.failure("출금 계좌가 비활성 상태입니다", 
                        "계좌 상태: " + fromAccount.getAccountStatus().getDescription());
            }
            
            if (toAccount.getAccountStatus() != BankingAccount.AccountStatus.ACTIVE) {
                return TransactionResponseDto.failure("입금 계좌가 비활성 상태입니다", 
                        "계좌 상태: " + toAccount.getAccountStatus().getDescription());
            }
            
            // 4. 잔액 확인
            if (!fromAccount.hasSufficientBalance(request.getAmount())) {
                return TransactionResponseDto.failure("잔액이 부족합니다", 
                        "현재 잔액: " + fromAccount.getBalance() + "원, 요청 금액: " + request.getAmount() + "원");
            }
            
            // 5. 출금 처리 (은행 서버)
            String description = request.getDescription() != null ? request.getDescription() : "계좌 송금";
            BankWithdrawalService.BankWithdrawalResult withdrawalResult = 
                    bankWithdrawalService.processWithdrawal(
                            fromAccount.getAccountNumber(), 
                            request.getAmount(), 
                            description + " (송금)"
                    );
            
            if (!withdrawalResult.isSuccess()) {
                log.error("출금 실패 - 계좌: {}, 사유: {}", fromAccount.getAccountNumber(), withdrawalResult.getMessage());
                return TransactionResponseDto.failure("출금 처리 실패", withdrawalResult.getMessage());
            }
            
            log.info("출금 성공 - 계좌: {}, 거래ID: {}", fromAccount.getAccountNumber(), withdrawalResult.getTransactionId());
            
            // 6. 입금 처리 (은행 서버)
            // IRP 계좌면 IRP 입금 API 호출, 일반 계좌면 일반 입금 API 호출
            boolean isIrpAccount = toAccount.getAccountType() == BankingAccount.AccountType.SECURITIES &&
                                   (toAccount.getAccountName() != null && toAccount.getAccountName().contains("IRP"));
            
            // 은행 코드 추출 (계좌번호 앞 3자리)
            String toBankCode = extractBankCode(toAccount.getAccountNumber());
            String accountType = isIrpAccount ? "IRP" : "GENERAL";
            
            BankDepositService.BankDepositResult depositResult;
            log.info("{} 계좌 입금 처리 - 계좌: {}, 은행코드: {}", accountType, toAccount.getAccountNumber(), toBankCode);
            depositResult = bankDepositService.processDeposit(
                    toAccount.getAccountNumber(), 
                    request.getAmount(), 
                    description + " (수신)",
                    accountType,
                    toBankCode
            );
            
            if (!depositResult.isSuccess()) {
                log.error("입금 실패 - 계좌: {}, 사유: {}", toAccount.getAccountNumber(), depositResult.getMessage());
                // TODO: 보상 트랜잭션 - 출금 취소 필요
                return TransactionResponseDto.failure("입금 처리 실패", 
                        depositResult.getMessage() + " (출금은 완료되었으나 입금 실패)");
            }
            
            log.info("입금 성공 - 계좌: {}, 거래ID: {}", toAccount.getAccountNumber(), depositResult.getTransactionId());
            
            // 7. 하나인플랜 DB 잔액 동기화 (DepositSubscriptionService 패턴)
            BigDecimal fromNewBalance = fromAccount.getBalance().subtract(request.getAmount());
            BigDecimal toNewBalance = toAccount.getBalance().add(request.getAmount());
            
            fromAccount.setBalance(fromNewBalance);
            toAccount.setBalance(toNewBalance);
            
            accountRepository.save(fromAccount);
            accountRepository.save(toAccount);
            
            log.info("DB 잔액 동기화 완료 - 출금 계좌 잔액: {}원, 입금 계좌 잔액: {}원", fromNewBalance, toNewBalance);
            
            // 8. 하나인플랜 DB에 거래 내역 2건 저장 (출금 + 입금)
            String transactionNumber = generateTransactionNumber();
            
            // 8-1. 출금 거래 내역
            Transaction withdrawalTransaction = Transaction.builder()
                    .transactionNumber(transactionNumber + "-OUT")
                    .fromAccountId(fromAccount.getAccountId())
                    .toAccountId(toAccount.getAccountId())
                    .fromAccountNumber(fromAccount.getAccountNumber())
                    .toAccountNumber(toAccount.getAccountNumber())
                    .transactionType(Transaction.TransactionType.TRANSFER)
                    .transactionCategory(Transaction.TransactionCategory.OTHER)
                    .amount(request.getAmount())
                    .balanceAfter(fromNewBalance)
                    .transactionDirection(Transaction.TransactionDirection.DEBIT)
                    .transactionStatus(Transaction.TransactionStatus.COMPLETED)
                    .description(description + " (송금)")
                    .memo(request.getMemo())
                    .referenceNumber(withdrawalResult.getTransactionId())
                    .build();
            
            // 8-2. 입금 거래 내역
            Transaction depositTransaction = Transaction.builder()
                    .transactionNumber(transactionNumber + "-IN")
                    .fromAccountId(fromAccount.getAccountId())
                    .toAccountId(toAccount.getAccountId())
                    .fromAccountNumber(fromAccount.getAccountNumber())
                    .toAccountNumber(toAccount.getAccountNumber())
                    .transactionType(Transaction.TransactionType.TRANSFER)
                    .transactionCategory(Transaction.TransactionCategory.OTHER)
                    .amount(request.getAmount())
                    .balanceAfter(toNewBalance)
                    .transactionDirection(Transaction.TransactionDirection.CREDIT)
                    .transactionStatus(Transaction.TransactionStatus.COMPLETED)
                    .description(description + " (수신)")
                    .memo(request.getMemo())
                    .referenceNumber(depositResult.getTransactionId())
                    .build();
            
            transactionRepository.save(withdrawalTransaction);
            transactionRepository.save(depositTransaction);
            
            log.info("송금 완료 - 거래번호: {}, 출금: {}원 (잔액 {}원), 입금: {}원 (잔액 {}원)", 
                    transactionNumber, request.getAmount(), fromNewBalance, request.getAmount(), toNewBalance);
            
            return TransactionResponseDto.success(
                    "송금이 완료되었습니다",
                    transactionNumber,
                    request.getAmount(),
                    fromNewBalance,
                    BigDecimal.ZERO,
                    "완료"
            );
            
        } catch (Exception e) {
            log.error("송금 처리 실패 - 출금 계좌: {}, 입금 계좌: {}, 오류: {}", 
                    request.getFromAccountId(), request.getToAccountId(), e.getMessage(), e);
            return TransactionResponseDto.failure("송금 처리 중 오류가 발생했습니다", e.getMessage());
        }
    }
    
    /**
     * 거래 번호 생성
     */
    private String generateTransactionNumber() {
        return "TRF-" + System.currentTimeMillis() + "-" + String.format("%04d", (int)(Math.random() * 10000));
    }
    
    /**
     * 계좌번호에서 은행 코드 추출
     */
    private String extractBankCode(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 3) {
            return "UNKNOWN";
        }
        
        String cleanAccountNumber = accountNumber.replace("-", "");
        String prefix = cleanAccountNumber.substring(0, 3);
        
        // 하나은행: 110-119 또는 081
        if (prefix.equals("081") || (prefix.compareTo("110") >= 0 && prefix.compareTo("119") <= 0)) {
            return "081";
        }
        // 국민은행: 123-129 또는 004
        else if (prefix.equals("004") || (prefix.compareTo("123") >= 0 && prefix.compareTo("129") <= 0)) {
            return "004";
        }
        // 신한은행: 456-459 또는 088
        else if (prefix.equals("088") || (prefix.compareTo("456") >= 0 && prefix.compareTo("459") <= 0)) {
            return "088";
        }
        
        return prefix;
    }
}

