package com.example.visa.recon.batch;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.visa.recon.mapper.VisaBase2RecordMapper;
import com.example.visa.recon.model.dto.VisaBase2Record;
import com.example.visa.recon.model.entity.VisaBase2RecordEntity;
import com.example.visa.recon.repository.VisaBase2RecordRepository;
import com.example.visa.recon.service.Discrepancy;

@Component
public class FileToDbReconciliationProcessor implements ItemProcessor<VisaBase2Record, VisaBase2RecordEntity> {
    private static final Logger logger = LoggerFactory.getLogger(FileToDbReconciliationProcessor.class);

    @Autowired
    private VisaBase2RecordMapper mapper;

    @Autowired
    private VisaBase2RecordRepository repository;


    @Override
    public VisaBase2RecordEntity process(VisaBase2Record record) throws Exception {
        logger.debug("Processing record with transaction ID: {}", record.getTransactionId());
        
        // Convert DTO to entity
        VisaBase2RecordEntity entity = mapper.toEntity(record);
        
        // Check if record exists in database by transactionId
        VisaBase2RecordEntity existingEntity = repository.findByTransactionId(entity.getTransactionId());
        
        if (existingEntity != null) {
            // Compare fields and detect discrepancies
            List<Discrepancy> discrepancies = detectDiscrepancies(record, existingEntity);
            if (!discrepancies.isEmpty()) {
                logDiscrepancies(discrepancies);
                return entity; // Return for update
            }
        } else {
            // New record, log as missing in network
            Discrepancy discrepancy = new Discrepancy(
                record.getTransactionId(),
                "Missing in Network"
            );
            logDiscrepancy(discrepancy);
            return entity; // Return for insert
        }
        
        return null; // Skip if no changes needed
    }

    private List<Discrepancy> detectDiscrepancies(VisaBase2Record record, VisaBase2RecordEntity existingEntity) {
        List<Discrepancy> discrepancies = new ArrayList<>();
        
        // Check amount mismatch
        if (!record.getAmount().equals(existingEntity.getAmount())) {
            discrepancies.add(new Discrepancy(
                record.getTransactionId(),
                "Amount Mismatch",
                new BigDecimal(record.getAmount()),
                new BigDecimal(existingEntity.getAmount())
            ));
        }

        // Check response code mismatch
        if (!record.getResponseCode().equals(existingEntity.getResponseCode())) {
            discrepancies.add(new Discrepancy(
                record.getTransactionId(),
                "Response Code Mismatch"
            ));
        }

        // Check authorization code mismatch
        if (!record.getAuthorizationCode().equals(existingEntity.getAuthorizationCode())) {
            discrepancies.add(new Discrepancy(
                record.getTransactionId(),
                "Authorization Code Mismatch"
            ));
        }

        // Check transaction date mismatch
        if (!record.getTransactionDate().equals(existingEntity.getTransactionDate())) {
            discrepancies.add(new Discrepancy(
                record.getTransactionId(),
                "Transaction Date Mismatch"
            ));
        }

        return discrepancies;
    }

    private void logDiscrepancies(List<Discrepancy> discrepancies) {
        for (Discrepancy discrepancy : discrepancies) {
            logDiscrepancy(discrepancy);
        }
    }

    private void logDiscrepancy(Discrepancy discrepancy) {
        if (discrepancy.getAmountSwitch() != null && discrepancy.getAmountNetwork() != null) {
            logger.warn("Discrepancy detected for transaction {}: Type={}, Switch Amount={}, Network Amount={}",
                discrepancy.getTransactionId(),
                discrepancy.getDiscrepancyType(),
                discrepancy.getAmountSwitch(),
                discrepancy.getAmountNetwork());
        } else {
            logger.warn("Discrepancy detected for transaction {}: Type={}",
                discrepancy.getTransactionId(),
                discrepancy.getDiscrepancyType());
        }
    }
} 