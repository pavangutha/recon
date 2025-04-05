# Visa Reconciliation System

A robust Spring Boot application for performing two-way reconciliation between CSV files and database records for Visa transactions.

## Features

### 1. CSV File Generation
- Generates test data CSV files with realistic transaction records
- Supports parallel processing for high-performance generation
- Configurable batch size and buffer size
- Thread-safe random data generation
- Predefined card numbers, merchant names, and cardholder names
- Unique transaction IDs and RRNs (Reference Retrieval Numbers)

### 2. Two-Way Reconciliation
- File to Database reconciliation
- Database to File reconciliation
- Batch processing for improved performance
- Parallel processing support
- Comprehensive discrepancy detection:
  - Amount mismatches
  - Response code mismatches
  - Authorization code mismatches
  - Transaction date mismatches
  - Missing records

### 3. Report Generation
- Detailed Excel reports with multiple sheets:
  - Summary statistics
  - File to Database discrepancies
  - Database to File discrepancies
  - Detailed breakdown by discrepancy type
- Color-coded cells for better visualization
- Comprehensive statistics tracking

### 4. Scheduling Support
- Configurable scheduling using cron expressions
- Automatic reconciliation runs
- Configurable file paths and batch sizes
- Error handling and logging

## Technical Stack

- **Framework**: Spring Boot
- **Database**: MySQL
- **ORM**: Spring Data JPA
- **File Processing**: Apache POI (Excel), CSV
- **Build Tool**: Maven
- **Java Version**: 17

## Project Structure

```
src/main/java/com/example/visa/recon/
├── config/
│   ├── SchedulingConfig.java
│   └── ThreadPoolConfig.java
├── controller/
│   └── ReconciliationController.java
├── model/
│   ├── dto/
│   │   └── VisaBase2Record.java
│   ├── entity/
│   │   └── VisaBase2RecordEntity.java
│   └── enums/
│       └── TransactionType.java
├── repository/
│   └── VisaBase2RecordRepository.java
├── service/
│   ├── CsvFileGenrationService.java
│   ├── ExcelReportGenerator.java
│   └── TwoWayBatchReconciliationService.java
└── scheduler/
    └── ReconciliationScheduler.java
```

## Configuration

### Application Properties
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/visa_recon
    username: root
    password: root
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true

reconciliation:
  file:
    input-path: D:/input/visa_base2.csv
    output-path: D:/output/reconciliation_report.xlsx
  batch:
    size: 1000
  scheduler:
    cron: "0 0 1 * * ?"  # Run at 1 AM daily
    enabled: true

thread-pool:
  core-size: 4
  max-size: 8
  queue-capacity: 100
  keep-alive-seconds: 60
```

## API Endpoints

### 1. Generate CSV File
```http
POST /api/generate-csv
Content-Type: application/json

{
    "filepath": "D:/output/visa_base2.csv",
    "totalRecords": 500
}
```

### 2. Perform Two-Way Reconciliation
```http
POST /api/reconcile/two-way
Content-Type: application/json

{
    "filepath": "D:/input/visa_base2.csv",
    "reportPath": "D:/output/reconciliation_report.xlsx",
    "batchSize": 1000
}
```

## Performance Optimizations

1. **Batch Processing**
   - Configurable batch size for database operations
   - Efficient memory usage with batch processing
   - Reduced database round trips

2. **Parallel Processing**
   - Thread pool configuration for concurrent operations
   - Parallel stream processing for record generation
   - Thread-safe collections and counters

3. **Memory Management**
   - Buffered file operations
   - Efficient string handling
   - Proper resource cleanup

4. **Database Optimization**
   - Batch save operations
   - Indexed queries
   - Transaction management

## Error Handling

- Comprehensive exception handling
- Detailed error logging
- Transaction rollback on failures
- Graceful error recovery

## Logging

- SLF4J with Logback
- Different log levels for different environments
- Detailed transaction logging
- Performance metrics logging

## Setup and Installation

1. **Prerequisites**
   - Java 17 or higher
   - MySQL 8.0 or higher
   - Maven 3.6 or higher

2. **Database Setup**
   ```sql
   CREATE DATABASE visa_recon;
   ```

3. **Build and Run**
   ```bash
   mvn clean install
   java -jar target/visa-recon-0.0.1-SNAPSHOT.jar
   ```

## Usage Examples

### 1. Generate Test Data
```bash
curl -X POST http://localhost:8080/api/generate-csv \
  -H "Content-Type: application/json" \
  -d '{"filepath": "D:/output/visa_base2.csv", "totalRecords": 500}'
```

### 2. Perform Reconciliation
```bash
curl -X POST http://localhost:8080/api/reconcile/two-way \
  -H "Content-Type: application/json" \
  -d '{
    "filepath": "D:/input/visa_base2.csv",
    "reportPath": "D:/output/reconciliation_report.xlsx",
    "batchSize": 1000
  }'
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Redis Integration

### Use Cases for Redis in the Project

1. **Transaction ID Caching**
   - Cache frequently accessed transaction IDs
   - Reduce database load during reconciliation
   - Store mapping between transaction IDs and their status
   - Example: `transaction:{id} -> {status, lastUpdated}`

