package com.hanainplan.hana.fund.repository;

import com.hanainplan.hana.fund.entity.FundProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FundProductRepository extends JpaRepository<FundProduct, String> {

    // 활성 펀드 상품 목록 조회
    List<FundProduct> findByIsActiveTrueOrderByFundNameAsc();

    // 펀드 유형별 조회
    List<FundProduct> findByFundTypeAndIsActiveTrueOrderByFundNameAsc(String fundType);

    // 위험등급별 조회
    List<FundProduct> findByRiskLevelAndIsActiveTrueOrderByFundNameAsc(String riskLevel);

    // 투자 지역별 조회
    List<FundProduct> findByInvestmentRegionAndIsActiveTrueOrderByFundNameAsc(String investmentRegion);

    // IRP 편입 가능 펀드 조회
    List<FundProduct> findByIsIrpEligibleTrueAndIsActiveTrueOrderByFundNameAsc();

    // 펀드명으로 검색
    List<FundProduct> findByFundNameContainingAndIsActiveTrueOrderByFundNameAsc(String fundName);

    // 수익률 상위 펀드 조회 (1년 기준)
    @Query("SELECT f FROM FundProduct f WHERE f.isActive = true AND f.return1year IS NOT NULL ORDER BY f.return1year DESC")
    List<FundProduct> findTopPerformingFunds();

    // 위험등급 범위로 조회
    @Query("SELECT f FROM FundProduct f WHERE f.isActive = true AND f.riskLevel IN :riskLevels ORDER BY f.fundName ASC")
    List<FundProduct> findByRiskLevels(@Param("riskLevels") List<String> riskLevels);

    // 펀드 코드 존재 여부
    boolean existsByFundCode(String fundCode);
}

