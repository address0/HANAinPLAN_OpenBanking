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

    Optional<FundNavHistory> findByFundCodeAndBaseDate(String fundCode, LocalDate baseDate);

    @Query("SELECT h FROM FundNavHistory h WHERE h.fundCode = :fundCode ORDER BY h.baseDate DESC LIMIT 1")
    Optional<FundNavHistory> findLatestByFundCode(@Param("fundCode") String fundCode);

    List<FundNavHistory> findByFundCodeOrderByBaseDateDesc(String fundCode);

    @Query("SELECT h FROM FundNavHistory h WHERE h.fundCode = :fundCode AND h.baseDate BETWEEN :startDate AND :endDate ORDER BY h.baseDate DESC")
    List<FundNavHistory> findByFundCodeAndDateRange(
        @Param("fundCode") String fundCode,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    List<FundNavHistory> findByBaseDate(LocalDate baseDate);

    @Query("SELECT h FROM FundNavHistory h WHERE h.fundCode = :fundCode AND h.baseDate >= :fromDate ORDER BY h.baseDate DESC")
    List<FundNavHistory> findRecentHistory(
        @Param("fundCode") String fundCode,
        @Param("fromDate") LocalDate fromDate
    );

    boolean existsByFundCodeAndBaseDate(String fundCode, LocalDate baseDate);

    @Query("SELECT h FROM FundNavHistory h WHERE h.fundCode = :fundCode ORDER BY h.baseDate ASC LIMIT 1")
    Optional<FundNavHistory> findOldestByFundCode(@Param("fundCode") String fundCode);
}