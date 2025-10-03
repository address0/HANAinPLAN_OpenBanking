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

/**
 * 정기예금 포트폴리오 조회 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DepositPortfolioService {

    private final DepositPortfolioRepository depositPortfolioRepository;

    /**
     * 사용자의 모든 정기예금 포트폴리오 조회
     */
    public List<DepositPortfolioDto> getUserPortfolio(Long userId) {
        log.info("사용자 포트폴리오 조회 - 사용자 ID: {}", userId);
        
        List<DepositPortfolio> portfolios = depositPortfolioRepository.findByUserIdOrderBySubscriptionDateDesc(userId);
        
        return portfolios.stream()
                .map(DepositPortfolioDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 사용자의 활성 정기예금 포트폴리오만 조회
     */
    public List<DepositPortfolioDto> getActivePortfolio(Long userId) {
        log.info("사용자 활성 포트폴리오 조회 - 사용자 ID: {}", userId);
        
        List<DepositPortfolio> portfolios = depositPortfolioRepository.findByUserIdAndStatus(userId, "ACTIVE");
        
        return portfolios.stream()
                .map(DepositPortfolioDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 포트폴리오 상세 조회
     */
    public DepositPortfolioDto getPortfolioDetail(Long portfolioId) {
        log.info("포트폴리오 상세 조회 - 포트폴리오 ID: {}", portfolioId);
        
        DepositPortfolio portfolio = depositPortfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new RuntimeException("포트폴리오를 찾을 수 없습니다: " + portfolioId));
        
        return DepositPortfolioDto.from(portfolio);
    }

    /**
     * IRP 계좌번호로 포트폴리오 조회 (IRP 내 가입 상품 전체)
     */
    public List<DepositPortfolioDto> getPortfolioByIrpAccount(String irpAccountNumber) {
        log.info("IRP 계좌 포트폴리오 조회 - IRP 계좌번호: {}", irpAccountNumber);
        
        List<DepositPortfolio> portfolios = depositPortfolioRepository
                .findByIrpAccountNumberOrderBySubscriptionDateDesc(irpAccountNumber);
        
        return portfolios.stream()
                .map(DepositPortfolioDto::from)
                .collect(Collectors.toList());
    }

    /**
     * IRP 계좌번호로 활성 포트폴리오만 조회
     */
    public List<DepositPortfolioDto> getActivePortfolioByIrpAccount(String irpAccountNumber) {
        log.info("IRP 계좌 활성 포트폴리오 조회 - IRP 계좌번호: {}", irpAccountNumber);
        
        List<DepositPortfolio> portfolios = depositPortfolioRepository
                .findByIrpAccountNumberAndStatus(irpAccountNumber, "ACTIVE");
        
        return portfolios.stream()
                .map(DepositPortfolioDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 사용자 포트폴리오 요약 정보
     */
    public Map<String, Object> getPortfolioSummary(Long userId) {
        log.info("사용자 포트폴리오 요약 조회 - 사용자 ID: {}", userId);
        
        List<DepositPortfolio> allPortfolios = depositPortfolioRepository.findByUserIdOrderBySubscriptionDateDesc(userId);
        List<DepositPortfolio> activePortfolios = allPortfolios.stream()
                .filter(p -> "ACTIVE".equals(p.getStatus()))
                .collect(Collectors.toList());
        
        // 활성 정기예금 총 원금
        BigDecimal totalPrincipal = activePortfolios.stream()
                .map(DepositPortfolio::getPrincipalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 예상 총 이자
        BigDecimal totalExpectedInterest = activePortfolios.stream()
                .map(DepositPortfolio::getExpectedInterest)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 예상 만기 총액
        BigDecimal totalMaturityAmount = activePortfolios.stream()
                .map(DepositPortfolio::getMaturityAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 은행별 분류
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

