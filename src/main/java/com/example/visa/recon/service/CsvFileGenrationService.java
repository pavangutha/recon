package com.example.visa.recon.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.visa.recon.mapper.VisaBase2RecordMapper;
import com.example.visa.recon.model.dto.VisaBase2Record;
import com.example.visa.recon.model.entity.VisaBase2RecordEntity;
import com.example.visa.recon.model.enums.TransactionType;
import com.example.visa.recon.repository.VisaBase2RecordRepository;

@Service
public class CsvFileGenrationService {
    private static final Logger logger = LoggerFactory.getLogger(CsvFileGenrationService.class);
    private static final AtomicInteger sequenceNumber = new AtomicInteger(0);
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
        File file = new File(filePath);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            // Write Header
            writer.write("transactionType,transactionId,cardNumber,amount,Stan,currencyCode,transactionDate,transactionTime," +
                    "responseCode,accountType,authorizationCode,merchantId,merchantCategoryCode,terminalId,cardExpiryDate," +
                    "cardholderName,accountHolderName,transactionFee,authorizationIndicator,acquirerBin,issuerBin," +
                    "merchantName,transactionCode,reasonCode,rrn,originalTransactionId,acquirerReferenceNumber,batchNumber," +
                    "dateOfSettlement,settlementAmount,issuerResponseCode,transactionOrigin,transactionReference," +
                    "originalTransactionAmount,refundAmount,adjustmentAmount,loyaltyPointsEarned,loyaltyPointsRedeemed," +
                    "reversalIndicator,authorizationDateTime,originalAuthorizationCode,narrative");
            writer.newLine();
            // Generate and write 100,000 records
            for (int i = 0; i < totalRecords; i++) {
                try {
                    VisaBase2Record record = generateRandomRecord(i + 1);
                    writer.write(recordToCsv(record));
                    VisaBase2RecordEntity entity = mapper.toEntity(record);
                    System.out.println("Stan: in service "+entity.getStan());
                    repository.saveAndFlush(entity);
                    if (i % 100 == 0) {
                        logger.info("Processed {} records", i);
                    }
                    writer.newLine();
                } catch (Exception e) {
                    logger.error("Error processing record {}: {}", i, e.getMessage(), e);
                    throw e;
                }
            }
            
            // Write Trailer with total count
            writer.write("TRAILER," + totalRecords); // You can customize the trailer
            writer.newLine();
        }
        return "CSV file generated successfully with " + totalRecords + " records";
    }

    private static String recordToCsv(VisaBase2Record record) {
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

    private static VisaBase2Record generateRandomRecord(int index) {
        Random rand = new Random();
        return VisaBase2Record.builder()
                .transactionType(TransactionType.values()[rand.nextInt(TransactionType.values().length)].name())
                .transactionId(String.valueOf("1"+System.nanoTime() + rand.nextInt(1000)))
                .cardNumber("423456781234567" + rand.nextInt(10))
                .amount(String.format("%.2f", rand.nextFloat() * 1000))
                .Stan(String.valueOf(rand.nextInt(1000000)))
                .currencyCode("356")
                .transactionDate("2025-03-23")
                .transactionTime("10:00:00")
                .responseCode("00")
                .accountType("Savings")
                .authorizationCode("AUTH" + rand.nextInt(1000))
                .merchantId("MERCHANT" + rand.nextInt(100))
                .merchantCategoryCode("5411")
                .terminalId("TERM" + rand.nextInt(10))
                .cardExpiryDate("12/25")
                .cardholderName("John Doe")
                .accountHolderName("John Doe")
                .transactionFee("2.50")
                .authorizationIndicator("A")
                .acquirerBin("123456")
                .issuerBin("654321")
                .merchantName("SuperMart")
                .transactionCode("SALE")
                .reasonCode("00")   
                .rrn(generateRRN())
                .originalTransactionId("")
                .acquirerReferenceNumber("123ABC")
                .batchNumber("BATCH001")
                .dateOfSettlement("2025-03-24")
                .settlementAmount(String.format("%.2f", rand.nextFloat() * 1000))
                .issuerResponseCode("00")
                .transactionOrigin("Online")
                .transactionReference("REF" + rand.nextInt(10000))
                .originalTransactionAmount("0")
                .refundAmount("0")
                .adjustmentAmount("0")
                .loyaltyPointsEarned("10")
                .loyaltyPointsRedeemed("5")
                .reversalIndicator("N")
                .authorizationDateTime("2025-03-23T10:00:00")
                .originalAuthorizationCode("")
                .narrative("Transaction successful")
                .build();
    }
    public static String generateRRN() {
        // Get current date
        Date now = new Date();
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
        SimpleDateFormat dayOfYearFormat = new SimpleDateFormat("DDD");

        // Get year and day of year
        String year = yearFormat.format(now);
        String dayOfYear = dayOfYearFormat.format(now);

        // Get sequence number (reset daily at midnight)
        int seqNumber = sequenceNumber.incrementAndGet();
        if (seqNumber > 9999) {
            sequenceNumber.set(1);  // Reset sequence if it exceeds 9999
            seqNumber = 1;
        }

        // Format the sequence number as a 4-digit number
        String sequenceStr = String.format("%04d", seqNumber);

        // Construct the RRN in format YYYYDDDSSSS
        return year + dayOfYear + sequenceStr;
    }
}

