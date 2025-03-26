package com.example.visa.recon.exception;

public final class ErrorMessages {
    public static final String TRANSACTION_NOT_FOUND = "Transaction with ID %s not found";
    public static final String DUPLICATE_TRANSACTION = "Transaction with ID %s already exists";
    public static final String INVALID_AMOUNT = "Transaction amount must be greater than zero";
    public static final String MERCHANT_ID_REQUIRED = "Merchant ID is required";
    public static final String FILE_PROCESSING_ERROR = "Error processing file %s: %s";
    
    private ErrorMessages() {
        // Prevent instantiation
    }
} 