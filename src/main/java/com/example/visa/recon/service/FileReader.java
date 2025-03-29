package com.example.visa.recon.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.visa.recon.model.dto.VisaBase2Record;

/**
 * Service responsible for reading and processing transaction files.
 * Provides multiple methods for handling large files efficiently:
 * - Streaming records for memory-efficient processing
 * - Sequential processing for simple operations
 * - Parallel processing for improved performance
 * - Batch processing for database operations
 */
@Service
public class FileReader {
    private static final Logger logger = LoggerFactory.getLogger(FileReader.class);

    /**
     * Streams records from a file for memory-efficient processing.
     * Skips the header row and filters out empty lines.
     * 
     * @param filePath Path to the file to read
     * @return Stream of parsed VisaBase2Record objects
     * @throws RuntimeException if file cannot be read or closed
     */
    public Stream<VisaBase2Record> streamRecords(String filePath) {
        logger.info("Starting to stream records from file: {}", filePath);
        try {
            BufferedReader reader = Files.newBufferedReader(Paths.get(filePath));
            return reader.lines()
                        .skip(1)  // Skip header if exists
                        .filter(line -> !line.trim().isEmpty())
                        .map(this::parseRecord)
                        .onClose(() -> {
                            try {
                                reader.close();
                                logger.debug("File reader closed successfully");
                            } catch (IOException e) {
                                logger.error("Error closing file reader", e);
                                throw new RuntimeException(e);
                            }
                        });
        } catch (IOException e) {
            logger.error("Error streaming file: {}", filePath, e);
            throw new RuntimeException("Error streaming file: " + filePath, e);
        }
    }

    /**
     * Parses a CSV line into a VisaBase2Record object.
     * Expects a comma-separated string with at least 42 fields.
     * 
     * @param line The CSV line to parse
     * @return Parsed VisaBase2Record object, or null if parsing fails
     */
    private VisaBase2Record parseRecord(String line) {
        try {
            String[] fields = line.split(",");
            if (fields.length < 40) {
                logger.error("Invalid record format: {}", line);
                throw new IllegalArgumentException("Invalid record format: " + line);
            }
            VisaBase2Record record = new VisaBase2Record(
                fields[0].trim(),
                fields[1].trim(),
                fields[2].trim(),
                fields[3].trim(),
                fields[4].trim(),
                fields[5].trim(),
                fields[6].trim(),
                fields[7].trim(),
                fields[8].trim(),
                fields[9].trim(),
                fields[10].trim(),
                fields[11].trim(),
                fields[12].trim(),
                fields[13].trim(),
                fields[14].trim(),
                fields[15].trim(),
                fields[16].trim(),
                fields[17].trim(),
                fields[18].trim(),
                fields[19].trim(),
                fields[20].trim(),
                fields[21].trim(),
                fields[22].trim(),
                fields[23].trim(),
                fields[24].trim(),
                fields[25].trim(),
                fields[26].trim(),
                fields[27].trim(),
                fields[28].trim(),
                fields[29].trim(),
                fields[30].trim(),
                fields[31].trim(),
                fields[32].trim(),
                fields[33].trim(),
                fields[34].trim(),
                fields[35].trim(),
                fields[36].trim(),
                fields[37].trim(),
                fields[38].trim(),
                fields[39].trim(),
                fields[40].trim(), 
                fields[41].trim()
            );
            logger.trace("Successfully parsed record with ID: {}", record.getTransactionId());
            return record;
        } catch (Exception e) {
            logger.error("Error parsing record: {}", line, e);
            return null;
        }
    }

