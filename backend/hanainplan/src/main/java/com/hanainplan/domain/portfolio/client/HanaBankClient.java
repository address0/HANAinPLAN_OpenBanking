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
    
    @Value("${hana.bank.url:http://localhost:8081}")
    private String hanaBankUrl;

    /**
     * 펀드 매수 API 호출
     */
    public FundPurchaseResponse purchaseFund(String customerCi, String childFundCd, BigDecimal purchaseAmount) {
        try {
            String url = hanaBankUrl + "/api/hana/fund-subscription/purchase";
            
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
            String url = hanaBankUrl + "/api/hana/fund-subscription/redeem";
            
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
}
