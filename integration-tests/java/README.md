# Integration Tests - Parkeoya Backend

## Overview
This directory contains integration tests for the Parkeoya backend application using TestContainers for database integration.

## Test Files

### ParkeoyaIntegrationTest.java
Comprehensive integration test that validates:
- Application startup with real database
- MySQL TestContainer integration
- Basic API endpoint functionality
- Database connectivity and configuration

## TestContainers Configuration
- **MySQL 8.0**: Containerized database for testing
- **Dynamic Port Assignment**: Automatic port configuration
- **Isolated Environment**: Each test run uses a fresh database instance

## Running Integration Tests
```bash
# Run all integration tests
mvn test -Dtest=*IntegrationTest

# Run specific integration test
mvn test -Dtest=ParkeoyaIntegrationTest

# Run with TestContainer debugging
mvn test -Dtest=ParkeoyaIntegrationTest -Dtestcontainers.reuse.enable=true
```

## Requirements
- Docker installed and running
- Java 17+
- Maven 3.6+
- Sufficient memory for container startup (recommended 4GB+)

## Test Environment
- **Database**: MySQL 8.0 in Docker container
- **Spring Profile**: test
- **Port**: Dynamically assigned
- **Data Persistence**: Temporary (destroyed after test completion)

## Configuration
Integration tests use application-test.properties for configuration:
- Dynamic database URL from TestContainer
- Test-specific logging levels
- Disabled external service integrations