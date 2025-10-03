package com.hanainplan.hana.fund.repository;

import com.hanainplan.hana.fund.entity.FundMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FundMasterRepository extends JpaRepository<FundMaster, String> {

    // 활성 펀드 목록 조회
    List<FundMaster> findByIsActiveTrueOrderByFundNameAsc();

    // 펀드명으로 검색
    List<FundMaster> findByFundNameContainingAndIsActiveTrue(String fundName);

    // 자산 유형별 조회
    List<FundMaster> findByAssetTypeAndIsActiveTrue(String assetType);

    // 위험등급별 조회
    List<FundMaster> findByRiskGradeAndIsActiveTrue(String riskGrade);

    // 펀드 구분별 조회
    List<FundMaster> findByFundGbAndIsActiveTrue(Integer fundGb);

    // 펀드 코드 존재 여부
    boolean existsByFundCd(String fundCd);

    // 클래스 정보 포함 조회 (Fetch Join)
    @Query("SELECT DISTINCT fm FROM FundMaster fm " +
           "LEFT JOIN FETCH fm.fundClasses " +
           "WHERE fm.isActive = true " +
           "ORDER BY fm.fundName")
    List<FundMaster> findAllWithClasses();

    // 특정 펀드의 클래스 정보 포함 조회
    @Query("SELECT fm FROM FundMaster fm " +
           "LEFT JOIN FETCH fm.fundClasses " +
           "WHERE fm.fundCd = :fundCd")
    FundMaster findByFundCdWithClasses(@Param("fundCd") String fundCd);
}

