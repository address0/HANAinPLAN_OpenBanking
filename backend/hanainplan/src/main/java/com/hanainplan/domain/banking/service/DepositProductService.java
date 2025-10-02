package com.hanainplan.domain.banking.service;

import com.hanainplan.domain.banking.dto.DepositProductResponse;
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
import java.util.List;
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
     * 사용자 목표 기반 최적 예금 상품 추천
     */
    @Transactional(readOnly = true)
    public OptimalDepositRecommendation recommendOptimalDeposit(Long userId, LocalDate retirementDate, BigDecimal goalAmount) {
        log.info("최적 예금 상품 추천 시작: userId={}, retirementDate={}, goalAmount={}", userId, retirementDate, goalAmount);

        // 1. 사용자 정보 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));

        // 2. IRP 계좌 조회
        IrpAccount irpAccount = irpAccountRepository.findByCustomerIdAndAccountStatus(userId, "ACTIVE")
                .orElseThrow(() -> new RuntimeException("활성화된 IRP 계좌가 없습니다. IRP 계좌를 먼저 개설해주세요."));

        // 3. 목표 분석
        LocalDate birthDate = user.getBirthDate();
        BigDecimal currentBalance = irpAccount.getCurrentBalance();

        // 은퇴까지 남은 기간 계산
        Period period = Period.between(LocalDate.now(), retirementDate);
        int yearsToRetirement = period.getYears();
        int monthsToRetirement = yearsToRetirement * 12 + period.getMonths();

        log.info("목표 분석 - 은퇴까지: {}년 {}개월, 현재 잔액: {}원, 목표: {}원", 
                yearsToRetirement, period.getMonths(), currentBalance, goalAmount);

        // 4. 부족 금액 계산
        BigDecimal shortfall = goalAmount.subtract(currentBalance);
        
        if (shortfall.compareTo(BigDecimal.ZERO) <= 0) {
            log.info("이미 목표 금액을 달성했습니다");
            shortfall = BigDecimal.ZERO;
        }

        // 5. 최적 상품 유형 및 기간 결정
        int productType;
        int contractPeriod;
        String contractPeriodUnit;
        String recommendationReason;

        if (yearsToRetirement >= 3) {
            // 3년 이상: 디폴트옵션 추천
            productType = 1;
            contractPeriod = 36;
            contractPeriodUnit = "개월";
            recommendationReason = String.format("은퇴까지 %d년이 남아, 장기 투자에 유리한 디폴트옵션(3년)을 추천드립니다. " +
                    "만기 시 펀드 포트폴리오와 함께 자동 재예치되어 안정적인 자산 증식이 가능합니다.", yearsToRetirement);
        } else if (yearsToRetirement >= 1) {
            // 1~3년: 일반 상품 (은퇴까지의 기간에 맞춤)
            productType = 0;
            if (monthsToRetirement >= 24) {
                contractPeriod = 24;
            } else if (monthsToRetirement >= 12) {
                contractPeriod = 12;
            } else {
                contractPeriod = 6;
            }
            contractPeriodUnit = "개월";
            recommendationReason = String.format("은퇴까지 %d년이 남아, %d개월 일반 정기예금을 추천드립니다. " +
                    "은퇴 시점에 맞춰 만기가 도래하여 자금 운용이 용이합니다.", yearsToRetirement, contractPeriod);
        } else {
            // 1년 미만: 일단위 상품 (짧은 기간)
            productType = 2;
            int daysToRetirement = period.getDays() + (period.getMonths() * 30);
            if (daysToRetirement < 31) {
                daysToRetirement = 31; // 최소 31일
            }
            contractPeriod = Math.min(daysToRetirement, 365); // 최대 1년
            contractPeriodUnit = "일";
            recommendationReason = String.format("은퇴까지 %d일이 남아, 일단위 정기예금(%d일)을 추천드립니다. " +
                    "짧은 기간에 맞춰 유연하게 자금을 운용할 수 있습니다.", daysToRetirement, contractPeriod);
        }

        // 6. 추천 예치 금액 계산 (부족 금액의 50% 또는 현재 잔액의 30% 중 작은 값)
        BigDecimal recommendedAmount;
        if (shortfall.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal halfShortfall = shortfall.divide(BigDecimal.valueOf(2), 0, RoundingMode.DOWN);
            BigDecimal thirtyPercentBalance = currentBalance.multiply(BigDecimal.valueOf(0.3))
                    .setScale(0, RoundingMode.DOWN);
            
            recommendedAmount = halfShortfall.min(thirtyPercentBalance);
            
            // 최소 금액 보장 (100만원)
            BigDecimal minAmount = BigDecimal.valueOf(1_000_000);
            if (recommendedAmount.compareTo(minAmount) < 0) {
                recommendedAmount = minAmount;
            }
        } else {
            // 목표 달성 시 현재 잔액의 20%
            recommendedAmount = currentBalance.multiply(BigDecimal.valueOf(0.2))
                    .setScale(0, RoundingMode.DOWN);
        }

        // 7. 금리 및 예상 수익 계산
        BigDecimal appliedRate = InterestRateCalculator.getBaseRate(productType, contractPeriod);
        
        int months = productType == 2 
                ? (int) Math.ceil(contractPeriod / 30.0) 
                : contractPeriod;
        
        BigDecimal expectedInterest = InterestRateCalculator.calculateMaturityInterest(
                recommendedAmount, appliedRate, months);
        
        BigDecimal expectedMaturityAmount = recommendedAmount.add(expectedInterest);
        
        LocalDate expectedMaturityDate = InterestRateCalculator.calculateMaturityDate(
                LocalDate.now(), productType, contractPeriod);

        // 8. 하나은행 상품 조회 (현재는 하나은행만)
        DepositProduct hanaProduct = depositProductRepository.findByBankCode("HANA").stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("예금 상품을 찾을 수 없습니다"));

        // 9. 추천 결과 구성
        OptimalDepositRecommendation recommendation = OptimalDepositRecommendation.builder()
                .depositCode(hanaProduct.getDepositCode())
                .depositName(hanaProduct.getName())
                .bankCode(hanaProduct.getBankCode())
                .bankName(hanaProduct.getBankName())
                .productType(productType)
                .productTypeName(getProductTypeName(productType))
                .contractPeriod(contractPeriod)
                .contractPeriodUnit(contractPeriodUnit)
                .recommendedAmount(recommendedAmount)
                .appliedRate(appliedRate)
                .expectedInterest(expectedInterest)
                .expectedMaturityAmount(expectedMaturityAmount)
                .expectedMaturityDate(expectedMaturityDate)
                .recommendationReason(recommendationReason)
                .yearsToRetirement(yearsToRetirement)
                .currentIrpBalance(currentBalance)
                .targetAmount(goalAmount)
                .shortfall(shortfall)
                .build();

        log.info("최적 예금 상품 추천 완료 - 상품유형: {}, 기간: {}{}, 금액: {}원", 
                productType, contractPeriod, contractPeriodUnit, recommendedAmount);

        return recommendation;
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

