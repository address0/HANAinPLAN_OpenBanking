package com.hanainplan.domain.banking.repository;

import com.hanainplan.domain.banking.entity.DepositPortfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepositPortfolioRepository extends JpaRepository<DepositPortfolio, Long> {

    List<DepositPortfolio> findByUserIdOrderBySubscriptionDateDesc(Long userId);

    List<DepositPortfolio> findByCustomerCiOrderBySubscriptionDateDesc(String customerCi);

    List<DepositPortfolio> findByUserIdAndStatus(Long userId, String status);

    List<DepositPortfolio> findByBankCode(String bankCode);

    @Query("SELECT COALESCE(SUM(d.principalAmount), 0) FROM DepositPortfolio d " +
           "WHERE d.userId = :userId AND d.status = 'ACTIVE'")
    java.math.BigDecimal getTotalActivePrincipalByUserId(@Param("userId") Long userId);

    List<DepositPortfolio> findByIrpAccountNumberOrderBySubscriptionDateDesc(String irpAccountNumber);

    List<DepositPortfolio> findByIrpAccountNumberAndStatus(String irpAccountNumber, String status);
}