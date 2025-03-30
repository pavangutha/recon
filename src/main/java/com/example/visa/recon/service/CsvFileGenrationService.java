package com.example.visa.recon.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.visa.recon.model.dto.VisaBase2Record;
import com.example.visa.recon.model.entity.VisaBase2RecordEntity;
import com.example.visa.recon.model.enums.TransactionType;
import com.example.visa.recon.repository.VisaBase2RecordRepository;
import com.example.visa.recon.mapper.VisaBase2RecordMapper;

@Service
public class CsvFileGenrationService {
    private static final Logger logger = LoggerFactory.getLogger(CsvFileGenrationService.class);
    private static final int BUFFER_SIZE = 8192; // 8KB buffer
    private static final int BATCH_SIZE = 1000;
    private static final AtomicInteger sequenceNumber = new AtomicInteger(0);
    private static final AtomicInteger transactionIdCounter = new AtomicInteger(0);
    
    // Thread-safe date formatters
    private static final ThreadLocal<SimpleDateFormat> yearFormat = 
        ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy"));
    private static final ThreadLocal<SimpleDateFormat> dayOfYearFormat = 
        ThreadLocal.withInitial(() -> new SimpleDateFormat("DDD"));
    
    // Predefined values for better performance
    private static final String[] CARD_NUMBERS = {
        // Visa cards (starting with 4)
        "4234567812345670", "4234567812345671", "4234567812345672", 
        "4234567812345673", "4234567812345674", "4234567812345675",
        "4532756278912345", "4532756278912346", "4532756278912347",
        "4532756278912348", "4532756278912349",
        
        // Mastercard (starting with 5)
        //"5234567890123456", "5234567890123457", "5234567890123458",
        //"5234567890123459", "5234567890123460",
        //"5532756278912345", "5532756278912346", "5532756278912347",
        //"5532756278912348", "5532756278912349",
        
        // American Express (starting with 3)
        //"343456789012345", "343456789012346", "343456789012347",
        //"343456789012348", "343456789012349",
        
        // RuPay (starting with 6)
        //"6234567890123456", "6234567890123457", "6234567890123458",
        //"6234567890123459", "6234567890123460",
        
        // Debit cards (starting with 4)
        "4024007198765432", "4024007198765433", "4024007198765434",
        "4024007198765435", "4024007198765436",
        
        // Credit cards (starting with 4)
        "4532756278912345", "4532756278912346", "4532756278912347",
        "4532756278912348", "4532756278912349",
        
        // Corporate cards (starting with 5)
        //"5532756278912345", "5532756278912346", "5532756278912347",
        //"5532756278912348", "5532756278912349",
        
        // Prepaid cards (starting with 4)
        "4024007198765432", "4024007198765433", "4024007198765434",
        "4024007198765435", "4024007198765436"
    };
    private static final String[] MERCHANT_NAMES = {
        "SuperMart", "QuickStore", "MegaShop", "GlobalRetail", "LocalMarket"
    };
    private static final String[] CARDHOLDER_NAMES = {
        "Ravi Kumar", "Priya Sharma", "Amit Patel", "Neha Singh", "Rajesh Verma"
    };

    @Autowired
    private final VisaBase2RecordRepository repository;
    
    @Autowired
    private final VisaBase2RecordMapper mapper;

