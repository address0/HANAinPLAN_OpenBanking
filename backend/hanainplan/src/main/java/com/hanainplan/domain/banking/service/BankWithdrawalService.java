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
public class BankWithdrawalService {

    private final RestTemplate restTemplate;

    @Value("${external.api.hana-bank.base-url:http://localhost:8081}")
    private String hanaBankBaseUrl;

    @Value("${external.api.kookmin-bank.base-url:http://localhost:8083}")
    private String kookminBankBaseUrl;

    @Value("${external.api.shinhan-bank.base-url:http://localhost:8082}")
    private String shinhanBankBaseUrl;

    public BankWithdrawalResult processWithdrawal(String accountNumber, BigDecimal amount, String description) {
        String bankCode = extractBankCodeFromAccountNumber(accountNumber);

        log.info("은행 출금 요청 - 계좌번호: {}, 은행코드: {}, 금액: {}원", accountNumber, bankCode, amount);

        try {
            switch (bankCode) {
                case "081":
                    return processHanaBankWithdrawal(accountNumber, amount, description);
                case "004":
                    return processKookminBankWithdrawal(accountNumber, amount, description);
                case "088":
                    return processShinhanBankWithdrawal(accountNumber, amount, description);
                default:
                    log.warn("지원하지 않는 은행 코드: {}", bankCode);
                    return BankWithdrawalResult.failure("지원하지 않는 은행입니다: " + bankCode);
            }
        } catch (Exception e) {
            log.error("은행 출금 요청 실패 - 계좌번호: {}, 오류: {}", accountNumber, e.getMessage());
            return BankWithdrawalResult.failure("은행 출금 요청 실패: " + e.getMessage());
        }
    }

    private String extractBankCodeFromAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 3) {
            return "UNKNOWN";
        }

        String cleanAccountNumber = accountNumber.replace("-", "");
        String prefix = cleanAccountNumber.substring(0, 3);

        if (prefix.equals("081") || (prefix.compareTo("110") >= 0 && prefix.compareTo("119") <= 0)) {
            return "081";
        } else if (prefix.equals("004") || (prefix.compareTo("123") >= 0 && prefix.compareTo("129") <= 0)) {
            return "004";
        } else if (prefix.equals("088") || (prefix.compareTo("456") >= 0 && prefix.compareTo("459") <= 0)) {
            return "088";
        }

        return prefix;
    }

    private BankWithdrawalResult processHanaBankWithdrawal(String accountNumber, BigDecimal amount, String description) {
        try {
            String url = hanaBankBaseUrl + "/api/hana/accounts/withdrawal";

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
                    log.info("하나은행 출금 성공 - 계좌번호: {}, 금액: {}원", accountNumber, amount);
                    return BankWithdrawalResult.success(message, (String) responseBody.get("transactionId"));
                } else {
                    log.error("하나은행 출금 실패 - 계좌번호: {}, 메시지: {}", accountNumber, message);
                    return BankWithdrawalResult.failure(message != null ? message : "하나은행 출금 실패");
                }
            } else {
                return BankWithdrawalResult.failure("하나은행 서버 응답 오류");
            }

        } catch (Exception e) {
            log.error("하나은행 출금 요청 실패 - 계좌번호: {}, 오류: {}", accountNumber, e.getMessage());
            return BankWithdrawalResult.failure("하나은행 연동 실패: " + e.getMessage());
        }
    }

    private BankWithdrawalResult processKookminBankWithdrawal(String accountNumber, BigDecimal amount, String description) {
        try {
            String url = kookminBankBaseUrl + "/api/kookmin/accounts/withdrawal";

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
                    log.info("국민은행 출금 성공 - 계좌번호: {}, 금액: {}원", accountNumber, amount);
                    return BankWithdrawalResult.success(message, (String) responseBody.get("transactionId"));
                } else {
                    log.error("국민은행 출금 실패 - 계좌번호: {}, 메시지: {}", accountNumber, message);
                    return BankWithdrawalResult.failure(message != null ? message : "국민은행 출금 실패");
                }
            } else {
                return BankWithdrawalResult.failure("국민은행 서버 응답 오류");
            }

        } catch (Exception e) {
            log.error("국민은행 출금 요청 실패 - 계좌번호: {}, 오류: {}", accountNumber, e.getMessage());
            return BankWithdrawalResult.failure("국민은행 연동 실패: " + e.getMessage());
        }
    }

    private BankWithdrawalResult processShinhanBankWithdrawal(String accountNumber, BigDecimal amount, String description) {
        try {
            String url = shinhanBankBaseUrl + "/api/shinhan/accounts/withdrawal";

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
                    log.info("신한은행 출금 성공 - 계좌번호: {}, 금액: {}원", accountNumber, amount);
                    return BankWithdrawalResult.success(message, (String) responseBody.get("transactionId"));
                } else {
                    log.error("신한은행 출금 실패 - 계좌번호: {}, 메시지: {}", accountNumber, message);
                    return BankWithdrawalResult.failure(message != null ? message : "신한은행 출금 실패");
                }
            } else {
                return BankWithdrawalResult.failure("신한은행 서버 응답 오류");
            }

        } catch (Exception e) {
            log.error("신한은행 출금 요청 실패 - 계좌번호: {}, 오류: {}", accountNumber, e.getMessage());
            return BankWithdrawalResult.failure("신한은행 연동 실패: " + e.getMessage());
        }
    }

    public static class BankWithdrawalResult {
        private final boolean success;
        private final String message;
        private final String transactionId;

        private BankWithdrawalResult(boolean success, String message, String transactionId) {
            this.success = success;
            this.message = message;
            this.transactionId = transactionId;
        }

        public static BankWithdrawalResult success(String message, String transactionId) {
            return new BankWithdrawalResult(true, message, transactionId);
        }

        public static BankWithdrawalResult failure(String message) {
            return new BankWithdrawalResult(false, message, null);
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