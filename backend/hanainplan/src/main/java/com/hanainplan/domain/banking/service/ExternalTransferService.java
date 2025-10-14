package com.hanainplan.domain.banking.service;

import com.hanainplan.domain.banking.dto.AccountVerificationResponseDto;
import com.hanainplan.domain.banking.dto.ExternalTransferRequestDto;
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
import java.time.LocalDateTime;

/**
 * 일반 외부 계좌 송금 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalTransferService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final ExternalAccountVerificationService verificationService;
    private final BankWithdrawalService withdrawalService;
    private final BankDepositService depositService;

    /**
     * 외부 일반 계좌로 송금 처리
     */
    @Transactional
    public TransactionResponseDto transferToExternalAccount(ExternalTransferRequestDto request) {
        log.info("외부 계좌 송금 처리 시작 - 출금계좌ID: {}, 수신계좌번호: {}, 금액: {}원",
                request.getFromAccountId(), request.getToAccountNumber(), request.getAmount());

        try {
            // 1. 출금 계좌 조회 및 검증
            BankingAccount fromAccount = accountRepository.findById(request.getFromAccountId())
                    .orElseThrow(() -> new RuntimeException("출금 계좌를 찾을 수 없습니다: " + request.getFromAccountId()));

            if (fromAccount.getAccountStatus() != BankingAccount.AccountStatus.ACTIVE) {
                return TransactionResponseDto.failure("출금 계좌가 비활성 상태입니다",
                        "계좌 상태: " + fromAccount.getAccountStatus().getDescription());
            }

            if (!fromAccount.hasSufficientBalance(request.getAmount())) {
                return TransactionResponseDto.failure("잔액이 부족합니다",
                        "현재 잔액: " + fromAccount.getBalance() + "원, 요청 금액: " + request.getAmount() + "원");
            }

            // 2. 수신 계좌 검증
            AccountVerificationResponseDto verificationResult =
                    verificationService.verifyExternalAccount(request.getToAccountNumber());

            if (!verificationResult.isExists()) {
                return TransactionResponseDto.failure("수신 계좌를 찾을 수 없습니다",
                        verificationResult.getMessage());
            }

            if (!"GENERAL".equals(verificationResult.getAccountType())) {
                return TransactionResponseDto.failure("일반 계좌가 아닙니다",
                        "IRP 계좌로는 일반 송금을 할 수 없습니다. IRP 송금을 이용해주세요");
            }

            if (!"ACTIVE".equals(verificationResult.getAccountStatus())) {
                return TransactionResponseDto.failure("비활성 상태의 계좌입니다",
                        "계좌 상태: " + verificationResult.getAccountStatus());
            }

            // 3. 출금 처리
            BankWithdrawalService.BankWithdrawalResult withdrawalResult =
                    withdrawalService.processWithdrawal(
                            fromAccount.getAccountNumber(),
                            request.getAmount(),
                            request.getDescription() != null ? request.getDescription() : "계좌 송금"
                    );

            if (!withdrawalResult.isSuccess()) {
                return TransactionResponseDto.failure("출금 처리 실패", withdrawalResult.getMessage());
            }

            log.info("출금 처리 완료 - 거래ID: {}", withdrawalResult.getTransactionId());

            // 4. 입금 처리
            BankDepositService.BankDepositResult depositResult =
                    depositService.processDeposit(
                            request.getToAccountNumber(),
                            request.getAmount(),
                            request.getDescription() != null ? request.getDescription() : "계좌 입금",
                            "GENERAL",
                            verificationResult.getBankCode()
                    );

            if (!depositResult.isSuccess()) {
                log.error("입금 처리 실패 - 출금은 완료됨, 수동 조정 필요");
                return TransactionResponseDto.failure("입금 처리 실패 (출금은 완료됨)", depositResult.getMessage());
            }

            log.info("입금 처리 완료 - 거래ID: {}", depositResult.getTransactionId());

            // 5. HANAinPLAN 계좌 잔액 업데이트
            // 5-1. 출금 계좌 잔액 차감
            fromAccount.updateBalance(request.getAmount().negate());
            BankingAccount updatedFromAccount = accountRepository.save(fromAccount);
            
            // 5-2. 수신 계좌 잔액 증가 (HANAinPLAN에도 등록되어 있을 수 있음)
            accountRepository.findByAccountNumber(request.getToAccountNumber())
                    .ifPresent(toAccount -> {
                        toAccount.updateBalance(request.getAmount());
                        accountRepository.save(toAccount);
                        log.info("HANAinPLAN 수신 계좌 잔액 업데이트 완료 - 계좌번호: {}, 새 잔액: {}원", 
                                request.getToAccountNumber(), toAccount.getBalance());
                    });

            // 6. HANAinPLAN 거래 기록 저장 (출금 거래)
            String withdrawalTransactionNumber = java.util.UUID.randomUUID().toString();
            
            Transaction withdrawalTransaction = Transaction.builder()
                    .transactionNumber(withdrawalTransactionNumber)
                    .fromAccountId(request.getFromAccountId())
                    .fromAccountNumber(updatedFromAccount.getAccountNumber()) // 출금 계좌번호
                    .toAccountId(null) // 외부 계좌로 송금
                    .toAccountNumber(request.getToAccountNumber()) // 수신 계좌번호
                    .transactionType(Transaction.TransactionType.TRANSFER)
                    .transactionCategory(Transaction.TransactionCategory.OTHER)
                    .amount(request.getAmount())
                    .balanceAfter(updatedFromAccount.getBalance()) // 출금 후 잔액
                    .transactionDirection(Transaction.TransactionDirection.DEBIT)
                    .transactionStatus(Transaction.TransactionStatus.COMPLETED)
                    .description("계좌 이체") // 일반 송금 표준 이름
                    .memo(request.getDescription() != null && !request.getDescription().trim().isEmpty() 
                            ? request.getDescription() 
                            : null) // 사용자가 입력한 내용, 없으면 null
                    .referenceNumber(withdrawalResult.getTransactionId())
                    .transactionDate(LocalDateTime.now())
                    .build();

            Transaction savedWithdrawalTransaction = transactionRepository.save(withdrawalTransaction);
            
            // 7. HANAinPLAN 거래 기록 저장 (입금 거래)
            // 수신 계좌가 HANAinPLAN의 tb_banking_account에 있는 경우에만 입금 거래 저장
            accountRepository.findByAccountNumber(request.getToAccountNumber())
                    .ifPresent(toAccount -> {
                        String depositTransactionNumber = java.util.UUID.randomUUID().toString();
                        
                        Transaction depositTransaction = Transaction.builder()
                                .transactionNumber(depositTransactionNumber)
                                .fromAccountId(null) // 입금 거래는 fromAccountId를 null로 (수신 계좌에만 표시하기 위해)
                                .fromAccountNumber(updatedFromAccount.getAccountNumber()) // 출금 계좌번호는 기록
                                .toAccountId(toAccount.getAccountId()) // 수신 계좌 ID
                                .toAccountNumber(request.getToAccountNumber()) // 수신 계좌번호
                                .transactionType(Transaction.TransactionType.TRANSFER)
                                .transactionCategory(Transaction.TransactionCategory.OTHER)
                                .amount(request.getAmount())
                                .balanceAfter(toAccount.getBalance()) // 입금 후 잔액
                                .transactionDirection(Transaction.TransactionDirection.CREDIT)
                                .transactionStatus(Transaction.TransactionStatus.COMPLETED)
                                .description("계좌 이체") // 일반 송금 표준 이름
                                .memo(request.getDescription() != null && !request.getDescription().trim().isEmpty() 
                                        ? request.getDescription() 
                                        : null) // 사용자가 입력한 내용, 없으면 null
                                .referenceNumber(depositResult.getTransactionId())
                                .transactionDate(LocalDateTime.now())
                                .build();
                        
                        transactionRepository.save(depositTransaction);
                        log.info("HANAinPLAN 입금 거래내역 저장 완료 - 거래번호: {}", depositTransactionNumber);
                    });

            Transaction savedTransaction = savedWithdrawalTransaction;

            log.info("외부 계좌 송금 완료 - 거래번호: {}, 출금계좌: {}, 수신계좌: {}, 금액: {}원",
                    savedTransaction.getTransactionNumber(),
                    fromAccount.getAccountNumber(),
                    request.getToAccountNumber(),
                    request.getAmount());

            return TransactionResponseDto.success(
                    "송금이 완료되었습니다",
                    savedTransaction.getTransactionNumber(),
                    request.getAmount(),
                    fromAccount.getBalance(),
                    BigDecimal.ZERO,
                    savedTransaction.getTransactionStatus().getDescription()
            );

        } catch (Exception e) {
            log.error("외부 계좌 송금 실패 - 출금계좌ID: {}, 오류: {}", request.getFromAccountId(), e.getMessage());
            return TransactionResponseDto.failure("송금 중 오류가 발생했습니다", e.getMessage());
        }
    }

}

