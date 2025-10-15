package com.hanainplan.domain.fund.service;

import com.hanainplan.domain.fund.dto.FundNavCrawlResult;
import com.hanainplan.domain.fund.entity.FundClass;
import com.hanainplan.domain.fund.entity.FundNav;
import com.hanainplan.domain.fund.repository.FundClassRepository;
import com.hanainplan.domain.fund.repository.FundNavRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class FundNavCrawlerService {

    private final FundClassRepository fundClassRepository;
    private final FundNavRepository fundNavRepository;
    private final RestTemplate restTemplate;

    @Value("${fund.crawler.python-path:python3}")
    private String pythonPath;

    @Value("${fund.crawler.script-path:backend/hanainplan/crawler/fund_crawler.py}")
    private String scriptPath;

    @Value("${fund.crawler.timeout-seconds:10}")
    private long timeoutSeconds;

    @Value("${bank.hana.url:http://localhost:8081}")
    private String hanaBankUrl;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Transactional
    public FundNavCrawlResult crawlAndUpdateAllFundNav() {
        LocalDateTime startTime = LocalDateTime.now();
        log.info("====================================================");
        log.info("펀드 기준가 크롤링 시작: {}", startTime.format(TIME_FORMATTER));
        log.info("====================================================");

        FundNavCrawlResult result = FundNavCrawlResult.builder()
                .startTime(startTime.format(TIME_FORMATTER))
                .build();

        List<FundClass> fundClasses = fundClassRepository.findAll();
        result.setTotalCount(fundClasses.size());

        log.info("총 {}개의 펀드 클래스를 크롤링합니다.", fundClasses.size());

        int successCount = 0;
        int failureCount = 0;

        for (FundClass fundClass : fundClasses) {
            try {
                String fundCd = fundClass.getFundMaster().getFundCd();
                String childFundCd = fundClass.getChildFundCd();
                String fundName = fundClass.getDisplayName();

                log.debug("크롤링 시작: {} (모펀드: {}, 자펀드: {})", fundName, fundCd, childFundCd);

                BigDecimal nav = executePythonCrawler(fundCd, childFundCd);

                if (nav != null) {
                    saveOrUpdateNav(childFundCd, nav);
                    successCount++;
                    log.info("✓ 크롤링 성공: {} - 기준가: {}", fundName, nav);
                } else {
                    failureCount++;
                    result.addFailedFund(fundCd, childFundCd, fundName, "기준가를 찾을 수 없음");
                    log.warn("✗ 크롤링 실패: {} - 기준가를 찾을 수 없음", fundName);
                }

            } catch (Exception e) {
                failureCount++;
                String fundName = fundClass.getDisplayName();
                result.addFailedFund(
                        fundClass.getFundMaster().getFundCd(),
                        fundClass.getChildFundCd(),
                        fundName,
                        e.getMessage()
                );
                log.error("✗ 크롤링 실패: {} - {}", fundName, e.getMessage());
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("크롤링 딜레이 중 인터럽트 발생");
            }
        }

        LocalDateTime endTime = LocalDateTime.now();
        result.setSuccessCount(successCount);
        result.setFailureCount(failureCount);
        result.setEndTime(endTime.format(TIME_FORMATTER));
        result.setDurationSeconds(
                java.time.Duration.between(startTime, endTime).getSeconds()
        );

        log.info("====================================================");
        log.info("펀드 기준가 크롤링 완료: {}", endTime.format(TIME_FORMATTER));
        log.info("총 {}건 / 성공 {}건 / 실패 {}건 / 성공률 {:.2f}%",
                result.getTotalCount(),
                result.getSuccessCount(),
                result.getFailureCount(),
                result.getSuccessRate());
        log.info("소요 시간: {}초", result.getDurationSeconds());
        log.info("====================================================");

        return result;
    }

    private BigDecimal executePythonCrawler(String fundCd, String childFundCd) throws Exception {
        File scriptFile = new File(scriptPath);
        String absoluteScriptPath = scriptFile.getAbsolutePath();

        ProcessBuilder processBuilder = new ProcessBuilder(
                pythonPath,
                absoluteScriptPath,
                fundCd,
                childFundCd
        );

        processBuilder.directory(new File(System.getProperty("user.dir")));

        Process process = processBuilder.start();

        StringBuilder output = new StringBuilder();
        StringBuilder errorOutput = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
             BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {

            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);

            if (!finished) {
                process.destroyForcibly();
                throw new RuntimeException("크롤링 타임아웃 (" + timeoutSeconds + "초 초과)");
            }

            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            while ((line = errorReader.readLine()) != null) {
                errorOutput.append(line).append("\n");
            }

            int exitCode = process.exitValue();

            if (exitCode != 0) {
                throw new RuntimeException("Python 크롤러 실행 실패 (exit code: " + exitCode + "): " + errorOutput.toString());
            }

            String result = output.toString().trim();
            if (result.isEmpty()) {
                return null;
            }

            return new BigDecimal(result);

        } finally {
            process.destroyForcibly();
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void saveOrUpdateNav(String childFundCd, BigDecimal nav) {
        LocalDate today = LocalDate.now();

        FundNav fundNav = fundNavRepository.findByChildFundCdAndNavDate(childFundCd, today)
                .orElse(null);

        if (fundNav != null) {
            fundNav.setNav(nav);
            fundNav.setPublishedAt(LocalDateTime.now());
            fundNavRepository.save(fundNav);
            log.debug("기준가 업데이트: {} - {}", childFundCd, nav);
        } else {
            fundNav = FundNav.builder()
                    .childFundCd(childFundCd)
                    .navDate(today)
                    .nav(nav)
                    .publishedAt(LocalDateTime.now())
                    .build();
            fundNavRepository.save(fundNav);
            log.debug("기준가 신규 생성: {} - {}", childFundCd, nav);
        }

        syncToHanaBank(childFundCd, today, nav);
    }

    private void syncToHanaBank(String childFundCd, LocalDate navDate, BigDecimal nav) {
        try {
            String url = hanaBankUrl + "/api/hana/fund-nav";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("childFundCd", childFundCd);
            requestBody.put("navDate", navDate);
            requestBody.put("nav", nav);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.debug("하나은행 기준가 동기화 성공: {} - {}", childFundCd, nav);
            } else {
                log.warn("하나은행 기준가 동기화 실패: {} - status: {}", childFundCd, response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("하나은행 기준가 동기화 중 오류 발생: {} - {}", childFundCd, e.getMessage());
        }
    }

    @Transactional
    public BigDecimal crawlSingleFundNav(String childFundCd) {
        FundClass fundClass = fundClassRepository.findById(childFundCd)
                .orElseThrow(() -> new IllegalArgumentException("펀드 클래스를 찾을 수 없습니다: " + childFundCd));

        String fundCd = fundClass.getFundMaster().getFundCd();

        try {
            BigDecimal nav = executePythonCrawler(fundCd, childFundCd);
            if (nav != null) {
                saveOrUpdateNav(childFundCd, nav);
                log.info("펀드 기준가 크롤링 성공: {} - {}", fundClass.getDisplayName(), nav);
                return nav;
            } else {
                throw new RuntimeException("기준가를 찾을 수 없습니다.");
            }
        } catch (Exception e) {
            log.error("펀드 기준가 크롤링 실패: {}", fundClass.getDisplayName(), e);
            throw new RuntimeException("크롤링 실패: " + e.getMessage(), e);
        }
    }
}