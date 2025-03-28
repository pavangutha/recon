# Visa Transaction Reconciliation System

A Spring Boot application for reconciling transactions between switch and network systems (Visa/RuPay). This system provides automated reconciliation capabilities with support for both file-based and database-based transaction processing.

## Features

- **Batch Processing**: Efficiently process large transaction files using Spring Batch
- **Transaction Matching**: 
  - Exact matching based on transaction IDs
  - Fuzzy matching with configurable tolerance levels
  - Parallel processing for improved performance
- **File Processing**:
  - CSV file generation with configurable record counts
  - Support for large file processing with streaming
  - Batch processing with memory-efficient operations
- **Discrepancy Detection**:
  - Amount mismatches
  - Missing transactions
  - Customizable discrepancy types
- **Reporting**:
  - Detailed reconciliation reports
  - Summary statistics
  - Transaction matching reports

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- MySQL 8.0 or higher
- Spring Boot 3.x

## Configuration

The application can be configured through `application.yml`:

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
  batch:
    jdbc:
      initialize-schema: always
    job:
      enabled: true

reconciliation:
  input:
    file: /path/to/input/file.csv
  output:
    file: /path/to/output/file.csv
```

## Database Schema

The application uses the following Spring Batch metadata tables:
- `BATCH_JOB_INSTANCE`
- `BATCH_JOB_EXECUTION`
- `BATCH_JOB_EXECUTION_PARAMS`
- `BATCH_STEP_EXECUTION`
- `BATCH_STEP_EXECUTION_CONTEXT`
- `BATCH_JOB_EXECUTION_CONTEXT`

## API Endpoints

### Data Ingestion
```
POST /api/dataingestion
Content-Type: application/json

{
    "filepath": "/path/to/output/file.csv",
    "totalrecords": 100000
}
```

### Reconciliation
```
POST /api/reconcile
Content-Type: application/json

{
    "filepath": "/path/to/input/file.csv"
}
```

## Project Structure

```
src/main/java/com/example/visa/recon/
├── batch/                 # Spring Batch components
├── config/               # Application configuration
├── controller/           # REST controllers
├── exception/           # Custom exceptions
├── mapper/              # DTO-Entity mappers
├── model/               # Data models
│   ├── dto/            # Data Transfer Objects
│   ├── entity/         # JPA entities
│   └── enums/          # Enumerations
├── repository/          # JPA repositories
└── service/            # Business logic
```

## Building and Running

1. Clone the repository:
```bash
git clone <repository-url>
cd visa-reconciliation
```

2. Build the project:
```bash
mvn clean install
```

3. Run the application:
```bash
mvn spring-boot:run
```

## Error Handling

The application includes comprehensive error handling with custom exceptions:
- `FileProcessingException`: For file-related errors
- `TransactionNotFoundException`: When a transaction is not found
- `TransactionProcessingException`: For processing errors
- `TransactionValidationException`: For validation errors
- `DuplicateTransactionException`: For duplicate transactions

## Logging

The application uses SLF4J with Logback for logging. Log levels can be configured in `application.yml`:

```yaml
logging:
  level:
    root: INFO
    com.example.visa.recon: DEBUG
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.