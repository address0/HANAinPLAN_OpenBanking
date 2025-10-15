package com.hanainplan.domain.fund.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FundNavCrawlResult {

    private int totalCount;

    private int successCount;

    private int failureCount;

    @Builder.Default
    private List<FailedFund> failedFunds = new ArrayList<>();

    private String startTime;

    private String endTime;

    private long durationSeconds;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FailedFund {
        private String fundCd;

        private String childFundCd;

        private String fundName;

        private String errorMessage;
    }

    public double getSuccessRate() {
        if (totalCount == 0) {
            return 0.0;
        }
        return (double) successCount / totalCount * 100;
    }

    public void addFailedFund(String fundCd, String childFundCd, String fundName, String errorMessage) {
        failedFunds.add(FailedFund.builder()
                .fundCd(fundCd)
                .childFundCd(childFundCd)
                .fundName(fundName)
                .errorMessage(errorMessage)
                .build());
    }
}