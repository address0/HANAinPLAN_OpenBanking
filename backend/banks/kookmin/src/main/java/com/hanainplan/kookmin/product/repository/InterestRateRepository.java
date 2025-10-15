package com.hanainplan.kookmin.product.repository;

import com.hanainplan.kookmin.product.entity.InterestRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InterestRateRepository extends JpaRepository<InterestRate, Long> {

    List<InterestRate> findByProductCode(String productCode);

    List<InterestRate> findByProductCodeAndInterestType(String productCode, InterestRate.InterestType interestType);

    Optional<InterestRate> findByProductCodeAndInterestTypeAndMaturityPeriod(
            String productCode, 
            InterestRate.InterestType interestType, 
            String maturityPeriod
    );

    Optional<InterestRate> findByProductCodeAndInterestTypeAndMaturityPeriodAndEffectiveDate(
            String productCode, 
            InterestRate.InterestType interestType, 
            String maturityPeriod,
            LocalDate effectiveDate
    );

    @Query("SELECT ir FROM InterestRate ir WHERE ir.productCode = :productCode " +
           "AND ir.interestType = 'BASIC' AND ir.maturityPeriod = :maturityPeriod " +
           "ORDER BY ir.effectiveDate DESC")
    Optional<InterestRate> findLatestBasicRateByProductCodeAndMaturityPeriod(
            @Param("productCode") String productCode, 
            @Param("maturityPeriod") String maturityPeriod
    );

    @Query("SELECT ir FROM InterestRate ir WHERE ir.productCode = :productCode " +
           "AND ir.interestType = 'PREFERENTIAL' AND ir.maturityPeriod = :maturityPeriod " +
           "ORDER BY ir.effectiveDate DESC")
    Optional<InterestRate> findLatestPreferentialRateByProductCodeAndMaturityPeriod(
            @Param("productCode") String productCode, 
            @Param("maturityPeriod") String maturityPeriod
    );

    @Query("SELECT ir FROM InterestRate ir WHERE ir.productCode = :productCode " +
           "ORDER BY ir.effectiveDate DESC, ir.interestType")
    List<InterestRate> findAllByProductCodeOrderByEffectiveDateDesc(@Param("productCode") String productCode);

    List<InterestRate> findByIsIrp(Boolean isIrp);

    List<InterestRate> findByProductCodeAndIsIrp(String productCode, Boolean isIrp);
}