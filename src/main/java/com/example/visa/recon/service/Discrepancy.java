package com.example.visa.recon.service;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class Discrepancy {
    private String transactionId;
    private String discrepancyType;
    private BigDecimal amountSwitch;
    private BigDecimal amountNetwork;

    public Discrepancy(String transactionId, String discrepancyType) {
        this.transactionId = transactionId;
        this.discrepancyType = discrepancyType;
    }

    public Discrepancy(String transactionId, String discrepancyType, BigDecimal amountSwitch, BigDecimal amountNetwork) {
        this.transactionId = transactionId;
        this.discrepancyType = discrepancyType;
        this.amountSwitch = amountSwitch;
        this.amountNetwork = amountNetwork;
    }

    // Getters and Setters
}

