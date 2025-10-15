package com.hanainplan.domain.banking.service;

import com.hanainplan.domain.banking.dto.TaxResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;

@Service
@Slf4j
public class TaxCalculationService {

    private static final BigDecimal INTEREST_INCOME_TAX_RATE = new BigDecimal("0.14");
    private static final BigDecimal LOCAL_TAX_RATE = new BigDecimal("0.1");
    private static final BigDecimal DIVIDEND_INCOME_TAX_RATE = new BigDecimal("0.14");

    public TaxResult calculateInterestTax(BigDecimal interestAmount) {
        log.debug("이자소득세 계산 - 금액: {}", interestAmount);

        BigDecimal incomeTax = interestAmount
                .multiply(INTEREST_INCOME_TAX_RATE)
                .setScale(0, RoundingMode.DOWN);
        
        BigDecimal localTax = incomeTax
                .multiply(LOCAL_TAX_RATE)
                .setScale(0, RoundingMode.DOWN);
        
        BigDecimal totalTax = incomeTax.add(localTax);
        BigDecimal netAmount = interestAmount.subtract(totalTax);

        log.debug("세금 계산 결과 - 세전: {}, 소득세: {}, 지방세: {}, 세후: {}",
                interestAmount, incomeTax, localTax, netAmount);

        return TaxResult.builder()
                .grossAmount(interestAmount)
                .incomeTax(incomeTax)
                .localTax(localTax)
                .totalTax(totalTax)
                .netAmount(netAmount)
                .taxRate(INTEREST_INCOME_TAX_RATE.add(LOCAL_TAX_RATE))
                .pensionTaxApplied(false)
                .taxType("INTEREST")
                .build();
    }

    public TaxResult calculateDividendTax(BigDecimal dividendAmount) {
        log.debug("배당소득세 계산 - 금액: {}", dividendAmount);

        BigDecimal incomeTax = dividendAmount
                .multiply(DIVIDEND_INCOME_TAX_RATE)
                .setScale(0, RoundingMode.DOWN);
        
        BigDecimal localTax = incomeTax
                .multiply(LOCAL_TAX_RATE)
                .setScale(0, RoundingMode.DOWN);
        
        BigDecimal totalTax = incomeTax.add(localTax);
        BigDecimal netAmount = dividendAmount.subtract(totalTax);

        return TaxResult.builder()
                .grossAmount(dividendAmount)
                .incomeTax(incomeTax)
                .localTax(localTax)
                .totalTax(totalTax)
                .netAmount(netAmount)
                .taxRate(DIVIDEND_INCOME_TAX_RATE.add(LOCAL_TAX_RATE))
                .pensionTaxApplied(false)
                .taxType("DIVIDEND")
                .build();
    }

    public TaxResult calculatePensionTax(BigDecimal pensionAmount, LocalDate birthDate) {
        log.debug("연금소득세 계산 - 금액: {}, 생년월일: {}", pensionAmount, birthDate);

        int age = Period.between(birthDate, LocalDate.now()).getYears();

        BigDecimal taxRate;
        if (age < 70) {
            taxRate = new BigDecimal("0.055");
        } else if (age < 80) {
            taxRate = new BigDecimal("0.044");
        } else {
            taxRate = new BigDecimal("0.033");
        }

        BigDecimal totalTax = pensionAmount
                .multiply(taxRate)
                .setScale(0, RoundingMode.DOWN);
        
        BigDecimal netAmount = pensionAmount.subtract(totalTax);

        log.debug("연금소득세 계산 결과 - 나이: {}, 세율: {}%, 세전: {}, 세금: {}, 세후: {}",
                age, taxRate.multiply(BigDecimal.valueOf(100)), pensionAmount, totalTax, netAmount);

        return TaxResult.builder()
                .grossAmount(pensionAmount)
                .incomeTax(totalTax)
                .localTax(BigDecimal.ZERO)
                .totalTax(totalTax)
                .netAmount(netAmount)
                .taxRate(taxRate)
                .pensionTaxApplied(true)
                .taxType("PENSION")
                .build();
    }

    public BigDecimal getStandardInterestTaxRate() {
        return INTEREST_INCOME_TAX_RATE.add(
                INTEREST_INCOME_TAX_RATE.multiply(LOCAL_TAX_RATE));
    }

    public BigDecimal getPensionTaxRate(LocalDate birthDate) {
        int age = Period.between(birthDate, LocalDate.now()).getYears();
        
        if (age < 70) {
            return new BigDecimal("0.055");
        } else if (age < 80) {
            return new BigDecimal("0.044");
        } else {
            return new BigDecimal("0.033");
        }
    }
}

