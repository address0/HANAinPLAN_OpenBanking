package com.hanainplan.hana.product.repository;

import com.hanainplan.hana.product.entity.GeneralInsurance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface GeneralInsuranceRepository extends JpaRepository<GeneralInsurance, Long> {

    Optional<GeneralInsurance> findByProductCode(String productCode);

    List<GeneralInsurance> findByCategory(String category);

    List<GeneralInsurance> findByIsActiveTrue();

    @Query("SELECT p FROM GeneralInsurance p WHERE p.category = :category AND p.isActive = true")
    List<GeneralInsurance> findByCategoryAndActive(@Param("category") String category);

    @Query("SELECT p FROM GeneralInsurance p WHERE p.paymentAmount >= :minAmount AND p.isActive = true")
    List<GeneralInsurance> findByMinPaymentAmount(@Param("minAmount") BigDecimal minAmount);

    @Query("SELECT p FROM GeneralInsurance p WHERE p.subscriptionAmountBasic <= :maxAmount AND p.isActive = true")
    List<GeneralInsurance> findByMaxSubscriptionAmount(@Param("maxAmount") BigDecimal maxAmount);

    @Query("SELECT p FROM GeneralInsurance p WHERE p.productName LIKE %:keyword% AND p.isActive = true")
    List<GeneralInsurance> findByProductNameContaining(@Param("keyword") String keyword);

    List<GeneralInsurance> findBySalesChannel(String salesChannel);

    List<GeneralInsurance> findByIsUniversal(String isUniversal);

    @Query("SELECT p FROM GeneralInsurance p WHERE p.interestRate >= :minRate AND p.isActive = true")
    List<GeneralInsurance> findByMinInterestRate(@Param("minRate") BigDecimal minRate);
}
