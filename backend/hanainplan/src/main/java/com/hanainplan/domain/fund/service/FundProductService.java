package com.hanainplan.domain.fund.service;

import com.hanainplan.domain.banking.client.HanaBankClient;
import com.hanainplan.domain.fund.dto.FundProductDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 하나인플랜 펀드 상품 서비스
 * - 하나은행 API를 통해 펀드 상품 정보 조회
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FundProductService {

    private final HanaBankClient hanaBankClient;

    /**
     * 모든 활성 펀드 상품 조회
     */
    public List<FundProductDto> getAllFundProducts() {
        log.info("펀드 상품 목록 조회 요청");
        
        try {
            List<Map<String, Object>> response = hanaBankClient.getAllFundProducts();
            List<FundProductDto> products = response.stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList());
            
            log.info("펀드 상품 목록 조회 완료 - {}건", products.size());
            return products;
        } catch (Exception e) {
            log.error("펀드 상품 목록 조회 실패", e);
            throw new RuntimeException("펀드 상품 목록 조회에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 펀드 코드로 상품 상세 조회
     */
    public FundProductDto getFundProductByCode(String fundCode) {
        log.info("펀드 상품 상세 조회 - fundCode: {}", fundCode);
        
        try {
            Map<String, Object> response = hanaBankClient.getFundProduct(fundCode);
            FundProductDto product = mapToDto(response);
            
            log.info("펀드 상품 상세 조회 완료 - fundCode: {}, fundName: {}", 
                    product.getFundCode(), product.getFundName());
            return product;
        } catch (Exception e) {
            log.error("펀드 상품 상세 조회 실패 - fundCode: {}", fundCode, e);
            throw new RuntimeException("펀드 상품 조회에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 펀드 유형별 조회
     */
    public List<FundProductDto> getFundProductsByType(String fundType) {
        log.info("펀드 유형별 조회 - fundType: {}", fundType);
        
        try {
            List<Map<String, Object>> response = hanaBankClient.getFundProductsByType(fundType);
            List<FundProductDto> products = response.stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList());
            
            log.info("펀드 유형별 조회 완료 - fundType: {}, 결과: {}건", fundType, products.size());
            return products;
        } catch (Exception e) {
            log.error("펀드 유형별 조회 실패 - fundType: {}", fundType, e);
            throw new RuntimeException("펀드 조회에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 위험등급별 조회
     */
    public List<FundProductDto> getFundProductsByRiskLevel(String riskLevel) {
        log.info("위험등급별 펀드 조회 - riskLevel: {}", riskLevel);
        
        try {
            List<Map<String, Object>> response = hanaBankClient.getFundProductsByRiskLevel(riskLevel);
            List<FundProductDto> products = response.stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList());
            
            log.info("위험등급별 펀드 조회 완료 - riskLevel: {}, 결과: {}건", riskLevel, products.size());
            return products;
        } catch (Exception e) {
            log.error("위험등급별 펀드 조회 실패 - riskLevel: {}", riskLevel, e);
            throw new RuntimeException("펀드 조회에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * IRP 편입 가능 펀드 조회
     */
    public List<FundProductDto> getIrpEligibleFunds() {
        log.info("IRP 편입 가능 펀드 조회");
        
        try {
            List<Map<String, Object>> response = hanaBankClient.getIrpEligibleFunds();
            List<FundProductDto> products = response.stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList());
            
            log.info("IRP 편입 가능 펀드 조회 완료 - 결과: {}건", products.size());
            return products;
        } catch (Exception e) {
            log.error("IRP 편입 가능 펀드 조회 실패", e);
            throw new RuntimeException("펀드 조회에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 수익률 상위 펀드 조회
     */
    public List<FundProductDto> getTopPerformingFunds() {
        log.info("수익률 상위 펀드 조회");
        
        try {
            List<Map<String, Object>> response = hanaBankClient.getTopPerformingFunds();
            List<FundProductDto> products = response.stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList());
            
            log.info("수익률 상위 펀드 조회 완료 - 결과: {}건", products.size());
            return products;
        } catch (Exception e) {
            log.error("수익률 상위 펀드 조회 실패", e);
            throw new RuntimeException("펀드 조회에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 복합 필터링 조회
     */
    public List<FundProductDto> filterFunds(
            String fundType, 
            String riskLevel, 
            String investmentRegion, 
            Boolean isIrpEligible
    ) {
        log.info("복합 필터링 조회 - fundType: {}, riskLevel: {}, region: {}, irpEligible: {}", 
                fundType, riskLevel, investmentRegion, isIrpEligible);
        
        try {
            List<Map<String, Object>> response = hanaBankClient.filterFunds(
                    fundType, riskLevel, investmentRegion, isIrpEligible);
            
            List<FundProductDto> products = response.stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList());
            
            log.info("복합 필터링 조회 완료 - 결과: {}건", products.size());
            return products;
        } catch (Exception e) {
            log.error("복합 필터링 조회 실패", e);
            throw new RuntimeException("펀드 조회에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * Map -> FundProductDto 변환
     */
    private FundProductDto mapToDto(Map<String, Object> map) {
        return FundProductDto.builder()
                .fundCode((String) map.get("fundCode"))
                .fundName((String) map.get("fundName"))
                .fundType((String) map.get("fundType"))
                .investmentRegion((String) map.get("investmentRegion"))
                .riskLevel((String) map.get("riskLevel"))
                .salesFeeRate(toBigDecimal(map.get("salesFeeRate")))
                .managementFeeRate(toBigDecimal(map.get("managementFeeRate")))
                .trustFeeRate(toBigDecimal(map.get("trustFeeRate")))
                .totalExpenseRatio(toBigDecimal(map.get("totalExpenseRatio")))
                .redemptionFeeRate(toBigDecimal(map.get("redemptionFeeRate")))
                .return1month(toBigDecimal(map.get("return1month")))
                .return3month(toBigDecimal(map.get("return3month")))
                .return6month(toBigDecimal(map.get("return6month")))
                .return1year(toBigDecimal(map.get("return1year")))
                .return3year(toBigDecimal(map.get("return3year")))
                .managementCompany((String) map.get("managementCompany"))
                .trustCompany((String) map.get("trustCompany"))
                .minInvestmentAmount(toBigDecimal(map.get("minInvestmentAmount")))
                .isIrpEligible((Boolean) map.get("isIrpEligible"))
                .description((String) map.get("description"))
                .isActive((Boolean) map.get("isActive"))
                .build();
    }

    /**
     * Object -> BigDecimal 변환
     */
    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        if (value instanceof String) {
            return new BigDecimal((String) value);
        }
        return null;
    }
}

