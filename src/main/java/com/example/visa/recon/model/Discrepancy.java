package com.example.visa.recon.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Discrepancy {
    private final String transactionId;
    private final String discrepancyType;
    private final BigDecimal amountSwitch;
    private final BigDecimal amountNetwork;
    private final LocalDateTime createdAt;

    public Discrepancy(String transactionId, String discrepancyType) {
        this.transactionId = transactionId;
        this.discrepancyType = discrepancyType;
        this.amountSwitch = null;
        this.amountNetwork = null;
        this.createdAt = LocalDateTime.now();
    }

    public Discrepancy(String transactionId, String discrepancyType, 
                      BigDecimal amountSwitch, BigDecimal amountNetwork) {
        this.transactionId = transactionId;
        this.discrepancyType = discrepancyType;
        this.amountSwitch = amountSwitch;
        this.amountNetwork = amountNetwork;
        this.createdAt = LocalDateTime.now();
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getDiscrepancyType() {
        return discrepancyType;
    }

    public BigDecimal getAmountSwitch() {
        return amountSwitch;
    }

    public BigDecimal getAmountNetwork() {
        return amountNetwork;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
} 