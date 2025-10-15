package com.hanainplan.hana.fund.repository;

import com.hanainplan.hana.fund.entity.FundNav;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FundNavRepository extends JpaRepository<FundNav, FundNav.FundNavId> {

    Optional<FundNav> findByChildFundCdAndNavDate(String childFundCd, LocalDate navDate);

    @Query("SELECT fn FROM FundNav fn " +
           "WHERE fn.childFundCd = :childFundCd " +
           "ORDER BY fn.navDate DESC " +
           "LIMIT 1")
    Optional<FundNav> findLatestByChildFundCd(@Param("childFundCd") String childFundCd);

    List<FundNav> findByChildFundCdOrderByNavDateDesc(String childFundCd);

    @Query("SELECT fn FROM FundNav fn " +
           "WHERE fn.childFundCd = :childFundCd " +
           "AND fn.navDate BETWEEN :startDate AND :endDate " +
           "ORDER BY fn.navDate DESC")
    List<FundNav> findByChildFundCdAndDateRange(
        @Param("childFundCd") String childFundCd,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    List<FundNav> findByNavDate(LocalDate navDate);

    @Query("SELECT fn FROM FundNav fn " +
           "WHERE fn.childFundCd = :childFundCd " +
           "AND fn.navDate >= :fromDate " +
           "ORDER BY fn.navDate DESC")
    List<FundNav> findRecentNav(
        @Param("childFundCd") String childFundCd,
        @Param("fromDate") LocalDate fromDate
    );

    boolean existsByChildFundCdAndNavDate(String childFundCd, LocalDate navDate);

    @Query("SELECT fn FROM FundNav fn " +
           "WHERE fn.childFundCd = :childFundCd " +
           "ORDER BY fn.navDate ASC " +
           "LIMIT 1")
    Optional<FundNav> findOldestByChildFundCd(@Param("childFundCd") String childFundCd);
}