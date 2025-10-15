package com.hanainplan.domain.banking.service;

import com.hanainplan.domain.banking.dto.*;
import com.hanainplan.domain.banking.entity.BankingAccount;
import com.hanainplan.domain.banking.entity.Transaction;
import com.hanainplan.domain.banking.repository.AccountRepository;
import com.hanainplan.domain.banking.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final AccountService accountService;
    private final BankWithdrawalService bankWithdrawalService;

    public TransactionResponseDto deposit(DepositRequestDto request) {
        log.info("입금 처리 요청 - 계좌 ID: {}, 금액: {}", request.getAccountId(), request.getAmount());

        try {
            BankingAccount account = accountRepository.findById(request.getAccountId())
                    .orElseThrow(() -> new RuntimeException("계좌를 찾을 수 없습니다: " + request.getAccountId()));

            if (account.getAccountStatus() != BankingAccount.AccountStatus.ACTIVE) {
                return TransactionResponseDto.failure("비활성 계좌입니다", "계좌 상태: " + account.getAccountStatus().getDescription());
            }

            Transaction transaction = createTransaction(
                    null,
                    request.getAccountId(),
                    Transaction.TransactionType.DEPOSIT,
                    Transaction.TransactionCategory.OTHER,
                    request.getAmount(),
                    request.getDescription(),
                    request.getMemo(),
                    request.getReferenceNumber()
            );

            account.updateBalance(request.getAmount());
            accountRepository.save(account);

            transaction.complete();
            transaction.setBalanceAfter(account.getBalance());
            Transaction savedTransaction = transactionRepository.save(transaction);

            log.info("입금 처리 완료 - 거래 ID: {}, 거래번호: {}, 새 잔액: {}", 
                    savedTransaction.getTransactionId(), savedTransaction.getTransactionNumber(), account.getBalance());

            return TransactionResponseDto.success(
                    "입금이 완료되었습니다",
                    savedTransaction.getTransactionNumber(),
                    request.getAmount(),
                    account.getBalance(),
                    BigDecimal.ZERO,
                    savedTransaction.getTransactionStatus().getDescription()
            );

        } catch (Exception e) {
            log.error("입금 처리 실패 - 계좌 ID: {}, 금액: {}, 오류: {}", 
                    request.getAccountId(), request.getAmount(), e.getMessage());
            return TransactionResponseDto.failure("입금 처리 중 오류가 발생했습니다", e.getMessage());
        }
    }

    public TransactionResponseDto withdrawal(WithdrawalRequestDto request) {
        log.info("출금 처리 요청 - 계좌 ID: {}, 금액: {}", request.getAccountId(), request.getAmount());

        try {
            BankingAccount account = accountRepository.findById(request.getAccountId())
                    .orElseThrow(() -> new RuntimeException("계좌를 찾을 수 없습니다: " + request.getAccountId()));

            if (account.getAccountStatus() != BankingAccount.AccountStatus.ACTIVE) {
                return TransactionResponseDto.failure("비활성 계좌입니다", "계좌 상태: " + account.getAccountStatus().getDescription());
            }

            if (!account.hasSufficientBalance(request.getAmount())) {
                return TransactionResponseDto.failure("잔액이 부족합니다", 
                        "현재 잔액: " + account.getBalance() + ", 요청 금액: " + request.getAmount());
            }

            Transaction transaction = createTransaction(
                    request.getAccountId(),
                    null,
                    Transaction.TransactionType.WITHDRAWAL,
                    Transaction.TransactionCategory.OTHER,
                    request.getAmount(),
                    request.getDescription(),
                    request.getMemo(),
                    request.getReferenceNumber()
            );

            BankWithdrawalService.BankWithdrawalResult bankResult = 
                    bankWithdrawalService.processWithdrawal(
                            account.getAccountNumber(), 
                            request.getAmount(), 
                            request.getDescription()
                    );

            if (!bankResult.isSuccess()) {
                transaction.fail("은행 서버 출금 실패: " + bankResult.getMessage());
                transactionRepository.save(transaction);

                log.error("은행 출금 실패 - 계좌번호: {}, 메시지: {}", account.getAccountNumber(), bankResult.getMessage());
                return TransactionResponseDto.failure("출금 실패", bankResult.getMessage());
            }

            account.updateBalance(request.getAmount().negate());
            accountRepository.save(account);

            transaction.complete();
            transaction.setBalanceAfter(account.getBalance());
            transaction.setReferenceNumber(bankResult.getTransactionId());
            Transaction savedTransaction = transactionRepository.save(transaction);

            log.info("출금 처리 완료 - 거래 ID: {}, 거래번호: {}, 은행 거래 ID: {}, 새 잔액: {}",
                    savedTransaction.getTransactionId(), savedTransaction.getTransactionNumber(), 
                    bankResult.getTransactionId(), account.getBalance());

            return TransactionResponseDto.success(
                    "출금이 완료되었습니다",
                    savedTransaction.getTransactionNumber(),
                    request.getAmount(),
                    account.getBalance(),
                    BigDecimal.ZERO,
                    savedTransaction.getTransactionStatus().getDescription()
            );

        } catch (Exception e) {
            log.error("출금 처리 실패 - 계좌 ID: {}, 금액: {}, 오류: {}", 
                    request.getAccountId(), request.getAmount(), e.getMessage());
            return TransactionResponseDto.failure("출금 처리 중 오류가 발생했습니다", e.getMessage());
        }
    }

    public TransactionResponseDto transfer(TransferRequestDto request) {
        log.info("이체 처리 요청 - 출금 계좌 ID: {}, 입금 계좌 ID: {}, 금액: {}", 
                request.getFromAccountId(), request.getToAccountId(), request.getAmount());

        try {
            BankingAccount fromAccount = accountRepository.findById(request.getFromAccountId())
                    .orElseThrow(() -> new RuntimeException("출금 계좌를 찾을 수 없습니다: " + request.getFromAccountId()));

            BankingAccount toAccount = accountRepository.findById(request.getToAccountId())
                    .orElseThrow(() -> new RuntimeException("입금 계좌를 찾을 수 없습니다: " + request.getToAccountId()));

            if (fromAccount.getAccountStatus() != BankingAccount.AccountStatus.ACTIVE) {
                return TransactionResponseDto.failure("출금 계좌가 비활성 상태입니다", 
                        "계좌 상태: " + fromAccount.getAccountStatus().getDescription());
            }

            if (toAccount.getAccountStatus() != BankingAccount.AccountStatus.ACTIVE) {
                return TransactionResponseDto.failure("입금 계좌가 비활성 상태입니다", 
                        "계좌 상태: " + toAccount.getAccountStatus().getDescription());
            }

            if (!fromAccount.hasSufficientBalance(request.getAmount())) {
                return TransactionResponseDto.failure("잔액이 부족합니다", 
                        "현재 잔액: " + fromAccount.getBalance() + ", 요청 금액: " + request.getAmount());
            }

            Transaction transaction = createTransaction(
                    request.getFromAccountId(),
                    request.getToAccountId(),
                    Transaction.TransactionType.TRANSFER,
                    Transaction.TransactionCategory.OTHER,
                    request.getAmount(),
                    request.getDescription(),
                    request.getMemo(),
                    request.getReferenceNumber()
            );

            fromAccount.updateBalance(request.getAmount().negate());
            toAccount.updateBalance(request.getAmount());

            accountRepository.save(fromAccount);
            accountRepository.save(toAccount);

            transaction.complete();
            transaction.setBalanceAfter(fromAccount.getBalance());
            Transaction savedTransaction = transactionRepository.save(transaction);

            log.info("이체 처리 완료 - 거래 ID: {}, 거래번호: {}, 출금 계좌 잔액: {}, 입금 계좌 잔액: {}", 
                    savedTransaction.getTransactionId(), savedTransaction.getTransactionNumber(), 
                    fromAccount.getBalance(), toAccount.getBalance());

            return TransactionResponseDto.success(
                    "이체가 완료되었습니다",
                    savedTransaction.getTransactionNumber(),
                    request.getAmount(),
                    fromAccount.getBalance(),
                    BigDecimal.ZERO,
                    savedTransaction.getTransactionStatus().getDescription()
            );

        } catch (Exception e) {
            log.error("이체 처리 실패 - 출금 계좌 ID: {}, 입금 계좌 ID: {}, 금액: {}, 오류: {}", 
                    request.getFromAccountId(), request.getToAccountId(), request.getAmount(), e.getMessage());
            return TransactionResponseDto.failure("이체 처리 중 오류가 발생했습니다", e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public Page<TransactionDto> getTransactionHistory(TransactionHistoryRequestDto request) {
        log.info("거래 내역 조회 - 계좌 ID: {}, 계좌번호: {}, 페이지: {}, 크기: {}",
                request.getAccountId(), request.getAccountNumber(), request.getPage(), request.getSize());

        Sort sort = Sort.by(Sort.Direction.fromString(request.getSortDirection()), request.getSortBy());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        Page<Transaction> transactions;

        if (request.getAccountNumber() != null && !request.getAccountNumber().isEmpty()) {
            if (request.getStartDate() != null && request.getEndDate() != null) {
                transactions = transactionRepository.findByAccountNumberAndDateRange(
                        request.getAccountNumber(), request.getStartDate(), request.getEndDate(), pageable);
            } else {
                transactions = transactionRepository.findByAccountNumberOrderByTransactionDateDesc(
                        request.getAccountNumber(), pageable);
            }
        } else {
            if (request.getStartDate() != null && request.getEndDate() != null) {
                transactions = transactionRepository.findTransactionsByAccountAndDateRange(
                        request.getAccountId(), request.getStartDate(), request.getEndDate(), pageable);
            } else {
                transactions = transactionRepository.findByFromAccountIdOrToAccountIdOrderByTransactionDateDesc(
                        request.getAccountId(), request.getAccountId(), pageable);
            }
        }

        return transactions.map(TransactionDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public Optional<TransactionDto> getTransaction(Long transactionId) {
        log.info("거래 상세 조회 - 거래 ID: {}", transactionId);

        return transactionRepository.findById(transactionId)
                .map(TransactionDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public Optional<TransactionDto> getTransactionByNumber(String transactionNumber) {
        log.info("거래번호로 거래 조회 - 거래번호: {}", transactionNumber);

        return transactionRepository.findByTransactionNumber(transactionNumber)
                .map(TransactionDto::fromEntity);
    }

    private Transaction createTransaction(Long fromAccountId, Long toAccountId, 
                                        Transaction.TransactionType type, Transaction.TransactionCategory category,
                                        BigDecimal amount, String description, String memo, String referenceNumber) {

        Transaction.TransactionDirection direction = Transaction.TransactionDirection
                .fromTransactionType(type, fromAccountId != null);

        return Transaction.builder()
                .transactionNumber(Transaction.generateTransactionNumber())
                .fromAccountId(fromAccountId)
                .toAccountId(toAccountId)
                .transactionType(type)
                .transactionCategory(category)
                .amount(amount)
                .transactionDirection(direction)
                .description(description)
                .memo(memo)
                .referenceNumber(referenceNumber)
                .transactionStatus(Transaction.TransactionStatus.PENDING)
                .transactionDate(LocalDateTime.now())
                .build();
    }
}