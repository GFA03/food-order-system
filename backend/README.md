# OmniEats Backend Microservices

This directory contains the Spring Boot microservices for the OmniEats application. It is managed as a multi-module Maven project where all services inherit versions from the `omnieats-parent` `pom.xml`.

## Project Structure
- `gateway-service/`: The single entry point utilizing Spring Cloud Gateway.
- `identity-service/`: Authentication and user domain.
- `restaurant-service/`: Core domain handling menus and orders.
- `ai-aggregator-service/`: Orchestration API routing to local LLMs (Ollama/Llama 4).

## Prerequisites
- **JDK 17 or 21** installed locally.
- (Optional but recommended) Docker for running local infrastructure like Postgres/Redis.

---

## How to Build and Test

We utilize the Maven Wrapper (`mvnw`), meaning you do not need to install Maven on your machine. All commands should be executed from the `backend/` directory.

### Running All Tests (Backend-wide)
To build the parent POM and execute all unit and integration tests across every microservice sequentially, run:
```bash
./mvnw clean test
```

### Building the Entire Project (Without Tests)
If you just want to verify compilation or prepare the jar artifacts without waiting for tests:
```bash
./mvnw clean package -DskipTests
```

### Building or Testing a Specific Service
Use the Maven project list flag (`-pl`) to target a specific folder. For example, to run tests just for the Identity Service:
```bash
./mvnw clean test -pl identity-service
```

---

## How to Run a Service Locally

You can leverage the Spring Boot Maven plugin to spin up any microservice locally without packaging it into a jar first.

### Start the Gateway
```bash
./mvnw spring-boot:run -pl gateway-service
```
*(The gateway will start on port 8080 by default).*

### Start a Specific Domain Service
```bash
./mvnw spring-boot:run -pl restaurant-service
```
*(Note: To successfully boot domain services like `restaurant-service` or `identity-service` without crashes, you will first need a running PostgreSQL/Redis instance configured in their respective `application.yml` files).*
