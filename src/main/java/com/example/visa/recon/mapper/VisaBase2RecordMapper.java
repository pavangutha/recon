package com.example.visa.recon.mapper;

import org.springframework.stereotype.Component;

import com.example.visa.recon.model.dto.VisaBase2Record;
import com.example.visa.recon.model.entity.VisaBase2RecordEntity;

@Component
public class VisaBase2RecordMapper {

    public VisaBase2RecordEntity toEntity(VisaBase2Record dto) {
        if (dto == null) return null;
        System.out.println("Stan: "+dto.getStan());

        VisaBase2RecordEntity entity = new VisaBase2RecordEntity();
        try {
            
        
        entity.setTransactionType(dto.getTransactionType());
        entity.setTransactionId(dto.getTransactionId());
        entity.setCardNumber(dto.getCardNumber());
        entity.setAmount(dto.getAmount());
        entity.setStan(dto.getStan());
        entity.setCurrencyCode(dto.getCurrencyCode());
        entity.setTransactionDate(dto.getTransactionDate());
        entity.setTransactionTime(dto.getTransactionTime());
        entity.setResponseCode(dto.getResponseCode());
        entity.setAccountType(dto.getAccountType());
        entity.setAuthorizationCode(dto.getAuthorizationCode());
        entity.setMerchantId(dto.getMerchantId());
        entity.setMerchantCategoryCode(dto.getMerchantCategoryCode());
        entity.setTerminalId(dto.getTerminalId());
        entity.setCardExpiryDate(dto.getCardExpiryDate());
        entity.setCardholderName(dto.getCardholderName());
        entity.setAccountHolderName(dto.getAccountHolderName());
        entity.setTransactionFee(dto.getTransactionFee());
        entity.setAuthorizationIndicator(dto.getAuthorizationIndicator());
        entity.setAcquirerBin(dto.getAcquirerBin());
        entity.setIssuerBin(dto.getIssuerBin());
        entity.setMerchantName(dto.getMerchantName());
        entity.setTransactionCode(dto.getTransactionCode());
        entity.setReasonCode(dto.getReasonCode());
        entity.setRrn(dto.getRrn());
        entity.setOriginalTransactionId(dto.getOriginalTransactionId());
        entity.setAcquirerReferenceNumber(dto.getAcquirerReferenceNumber());
        entity.setBatchNumber(dto.getBatchNumber());
        entity.setDateOfSettlement(dto.getDateOfSettlement());
        entity.setSettlementAmount(dto.getSettlementAmount());
        entity.setIssuerResponseCode(dto.getIssuerResponseCode());
        entity.setTransactionOrigin(dto.getTransactionOrigin());
        entity.setTransactionReference(dto.getTransactionReference());
        entity.setOriginalTransactionAmount(dto.getOriginalTransactionAmount());
        entity.setRefundAmount(dto.getRefundAmount());
        entity.setAdjustmentAmount(dto.getAdjustmentAmount());
        entity.setLoyaltyPointsEarned(dto.getLoyaltyPointsEarned());
        entity.setLoyaltyPointsRedeemed(dto.getLoyaltyPointsRedeemed());
        entity.setReversalIndicator(dto.getReversalIndicator());
        entity.setAuthorizationDateTime(dto.getAuthorizationDateTime());
        entity.setOriginalAuthorizationCode(dto.getOriginalAuthorizationCode());
        entity.setNarrative(dto.getNarrative());
        } catch (Exception e) {
            System.out.println("Error: "+e.getMessage());
        }
        return entity;
    }

    public VisaBase2Record toDto(VisaBase2RecordEntity entity) {
        if (entity == null) return null;
        
        return VisaBase2Record.builder()
                .transactionType(entity.getTransactionType())
                .transactionId(entity.getTransactionId())
                .cardNumber(entity.getCardNumber())
                .amount(entity.getAmount())
                .Stan(entity.getStan())
                .currencyCode(entity.getCurrencyCode())
                .transactionDate(entity.getTransactionDate())
                .transactionTime(entity.getTransactionTime())
                .responseCode(entity.getResponseCode())
                .accountType(entity.getAccountType())
                .authorizationCode(entity.getAuthorizationCode())
                .merchantId(entity.getMerchantId())
                .merchantCategoryCode(entity.getMerchantCategoryCode())
                .terminalId(entity.getTerminalId())
                .cardExpiryDate(entity.getCardExpiryDate())
                .cardholderName(entity.getCardholderName())
                .accountHolderName(entity.getAccountHolderName())
                .transactionFee(entity.getTransactionFee())
                .authorizationIndicator(entity.getAuthorizationIndicator())
                .acquirerBin(entity.getAcquirerBin())
                .issuerBin(entity.getIssuerBin())
                .merchantName(entity.getMerchantName())
                .transactionCode(entity.getTransactionCode())
                .reasonCode(entity.getReasonCode())
                .rrn(entity.getRrn())
                .originalTransactionId(entity.getOriginalTransactionId())
                .acquirerReferenceNumber(entity.getAcquirerReferenceNumber())
                .batchNumber(entity.getBatchNumber())
                .dateOfSettlement(entity.getDateOfSettlement())
                .settlementAmount(entity.getSettlementAmount())
                .issuerResponseCode(entity.getIssuerResponseCode())
                .transactionOrigin(entity.getTransactionOrigin())
                .transactionReference(entity.getTransactionReference())
                .originalTransactionAmount(entity.getOriginalTransactionAmount())
                .refundAmount(entity.getRefundAmount())
                .adjustmentAmount(entity.getAdjustmentAmount())
                .loyaltyPointsEarned(entity.getLoyaltyPointsEarned())
                .loyaltyPointsRedeemed(entity.getLoyaltyPointsRedeemed())
                .reversalIndicator(entity.getReversalIndicator())
                .authorizationDateTime(entity.getAuthorizationDateTime())
                .originalAuthorizationCode(entity.getOriginalAuthorizationCode())
                .narrative(entity.getNarrative())
                .build();
    }
} 