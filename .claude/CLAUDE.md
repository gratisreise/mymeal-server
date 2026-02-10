# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**mymeal-server** is a fresh Spring Boot 3.5.10 web service built with Java 21 and Gradle. This is a new project skeleton with no business logic implemented yet.

## Build Commands

```bash
# Build the project
./gradlew build

# Run the application
./gradlew bootRun

# Run all tests
./gradlew test

# Clean build artifacts
./gradlew clean

# Build and run tests together
./gradlew clean build

# Create OCI image (Docker)
./gradlew bootBuildImage
```

## Project Structure

```
src/main/java/com/mymealserver/
├── MymealServerApplication.java    # Main Spring Boot application entry point

src/main/resources/
├── application.properties           # Spring configuration

src/test/java/com/mymealserver/
├── MymealServerApplicationTests.java  # Integration tests
```

## Development Guidelines

### Adding Features

When adding new features, follow Spring Boot conventions:

1. **Controllers**: Add to `com.mymealserver.controller` package for REST endpoints
2. **Services**: Add to `com.mymealserver.service` package for business logic
3. **Repositories**: Add to `com.mymealserver.repository` package for data access
4. **Models/Entities**: Add to `com.mymealserver.model` or `.entity` package

### Dependencies

Currently only includes Spring Boot starter dependencies. Add new dependencies via `build.gradle`:

```gradle
implementation 'org.springframework.boot:spring-boot-starter-web'     // For REST APIs
implementation 'org.springframework.boot:spring-boot-starter-data-jpa' // For database
implementation 'org.springframework.boot:spring-boot-starter-validation' // For validation
```

### Testing

Use `@SpringBootTest` for integration tests. Test classes should be in the same package structure under `src/test/java`.

### Configuration

Application properties are in `application.properties`. For profile-specific configs, use `application-{profile}.properties`.
