package com.hanainplan.kookmin.product.repository;

import com.hanainplan.kookmin.product.entity.ProductSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductSubscriptionRepository extends JpaRepository<ProductSubscription, Long> {

    List<ProductSubscription> findByCustomerCiOrderBySubscriptionDateDesc(String customerCi);

    Optional<ProductSubscription> findByAccountNumber(String accountNumber);

    List<ProductSubscription> findByProductCode(String productCode);

    List<ProductSubscription> findByStatus(String status);

    List<ProductSubscription> findByCustomerCiAndStatus(String customerCi, String status);

    @Query("SELECT s FROM ProductSubscription s WHERE s.maturityDate BETWEEN :startDate AND :endDate AND s.status = 'ACTIVE'")
    List<ProductSubscription> findUpcomingMaturities(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT s FROM ProductSubscription s WHERE s.nextInterestPaymentDate BETWEEN :startDate AND :endDate AND s.status = 'ACTIVE'")
    List<ProductSubscription> findUpcomingInterestPayments(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT s FROM ProductSubscription s WHERE s.subscriptionDate BETWEEN :startDate AND :endDate")
    List<ProductSubscription> findBySubscriptionDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT s FROM ProductSubscription s WHERE s.missedInstallments > 0 AND s.status = 'ACTIVE'")
    List<ProductSubscription> findWithMissedInstallments();

    @Query("SELECT s FROM ProductSubscription s WHERE s.finalAppliedRate >= :minRate AND s.status = 'ACTIVE'")
    List<ProductSubscription> findByMinInterestRate(@Param("minRate") java.math.BigDecimal minRate);

    List<ProductSubscription> findByBranchName(String branchName);

    boolean existsByCustomerCiAndProductCode(String customerCi, String productCode);
}