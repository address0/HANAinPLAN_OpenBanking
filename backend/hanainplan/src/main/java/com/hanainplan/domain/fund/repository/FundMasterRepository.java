package com.hanainplan.domain.fund.repository;

import com.hanainplan.domain.fund.entity.FundMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FundMasterRepository extends JpaRepository<FundMaster, String> {

    List<FundMaster> findByIsActiveTrueOrderByFundNameAsc();

    List<FundMaster> findByAssetTypeAndIsActiveTrueOrderByFundNameAsc(String assetType);
}