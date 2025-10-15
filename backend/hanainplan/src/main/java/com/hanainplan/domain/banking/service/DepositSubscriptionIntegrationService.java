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

    @Transactional
    public Map<String, Object> subscribeDeposit(DepositSubscriptionRequest request) {
        log.info("HANAinPLAN 정기예금 가입 요청: userId={}, bankCode={}, depositCode={}", 
                request.getUserId(), request.getBankCode(), request.getDepositCode());

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + request.getUserId()));

        if (user.getCi() == null || user.getCi().isEmpty()) {
            throw new RuntimeException("실명인증이 완료되지 않은 사용자입니다. CI 값이 없습니다.");
        }

        String customerCi = user.getCi();
        log.info("사용자 CI 조회 완료: userId={}, ci={}", request.getUserId(), customerCi);

        request.validateContractPeriod();

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

        BigDecimal newIrpBalance = irpAccount.getCurrentBalance().subtract(request.getSubscriptionAmount());
        irpAccount.setCurrentBalance(newIrpBalance);
        irpAccountRepository.save(irpAccount);

        log.info("하나인플랜 IRP 계좌 잔액 차감 완료 (tb_irp_account) - 계좌: {}, 차감액: {}원, 남은 잔액: {}원", 
                request.getIrpAccountNumber(), request.getSubscriptionAmount(), newIrpBalance);

        BankingAccount irpBankingAccount = accountRepository.findByAccountNumber(request.getIrpAccountNumber())
                .orElseThrow(() -> new RuntimeException("IRP 계좌(BankingAccount)를 찾을 수 없습니다: " + request.getIrpAccountNumber()));

        irpBankingAccount.setBalance(newIrpBalance);
        accountRepository.save(irpBankingAccount);

        log.info("하나인플랜 IRP 계좌 잔액 차감 완료 (tb_banking_account) - 계좌: {}, 남은 잔액: {}원", 
                request.getIrpAccountNumber(), newIrpBalance);

        Map<String, Object> bankResponse = callBankSubscriptionApi(request, customerCi);

        DepositSubscription subscription = saveSubscription(request, customerCi, bankResponse);

        createDepositSubscriptionTransaction(request, irpAccount, subscription);

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

    private Map<String, Object> callBankSubscriptionApi(DepositSubscriptionRequest request, String customerCi) {
        log.info("은행 API 호출: bankCode={}, depositCode={}", request.getBankCode(), request.getDepositCode());

        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("customerCi", customerCi);
            requestBody.put("irpAccountNumber", request.getIrpAccountNumber());
            requestBody.put("linkedAccountNumber", request.getLinkedAccountNumber());
            requestBody.put("depositCode", request.getDepositCode());
            requestBody.put("productType", request.getProductType());
            requestBody.put("contractPeriod", request.getContractPeriod());
            requestBody.put("subscriptionAmount", request.getSubscriptionAmount());

            Map<String, Object> response;
            switch (request.getBankCode().toUpperCase()) {
                case "081":
                case "HANA":
                    response = hanaBankClient.subscribeDeposit(requestBody);
                    break;
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

    private DepositSubscription saveSubscription(DepositSubscriptionRequest request, String customerCi, Map<String, Object> bankResponse) {
        BigDecimal appliedRate = InterestRateCalculator.getBaseRate(
                request.getProductType(), 
                request.getContractPeriod());

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

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getUserSubscriptions(Long userId) {
        log.info("사용자 정기예금 가입 내역 조회: userId={}", userId);

        List<DepositSubscription> subscriptions = depositSubscriptionRepository
                .findByUserIdOrderBySubscriptionDateDesc(userId);

        return subscriptions.stream()
                .map(this::convertToMap)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getSubscriptionByAccountNumber(String accountNumber) {
        log.info("계좌번호로 가입 내역 조회: accountNumber={}", accountNumber);

        DepositSubscription subscription = depositSubscriptionRepository
                .findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("가입 내역을 찾을 수 없습니다: " + accountNumber));

        return convertToMap(subscription);
    }

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
            Map<String, Object> bankResponse;
            switch (subscription.getBankCode()) {
                case "081":
                    bankResponse = hanaBankClient.terminateDeposit(accountNumber);
                    break;
                default:
                    throw new RuntimeException("지원하지 않는 은행입니다: " + subscription.getBankCode());
            }

            if (bankResponse != null) {

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

    private String getProductTypeName(Integer productType) {
        if (productType == null) return "알 수 없음";
        switch (productType) {
            case 0: return "일반";
            case 1: return "디폴트옵션";
            case 2: return "일단위";
            default: return "알 수 없음";
        }
    }

    private void createDepositSubscriptionTransaction(DepositSubscriptionRequest request, 
                                                      IrpAccount irpAccount, 
                                                      DepositSubscription subscription) {
        try {
            BankingAccount irpBankingAccount = accountRepository.findByAccountNumber(request.getIrpAccountNumber())
                    .orElseThrow(() -> new RuntimeException("IRP 계좌를 찾을 수 없습니다: " + request.getIrpAccountNumber()));

            String transactionNumber = Transaction.generateTransactionNumber();

            Transaction transaction = Transaction.builder()
                    .transactionNumber(transactionNumber)
                    .fromAccountId(irpBankingAccount.getAccountId())
                    .toAccountId(null)
                    .transactionType(Transaction.TransactionType.TRANSFER)
                    .transactionCategory(Transaction.TransactionCategory.INVESTMENT)
                    .amount(request.getSubscriptionAmount())
                    .balanceAfter(irpAccount.getCurrentBalance())
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
        }
    }
}