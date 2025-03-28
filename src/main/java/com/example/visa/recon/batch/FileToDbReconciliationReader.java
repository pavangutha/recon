package com.example.visa.recon.batch;

import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.visa.recon.model.dto.VisaBase2Record;
import com.example.visa.recon.service.FileReader;

@Component
public class FileToDbReconciliationReader implements ItemReader<VisaBase2Record> {
    
    @Autowired
    private FileReader fileReader;
    
    private String filePath;
    private boolean initialized = false;

    @Override
    public VisaBase2Record read() throws Exception {
        if (!initialized) {
            // Initialize the file reader with the file path
            filePath = "visa_base2_transactions.csv"; // This should be configurable    
            initialized = true;
        }
        
        // Use the existing FileReader service to read records
        return fileReader.streamRecords(filePath)
            .findFirst()
            .orElse(null);
    }
} 