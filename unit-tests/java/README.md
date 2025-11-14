# Java Unit Tests - Parkeoya Backend

## Overview
This directory contains comprehensive unit tests for the Parkeoya backend application, covering the core bounded contexts excluding device management.

## Test Structure

### IAM (Identity and Access Management)
- **UserCommandServiceImplTest.java**: Tests for user authentication, registration, and role management
  - Sign-in functionality
  - Driver registration
  - Parking owner registration
  - Role validation

### Parking Management
- **ParkingCommandServiceImplTest.java**: Tests for parking business logic
  - Parking creation and registration
  - Parking spot management
  - Availability updates
  - Edge server integration

### Reservations
- **ReservationCommandServiceImplTest.java**: Tests for reservation operations
  - Reservation creation
  - Reservation status management
  - Payment integration
  - Cancellation logic

## Controller Tests

### REST API Endpoints
- **AuthenticationControllerTest.java**: MVC tests for authentication endpoints
- **RolesControllerTest.java**: Tests for role management API
- **ParkingsControllerTest.java**: Tests for parking REST endpoints
- **ReservationsControllerTest.java**: Tests for reservation API endpoints

## Testing Framework
- **JUnit 5**: Primary testing framework
- **Mockito 5.8.0**: Mocking framework for dependencies
- **Spring Boot Test**: Integration with Spring Boot test slices
- **TestContainers**: For integration testing with real databases

## Running Tests
```bash
mvn test
mvn test -Dtest=UserCommandServiceImplTest
mvn test -Dtest=ParkingCommandServiceImplTest
mvn test -Dtest=ReservationCommandServiceImplTest
```

## Test Coverage
- Service layer business logic
- REST API endpoints
- Domain model validation
- External service integration
- Error handling scenarios

## Dependencies
See `../config/pom.xml` for complete testing dependencies including:
- Spring Boot Test Starter
- Mockito Core and JUnit Jupiter
- TestContainers MySQL
- AssertJ for fluent assertions