2. **Batch Processing State**
   - Store batch processing progress
   - Track completed batches
   - Maintain reconciliation state
   - Example: `batch:{batchId} -> {status, processedCount, totalCount}`

3. **Report Generation Caching**
   - Cache generated reports temporarily
   - Store report metadata
   - Enable quick report retrieval
   - Example: `report:{reportId} -> {metadata, status}`

4. **Performance Metrics**
   - Store real-time processing statistics
   - Track reconciliation performance
   - Monitor system health
   - Example: `metrics:{date} -> {processedCount, errorCount, avgProcessingTime}`

5. **Lock Management**
   - Implement distributed locking for batch processing
   - Prevent concurrent reconciliation runs
   - Manage resource access
   - Example: `lock:reconciliation:{batchId}`

### Redis Configuration

```yaml
spring:
  redis:
    host: localhost
    port: 6379
    password: your_password
    database: 0
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0
        max-wait: -1ms
```

### Redis Implementation Examples

1. **Transaction Caching Service**
```java
@Service
public class TransactionCacheService {
    private final RedisTemplate<String, String> redisTemplate;
    private static final String TRANSACTION_KEY_PREFIX = "transaction:";
    
    public void cacheTransaction(String transactionId, String status) {
        String key = TRANSACTION_KEY_PREFIX + transactionId;
        redisTemplate.opsForValue().set(key, status, 24, TimeUnit.HOURS);
    }
    
    public String getTransactionStatus(String transactionId) {
        String key = TRANSACTION_KEY_PREFIX + transactionId;
        return redisTemplate.opsForValue().get(key);
    }
}
```

2. **Batch Processing State Management**
```java
@Service
public class BatchStateService {
    private final RedisTemplate<String, String> redisTemplate;
    private static final String BATCH_KEY_PREFIX = "batch:";
    
    public void updateBatchProgress(String batchId, int processed, int total) {
        String key = BATCH_KEY_PREFIX + batchId;
        Map<String, String> state = new HashMap<>();
        state.put("processed", String.valueOf(processed));
        state.put("total", String.valueOf(total));
        state.put("status", "IN_PROGRESS");
        redisTemplate.opsForHash().putAll(key, state);
    }
}
```

3. **Distributed Lock Implementation**
```java
@Service
public class ReconciliationLockService {
    private final RedisTemplate<String, String> redisTemplate;
    private static final String LOCK_KEY_PREFIX = "lock:reconciliation:";
    
    public boolean acquireLock(String batchId, long timeoutSeconds) {
        String key = LOCK_KEY_PREFIX + batchId;
        return redisTemplate.opsForValue()
            .setIfAbsent(key, "LOCKED", timeoutSeconds, TimeUnit.SECONDS);
    }
    
    public void releaseLock(String batchId) {
        String key = LOCK_KEY_PREFIX + batchId;
        redisTemplate.delete(key);
    }
}
```

### Benefits of Redis Integration

1. **Performance Improvements**
   - Reduced database load
   - Faster data retrieval
   - Lower latency for frequently accessed data
   - Improved response times

2. **Scalability**
   - Better handling of concurrent requests
   - Distributed processing support
   - Improved resource utilization
   - Enhanced system reliability

3. **State Management**
   - Reliable state tracking
   - Better error recovery
   - Improved monitoring capabilities
   - Enhanced debugging support

4. **Resource Optimization**
   - Reduced memory usage
   - Better CPU utilization
   - Optimized network traffic
   - Improved system efficiency

### Implementation Steps

1. **Add Redis Dependencies**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

2. **Configure Redis Connection**
```java
@Configuration
public class RedisConfig {
    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        return template;
    }
}
```

3. **Integrate Redis Services**
   - Add Redis services to existing components
   - Implement caching strategies
   - Add state management
   - Configure monitoring

## Discrepancy Report Structure

### 1. Summary Sheet
```excel
Sheet Name: Summary
Columns:
- Total Records Processed
- Total Matched Records
- Total Discrepancies
- File to DB Discrepancies
- DB to File Discrepancies
- Processing Start Time
- Processing End Time
- Total Processing Duration
- Status (Success/Failed)
```

### 2. File to Database Discrepancies
```excel
Sheet Name: FileToDB_Discrepancies
Columns:
- Transaction ID
- File Amount
- DB Amount
- File Response Code
- DB Response Code
- File Auth Code
- DB Auth Code
- File Transaction Date
- DB Transaction Date
- Discrepancy Type
- Status
- Remarks
```

### 3. Database to File Discrepancies
```excel
Sheet Name: DBToFile_Discrepancies
Columns:
- Transaction ID
- DB Amount
- File Amount
- DB Response Code
- File Response Code
- DB Auth Code
- File Auth Code
- DB Transaction Date
- File Transaction Date
- Discrepancy Type
- Status
- Remarks
```

### 4. Detailed Breakdown Sheet
```excel
Sheet Name: Detailed_Breakdown
Columns:
- Discrepancy Type
- Count
- Percentage
- Example Transaction IDs
- Impact Level (High/Medium/Low)
```

