package com.hanainplan.hana.fund.service;

import com.hanainplan.hana.fund.entity.FundProduct;
import com.hanainplan.hana.fund.repository.FundProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 하나은행 펀드 상품 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FundProductService {

    private final FundProductRepository fundProductRepository;

    /**
     * 모든 활성 펀드 상품 조회
     */
    public List<FundProduct> getAllActiveFundProducts() {
        log.info("활성 펀드 상품 목록 조회");
        return fundProductRepository.findByIsActiveTrueOrderByFundNameAsc();
    }

    /**
     * 펀드 코드로 상품 조회
     */
    public Optional<FundProduct> getFundProductByCode(String fundCode) {
        log.info("펀드 상품 조회 - fundCode: {}", fundCode);
        return fundProductRepository.findById(fundCode);
    }

    /**
     * 펀드 유형별 조회
     */
    public List<FundProduct> getFundProductsByType(String fundType) {
        log.info("펀드 유형별 조회 - fundType: {}", fundType);
        return fundProductRepository.findByFundTypeAndIsActiveTrueOrderByFundNameAsc(fundType);
    }

    /**
     * 위험등급별 조회
     */
    public List<FundProduct> getFundProductsByRiskLevel(String riskLevel) {
        log.info("위험등급별 펀드 조회 - riskLevel: {}", riskLevel);
        return fundProductRepository.findByRiskLevelAndIsActiveTrueOrderByFundNameAsc(riskLevel);
    }

    /**
     * 투자 지역별 조회
     */
    public List<FundProduct> getFundProductsByInvestmentRegion(String investmentRegion) {
        log.info("투자 지역별 펀드 조회 - investmentRegion: {}", investmentRegion);
        return fundProductRepository.findByInvestmentRegionAndIsActiveTrueOrderByFundNameAsc(investmentRegion);
    }

    /**
     * IRP 편입 가능 펀드 조회
     */
    public List<FundProduct> getIrpEligibleFunds() {
        log.info("IRP 편입 가능 펀드 조회");
        return fundProductRepository.findByIsIrpEligibleTrueAndIsActiveTrueOrderByFundNameAsc();
    }

    /**
     * 수익률 상위 펀드 조회
     */
    public List<FundProduct> getTopPerformingFunds() {
        log.info("수익률 상위 펀드 조회");
        return fundProductRepository.findTopPerformingFunds();
    }

    /**
     * 펀드명으로 검색
     */
    public List<FundProduct> searchFundsByName(String fundName) {
        log.info("펀드명 검색 - keyword: {}", fundName);
        return fundProductRepository.findByFundNameContainingAndIsActiveTrueOrderByFundNameAsc(fundName);
    }

    /**
     * 복합 필터링 조회
     */
    public List<FundProduct> searchFunds(String fundType, String riskLevel, String investmentRegion, Boolean isIrpEligible) {
        log.info("복합 필터링 조회 - fundType: {}, riskLevel: {}, region: {}, irpEligible: {}", 
                fundType, riskLevel, investmentRegion, isIrpEligible);
        
        List<FundProduct> results = fundProductRepository.findByIsActiveTrueOrderByFundNameAsc();
        
        // 펀드 유형 필터
        if (fundType != null && !fundType.isBlank()) {
            results = results.stream()
                    .filter(f -> fundType.equals(f.getFundType()))
                    .toList();
        }
        
        // 위험등급 필터
        if (riskLevel != null && !riskLevel.isBlank()) {
            results = results.stream()
                    .filter(f -> riskLevel.equals(f.getRiskLevel()))
                    .toList();
        }
        
        // 투자 지역 필터
        if (investmentRegion != null && !investmentRegion.isBlank()) {
            results = results.stream()
                    .filter(f -> investmentRegion.equals(f.getInvestmentRegion()))
                    .toList();
        }
        
        // IRP 편입 가능 여부 필터
        if (isIrpEligible != null && isIrpEligible) {
            results = results.stream()
                    .filter(f -> Boolean.TRUE.equals(f.getIsIrpEligible()))
                    .toList();
        }
        
        log.info("필터링 결과: {}건", results.size());
        return results;
    }

    /**
     * 펀드 상품 등록 (관리자용)
     */
    @Transactional
    public FundProduct createFundProduct(FundProduct fundProduct) {
        log.info("펀드 상품 등록 - fundCode: {}, fundName: {}", 
                fundProduct.getFundCode(), fundProduct.getFundName());
        
        if (fundProductRepository.existsByFundCode(fundProduct.getFundCode())) {
            throw new IllegalArgumentException("이미 존재하는 펀드 코드입니다: " + fundProduct.getFundCode());
        }
        
        // 총 보수율 계산
        fundProduct.setTotalExpenseRatio(fundProduct.calculateTotalExpenseRatio());
        
        FundProduct saved = fundProductRepository.save(fundProduct);
        log.info("펀드 상품 등록 완료 - fundCode: {}", saved.getFundCode());
        
        return saved;
    }

    /**
     * 펀드 상품 수정 (관리자용)
     */
    @Transactional
    public FundProduct updateFundProduct(String fundCode, FundProduct updatedProduct) {
        log.info("펀드 상품 수정 - fundCode: {}", fundCode);
        
        FundProduct existing = fundProductRepository.findById(fundCode)
                .orElseThrow(() -> new IllegalArgumentException("펀드 상품을 찾을 수 없습니다: " + fundCode));
        
        // 수정 가능한 필드 업데이트
        if (updatedProduct.getFundName() != null) {
            existing.setFundName(updatedProduct.getFundName());
        }
        if (updatedProduct.getDescription() != null) {
            existing.setDescription(updatedProduct.getDescription());
        }
        if (updatedProduct.getSalesFeeRate() != null) {
            existing.setSalesFeeRate(updatedProduct.getSalesFeeRate());
        }
        if (updatedProduct.getManagementFeeRate() != null) {
            existing.setManagementFeeRate(updatedProduct.getManagementFeeRate());
        }
        if (updatedProduct.getMinInvestmentAmount() != null) {
            existing.setMinInvestmentAmount(updatedProduct.getMinInvestmentAmount());
        }
        if (updatedProduct.getIsActive() != null) {
            existing.setIsActive(updatedProduct.getIsActive());
        }
        
        // 총 보수율 재계산
        existing.setTotalExpenseRatio(existing.calculateTotalExpenseRatio());
        
        FundProduct saved = fundProductRepository.save(existing);
        log.info("펀드 상품 수정 완료 - fundCode: {}", fundCode);
        
        return saved;
    }

    /**
     * 펀드 상품 비활성화 (관리자용)
     */
    @Transactional
    public void deactivateFundProduct(String fundCode) {
        log.info("펀드 상품 비활성화 - fundCode: {}", fundCode);
        
        FundProduct fundProduct = fundProductRepository.findById(fundCode)
                .orElseThrow(() -> new IllegalArgumentException("펀드 상품을 찾을 수 없습니다: " + fundCode));
        
        fundProduct.setIsActive(false);
        fundProductRepository.save(fundProduct);
        
        log.info("펀드 상품 비활성화 완료 - fundCode: {}", fundCode);
    }
}

