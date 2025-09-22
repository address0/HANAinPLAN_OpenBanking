package com.hanainplan.shinhan.product.repository;

import com.hanainplan.shinhan.product.entity.InterestRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InterestRateRepository extends JpaRepository<InterestRate, Long> {

    /**
     * 상품코드로 금리 목록 조회
     */
    List<InterestRate> findByProductCode(String productCode);

    /**
     * 상품코드와 금리종류로 금리 조회
     */
    List<InterestRate> findByProductCodeAndInterestType(String productCode, InterestRate.InterestType interestType);

    /**
     * 상품코드, 금리종류, 만기기간으로 금리 조회
     */
    Optional<InterestRate> findByProductCodeAndInterestTypeAndMaturityPeriod(
            String productCode, 
            InterestRate.InterestType interestType, 
            String maturityPeriod
    );

    /**
     * 상품코드, 금리종류, 만기기간, 적용일자로 금리 조회
     */
    Optional<InterestRate> findByProductCodeAndInterestTypeAndMaturityPeriodAndEffectiveDate(
            String productCode, 
            InterestRate.InterestType interestType, 
            String maturityPeriod,
            LocalDate effectiveDate
    );

    /**
     * 상품코드와 만기기간으로 기본금리 조회 (가장 최근 적용일자 기준)
     */
    @Query("SELECT ir FROM InterestRate ir WHERE ir.productCode = :productCode " +
           "AND ir.interestType = 'BASIC' AND ir.maturityPeriod = :maturityPeriod " +
           "ORDER BY ir.effectiveDate DESC")
    Optional<InterestRate> findLatestBasicRateByProductCodeAndMaturityPeriod(
            @Param("productCode") String productCode, 
            @Param("maturityPeriod") String maturityPeriod
    );

    /**
     * 상품코드와 만기기간으로 우대금리 조회 (가장 최근 적용일자 기준)
     */
    @Query("SELECT ir FROM InterestRate ir WHERE ir.productCode = :productCode " +
           "AND ir.interestType = 'PREFERENTIAL' AND ir.maturityPeriod = :maturityPeriod " +
           "ORDER BY ir.effectiveDate DESC")
    Optional<InterestRate> findLatestPreferentialRateByProductCodeAndMaturityPeriod(
            @Param("productCode") String productCode, 
            @Param("maturityPeriod") String maturityPeriod
    );

    /**
     * 상품코드로 모든 금리 조회 (적용일자 내림차순)
     */
    @Query("SELECT ir FROM InterestRate ir WHERE ir.productCode = :productCode " +
           "ORDER BY ir.effectiveDate DESC, ir.interestType")
    List<InterestRate> findAllByProductCodeOrderByEffectiveDateDesc(@Param("productCode") String productCode);

    /**
     * IRP 상품 여부로 금리 조회
     */
    List<InterestRate> findByIsIrp(Boolean isIrp);

    /**
     * 상품코드와 IRP 여부로 금리 조회
     */
    List<InterestRate> findByProductCodeAndIsIrp(String productCode, Boolean isIrp);
}
