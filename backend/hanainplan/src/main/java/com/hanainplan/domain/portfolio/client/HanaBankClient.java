package com.hanainplan.domain.portfolio.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class HanaBankClient {

    private final RestTemplate restTemplate;
    
    @Value("${hana.bank.api.url:http://localhost:8081}")
    private String hanaBankApiUrl;

    /**
     * 펀드 매수 API 호출
     */
    public FundPurchaseResponse purchaseFund(String customerCi, String childFundCd, BigDecimal purchaseAmount) {
        try {
            String url = hanaBankApiUrl + "/api/hana/fund-subscription/purchase";
            
            FundPurchaseRequest request = FundPurchaseRequest.builder()
                    .customerCi(customerCi)
                    .childFundCd(childFundCd)
                    .purchaseAmount(purchaseAmount)
                    .build();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<FundPurchaseRequest> entity = new HttpEntity<>(request, headers);

            log.info("하나은행 펀드 매수 API 호출 - URL: {}, 요청: {}", url, request);
            
            ResponseEntity<FundPurchaseResponse> response = restTemplate.postForEntity(url, entity, FundPurchaseResponse.class);
            
            log.info("하나은행 펀드 매수 API 응답 - 성공: {}, 구독ID: {}", 
                    response.getBody().isSuccess(), response.getBody().getSubscriptionId());
            
            return response.getBody();
            
        } catch (Exception e) {
            log.error("하나은행 펀드 매수 API 호출 실패", e);
            return FundPurchaseResponse.builder()
                    .success(false)
                    .errorMessage("펀드 매수 API 호출 실패: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 펀드 매도 API 호출
     */
    public FundRedemptionResponse redeemFund(String customerCi, Long subscriptionId, BigDecimal sellUnits, boolean sellAll) {
        try {
            String url = hanaBankApiUrl + "/api/hana/fund-subscription/redeem";
            
            FundRedemptionRequest request = FundRedemptionRequest.builder()
                    .customerCi(customerCi)
                    .subscriptionId(subscriptionId)
                    .sellUnits(sellUnits)
                    .sellAll(sellAll)
                    .build();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<FundRedemptionRequest> entity = new HttpEntity<>(request, headers);

            log.info("하나은행 펀드 매도 API 호출 - URL: {}, 요청: {}", url, request);
            
            ResponseEntity<FundRedemptionResponse> response = restTemplate.postForEntity(url, entity, FundRedemptionResponse.class);
            
            log.info("하나은행 펀드 매도 API 응답 - 성공: {}, 매도좌수: {}, 실수령액: {}", 
                    response.getBody().isSuccess(), response.getBody().getSellUnits(), response.getBody().getNetAmount());
            
            return response.getBody();
            
        } catch (Exception e) {
            log.error("하나은행 펀드 매도 API 호출 실패", e);
            return FundRedemptionResponse.builder()
                    .success(false)
                    .errorMessage("펀드 매도 API 호출 실패: " + e.getMessage())
                    .build();
        }
    }

    /**
     * IRP 계좌 현금 잔액 조회
     */
    public IrpAccountBalanceResponse getIrpAccountBalance(String accountNumber) {
        String url = hanaBankApiUrl + "/api/hana/irp/" + accountNumber + "/balance";
        log.info("하나은행 IRP 계좌 잔액 조회 요청 - URL: {}, accountNumber: {}", url, accountNumber);

        try {
            ResponseEntity<IrpAccountBalanceResponse> responseEntity =
                    restTemplate.getForEntity(url, IrpAccountBalanceResponse.class);
            log.info("하나은행 IRP 계좌 잔액 조회 응답: {}", responseEntity.getBody());
            return responseEntity.getBody();
        } catch (Exception e) {
            log.error("하나은행 IRP 계좌 잔액 조회 실패: {}", e.getMessage(), e);
            return IrpAccountBalanceResponse.builder()
                    .success(false)
                    .errorMessage("IRP 계좌 잔액 조회 실패: " + e.getMessage())
                    .build();
        }
    }

    /**
     * IRP 예금 보유 내역 조회
     */
    public IrpDepositHoldingsResponse getIrpDepositHoldings(String accountNumber) {
        String url = hanaBankApiUrl + "/api/hana/irp/" + accountNumber + "/deposits";
        log.info("하나은행 IRP 예금 보유 조회 요청 - URL: {}, accountNumber: {}", url, accountNumber);

        try {
            ResponseEntity<IrpDepositHoldingsResponse> responseEntity =
                    restTemplate.getForEntity(url, IrpDepositHoldingsResponse.class);
            log.info("하나은행 IRP 예금 보유 조회 응답: {}", responseEntity.getBody());
            return responseEntity.getBody();
        } catch (Exception e) {
            log.error("하나은행 IRP 예금 보유 조회 실패: {}", e.getMessage(), e);
            return IrpDepositHoldingsResponse.builder()
                    .success(false)
                    .errorMessage("IRP 예금 보유 조회 실패: " + e.getMessage())
                    .build();
        }
    }

    /**
     * IRP 펀드 보유 내역 조회
     */
    public IrpFundHoldingsResponse getIrpFundHoldings(String accountNumber) {
        String url = hanaBankApiUrl + "/api/hana/irp/" + accountNumber + "/funds";
        log.info("하나은행 IRP 펀드 보유 조회 요청 - URL: {}, accountNumber: {}", url, accountNumber);

        try {
            ResponseEntity<IrpFundHoldingsResponse> responseEntity =
                    restTemplate.getForEntity(url, IrpFundHoldingsResponse.class);
            log.info("하나은행 IRP 펀드 보유 조회 응답: {}", responseEntity.getBody());
            return responseEntity.getBody();
        } catch (Exception e) {
            log.error("하나은행 IRP 펀드 보유 조회 실패: {}", e.getMessage(), e);
            return IrpFundHoldingsResponse.builder()
                    .success(false)
                    .errorMessage("IRP 펀드 보유 조회 실패: " + e.getMessage())
                    .build();
        }
    }

    /**
     * IRP 포트폴리오 전체 조회
     */
    public IrpPortfolioResponse getIrpPortfolio(String accountNumber) {
        String url = hanaBankApiUrl + "/api/hana/irp/" + accountNumber + "/portfolio";
        log.info("하나은행 IRP 포트폴리오 조회 요청 - URL: {}, accountNumber: {}", url, accountNumber);

        try {
            ResponseEntity<IrpPortfolioResponse> responseEntity =
                    restTemplate.getForEntity(url, IrpPortfolioResponse.class);
            log.info("하나은행 IRP 포트폴리오 조회 응답: {}", responseEntity.getBody());
            return responseEntity.getBody();
        } catch (Exception e) {
            log.error("하나은행 IRP 포트폴리오 조회 실패: {}", e.getMessage(), e);
            return IrpPortfolioResponse.builder()
                    .success(false)
                    .errorMessage("IRP 포트폴리오 조회 실패: " + e.getMessage())
                    .build();
        }
    }

    // DTO 클래스들
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class FundPurchaseRequest {
        private String customerCi;
        private String childFundCd;
        private BigDecimal purchaseAmount;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class FundPurchaseResponse {
        private boolean success;
        private Long subscriptionId;
        private BigDecimal purchaseUnits;
        private BigDecimal purchaseFee;
        private String errorMessage;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class FundRedemptionRequest {
        private String customerCi;
        private Long subscriptionId;
        private BigDecimal sellUnits;
        private boolean sellAll;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class FundRedemptionResponse {
        private boolean success;
        private BigDecimal sellUnits;
        private BigDecimal netAmount;
        private BigDecimal redemptionFee;
        private String errorMessage;
    }

    // IRP 관련 DTO 클래스들
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class IrpAccountBalanceResponse {
        private boolean success;
        private String errorMessage;
        private String accountNumber;
        private BigDecimal cashBalance;
        private java.time.LocalDateTime lastUpdated;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class IrpDepositHoldingsResponse {
        private boolean success;
        private String errorMessage;
        private java.util.List<IrpDepositHolding> holdings;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class IrpDepositHolding {
        private Long subscriptionId;
        private String productCode;
        private String productName;
        private BigDecimal principalAmount;
        private BigDecimal currentValue;
        private BigDecimal interestRate;
        private java.time.LocalDateTime subscriptionDate;
        private java.time.LocalDateTime maturityDate;
        private String status;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class IrpFundHoldingsResponse {
        private boolean success;
        private String errorMessage;
        private java.util.List<IrpFundHolding> holdings;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class IrpFundHolding {
        private Long subscriptionId;
        private String fundCode;
        private String fundName;
        private BigDecimal units;
        private BigDecimal currentNav;
        private BigDecimal purchaseNav;
        private BigDecimal currentValue;
        private BigDecimal purchaseAmount;
        private BigDecimal totalReturn;
        private BigDecimal returnRate;
        private java.time.LocalDateTime subscriptionDate;
        private String status;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class IrpPortfolioResponse {
        private boolean success;
        private String errorMessage;
        private String accountNumber;
        private BigDecimal totalValue;
        private BigDecimal cashBalance;
        private java.util.List<IrpDepositHolding> depositHoldings;
        private java.util.List<IrpFundHolding> fundHoldings;
        private java.time.LocalDateTime lastUpdated;
    }
}
