package com.hanainplan.domain.fund.repository;

import com.hanainplan.domain.fund.entity.FundClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FundClassRepository extends JpaRepository<FundClass, String> {

    List<FundClass> findBySaleStatusOrderByChildFundCdAsc(String saleStatus);

    @Query("SELECT fc FROM FundClass fc WHERE fc.fundMaster.fundCd = :fundCd")
    List<FundClass> findByFundMasterFundCd(@Param("fundCd") String fundCd);

    @Query("SELECT fc FROM FundClass fc WHERE fc.fundMaster.assetType = :assetType AND fc.saleStatus = 'ON'")
    List<FundClass> findByAssetType(@Param("assetType") String assetType);

    @Query("SELECT fc FROM FundClass fc WHERE fc.classCode = :classCode AND fc.saleStatus = 'ON'")
    List<FundClass> findByClassCode(@Param("classCode") String classCode);
}