package com.example.visa.recon.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import lombok.Data;

@Data
public class TxnMatching {
    private String transactionId;
    private String transactionDate;
    private String amount;

    // Generate a hash key based on transactionId, timestamp, and amount
    public String generateHashKey() {
        String input = this.transactionId + this.transactionDate + this.amount;
        return hash(input);
    }

    private String hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing input", e);
        }
    }
}