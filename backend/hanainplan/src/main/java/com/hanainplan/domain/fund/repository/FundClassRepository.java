package com.hanainplan.domain.fund.repository;

import com.hanainplan.domain.fund.entity.FundClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * FundClass Repository
 */
@Repository
public interface FundClassRepository extends JpaRepository<FundClass, String> {

    /**
     * 판매중인 펀드 클래스 목록 조회
     */
    List<FundClass> findBySaleStatusOrderByChildFundCdAsc(String saleStatus);

    /**
     * 모펀드 코드로 클래스 목록 조회
     */
    @Query("SELECT fc FROM FundClass fc WHERE fc.fundMaster.fundCd = :fundCd")
    List<FundClass> findByFundMasterFundCd(@Param("fundCd") String fundCd);

    /**
     * 자산 유형별 조회
     */
    @Query("SELECT fc FROM FundClass fc WHERE fc.fundMaster.assetType = :assetType AND fc.saleStatus = 'ON'")
    List<FundClass> findByAssetType(@Param("assetType") String assetType);

    /**
     * 클래스 코드별 조회
     */
    @Query("SELECT fc FROM FundClass fc WHERE fc.classCode = :classCode AND fc.saleStatus = 'ON'")
    List<FundClass> findByClassCode(@Param("classCode") String classCode);
}

