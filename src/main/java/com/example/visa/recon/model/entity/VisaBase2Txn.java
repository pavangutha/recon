package com.example.visa.recon.model.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.visa.recon.model.enums.CardType;
import com.example.visa.recon.model.enums.TransactionStatus;
import com.example.visa.recon.model.enums.TransactionType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "visa_base2_transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisaBase2Txn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_id", unique = true, nullable = false)
    private String transactionId;

    @Column(name = "merchant_name")
    private String merchantName;

    @Column(name = "merchant_id")
    private String merchantId;

    @Column(name = "merchant_category_code")
    private String merchantCategoryCode;

    @Column(name = "transaction_amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal amount;

    @Column(name = "transaction_currency", length = 3)
    private String currency;

    @Column(name = "transaction_date_time", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "card_number", length = 16)
    private String cardNumber;

    @Column(name = "card_type")
    @Enumerated(EnumType.STRING)
    private CardType cardType;

    @Column(name = "transaction_type")
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    @Column(name = "authorization_code", length = 6)
    private String authorizationCode;

    @Column(name = "response_code", length = 2)
    private String responseCode;

    @Column(name = "processing_code", length = 6)
    private String processingCode;

    @Column(name = "terminal_id")
    private String terminalId;

    @Column(name = "terminal_location")
    private String terminalLocation;

    @Column(name = "country_code", length = 3)
    private String countryCode;

    @Column(name = "settlement_amount", precision = 19, scale = 4)
    private BigDecimal settlementAmount;

    @Column(name = "settlement_currency", length = 3)
    private String settlementCurrency;

    @Column(name = "transaction_status")
    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "batch_id")
    private String batchId;

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