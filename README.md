# Visa Transaction Reconciliation Service

A Spring Boot application for reconciling Visa Base II transaction records between files and database.

## Features

- File to Database reconciliation
- Database to File reconciliation 
- Batch processing with Spring Batch
- Parallel processing capabilities
- Transaction record validation
- Detailed logging and monitoring
- CSV file handling

## Getting Started

### Prerequisites

- Java 11 or higher
- Maven
- MySQL/PostgreSQL database

### Installation

1. Clone the repository
2. Configure database properties in `application.properties`
3. Run `mvn clean install`
4. Start the application with `mvn spring-boot:run`

## Usage

The service exposes REST endpoints for:

- Initiating reconciliation jobs
- Checking job status
- Retrieving reconciliation results

Example API calls: