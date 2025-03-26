package com.example.visa.recon.exception;

import org.springframework.http.HttpStatus;

public class TransactionValidationException extends BaseException {
    private static final String ERROR_CODE = "VISA-400";
    
    public TransactionValidationException(String message) {
        super(
            message,
            ERROR_CODE,
            HttpStatus.BAD_REQUEST
        );
    }
} 