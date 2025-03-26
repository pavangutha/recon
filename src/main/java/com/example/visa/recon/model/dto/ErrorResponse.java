package com.example.visa.recon.model.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ErrorResponse {
    private String errorCode;
    private String message;
    private int status;
    private String path;
    private LocalDateTime timestamp;
    private String traceId;
} 