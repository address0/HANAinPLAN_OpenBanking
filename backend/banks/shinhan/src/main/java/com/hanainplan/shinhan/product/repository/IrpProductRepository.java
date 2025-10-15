package com.hanainplan.shinhan.product.repository;

import com.hanainplan.shinhan.product.entity.IrpProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface IrpProductRepository extends JpaRepository<IrpProduct, Long> {

    Optional<IrpProduct> findByProductCode(String productCode);

    List<IrpProduct> findByProductType(String productType);

    List<IrpProduct> findByIsActiveTrue();

    @Query("SELECT p FROM IrpProduct p WHERE p.productType = :productType AND p.isActive = true")
    List<IrpProduct> findByProductTypeAndActive(@Param("productType") String productType);

    @Query("SELECT p FROM IrpProduct p WHERE p.startDate <= :date AND p.endDate >= :date AND p.isActive = true")
    List<IrpProduct> findActiveProductsByDate(@Param("date") LocalDate date);

    @Query("SELECT p FROM IrpProduct p WHERE p.managementFeeRate <= :maxFeeRate AND p.isActive = true")
    List<IrpProduct> findByMaxManagementFeeRate(@Param("maxFeeRate") BigDecimal maxFeeRate);

    @Query("SELECT p FROM IrpProduct p WHERE p.totalFeeRate <= :maxTotalFeeRate AND p.isActive = true")
    List<IrpProduct> findByMaxTotalFeeRate(@Param("maxTotalFeeRate") BigDecimal maxTotalFeeRate);

    @Query("SELECT p FROM IrpProduct p WHERE p.expectedReturnRate >= :minReturnRate AND p.isActive = true")
    List<IrpProduct> findByMinExpectedReturnRate(@Param("minReturnRate") BigDecimal minReturnRate);

    List<IrpProduct> findByRiskLevel(String riskLevel);

    @Query("SELECT p FROM IrpProduct p WHERE p.riskLevel = :riskLevel AND p.isActive = true")
    List<IrpProduct> findByRiskLevelAndActive(@Param("riskLevel") String riskLevel);

    List<IrpProduct> findByManagementCompany(String managementCompany);

    List<IrpProduct> findByTrustCompany(String trustCompany);

    @Query("SELECT p FROM IrpProduct p WHERE p.minimumContribution <= :maxAmount AND p.isActive = true")
    List<IrpProduct> findByMaxMinimumContribution(@Param("maxAmount") BigDecimal maxAmount);

    @Query("SELECT p FROM IrpProduct p WHERE p.annualContributionLimit >= :minLimit AND p.isActive = true")
    List<IrpProduct> findByMinAnnualContributionLimit(@Param("minLimit") BigDecimal minLimit);

    @Query("SELECT p FROM IrpProduct p WHERE p.productName LIKE %:keyword% AND p.isActive = true")
    List<IrpProduct> findByProductNameContaining(@Param("keyword") String keyword);

    @Query("SELECT p FROM IrpProduct p WHERE p.guaranteeType = :guaranteeType AND p.isActive = true")
    List<IrpProduct> findByGuaranteeTypeAndActive(@Param("guaranteeType") String guaranteeType);

    @Query("SELECT p FROM IrpProduct p WHERE p.autoRebalancing = :autoRebalancing AND p.isActive = true")
    List<IrpProduct> findByAutoRebalancingAndActive(@Param("autoRebalancing") String autoRebalancing);

    @Query("SELECT p FROM IrpProduct p WHERE p.contributionFrequency = :frequency AND p.isActive = true")
    List<IrpProduct> findByContributionFrequencyAndActive(@Param("frequency") String frequency);
}