package com.example.visa.recon.exception;

import org.springframework.http.HttpStatus;

public class FileProcessingException extends BaseException {
    private static final String ERROR_CODE = "VISA-502";
    
    public FileProcessingException(String filename, String reason) {
        super(
            String.format("Error processing file %s: %s", filename, reason),
            ERROR_CODE,
            HttpStatus.BAD_GATEWAY
        );
    }
} 