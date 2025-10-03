package com.hanainplan.hana.fund.repository;

import com.hanainplan.hana.fund.entity.FundNavHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FundNavHistoryRepository extends JpaRepository<FundNavHistory, Long> {

    // 펀드 코드와 기준일로 조회
    Optional<FundNavHistory> findByFundCodeAndBaseDate(String fundCode, LocalDate baseDate);

    // 펀드의 최신 기준가 조회
    @Query("SELECT h FROM FundNavHistory h WHERE h.fundCode = :fundCode ORDER BY h.baseDate DESC LIMIT 1")
    Optional<FundNavHistory> findLatestByFundCode(@Param("fundCode") String fundCode);

    // 펀드의 기준가 이력 조회 (최근순)
    List<FundNavHistory> findByFundCodeOrderByBaseDateDesc(String fundCode);

    // 펀드의 기준가 이력 조회 (기간 지정)
    @Query("SELECT h FROM FundNavHistory h WHERE h.fundCode = :fundCode AND h.baseDate BETWEEN :startDate AND :endDate ORDER BY h.baseDate DESC")
    List<FundNavHistory> findByFundCodeAndDateRange(
        @Param("fundCode") String fundCode,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    // 특정일의 모든 펀드 기준가 조회
    List<FundNavHistory> findByBaseDate(LocalDate baseDate);

    // 최근 N일간의 기준가 조회
    @Query("SELECT h FROM FundNavHistory h WHERE h.fundCode = :fundCode AND h.baseDate >= :fromDate ORDER BY h.baseDate DESC")
    List<FundNavHistory> findRecentHistory(
        @Param("fundCode") String fundCode,
        @Param("fromDate") LocalDate fromDate
    );

    // 기준가 존재 여부 확인
    boolean existsByFundCodeAndBaseDate(String fundCode, LocalDate baseDate);

    // 펀드의 가장 오래된 기준가 조회
    @Query("SELECT h FROM FundNavHistory h WHERE h.fundCode = :fundCode ORDER BY h.baseDate ASC LIMIT 1")
    Optional<FundNavHistory> findOldestByFundCode(@Param("fundCode") String fundCode);
}

