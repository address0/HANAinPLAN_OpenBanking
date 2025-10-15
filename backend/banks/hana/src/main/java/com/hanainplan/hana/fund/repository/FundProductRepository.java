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

    List<FundProduct> findByIsActiveTrueOrderByFundNameAsc();

    List<FundProduct> findByFundTypeAndIsActiveTrueOrderByFundNameAsc(String fundType);

    List<FundProduct> findByRiskLevelAndIsActiveTrueOrderByFundNameAsc(String riskLevel);

    List<FundProduct> findByInvestmentRegionAndIsActiveTrueOrderByFundNameAsc(String investmentRegion);

    List<FundProduct> findByIsIrpEligibleTrueAndIsActiveTrueOrderByFundNameAsc();

    List<FundProduct> findByFundNameContainingAndIsActiveTrueOrderByFundNameAsc(String fundName);

    @Query("SELECT f FROM FundProduct f WHERE f.isActive = true AND f.return1year IS NOT NULL ORDER BY f.return1year DESC")
    List<FundProduct> findTopPerformingFunds();

    @Query("SELECT f FROM FundProduct f WHERE f.isActive = true AND f.riskLevel IN :riskLevels ORDER BY f.fundName ASC")
    List<FundProduct> findByRiskLevels(@Param("riskLevels") List<String> riskLevels);

    boolean existsByFundCode(String fundCode);
}