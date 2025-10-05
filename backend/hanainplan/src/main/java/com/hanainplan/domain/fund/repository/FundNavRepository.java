package com.hanainplan.domain.fund.repository;

import com.hanainplan.domain.fund.entity.FundNav;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * FundNav Repository
 */
@Repository
public interface FundNavRepository extends JpaRepository<FundNav, Long> {

    /**
     * 특정 펀드 클래스의 최신 기준가 조회
     */
    @Query("SELECT fn FROM FundNav fn WHERE fn.childFundCd = :childFundCd ORDER BY fn.navDate DESC LIMIT 1")
    Optional<FundNav> findLatestByChildFundCd(@Param("childFundCd") String childFundCd);

    /**
     * 특정 펀드 클래스의 기준가 이력 조회
     */
    List<FundNav> findByChildFundCdOrderByNavDateDesc(String childFundCd);

    /**
     * 특정 날짜의 기준가 조회
     */
    Optional<FundNav> findByChildFundCdAndNavDate(String childFundCd, LocalDate navDate);

    /**
     * 특정 펀드의 기준가 존재 여부 확인
     */
    boolean existsByChildFundCdAndNavDate(String childFundCd, LocalDate navDate);
}

