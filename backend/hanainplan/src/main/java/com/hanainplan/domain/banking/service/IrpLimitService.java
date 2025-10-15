package com.hanainplan.domain.banking.service;

import com.hanainplan.domain.banking.dto.IrpLimitStatus;
import com.hanainplan.domain.banking.exception.IrpLimitExceededException;
import com.hanainplan.domain.banking.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class IrpLimitService {

    private static final BigDecimal ANNUAL_LIMIT = new BigDecimal("9000000");
    
    private final TransactionRepository transactionRepository;

    public void checkAnnualLimit(String customerCi, BigDecimal depositAmount) {
        int currentYear = LocalDate.now().getYear();
        
        BigDecimal yearlyTotal = transactionRepository
                .sumIrpDepositsByCustomerCiAndYear(customerCi, currentYear)
                .orElse(BigDecimal.ZERO);
        
        BigDecimal newTotal = yearlyTotal.add(depositAmount);
        
        if (newTotal.compareTo(ANNUAL_LIMIT) > 0) {
            BigDecimal remaining = ANNUAL_LIMIT.subtract(yearlyTotal);
            
            log.warn("IRP 연간 납입 한도 초과 - customerCi: {}, 현재 납입액: {}, 요청 금액: {}, 잔여 한도: {}",
                    customerCi, yearlyTotal, depositAmount, remaining);
            
            throw new IrpLimitExceededException(
                    String.format("IRP 연간 납입 한도 초과. 잔여 한도: %,d원", remaining.intValue()),
                    customerCi,
                    depositAmount,
                    remaining
            );
        }
        
        log.info("IRP 한도 체크 통과 - customerCi: {}, 현재: {}, 요청: {}, 새 합계: {}",
                customerCi, yearlyTotal, depositAmount, newTotal);
    }

    public IrpLimitStatus getAnnualLimitStatus(String customerCi) {
        int currentYear = LocalDate.now().getYear();
        
        BigDecimal yearlyTotal = transactionRepository
                .sumIrpDepositsByCustomerCiAndYear(customerCi, currentYear)
                .orElse(BigDecimal.ZERO);
        
        return IrpLimitStatus.of(currentYear, yearlyTotal, ANNUAL_LIMIT);
    }

    public BigDecimal getAnnualLimit() {
        return ANNUAL_LIMIT;
    }

    public BigDecimal getRemainingLimit(String customerCi) {
        IrpLimitStatus status = getAnnualLimitStatus(customerCi);
        return status.getRemaining();
    }
}

