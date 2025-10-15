package com.hanainplan.domain.fund.repository;

import com.hanainplan.domain.fund.entity.FundNav;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FundNavRepository extends JpaRepository<FundNav, Long> {

    @Query("SELECT fn FROM FundNav fn WHERE fn.childFundCd = :childFundCd ORDER BY fn.navDate DESC LIMIT 1")
    Optional<FundNav> findLatestByChildFundCd(@Param("childFundCd") String childFundCd);

    List<FundNav> findByChildFundCdOrderByNavDateDesc(String childFundCd);

    Optional<FundNav> findByChildFundCdAndNavDate(String childFundCd, LocalDate navDate);

    boolean existsByChildFundCdAndNavDate(String childFundCd, LocalDate navDate);
}