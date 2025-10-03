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

/**
 * 하나인플랜 펀드 매수 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FundSubscriptionService {

    private final HanaBankClient hanaBankClient;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final IrpAccountRepository irpAccountRepository;
    private final AccountRepository accountRepository;

    /**
     * 펀드 매수
     */
    @Transactional
    public FundPurchaseResponseDto purchaseFund(FundPurchaseRequestDto request) {
        log.info("펀드 매수 요청 - userId: {}, childFundCd: {}, amount: {}",
                request.getUserId(), request.getChildFundCd(), request.getPurchaseAmount());

        try {
            // 유효성 검증
            request.validate();

            // userId로 실제 사용자 정보 조회 (실제 CI 값 가져오기)
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + request.getUserId()));
            
            String realCustomerCi = user.getCi();
            if (realCustomerCi == null || realCustomerCi.isEmpty()) {
                throw new IllegalArgumentException("사용자 CI 정보가 없습니다. 사용자 ID: " + request.getUserId());
            }
            
            log.info("사용자 정보 조회 완료 - userId: {}, 실제 CI: {}", request.getUserId(), realCustomerCi);

            // 하나은행 API 호출용 요청 변환 (실제 CI 사용)
            Map<String, Object> bankRequest = new HashMap<>();
            bankRequest.put("customerCi", realCustomerCi);  // 실제 CI 사용!
            bankRequest.put("childFundCd", request.getChildFundCd());
            bankRequest.put("purchaseAmount", request.getPurchaseAmount());
            // irpAccountNumber는 하나은행 API에서 customerCi로 자동 조회
            
            log.info("하나은행 API 호출 - 실제 customerCi: {}, childFundCd: {}", 
                    realCustomerCi, request.getChildFundCd());

            // 하나은행 API 호출
            Map<String, Object> bankResponse = hanaBankClient.purchaseFund(bankRequest);

            // 응답 변환
            FundPurchaseResponseDto response = objectMapper.convertValue(bankResponse, FundPurchaseResponseDto.class);

            if (response.isSuccess()) {
                log.info("펀드 매수 성공 - subscriptionId: {}", response.getSubscriptionId());
                
                // 하나인플랜 DB의 IRP 계좌 잔액도 동기화
                syncIrpAccountBalance(realCustomerCi, request.getPurchaseAmount());
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

    /**
     * 하나인플랜 DB의 IRP 계좌 잔액 동기화 (출금)
     */
    private void syncIrpAccountBalance(String customerCi, BigDecimal withdrawAmount) {
        try {
            // 1. IRP 계좌 테이블 업데이트 (tb_irp_account)
            List<IrpAccount> irpAccounts = irpAccountRepository.findByCustomerCiOrderByCreatedDateDesc(customerCi);
            if (!irpAccounts.isEmpty()) {
                IrpAccount irpAccount = irpAccounts.get(0); // 첫 번째 활성 계좌
                BigDecimal oldBalance = irpAccount.getCurrentBalance();
                BigDecimal newBalance = oldBalance.subtract(withdrawAmount);
                
                irpAccount.setCurrentBalance(newBalance);
                irpAccountRepository.save(irpAccount);
                
                log.info("하나인플랜 IRP 계좌 출금 완료 - 계좌번호: {}, {}원 -> {}원",
                        irpAccount.getAccountNumber(), oldBalance, newBalance);

                // 2. 통합 계좌 테이블 업데이트 (tb_banking_account, account_type=6)
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
            // 동기화 실패는 로그만 남기고 매수 성공은 유지
        }
    }

    /**
     * 하나인플랜 DB의 IRP 계좌 잔액 동기화 (입금)
     */
    private void syncIrpAccountBalanceDeposit(String customerCi, BigDecimal depositAmount) {
        try {
            // 1. IRP 계좌 테이블 업데이트 (tb_irp_account)
            List<IrpAccount> irpAccounts = irpAccountRepository.findByCustomerCiOrderByCreatedDateDesc(customerCi);
            if (!irpAccounts.isEmpty()) {
                IrpAccount irpAccount = irpAccounts.get(0); // 첫 번째 활성 계좌
                BigDecimal oldBalance = irpAccount.getCurrentBalance();
                BigDecimal newBalance = oldBalance.add(depositAmount);
                
                irpAccount.setCurrentBalance(newBalance);
                irpAccountRepository.save(irpAccount);
                
                log.info("하나인플랜 IRP 계좌 입금 완료 - 계좌번호: {}, {}원 -> {}원",
                        irpAccount.getAccountNumber(), oldBalance, newBalance);

                // 2. 통합 계좌 테이블 업데이트 (tb_banking_account, account_type=6)
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
            // 동기화 실패는 로그만 남기고 매도 성공은 유지
        }
    }

    /**
     * 펀드 매도 (환매)
     */
    @Transactional
    public FundRedemptionResponseDto redeemFund(FundRedemptionRequestDto request) {
        log.info("펀드 매도 요청 - userId: {}, subscriptionId: {}, sellUnits: {}",
                request.getUserId(), request.getSubscriptionId(), request.getSellUnits());

        try {
            // 유효성 검증
            request.validate();

            // userId로 실제 사용자 정보 조회 (실제 CI 값 가져오기)
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + request.getUserId()));
            
            String realCustomerCi = user.getCi();
            if (realCustomerCi == null || realCustomerCi.isEmpty()) {
                throw new IllegalArgumentException("사용자 CI 정보가 없습니다. 사용자 ID: " + request.getUserId());
            }
            
            log.info("사용자 정보 조회 완료 - userId: {}, 실제 CI: {}", request.getUserId(), realCustomerCi);

            // 하나은행 API 호출용 요청 변환 (실제 CI 사용)
            Map<String, Object> bankRequest = new HashMap<>();
            bankRequest.put("customerCi", realCustomerCi);  // 실제 CI 사용!
            bankRequest.put("subscriptionId", request.getSubscriptionId());
            bankRequest.put("sellUnits", request.getSellUnits());
            bankRequest.put("sellAll", request.getSellAll());
            // irpAccountNumber는 하나은행 API에서 customerCi로 자동 조회
            
            log.info("하나은행 API 호출 - 실제 customerCi: {}, subscriptionId: {}", 
                    realCustomerCi, request.getSubscriptionId());

            // 하나은행 API 호출
            Map<String, Object> bankResponse = hanaBankClient.redeemFund(bankRequest);

            // 응답 변환
            FundRedemptionResponseDto response = objectMapper.convertValue(
                    bankResponse, FundRedemptionResponseDto.class);

            if (response.isSuccess()) {
                log.info("펀드 매도 성공 - 매도 좌수: {}, 실수령액: {}원, 실현 손익: {}원",
                        response.getSellUnits(), response.getNetAmount(), response.getProfit());
                
                // 하나인플랜 DB의 IRP 계좌 잔액도 동기화 (입금)
                BigDecimal depositAmount = response.getNetAmount(); // 실수령액
                if (depositAmount != null && depositAmount.compareTo(BigDecimal.ZERO) > 0) {
                    syncIrpAccountBalanceDeposit(realCustomerCi, depositAmount);
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

    /**
     * 사용자의 활성 펀드 가입 목록 조회
     */
    public java.util.List<java.util.Map<String, Object>> getActiveSubscriptions(Long userId) {
        log.info("활성 펀드 가입 목록 조회 - userId: {}", userId);

        try {
            // userId로 실제 사용자 정보 조회 (실제 CI 값 가져오기)
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
            
            String realCustomerCi = user.getCi();
            if (realCustomerCi == null || realCustomerCi.isEmpty()) {
                throw new IllegalArgumentException("사용자 CI 정보가 없습니다. 사용자 ID: " + userId);
            }
            
            log.info("사용자 정보 조회 완료 - userId: {}, 실제 CI: {}", userId, realCustomerCi);

            // 하나은행 API 호출
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

    /**
     * 사용자의 펀드 거래 내역 조회
     */
    public java.util.List<java.util.Map<String, Object>> getUserTransactions(Long userId) {
        log.info("펀드 거래 내역 조회 - userId: {}", userId);

        try {
            // userId로 실제 사용자 정보 조회 (실제 CI 값 가져오기)
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
            
            String realCustomerCi = user.getCi();
            if (realCustomerCi == null || realCustomerCi.isEmpty()) {
                throw new IllegalArgumentException("사용자 CI 정보가 없습니다. 사용자 ID: " + userId);
            }
            
            log.info("사용자 정보 조회 완료 - userId: {}, 실제 CI: {}", userId, realCustomerCi);

            // 하나은행 API 호출
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

    /**
     * 사용자의 펀드 거래 통계 조회
     */
    public java.util.Map<String, Object> getTransactionStats(Long userId) {
        log.info("펀드 거래 통계 조회 - userId: {}", userId);

        try {
            // userId로 실제 사용자 정보 조회 (실제 CI 값 가져오기)
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
            
            String realCustomerCi = user.getCi();
            if (realCustomerCi == null || realCustomerCi.isEmpty()) {
                throw new IllegalArgumentException("사용자 CI 정보가 없습니다. 사용자 ID: " + userId);
            }
            
            log.info("사용자 정보 조회 완료 - userId: {}, 실제 CI: {}", userId, realCustomerCi);

            // 하나은행 API 호출
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
}

