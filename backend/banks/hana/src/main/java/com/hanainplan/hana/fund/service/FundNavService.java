package com.hanainplan.hana.fund.service;

import com.hanainplan.hana.fund.entity.FundNav;
import com.hanainplan.hana.fund.repository.FundNavRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 펀드 기준가 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FundNavService {

    private final FundNavRepository fundNavRepository;

    /**
     * 펀드 기준가 저장 또는 업데이트
     * - 같은 날짜에 이미 기준가가 있으면 UPDATE
     * - 없으면 새로 INSERT
     *
     * @param childFundCd 자펀드 코드
     * @param navDate 기준일
     * @param nav 기준가
     * @return 저장된 FundNav
     */
    @Transactional
    public FundNav saveOrUpdateNav(String childFundCd, LocalDate navDate, BigDecimal nav) {
        log.info("기준가 저장/업데이트 요청: childFundCd={}, navDate={}, nav={}", 
                childFundCd, navDate, nav);

        Optional<FundNav> existing = fundNavRepository.findByChildFundCdAndNavDate(childFundCd, navDate);

        FundNav fundNav;
        if (existing.isPresent()) {
            // 기존 데이터 업데이트
            fundNav = existing.get();
            fundNav.setNav(nav);
            fundNav.setPublishedAt(LocalDateTime.now());
            log.info("기존 기준가 업데이트: {}", fundNav);
        } else {
            // 새 데이터 삽입
            fundNav = FundNav.builder()
                    .childFundCd(childFundCd)
                    .navDate(navDate)
                    .nav(nav)
                    .publishedAt(LocalDateTime.now())
                    .build();
            log.info("신규 기준가 생성: {}", fundNav);
        }

        return fundNavRepository.save(fundNav);
    }

    /**
     * 최신 기준가 조회
     */
    public Optional<FundNav> getLatestNav(String childFundCd) {
        return fundNavRepository.findLatestByChildFundCd(childFundCd);
    }

    /**
     * 특정 날짜의 기준가 조회
     */
    public Optional<FundNav> getNavByDate(String childFundCd, LocalDate navDate) {
        return fundNavRepository.findByChildFundCdAndNavDate(childFundCd, navDate);
    }
}

