package com.hanainplan.domain.banking.exception;

public class IrpLimitExceededException extends RuntimeException {
    
    private final String customerCi;
    private final java.math.BigDecimal requestedAmount;
    private final java.math.BigDecimal remaining;

    public IrpLimitExceededException(String message, String customerCi, 
                                     java.math.BigDecimal requestedAmount, 
                                     java.math.BigDecimal remaining) {
        super(message);
        this.customerCi = customerCi;
        this.requestedAmount = requestedAmount;
        this.remaining = remaining;
    }

    public String getCustomerCi() {
        return customerCi;
    }

    public java.math.BigDecimal getRequestedAmount() {
        return requestedAmount;
    }

    public java.math.BigDecimal getRemaining() {
        return remaining;
    }
}

