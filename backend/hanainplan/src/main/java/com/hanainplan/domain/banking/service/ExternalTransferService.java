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

@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalTransferService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final ExternalAccountVerificationService verificationService;
    private final BankWithdrawalService withdrawalService;
    private final BankDepositService depositService;

    @Transactional
    public TransactionResponseDto transferToExternalAccount(ExternalTransferRequestDto request) {
        log.info("외부 계좌 송금 처리 시작 - 출금계좌ID: {}, 수신계좌번호: {}, 금액: {}원",
                request.getFromAccountId(), request.getToAccountNumber(), request.getAmount());

        try {
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

            fromAccount.updateBalance(request.getAmount().negate());
            BankingAccount updatedFromAccount = accountRepository.save(fromAccount);

            accountRepository.findByAccountNumber(request.getToAccountNumber())
                    .ifPresent(toAccount -> {
                        toAccount.updateBalance(request.getAmount());
                        accountRepository.save(toAccount);
                        log.info("HANAinPLAN 수신 계좌 잔액 업데이트 완료 - 계좌번호: {}, 새 잔액: {}원", 
                                request.getToAccountNumber(), toAccount.getBalance());
                    });

            String withdrawalTransactionNumber = java.util.UUID.randomUUID().toString();

            Transaction withdrawalTransaction = Transaction.builder()
                    .transactionNumber(withdrawalTransactionNumber)
                    .fromAccountId(request.getFromAccountId())
                    .fromAccountNumber(updatedFromAccount.getAccountNumber())
                    .toAccountId(null)
                    .toAccountNumber(request.getToAccountNumber())
                    .transactionType(Transaction.TransactionType.TRANSFER)
                    .transactionCategory(Transaction.TransactionCategory.OTHER)
                    .amount(request.getAmount())
                    .balanceAfter(updatedFromAccount.getBalance())
                    .transactionDirection(Transaction.TransactionDirection.DEBIT)
                    .transactionStatus(Transaction.TransactionStatus.COMPLETED)
                    .description("계좌 이체")
                    .memo(request.getDescription() != null && !request.getDescription().trim().isEmpty() 
                            ? request.getDescription() 
                            : null)
                    .referenceNumber(withdrawalResult.getTransactionId())
                    .transactionDate(LocalDateTime.now())
                    .build();

            Transaction savedWithdrawalTransaction = transactionRepository.save(withdrawalTransaction);

            accountRepository.findByAccountNumber(request.getToAccountNumber())
                    .ifPresent(toAccount -> {
                        String depositTransactionNumber = java.util.UUID.randomUUID().toString();

                        Transaction depositTransaction = Transaction.builder()
                                .transactionNumber(depositTransactionNumber)
                                .fromAccountId(null)
                                .fromAccountNumber(updatedFromAccount.getAccountNumber())
                                .toAccountId(toAccount.getAccountId())
                                .toAccountNumber(request.getToAccountNumber())
                                .transactionType(Transaction.TransactionType.TRANSFER)
                                .transactionCategory(Transaction.TransactionCategory.OTHER)
                                .amount(request.getAmount())
                                .balanceAfter(toAccount.getBalance())
                                .transactionDirection(Transaction.TransactionDirection.CREDIT)
                                .transactionStatus(Transaction.TransactionStatus.COMPLETED)
                                .description("계좌 이체")
                                .memo(request.getDescription() != null && !request.getDescription().trim().isEmpty() 
                                        ? request.getDescription() 
                                        : null)
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