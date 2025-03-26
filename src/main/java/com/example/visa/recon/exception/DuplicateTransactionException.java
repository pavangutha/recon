package com.example.visa.recon.exception;

import org.springframework.http.HttpStatus;

public class DuplicateTransactionException extends BaseException {
    private static final String ERROR_CODE = "VISA-409";
    
    public DuplicateTransactionException(String transactionId) {
        super(
            String.format("Transaction with ID %s already exists", transactionId),
            ERROR_CODE,
            HttpStatus.CONFLICT
        );
    }
} 