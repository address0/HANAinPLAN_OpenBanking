package com.hanainplan.domain.banking.repository;

import com.hanainplan.domain.banking.entity.IrpProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * IRP 상품 리포지토리
 */
@Repository
public interface IrpProductRepository extends JpaRepository<IrpProduct, Long> {

    /**
     * 은행 코드와 상품 코드로 IRP 상품 조회
     */
    Optional<IrpProduct> findByBankCodeAndProductCode(String bankCode, String productCode);

    /**
     * 은행 코드로 활성화된 IRP 상품 조회
     */
    List<IrpProduct> findByBankCodeAndIsActiveTrueOrderByCreatedDateDesc(String bankCode);

    /**
     * 모든 활성화된 IRP 상품 조회
     */
    List<IrpProduct> findByIsActiveTrueOrderByBankCodeAscProductNameAsc();

    /**
     * 판매 중인 IRP 상품 조회
     */
    @Query("SELECT ip FROM IrpProduct ip WHERE ip.isActive = true AND " +
           "(ip.startDate IS NULL OR ip.startDate <= CURRENT_DATE) AND " +
           "(ip.endDate IS NULL OR ip.endDate >= CURRENT_DATE)")
    List<IrpProduct> findAvailableProducts();

    /**
     * 특정 은행의 판매 중인 IRP 상품 조회
     */
    @Query("SELECT ip FROM IrpProduct ip WHERE ip.bankCode = :bankCode AND ip.isActive = true AND " +
           "(ip.startDate IS NULL OR ip.startDate <= CURRENT_DATE) AND " +
           "(ip.endDate IS NULL OR ip.endDate >= CURRENT_DATE)")
    List<IrpProduct> findAvailableProductsByBank(@Param("bankCode") String bankCode);

    /**
     * 동기화가 필요한 IRP 상품 조회
     */
    @Query("SELECT ip FROM IrpProduct ip WHERE ip.syncStatus = 'PENDING' OR ip.lastSyncDate < :threshold")
    List<IrpProduct> findProductsNeedingSync(@Param("threshold") LocalDateTime threshold);

    /**
     * 동기화 실패한 IRP 상품 조회
     */
    List<IrpProduct> findBySyncStatusOrderByLastSyncDateDesc(String syncStatus);


    /**
     * 은행별 IRP 상품 통계 조회
     */
    @Query("SELECT ip.bankCode, COUNT(ip) FROM IrpProduct ip WHERE ip.isActive = true GROUP BY ip.bankCode")
    List<Object[]> getIrpProductStatisticsByBank();

    /**
     * 상품명으로 검색
     */
    List<IrpProduct> findByProductNameContainingIgnoreCaseOrderByBankCodeAscProductNameAsc(String productName);

    /**
     * 특정 은행의 상품명으로 검색
     */
    List<IrpProduct> findByBankCodeAndProductNameContainingIgnoreCaseOrderByProductNameAsc(String bankCode, String productName);

    /**
     * 최소 납입금액 범위로 상품 조회
     */
    @Query("SELECT ip FROM IrpProduct ip WHERE ip.isActive = true AND " +
           "ip.minimumContribution >= :minAmount AND ip.minimumContribution <= :maxAmount " +
           "ORDER BY ip.bankCode, ip.minimumContribution")
    List<IrpProduct> findByContributionRange(@Param("minAmount") java.math.BigDecimal minAmount,
                                           @Param("maxAmount") java.math.BigDecimal maxAmount);

    /**
     * 특정 은행의 최소 납입금액 범위로 상품 조회
     */
    @Query("SELECT ip FROM IrpProduct ip WHERE ip.bankCode = :bankCode AND ip.isActive = true AND " +
           "ip.minimumContribution >= :minAmount AND ip.minimumContribution <= :maxAmount " +
           "ORDER BY ip.minimumContribution")
    List<IrpProduct> findByBankAndContributionRange(@Param("bankCode") String bankCode,
                                                  @Param("minAmount") java.math.BigDecimal minAmount,
                                                  @Param("maxAmount") java.math.BigDecimal maxAmount);

    /**
     * 위험 등급별 상품 조회
     */
    List<IrpProduct> findByRiskLevelOrderByBankCodeAscExpectedReturnRateDesc(String riskLevel);

    /**
     * 특정 은행의 위험 등급별 상품 조회
     */
    List<IrpProduct> findByBankCodeAndRiskLevelOrderByExpectedReturnRateDesc(String bankCode, String riskLevel);

    /**
     * 예상 수익률 순으로 상품 조회 (내림차순)
     */
    @Query("SELECT ip FROM IrpProduct ip WHERE ip.isActive = true ORDER BY ip.expectedReturnRate DESC")
    List<IrpProduct> findByExpectedReturnRateDesc();

    /**
     * 특정 은행의 예상 수익률 순으로 상품 조회
     */
    @Query("SELECT ip FROM IrpProduct ip WHERE ip.bankCode = :bankCode AND ip.isActive = true ORDER BY ip.expectedReturnRate DESC")
    List<IrpProduct> findByBankAndExpectedReturnRateDesc(@Param("bankCode") String bankCode);

    /**
     * 총 수수료율 순으로 상품 조회 (오름차순)
     */
    @Query("SELECT ip FROM IrpProduct ip WHERE ip.isActive = true ORDER BY ip.totalFeeRate ASC")
    List<IrpProduct> findByTotalFeeRateAsc();

    /**
     * 특정 은행의 총 수수료율 순으로 상품 조회
     */
    @Query("SELECT ip FROM IrpProduct ip WHERE ip.bankCode = :bankCode AND ip.isActive = true ORDER BY ip.totalFeeRate ASC")
    List<IrpProduct> findByBankAndTotalFeeRateAsc(@Param("bankCode") String bankCode);
}
