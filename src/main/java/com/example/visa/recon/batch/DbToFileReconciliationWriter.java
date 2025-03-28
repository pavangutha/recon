package com.example.visa.recon.batch;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.visa.recon.model.dto.VisaBase2Record;

@Component
public class DbToFileReconciliationWriter implements ItemWriter<VisaBase2Record> {

    @Value("${reconciliation.output.file}")
    private String outputFilePath;

    @Override
    public void write(Chunk<? extends VisaBase2Record> chunk) throws Exception {
        Path path = Paths.get(outputFilePath);
        
        // Create output file if it doesn't exist
        if (!Files.exists(path)) {
            Files.createFile(path);
        }

        // Append records to output file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path.toFile(), true))) {
            for (VisaBase2Record record : chunk.getItems()) {
                writer.write(recordToCsv(record));
                writer.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Error writing to output file: " + outputFilePath, e);
        }
    }

    private String recordToCsv(VisaBase2Record record) {
        return String.join(",", 
            record.getTransactionType(),
            record.getTransactionId(),
            record.getCardNumber(),
            record.getAmount(),
            record.getStan(),
            record.getCurrencyCode(),
            record.getTransactionDate(),
            record.getTransactionTime(),
            record.getResponseCode(),
            record.getAccountType(),
            record.getAuthorizationCode(),
            record.getMerchantId(),
            record.getMerchantCategoryCode(),
            record.getTerminalId(),
            record.getCardExpiryDate(),
            record.getCardholderName(),
            record.getAccountHolderName(),
            record.getTransactionFee(),
            record.getAuthorizationIndicator(),
            record.getAcquirerBin(),
            record.getIssuerBin(),
            record.getMerchantName(),
            record.getTransactionCode(),
            record.getReasonCode(),
            record.getRrn(),
            record.getOriginalTransactionId(),
            record.getAcquirerReferenceNumber(),
            record.getBatchNumber(),
            record.getDateOfSettlement(),
            record.getSettlementAmount(),
            record.getIssuerResponseCode(),
            record.getTransactionOrigin(),
            record.getTransactionReference(),
            record.getOriginalTransactionAmount(),
            record.getRefundAmount(),
            record.getAdjustmentAmount(),
            record.getLoyaltyPointsEarned(),
            record.getLoyaltyPointsRedeemed(),
            record.getReversalIndicator(),
            record.getAuthorizationDateTime(),
            record.getOriginalAuthorizationCode(),
            record.getNarrative()
        );
    }
} 