package com.example.visa.recon.batch;

import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.visa.recon.model.entity.VisaBase2RecordEntity;
import com.example.visa.recon.repository.VisaBase2RecordRepository;

@Component
public class DbToFileReconciliationReader implements ItemReader<VisaBase2RecordEntity> {
    
    @Autowired
    private VisaBase2RecordRepository repository;
    
    private boolean initialized = false;

    @Override
    public VisaBase2RecordEntity read() throws Exception {
        if (!initialized) {
            initialized = true;
        }
        
        // Read records from database
        return repository.findAll().stream()
            .findFirst()
            .orElse(null);
    }
} 