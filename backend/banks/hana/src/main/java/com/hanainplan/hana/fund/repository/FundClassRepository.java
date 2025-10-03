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

    // 모펀드 코드로 클래스 목록 조회
    @Query("SELECT fc FROM FundClass fc WHERE fc.fundMaster.fundCd = :fundCd")
    List<FundClass> findByFundMasterFundCd(@Param("fundCd") String fundCd);

    // 판매중인 클래스만 조회
    List<FundClass> findBySaleStatusOrderByChildFundCdAsc(String saleStatus);

    // 클래스 코드로 조회
    List<FundClass> findByClassCodeOrderByChildFundCdAsc(String classCode);

    // 클래스 상세 정보 포함 조회 (규칙, 수수료 포함)
    @Query("SELECT fc FROM FundClass fc " +
           "LEFT JOIN FETCH fc.fundRules " +
           "LEFT JOIN FETCH fc.fundFees " +
           "WHERE fc.childFundCd = :childFundCd")
    Optional<FundClass> findByChildFundCdWithDetails(@Param("childFundCd") String childFundCd);

    // 판매중인 클래스 (상세 정보 포함)
    @Query("SELECT fc FROM FundClass fc " +
           "LEFT JOIN FETCH fc.fundRules " +
           "LEFT JOIN FETCH fc.fundFees " +
           "LEFT JOIN FETCH fc.fundMaster " +
           "WHERE fc.saleStatus = 'ON' " +
           "ORDER BY fc.childFundCd")
    List<FundClass> findAllOnSaleWithDetails();

    // 클래스 코드 존재 여부
    boolean existsByChildFundCd(String childFundCd);
}

