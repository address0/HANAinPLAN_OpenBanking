package com.hanainplan.domain.banking.repository;

import com.hanainplan.domain.banking.entity.DepositPortfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 정기예금 포트폴리오 Repository
 */
@Repository
public interface DepositPortfolioRepository extends JpaRepository<DepositPortfolio, Long> {
    
    /**
     * 사용자 ID로 포트폴리오 조회
     */
    List<DepositPortfolio> findByUserIdOrderBySubscriptionDateDesc(Long userId);
    
    /**
     * 고객 CI로 포트폴리오 조회
     */
    List<DepositPortfolio> findByCustomerCiOrderBySubscriptionDateDesc(String customerCi);
    
    /**
     * 사용자 ID와 상태로 포트폴리오 조회
     */
    List<DepositPortfolio> findByUserIdAndStatus(Long userId, String status);
    
    /**
     * 은행 코드로 포트폴리오 조회
     */
    List<DepositPortfolio> findByBankCode(String bankCode);
    
    /**
     * 사용자의 활성 정기예금 포트폴리오 합계 조회
     */
    @Query("SELECT COALESCE(SUM(d.principalAmount), 0) FROM DepositPortfolio d " +
           "WHERE d.userId = :userId AND d.status = 'ACTIVE'")
    java.math.BigDecimal getTotalActivePrincipalByUserId(@Param("userId") Long userId);
    
    /**
     * IRP 계좌번호로 포트폴리오 조회 (가입일 최신순)
     */
    List<DepositPortfolio> findByIrpAccountNumberOrderBySubscriptionDateDesc(String irpAccountNumber);
    
    /**
     * IRP 계좌번호와 상태로 포트폴리오 조회
     */
    List<DepositPortfolio> findByIrpAccountNumberAndStatus(String irpAccountNumber, String status);
}

