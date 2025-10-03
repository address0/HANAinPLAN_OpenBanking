package com.hanainplan.domain.banking.service;

import com.hanainplan.domain.banking.dto.DepositProductResponse;
import com.hanainplan.domain.banking.dto.InterestRateDto;
import com.hanainplan.domain.banking.dto.OptimalDepositRecommendation;
import com.hanainplan.domain.banking.entity.DepositProduct;
import com.hanainplan.domain.banking.entity.IrpAccount;
import com.hanainplan.domain.banking.repository.DepositProductRepository;
import com.hanainplan.domain.banking.repository.IrpAccountRepository;
import com.hanainplan.domain.banking.util.InterestRateCalculator;
import com.hanainplan.domain.user.entity.User;
import com.hanainplan.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 예금 상품 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DepositProductService {

    private final DepositProductRepository depositProductRepository;
    private final IrpAccountRepository irpAccountRepository;
    private final UserRepository userRepository;
    private final InterestRateService interestRateService;

    /**
     * 모든 예금 상품 조회
     */
    @Transactional(readOnly = true)
    public List<DepositProductResponse> getAllDepositProducts() {
        log.info("모든 예금 상품 조회");
        
        List<DepositProduct> products = depositProductRepository.findAll();
        
        return products.stream()
                .map(DepositProductResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 은행별 예금 상품 조회
     */
    @Transactional(readOnly = true)
    public List<DepositProductResponse> getDepositProductsByBank(String bankCode) {
        log.info("은행별 예금 상품 조회: bankCode={}", bankCode);
        
        List<DepositProduct> products = depositProductRepository.findByBankCode(bankCode);
        
        return products.stream()
                .map(DepositProductResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 사용자 정기예금 추천 (단순화 버전)
     * - 사용자가 원하는 예치 금액으로 은퇴 시점에 맞는 최적 상품 추천
     */
    @Transactional(readOnly = true)
    public OptimalDepositRecommendation recommendOptimalDeposit(Long userId, LocalDate retirementDate, BigDecimal depositAmount) {
        log.info("정기예금 상품 추천 시작: userId={}, retirementDate={}, depositAmount={}원", userId, retirementDate, depositAmount);

        // 1. 사용자 정보 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));

        // 2. IRP 계좌 조회
        IrpAccount irpAccount = irpAccountRepository.findByCustomerIdAndAccountStatus(userId, "ACTIVE")
                .orElseThrow(() -> new RuntimeException("활성화된 IRP 계좌가 없습니다. IRP 계좌를 먼저 개설해주세요."));

        // 3. IRP 잔액 확인
        BigDecimal currentBalance = irpAccount.getCurrentBalance();
        
        // 예치 금액이 IRP 잔액을 초과하는지 확인
        if (depositAmount.compareTo(currentBalance) > 0) {
            throw new RuntimeException(String.format("예치 희망 금액(%s원)이 IRP 계좌 잔액(%s원)을 초과합니다.", 
                    depositAmount, currentBalance));
        }

        // 4. 은퇴까지 남은 기간 계산
        Period period = Period.between(LocalDate.now(), retirementDate);
        int yearsToRetirement = period.getYears();
        int monthsToRetirement = yearsToRetirement * 12 + period.getMonths();

        log.info("은퇴까지: {}년 {}개월, 현재 IRP 잔액: {}원, 예치 희망 금액: {}원", 
                yearsToRetirement, period.getMonths(), currentBalance, depositAmount);

        // 5. 최소 예치 금액 확인 (100만원)
        BigDecimal minAmount = BigDecimal.valueOf(1_000_000);
        if (depositAmount.compareTo(minAmount) < 0) {
            throw new RuntimeException(String.format("최소 예치 금액은 %s원입니다.", minAmount));
        }

        // 6. 실시간 금리 정보 조회
        List<InterestRateDto> allRates = interestRateService.getAllInterestRates();
        log.info("전체 {}개 은행의 금리 정보 조회 완료", allRates.size());

        // 7. 최적 상품 유형 및 기간 결정 (은퇴 시점에 맞춤)
        String targetMaturityPeriod;
        String recommendationReason;
        int contractPeriod;
        boolean isDayUnit = false; // 일 단위 상품 여부

        if (yearsToRetirement >= 5) {
            // 5년 이상: 5년 상품 추천
            targetMaturityPeriod = "5년";
            contractPeriod = 60;
            recommendationReason = String.format("은퇴까지 %d년이 남아, 5년 정기예금을 추천드립니다. " +
                    "장기 투자로 안정적이고 높은 수익을 기대할 수 있습니다.", yearsToRetirement);
        } else if (yearsToRetirement >= 3) {
            // 3~5년: 3년 상품 추천
            targetMaturityPeriod = "3년";
            contractPeriod = 36;
            recommendationReason = String.format("은퇴까지 %d년이 남아, 3년 정기예금을 추천드립니다. " +
                    "은퇴 시점에 맞춰 만기가 도래하여 자금 운용이 용이합니다.", yearsToRetirement);
        } else if (yearsToRetirement >= 2) {
            // 2~3년: 2년 상품 추천
            targetMaturityPeriod = "2년";
            contractPeriod = 24;
            recommendationReason = String.format("은퇴까지 %d년이 남아, 2년 정기예금을 추천드립니다. " +
                    "은퇴 시점에 맞춰 만기가 도래하여 자금 운용이 용이합니다.", yearsToRetirement);
        } else if (yearsToRetirement >= 1) {
            // 1~2년: 1년 상품 추천
            targetMaturityPeriod = "1년";
            contractPeriod = 12;
            recommendationReason = String.format("은퇴까지 %d년이 남아, 1년 정기예금을 추천드립니다. " +
                    "은퇴 시점에 맞춰 만기가 도래하여 자금 운용이 용이합니다.", yearsToRetirement);
        } else if (monthsToRetirement >= 6) {
            // 6개월~1년: 6개월 상품
            targetMaturityPeriod = "6개월";
            contractPeriod = 6;
            recommendationReason = "은퇴까지 6개월 이상 1년 미만이 남아, 6개월 정기예금을 추천드립니다. " +
                    "짧은 기간에 맞춰 유연하게 자금을 운용할 수 있습니다.";
        } else {
            // 6개월 미만: 일 단위 상품 (90일 또는 180일)
            isDayUnit = true;
            int daysToRetirement = Period.between(LocalDate.now(), retirementDate).getDays() + 
                                   monthsToRetirement * 30; // 대략적인 일수 계산
            
            if (daysToRetirement >= 120) {
                // 120일 이상: 180일 상품
                targetMaturityPeriod = "180일";
                contractPeriod = 6; // 개월 단위로는 6개월로 계산
                recommendationReason = String.format("은퇴까지 약 %d개월(%d일)이 남아, 180일 정기예금을 추천드립니다. " +
                        "은퇴 시점에 맞춰 만기가 도래하여 자금을 받을 수 있습니다.", monthsToRetirement, daysToRetirement);
            } else {
                // 120일 미만: 90일 상품
                targetMaturityPeriod = "90일";
                contractPeriod = 3; // 개월 단위로는 3개월로 계산
                recommendationReason = String.format("은퇴까지 약 %d개월(%d일)이 남아, 90일 정기예금을 추천드립니다. " +
                        "은퇴 시점에 맞춰 만기가 도래하여 자금을 받을 수 있습니다.", monthsToRetirement, daysToRetirement);
            }
        }

        // 8. 해당 기간의 금리 필터링 (BASIC 금리만)
        List<InterestRateDto> candidateRates = allRates.stream()
                .filter(rate -> "BASIC".equals(rate.getInterestType()))
                .filter(rate -> targetMaturityPeriod.equals(rate.getMaturityPeriod()))
                .sorted(Comparator.comparing(InterestRateDto::getInterestRate).reversed())
                .collect(Collectors.toList());

        if (candidateRates.isEmpty()) {
            throw new RuntimeException("추천 가능한 금리 정보가 없습니다. 은행 서버를 확인해주세요.");
        }

        // 9. 최고 금리 상품 선택
        InterestRateDto bestRate = candidateRates.get(0);
        log.info("최적 금리 선택 - 은행: {}, 기간: {}, 금리: {}%", 
                bestRate.getBankName(), bestRate.getMaturityPeriod(), bestRate.getInterestRate().multiply(BigDecimal.valueOf(100)));

        // 10. 예상 수익 계산 (사용자 입력 금액 기준)
        BigDecimal expectedInterest = calculateInterest(depositAmount, bestRate.getInterestRate(), contractPeriod);
        BigDecimal expectedMaturityAmount = depositAmount.add(expectedInterest);
        LocalDate expectedMaturityDate = LocalDate.now().plusMonths(contractPeriod);

        // 11. 대안 상품 TOP 3 구성 (최고 금리 제외)
        List<OptimalDepositRecommendation.AlternativeDepositOption> alternatives = new ArrayList<>();
        for (int i = 1; i < Math.min(candidateRates.size(), 4); i++) {
            InterestRateDto rate = candidateRates.get(i);
            BigDecimal altInterest = calculateInterest(depositAmount, rate.getInterestRate(), contractPeriod);
            BigDecimal altMaturityAmount = depositAmount.add(altInterest);
            
            String reason = String.format("%s은행의 %s 상품으로, 연 %.2f%% 금리를 제공합니다.", 
                    rate.getBankName(), rate.getMaturityPeriod(), rate.getInterestRate().multiply(BigDecimal.valueOf(100)));
            
            alternatives.add(OptimalDepositRecommendation.AlternativeDepositOption.builder()
                    .bankCode(rate.getBankCode())
                    .bankName(rate.getBankName())
                    .maturityPeriod(rate.getMaturityPeriod())
                    .interestRate(rate.getInterestRate())
                    .expectedInterest(altInterest)
                    .expectedMaturityAmount(altMaturityAmount)
                    .reason(reason)
                    .build());
        }

        // 12. 최종 추천 결과 구성
        int productType = isDayUnit ? 2 : 0; // 일 단위: 2, 일반: 0
        String productTypeName = isDayUnit ? "일단위 정기예금" : "일반 정기예금";
        String periodUnit = isDayUnit ? "일" : "개월";
        
        OptimalDepositRecommendation recommendation = OptimalDepositRecommendation.builder()
                .depositCode(bestRate.getProductCode())
                .depositName(bestRate.getProductName())
                .bankCode(bestRate.getBankCode())
                .bankName(bestRate.getBankName())
                .productType(productType)
                .productTypeName(productTypeName)
                .contractPeriod(contractPeriod)
                .contractPeriodUnit(periodUnit)
                .maturityPeriod(targetMaturityPeriod)
                .depositAmount(depositAmount) // 사용자 입력 금액 그대로 사용
                .appliedRate(bestRate.getInterestRate())
                .expectedInterest(expectedInterest)
                .expectedMaturityAmount(expectedMaturityAmount)
                .expectedMaturityDate(expectedMaturityDate)
                .recommendationReason(recommendationReason + String.format(" %s은행이 연 %.2f%%로 가장 높은 금리를 제공합니다.", 
                        bestRate.getBankName(), bestRate.getInterestRate().multiply(BigDecimal.valueOf(100))))
                .yearsToRetirement(yearsToRetirement)
                .currentIrpBalance(currentBalance)
                .alternativeOptions(alternatives)
                .build();

        log.info("정기예금 상품 추천 완료 - 은행: {}, 기간: {}, 금리: {}%, 예치금액: {}원, 만기예상액: {}원", 
                bestRate.getBankName(), targetMaturityPeriod, bestRate.getInterestRate().multiply(BigDecimal.valueOf(100)), 
                depositAmount, expectedMaturityAmount);

        return recommendation;
    }

    /**
     * 이자 계산 (단리)
     */
    private BigDecimal calculateInterest(BigDecimal principal, BigDecimal rate, int months) {
        return principal
                .multiply(rate)
                .multiply(BigDecimal.valueOf(months))
                .divide(BigDecimal.valueOf(12), 2, RoundingMode.DOWN);
    }

    private String getProductTypeName(Integer productType) {
        switch (productType) {
            case 0: return "일반";
            case 1: return "디폴트옵션";
            case 2: return "일단위";
            default: return "알 수 없음";
        }
    }
}

