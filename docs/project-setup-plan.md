# OmniEats: Project Setup Phase Detailed Plan

This document outlines the detailed plan for the initial project setup phase of the OmniEats application, based on the approved architecture and requirements.

## 1. Prerequisites and Tooling
- **JDK 17/21**: For Spring Boot microservices.
- **Node.js (v18+) & npm/yarn**: For the React frontend.
- **Docker & Docker Compose**: For containerization and local infrastructure dependencies.
- **Kubernetes (Minikube or Kind)**: For local K8s orchestration and testing.
- **Ollama**: Installed locally or run via Docker to serve the local LLMs (Llama 4 8B, Gemma 3).
- **Git**: Configuration and initial repository setup.

## 2. Project Structure & Repository Initialization
Adopt a monorepo structure to simplify initial development, CI/CD, and orchestration.

```text
food-order-system/
├── frontend/                 # React SPA (Client)
├── backend/                  # Spring Boot Microservices Parent
│   ├── gateway-service/      # Spring Cloud Gateway
│   ├── identity-service/     # Auth & Users
│   ├── restaurant-service/   # Menus & Orders (Multi-Tenant)
│   └── ai-aggregator-service/# AI Integration & Coordination
├── k8s/                      # Kubernetes YAML Manifests
├── docker/                   # Local Docker Compose for quick dev
└── docs/                     # Architecture & Documentation
```

## 3. Backend Microservices Setup (Spring Boot)
Initialize projects utilizing Spring Initializr under a parent POM/build script.

### 3.1 `gateway-service`
- **Role**: Single entrypoint, routing, rate-limiting, and JWT validation proxy.
- **Dependencies**: Spring Cloud Gateway, Resilience4j, Spring Boot Actuator, Prometheus.
- **Setup**: Define generic routing rules for `/api/auth`, `/api/restaurants` (routing dynamically to the load-balanced `restaurant-service`), and custom JWT validation filters linking to Redis.

### 3.2 `identity-service`
- **Role**: Identity domain, Authentication & Users.
- **Dependencies**: Spring Web, Spring Security, Spring Data JPA, PostgreSQL Driver, Spring Data Redis, JWT Library.
- **Setup**: Establish `User` and `UserProfile` entities. Configure BCrypt, JWT token generation, and setup `application-dev.yml` to target the local Postgres and Redis instances.

### 3.3 `restaurant-service` (Multi-Tenant)
- **Role**: Core entity management (Restaurants, Menus, Orders).
- **Dependencies**: Spring Web, Spring Data JPA, PostgreSQL Driver, Spring Boot Actuator.
- **Setup**: Implement `Restaurant`, `MenuItem`, `Order`, `OrderItem`, `CuisineTag` entities. Construct CRUD controllers with `Pageable` parameters for infinite scrolling data feeds. Support for multi-tenant pod scaling.

### 3.4 `ai-aggregator-service`
- **Role**: Aggregator and semantic interface with local LLM.
- **Dependencies**: Spring Web, Spring AI, Spring Boot Actuator.
- **Setup**: Integrate with local Ollama endpoints. Create asynchronous orchestration to collect data from `restaurant-service` and inject into LLM prompts. Implement fallback heuristics (Resilience4j).

## 4. Frontend Setup (React)
- **Initialization**: Scaffold via Vite (`npm create vite@latest frontend -- --template react-ts`).
- **Core Libraries**:
  - `react-hook-form` & `zod` for client-side boundaries / validation.
  - `axios` (or fetch) for API queries targeting the Gateway.
  - Routing via `react-router-dom` incorporating standard error boundary patterns.
  - CSS framework (Tailwind or Vanilla CSS) focusing on high-quality, modern UI implementations.
- **Wiring**: Configure Axios interceptors to dynamically inject JWTs into `Authorization` headers.

## 5. Local Infrastructure (Docker Compose)
Establish `docker/docker-compose.yml` to provision dependent services locally without the overhead of Kubernetes during active feature coding.
- **PostgreSQL**: Two logical databases (`identity_db`, `restaurant_db`).
- **Redis**: Volatile cache for JWT token whitelists/blacklists and prompt caching.
- **Prometheus & Grafana**: For metrics scraping of local JVM instances.

## 6. Containerization (Dockerfiles)
Create standard OCI-compliant `Dockerfile` definitions in each respective application folder:
- **Backend (Multi-stage build)**: Stage 1 building via Maven/Gradle; Stage 2 packing into minimal JRE images (`eclipse-temurin:17-jre-alpine`).
- **Frontend**: Stage 1 building React app; Stage 2 serving static assets via Nginx alpine.

## 7. Kubernetes Orchestration Automation
Define the declarative deployments in the `k8s/` directory to mimic production.

### 7.1 Configuration & Secrets
- `configmaps.yaml`: Environment variables, routing domains, Spring Context profiles.
- `secrets.yaml`: Base64 encoded PostgreSQL credentials, Redis passwords, JWT secret keys.

### 7.2 Persistence & Stateful Services
- **Postgres / Redis**: Deploy via `StatefulSet` with mapped `PersistentVolumeClaim` (PVC) to guarantee data survivability across pod cycles. Establish corresponding internal `ClusterIP` Services.

### 7.3 Microservices Deployments
- **Identity & AI & Gateway Services**: Standard `Deployment` configs (1 replica) with `ClusterIP` routing.
- **Restaurant Service**: Standard `Deployment` scaled to minimum 3 replicas, demonstrating horizontal multi-tenant distribution. Load balanced natively by the `ClusterIP` abstraction.

### 7.4 Ingress & Observability
- **Nginx Ingress**: Route paths to the `gateway-service` and the `frontend` container based on host or path rules.
- **Monitoring**: Deploy Prometheus to scrape `/actuator/prometheus` and Grafana for dashboard visualizations.

## 8. CI/CD Integration (GitHub Actions)
Construct `.github/workflows/pipeline.yml`:
1. **Trigger**: On push to specific protected branches (e.g., `main`, `develop`).
2. **Build & Test**: Compile Java and Node code, run integration/unit tests (enforcing 70% threshold).
3. **Containerize**: Build Docker images and tag with the commit SHA.
4. **Publish**: Push to a container registry.
5. **(Optional) Delopy**: Update Kubernetes deployment manifests to pull new image SHAs.

## 9. Next Steps
1. Create the base folders and initializing the Monorepo.
2. Generate base microservices via Spring Initializr.
3. Establish the `docker-compose.yml` for postgres and redis databases.
4. Begin scaffolding the React client.
