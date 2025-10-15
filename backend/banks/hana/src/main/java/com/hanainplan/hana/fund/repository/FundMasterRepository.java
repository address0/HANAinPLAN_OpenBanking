package com.hanainplan.hana.fund.repository;

import com.hanainplan.hana.fund.entity.FundMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FundMasterRepository extends JpaRepository<FundMaster, String> {

    List<FundMaster> findByIsActiveTrueOrderByFundNameAsc();

    List<FundMaster> findByFundNameContainingAndIsActiveTrue(String fundName);

    List<FundMaster> findByAssetTypeAndIsActiveTrue(String assetType);

    List<FundMaster> findByRiskGradeAndIsActiveTrue(String riskGrade);

    List<FundMaster> findByFundGbAndIsActiveTrue(Integer fundGb);

    boolean existsByFundCd(String fundCd);

    @Query("SELECT DISTINCT fm FROM FundMaster fm " +
           "LEFT JOIN FETCH fm.fundClasses " +
           "WHERE fm.isActive = true " +
           "ORDER BY fm.fundName")
    List<FundMaster> findAllWithClasses();

    @Query("SELECT fm FROM FundMaster fm " +
           "LEFT JOIN FETCH fm.fundClasses " +
           "WHERE fm.fundCd = :fundCd")
    FundMaster findByFundCdWithClasses(@Param("fundCd") String fundCd);
}