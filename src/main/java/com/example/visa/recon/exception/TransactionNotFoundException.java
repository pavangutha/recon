package com.example.visa.recon.exception;

import org.springframework.http.HttpStatus;

public class TransactionNotFoundException extends BaseException {
    private static final String ERROR_CODE = "VISA-404";
    
    public TransactionNotFoundException(String transactionId) {
        super(
            String.format("Transaction with ID %s not found", transactionId),
            ERROR_CODE,
            HttpStatus.NOT_FOUND
        );
    }
} 