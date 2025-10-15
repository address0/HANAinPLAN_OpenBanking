package com.hanainplan.domain.banking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class BankDepositService {

    private final RestTemplate restTemplate;

    @Value("${external.api.hana-bank.base-url:http://localhost:8081}")
    private String hanaBankBaseUrl;

    @Value("${external.api.kookmin-bank.base-url:http://localhost:8082}")
    private String kookminBankBaseUrl;

    @Value("${external.api.shinhan-bank.base-url:http://localhost:8083}")
    private String shinhanBankBaseUrl;

    public BankDepositResult processDeposit(String accountNumber, BigDecimal amount, String description, 
                                            String accountType, String bankCode) {
        log.info("은행 입금 요청 - 계좌번호: {}, 은행코드: {}, 계좌유형: {}, 금액: {}원", 
                accountNumber, bankCode, accountType, amount);

        try {
            if ("IRP".equals(accountType)) {
                return processHanaIrpDeposit(accountNumber, amount, description);
            } else {
                return processGeneralDeposit(accountNumber, amount, description, bankCode);
            }
        } catch (Exception e) {
            log.error("은행 입금 요청 실패 - 계좌번호: {}, 오류: {}", accountNumber, e.getMessage());
            return BankDepositResult.failure("은행 입금 요청 실패: " + e.getMessage());
        }
    }

    private BankDepositResult processHanaIrpDeposit(String accountNumber, BigDecimal amount, String description) {
        try {
            String url = hanaBankBaseUrl + "/api/v1/irp/deposit";

            Map<String, Object> request = new HashMap<>();
            request.put("accountNumber", accountNumber);
            request.put("amount", amount);
            request.put("description", description);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                Boolean success = (Boolean) responseBody.get("success");
                String message = (String) responseBody.get("message");

                if (Boolean.TRUE.equals(success)) {
                    log.info("하나은행 IRP 입금 성공 - 계좌번호: {}, 금액: {}원", accountNumber, amount);
                    return BankDepositResult.success(message, (String) responseBody.get("transactionId"));
                } else {
                    log.error("하나은행 IRP 입금 실패 - 계좌번호: {}, 메시지: {}", accountNumber, message);
                    return BankDepositResult.failure(message != null ? message : "하나은행 IRP 입금 실패");
                }
            } else {
                return BankDepositResult.failure("하나은행 IRP 서버 응답 오류");
            }

        } catch (Exception e) {
            log.error("하나은행 IRP 입금 요청 실패 - 계좌번호: {}, 오류: {}", accountNumber, e.getMessage());
            return BankDepositResult.failure("하나은행 IRP 연동 실패: " + e.getMessage());
        }
    }

    private BankDepositResult processGeneralDeposit(String accountNumber, BigDecimal amount, String description, String bankCode) {
        try {
            String url = getBankDepositApiUrl(bankCode);

            Map<String, Object> request = new HashMap<>();
            request.put("accountNumber", accountNumber);
            request.put("amount", amount);
            request.put("description", description);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                Boolean success = (Boolean) responseBody.get("success");
                String message = (String) responseBody.get("message");

                if (Boolean.TRUE.equals(success)) {
                    String bankName = getBankName(bankCode);
                    log.info("{} 입금 성공 - 계좌번호: {}, 금액: {}원", bankName, accountNumber, amount);
                    return BankDepositResult.success(message, (String) responseBody.get("transactionId"));
                } else {
                    log.error("{} 입금 실패 - 계좌번호: {}, 메시지: {}", getBankName(bankCode), accountNumber, message);
                    return BankDepositResult.failure(message != null ? message : "은행 입금 실패");
                }
            } else {
                return BankDepositResult.failure("은행 서버 응답 오류");
            }

        } catch (Exception e) {
            log.error("{} 입금 요청 실패 - 계좌번호: {}, 오류: {}", getBankName(bankCode), accountNumber, e.getMessage());
            return BankDepositResult.failure("은행 연동 실패: " + e.getMessage());
        }
    }

    private String getBankDepositApiUrl(String bankCode) {
        switch (bankCode) {
            case "081":
                return hanaBankBaseUrl + "/api/hana/accounts/deposit";
            case "004":
                return kookminBankBaseUrl + "/api/kookmin/accounts/deposit";
            case "088":
                return shinhanBankBaseUrl + "/api/shinhan/accounts/deposit";
            default:
                throw new IllegalArgumentException("지원하지 않는 은행 코드: " + bankCode);
        }
    }

    private String getBankName(String bankCode) {
        switch (bankCode) {
            case "081":
                return "하나은행";
            case "004":
                return "국민은행";
            case "088":
                return "신한은행";
            default:
                return "알 수 없는 은행";
        }
    }

    public static class BankDepositResult {
        private final boolean success;
        private final String message;
        private final String transactionId;

        private BankDepositResult(boolean success, String message, String transactionId) {
            this.success = success;
            this.message = message;
            this.transactionId = transactionId;
        }

        public static BankDepositResult success(String message, String transactionId) {
            return new BankDepositResult(true, message, transactionId);
        }

        public static BankDepositResult failure(String message) {
            return new BankDepositResult(false, message, null);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public String getTransactionId() {
            return transactionId;
        }
    }
}