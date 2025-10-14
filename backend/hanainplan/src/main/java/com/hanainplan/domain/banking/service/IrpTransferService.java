package com.hanainplan.domain.banking.service;

import com.hanainplan.domain.banking.dto.AccountVerificationResponseDto;
import com.hanainplan.domain.banking.dto.TransactionResponseDto;
import com.hanainplan.domain.banking.dto.TransferToIrpRequestDto;
import com.hanainplan.domain.banking.entity.BankingAccount;
import com.hanainplan.domain.banking.entity.IrpAccount;
import com.hanainplan.domain.banking.entity.Transaction;
import com.hanainplan.domain.banking.repository.AccountRepository;
import com.hanainplan.domain.banking.repository.IrpAccountRepository;
import com.hanainplan.domain.banking.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * IRP 계좌 송금 전용 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IrpTransferService {

    private final AccountRepository accountRepository;
    private final IrpAccountRepository irpAccountRepository;
    private final TransactionRepository transactionRepository;
    private final ExternalAccountVerificationService verificationService;
    private final BankWithdrawalService withdrawalService;
    private final BankDepositService depositService;

    /**
     * IRP 계좌로 송금 처리
     */
    @Transactional
    public TransactionResponseDto transferToIrp(TransferToIrpRequestDto request) {
        log.info("IRP 계좌 송금 처리 시작 - 출금계좌ID: {}, IRP계좌번호: {}, 금액: {}원",
                request.getFromAccountId(), request.getToIrpAccountNumber(), request.getAmount());

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

            // 2. IRP 수신 계좌 검증
            AccountVerificationResponseDto verificationResult =
                    verificationService.verifyExternalAccount(request.getToIrpAccountNumber());

            if (!verificationResult.isExists()) {
                return TransactionResponseDto.failure("IRP 계좌를 찾을 수 없습니다",
                        verificationResult.getMessage());
            }

            if (!"IRP".equals(verificationResult.getAccountType())) {
                return TransactionResponseDto.failure("IRP 계좌가 아닙니다",
                        "일반 계좌로는 IRP 송금을 할 수 없습니다");
            }

            if (!"ACTIVE".equals(verificationResult.getAccountStatus())) {
                return TransactionResponseDto.failure("비활성 상태의 IRP 계좌입니다",
                        "계좌 상태: " + verificationResult.getAccountStatus());
            }

            // 3. 출금 처리
            BankWithdrawalService.BankWithdrawalResult withdrawalResult =
                    withdrawalService.processWithdrawal(
                            fromAccount.getAccountNumber(),
                            request.getAmount(),
                            request.getDescription() != null ? request.getDescription() : "IRP 계좌 송금"
                    );

            if (!withdrawalResult.isSuccess()) {
                return TransactionResponseDto.failure("출금 처리 실패", withdrawalResult.getMessage());
            }

            log.info("출금 처리 완료 - 거래ID: {}", withdrawalResult.getTransactionId());

            // 4. IRP 입금 처리
            BankDepositService.BankDepositResult depositResult =
                    depositService.processDeposit(
                            request.getToIrpAccountNumber(),
                            request.getAmount(),
                            request.getDescription() != null ? request.getDescription() : "IRP 계좌 입금",
                            "IRP",
                            verificationResult.getBankCode()
                    );

            if (!depositResult.isSuccess()) {
                log.error("IRP 입금 처리 실패 - 출금은 완료됨, 수동 조정 필요");
                return TransactionResponseDto.failure("IRP 입금 처리 실패 (출금은 완료됨)", depositResult.getMessage());
            }

            log.info("IRP 입금 처리 완료 - 거래ID: {}", depositResult.getTransactionId());

            // 5. HANAinPLAN 계좌 잔액 업데이트
            // 5-1. 출금 계좌 잔액 차감
            fromAccount.updateBalance(request.getAmount().negate());
            BankingAccount updatedFromAccount = accountRepository.save(fromAccount);
            
            // 5-2. IRP 수신 계좌 잔액 증가 (tb_banking_account)
            accountRepository.findByAccountNumber(request.getToIrpAccountNumber())
                    .ifPresent(irpBankingAccount -> {
                        irpBankingAccount.updateBalance(request.getAmount());
                        accountRepository.save(irpBankingAccount);
                        log.info("HANAinPLAN BankingAccount IRP 잔액 업데이트 완료 - 계좌번호: {}, 새 잔액: {}원", 
                                request.getToIrpAccountNumber(), irpBankingAccount.getBalance());
                    });
            
            // 5-3. IRP 전용 테이블 업데이트 (tb_irp_account)
            irpAccountRepository.findByAccountNumber(request.getToIrpAccountNumber())
                    .ifPresent(irpAccount -> {
                        BigDecimal newBalance = irpAccount.getCurrentBalance().add(request.getAmount());
                        BigDecimal newTotalContribution = irpAccount.getTotalContribution().add(request.getAmount());
                        
                        irpAccount.setCurrentBalance(newBalance);
                        irpAccount.setTotalContribution(newTotalContribution);
                        irpAccount.setLastContributionDate(LocalDate.now());
                        irpAccount.setExternalLastUpdated(LocalDateTime.now());
                        irpAccountRepository.save(irpAccount);
                        
                        log.info("HANAinPLAN IrpAccount 업데이트 완료 - 계좌번호: {}, 새 잔액: {}원, 총 납입: {}원", 
                                request.getToIrpAccountNumber(), newBalance, newTotalContribution);
                    });

            // 6. HANAinPLAN 거래 기록 저장 (출금 거래)
            String withdrawalTransactionNumber = java.util.UUID.randomUUID().toString();
            
            Transaction withdrawalTransaction = Transaction.builder()
                    .transactionNumber(withdrawalTransactionNumber)
                    .fromAccountId(request.getFromAccountId())
                    .fromAccountNumber(updatedFromAccount.getAccountNumber()) // 출금 계좌번호
                    .toAccountId(null) // IRP 계좌로 송금
                    .toAccountNumber(request.getToIrpAccountNumber()) // 수신 IRP 계좌번호
                    .transactionType(Transaction.TransactionType.TRANSFER)
                    .transactionCategory(Transaction.TransactionCategory.INVESTMENT)
                    .amount(request.getAmount())
                    .balanceAfter(updatedFromAccount.getBalance()) // 출금 후 잔액
                    .transactionDirection(Transaction.TransactionDirection.DEBIT)
                    .transactionStatus(Transaction.TransactionStatus.COMPLETED)
                    .description("개인연금 입출금") // IRP 거래 표준 이름
                    .memo(request.getDescription() != null && !request.getDescription().trim().isEmpty() 
                            ? request.getDescription() 
                            : null) // 사용자가 입력한 내용, 없으면 null
                    .referenceNumber(withdrawalResult.getTransactionId())
                    .transactionDate(LocalDateTime.now())
                    .build();

            Transaction savedWithdrawalTransaction = transactionRepository.save(withdrawalTransaction);
            
            // 7. HANAinPLAN 거래 기록 저장 (IRP 입금 거래)
            // IRP 계좌가 HANAinPLAN의 tb_banking_account에 있는 경우에만 입금 거래 저장
            accountRepository.findByAccountNumber(request.getToIrpAccountNumber())
                    .ifPresent(irpBankingAccount -> {
                        String depositTransactionNumber = java.util.UUID.randomUUID().toString();
                        
                        Transaction depositTransaction = Transaction.builder()
                                .transactionNumber(depositTransactionNumber)
                                .fromAccountId(null) // 입금 거래는 fromAccountId를 null로 (IRP 계좌에만 표시하기 위해)
                                .fromAccountNumber(updatedFromAccount.getAccountNumber()) // 출금 계좌번호는 기록
                                .toAccountId(irpBankingAccount.getAccountId()) // IRP 계좌 ID
                                .toAccountNumber(request.getToIrpAccountNumber()) // IRP 계좌번호
                                .transactionType(Transaction.TransactionType.TRANSFER)
                                .transactionCategory(Transaction.TransactionCategory.INVESTMENT)
                                .amount(request.getAmount())
                                .balanceAfter(irpBankingAccount.getBalance()) // 입금 후 잔액
                                .transactionDirection(Transaction.TransactionDirection.CREDIT)
                                .transactionStatus(Transaction.TransactionStatus.COMPLETED)
                                .description("개인연금 입출금") // IRP 거래 표준 이름
                                .memo(request.getDescription() != null && !request.getDescription().trim().isEmpty() 
                                        ? request.getDescription() 
                                        : null) // 사용자가 입력한 내용, 없으면 null
                                .referenceNumber(depositResult.getTransactionId())
                                .transactionDate(LocalDateTime.now())
                                .build();
                        
                        transactionRepository.save(depositTransaction);
                        log.info("HANAinPLAN IRP 입금 거래내역 저장 완료 - 거래번호: {}", depositTransactionNumber);
                    });

            Transaction savedTransaction = savedWithdrawalTransaction;

            log.info("IRP 계좌 송금 완료 - 거래번호: {}, 출금계좌: {}, IRP계좌: {}, 금액: {}원",
                    savedTransaction.getTransactionNumber(),
                    fromAccount.getAccountNumber(),
                    request.getToIrpAccountNumber(),
                    request.getAmount());

            return TransactionResponseDto.success(
                    "IRP 계좌 송금이 완료되었습니다",
                    savedTransaction.getTransactionNumber(),
                    request.getAmount(),
                    fromAccount.getBalance(),
                    BigDecimal.ZERO,
                    savedTransaction.getTransactionStatus().getDescription()
            );

        } catch (Exception e) {
            log.error("IRP 계좌 송금 실패 - 출금계좌ID: {}, 오류: {}", request.getFromAccountId(), e.getMessage());
            return TransactionResponseDto.failure("IRP 계좌 송금 중 오류가 발생했습니다", e.getMessage());
        }
    }

}

