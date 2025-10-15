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

@Service
@RequiredArgsConstructor
@Slf4j
public class FundNavService {

    private final FundNavRepository fundNavRepository;

    @Transactional
    public FundNav saveOrUpdateNav(String childFundCd, LocalDate navDate, BigDecimal nav) {
        log.info("기준가 저장/업데이트 요청: childFundCd={}, navDate={}, nav={}", 
                childFundCd, navDate, nav);

        Optional<FundNav> existing = fundNavRepository.findByChildFundCdAndNavDate(childFundCd, navDate);

        FundNav fundNav;
        if (existing.isPresent()) {
            fundNav = existing.get();
            fundNav.setNav(nav);
            fundNav.setPublishedAt(LocalDateTime.now());
            log.info("기존 기준가 업데이트: {}", fundNav);
        } else {
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

    public Optional<FundNav> getLatestNav(String childFundCd) {
        return fundNavRepository.findLatestByChildFundCd(childFundCd);
    }

    public Optional<FundNav> getNavByDate(String childFundCd, LocalDate navDate) {
        return fundNavRepository.findByChildFundCdAndNavDate(childFundCd, navDate);
    }
}