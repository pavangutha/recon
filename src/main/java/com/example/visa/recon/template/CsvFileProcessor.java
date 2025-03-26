package com.example.visa.recon.template;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.visa.recon.model.entity.VisaBase2Transaction;

@Service
public class CsvFileProcessor extends FileProcessor {
    @Override
    protected void validateFile(String filePath) {
        // CSV specific validation
    }

    @Override
    protected List<String> readLines(String filePath) {
        // CSV specific reading
        return null; // TODO: Implement CSV reading
    }

    @Override
    protected List<VisaBase2Transaction> parseTransactions(List<String> lines) {
        // CSV specific parsing
        return null; // TODO: Implement CSV parsing
    }
} 