### Report Formatting

1. **Color Coding**
   - Green: Matched records
   - Red: Amount mismatches
   - Yellow: Response code mismatches
   - Orange: Authorization code mismatches
   - Blue: Date mismatches
   - Purple: Missing records

2. **Conditional Formatting**
   - Amount differences > 10%: Bold red
   - Critical response codes: Highlighted yellow
   - Missing records: Highlighted purple
   - Date differences > 1 day: Highlighted blue

3. **Summary Statistics**
   - Pie charts for discrepancy distribution
   - Bar charts for trend analysis
   - Line charts for processing timeline

### Report Generation Code Example

```java
@Service
public class ExcelReportGenerator {
    private static final Logger logger = LoggerFactory.getLogger(ExcelReportGenerator.class);
    
    public void generateReport(String reportPath, ReconciliationResult result) {
        try (Workbook workbook = new XSSFWorkbook()) {
            // Create Summary Sheet
            Sheet summarySheet = workbook.createSheet("Summary");
            createSummarySheet(summarySheet, result);
            
            // Create File to DB Discrepancies Sheet
            Sheet fileToDBSheet = workbook.createSheet("FileToDB_Discrepancies");
            createFileToDBSheet(fileToDBSheet, result.getFileToDBDiscrepancies());
            
            // Create DB to File Discrepancies Sheet
            Sheet dbToFileSheet = workbook.createSheet("DBToFile_Discrepancies");
            createDBToFileSheet(dbToFileSheet, result.getDBToFileDiscrepancies());
            
            // Create Detailed Breakdown Sheet
            Sheet breakdownSheet = workbook.createSheet("Detailed_Breakdown");
            createBreakdownSheet(breakdownSheet, result);
            
            // Apply formatting
            applyFormatting(workbook);
            
            // Save workbook
            try (FileOutputStream fileOut = new FileOutputStream(reportPath)) {
                workbook.write(fileOut);
            }
            
            logger.info("Report generated successfully at: {}", reportPath);
        } catch (IOException e) {
            logger.error("Error generating report: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate report", e);
        }
    }
    
    private void createSummarySheet(Sheet sheet, ReconciliationResult result) {
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Metric");
        headerRow.createCell(1).setCellValue("Value");
        
        int rowNum = 1;
        createSummaryRow(sheet, rowNum++, "Total Records Processed", result.getTotalRecords());
        createSummaryRow(sheet, rowNum++, "Total Matched Records", result.getMatchedRecords());
        createSummaryRow(sheet, rowNum++, "Total Discrepancies", result.getTotalDiscrepancies());
        createSummaryRow(sheet, rowNum++, "File to DB Discrepancies", result.getFileToDBDiscrepancies().size());
        createSummaryRow(sheet, rowNum++, "DB to File Discrepancies", result.getDBToFileDiscrepancies().size());
        createSummaryRow(sheet, rowNum++, "Processing Start Time", result.getStartTime());
        createSummaryRow(sheet, rowNum++, "Processing End Time", result.getEndTime());
        createSummaryRow(sheet, rowNum++, "Total Duration", result.getDuration());
        createSummaryRow(sheet, rowNum++, "Status", result.getStatus());
    }
    
    private void createFileToDBSheet(Sheet sheet, List<Discrepancy> discrepancies) {
        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Transaction ID", "File Amount", "DB Amount", "File Response Code", 
                          "DB Response Code", "File Auth Code", "DB Auth Code", 
                          "File Transaction Date", "DB Transaction Date", 
                          "Discrepancy Type", "Status", "Remarks"};
        
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }
        
        // Create data rows
        int rowNum = 1;
        for (Discrepancy discrepancy : discrepancies) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(discrepancy.getTransactionId());
            row.createCell(1).setCellValue(discrepancy.getFileAmount());
            row.createCell(2).setCellValue(discrepancy.getDbAmount());
            // ... set other cells
        }
    }
    
    private void applyFormatting(Workbook workbook) {
        // Apply conditional formatting
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            Sheet sheet = workbook.getSheetAt(i);
            applyConditionalFormatting(sheet);
        }
    }
}
```

### Report Features

1. **Interactive Elements**
   - Filterable columns
   - Sortable data
   - Hidden columns for detailed view
   - Data validation

2. **Visual Elements**
   - Charts and graphs
   - Color-coded cells
   - Icons for status
   - Progress bars

3. **Data Organization**
   - Grouped by discrepancy type
   - Sorted by severity
   - Filtered by date range
   - Aggregated statistics

4. **Export Options**
   - Excel format
   - CSV format
   - PDF format
   - Custom formats

### Report Usage

1. **Analysis**
   - Identify patterns in discrepancies
   - Track reconciliation trends
   - Monitor system performance
   - Generate insights

2. **Audit Trail**
   - Record reconciliation history
   - Track resolution status
   - Maintain compliance records
   - Support investigations

3. **Performance Monitoring**
   - Track processing times
   - Monitor error rates
   - Analyze system efficiency
   - Identify bottlenecks