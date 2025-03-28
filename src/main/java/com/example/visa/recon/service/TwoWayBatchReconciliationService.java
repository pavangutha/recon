package com.example.visa.recon.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.visa.recon.model.dto.VisaBase2Record;
import com.example.visa.recon.model.entity.VisaBase2RecordEntity;
import com.example.visa.recon.repository.VisaBase2RecordRepository;
import com.example.visa.recon.mapper.VisaBase2RecordMapper;

@Service
public class TwoWayBatchReconciliationService {
    private static final Logger logger = LoggerFactory.getLogger(TwoWayBatchReconciliationService.class);

    @Autowired
    private FileReader fileReader;

    @Autowired
    private VisaBase2RecordRepository repository;

    @Autowired
    private VisaBase2RecordMapper mapper;

    @Autowired
    private ExcelReportGenerator reportGenerator;

    public void performTwoWayReconciliation(String filePath, String reportPath, int batchSize) {
        logger.info("Starting two-way reconciliation between file and database");
        
        List<Discrepancy> fileToDbDiscrepancies = new ArrayList<>();
        List<Discrepancy> dbToFileDiscrepancies = new ArrayList<>();
        
        AtomicInteger processedCount = new AtomicInteger(0);
        AtomicInteger matchedCount = new AtomicInteger(0);

        // Process file records against database
        fileReader.processByBatch(filePath, batchSize, batch -> {
            for (VisaBase2Record record : batch) {
                try {
                    processFileRecord(record, fileToDbDiscrepancies, matchedCount);
                    processedCount.incrementAndGet();
                } catch (Exception e) {
                    logger.error("Error processing file record {}: {}", record.getTransactionId(), e.getMessage());
                    fileToDbDiscrepancies.add(new Discrepancy(
                        record.getTransactionId(),
                        "Processing Error: " + e.getMessage()
                    ));
                }
            }
        });

        // Process database records against file
        List<VisaBase2RecordEntity> dbRecords = repository.findAll();
        for (VisaBase2RecordEntity dbRecord : dbRecords) {
            try {
                processDbRecord(dbRecord, filePath, dbToFileDiscrepancies);
            } catch (Exception e) {
                logger.error("Error processing DB record {}: {}", dbRecord.getTransactionId(), e.getMessage());
                dbToFileDiscrepancies.add(new Discrepancy(
                    dbRecord.getTransactionId(),
                    "Processing Error: " + e.getMessage()
                ));
            }
        }

        // Generate comprehensive report
        reportGenerator.generateReport(reportPath, fileToDbDiscrepancies, dbToFileDiscrepancies);
        
        logger.info("Two-way reconciliation completed. Processed {} records, matched {} records. " +
                   "Found {} file-to-DB discrepancies and {} DB-to-file discrepancies",
            processedCount.get(), matchedCount.get(), 
            fileToDbDiscrepancies.size(), dbToFileDiscrepancies.size());
    }

    private void processFileRecord(VisaBase2Record record, List<Discrepancy> discrepancies, AtomicInteger matchedCount) {
        VisaBase2RecordEntity entity = mapper.toEntity(record);
        VisaBase2RecordEntity existingEntity = repository.findByTransactionId(entity.getTransactionId());

        if (existingEntity != null) {
            // Check for discrepancies
            if (!existingEntity.getAmount().equals(entity.getAmount())) {
                discrepancies.add(new Discrepancy(
                    record.getTransactionId(),
                    "Amount Mismatch",
                    new BigDecimal(record.getAmount()),
                    new BigDecimal(existingEntity.getAmount())
                ));
            }

            if (!existingEntity.getResponseCode().equals(entity.getResponseCode())) {
                discrepancies.add(new Discrepancy(
                    record.getTransactionId(),
                    "Response Code Mismatch"
                ));
            }

            if (!existingEntity.getAuthorizationCode().equals(entity.getAuthorizationCode())) {
                discrepancies.add(new Discrepancy(
                    record.getTransactionId(),
                    "Authorization Code Mismatch"
                ));
            }

            if (!existingEntity.getTransactionDate().equals(entity.getTransactionDate())) {
                discrepancies.add(new Discrepancy(
                    record.getTransactionId(),
                    "Transaction Date Mismatch"
                ));
            }

            matchedCount.incrementAndGet();
        } else {
            // Record missing in database
            discrepancies.add(new Discrepancy(
                record.getTransactionId(),
                "Missing in Database"
            ));
        }
    }

    private void processDbRecord(VisaBase2RecordEntity dbRecord, String filePath, List<Discrepancy> discrepancies) {
        // Check if record exists in file
        boolean recordExists = fileReader.streamRecords(filePath)
            .anyMatch(record -> record.getTransactionId().equals(dbRecord.getTransactionId()));

        if (!recordExists) {
            discrepancies.add(new Discrepancy(
                dbRecord.getTransactionId(),
                "Missing in File"
            ));
        }
    }
} 