    public CsvFileGenrationService(VisaBase2RecordRepository repository, VisaBase2RecordMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional
    public String generateCsvFile(String filePath, int totalRecords) throws IOException {
        logger.info("Starting CSV file generation with {} records", totalRecords);
        long startTime = System.currentTimeMillis();
        
        File file = new File(filePath);
        AtomicInteger processedCount = new AtomicInteger(0);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file), BUFFER_SIZE)) {
            // Write Header
            writeHeader(writer);

            // Process records in batches
            for (int i = 0; i < totalRecords; i += BATCH_SIZE) {
                int currentBatchSize = Math.min(BATCH_SIZE, totalRecords - i);
                List<VisaBase2RecordEntity> batchEntities = new ArrayList<>(currentBatchSize);

                // Generate and process batch
                List<VisaBase2Record> records = Stream.iterate(i, n -> n + 1)
                    .limit(currentBatchSize)
                    .parallel()
                    .map(recordNumber -> {
                        try {
                            VisaBase2Record record = generateRandomRecord(recordNumber + 1);
                            return record;
                        } catch (Exception e) {
                            logger.error("Error generating record {}: {}", recordNumber, e.getMessage(), e);
                            throw new RuntimeException(e);
                        }
                    })
                    .collect(Collectors.toList());

                // Write records to CSV and collect entities
                for (VisaBase2Record record : records) {
                    try {
                        writer.write(recordToCsv(record));
                        writer.newLine();
                        batchEntities.add(mapper.toEntity(record));
                        
                        int count = processedCount.incrementAndGet();
                        if (count % 10000 == 0) {
                            logger.info("Processed {} records", count);
                        }
                    } catch (Exception e) {
                        logger.error("Error writing record {}: {}", record.getTransactionId(), e.getMessage(), e);
                        throw new RuntimeException(e);
                    }
                }

                // Save batch to database
                if (!batchEntities.isEmpty()) {
                    try {
                        repository.saveAll(batchEntities);
                        logger.debug("Saved batch of {} records to database", batchEntities.size());
                    } catch (Exception e) {
                        logger.error("Error saving batch to database: {}", e.getMessage(), e);
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        long endTime = System.currentTimeMillis();
        logger.info("CSV file generation completed: {} records in {} ms", 
            processedCount.get(), (endTime - startTime));
        
        return "CSV file generated successfully with " + processedCount.get() + " records";
    }

    private void writeHeader(BufferedWriter writer) throws IOException {
        writer.write("transactionType,transactionId,cardNumber,amount,Stan,currencyCode,transactionDate,transactionTime," +
                "responseCode,accountType,authorizationCode,merchantId,merchantCategoryCode,terminalId,cardExpiryDate," +
                "cardholderName,accountHolderName,transactionFee,authorizationIndicator,acquirerBin,issuerBin," +
                "merchantName,transactionCode,reasonCode,rrn,originalTransactionId,acquirerReferenceNumber,batchNumber," +
                "dateOfSettlement,settlementAmount,issuerResponseCode,transactionOrigin,transactionReference," +
                "originalTransactionAmount,refundAmount,adjustmentAmount,loyaltyPointsEarned,loyaltyPointsRedeemed," +
                "reversalIndicator,authorizationDateTime,originalAuthorizationCode,narrative");
        writer.newLine();
    }

    private String recordToCsv(VisaBase2Record record) {
        return String.join(",", 
            record.getTransactionType(),
            record.getTransactionId(),
            record.getCardNumber(),
            record.getAmount(),
            record.getStan(),
            record.getCurrencyCode(),
            record.getTransactionDate(),
            record.getTransactionTime(),
            record.getResponseCode(),
            record.getAccountType(),
            record.getAuthorizationCode(),
            record.getMerchantId(),
            record.getMerchantCategoryCode(),
            record.getTerminalId(),
            record.getCardExpiryDate(),
            record.getCardholderName(),
            record.getAccountHolderName(),
            record.getTransactionFee(),
            record.getAuthorizationIndicator(),
            record.getAcquirerBin(),
            record.getIssuerBin(),
            record.getMerchantName(),
            record.getTransactionCode(),
            record.getReasonCode(),
            record.getRrn(),
            record.getOriginalTransactionId(),
            record.getAcquirerReferenceNumber(),
            record.getBatchNumber(),
            record.getDateOfSettlement(),
            record.getSettlementAmount(),
            record.getIssuerResponseCode(),
            record.getTransactionOrigin(),
            record.getTransactionReference(),
            record.getOriginalTransactionAmount(),
            record.getRefundAmount(),
            record.getAdjustmentAmount(),
            record.getLoyaltyPointsEarned(),
            record.getLoyaltyPointsRedeemed(),
            record.getReversalIndicator(),
            record.getAuthorizationDateTime(),
            record.getOriginalAuthorizationCode(),
            record.getNarrative()
        );
    }

    private VisaBase2Record generateRandomRecord(int recordNumber) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        Date now = new Date();
        
        // Generate unique transaction ID using timestamp, record number, and counter
        String timestamp = String.valueOf(System.currentTimeMillis());
        String counter = String.format("%06d", transactionIdCounter.incrementAndGet());
        String transactionId = String.format("TXN%s%s", timestamp, counter);
        
        // Generate RRN
        String rrn = generateRRN(now);
        
        // Generate random amount between 100 and 10000
        double amount = random.nextDouble(100, 10000);
        String formattedAmount = String.format("%.2f", amount);
        
        // Select random values from predefined arrays
        String cardNumber = CARD_NUMBERS[random.nextInt(CARD_NUMBERS.length)];
        String merchantName = MERCHANT_NAMES[random.nextInt(MERCHANT_NAMES.length)];
        String cardholderName = CARDHOLDER_NAMES[random.nextInt(CARDHOLDER_NAMES.length)];
        
        // Generate random authorization code
        String authCode = String.format("AUTH%04d", random.nextInt(10000));
        
        return new VisaBase2Record(
            TransactionType.values()[random.nextInt(TransactionType.values().length)].name(),
            transactionId,
            cardNumber,
            formattedAmount,
            String.valueOf(random.nextInt(1000000)),
            "356",
            "2025-03-23",
            "10:00:00",
            "00",
            "Savings",
            authCode,
            "MERCHANT" + random.nextInt(100),
            "5411",
            "TERM" + random.nextInt(10),
            "12/25",
            cardholderName,
            cardholderName,
            "2.50",
            "A",
            "123456",
            "654321",
            merchantName,
            "SALE",
            "00",
            rrn,
            "",
            "123ABC",
            "BATCH001",
            "2025-03-24",
            formattedAmount,
            "00",
            "Online",
            "REF" + random.nextInt(10000),
            "0",
            "0",
            "0",
            "10",
            "5",
            "N",
            "2025-03-23T10:00:00",
            "",
            "Transaction successful"
        );
    }

    private String generateRRN(Date date) {
        // Get year and day of year using thread-safe formatters
        String year = yearFormat.get().format(date);
        String dayOfYear = dayOfYearFormat.get().format(date);
        
        // Get sequence number (reset daily at midnight)
        int seqNumber = sequenceNumber.incrementAndGet();
        if (seqNumber > 9999) {
            sequenceNumber.set(1);
            seqNumber = 1;
        }
        
        // Format the sequence number as a 4-digit number
        String sequenceStr = String.format("%04d", seqNumber);
        
        // Construct the RRN in format YYYYDDDSSSS
        return year + dayOfYear + sequenceStr;
    }
}

