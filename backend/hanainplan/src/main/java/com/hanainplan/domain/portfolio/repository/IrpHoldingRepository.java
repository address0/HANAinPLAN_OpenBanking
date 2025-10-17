package com.hanainplan.domain.portfolio.repository;

import com.hanainplan.domain.portfolio.entity.IrpHolding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IrpHoldingRepository extends JpaRepository<IrpHolding, Long> {

    List<IrpHolding> findByCustomerIdOrderByAssetTypeAsc(Long customerId);

    List<IrpHolding> findByCustomerIdAndIrpAccountNumberOrderByAssetTypeAsc(Long customerId, String irpAccountNumber);

    List<IrpHolding> findByCustomerIdAndAssetTypeOrderByCurrentValueDesc(Long customerId, IrpHolding.AssetType assetType);

    @Query("SELECT ih FROM IrpHolding ih WHERE ih.customerId = :customerId AND ih.status = 'ACTIVE' ORDER BY ih.assetType ASC")
    List<IrpHolding> findActiveHoldingsByCustomer(@Param("customerId") Long customerId);

    @Query("SELECT ih FROM IrpHolding ih WHERE ih.customerId = :customerId AND ih.irpAccountNumber = :irpAccountNumber AND ih.status = 'ACTIVE' ORDER BY ih.assetType ASC")
    List<IrpHolding> findActiveHoldingsByCustomerAndAccount(@Param("customerId") Long customerId, @Param("irpAccountNumber") String irpAccountNumber);

    @Query("SELECT SUM(ih.currentValue) FROM IrpHolding ih WHERE ih.customerId = :customerId AND ih.status = 'ACTIVE'")
    Optional<Double> calculateTotalValueByCustomer(@Param("customerId") Long customerId);

    @Query("SELECT SUM(ih.currentValue) FROM IrpHolding ih WHERE ih.customerId = :customerId AND ih.assetType = :assetType AND ih.status = 'ACTIVE'")
    Optional<Double> calculateTotalValueByCustomerAndAssetType(@Param("customerId") Long customerId, @Param("assetType") IrpHolding.AssetType assetType);

    @Query("SELECT ih FROM IrpHolding ih WHERE ih.assetType = :assetType AND ih.status = 'ACTIVE' AND ih.lastSyncedAt < :beforeDate")
    List<IrpHolding> findStaleHoldingsByAssetType(@Param("assetType") IrpHolding.AssetType assetType, @Param("beforeDate") LocalDateTime beforeDate);

    @Query("SELECT ih FROM IrpHolding ih WHERE ih.customerId = :customerId AND ih.assetCode = :assetCode AND ih.status = 'ACTIVE'")
    Optional<IrpHolding> findByCustomerAndAssetCode(@Param("customerId") Long customerId, @Param("assetCode") String assetCode);

    @Query("SELECT ih FROM IrpHolding ih WHERE ih.customerId = :customerId AND ih.assetType = :assetType AND ih.assetCode = :assetCode AND ih.status = 'ACTIVE'")
    Optional<IrpHolding> findByCustomerAndAssetTypeAndCode(@Param("customerId") Long customerId, @Param("assetType") IrpHolding.AssetType assetType, @Param("assetCode") String assetCode);

    @Query("SELECT DISTINCT ih.customerId FROM IrpHolding ih WHERE ih.status = 'ACTIVE'")
    List<Long> findDistinctActiveCustomerIds();

    @Query("SELECT ih FROM IrpHolding ih WHERE ih.customerId = :customerId AND ih.maturityDate <= :currentDate AND ih.assetType = 'DEPOSIT' AND ih.status = 'ACTIVE'")
    List<IrpHolding> findMaturedDeposits(@Param("customerId") Long customerId, @Param("currentDate") LocalDateTime currentDate);

    @Query("SELECT ih FROM IrpHolding ih WHERE ih.lastSyncedAt < :beforeDate ORDER BY ih.lastSyncedAt ASC")
    List<IrpHolding> findHoldingsNeedingSync(@Param("beforeDate") LocalDateTime beforeDate);

    boolean existsByCustomerIdAndAssetTypeAndAssetCodeAndStatus(Long customerId, IrpHolding.AssetType assetType, String assetCode, String status);

    void deleteByCustomerIdAndAssetTypeAndAssetCode(Long customerId, IrpHolding.AssetType assetType, String assetCode);
}
