package com.example.visa.recon.exception;

import org.springframework.http.HttpStatus;

public class TransactionProcessingException extends BaseException {
    private static final String ERROR_CODE = "VISA-500";
    
    public TransactionProcessingException(String message, Throwable cause) {
        super(
            message,
            ERROR_CODE,
            HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
} 