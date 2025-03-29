# Visa Transaction Reconciliation Application - Technical Documentation
# Visa Transaction Reconciliation Application - Technical Documentation

## 1. Overview

The Visa Transaction Reconciliation Application is a Spring Boot-based system designed to reconcile transactions between a switch system and network (Visa/RuPay) transactions. The application provides REST APIs for data ingestion, reconciliation processing, and report generation.

## 2. System Architecture

### 2.1 Core Components

- **Controllers**: 
  - ReconciliationController: Handles reconciliation API endpoints
  - Exposes endpoints for data ingestion, reconciliation, and report generation

- **Services**:
  - TwoWayBatchReconciliationService: Handles batch reconciliation processing
  - CsvFileGenrationService: Manages CSV file operations
  - ReconciliationJobService: Manages reconciliation jobs
  - DiscrepancyReportService: Generates discrepancy reports

- **Models**:
  - VisaBase2Transaction: Entity for storing Visa transactions
  - VisaBase2Record: DTO for transaction data transfer
  - VisaBase2RecordEntity: Entity for Base2 record storage

- **Configuration**:
  - ThreadPoolConfig: Configures async execution with customizable thread pools
  - Supports parallel processing with configurable core/max pool sizes

### 2.2 Key Features

1. **Data Ingestion**
   - REST API endpoint for transaction data ingestion
   - Support for CSV file processing
   - Data validation and transformation

2. **Two-Way Reconciliation**
   - Batch processing with configurable batch sizes
   - Parallel processing using thread pools
   - Comprehensive matching algorithms

3. **Report Generation**
   - Detailed discrepancy reporting
   - Excel report generation
   - Configurable report formats and paths

## 3. Data Model

### 3.1 VisaBase2Transaction Entity

Key fields:
- id (Long): Primary key
- transactionType (String)
- transactionId (String, unique)
- cardNumber (String)
- amount (BigDecimal)
- currencyCode (String)
- transactionDate (LocalDateTime)

### 3.2 VisaBase2Record Entity

Additional fields:
- stan (String)
- responseCode (String)
- accountType (String)
- authorizationCode (String)
- merchantId (String)
- merchantCategoryCode (String)
- terminalId (String)
- cardExpiryDate (String)

## 4. API Endpoints

1. POST /dataingestion
   - Processes incoming transaction data
   - Accepts JSON payload with filepath and total records

2. POST /reconcile
   - Triggers reconciliation process
   - Returns job ID for tracking

3. POST /reconcile/two-way
   - Performs two-way batch reconciliation
   - Configurable batch size and report path

## 1. Overview

The Visa Transaction Reconciliation Application is a Spring Boot-based system designed to reconcile transactions between a switch system and network (Visa/RuPay) transactions. The application processes transaction data, identifies discrepancies, and generates detailed reconciliation reports.

## 2. System Architecture

### 2.1 Core Components

- **Controllers**: Handle HTTP requests and API endpoints
- **Services**: Implement business logic and data processing
- **Repositories**: Manage database operations
- **Models**: Define data structures (DTOs and Entities)
- **Exceptions**: Handle error scenarios
- **Mappers**: Convert between DTOs and Entities

### 2.2 Key Services

1. **ReconciliationService**
   - Orchestrates the reconciliation process
   - Scheduled to run daily at 11 PM
   - Generates and saves reconciliation reports

2. **DiscrepancyReportService**
   - Analyzes transactions for discrepancies
   - Categorizes discrepancies into types:
     - Missing transactions
     - Amount mismatches
     - Duplicate transactions
     - Date mismatches
     - Other discrepancies
   - Generates detailed reports and statistics

3. **CsvFileGenrationService**
   - Handles CSV file generation and processing
   - Generates random transaction records
   - Manages database operations for transaction records

4. **TransactionMatcher**
   - Implements transaction matching algorithms
   - Supports exact and fuzzy matching
   - Calculates match scores based on multiple criteria

## 3. Data Model

### 3.1 Transaction Record (VisaBase2Record)

Key fields:
- transactionId (unique identifier)
- transactionType
- amount
- transactionDate
- transactionTime
- cardNumber
- merchantId
- responseCode
- [Additional fields as per Visa Base2 format]

### 3.2 Discrepancy Types

1. **Missing Transactions**
   - Transactions present in switch but not in network
   - Error Code: VISA-404

2. **Amount Mismatches**
   - Transactions with different amounts between systems
   - Includes amount difference calculation

3. **Duplicate Transactions**
   - Multiple occurrences of the same transaction ID
   - Error Code: VISA-409

4. **Date Mismatches**
   - Transactions with different dates between systems

## 4. API Endpoints

### 4.1 Data Ingestion
```
POST /api/dataingestion
Content-Type: application/json

Request Body:
{
    "filepath": "string",
    "totalrecords": "integer"
}
```

### 4.2 Reconciliation
```
POST /api/reconcile
Content-Type: application/json

Request Body: Transaction data for reconciliation
```

## 5. Error Handling

### 5.1 Exception Types

1. **BaseException**
   - Parent class for all custom exceptions
   - Includes error code and HTTP status

2. **FileProcessingException**
   - Error Code: VISA-502
   - HTTP Status: BAD_GATEWAY

3. **TransactionNotFoundException**
   - Error Code: VISA-404
   - HTTP Status: NOT_FOUND

4. **TransactionProcessingException**
   - Error Code: VISA-500
   - HTTP Status: INTERNAL_SERVER_ERROR

5. **TransactionValidationException**
   - Error Code: VISA-400
   - HTTP Status: BAD_REQUEST

6. **DuplicateTransactionException**
   - Error Code: VISA-409
   - HTTP Status: CONFLICT

### 5.2 Global Exception Handler

- Centralized exception handling
- Generates unique trace IDs
- Returns standardized error responses
- Logs errors with appropriate severity

## 6. Database Configuration

### 6.1 Properties
```yaml
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:visadb}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```

## 7. Transaction Processing

### 7.1 File Processing
- Supports CSV file format
- Streams records for efficient processing
- Implements batch processing capabilities
- Handles large file processing with parallel streams

### 7.2 Transaction Matching
- Exact matching based on transaction ID
- Fuzzy matching with configurable tolerance
- Match score calculation based on:
  - Transaction ID (30% weight)
  - Timestamp (35% weight)
  - Amount (35% weight)

## 8. Reporting

### 8.1 Report Format
```
=== Transaction Reconciliation Report ===
Generated at: [timestamp]

Summary:
--------
Total Missing Transactions: [count]
Total Amount Mismatches: [count]
Total Duplicate Transactions: [count]
Total Date Mismatches: [count]
Other Discrepancies: [count]

[Detailed sections for each discrepancy type]
```

### 8.2 Statistics
- Total number of discrepancies
- Counts by category
- Total amount discrepancy
- Transaction volume metrics

## 9. Security Considerations

- Environment variable-based configuration
- No hardcoded credentials
- Input validation and sanitization
- Exception handling with secure error messages

## 10. Performance Considerations

- Parallel processing for large datasets
- Batch processing capabilities
- Efficient data structures for lookups
- Streaming for file operations

## 11. Dependencies

- Spring Boot
- Spring Data JPA
- PostgreSQL Driver
- Lombok
- SLF4J for logging

## 12. Future Enhancements

1. **Additional Features**
   - Support for more file formats
   - Enhanced matching algorithms
   - Real-time reconciliation
   - API for report retrieval

2. **Monitoring and Metrics**
   - Performance monitoring
   - Transaction volume tracking
   - Error rate monitoring
   - System health checks

3. **Integration**
   - External system integration
   - Notification system
   - Audit logging
   - User management 