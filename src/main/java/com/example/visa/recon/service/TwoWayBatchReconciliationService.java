package com.example.visa.recon.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.visa.recon.model.dto.VisaBase2Record;
import com.example.visa.recon.model.entity.VisaBase2RecordEntity;
import com.example.visa.recon.repository.VisaBase2RecordRepository;
import com.example.visa.recon.mapper.VisaBase2RecordMapper;

@Service
public class TwoWayBatchReconciliationService {
    private static final Logger logger = LoggerFactory.getLogger(TwoWayBatchReconciliationService.class);
    
    @Value("${reconciliation.batch.size:1000}")
    private int batchSize;
    
    @Value("${reconciliation.thread-pool.core-size:4}")
    private int corePoolSize;
    
    @Value("${reconciliation.thread-pool.max-size:8}")
    private int maxPoolSize;
    
    @Value("${reconciliation.thread-pool.queue-capacity:100}")
    private int queueCapacity;

    @Autowired
    private FileReader fileReader;

    @Autowired
    private VisaBase2RecordRepository repository;

    @Autowired
    private VisaBase2RecordMapper mapper;

    @Autowired
    private ExcelReportGenerator reportGenerator;

    @Async
    @Transactional(readOnly = true)
    public void performTwoWayReconciliation(String filePath, String reportPath, int batchSize) {
        logger.info("Starting optimized two-way reconciliation between file and database");
        
        List<Discrepancy> fileToDbDiscrepancies = new CopyOnWriteArrayList<>();
        List<Discrepancy> dbToFileDiscrepancies = new CopyOnWriteArrayList<>();
        Set<String> fileTransactionIds = ConcurrentHashMap.newKeySet();
        
        AtomicInteger processedCount = new AtomicInteger(0);
        AtomicInteger matchedCount = new AtomicInteger(0);
        AtomicInteger totalFileRecords = new AtomicInteger(0);
        AtomicInteger totalDbRecords = new AtomicInteger(0);

        // First pass: Cache file transaction IDs with parallel processing
        logger.info("Caching file transaction IDs...");
        fileReader.streamRecords(filePath)
            .parallel()
            .forEach(record -> {
                if (record != null && record.getTransactionId() != null) {
                    fileTransactionIds.add(record.getTransactionId());
                    totalFileRecords.incrementAndGet();
                }
            });
        logger.info("Total file records found: {}", totalFileRecords.get());

        // Process file records against database in optimized batches
        logger.info("Processing file records against database...");
        fileReader.processByBatch(filePath, batchSize, batch -> {
            List<VisaBase2RecordEntity> entities = batch.stream()
                .filter(record -> record != null && record.getTransactionId() != null)
                .map(mapper::toEntity)
                .collect(Collectors.toList());

            if (!entities.isEmpty()) {
                // Batch database lookup with optimized query
                List<String> transactionIds = entities.stream()
                    .map(VisaBase2RecordEntity::getTransactionId)
                    .filter(id -> id != null)
                    .collect(Collectors.toList());

                if (!transactionIds.isEmpty()) {
                    // Use parallel stream for processing database results
                    Map<String, VisaBase2RecordEntity> existingEntities = repository
                        .findByTransactionIdIn(transactionIds)
                        .parallelStream()
                        .filter(entity -> entity != null && entity.getTransactionId() != null)
                        .collect(Collectors.toConcurrentMap(
                            VisaBase2RecordEntity::getTransactionId,
                            entity -> entity,
                            (existing, replacement) -> existing
                        ));

                    // Process batch with parallel stream
                    entities.parallelStream()
                        .forEach(entity -> {
                            try {
                                processFileRecord(entity, existingEntities.get(entity.getTransactionId()), 
                                    fileToDbDiscrepancies, matchedCount);
                                processedCount.incrementAndGet();
                            } catch (Exception e) {
                                logger.error("Error processing file record {}: {}", 
                                    entity.getTransactionId(), e.getMessage());
                                fileToDbDiscrepancies.add(new Discrepancy(
                                    entity.getTransactionId(),
                                    "Processing Error: " + e.getMessage()
                                ));
                            }
                        });
                }
            }
        });

        // Process database records against file in optimized batches
        logger.info("Processing database records against file...");
        List<VisaBase2RecordEntity> dbRecords = repository.findAll();
        totalDbRecords.set(dbRecords.size());
        logger.info("Total database records found: {}", totalDbRecords.get());

        // Process DB records in parallel batches
        dbRecords.parallelStream()
            .filter(dbRecord -> dbRecord != null && dbRecord.getTransactionId() != null)
            .forEach(dbRecord -> {
                try {
                    if (!fileTransactionIds.contains(dbRecord.getTransactionId())) {
                        dbToFileDiscrepancies.add(new Discrepancy(
                            dbRecord.getTransactionId(),
                            "Missing in File"
                        ));
                    }
                } catch (Exception e) {
                    logger.error("Error processing DB record {}: {}", 
                        dbRecord.getTransactionId(), e.getMessage());
                    dbToFileDiscrepancies.add(new Discrepancy(
                        dbRecord.getTransactionId(),
                        "Processing Error: " + e.getMessage()
                    ));
                }
            });

        // Generate report asynchronously
        logger.info("Generating reconciliation report...");
        reportGenerator.generateReport(reportPath, fileToDbDiscrepancies, dbToFileDiscrepancies);
        
        logger.info("Two-way reconciliation completed. Processed {} records, matched {} records. " +
                   "Found {} file-to-DB discrepancies and {} DB-to-file discrepancies. " +
                   "Total file records: {}, Total DB records: {}",
            processedCount.get(), matchedCount.get(), 
            fileToDbDiscrepancies.size(), dbToFileDiscrepancies.size(),
            totalFileRecords.get(), totalDbRecords.get());
    }

    private void processFileRecord(VisaBase2RecordEntity entity, 
                                 VisaBase2RecordEntity existingEntity,
                                 List<Discrepancy> discrepancies, 
                                 AtomicInteger matchedCount) {
        if (existingEntity != null) {
            // Check for discrepancies with optimized comparisons
            boolean hasDiscrepancy = false;
            String discrepancyType = null;
            BigDecimal fileAmount = null;
            BigDecimal dbAmount = null;

            if (!existingEntity.getAmount().equals(entity.getAmount())) {
                hasDiscrepancy = true;
                discrepancyType = "Amount Mismatch";
                fileAmount = new BigDecimal(entity.getAmount());
                dbAmount = new BigDecimal(existingEntity.getAmount());
            } else if (!existingEntity.getResponseCode().equals(entity.getResponseCode())) {
                hasDiscrepancy = true;
                discrepancyType = "Response Code Mismatch";
            } else if (!existingEntity.getAuthorizationCode().equals(entity.getAuthorizationCode())) {
                hasDiscrepancy = true;
                discrepancyType = "Authorization Code Mismatch";
            } else if (!existingEntity.getTransactionDate().equals(entity.getTransactionDate())) {
                hasDiscrepancy = true;
                discrepancyType = "Transaction Date Mismatch";
            }

            if (hasDiscrepancy) {
                discrepancies.add(new Discrepancy(
                    entity.getTransactionId(),
                    discrepancyType,
                    fileAmount,
                    dbAmount
                ));
            } else {
                matchedCount.incrementAndGet();
            }
        } else {
            discrepancies.add(new Discrepancy(
                entity.getTransactionId(),
                "Missing in Database"
            ));
        }
    }
} 