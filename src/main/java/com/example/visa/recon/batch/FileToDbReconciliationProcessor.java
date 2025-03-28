package com.example.visa.recon.batch;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.visa.recon.mapper.VisaBase2RecordMapper;
import com.example.visa.recon.model.dto.VisaBase2Record;
import com.example.visa.recon.model.entity.VisaBase2RecordEntity;
import com.example.visa.recon.repository.VisaBase2RecordRepository;

@Component
public class FileToDbReconciliationProcessor implements ItemProcessor<VisaBase2Record, VisaBase2RecordEntity> {

    @Autowired
    private VisaBase2RecordMapper mapper;

    @Autowired
    private VisaBase2RecordRepository repository;

    @Override
    public VisaBase2RecordEntity process(VisaBase2Record record) throws Exception {
        // Convert DTO to entity
        VisaBase2RecordEntity entity = mapper.toEntity(record);
        
        // Check if record exists in database by transactionId
        VisaBase2RecordEntity existingEntity = repository.findByTransactionId(entity.getTransactionId());
        if (existingEntity != null) {
            // Compare fields and mark for update if different
            if (!existingEntity.equals(entity)) {
                return entity; // Return for update
            }
        } else {
            // New record, return for insert
            return entity;
        }
        
        return null; // Skip if no changes needed
    }
} 