    /**
     * Processes a large file sequentially, applying a consumer to each record.
     * Suitable for operations that need to be performed in order.
     * 
     * @param filePath Path to the file to process
     * @param recordProcessor Consumer to process each record
     * @throws RuntimeException if file cannot be read
     */
    public void processLargeFile(String filePath, Consumer<VisaBase2Record> recordProcessor) {
        logger.info("Starting sequential processing of file: {}", filePath);
        long startTime = System.currentTimeMillis();
        final AtomicInteger recordCount = new AtomicInteger(0);

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(filePath))) {
            reader.lines()
                 .skip(1)  // Skip header if exists
                 .filter(line -> !line.trim().isEmpty())
                 .map(this::parseRecord)
                 .forEach(record -> {
                     recordProcessor.accept(record);
                     int count = recordCount.incrementAndGet();
                     if (count % 1000 == 0) {
                         logger.debug("Processed {} records", count);
                     }
                 });
        } catch (IOException e) {
            logger.error("Error processing file: {}", filePath, e);
            throw new RuntimeException("Error processing file: " + filePath, e);
        }

        long endTime = System.currentTimeMillis();
        logger.info("Completed processing {} records in {} ms", 
            recordCount.get(), (endTime - startTime));
    }

    /**
     * Processes a large file in parallel using Java streams.
     * Suitable for operations that can be performed independently.
     * 
     * @param filePath Path to the file to process
     * @param recordProcessor Consumer to process each record
     */
    public void processLargeFileParallel(String filePath, Consumer<VisaBase2Record> recordProcessor) {
        logger.info("Starting parallel processing of file: {}", filePath);
        long startTime = System.currentTimeMillis();
        final AtomicInteger recordCount = new AtomicInteger(0);

        streamRecords(filePath)
            .parallel()
            .forEach(record -> {
                recordProcessor.accept(record);
                int count = recordCount.incrementAndGet();
                if (count % 1000 == 0) {
                    logger.debug("Processed {} records", count);
                }
            });

        long endTime = System.currentTimeMillis();
        logger.info("Completed parallel processing of {} records in {} ms", 
            recordCount.get(), (endTime - startTime));
    }

    /**
     * Collects transaction IDs from a file.
     * Useful for quick validation or indexing operations.
     * 
     * @param filePath Path to the file to process
     * @return List of transaction IDs
     */
    public List<String> collectField1Values(String filePath) {
        logger.info("Collecting transaction IDs from file: {}", filePath);
        long startTime = System.currentTimeMillis();

        List<String> result = streamRecords(filePath)
            .map(record -> record.getTransactionId())
            .collect(Collectors.toList());

        long endTime = System.currentTimeMillis();
        logger.info("Collected {} transaction IDs in {} ms", 
            result.size(), (endTime - startTime));
        return result;
    }

    /**
     * Processes a file in batches, grouping records for efficient processing.
     * Suitable for database operations or other batch-oriented tasks.
     * 
     * @param filePath Path to the file to process
     * @param batchSize Size of each batch
     * @param batchProcessor Consumer to process each batch of records
     * @throws RuntimeException if file cannot be read
     */
    /**
     * Processes a file in batches, grouping records for efficient processing.
     * Uses buffered streaming and parallel processing for improved performance.
     * 
     * @param filePath Path to the file to process
     * @param batchSize Size of each batch
     * @param batchProcessor Consumer to process each batch of records
     * @throws RuntimeException if file cannot be read
     */
    public void processByBatch(String filePath, int batchSize, Consumer<List<VisaBase2Record>> batchProcessor) {
        logger.info("Starting optimized batch processing of file: {} with batch size: {}", filePath, batchSize);
        long startTime = System.currentTimeMillis();
        final AtomicInteger recordCount = new AtomicInteger(0);
        final AtomicInteger batchCount = new AtomicInteger(0);

        try {
            List<VisaBase2Record> batch = new ArrayList<>(batchSize);
            
            streamRecords(filePath)
                .parallel()
                .forEach(record -> {
                    batch.add(record);
                    recordCount.incrementAndGet();
                    
                    if (batch.size() >= batchSize) {
                        processBatch(batch, batchProcessor, batchCount);
                        batch.clear();
                    }
                });

            // Process remaining records
            if (!batch.isEmpty()) {
                processBatch(batch, batchProcessor, batchCount);
            }

        } catch (Exception e) {
            logger.error("Error processing file in batches: {}", filePath, e);
            throw new RuntimeException("Error processing file: " + filePath, e);
        }

        long endTime = System.currentTimeMillis();
        logger.info("Completed batch processing: {} records in {} batches, took {} ms", 
            recordCount.get(), batchCount.get(), (endTime - startTime));
    }

    private void processBatch(List<VisaBase2Record> batch, 
                            Consumer<List<VisaBase2Record>> batchProcessor,
                            AtomicInteger batchCount) {
        List<VisaBase2Record> batchCopy = new ArrayList<>(batch);
        batchProcessor.accept(batchCopy);
        int count = batchCount.incrementAndGet();
        logger.debug("Processed batch {} with {} records", count, batchCopy.size());
    }
    public void processByBatch1(String filePath, int batchSize, Consumer<List<VisaBase2Record>> batchProcessor) {
        logger.info("Starting batch processing of file: {} with batch size: {}", filePath, batchSize);
        long startTime = System.currentTimeMillis();
        final AtomicInteger batchCount = new AtomicInteger(0);

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(filePath))) {
            reader.lines()
                 .skip(1)
                 .filter(line -> !line.trim().isEmpty())
                 .map(this::parseRecord)
                 .collect(Collectors.groupingBy(record -> 
                     ThreadLocalRandom.current().nextInt(batchSize)))
                 .values()
                 .forEach(batch -> {
                     batchProcessor.accept(batch);
                     int count = batchCount.incrementAndGet();
                     logger.debug("Processed batch {} with {} records", count, batch.size());
                 });
        } catch (IOException e) {
            logger.error("Error processing file in batches: {}", filePath, e);
            throw new RuntimeException("Error processing file: " + filePath, e);
        }

        long endTime = System.currentTimeMillis();
        logger.info("Completed batch processing of {} batches in {} ms", 
            batchCount.get(), (endTime - startTime));
    }
}