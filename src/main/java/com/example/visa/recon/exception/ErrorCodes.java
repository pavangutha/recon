package com.example.visa.recon.exception;

public final class ErrorCodes {
    public static final String TRANSACTION_NOT_FOUND = "VISA-404";
    public static final String VALIDATION_ERROR = "VISA-400";
    public static final String PROCESSING_ERROR = "VISA-500";
    public static final String DUPLICATE_TRANSACTION = "VISA-409";
    public static final String FILE_PROCESSING_ERROR = "VISA-502";
    
    private ErrorCodes() {
        // Prevent instantiation
    }
} 