package com.hanainplan.domain.fund.service;

import com.hanainplan.domain.fund.dto.FundPortfolioDto;
import com.hanainplan.domain.fund.entity.FundPortfolio;
import com.hanainplan.domain.fund.repository.FundPortfolioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FundPortfolioService {

    private final FundPortfolioRepository fundPortfolioRepository;

    public List<FundPortfolioDto> getActivePortfoliosByUserId(Long userId) {
        log.info("사용자 활성 펀드 포트폴리오 조회 - 사용자 ID: {}", userId);

        List<FundPortfolio> portfolios = fundPortfolioRepository.findActivePortfoliosByUserId(userId);

        log.info("활성 포트폴리오 조회 완료 - {}건", portfolios.size());

        return portfolios.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<FundPortfolioDto> getAllPortfoliosByUserId(Long userId) {
        log.info("사용자 전체 펀드 포트폴리오 조회 - 사용자 ID: {}", userId);

        List<FundPortfolio> portfolios = fundPortfolioRepository.findByUserIdOrderByCreatedAtDesc(userId);

        log.info("전체 포트폴리오 조회 완료 - {}건", portfolios.size());

        return portfolios.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private FundPortfolioDto toDto(FundPortfolio portfolio) {
        return FundPortfolioDto.builder()
                .portfolioId(portfolio.getPortfolioId())
                .userId(portfolio.getUserId())
                .customerCi(portfolio.getCustomerCi())
                .bankCode(portfolio.getBankCode())
                .bankName(portfolio.getBankName())
                .fundCode(portfolio.getFundCode())
                .childFundCd(portfolio.getChildFundCd())
                .fundName(portfolio.getFundName())
                .classCode(portfolio.getClassCode())
                .fundType(portfolio.getFundType())
                .riskLevel(portfolio.getRiskLevel())
                .purchaseDate(portfolio.getPurchaseDate())
                .purchaseNav(portfolio.getPurchaseNav())
                .purchaseAmount(portfolio.getPurchaseAmount())
                .purchaseFee(portfolio.getPurchaseFee())
                .purchaseUnits(portfolio.getPurchaseUnits())
                .currentUnits(portfolio.getCurrentUnits())
                .currentNav(portfolio.getCurrentNav())
                .currentValue(portfolio.getCurrentValue())
                .totalReturn(portfolio.getTotalReturn())
                .returnRate(portfolio.getReturnRate())
                .accumulatedFees(portfolio.getAccumulatedFees())
                .irpAccountNumber(portfolio.getIrpAccountNumber())
                .subscriptionId(portfolio.getSubscriptionId())
                .status(portfolio.getStatus())
                .createdAt(portfolio.getCreatedAt())
                .updatedAt(portfolio.getUpdatedAt())
                .build();
    }
}