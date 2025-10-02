package com.hanainplan.domain.banking.service;

import com.hanainplan.domain.banking.client.HanaBankClient;
import com.hanainplan.domain.banking.dto.DepositSubscriptionRequest;
import com.hanainplan.domain.banking.entity.BankingAccount;
import com.hanainplan.domain.banking.entity.DepositSubscription;
import com.hanainplan.domain.banking.entity.IrpAccount;
import com.hanainplan.domain.banking.entity.Transaction;
import com.hanainplan.domain.banking.repository.AccountRepository;
import com.hanainplan.domain.banking.repository.DepositSubscriptionRepository;
import com.hanainplan.domain.banking.repository.IrpAccountRepository;
import com.hanainplan.domain.banking.repository.TransactionRepository;
import com.hanainplan.domain.banking.util.InterestRateCalculator;
import com.hanainplan.domain.user.entity.User;
import com.hanainplan.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * HANAinPLAN 정기예금 가입 통합 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DepositSubscriptionIntegrationService {

    private final DepositSubscriptionRepository depositSubscriptionRepository;
    private final HanaBankClient hanaBankClient;
    private final UserRepository userRepository;
    private final IrpAccountRepository irpAccountRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    /**
     * 정기예금 가입
     */
    @Transactional
    public Map<String, Object> subscribeDeposit(DepositSubscriptionRequest request) {
        log.info("HANAinPLAN 정기예금 가입 요청: userId={}, bankCode={}, depositCode={}", 
                request.getUserId(), request.getBankCode(), request.getDepositCode());

        // 1. userId로 User 조회하여 customerCi 가져오기
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + request.getUserId()));
        
        if (user.getCi() == null || user.getCi().isEmpty()) {
            throw new RuntimeException("실명인증이 완료되지 않은 사용자입니다. CI 값이 없습니다.");
        }
        
        String customerCi = user.getCi();
        log.info("사용자 CI 조회 완료: userId={}, ci={}", request.getUserId(), customerCi);

        // 2. 요청 검증
        request.validateContractPeriod();

        // 3. IRP 계좌 조회 및 잔액 확인
        IrpAccount irpAccount = irpAccountRepository.findByAccountNumber(request.getIrpAccountNumber())
                .orElseThrow(() -> new RuntimeException("IRP 계좌를 찾을 수 없습니다: " + request.getIrpAccountNumber()));
        
        if (!irpAccount.getCustomerCi().equals(customerCi)) {
            throw new RuntimeException("IRP 계좌 소유자가 일치하지 않습니다");
        }
        
        if (!"ACTIVE".equals(irpAccount.getAccountStatus())) {
            throw new RuntimeException("활성화된 IRP 계좌가 아닙니다");
        }
        
        if (irpAccount.getCurrentBalance().compareTo(request.getSubscriptionAmount()) < 0) {
            throw new RuntimeException("IRP 계좌 잔액이 부족합니다. 현재 잔액: " + irpAccount.getCurrentBalance() + "원");
        }

        // 4. IRP 계좌에서 정기예금 가입 금액 차감 (tb_irp_account)
        BigDecimal newIrpBalance = irpAccount.getCurrentBalance().subtract(request.getSubscriptionAmount());
        irpAccount.setCurrentBalance(newIrpBalance);
        irpAccountRepository.save(irpAccount);
        
        log.info("하나인플랜 IRP 계좌 잔액 차감 완료 (tb_irp_account) - 계좌: {}, 차감액: {}원, 남은 잔액: {}원", 
                request.getIrpAccountNumber(), request.getSubscriptionAmount(), newIrpBalance);
        
        // 5. IRP 계좌의 BankingAccount도 차감 (tb_banking_account)
        BankingAccount irpBankingAccount = accountRepository.findByAccountNumber(request.getIrpAccountNumber())
                .orElseThrow(() -> new RuntimeException("IRP 계좌(BankingAccount)를 찾을 수 없습니다: " + request.getIrpAccountNumber()));
        
        irpBankingAccount.setBalance(newIrpBalance);
        accountRepository.save(irpBankingAccount);
        
        log.info("하나인플랜 IRP 계좌 잔액 차감 완료 (tb_banking_account) - 계좌: {}, 남은 잔액: {}원", 
                request.getIrpAccountNumber(), newIrpBalance);

        // 5. 은행별 API 호출하여 실제 가입 처리
        Map<String, Object> bankResponse = callBankSubscriptionApi(request, customerCi);

        // 6. HANAinPLAN DB에 가입 내역 저장
        DepositSubscription subscription = saveSubscription(request, customerCi, bankResponse);

        // 7. 거래내역 생성 (IRP 계좌 출금)
        createDepositSubscriptionTransaction(request, irpAccount, subscription);

        // 8. 응답 구성
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "정기예금 가입이 완료되었습니다");
        response.put("subscriptionId", subscription.getSubscriptionId());
        response.put("accountNumber", subscription.getAccountNumber());
        response.put("bankName", subscription.getBankName());
        response.put("expectedInterest", bankResponse.get("expectedInterest"));
        response.put("expectedMaturityAmount", bankResponse.get("expectedMaturityAmount"));
        response.put("maturityDate", subscription.getMaturityDate());

        log.info("정기예금 가입 완료: subscriptionId={}, accountNumber={}", 
                subscription.getSubscriptionId(), subscription.getAccountNumber());

        return response;
    }

    /**
     * 은행 API 호출 (OpenFeign 사용)
     */
    private Map<String, Object> callBankSubscriptionApi(DepositSubscriptionRequest request, String customerCi) {
        log.info("은행 API 호출: bankCode={}, depositCode={}", request.getBankCode(), request.getDepositCode());

        try {
            // 요청 바디 구성
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("customerCi", customerCi);
            requestBody.put("irpAccountNumber", request.getIrpAccountNumber());
            requestBody.put("linkedAccountNumber", request.getLinkedAccountNumber());
            requestBody.put("depositCode", request.getDepositCode());
            requestBody.put("productType", request.getProductType());
            requestBody.put("contractPeriod", request.getContractPeriod());
            requestBody.put("subscriptionAmount", request.getSubscriptionAmount());

            // 은행별로 Feign Client 호출 (하나은행만 Feign Client 사용, 추후 확장 가능)
            Map<String, Object> response;
            switch (request.getBankCode().toUpperCase()) {
                case "081": // 하나은행 (코드)
                case "HANA": // 하나은행 (문자열)
                    response = hanaBankClient.subscribeDeposit(requestBody);
                    break;
                // 추후 다른 은행 Feign Client 추가 가능
                // case "088": // 신한은행
                // case "SHINHAN":
                //     response = shinhanBankClient.subscribeDeposit(requestBody);
                //     break;
                default:
                    throw new RuntimeException("지원하지 않는 은행입니다: " + request.getBankCode());
            }

            log.info("은행 API 호출 성공: bankCode={}", request.getBankCode());
            return response;

        } catch (Exception e) {
            log.error("은행 API 호출 오류: bankCode={}, error={}", request.getBankCode(), e.getMessage());
            throw new RuntimeException("은행 API 호출 실패: " + e.getMessage(), e);
        }
    }

    /**
     * HANAinPLAN DB에 가입 내역 저장
     */
    private DepositSubscription saveSubscription(DepositSubscriptionRequest request, String customerCi, Map<String, Object> bankResponse) {
        // 금리 계산
        BigDecimal appliedRate = InterestRateCalculator.getBaseRate(
                request.getProductType(), 
                request.getContractPeriod());

        // 만기일 계산
        LocalDate subscriptionDate = LocalDate.now();
        LocalDate maturityDate = InterestRateCalculator.calculateMaturityDate(
                subscriptionDate, 
                request.getProductType(), 
                request.getContractPeriod());

        DepositSubscription subscription = DepositSubscription.builder()
                .userId(request.getUserId())
                .customerCi(customerCi)
                .accountNumber((String) bankResponse.get("accountNumber"))
                .status("ACTIVE")
                .subscriptionDate(subscriptionDate)
                .maturityDate(maturityDate)
                .contractPeriod(request.getContractPeriod())
                .productType(request.getProductType())
                .bankName(getBankName(request.getBankCode()))
                .bankCode(request.getBankCode())
                .depositCode(request.getDepositCode())
                .rate(appliedRate)
                .currentBalance(request.getSubscriptionAmount())
                .unpaidInterest(BigDecimal.ZERO)
                .build();

        return depositSubscriptionRepository.save(subscription);
    }

    /**
     * 사용자의 정기예금 가입 내역 조회
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getUserSubscriptions(Long userId) {
        log.info("사용자 정기예금 가입 내역 조회: userId={}", userId);

        List<DepositSubscription> subscriptions = depositSubscriptionRepository
                .findByUserIdOrderBySubscriptionDateDesc(userId);

        return subscriptions.stream()
                .map(this::convertToMap)
                .collect(Collectors.toList());
    }

    /**
     * 계좌번호로 가입 내역 조회
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getSubscriptionByAccountNumber(String accountNumber) {
        log.info("계좌번호로 가입 내역 조회: accountNumber={}", accountNumber);

        DepositSubscription subscription = depositSubscriptionRepository
                .findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("가입 내역을 찾을 수 없습니다: " + accountNumber));

        return convertToMap(subscription);
    }

    /**
     * 중도해지 (OpenFeign 사용)
     */
    @Transactional
    public Map<String, Object> terminateEarly(String accountNumber) {
        log.info("정기예금 중도해지 요청: accountNumber={}", accountNumber);

        DepositSubscription subscription = depositSubscriptionRepository
                .findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("가입 내역을 찾을 수 없습니다: " + accountNumber));

        if (!"ACTIVE".equals(subscription.getStatus())) {
            throw new RuntimeException("활성 상태의 예금만 해지할 수 있습니다");
        }

        try {
            // 은행별 Feign Client 호출 (하나은행만 구현)
            Map<String, Object> bankResponse;
            switch (subscription.getBankCode()) {
                case "081": // 하나은행
                    bankResponse = hanaBankClient.terminateDeposit(accountNumber);
                    break;
                default:
                    throw new RuntimeException("지원하지 않는 은행입니다: " + subscription.getBankCode());
            }

            if (bankResponse != null) {

                // HANAinPLAN DB 업데이트
                subscription.setStatus("CLOSED");
                if (bankResponse.get("unpaidInterest") != null) {
                    subscription.setUnpaidInterest(new BigDecimal(bankResponse.get("unpaidInterest").toString()));
                }
                if (bankResponse.get("currentBalance") != null) {
                    subscription.setCurrentBalance(new BigDecimal(bankResponse.get("currentBalance").toString()));
                }
                
                depositSubscriptionRepository.save(subscription);

                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "중도해지가 완료되었습니다");
                result.put("accountNumber", accountNumber);
                result.put("earlyInterest", subscription.getUnpaidInterest());
                result.put("totalAmount", subscription.getCurrentBalance());

                return result;
            } else {
                throw new RuntimeException("은행 API 중도해지 처리 실패");
            }

        } catch (Exception e) {
            log.error("중도해지 처리 오류: {}", e.getMessage());
            throw new RuntimeException("중도해지 처리 실패: " + e.getMessage(), e);
        }
    }

    /**
     * Entity를 Map으로 변환
     */
    private Map<String, Object> convertToMap(DepositSubscription subscription) {
        Map<String, Object> map = new HashMap<>();
        map.put("subscriptionId", subscription.getSubscriptionId());
        map.put("userId", subscription.getUserId());
        map.put("customerCi", subscription.getCustomerCi());
        map.put("accountNumber", subscription.getAccountNumber());
        map.put("status", subscription.getStatus());
        map.put("subscriptionDate", subscription.getSubscriptionDate());
        map.put("maturityDate", subscription.getMaturityDate());
        map.put("contractPeriod", subscription.getContractPeriod());
        map.put("contractPeriodUnit", subscription.getProductType() == 2 ? "일" : "개월");
        map.put("productType", subscription.getProductType());
        map.put("productTypeName", getProductTypeName(subscription.getProductType()));
        map.put("bankName", subscription.getBankName());
        map.put("bankCode", subscription.getBankCode());
        map.put("depositCode", subscription.getDepositCode());
        map.put("rate", subscription.getRate());
        map.put("currentBalance", subscription.getCurrentBalance());
        map.put("unpaidInterest", subscription.getUnpaidInterest());
        map.put("lastInterestCalculationDate", subscription.getLastInterestCalculationDate());
        map.put("nextInterestPaymentDate", subscription.getNextInterestPaymentDate());
        return map;
    }

    /**
     * 은행 이름 조회
     */
    private String getBankName(String bankCode) {
        switch (bankCode.toUpperCase()) {
            case "HANA":
                return "하나은행";
            case "SHINHAN":
                return "신한은행";
            case "KOOKMIN":
                return "국민은행";
            default:
                return bankCode;
        }
    }

    /**
     * 상품 유형 이름 조회
     */
    private String getProductTypeName(Integer productType) {
        if (productType == null) return "알 수 없음";
        switch (productType) {
            case 0: return "일반";
            case 1: return "디폴트옵션";
            case 2: return "일단위";
            default: return "알 수 없음";
        }
    }

    /**
     * 정기예금 가입 거래내역 생성
     */
    private void createDepositSubscriptionTransaction(DepositSubscriptionRequest request, 
                                                      IrpAccount irpAccount, 
                                                      DepositSubscription subscription) {
        try {
            // IRP 계좌를 BankingAccount로 조회
            BankingAccount irpBankingAccount = accountRepository.findByAccountNumber(request.getIrpAccountNumber())
                    .orElseThrow(() -> new RuntimeException("IRP 계좌를 찾을 수 없습니다: " + request.getIrpAccountNumber()));
            
            String transactionNumber = Transaction.generateTransactionNumber();
            
            // IRP 계좌 출금 거래내역 생성 (정기예금 가입)
            Transaction transaction = Transaction.builder()
                    .transactionNumber(transactionNumber)
                    .fromAccountId(irpBankingAccount.getAccountId())
                    .toAccountId(null) // 정기예금 계좌는 별도 시스템
                    .transactionType(Transaction.TransactionType.TRANSFER)
                    .transactionCategory(Transaction.TransactionCategory.INVESTMENT)
                    .amount(request.getSubscriptionAmount())
                    .balanceAfter(irpAccount.getCurrentBalance()) // 이미 차감된 잔액
                    .transactionDirection(Transaction.TransactionDirection.DEBIT)
                    .description("정기예금 가입: " + subscription.getDepositCode())
                    .transactionStatus(Transaction.TransactionStatus.COMPLETED)
                    .transactionDate(java.time.LocalDateTime.now())
                    .processedDate(java.time.LocalDateTime.now())
                    .referenceNumber("DEPOSIT_SUB_" + subscription.getAccountNumber())
                    .memo("정기예금(" + subscription.getAccountNumber() + ") 가입")
                    .build();
            
            transactionRepository.save(transaction);
            
            log.info("IRP 계좌 출금 거래내역 생성 완료 - IRP계좌: {}, 금액: {}", 
                    request.getIrpAccountNumber(), request.getSubscriptionAmount());
            
        } catch (Exception e) {
            log.error("정기예금 가입 거래내역 생성 실패: {}", e.getMessage(), e);
            // 거래내역 저장 실패는 치명적이지 않으므로 경고만 남김
        }
    }
}

