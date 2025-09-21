package com.hanainplan.samsung.product.repository;

import com.hanainplan.samsung.product.entity.PensionInsurance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PensionInsuranceRepository extends JpaRepository<PensionInsurance, Long> {

    Optional<PensionInsurance> findByProductCode(String productCode);

    List<PensionInsurance> findBySubscriptionType(String subscriptionType);

    List<PensionInsurance> findByIsActiveTrue();

    @Query("SELECT p FROM PensionInsurance p WHERE p.subscriptionType = :subscriptionType AND p.isActive = true")
    List<PensionInsurance> findBySubscriptionTypeAndActive(@Param("subscriptionType") String subscriptionType);

    @Query("SELECT p FROM PensionInsurance p WHERE p.premiumPayment <= :maxAmount AND p.isActive = true")
    List<PensionInsurance> findByMaxPremiumPayment(@Param("maxAmount") BigDecimal maxAmount);

    @Query("SELECT p FROM PensionInsurance p WHERE p.productName LIKE %:keyword% AND p.isActive = true")
    List<PensionInsurance> findByProductNameContaining(@Param("keyword") String keyword);

    List<PensionInsurance> findBySalesChannel(String salesChannel);

    List<PensionInsurance> findByIsUniversal(String isUniversal);

    List<PensionInsurance> findByPaymentMethod(String paymentMethod);

    @Query("SELECT p FROM PensionInsurance p WHERE p.currentAnnouncedRate >= :minRate AND p.isActive = true")
    List<PensionInsurance> findByMinCurrentAnnouncedRate(@Param("minRate") BigDecimal minRate);

    @Query("SELECT p FROM PensionInsurance p WHERE p.businessExpenseRatio <= :maxRatio AND p.isActive = true")
    List<PensionInsurance> findByMaxBusinessExpenseRatio(@Param("maxRatio") BigDecimal maxRatio);

    @Query("SELECT p FROM PensionInsurance p WHERE p.expectedReturnRateCurrent >= :minRate AND p.isActive = true")
    List<PensionInsurance> findByMinExpectedReturnRate(@Param("minRate") BigDecimal minRate);
}
