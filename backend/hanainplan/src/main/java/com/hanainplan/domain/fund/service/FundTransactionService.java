package com.hanainplan.domain.fund.service;

import com.hanainplan.domain.fund.dto.FundTransactionDto;
import com.hanainplan.domain.fund.dto.FundTransactionStatsDto;
import com.hanainplan.domain.fund.entity.FundTransaction;
import com.hanainplan.domain.fund.repository.FundTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FundTransactionService {

    private final FundTransactionRepository fundTransactionRepository;

    public List<FundTransactionDto> getUserTransactions(Long userId) {
        log.info("사용자 거래 내역 조회 - userId: {}", userId);

        List<FundTransaction> transactions = fundTransactionRepository.findByUserIdOrderByTransactionDateDesc(userId);

        log.info("거래 내역 조회 완료 - {}건", transactions.size());

        return transactions.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<FundTransactionDto> getCustomerTransactions(String customerCi) {
        log.info("고객 거래 내역 조회 - customerCi: {}", customerCi);

        return List.of();
    }

    public List<FundTransactionDto> getSubscriptionTransactions(Long subscriptionId) {
        log.info("가입 거래 내역 조회 - subscriptionId: {}", subscriptionId);

        List<FundTransaction> transactions = fundTransactionRepository.findByPortfolioIdOrderByTransactionDateDesc(subscriptionId);

        log.info("거래 내역 조회 완료 - {}건", transactions.size());

        return transactions.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public FundTransactionStatsDto getUserTransactionStats(Long userId) {
        log.info("사용자 거래 통계 조회 - userId: {}", userId);

        List<FundTransaction> transactions = fundTransactionRepository.findByUserIdOrderByTransactionDateDesc(userId);

        return calculateStats(transactions);
    }

    public FundTransactionStatsDto getTransactionStats(String customerCi) {
        log.info("고객 거래 통계 조회 - customerCi: {}", customerCi);

        return FundTransactionStatsDto.builder()
                .totalTransactionCount(0)
                .totalPurchaseCount(0)
                .totalRedemptionCount(0)
                .totalPurchaseAmount(BigDecimal.ZERO)
                .totalRedemptionAmount(BigDecimal.ZERO)
                .totalPurchaseFee(BigDecimal.ZERO)
                .totalRedemptionFee(BigDecimal.ZERO)
                .totalFees(BigDecimal.ZERO)
                .totalRealizedProfit(BigDecimal.ZERO)
                .netCashFlow(BigDecimal.ZERO)
                .build();
    }

    private FundTransactionStatsDto calculateStats(List<FundTransaction> transactions) {
        int totalCount = transactions.size();

        List<FundTransaction> buyTransactions = transactions.stream()
                .filter(FundTransaction::isBuyTransaction)
                .toList();

        List<FundTransaction> sellTransactions = transactions.stream()
                .filter(FundTransaction::isSellTransaction)
                .toList();

        BigDecimal totalPurchaseAmount = buyTransactions.stream()
                .map(FundTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRedemptionAmount = sellTransactions.stream()
                .map(FundTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPurchaseFee = buyTransactions.stream()
                .map(FundTransaction::getFee)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRedemptionFee = sellTransactions.stream()
                .map(FundTransaction::getFee)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalFees = totalPurchaseFee.add(totalRedemptionFee);

        BigDecimal totalRealizedProfit = BigDecimal.ZERO;

        BigDecimal netCashFlow = totalRedemptionAmount.subtract(totalPurchaseAmount).subtract(totalFees);

        return FundTransactionStatsDto.builder()
                .totalTransactionCount(totalCount)
                .totalPurchaseCount(buyTransactions.size())
                .totalRedemptionCount(sellTransactions.size())
                .totalPurchaseAmount(totalPurchaseAmount)
                .totalRedemptionAmount(totalRedemptionAmount)
                .totalPurchaseFee(totalPurchaseFee)
                .totalRedemptionFee(totalRedemptionFee)
                .totalFees(totalFees)
                .totalRealizedProfit(totalRealizedProfit)
                .netCashFlow(netCashFlow)
                .build();
    }

    private FundTransactionDto toDto(FundTransaction transaction) {
        String transactionTypeName = "BUY".equals(transaction.getTransactionType()) ? "매수" : 
                                     "SELL".equals(transaction.getTransactionType()) ? "매도" : "알수없음";

        return FundTransactionDto.builder()
                .transactionId(transaction.getTransactionId())
                .portfolioId(transaction.getPortfolioId())
                .userId(transaction.getUserId())
                .subscriptionId(transaction.getPortfolioId())
                .fundCode(transaction.getFundCode())
                .childFundCd(transaction.getFundCode())
                .transactionType(transaction.getTransactionType())
                .transactionTypeName(transactionTypeName)
                .transactionDate(transaction.getTransactionDate().toLocalDate())
                .settlementDate(transaction.getSettlementDate())
                .nav(transaction.getNav())
                .units(transaction.getUnits())
                .amount(transaction.getAmount())
                .fee(transaction.getFee())
                .irpAccountNumber(transaction.getIrpAccountNumber())
                .description(transaction.getDescription())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}