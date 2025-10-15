package com.hanainplan.domain.fund.repository;

import com.hanainplan.domain.fund.entity.FundPortfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FundPortfolioRepository extends JpaRepository<FundPortfolio, Long> {

    List<FundPortfolio> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<FundPortfolio> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, String status);

    @Query("SELECT f FROM FundPortfolio f WHERE f.userId = :userId AND f.status IN ('ACTIVE', 'PARTIAL_SOLD') ORDER BY f.createdAt DESC")
    List<FundPortfolio> findActivePortfoliosByUserId(@Param("userId") Long userId);

    @Query("SELECT f FROM FundPortfolio f WHERE f.userId = :userId AND f.fundCode = :fundCode AND f.status IN ('ACTIVE', 'PARTIAL_SOLD')")
    Optional<FundPortfolio> findByUserIdAndFundCodeAndStatus(
        @Param("userId") Long userId,
        @Param("fundCode") String fundCode
    );

    List<FundPortfolio> findByCustomerCiOrderByCreatedAtDesc(String customerCi);

    Optional<FundPortfolio> findByCustomerCiAndSubscriptionId(String customerCi, Long subscriptionId);

    @Query("SELECT f FROM FundPortfolio f WHERE f.customerCi = :customerCi AND f.status IN ('ACTIVE', 'PARTIAL_SOLD') ORDER BY f.createdAt DESC")
    List<FundPortfolio> findActivePortfoliosByCustomerCi(@Param("customerCi") String customerCi);

    List<FundPortfolio> findByUserIdAndBankCodeOrderByCreatedAtDesc(Long userId, String bankCode);

    List<FundPortfolio> findByUserIdAndFundTypeOrderByCreatedAtDesc(Long userId, String fundType);

    List<FundPortfolio> findByIrpAccountNumberOrderByCreatedAtDesc(String irpAccountNumber);

    @Query("SELECT f FROM FundPortfolio f WHERE f.status IN ('ACTIVE', 'PARTIAL_SOLD') ORDER BY f.createdAt DESC")
    List<FundPortfolio> findAllActivePortfolios();

    long countByUserId(Long userId);

    @Query("SELECT COUNT(f) FROM FundPortfolio f WHERE f.userId = :userId AND f.status IN ('ACTIVE', 'PARTIAL_SOLD')")
    long countActivePortfoliosByUserId(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(f.purchaseAmount), 0) FROM FundPortfolio f WHERE f.userId = :userId AND f.status IN ('ACTIVE', 'PARTIAL_SOLD')")
    java.math.BigDecimal getTotalInvestmentByUserId(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(f.currentValue), 0) FROM FundPortfolio f WHERE f.userId = :userId AND f.status IN ('ACTIVE', 'PARTIAL_SOLD')")
    java.math.BigDecimal getTotalCurrentValueByUserId(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(f.totalReturn), 0) FROM FundPortfolio f WHERE f.userId = :userId AND f.status IN ('ACTIVE', 'PARTIAL_SOLD')")
    java.math.BigDecimal getTotalReturnByUserId(@Param("userId") Long userId);
}