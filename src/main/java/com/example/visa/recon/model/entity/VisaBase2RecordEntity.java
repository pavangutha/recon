package com.example.visa.recon.model.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "visa_base2_transactions")
@Data
public class VisaBase2RecordEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_type")
    private String transactionType;

    @Column(name = "transaction_id", unique = true)
    private String transactionId;

    @Column(name = "card_number")
    private String cardNumber;

    @Column(name = "amount")
    private String amount;

    @Column(name = "stan")
    private String Stan;

    @Column(name = "currency_code")
    private String currencyCode;

    @Column(name = "transaction_date")
    private String transactionDate;

    @Column(name = "transaction_time")
    private String transactionTime;

    @Column(name = "response_code")
    private String responseCode;

    @Column(name = "account_type")
    private String accountType;

    @Column(name = "authorization_code")
    private String authorizationCode;

    @Column(name = "merchant_id")
    private String merchantId;

    @Column(name = "merchant_category_code")
    private String merchantCategoryCode;

    @Column(name = "terminal_id")
    private String terminalId;

    @Column(name = "card_expiry_date")
    private String cardExpiryDate;

    @Column(name = "cardholder_name")
    private String cardholderName;

    @Column(name = "account_holder_name")
    private String accountHolderName;

    @Column(name = "transaction_fee")
    private String transactionFee;

    @Column(name = "authorization_indicator")
    private String authorizationIndicator;

    @Column(name = "acquirer_bin")
    private String acquirerBin;

    @Column(name = "issuer_bin")
    private String issuerBin;

    @Column(name = "merchant_name")
    private String merchantName;

    @Column(name = "transaction_code")
    private String transactionCode;

    @Column(name = "reason_code")
    private String reasonCode;

    @Column(name = "rrn")
    private String rrn;

    @Column(name = "original_transaction_id")
    private String originalTransactionId;

    @Column(name = "acquirer_reference_number")
    private String acquirerReferenceNumber;

    @Column(name = "batch_number")
    private String batchNumber;

    @Column(name = "date_of_settlement")
    private String dateOfSettlement;

    @Column(name = "settlement_amount")
    private String settlementAmount;

    @Column(name = "issuer_response_code")
    private String issuerResponseCode;

    @Column(name = "transaction_origin")
    private String transactionOrigin;

    @Column(name = "transaction_reference")
    private String transactionReference;

    @Column(name = "original_transaction_amount")
    private String originalTransactionAmount;

    @Column(name = "refund_amount")
    private String refundAmount;

    @Column(name = "adjustment_amount")
    private String adjustmentAmount;

    @Column(name = "loyalty_points_earned")
    private String loyaltyPointsEarned;

    @Column(name = "loyalty_points_redeemed")
    private String loyaltyPointsRedeemed;

    @Column(name = "reversal_indicator")
    private String reversalIndicator;

    @Column(name = "authorization_date_time")
    private String authorizationDateTime;

    @Column(name = "original_authorization_code")
    private String originalAuthorizationCode;

    @Column(name = "narrative")
    private String narrative;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
} 