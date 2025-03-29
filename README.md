# Visa Reconciliation System

A high-performance Spring Boot application for reconciling Visa transaction records between file and database systems.

## Features

- Two-way reconciliation between file and database records
- Batch processing with optimized performance
- Parallel processing with configurable thread pools
- Automated scheduling of reconciliation jobs
- Comprehensive discrepancy reporting in Excel format
- Performance monitoring and statistics
- Configurable batch sizes and processing parameters

## Performance Optimizations

### Parallel Processing
- Parallel streams for file record processing
- Concurrent database record processing
- Thread pool configuration for async operations
- Optimized batch processing with parallel streams

### Database Optimizations
- Hibernate batch processing
- Ordered inserts and updates
- Batch versioning
- SQL query optimization
- Statistics generation for monitoring

### Memory Management
- Thread-safe collections (ConcurrentHashMap, CopyOnWriteArrayList)
- Optimized object creation
- Efficient batch processing
- Memory-mapped file support

## Technical Stack

- Java 17
- Spring Boot 3.x
- Spring Batch
- Spring Data JPA
- MySQL 8.x
- Apache POI (Excel reporting)
- Lombok
- SLF4J for logging

## Configuration

### Application Properties

```yaml
reconciliation:
  file:
    path: ${RECONCILIATION_FILE_PATH:/path/to/input/file.csv}
  report:
    path: ${RECONCILIATION_REPORT_PATH:/path/to/output/report.xlsx}
  batch:
    size: 1000
  schedule:
    enabled: true
    cron: "0 0 1 * * ?"  # Run at 1 AM daily
  thread-pool:
    core-size: 4
    max-size: 8
    queue-capacity: 100

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/visa_recon
    username: root
    password: root
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        jdbc:
          batch_size: 100
          batch_versioned_data: true
        order_inserts: true
        order_updates: true
```

### Thread Pool Configuration
- Core Pool Size: Number of threads to keep alive even when idle
- Max Pool Size: Maximum number of threads to create
- Queue Capacity: Size of the queue for holding tasks before they are executed

### Database Configuration
- Batch Size: Number of records to process in each database batch
- Batch Versioning: Optimistic locking for concurrent updates
- Ordered Operations: Optimized insert and update ordering

## API Endpoints

### Reconciliation Endpoints

1. **Manual Reconciliation**
```http
POST /api/reconcile
Content-Type: application/json

{
    "filepath": "/path/to/input/file.csv",
    "reportPath": "/path/to/output/report.xlsx",
    "batchSize": 1000
}
```

2. **Batch Processing**
```http
POST /api/process-batch
Content-Type: application/json

{
    "filepath": "/path/to/input/file.csv",
    "reportPath": "/path/to/output/report.xlsx",
    "batchSize": 1000
}
```

3. **Two-Way Reconciliation**
```http
POST /api/reconcile/two-way
Content-Type: application/json

{
    "filepath": "/path/to/input/file.csv",
    "reportPath": "/path/to/output/report.xlsx",
    "batchSize": 1000
}
```

## Performance Monitoring

The application provides detailed logging for monitoring performance:

- Total records processed
- Matched records count
- Discrepancy counts
- Processing time statistics
- Error rates and types

## Error Handling

- Comprehensive error logging
- Transaction rollback on failures
- Graceful error recovery
- Detailed error reporting in Excel output

## Security

- Input validation
- SQL injection prevention
- File access control
- Secure configuration management

## Best Practices

1. **Performance Tuning**
   - Adjust batch sizes based on available memory
   - Configure thread pool based on system resources
   - Monitor database performance metrics

2. **Resource Management**
   - Use appropriate batch sizes
   - Monitor memory usage
   - Configure proper logging levels

3. **Error Handling**
   - Implement proper error recovery
   - Log all errors with context
   - Generate detailed error reports

## Development Setup

1. Clone the repository
2. Configure application.yml with your settings
3. Build the project:
```bash
./mvnw clean install
```
4. Run the application:
```bash
./mvnw spring-boot:run
```

## Monitoring and Maintenance

1. **Performance Monitoring**
   - Monitor thread pool usage
   - Track database performance
   - Watch memory consumption

2. **Logging**
   - Configure appropriate log levels
   - Monitor error rates
   - Track processing statistics

3. **Database Maintenance**
   - Regular index optimization
   - Monitor query performance
   - Clean up old records

## Future Improvements

1. **Performance Enhancements**
   - Implement Redis caching
   - Add database partitioning
   - Optimize file processing

2. **Monitoring**
   - Add performance metrics
   - Implement health checks
   - Add detailed logging

3. **Features**
   - Real-time reconciliation
   - Advanced reporting
   - Automated error recovery

## Support

For issues and feature requests, please create an issue in the repository.