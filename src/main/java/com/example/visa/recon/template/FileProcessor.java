package com.example.visa.recon.template;

import java.util.List;

import com.example.visa.recon.model.entity.VisaBase2Transaction;

public abstract class FileProcessor {
    
    public final void processFile(String filePath) {
        validateFile(filePath);
        List<String> lines = readLines(filePath);
        List<VisaBase2Transaction> transactions = parseTransactions(lines);
        saveTransactions(transactions);
        generateReport(transactions);
    }

    protected abstract void validateFile(String filePath);
    protected abstract List<String> readLines(String filePath);
    protected abstract List<VisaBase2Transaction> parseTransactions(List<String> lines);
    
    private void saveTransactions(List<VisaBase2Transaction> transactions) {
        // Common save logic
    }

    private void generateReport(List<VisaBase2Transaction> transactions) {
        // Common report generation logic
    }
} 