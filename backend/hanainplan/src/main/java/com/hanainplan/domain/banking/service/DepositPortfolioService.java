package com.hanainplan.domain.banking.service;

import com.hanainplan.domain.banking.dto.DepositPortfolioDto;
import com.hanainplan.domain.banking.entity.DepositPortfolio;
import com.hanainplan.domain.banking.repository.DepositPortfolioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DepositPortfolioService {

    private final DepositPortfolioRepository depositPortfolioRepository;

    public List<DepositPortfolioDto> getUserPortfolio(Long userId) {
        log.info("사용자 포트폴리오 조회 - 사용자 ID: {}", userId);

        List<DepositPortfolio> portfolios = depositPortfolioRepository.findByUserIdOrderBySubscriptionDateDesc(userId);

        return portfolios.stream()
                .map(DepositPortfolioDto::from)
                .collect(Collectors.toList());
    }

    public List<DepositPortfolioDto> getActivePortfolio(Long userId) {
        log.info("사용자 활성 포트폴리오 조회 - 사용자 ID: {}", userId);

        List<DepositPortfolio> portfolios = depositPortfolioRepository.findByUserIdAndStatus(userId, "ACTIVE");

        return portfolios.stream()
                .map(DepositPortfolioDto::from)
                .collect(Collectors.toList());
    }

    public DepositPortfolioDto getPortfolioDetail(Long portfolioId) {
        log.info("포트폴리오 상세 조회 - 포트폴리오 ID: {}", portfolioId);

        DepositPortfolio portfolio = depositPortfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new RuntimeException("포트폴리오를 찾을 수 없습니다: " + portfolioId));

        return DepositPortfolioDto.from(portfolio);
    }

    public List<DepositPortfolioDto> getPortfolioByIrpAccount(String irpAccountNumber) {
        log.info("IRP 계좌 포트폴리오 조회 - IRP 계좌번호: {}", irpAccountNumber);

        List<DepositPortfolio> portfolios = depositPortfolioRepository
                .findByIrpAccountNumberOrderBySubscriptionDateDesc(irpAccountNumber);

        return portfolios.stream()
                .map(DepositPortfolioDto::from)
                .collect(Collectors.toList());
    }

    public List<DepositPortfolioDto> getActivePortfolioByIrpAccount(String irpAccountNumber) {
        log.info("IRP 계좌 활성 포트폴리오 조회 - IRP 계좌번호: {}", irpAccountNumber);

        List<DepositPortfolio> portfolios = depositPortfolioRepository
                .findByIrpAccountNumberAndStatus(irpAccountNumber, "ACTIVE");

        return portfolios.stream()
                .map(DepositPortfolioDto::from)
                .collect(Collectors.toList());
    }

    public Map<String, Object> getPortfolioSummary(Long userId) {
        log.info("사용자 포트폴리오 요약 조회 - 사용자 ID: {}", userId);

        List<DepositPortfolio> allPortfolios = depositPortfolioRepository.findByUserIdOrderBySubscriptionDateDesc(userId);
        List<DepositPortfolio> activePortfolios = allPortfolios.stream()
                .filter(p -> "ACTIVE".equals(p.getStatus()))
                .collect(Collectors.toList());

        BigDecimal totalPrincipal = activePortfolios.stream()
                .map(DepositPortfolio::getPrincipalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpectedInterest = activePortfolios.stream()
                .map(DepositPortfolio::getExpectedInterest)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalMaturityAmount = activePortfolios.stream()
                .map(DepositPortfolio::getMaturityAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Long> byBank = activePortfolios.stream()
                .collect(Collectors.groupingBy(
                        DepositPortfolio::getBankName,
                        Collectors.counting()
                ));

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalCount", allPortfolios.size());
        summary.put("activeCount", activePortfolios.size());
        summary.put("totalPrincipal", totalPrincipal);
        summary.put("totalExpectedInterest", totalExpectedInterest);
        summary.put("totalMaturityAmount", totalMaturityAmount);
        summary.put("portfoliosByBank", byBank);

        return summary;
    }
}