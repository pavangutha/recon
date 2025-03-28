package com.example.visa.recon.batch;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.visa.recon.mapper.VisaBase2RecordMapper;
import com.example.visa.recon.model.dto.VisaBase2Record;
import com.example.visa.recon.model.entity.VisaBase2RecordEntity;
import com.example.visa.recon.service.FileReader;

@Component
public class DbToFileReconciliationProcessor implements ItemProcessor<VisaBase2RecordEntity, VisaBase2Record> {

    @Autowired
    private VisaBase2RecordMapper mapper;

    @Autowired
    private FileReader fileReader;

    @Override
    public VisaBase2Record process(VisaBase2RecordEntity entity) throws Exception {
        // Convert entity to DTO
        VisaBase2Record dto = mapper.toDto(entity);
        
        // Check if record exists in file
        String filePath = "path/to/your/input/file.csv"; // This should be configurable
        boolean existsInFile = fileReader.streamRecords(filePath)
            .anyMatch(record -> record.getTransactionId().equals(dto.getTransactionId()));
            
        if (!existsInFile) {
            // Record exists in DB but not in file
            return dto;
        }
        
        return null; // Skip if record exists in both places
    }
} 