package com.hanainplan.domain.portfolio.dto;

import com.hanainplan.domain.portfolio.entity.IrpHolding;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IrpPortfolioResponse {

    private Long customerId;
    private String irpAccountNumber;
    private BigDecimal totalValue;
    private BigDecimal totalReturn;
    private BigDecimal totalReturnRate;
    private LocalDateTime lastSyncedAt;
    
    private CashSleeve cash;
    private DepositSleeve deposit;
    private FundSleeve fund;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CashSleeve {
        private BigDecimal amount;
        private BigDecimal weight;
        private BigDecimal returnAmount;
        private BigDecimal returnRate;
        private LocalDateTime lastUpdated;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DepositSleeve {
        private BigDecimal amount;
        private BigDecimal weight;
        private BigDecimal returnAmount;
        private BigDecimal returnRate;
        private LocalDateTime lastUpdated;
        private List<DepositItem> items;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DepositItem {
        private String assetCode;
        private String assetName;
        private BigDecimal amount;
        private BigDecimal weight;
        private BigDecimal interestRate;
        private LocalDateTime maturityDate;
        private Long daysToMaturity;
        private BigDecimal returnAmount;
        private BigDecimal returnRate;
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FundSleeve {
        private BigDecimal amount;
        private BigDecimal weight;
        private BigDecimal returnAmount;
        private BigDecimal returnRate;
        private LocalDateTime lastUpdated;
        private List<FundItem> items;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FundItem {
        private String assetCode;
        private String assetName;
        private String classCode;
        private BigDecimal units;
        private BigDecimal currentNav;
        private BigDecimal purchaseNav;
        private BigDecimal amount;
        private BigDecimal weight;
        private BigDecimal returnAmount;
        private BigDecimal returnRate;
        private String riskLevel;
        private String fundType;
        private String status;
    }

    public static IrpPortfolioResponse from(List<IrpHolding> holdings, Long customerId, String irpAccountNumber) {
        if (holdings == null || holdings.isEmpty()) {
            return IrpPortfolioResponse.builder()
                    .customerId(customerId)
                    .irpAccountNumber(irpAccountNumber)
                    .totalValue(BigDecimal.ZERO)
                    .totalReturn(BigDecimal.ZERO)
                    .totalReturnRate(BigDecimal.ZERO)
                    .cash(CashSleeve.builder()
                            .amount(BigDecimal.ZERO)
                            .weight(BigDecimal.ZERO)
                            .returnAmount(BigDecimal.ZERO)
                            .returnRate(BigDecimal.ZERO)
                            .build())
                    .deposit(DepositSleeve.builder()
                            .amount(BigDecimal.ZERO)
                            .weight(BigDecimal.ZERO)
                            .returnAmount(BigDecimal.ZERO)
                            .returnRate(BigDecimal.ZERO)
                            .items(List.of())
                            .build())
                    .fund(FundSleeve.builder()
                            .amount(BigDecimal.ZERO)
                            .weight(BigDecimal.ZERO)
                            .returnAmount(BigDecimal.ZERO)
                            .returnRate(BigDecimal.ZERO)
                            .items(List.of())
                            .build())
                    .build();
        }

        // 총 자산 계산
        BigDecimal totalValue = holdings.stream()
                .map(IrpHolding::getCurrentValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalReturn = holdings.stream()
                .map(holding -> holding.getTotalReturn() != null ? holding.getTotalReturn() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalReturnRate = BigDecimal.ZERO;
        if (totalValue.compareTo(BigDecimal.ZERO) > 0) {
            totalReturnRate = totalReturn.divide(totalValue, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        // 슬리브별 분리
        List<IrpHolding> cashHoldings = holdings.stream()
                .filter(h -> IrpHolding.AssetType.CASH.equals(h.getAssetType()))
                .toList();

        List<IrpHolding> depositHoldings = holdings.stream()
                .filter(h -> IrpHolding.AssetType.DEPOSIT.equals(h.getAssetType()))
                .toList();

        List<IrpHolding> fundHoldings = holdings.stream()
                .filter(h -> IrpHolding.AssetType.FUND.equals(h.getAssetType()))
                .toList();

        // 현금 슬리브
        CashSleeve cashSleeve = buildCashSleeve(cashHoldings, totalValue);

        // 예금 슬리브
        DepositSleeve depositSleeve = buildDepositSleeve(depositHoldings, totalValue);

        // 펀드 슬리브
        FundSleeve fundSleeve = buildFundSleeve(fundHoldings, totalValue);

        // 마지막 동기화 시간
        LocalDateTime lastSyncedAt = holdings.stream()
                .map(IrpHolding::getLastSyncedAt)
                .filter(time -> time != null)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        return IrpPortfolioResponse.builder()
                .customerId(customerId)
                .irpAccountNumber(irpAccountNumber)
                .totalValue(totalValue)
                .totalReturn(totalReturn)
                .totalReturnRate(totalReturnRate)
                .lastSyncedAt(lastSyncedAt)
                .cash(cashSleeve)
                .deposit(depositSleeve)
                .fund(fundSleeve)
                .build();
    }

    private static CashSleeve buildCashSleeve(List<IrpHolding> cashHoldings, BigDecimal totalValue) {
        if (cashHoldings.isEmpty()) {
            return CashSleeve.builder()
                    .amount(BigDecimal.ZERO)
                    .weight(BigDecimal.ZERO)
                    .returnAmount(BigDecimal.ZERO)
                    .returnRate(BigDecimal.ZERO)
                    .build();
        }

        BigDecimal cashAmount = cashHoldings.stream()
                .map(IrpHolding::getCurrentValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal cashWeight = BigDecimal.ZERO;
        if (totalValue.compareTo(BigDecimal.ZERO) > 0) {
            cashWeight = cashAmount.divide(totalValue, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        return CashSleeve.builder()
                .amount(cashAmount)
                .weight(cashWeight)
                .returnAmount(BigDecimal.ZERO) // 현금은 수익 없음
                .returnRate(BigDecimal.ZERO)
                .lastUpdated(cashHoldings.get(0).getLastSyncedAt())
                .build();
    }

    private static DepositSleeve buildDepositSleeve(List<IrpHolding> depositHoldings, BigDecimal totalValue) {
        if (depositHoldings.isEmpty()) {
            return DepositSleeve.builder()
                    .amount(BigDecimal.ZERO)
                    .weight(BigDecimal.ZERO)
                    .returnAmount(BigDecimal.ZERO)
                    .returnRate(BigDecimal.ZERO)
                    .items(List.of())
                    .build();
        }

        BigDecimal depositAmount = depositHoldings.stream()
                .map(IrpHolding::getCurrentValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal depositWeight = BigDecimal.ZERO;
        if (totalValue.compareTo(BigDecimal.ZERO) > 0) {
            depositWeight = depositAmount.divide(totalValue, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        BigDecimal depositReturn = depositHoldings.stream()
                .map(holding -> holding.getTotalReturn() != null ? holding.getTotalReturn() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal depositReturnRate = BigDecimal.ZERO;
        if (depositAmount.compareTo(BigDecimal.ZERO) > 0) {
            depositReturnRate = depositReturn.divide(depositAmount, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        List<DepositItem> depositItems = depositHoldings.stream()
                .map(holding -> DepositItem.builder()
                        .assetCode(holding.getAssetCode())
                        .assetName(holding.getAssetName())
                        .amount(holding.getCurrentValue())
                        .weight(holding.getCurrentValue().divide(totalValue, 4, BigDecimal.ROUND_HALF_UP)
                                .multiply(BigDecimal.valueOf(100)))
                        .interestRate(holding.getInterestRate())
                        .maturityDate(holding.getMaturityDate())
                        .daysToMaturity(calculateDaysToMaturity(holding.getMaturityDate()))
                        .returnAmount(holding.getTotalReturn() != null ? holding.getTotalReturn() : BigDecimal.ZERO)
                        .returnRate(holding.getReturnRate() != null ? holding.getReturnRate() : BigDecimal.ZERO)
                        .status(holding.getStatus())
                        .build())
                .toList();

        return DepositSleeve.builder()
                .amount(depositAmount)
                .weight(depositWeight)
                .returnAmount(depositReturn)
                .returnRate(depositReturnRate)
                .lastUpdated(depositHoldings.get(0).getLastSyncedAt())
                .items(depositItems)
                .build();
    }

    private static FundSleeve buildFundSleeve(List<IrpHolding> fundHoldings, BigDecimal totalValue) {
        if (fundHoldings.isEmpty()) {
            return FundSleeve.builder()
                    .amount(BigDecimal.ZERO)
                    .weight(BigDecimal.ZERO)
                    .returnAmount(BigDecimal.ZERO)
                    .returnRate(BigDecimal.ZERO)
                    .items(List.of())
                    .build();
        }

        BigDecimal fundAmount = fundHoldings.stream()
                .map(IrpHolding::getCurrentValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal fundWeight = BigDecimal.ZERO;
        if (totalValue.compareTo(BigDecimal.ZERO) > 0) {
            fundWeight = fundAmount.divide(totalValue, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        BigDecimal fundReturn = fundHoldings.stream()
                .map(holding -> holding.getTotalReturn() != null ? holding.getTotalReturn() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal fundReturnRate = BigDecimal.ZERO;
        if (fundAmount.compareTo(BigDecimal.ZERO) > 0) {
            fundReturnRate = fundReturn.divide(fundAmount, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        List<FundItem> fundItems = fundHoldings.stream()
                .map(holding -> FundItem.builder()
                        .assetCode(holding.getAssetCode())
                        .assetName(holding.getAssetName())
                        .classCode(null) // TODO: 실제 펀드 정보에서 가져오기
                        .units(holding.getUnits())
                        .currentNav(holding.getCurrentNav())
                        .purchaseNav(holding.getPurchaseNav())
                        .amount(holding.getCurrentValue())
                        .weight(holding.getCurrentValue().divide(totalValue, 4, BigDecimal.ROUND_HALF_UP)
                                .multiply(BigDecimal.valueOf(100)))
                        .returnAmount(holding.getTotalReturn() != null ? holding.getTotalReturn() : BigDecimal.ZERO)
                        .returnRate(holding.getReturnRate() != null ? holding.getReturnRate() : BigDecimal.ZERO)
                        .riskLevel("3") // TODO: 실제 펀드 정보에서 가져오기
                        .fundType("주식형") // TODO: 실제 펀드 정보에서 가져오기
                        .status(holding.getStatus())
                        .build())
                .toList();

        return FundSleeve.builder()
                .amount(fundAmount)
                .weight(fundWeight)
                .returnAmount(fundReturn)
                .returnRate(fundReturnRate)
                .lastUpdated(fundHoldings.get(0).getLastSyncedAt())
                .items(fundItems)
                .build();
    }

    private static Long calculateDaysToMaturity(LocalDateTime maturityDate) {
        if (maturityDate == null) {
            return null;
        }
        return java.time.Duration.between(LocalDateTime.now(), maturityDate).toDays();
    }
}
