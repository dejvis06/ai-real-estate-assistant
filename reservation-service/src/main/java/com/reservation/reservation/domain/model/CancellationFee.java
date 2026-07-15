package com.reservation.reservation.domain.model;

import java.math.BigDecimal;

public class CancellationFee {

    private final boolean applicable;
    private final BigDecimal amount;
    private final String currency;
    private final String reason;

    public CancellationFee(boolean applicable, BigDecimal amount, String currency, String reason) {
        this.applicable = applicable;
        this.amount = amount;
        this.currency = currency;
        this.reason = reason;
    }

    public boolean isApplicable() { return applicable; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public String getReason() { return reason; }
}
