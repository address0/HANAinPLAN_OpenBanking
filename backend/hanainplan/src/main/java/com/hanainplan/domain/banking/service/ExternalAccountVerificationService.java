package com.hanainplan.domain.banking.service;

import com.hanainplan.domain.banking.dto.AccountVerificationResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalAccountVerificationService {

    private final RestTemplate restTemplate;

    @Value("${external.api.hana-bank.base-url:http://localhost:8081}")
    private String hanaBankBaseUrl;

    @Value("${external.api.kookmin-bank.base-url:http://localhost:8082}")
    private String kookminBankBaseUrl;

    @Value("${external.api.shinhan-bank.base-url:http://localhost:8083}")
    private String shinhanBankBaseUrl;

    public AccountVerificationResponseDto verifyExternalAccount(String accountNumber) {
        log.info("외부 계좌 검증 시작 - 계좌번호: {}", accountNumber);

        if (accountNumber == null || accountNumber.length() < 3) {
            return AccountVerificationResponseDto.error("올바르지 않은 계좌번호입니다");
        }

        try {
            String bankCode = extractBankCode(accountNumber);

            if (bankCode == null) {
                log.warn("지원하지 않는 은행 - 계좌번호: {}", accountNumber);
                return AccountVerificationResponseDto.error("지원하지 않는 은행입니다");
            }

            if ("081".equals(bankCode)) {
                AccountVerificationResponseDto irpResult = verifyHanaIrpAccount(accountNumber);
                if (irpResult.isExists()) {
                    log.info("하나은행 IRP 계좌 확인 - 계좌번호: {}", accountNumber);
                    return irpResult;
                }

                log.info("하나은행 일반 계좌 확인 시도 - 계좌번호: {}", accountNumber);
                return verifyHanaGeneralAccount(accountNumber, bankCode);
            }

            return verifyGeneralAccount(accountNumber, bankCode);

        } catch (Exception e) {
            log.error("계좌 검증 실패 - 계좌번호: {}, 오류: {}", accountNumber, e.getMessage());
            return AccountVerificationResponseDto.error("계좌 검증 중 오류가 발생했습니다");
        }
    }

    private String extractBankCode(String accountNumber) {
        String cleanAccountNumber = accountNumber.replace("-", "");
        if (cleanAccountNumber.length() < 3) {
            return null;
        }

        String prefix = cleanAccountNumber.substring(0, 3);

        if (prefix.equals("081") || (prefix.compareTo("110") >= 0 && prefix.compareTo("119") <= 0)) {
            return "081";
        }
        else if (prefix.equals("004") || (prefix.compareTo("123") >= 0 && prefix.compareTo("129") <= 0)) {
            return "004";
        }
        else if (prefix.equals("088") || (prefix.compareTo("456") >= 0 && prefix.compareTo("459") <= 0)) {
            return "088";
        }

        return null;
    }

    private AccountVerificationResponseDto verifyHanaIrpAccount(String accountNumber) {
        try {
            String url = hanaBankBaseUrl + "/api/v1/irp/account/number/" + accountNumber;
            log.info("하나은행 IRP 계좌 조회 요청 - URL: {}", url);

            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                String accountStatus = (String) body.get("accountStatus");

                log.info("하나은행 IRP 계좌 확인됨 - 계좌번호: {}, 상태: {}", accountNumber, accountStatus);

                return AccountVerificationResponseDto.success(
                        "IRP",
                        "081",
                        accountStatus != null ? accountStatus : "ACTIVE",
                        accountNumber
                );
            }

            return AccountVerificationResponseDto.notFound(accountNumber);

        } catch (Exception e) {
            log.debug("하나은행 IRP 계좌 조회 실패 (일반 계좌일 가능성) - 계좌번호: {}", accountNumber);
            return AccountVerificationResponseDto.notFound(accountNumber);
        }
    }

    private AccountVerificationResponseDto verifyHanaGeneralAccount(String accountNumber, String bankCode) {
        try {
            String url = hanaBankBaseUrl + "/api/hana/accounts/" + accountNumber;
            log.info("하나은행 일반 계좌 조회 요청 - URL: {}", url);

            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("하나은행 일반 계좌 확인됨 - 계좌번호: {}", accountNumber);

                return AccountVerificationResponseDto.success(
                        "GENERAL",
                        bankCode,
                        "ACTIVE",
                        accountNumber
                );
            }

            return AccountVerificationResponseDto.notFound(accountNumber);

        } catch (Exception e) {
            log.error("하나은행 일반 계좌 조회 실패 - 계좌번호: {}, 오류: {}", accountNumber, e.getMessage());
            return AccountVerificationResponseDto.notFound(accountNumber);
        }
    }

    private AccountVerificationResponseDto verifyGeneralAccount(String accountNumber, String bankCode) {
        try {
            String url = getBankApiUrl(bankCode) + "/" + accountNumber;
            log.info("일반 계좌 조회 요청 - 은행코드: {}, URL: {}", bankCode, url);

            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("일반 계좌 확인됨 - 은행코드: {}, 계좌번호: {}", bankCode, accountNumber);

                return AccountVerificationResponseDto.success(
                        "GENERAL",
                        bankCode,
                        "ACTIVE",
                        accountNumber
                );
            }

            return AccountVerificationResponseDto.notFound(accountNumber);

        } catch (Exception e) {
            log.error("일반 계좌 조회 실패 - 은행코드: {}, 계좌번호: {}, 오류: {}", bankCode, accountNumber, e.getMessage());
            return AccountVerificationResponseDto.notFound(accountNumber);
        }
    }

    private String getBankApiUrl(String bankCode) {
        switch (bankCode) {
            case "081":
                return hanaBankBaseUrl + "/api/hana/accounts";
            case "004":
                return kookminBankBaseUrl + "/api/kookmin/accounts";
            case "088":
                return shinhanBankBaseUrl + "/api/shinhan/accounts";
            default:
                throw new IllegalArgumentException("지원하지 않는 은행 코드: " + bankCode);
        }
    }
}