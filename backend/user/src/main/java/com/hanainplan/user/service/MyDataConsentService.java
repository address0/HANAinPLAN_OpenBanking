package com.hanainplan.user.service;

import com.hanainplan.user.dto.CustomerAccountInfoDto;
import com.hanainplan.user.dto.MyDataConsentRequestDto;
import com.hanainplan.user.dto.MyDataConsentResponseDto;
import com.hanainplan.user.dto.CiConversionRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MyDataConsentService {

    private final RestTemplate restTemplate;
    private final CiConversionService ciConversionService;

    @Value("${bank.hana.url:http://localhost:8081}")
    private String hanaBankUrl;

    @Value("${bank.shinhan.url:http://localhost:8082}")
    private String shinhanBankUrl;

    @Value("${bank.kookmin.url:http://localhost:8083}")
    private String kookminBankUrl;

    @Value("${hanainplan.url:http://localhost:8080}")
    private String hanainplanUrl;

    private final ExecutorService executorService = Executors.newFixedThreadPool(3);

    /**
     * 마이데이터 수집 동의 처리 및 각 은행사 고객/계좌 정보 조회
     */
    public MyDataConsentResponseDto processMyDataConsent(MyDataConsentRequestDto request) {
        if (!request.isConsentToMyDataCollection()) {
            return new MyDataConsentResponseDto();
        }

        // 기존 CI 변환 API 사용
        String ci = ciConversionService.convertToCi(request.getName(), request.getBirthDate(), request.getGender(), request.getSocialNumber());
        System.out.println("=== CI 변환 결과 ===");
        System.out.println("이름: " + request.getName());
        System.out.println("생년월일: " + request.getBirthDate());
        System.out.println("성별: " + request.getGender());
        System.out.println("주민번호: " + request.getSocialNumber());
        System.out.println("변환된 CI: " + ci);
        System.out.println("==================");
        
        if (ci == null || ci.isEmpty()) {
            MyDataConsentResponseDto errorResponse = new MyDataConsentResponseDto();
            errorResponse.setMessage("CI 변환에 실패했습니다.");
            return errorResponse;
        }

        List<CustomerAccountInfoDto> bankAccountInfo = new ArrayList<>();
        
        // 각 은행사에 동시에 요청
        CompletableFuture<CustomerAccountInfoDto> hanaFuture = CompletableFuture.supplyAsync(() -> 
            getCustomerAccountInfoFromBank(hanaBankUrl, "하나은행", "001", ci), executorService);
        
        CompletableFuture<CustomerAccountInfoDto> shinhanFuture = CompletableFuture.supplyAsync(() -> 
            getCustomerAccountInfoFromBank(shinhanBankUrl, "신한은행", "002", ci), executorService);
        
        CompletableFuture<CustomerAccountInfoDto> kookminFuture = CompletableFuture.supplyAsync(() -> 
            getCustomerAccountInfoFromBank(kookminBankUrl, "국민은행", "003", ci), executorService);

        try {
            // 모든 은행사 응답 대기
            CustomerAccountInfoDto hanaInfo = hanaFuture.get();
            CustomerAccountInfoDto shinhanInfo = shinhanFuture.get();
            CustomerAccountInfoDto kookminInfo = kookminFuture.get();

            System.out.println("=== 은행사별 고객 조회 결과 ===");
            System.out.println("하나은행 고객 여부: " + hanaInfo.isCustomer() + ", 계좌 수: " + (hanaInfo.getAccounts() != null ? hanaInfo.getAccounts().size() : 0));
            System.out.println("신한은행 고객 여부: " + shinhanInfo.isCustomer() + ", 계좌 수: " + (shinhanInfo.getAccounts() != null ? shinhanInfo.getAccounts().size() : 0));
            System.out.println("국민은행 고객 여부: " + kookminInfo.isCustomer() + ", 계좌 수: " + (kookminInfo.getAccounts() != null ? kookminInfo.getAccounts().size() : 0));
            System.out.println("================================");

            // 고객인 은행사만 추가
            if (hanaInfo.isCustomer()) {
                bankAccountInfo.add(hanaInfo);
            }
            if (shinhanInfo.isCustomer()) {
                bankAccountInfo.add(shinhanInfo);
            }
            if (kookminInfo.isCustomer()) {
                bankAccountInfo.add(kookminInfo);
            }

        } catch (Exception e) {
            // 에러 처리
            e.printStackTrace();
        }

        // 응답 생성
        MyDataConsentResponseDto response = new MyDataConsentResponseDto();
        
        int totalAccounts = bankAccountInfo.stream()
            .mapToInt(bank -> bank.getAccounts() != null ? bank.getAccounts().size() : 0)
            .sum();
        
        if (bankAccountInfo.isEmpty()) {
            response.setMessage("등록된 계좌 정보가 없습니다. 해당 CI로 등록된 고객이 없거나 계좌가 없습니다.");
        } else {
            response.setMessage("마이데이터 수집 동의가 완료되었습니다. " + bankAccountInfo.size() + "개 은행에서 " + totalAccounts + "개의 계좌를 발견했습니다.");
        }
        
        response.setBankAccountInfo(bankAccountInfo);
        response.setTotalBanks(bankAccountInfo.size());
        response.setTotalAccounts(totalAccounts);

        return response;
    }

    /**
     * 특정 은행사에서 고객 및 계좌 정보 조회
     */
    private CustomerAccountInfoDto getCustomerAccountInfoFromBank(String bankUrl, String bankName, String bankCode, String ci) {
        try {
            // 은행 코드를 은행명으로 매핑
            String bankPath = getBankPath(bankCode);
            String url = bankUrl + "/api/" + bankPath + "/customer-accounts/ci/" + ci;
            
            System.out.println("=== 은행사 요청 정보 ===");
            System.out.println("은행: " + bankName);
            System.out.println("요청 URL: " + url);
            System.out.println("CI: " + ci);
            System.out.println("======================");
            
            ResponseEntity<Object> response = restTemplate.getForEntity(url, Object.class);
            
            System.out.println("=== 은행사 응답 정보 ===");
            System.out.println("은행: " + bankName);
            System.out.println("응답 상태: " + response.getStatusCode());
            System.out.println("응답 본문: " + response.getBody());
            System.out.println("======================");
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // 응답을 CustomerAccountInfoDto로 변환
                return convertToCustomerAccountInfoDto(response.getBody(), bankName, bankCode);
            }
        } catch (Exception e) {
            System.out.println("=== 은행사 요청 오류 ===");
            System.out.println("은행: " + bankName);
            System.out.println("오류: " + e.getMessage());
            System.out.println("======================");
            e.printStackTrace();
        }
        
        // 에러 시 빈 응답 반환
        CustomerAccountInfoDto emptyInfo = new CustomerAccountInfoDto();
        emptyInfo.setBankName(bankName);
        emptyInfo.setBankCode(bankCode);
        emptyInfo.setCustomerCi(ci);
        emptyInfo.setCustomer(false);
        emptyInfo.setAccounts(new ArrayList<>());
        
        return emptyInfo;
    }

    /**
     * 응답을 CustomerAccountInfoDto로 변환
     */
    private CustomerAccountInfoDto convertToCustomerAccountInfoDto(Object responseBody, String bankName, String bankCode) {
        CustomerAccountInfoDto info = new CustomerAccountInfoDto();
        info.setBankName(bankName);
        info.setBankCode(bankCode);
        
        try {
            // 응답 본문을 Map으로 변환하여 파싱
            if (responseBody instanceof java.util.Map) {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> responseMap = (java.util.Map<String, Object>) responseBody;
                
                // 고객 여부 확인
                Boolean customer = (Boolean) responseMap.get("customer");
                info.setCustomer(customer != null ? customer : false);
                
                // 고객 정보 추출
                String customerName = (String) responseMap.get("customerName");
                String customerCi = (String) responseMap.get("customerCi");
                info.setCustomerName(customerName != null ? customerName : "");
                info.setCustomerCi(customerCi != null ? customerCi : "");
                
                // 계좌 정보 추출
                Object accountsObj = responseMap.get("accounts");
                List<CustomerAccountInfoDto.AccountInfoDto> accounts = new ArrayList<>();
                if (accountsObj instanceof java.util.List) {
                    List<Object> accountsList = (List<Object>) accountsObj;
                    for (Object accountObj : accountsList) {
                        if (accountObj instanceof java.util.Map) {
                            @SuppressWarnings("unchecked")
                            java.util.Map<String, Object> accountMap = (java.util.Map<String, Object>) accountObj;
                            
                            CustomerAccountInfoDto.AccountInfoDto accountDto = new CustomerAccountInfoDto.AccountInfoDto();
                            accountDto.setAccountNumber((String) accountMap.get("accountNumber"));
                            accountDto.setAccountType((Integer) accountMap.get("accountType"));
                            
                            // balance를 안전하게 처리
                            Object balanceObj = accountMap.get("balance");
                            if (balanceObj != null) {
                                accountDto.setBalance(balanceObj);
                            } else {
                                accountDto.setBalance(0);
                            }
                            // 모든 날짜 필드를 안전하게 처리
                            Object openingDateObj = accountMap.get("openingDate");
                            if (openingDateObj instanceof java.time.LocalDate) {
                                accountDto.setOpeningDate(((java.time.LocalDate) openingDateObj).atStartOfDay());
                            } else if (openingDateObj instanceof java.time.LocalDateTime) {
                                accountDto.setOpeningDate((java.time.LocalDateTime) openingDateObj);
                            } else if (openingDateObj instanceof String) {
                                try {
                                    java.time.LocalDate openingDate = java.time.LocalDate.parse((String) openingDateObj);
                                    accountDto.setOpeningDate(openingDate.atStartOfDay());
                                } catch (Exception e) {
                                    accountDto.setOpeningDate(java.time.LocalDateTime.now());
                                }
                            } else {
                                accountDto.setOpeningDate(java.time.LocalDateTime.now());
                            }
                            
                            // createdAt 처리
                            Object createdAtObj = accountMap.get("createdAt");
                            if (createdAtObj instanceof java.time.LocalDateTime) {
                                accountDto.setCreatedAt((java.time.LocalDateTime) createdAtObj);
                            } else if (createdAtObj instanceof String) {
                                try {
                                    accountDto.setCreatedAt(java.time.LocalDateTime.parse((String) createdAtObj));
                                } catch (Exception e) {
                                    accountDto.setCreatedAt(java.time.LocalDateTime.now());
                                }
                            } else {
                                accountDto.setCreatedAt(java.time.LocalDateTime.now());
                            }
                            
                            // updatedAt 처리
                            Object updatedAtObj = accountMap.get("updatedAt");
                            if (updatedAtObj instanceof java.time.LocalDateTime) {
                                accountDto.setUpdatedAt((java.time.LocalDateTime) updatedAtObj);
                            } else if (updatedAtObj instanceof String) {
                                try {
                                    accountDto.setUpdatedAt(java.time.LocalDateTime.parse((String) updatedAtObj));
                                } catch (Exception e) {
                                    accountDto.setUpdatedAt(java.time.LocalDateTime.now());
                                }
                            } else {
                                accountDto.setUpdatedAt(java.time.LocalDateTime.now());
                            }
                            
                            accounts.add(accountDto);
                        }
                    }
                }
                info.setAccounts(accounts);
                
                System.out.println("=== 응답 변환 결과 ===");
                System.out.println("은행: " + bankName);
                System.out.println("고객 여부: " + info.isCustomer());
                System.out.println("고객명: " + info.getCustomerName());
                System.out.println("고객 CI: " + info.getCustomerCi());
                System.out.println("계좌 수: " + accounts.size());
                System.out.println("==================");
                
            } else {
                // 응답 형식이 예상과 다른 경우
                info.setCustomer(false);
                info.setCustomerName("");
                info.setCustomerCi("");
                info.setAccounts(new ArrayList<>());
            }
            
        } catch (Exception e) {
            System.out.println("응답 변환 오류: " + e.getMessage());
            e.printStackTrace();
            // 오류 시 빈 정보 반환
            info.setCustomer(false);
            info.setCustomerName("");
            info.setCustomerCi("");
            info.setAccounts(new ArrayList<>());
        }
        
        return info;
    }

    /**
     * 은행 코드를 은행 경로로 변환
     */
    private String getBankPath(String bankCode) {
        switch (bankCode) {
            case "001": return "hana";
            case "002": return "shinhan";
            case "003": return "kookmin";
            default: return bankCode.toLowerCase();
        }
    }

    /**
     * 계좌 정보를 hanainplan 서버에 저장
     */
    private void saveAccountsToHanainplan(Long userId, String ci, List<CustomerAccountInfoDto> bankAccountInfo) {
        try {
            // 모든 계좌 정보를 하나의 리스트로 변환
            List<MyDataAccountInfo> allAccounts = new ArrayList<>();
            
            for (CustomerAccountInfoDto bankInfo : bankAccountInfo) {
                if (bankInfo.getAccounts() != null) {
                    for (CustomerAccountInfoDto.AccountInfoDto account : bankInfo.getAccounts()) {
                        // balance를 안전하게 BigDecimal로 변환
                        java.math.BigDecimal balance;
                        Object balanceObj = account.getBalance();
                        if (balanceObj == null) {
                            balance = java.math.BigDecimal.ZERO;
                        } else if (balanceObj instanceof java.math.BigDecimal) {
                            balance = (java.math.BigDecimal) balanceObj;
                        } else if (balanceObj instanceof Number) {
                            balance = new java.math.BigDecimal(((Number) balanceObj).toString());
                        } else {
                            balance = new java.math.BigDecimal(balanceObj.toString());
                        }
                        
                        MyDataAccountInfo myDataAccount = new MyDataAccountInfo(
                            account.getAccountNumber(),
                            account.getAccountType(),
                            balance,
                            account.getOpeningDate()
                        );
                        allAccounts.add(myDataAccount);
                    }
                }
            }
            
            if (!allAccounts.isEmpty()) {
                // hanainplan 서버에 계좌 정보 저장 요청
                String url = hanainplanUrl + "/api/banking/mydata/accounts/save";
                
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                
                MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
                params.add("userId", userId);
                params.add("customerCi", ci);
                
                HttpEntity<List<MyDataAccountInfo>> requestEntity = new HttpEntity<>(allAccounts, headers);
                
                System.out.println("=== hanainplan 서버 계좌 저장 요청 ===");
                System.out.println("URL: " + url);
                System.out.println("사용자 ID: " + userId);
                System.out.println("CI: " + ci);
                System.out.println("계좌 수: " + allAccounts.size());
                System.out.println("=====================================");
                
                ResponseEntity<Object> response = restTemplate.postForEntity(url, requestEntity, Object.class, params);
                
                System.out.println("=== hanainplan 서버 응답 ===");
                System.out.println("응답 상태: " + response.getStatusCode());
                System.out.println("응답 본문: " + response.getBody());
                System.out.println("==========================");
                
                if (response.getStatusCode().is2xxSuccessful()) {
                    System.out.println("계좌 정보가 hanainplan 서버에 성공적으로 저장되었습니다.");
                } else {
                    System.out.println("계좌 정보 저장 실패: " + response.getStatusCode());
                }
            }
            
        } catch (Exception e) {
            System.out.println("=== hanainplan 서버 계좌 저장 오류 ===");
            System.out.println("오류: " + e.getMessage());
            System.out.println("=====================================");
            e.printStackTrace();
        }
    }
    
    /**
     * 마이데이터 계좌 정보 DTO
     */
    public static class MyDataAccountInfo {
        private String accountNumber;
        private Integer accountType;
        private java.math.BigDecimal balance;
        private java.time.LocalDateTime openingDate;
        
        public MyDataAccountInfo() {}
        
        public MyDataAccountInfo(String accountNumber, Integer accountType, java.math.BigDecimal balance, java.time.LocalDateTime openingDate) {
            this.accountNumber = accountNumber;
            this.accountType = accountType;
            this.balance = balance;
            this.openingDate = openingDate;
        }
        
        // Getters and Setters
        public String getAccountNumber() { return accountNumber; }
        public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
        
        public Integer getAccountType() { return accountType; }
        public void setAccountType(Integer accountType) { this.accountType = accountType; }
        
        public java.math.BigDecimal getBalance() { return balance; }
        public void setBalance(java.math.BigDecimal balance) { this.balance = balance; }
        
        public java.time.LocalDateTime getOpeningDate() { return openingDate; }
        public void setOpeningDate(java.time.LocalDateTime openingDate) { this.openingDate = openingDate; }
    }
}
