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

    // 사용자 ID로 펀드 포트폴리오 목록 조회
    List<FundPortfolio> findByUserIdOrderByCreatedAtDesc(Long userId);

    // 사용자 ID와 상태로 조회
    List<FundPortfolio> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, String status);

    // 활성 펀드 포트폴리오 목록 조회
    @Query("SELECT f FROM FundPortfolio f WHERE f.userId = :userId AND f.status IN ('ACTIVE', 'PARTIAL_SOLD') ORDER BY f.createdAt DESC")
    List<FundPortfolio> findActivePortfoliosByUserId(@Param("userId") Long userId);

    // 사용자 ID와 펀드 코드로 활성 포트폴리오 조회
    @Query("SELECT f FROM FundPortfolio f WHERE f.userId = :userId AND f.fundCode = :fundCode AND f.status IN ('ACTIVE', 'PARTIAL_SOLD')")
    Optional<FundPortfolio> findByUserIdAndFundCodeAndStatus(
        @Param("userId") Long userId,
        @Param("fundCode") String fundCode
    );

    // 고객 CI로 조회
    List<FundPortfolio> findByCustomerCiOrderByCreatedAtDesc(String customerCi);

    // 고객 CI로 활성 포트폴리오 조회
    @Query("SELECT f FROM FundPortfolio f WHERE f.customerCi = :customerCi AND f.status IN ('ACTIVE', 'PARTIAL_SOLD') ORDER BY f.createdAt DESC")
    List<FundPortfolio> findActivePortfoliosByCustomerCi(@Param("customerCi") String customerCi);

    // 은행별 포트폴리오 조회
    List<FundPortfolio> findByUserIdAndBankCodeOrderByCreatedAtDesc(Long userId, String bankCode);

    // 펀드 유형별 조회
    List<FundPortfolio> findByUserIdAndFundTypeOrderByCreatedAtDesc(Long userId, String fundType);

    // IRP 계좌번호로 조회
    List<FundPortfolio> findByIrpAccountNumberOrderByCreatedAtDesc(String irpAccountNumber);

    // 모든 활성 포트폴리오 조회 (배치용)
    @Query("SELECT f FROM FundPortfolio f WHERE f.status IN ('ACTIVE', 'PARTIAL_SOLD') ORDER BY f.createdAt DESC")
    List<FundPortfolio> findAllActivePortfolios();

    // 사용자의 펀드 포트폴리오 개수
    long countByUserId(Long userId);

    // 사용자의 활성 펀드 포트폴리오 개수
    @Query("SELECT COUNT(f) FROM FundPortfolio f WHERE f.userId = :userId AND f.status IN ('ACTIVE', 'PARTIAL_SOLD')")
    long countActivePortfoliosByUserId(@Param("userId") Long userId);

    // 사용자의 총 펀드 투자금액 합계
    @Query("SELECT COALESCE(SUM(f.purchaseAmount), 0) FROM FundPortfolio f WHERE f.userId = :userId AND f.status IN ('ACTIVE', 'PARTIAL_SOLD')")
    java.math.BigDecimal getTotalInvestmentByUserId(@Param("userId") Long userId);

    // 사용자의 총 평가금액 합계
    @Query("SELECT COALESCE(SUM(f.currentValue), 0) FROM FundPortfolio f WHERE f.userId = :userId AND f.status IN ('ACTIVE', 'PARTIAL_SOLD')")
    java.math.BigDecimal getTotalCurrentValueByUserId(@Param("userId") Long userId);

    // 사용자의 총 평가손익 합계
    @Query("SELECT COALESCE(SUM(f.totalReturn), 0) FROM FundPortfolio f WHERE f.userId = :userId AND f.status IN ('ACTIVE', 'PARTIAL_SOLD')")
    java.math.BigDecimal getTotalReturnByUserId(@Param("userId") Long userId);
}

