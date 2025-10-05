package com.hanainplan.domain.fund.repository;

import com.hanainplan.domain.fund.entity.FundMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * FundMaster Repository
 */
@Repository
public interface FundMasterRepository extends JpaRepository<FundMaster, String> {

    /**
     * 활성화된 모펀드 목록 조회
     */
    List<FundMaster> findByIsActiveTrueOrderByFundNameAsc();

    /**
     * 자산 유형별 조회
     */
    List<FundMaster> findByAssetTypeAndIsActiveTrueOrderByFundNameAsc(String assetType);
}

