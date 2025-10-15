package com.hanainplan.domain.fund.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 펀드 기준가 크롤링 결과
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FundNavCrawlResult {

    /**
     * 총 처리 건수
     */
    private int totalCount;

    /**
     * 성공 건수
     */
    private int successCount;

    /**
     * 실패 건수
     */
    private int failureCount;

    /**
     * 실패한 펀드 목록
     */
    @Builder.Default
    private List<FailedFund> failedFunds = new ArrayList<>();

    /**
     * 실행 시작 시간
     */
    private String startTime;

    /**
     * 실행 종료 시간
     */
    private String endTime;

    /**
     * 총 소요 시간 (초)
     */
    private long durationSeconds;

    /**
     * 실패한 펀드 정보
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FailedFund {
        /**
         * 모펀드 코드
         */
        private String fundCd;

        /**
         * 자펀드 코드
         */
        private String childFundCd;

        /**
         * 펀드명
         */
        private String fundName;

        /**
         * 실패 사유
         */
        private String errorMessage;
    }

    /**
     * 성공률 계산
     */
    public double getSuccessRate() {
        if (totalCount == 0) {
            return 0.0;
        }
        return (double) successCount / totalCount * 100;
    }

    /**
     * 실패 펀드 추가
     */
    public void addFailedFund(String fundCd, String childFundCd, String fundName, String errorMessage) {
        failedFunds.add(FailedFund.builder()
                .fundCd(fundCd)
                .childFundCd(childFundCd)
                .fundName(fundName)
                .errorMessage(errorMessage)
                .build());
    }
}

