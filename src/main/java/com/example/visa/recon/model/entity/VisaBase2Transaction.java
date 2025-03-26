package com.example.visa.recon.model.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
/*

*/

@Entity
@Table(name = "visa_base2_transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisaBase2Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_type")
    private String transactionType;

    @Column(name = "transaction_id", unique = true, nullable = false)
    private String transactionId;

    @Column(name = "card_number", length = 16)
    private String cardNumber;

    @Column(name = "amount", precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "currency_code", length = 3)
    private String currencyCode;

    @Column(name = "transaction_date")
    private LocalDateTime transactionDate;

    @Column(name = "response_code", length = 2)
    private String responseCode;

    @Column(name = "account_type")
    private String accountType;

    @Column(name = "authorization_code", length = 6)
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

    @Column(name = "transaction_fee", precision = 19, scale = 4)
    private BigDecimal transactionFee;

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

    @Column(name = "original_transaction_id")
    private String originalTransactionId;

    @Column(name = "acquirer_reference_number")
    private String acquirerReferenceNumber;

    @Column(name = "batch_number")
    private String batchNumber;

    @Column(name = "date_of_settlement")
    private LocalDateTime dateOfSettlement;

    @Column(name = "settlement_amount", precision = 19, scale = 4)
    private BigDecimal settlementAmount;

    @Column(name = "issuer_response_code")
    private String issuerResponseCode;

    @Column(name = "transaction_origin")
    private String transactionOrigin;

    @Column(name = "transaction_reference")
    private String transactionReference;

    @Column(name = "original_transaction_amount", precision = 19, scale = 4)
    private BigDecimal originalTransactionAmount;

    @Column(name = "refund_amount", precision = 19, scale = 4)
    private BigDecimal refundAmount;

    @Column(name = "adjustment_amount", precision = 19, scale = 4)
    private BigDecimal adjustmentAmount;

    @Column(name = "loyalty_points_earned")
    private Integer loyaltyPointsEarned;

    @Column(name = "loyalty_points_redeemed")
    private Integer loyaltyPointsRedeemed;

    @Column(name = "reversal_indicator")
    private String reversalIndicator;

    @Column(name = "authorization_date_time")
    private LocalDateTime authorizationDateTime;

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

    @Override
    public String toString() {
        return "VisaBase2Transaction{" +
                "transactionType='" + transactionType + '\'' +
                ", transactionId='" + transactionId + '\'' +
                ", cardNumber='" + cardNumber + '\'' +
                ", amount='" + amount + '\'' +
                ", currencyCode='" + currencyCode + '\'' +
                ", transactionDate='" + transactionDate + '\'' +
                ", responseCode='" + responseCode + '\'' +
                ", accountType='" + accountType + '\'' +
                ", authorizationCode='" + authorizationCode + '\'' +
                ", merchantId='" + merchantId + '\'' +
                ", merchantCategoryCode='" + merchantCategoryCode + '\'' +
                ", terminalId='" + terminalId + '\'' +
                ", cardExpiryDate='" + cardExpiryDate + '\'' +
                ", cardholderName='" + cardholderName + '\'' +
                ", accountHolderName='" + accountHolderName + '\'' +
                ", transactionFee='" + transactionFee + '\'' +
                ", authorizationIndicator='" + authorizationIndicator + '\'' +
                ", acquirerBin='" + acquirerBin + '\'' +
                ", issuerBin='" + issuerBin + '\'' +
                ", merchantName='" + merchantName + '\'' +
                ", transactionCode='" + transactionCode + '\'' +
                ", reasonCode='" + reasonCode + '\'' +
                ", originalTransactionId='" + originalTransactionId + '\'' +
                ", acquirerReferenceNumber='" + acquirerReferenceNumber + '\'' +
                ", batchNumber='" + batchNumber + '\'' +
                ", dateOfSettlement='" + dateOfSettlement + '\'' +
                ", settlementAmount='" + settlementAmount + '\'' +
                ", issuerResponseCode='" + issuerResponseCode + '\'' +
                ", transactionOrigin='" + transactionOrigin + '\'' +
                ", transactionReference='" + transactionReference + '\'' +
                ", originalTransactionAmount='" + originalTransactionAmount + '\'' +
                ", refundAmount='" + refundAmount + '\'' +
                ", adjustmentAmount='" + adjustmentAmount + '\'' +
                ", loyaltyPointsEarned='" + loyaltyPointsEarned + '\'' +
                ", loyaltyPointsRedeemed='" + loyaltyPointsRedeemed + '\'' +
                ", reversalIndicator='" + reversalIndicator + '\'' +
                ", authorizationDateTime='" + authorizationDateTime + '\'' +
                ", originalAuthorizationCode='" + originalAuthorizationCode + '\'' +
                ", narrative='" + narrative + '\'' +
                '}';
    }
}
