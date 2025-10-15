package com.hanainplan.domain.fund.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanainplan.domain.banking.client.HanaBankClient;
import com.hanainplan.domain.banking.entity.BankingAccount;
import com.hanainplan.domain.banking.entity.IrpAccount;
import com.hanainplan.domain.banking.repository.AccountRepository;
import com.hanainplan.domain.banking.repository.IrpAccountRepository;
import com.hanainplan.domain.fund.dto.FundPurchaseRequestDto;
import com.hanainplan.domain.fund.dto.FundPurchaseResponseDto;
import com.hanainplan.domain.fund.dto.FundRedemptionRequestDto;
import com.hanainplan.domain.fund.dto.FundRedemptionResponseDto;
import com.hanainplan.domain.user.entity.User;
import com.hanainplan.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FundSubscriptionService {

    private final HanaBankClient hanaBankClient;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final IrpAccountRepository irpAccountRepository;
    private final AccountRepository accountRepository;
    private final com.hanainplan.domain.banking.repository.TransactionRepository transactionRepository;
    private final FundPortfolioSyncService fundPortfolioSyncService;
    private final com.hanainplan.domain.banking.service.IrpLimitService irpLimitService;

    @Transactional
    public FundPurchaseResponseDto purchaseFund(FundPurchaseRequestDto request) {
        log.info("펀드 매수 요청 - userId: {}, childFundCd: {}, amount: {}",
                request.getUserId(), request.getChildFundCd(), request.getPurchaseAmount());

        try {
            request.validate();

            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + request.getUserId()));

            String realCustomerCi = user.getCi();
            if (realCustomerCi == null || realCustomerCi.isEmpty()) {
                throw new IllegalArgumentException("사용자 CI 정보가 없습니다. 사용자 ID: " + request.getUserId());
            }

            log.info("사용자 정보 조회 완료 - userId: {}, 실제 CI: {}", request.getUserId(), realCustomerCi);

            try {
                irpLimitService.checkAnnualLimit(realCustomerCi, request.getPurchaseAmount());
                log.info("IRP 연간 한도 체크 통과 - 금액: {}원", request.getPurchaseAmount());
            } catch (com.hanainplan.domain.banking.exception.IrpLimitExceededException e) {
                log.warn("펀드 매수 실패 - IRP 한도 초과: {}", e.getMessage());
                return FundPurchaseResponseDto.builder()
                        .success(false)
                        .message("IRP 연간 납입 한도 초과")
                        .errorMessage(e.getMessage())
                        .build();
            }

            Map<String, Object> bankRequest = new HashMap<>();
            bankRequest.put("customerCi", realCustomerCi);
            bankRequest.put("childFundCd", request.getChildFundCd());
            bankRequest.put("purchaseAmount", request.getPurchaseAmount());

            log.info("하나은행 API 호출 - 실제 customerCi: {}, childFundCd: {}", 
                    realCustomerCi, request.getChildFundCd());

            Map<String, Object> bankResponse = hanaBankClient.purchaseFund(bankRequest);

            FundPurchaseResponseDto response = objectMapper.convertValue(bankResponse, FundPurchaseResponseDto.class);

            if (response.isSuccess()) {
                log.info("펀드 매수 성공 - subscriptionId: {}", response.getSubscriptionId());

                syncIrpAccountBalance(realCustomerCi, request.getPurchaseAmount());

                createHanainplanPurchaseTransaction(
                    realCustomerCi,
                    response.getIrpAccountNumber(),
                    request.getPurchaseAmount(),
                    response.getIrpBalanceAfter(),
                    response.getFundName(),
                    response.getClassCode()
                );

                try {
                    fundPortfolioSyncService.syncUserPortfolio(realCustomerCi);
                    fundPortfolioSyncService.syncUserTransactions(realCustomerCi);
                    log.info("펀드 포트폴리오 및 거래내역 동기화 완료 - customerCi: {}", realCustomerCi);
                } catch (Exception e) {
                    log.error("펀드 포트폴리오 동기화 실패 - customerCi: {}", realCustomerCi, e);
                }
            } else {
                log.warn("펀드 매수 실패 - {}", response.getErrorMessage());
            }

            return response;

        } catch (Exception e) {
            log.error("펀드 매수 실패", e);
            return FundPurchaseResponseDto.builder()
                    .success(false)
                    .message("펀드 매수에 실패했습니다")
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    private void syncIrpAccountBalance(String customerCi, BigDecimal withdrawAmount) {
        try {
            List<IrpAccount> irpAccounts = irpAccountRepository.findByCustomerCiOrderByCreatedDateDesc(customerCi);
            if (!irpAccounts.isEmpty()) {
                IrpAccount irpAccount = irpAccounts.get(0);
                BigDecimal oldBalance = irpAccount.getCurrentBalance();
                BigDecimal newBalance = oldBalance.subtract(withdrawAmount);

                irpAccount.setCurrentBalance(newBalance);
                irpAccountRepository.save(irpAccount);

                log.info("하나인플랜 IRP 계좌 출금 완료 - 계좌번호: {}, {}원 -> {}원",
                        irpAccount.getAccountNumber(), oldBalance, newBalance);

                log.info("통합 계좌 테이블에서 IRP 계좌 조회 시도 - 계좌번호: {}", irpAccount.getAccountNumber());
                Optional<BankingAccount> bankingAccountOpt = accountRepository.findByAccountNumber(irpAccount.getAccountNumber());

                if (bankingAccountOpt.isPresent()) {
                    BankingAccount bankingAccount = bankingAccountOpt.get();
                    log.info("통합 계좌 발견 - 계좌 ID: {}, 계좌번호: {}, 현재 잔액: {}", 
                            bankingAccount.getAccountId(), bankingAccount.getAccountNumber(), bankingAccount.getBalance());

                    bankingAccount.setBalance(newBalance);
                    accountRepository.save(bankingAccount);
                    log.info("✅ 통합 계좌 테이블 잔액 업데이트 완료 - 계좌번호: {}, 새 잔액: {}", 
                            irpAccount.getAccountNumber(), newBalance);
                } else {
                    log.error("❌ 통합 계좌 테이블에서 IRP 계좌를 찾을 수 없습니다 - 계좌번호: {}", 
                            irpAccount.getAccountNumber());
                }
            } else {
                log.warn("하나인플랜에 IRP 계좌가 없습니다 - customerCi: {}", customerCi);
            }
        } catch (Exception e) {
            log.error("IRP 계좌 잔액 동기화 실패 - customerCi: {}", customerCi, e);
        }
    }

    private void syncIrpAccountBalanceDeposit(String customerCi, BigDecimal depositAmount) {
        try {
            List<IrpAccount> irpAccounts = irpAccountRepository.findByCustomerCiOrderByCreatedDateDesc(customerCi);
            if (!irpAccounts.isEmpty()) {
                IrpAccount irpAccount = irpAccounts.get(0);
                BigDecimal oldBalance = irpAccount.getCurrentBalance();
                BigDecimal newBalance = oldBalance.add(depositAmount);

                irpAccount.setCurrentBalance(newBalance);
                irpAccountRepository.save(irpAccount);

                log.info("하나인플랜 IRP 계좌 입금 완료 - 계좌번호: {}, {}원 -> {}원",
                        irpAccount.getAccountNumber(), oldBalance, newBalance);

                log.info("통합 계좌 테이블에서 IRP 계좌 조회 시도 - 계좌번호: {}", irpAccount.getAccountNumber());
                Optional<BankingAccount> bankingAccountOpt = accountRepository.findByAccountNumber(irpAccount.getAccountNumber());

                if (bankingAccountOpt.isPresent()) {
                    BankingAccount bankingAccount = bankingAccountOpt.get();
                    log.info("통합 계좌 발견 - 계좌 ID: {}, 계좌번호: {}, 현재 잔액: {}", 
                            bankingAccount.getAccountId(), bankingAccount.getAccountNumber(), bankingAccount.getBalance());

                    bankingAccount.setBalance(newBalance);
                    accountRepository.save(bankingAccount);
                    log.info("✅ 통합 계좌 테이블 잔액 업데이트 완료 - 계좌번호: {}, 새 잔액: {}", 
                            irpAccount.getAccountNumber(), newBalance);
                } else {
                    log.error("❌ 통합 계좌 테이블에서 IRP 계좌를 찾을 수 없습니다 - 계좌번호: {}", 
                            irpAccount.getAccountNumber());
                }
            } else {
                log.warn("하나인플랜에 IRP 계좌가 없습니다 - customerCi: {}", customerCi);
            }
        } catch (Exception e) {
            log.error("IRP 계좌 잔액 동기화 실패 - customerCi: {}", customerCi, e);
        }
    }

    @Transactional
    public FundRedemptionResponseDto redeemFund(FundRedemptionRequestDto request) {
        log.info("펀드 매도 요청 - userId: {}, subscriptionId: {}, sellUnits: {}",
                request.getUserId(), request.getSubscriptionId(), request.getSellUnits());

        try {
            request.validate();

            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + request.getUserId()));

            String realCustomerCi = user.getCi();
            if (realCustomerCi == null || realCustomerCi.isEmpty()) {
                throw new IllegalArgumentException("사용자 CI 정보가 없습니다. 사용자 ID: " + request.getUserId());
            }

            log.info("사용자 정보 조회 완료 - userId: {}, 실제 CI: {}", request.getUserId(), realCustomerCi);

            Map<String, Object> bankRequest = new HashMap<>();
            bankRequest.put("customerCi", realCustomerCi);
            bankRequest.put("subscriptionId", request.getSubscriptionId());
            bankRequest.put("sellUnits", request.getSellUnits());
            bankRequest.put("sellAll", request.getSellAll());

            log.info("하나은행 API 호출 - 실제 customerCi: {}, subscriptionId: {}", 
                    realCustomerCi, request.getSubscriptionId());

            Map<String, Object> bankResponse = hanaBankClient.redeemFund(bankRequest);

            FundRedemptionResponseDto response = objectMapper.convertValue(
                    bankResponse, FundRedemptionResponseDto.class);

            if (response.isSuccess()) {
                log.info("펀드 매도 성공 - 매도 좌수: {}, 실수령액: {}원, 실현 손익: {}원",
                        response.getSellUnits(), response.getNetAmount(), response.getProfit());

                BigDecimal depositAmount = response.getNetAmount();
                if (depositAmount != null && depositAmount.compareTo(BigDecimal.ZERO) > 0) {
                    syncIrpAccountBalanceDeposit(realCustomerCi, depositAmount);

                    createHanainplanRedemptionTransaction(
                        realCustomerCi,
                        response.getIrpAccountNumber(),
                        depositAmount,
                        response.getIrpBalanceAfter(),
                        response.getFundName(),
                        response.getClassCode(),
                        response.getProfit()
                    );

                    try {
                        fundPortfolioSyncService.syncUserPortfolio(realCustomerCi);
                        fundPortfolioSyncService.syncUserTransactions(realCustomerCi);
                        log.info("펀드 포트폴리오 및 거래내역 동기화 완료 - customerCi: {}", realCustomerCi);
                    } catch (Exception e) {
                        log.error("펀드 포트폴리오 동기화 실패 - customerCi: {}", realCustomerCi, e);
                    }
                }
            } else {
                log.warn("펀드 매도 실패 - {}", response.getErrorMessage());
            }

            return response;

        } catch (Exception e) {
            log.error("펀드 매도 실패", e);
            return FundRedemptionResponseDto.builder()
                    .success(false)
                    .message("펀드 매도에 실패했습니다")
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    public java.util.List<java.util.Map<String, Object>> getActiveSubscriptions(Long userId) {
        log.info("활성 펀드 가입 목록 조회 - userId: {}", userId);

        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

            String realCustomerCi = user.getCi();
            if (realCustomerCi == null || realCustomerCi.isEmpty()) {
                throw new IllegalArgumentException("사용자 CI 정보가 없습니다. 사용자 ID: " + userId);
            }

            log.info("사용자 정보 조회 완료 - userId: {}, 실제 CI: {}", userId, realCustomerCi);

            @SuppressWarnings("unchecked")
            java.util.List<java.util.Map<String, Object>> subscriptions = 
                    (java.util.List<java.util.Map<String, Object>>) hanaBankClient.getActiveSubscriptions(realCustomerCi);

            log.info("활성 펀드 조회 완료 - userId: {}, 가입 수: {}", userId, subscriptions.size());
            return subscriptions;

        } catch (Exception e) {
            log.error("활성 펀드 조회 실패 - userId: {}", userId, e);
            throw new RuntimeException("활성 펀드 조회에 실패했습니다: " + e.getMessage(), e);
        }
    }

    public java.util.List<java.util.Map<String, Object>> getUserTransactions(Long userId) {
        log.info("펀드 거래 내역 조회 - userId: {}", userId);

        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

            String realCustomerCi = user.getCi();
            if (realCustomerCi == null || realCustomerCi.isEmpty()) {
                throw new IllegalArgumentException("사용자 CI 정보가 없습니다. 사용자 ID: " + userId);
            }

            log.info("사용자 정보 조회 완료 - userId: {}, 실제 CI: {}", userId, realCustomerCi);

            @SuppressWarnings("unchecked")
            java.util.List<java.util.Map<String, Object>> transactions = 
                    (java.util.List<java.util.Map<String, Object>>) hanaBankClient.getCustomerTransactions(realCustomerCi);

            log.info("거래 내역 조회 완료 - userId: {}, 거래 수: {}", userId, transactions.size());
            return transactions;

        } catch (Exception e) {
            log.error("거래 내역 조회 실패 - userId: {}", userId, e);
            throw new RuntimeException("거래 내역 조회에 실패했습니다: " + e.getMessage(), e);
        }
    }

    public java.util.Map<String, Object> getTransactionStats(Long userId) {
        log.info("펀드 거래 통계 조회 - userId: {}", userId);

        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

            String realCustomerCi = user.getCi();
            if (realCustomerCi == null || realCustomerCi.isEmpty()) {
                throw new IllegalArgumentException("사용자 CI 정보가 없습니다. 사용자 ID: " + userId);
            }

            log.info("사용자 정보 조회 완료 - userId: {}, 실제 CI: {}", userId, realCustomerCi);

            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> stats = 
                    (java.util.Map<String, Object>) hanaBankClient.getTransactionStats(realCustomerCi);

            log.info("거래 통계 조회 완료 - userId: {}", userId);
            return stats;

        } catch (Exception e) {
            log.error("거래 통계 조회 실패 - userId: {}", userId, e);
            throw new RuntimeException("거래 통계 조회에 실패했습니다: " + e.getMessage(), e);
        }
    }

    private void createHanainplanPurchaseTransaction(
            String customerCi,
            String accountNumber,
            BigDecimal amount,
            BigDecimal balanceAfter,
            String fundName,
            String classCode) {

        try {
            java.util.Optional<BankingAccount> accountOpt = accountRepository.findByAccountNumber(accountNumber);
            if (accountOpt.isEmpty()) {
                log.warn("하나인플랜 거래내역 생성 실패 - IRP 계좌를 찾을 수 없음: {}", accountNumber);
                return;
            }

            BankingAccount irpAccount = accountOpt.get();

            String transactionNumber = com.hanainplan.domain.banking.entity.Transaction.generateTransactionNumber();

            com.hanainplan.domain.banking.entity.Transaction transaction = 
                    com.hanainplan.domain.banking.entity.Transaction.builder()
                    .transactionNumber(transactionNumber)
                    .fromAccountId(irpAccount.getAccountId())
                    .fromAccountNumber(accountNumber)
                    .transactionType(com.hanainplan.domain.banking.entity.Transaction.TransactionType.WITHDRAWAL)
                    .transactionCategory(com.hanainplan.domain.banking.entity.Transaction.TransactionCategory.INVESTMENT)
                    .transactionDirection(com.hanainplan.domain.banking.entity.Transaction.TransactionDirection.DEBIT)
                    .amount(amount)
                    .balanceAfter(balanceAfter)
                    .description(String.format("펀드 매수 - %s (%s클래스)", fundName, classCode))
                    .transactionStatus(com.hanainplan.domain.banking.entity.Transaction.TransactionStatus.COMPLETED)
                    .transactionDate(java.time.LocalDateTime.now())
                    .processedDate(java.time.LocalDateTime.now())
                    .referenceNumber(transactionNumber)
                    .build();

            transactionRepository.save(transaction);
            log.info("하나인플랜 펀드 매수 거래내역 생성 완료 - transactionNumber: {}, 계좌번호: {}", 
                    transactionNumber, accountNumber);

        } catch (Exception e) {
            log.error("하나인플랜 펀드 매수 거래내역 생성 실패 - 계좌번호: {}", accountNumber, e);
        }
    }

    private void createHanainplanRedemptionTransaction(
            String customerCi,
            String accountNumber,
            BigDecimal amount,
            BigDecimal balanceAfter,
            String fundName,
            String classCode,
            BigDecimal profit) {

        try {
            java.util.Optional<BankingAccount> accountOpt = accountRepository.findByAccountNumber(accountNumber);
            if (accountOpt.isEmpty()) {
                log.warn("하나인플랜 거래내역 생성 실패 - IRP 계좌를 찾을 수 없음: {}", accountNumber);
                return;
            }

            BankingAccount irpAccount = accountOpt.get();

            String transactionNumber = com.hanainplan.domain.banking.entity.Transaction.generateTransactionNumber();

            String profitText = profit != null && profit.compareTo(BigDecimal.ZERO) >= 0 
                    ? String.format("(+%,.0f원)", profit) 
                    : String.format("(%,.0f원)", profit != null ? profit : BigDecimal.ZERO);

            com.hanainplan.domain.banking.entity.Transaction transaction = 
                    com.hanainplan.domain.banking.entity.Transaction.builder()
                    .transactionNumber(transactionNumber)
                    .toAccountId(irpAccount.getAccountId())
                    .toAccountNumber(accountNumber)
                    .transactionType(com.hanainplan.domain.banking.entity.Transaction.TransactionType.DEPOSIT)
                    .transactionCategory(com.hanainplan.domain.banking.entity.Transaction.TransactionCategory.INVESTMENT)
                    .transactionDirection(com.hanainplan.domain.banking.entity.Transaction.TransactionDirection.CREDIT)
                    .amount(amount)
                    .balanceAfter(balanceAfter)
                    .description(String.format("펀드 매도 - %s (%s클래스) %s", fundName, classCode, profitText))
                    .transactionStatus(com.hanainplan.domain.banking.entity.Transaction.TransactionStatus.COMPLETED)
                    .transactionDate(java.time.LocalDateTime.now())
                    .processedDate(java.time.LocalDateTime.now())
                    .referenceNumber(transactionNumber)
                    .build();

            transactionRepository.save(transaction);
            log.info("하나인플랜 펀드 매도 거래내역 생성 완료 - transactionNumber: {}, 계좌번호: {}", 
                    transactionNumber, accountNumber);

        } catch (Exception e) {
            log.error("하나인플랜 펀드 매도 거래내역 생성 실패 - 계좌번호: {}", accountNumber, e);
        }
    }
}