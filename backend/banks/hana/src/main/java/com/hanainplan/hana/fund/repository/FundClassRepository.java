package com.hanainplan.hana.fund.repository;

import com.hanainplan.hana.fund.entity.FundClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FundClassRepository extends JpaRepository<FundClass, String> {

    @Query("SELECT fc FROM FundClass fc WHERE fc.fundMaster.fundCd = :fundCd")
    List<FundClass> findByFundMasterFundCd(@Param("fundCd") String fundCd);

    List<FundClass> findBySaleStatusOrderByChildFundCdAsc(String saleStatus);

    List<FundClass> findByClassCodeOrderByChildFundCdAsc(String classCode);

    @Query("SELECT fc FROM FundClass fc " +
           "LEFT JOIN FETCH fc.fundRules " +
           "LEFT JOIN FETCH fc.fundFees " +
           "WHERE fc.childFundCd = :childFundCd")
    Optional<FundClass> findByChildFundCdWithDetails(@Param("childFundCd") String childFundCd);

    @Query("SELECT fc FROM FundClass fc " +
           "LEFT JOIN FETCH fc.fundRules " +
           "LEFT JOIN FETCH fc.fundFees " +
           "LEFT JOIN FETCH fc.fundMaster " +
           "WHERE fc.saleStatus = 'ON' " +
           "ORDER BY fc.childFundCd")
    List<FundClass> findAllOnSaleWithDetails();

    boolean existsByChildFundCd(String childFundCd);
}