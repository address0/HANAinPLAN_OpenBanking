package com.hanainplan.domain.portfolio.repository;

import com.hanainplan.domain.portfolio.entity.RebalancingOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RebalancingOrderRepository extends JpaRepository<RebalancingOrder, Long> {

    List<RebalancingOrder> findByJobIdOrderByCreatedAtDesc(Long jobId);

    List<RebalancingOrder> findByJobIdAndStatus(Long jobId, RebalancingOrder.OrderStatus status);

    List<RebalancingOrder> findByJobIdAndOrderType(Long jobId, RebalancingOrder.OrderType orderType);

    List<RebalancingOrder> findByJobIdAndAssetType(Long jobId, RebalancingOrder.AssetType assetType);

    List<RebalancingOrder> findByStatus(RebalancingOrder.OrderStatus status);

    List<RebalancingOrder> findByOrderType(RebalancingOrder.OrderType orderType);

    List<RebalancingOrder> findByAssetType(RebalancingOrder.AssetType assetType);

    @Query("SELECT ro FROM RebalancingOrder ro WHERE ro.jobId = :jobId AND ro.status IN :statuses ORDER BY ro.createdAt DESC")
    List<RebalancingOrder> findByJobIdAndStatusIn(@Param("jobId") Long jobId, @Param("statuses") List<RebalancingOrder.OrderStatus> statuses);

    @Query("SELECT ro FROM RebalancingOrder ro WHERE ro.jobId = :jobId AND ro.orderType = :orderType AND ro.status = :status ORDER BY ro.createdAt DESC")
    List<RebalancingOrder> findByJobIdAndOrderTypeAndStatus(@Param("jobId") Long jobId, 
                                                           @Param("orderType") RebalancingOrder.OrderType orderType, 
                                                           @Param("status") RebalancingOrder.OrderStatus status);

    @Query("SELECT ro FROM RebalancingOrder ro WHERE ro.fundCode = :fundCode AND ro.status = :status ORDER BY ro.createdAt DESC")
    List<RebalancingOrder> findByFundCodeAndStatus(@Param("fundCode") String fundCode, 
                                                   @Param("status") RebalancingOrder.OrderStatus status);

    @Query("SELECT ro FROM RebalancingOrder ro WHERE ro.classCode = :classCode AND ro.status = :status ORDER BY ro.createdAt DESC")
    List<RebalancingOrder> findByClassCodeAndStatus(@Param("classCode") String classCode, 
                                                   @Param("status") RebalancingOrder.OrderStatus status);

    @Query("SELECT COUNT(ro) FROM RebalancingOrder ro WHERE ro.jobId = :jobId AND ro.status = :status")
    long countByJobIdAndStatus(@Param("jobId") Long jobId, @Param("status") RebalancingOrder.OrderStatus status);

    @Query("SELECT COUNT(ro) FROM RebalancingOrder ro WHERE ro.jobId = :jobId AND ro.orderType = :orderType AND ro.status = :status")
    long countByJobIdAndOrderTypeAndStatus(@Param("jobId") Long jobId, 
                                          @Param("orderType") RebalancingOrder.OrderType orderType, 
                                          @Param("status") RebalancingOrder.OrderStatus status);

    @Query("SELECT SUM(ro.orderAmount) FROM RebalancingOrder ro WHERE ro.jobId = :jobId AND ro.orderType = :orderType AND ro.status = :status")
    java.math.BigDecimal sumOrderAmountByJobIdAndOrderTypeAndStatus(@Param("jobId") Long jobId, 
                                                                   @Param("orderType") RebalancingOrder.OrderType orderType, 
                                                                   @Param("status") RebalancingOrder.OrderStatus status);

    @Query("SELECT SUM(ro.fee) FROM RebalancingOrder ro WHERE ro.jobId = :jobId AND ro.status = :status")
    java.math.BigDecimal sumFeeByJobIdAndStatus(@Param("jobId") Long jobId, @Param("status") RebalancingOrder.OrderStatus status);
}
