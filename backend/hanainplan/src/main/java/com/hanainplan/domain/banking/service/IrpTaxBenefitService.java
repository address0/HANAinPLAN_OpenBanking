package com.hanainplan.domain.banking.service;

import com.hanainplan.domain.banking.dto.AccountComparisonResult;
import com.hanainplan.domain.banking.dto.TaxBenefitResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Slf4j
public class IrpTaxBenefitService {

    private static final BigDecimal DEDUCTION_LIMIT = new BigDecimal("7000000");
    private static final BigDecimal HIGH_DEDUCTION_RATE = new BigDecimal("0.165");
    private static final BigDecimal LOW_DEDUCTION_RATE = new BigDecimal("0.132");
    private static final BigDecimal SALARY_THRESHOLD = new BigDecimal("55000000");
    
    private static final BigDecimal REGULAR_TAX_RATE = new BigDecimal("0.154");
    private static final BigDecimal PENSION_TAX_RATE = new BigDecimal("0.055");

    public TaxBenefitResult calculateTaxBenefit(BigDecimal annualDeposit, BigDecimal annualSalary) {
        log.info("IRP 세액공제 계산 - 납입액: {}, 연봉: {}", annualDeposit, annualSalary);

        BigDecimal deductibleAmount = annualDeposit.min(DEDUCTION_LIMIT);

        BigDecimal deductionRate;
        String salaryBracket;
        
        if (annualSalary.compareTo(SALARY_THRESHOLD) <= 0) {
            deductionRate = HIGH_DEDUCTION_RATE;
            salaryBracket = "5,500만원 이하";
        } else {
            deductionRate = LOW_DEDUCTION_RATE;
            salaryBracket = "5,500만원 초과";
        }

        BigDecimal taxDeduction = deductibleAmount
                .multiply(deductionRate)
                .setScale(0, RoundingMode.DOWN);

        log.info("세액공제 계산 완료 - 공제 대상: {}, 공제율: {}%, 공제액: {}",
                deductibleAmount, deductionRate.multiply(BigDecimal.valueOf(100)), taxDeduction);

        return TaxBenefitResult.builder()
                .depositAmount(annualDeposit)
                .deductibleAmount(deductibleAmount)
                .deductionRate(deductionRate)
                .taxDeduction(taxDeduction)
                .effectiveSavings(taxDeduction)
                .salaryBracket(salaryBracket)
                .build();
    }

    public AccountComparisonResult compareAccounts(
            BigDecimal investmentAmount,
            BigDecimal expectedReturn,
            BigDecimal annualSalary) {
        
        log.info("일반계좌 vs IRP 비교 - 투자금: {}, 예상수익: {}, 연봉: {}",
                investmentAmount, expectedReturn, annualSalary);

        BigDecimal regularTax = expectedReturn
                .multiply(REGULAR_TAX_RATE)
                .setScale(0, RoundingMode.DOWN);
        BigDecimal regularNetReturn = expectedReturn.subtract(regularTax);

        TaxBenefitResult irpBenefit = calculateTaxBenefit(investmentAmount, annualSalary);
        
        BigDecimal pensionTax = expectedReturn
                .multiply(PENSION_TAX_RATE)
                .setScale(0, RoundingMode.DOWN);
        BigDecimal irpNetReturn = expectedReturn.subtract(pensionTax);

        BigDecimal taxSavingFromLowerRate = regularTax.subtract(pensionTax);
        BigDecimal totalIrpBenefit = irpBenefit.getTaxDeduction().add(taxSavingFromLowerRate);
        
        BigDecimal totalRegularNet = regularNetReturn;
        BigDecimal totalIrpNet = irpNetReturn.add(irpBenefit.getTaxDeduction());
        BigDecimal advantageAmount = totalIrpNet.subtract(totalRegularNet);
        
        BigDecimal advantageRate = BigDecimal.ZERO;
        if (investmentAmount.compareTo(BigDecimal.ZERO) > 0) {
            advantageRate = advantageAmount
                    .divide(investmentAmount, 4, RoundingMode.HALF_UP);
        }

        log.info("비교 결과 - 일반계좌 순이익: {}, IRP 순이익: {}, IRP 우위: {}",
                totalRegularNet, totalIrpNet, advantageAmount);

        return AccountComparisonResult.builder()
                .investmentAmount(investmentAmount)
                .expectedReturn(expectedReturn)
                .regularAccountTax(regularTax)
                .regularAccountNetReturn(regularNetReturn)
                .irpTaxDeduction(irpBenefit.getTaxDeduction())
                .irpPensionTax(pensionTax)
                .irpNetReturn(irpNetReturn)
                .totalIrpBenefit(totalIrpBenefit)
                .advantageAmount(advantageAmount)
                .advantageRate(advantageRate)
                .build();
    }
}

