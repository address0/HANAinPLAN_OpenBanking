package com.hanainplan.hana.fund.service;

import com.hanainplan.hana.fund.dto.FundTransactionDto;
import com.hanainplan.hana.fund.dto.FundTransactionStatsDto;
import com.hanainplan.hana.fund.entity.FundTransaction;
import com.hanainplan.hana.fund.repository.FundTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FundTransactionService {

    private final FundTransactionRepository fundTransactionRepository;

    public List<FundTransactionDto> getCustomerTransactions(String customerCi) {
        log.info("고객 거래 내역 조회 - customerCi: {}", customerCi);

        List<FundTransaction> transactions = fundTransactionRepository
                .findByCustomerCiOrderByTransactionDateDesc(customerCi);

        log.info("거래 내역 조회 완료 - {}건", transactions.size());

        return transactions.stream()
                .map(FundTransactionDto::from)
                .collect(Collectors.toList());
    }

    public List<FundTransactionDto> getSubscriptionTransactions(Long subscriptionId) {
        log.info("가입 거래 내역 조회 - subscriptionId: {}", subscriptionId);

        List<FundTransaction> transactions = fundTransactionRepository
                .findBySubscriptionIdOrderByTransactionDateDesc(subscriptionId);

        log.info("거래 내역 조회 완료 - {}건", transactions.size());

        return transactions.stream()
                .map(FundTransactionDto::from)
                .collect(Collectors.toList());
    }

    public List<FundTransactionDto> getTransactionsByDateRange(
            String customerCi, 
            LocalDate startDate, 
            LocalDate endDate) {

        log.info("기간별 거래 내역 조회 - customerCi: {}, period: {} ~ {}", 
                customerCi, startDate, endDate);

        List<FundTransaction> transactions = fundTransactionRepository
                .findByCustomerCiAndDateRange(customerCi, startDate, endDate);

        log.info("거래 내역 조회 완료 - {}건", transactions.size());

        return transactions.stream()
                .map(FundTransactionDto::from)
                .collect(Collectors.toList());
    }

    public List<FundTransactionDto> getTransactionsByType(String customerCi, String transactionType) {
        log.info("거래 유형별 조회 - customerCi: {}, type: {}", customerCi, transactionType);

        List<FundTransaction> transactions = fundTransactionRepository
                .findByCustomerCiAndTransactionType(customerCi, transactionType);

        log.info("거래 내역 조회 완료 - {}건", transactions.size());

        return transactions.stream()
                .map(FundTransactionDto::from)
                .collect(Collectors.toList());
    }

    public FundTransactionStatsDto getTransactionStats(String customerCi) {
        log.info("거래 통계 조회 - customerCi: {}", customerCi);

        List<FundTransaction> allTransactions = fundTransactionRepository
                .findByCustomerCiOrderByTransactionDateDesc(customerCi);

        List<FundTransaction> purchases = allTransactions.stream()
                .filter(t -> "BUY".equals(t.getTransactionType()))
                .collect(Collectors.toList());

        int purchaseCount = purchases.size();
        BigDecimal purchaseAmount = fundTransactionRepository.getTotalPurchaseAmount(customerCi);
        BigDecimal purchaseFee = purchases.stream()
                .map(FundTransaction::getFee)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<FundTransaction> redemptions = allTransactions.stream()
                .filter(t -> "SELL".equals(t.getTransactionType()))
                .collect(Collectors.toList());

        int redemptionCount = redemptions.size();
        BigDecimal redemptionAmount = fundTransactionRepository.getTotalRedemptionAmount(customerCi);
        BigDecimal redemptionFee = redemptions.stream()
                .map(FundTransaction::getFee)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal realizedProfit = fundTransactionRepository.getTotalRealizedProfit(customerCi);
        BigDecimal totalFees = fundTransactionRepository.getTotalFees(customerCi);

        BigDecimal netCashFlow = purchaseAmount.subtract(redemptionAmount);

        FundTransactionStatsDto stats = FundTransactionStatsDto.builder()
                .customerCi(customerCi)
                .totalPurchaseCount(purchaseCount)
                .totalPurchaseAmount(purchaseAmount)
                .totalPurchaseFee(purchaseFee)
                .totalRedemptionCount(redemptionCount)
                .totalRedemptionAmount(redemptionAmount)
                .totalRedemptionFee(redemptionFee)
                .totalRealizedProfit(realizedProfit)
                .totalFees(totalFees)
                .totalTransactionCount(allTransactions.size())
                .netCashFlow(netCashFlow)
                .build();

        log.info("거래 통계 조회 완료 - 총 {}건, 실현손익: {}원", 
                allTransactions.size(), realizedProfit);

        return stats;
    }
}