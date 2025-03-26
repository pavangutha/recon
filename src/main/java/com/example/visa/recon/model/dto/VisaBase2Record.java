package com.example.visa.recon.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
/*

*/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisaBase2Record {
    private String transactionType;
    private String transactionId;
    private String cardNumber;
    private String amount;
    private String Stan;
    private String currencyCode;
    private String transactionDate;
    private String transactionTime;
    private String responseCode;
    private String accountType;
    private String authorizationCode;
    private String merchantId;
    private String merchantCategoryCode;
    private String terminalId;
    private String cardExpiryDate;
    private String cardholderName;
    private String accountHolderName;
    private String transactionFee;
    private String authorizationIndicator;
    private String acquirerBin;
    private String issuerBin;
    private String merchantName;
    private String transactionCode;
    private String reasonCode;
    private String rrn;
    private String originalTransactionId;
    private String acquirerReferenceNumber;
    private String batchNumber;
    private String dateOfSettlement;
    private String settlementAmount;
    private String issuerResponseCode;
    private String transactionOrigin;
    private String transactionReference;
    private String originalTransactionAmount;
    private String refundAmount;
    private String adjustmentAmount;
    private String loyaltyPointsEarned;
    private String loyaltyPointsRedeemed;
    private String reversalIndicator;
    private String authorizationDateTime;
    private String originalAuthorizationCode;
    private String narrative;

    @Override
    public String toString() {
        return "VisaBase2Transaction{" +
                "transactionType='" + transactionType + '\'' +
                ", transactionId='" + transactionId + '\'' +
                ", cardNumber='" + cardNumber + '\'' +
                ", amount='" + amount + '\'' +
                ", Stan='" + Stan + '\'' +
                ", currencyCode='" + currencyCode + '\'' +
                ", transactionDate='" + transactionDate + '\'' +
                ", transactionTime='" + transactionTime + '\'' +
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
                ", rrn='" + rrn + '\'' +
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
