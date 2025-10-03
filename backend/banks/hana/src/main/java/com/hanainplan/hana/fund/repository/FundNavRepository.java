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

    // 클래스 코드와 기준일로 조회
    Optional<FundNav> findByChildFundCdAndNavDate(String childFundCd, LocalDate navDate);

    // 클래스의 최신 기준가 조회
    @Query("SELECT fn FROM FundNav fn " +
           "WHERE fn.childFundCd = :childFundCd " +
           "ORDER BY fn.navDate DESC " +
           "LIMIT 1")
    Optional<FundNav> findLatestByChildFundCd(@Param("childFundCd") String childFundCd);

    // 클래스의 기준가 이력 조회 (최근순)
    List<FundNav> findByChildFundCdOrderByNavDateDesc(String childFundCd);

    // 기간별 기준가 조회
    @Query("SELECT fn FROM FundNav fn " +
           "WHERE fn.childFundCd = :childFundCd " +
           "AND fn.navDate BETWEEN :startDate AND :endDate " +
           "ORDER BY fn.navDate DESC")
    List<FundNav> findByChildFundCdAndDateRange(
        @Param("childFundCd") String childFundCd,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    // 특정일의 모든 기준가 조회
    List<FundNav> findByNavDate(LocalDate navDate);

    // 최근 N일 기준가 조회
    @Query("SELECT fn FROM FundNav fn " +
           "WHERE fn.childFundCd = :childFundCd " +
           "AND fn.navDate >= :fromDate " +
           "ORDER BY fn.navDate DESC")
    List<FundNav> findRecentNav(
        @Param("childFundCd") String childFundCd,
        @Param("fromDate") LocalDate fromDate
    );

    // 기준가 존재 여부
    boolean existsByChildFundCdAndNavDate(String childFundCd, LocalDate navDate);

    // 가장 오래된 기준가 조회
    @Query("SELECT fn FROM FundNav fn " +
           "WHERE fn.childFundCd = :childFundCd " +
           "ORDER BY fn.navDate ASC " +
           "LIMIT 1")
    Optional<FundNav> findOldestByChildFundCd(@Param("childFundCd") String childFundCd);
}

