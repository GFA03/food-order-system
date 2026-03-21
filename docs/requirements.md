# Architecture & Requirement Specification
## Project: OmniEats - AI-Powered Microservices Food Marketplace

---

### Phase 1: Mandatory Core Requirements

**1.1. Data Model Architecture**
The system implements a robust multi-vendor marketplace architecture utilizing seven primary, interconnected domain entities:
- **User**: Manages system credentials, authentication, and role-based access control.
- **UserProfile**: Extends the `User` entity (`@OneToOne`) to persist user-specific metadata, including delivery coordinates and dietary preferences.
- **Restaurant**: Represents the vendor entity within the marketplace.
- **MenuItem**: Associated with a `Restaurant` (`@ManyToOne`), representing individual customizable dishes.
- **Order**: Represents the high-level transactional record, mapped to a `User` (`@ManyToOne`).
- **OrderItem**: Serves as the transactional items bridge, maintaining specific quantities (`@OneToMany` within `Order`, `@ManyToOne` with `MenuItem`).
- **CuisineTag**: Facilitates flexible vendor categorization (e.g., "Vegan", "Italian") via a many-to-many relationship with the `Restaurant` entity.

**1.2. Data Access and Business Logic (CRUD Operations)**
- **Implementation Mechanism**: Data persistence is achieved universally via Spring Data JPA repositories.
- **Business Rule Enforcement**: A dedicated Service Layer encapsulates complex business validation constraints (e.g., restricting orders based on vendor operational hours).
- **Exception Management**: System-wide error state management is centralized using `@ControllerAdvice`, providing normalized API responses for domain-specific fault states such as `OrderNotFoundException` or `InsufficientInventoryException`.

**1.3. Multi-Environment Configuration Strategy**
- **Development Profile (`dev`)**: Provisions a localized PostgreSQL instance tailored for active iterative feature development.
- **Testing Profile (`test`)**: Utilizes an ephemeral H2 In-Memory database, optimizing execution speed and isolation for the CI/CD test suite.
- **Resource Management**: Profile-specific configuration manifests (`application-dev.yml`, `application-test.yml`) dynamically provision database credentials, tuning parameters, and granular logging thresholds based on the active runtime environment.

**1.4. Quality Assurance and Testing Framework**
- **Unit Testing Suite**: Leverages JUnit 5 and Mockito to validate isolated discrete service logic (e.g., dynamic tax computation algorithms), mandating a strict minimum of 70% codebase coverage.
- **Integration Testing Suite**: Validates core end-to-end user journeys spanning three primary scenarios:
  1. *User Onboarding*: Registration Flow -> Authentication Initialization -> Profile Configuration.
  2. *Discovery Engine*: AI-Driven Semantic Prompting -> Menu Retrieval -> Persistent Cart Management.
  3. *Transaction Processing*: Checkout Execution -> Order Instantiation -> Asynchronous Status Updates.

**1.5. Presentation Layer and Payload Validation**
- **Client Architecture**: A modern React-based Single Page Application (SPA) interfacing dynamically with the backend via RESTful APIs.
- **Data Validation Strategy**:
  - *Server-Side Boundary*: Strict enforcement via Bean Validation constraints (`@NotBlank`, `@Size`, `@Min`) applied symmetrically across all incoming Request Data Transfer Objects (DTOs).
  - *Client-Side Boundary*: Real-time asynchronous validation and user feedback powered seamlessly by React-Hook-Form integrations.
- **Graceful Error Handling**: Implements custom UI boundary components to handle semantic routing errors (e.g., 404 Entity Not Found) and critical infrastructure failures (e.g., 500 Subsystem Unavailable).

**1.6. Application Telemetry and Logging**
- **Standardization Architecture**: Adopts SLF4J coupled with Logback as the foundational logging facade and implementation framework.
- **Threshold Strategy & Routing**:
  - `INFO`: Records standard operational transaction flows and component lifecycle events.
  - `DEBUG`: Persists high-fidelity structured exchanges involving local LLM prompts and generative responses for fine-grained auditing.
  - `ERROR`: Routes critical application faults securely to a dedicated diagnostic output buffer (`logs/omnieats-error.log`).

**1.7. Data Pagination and Sorting Strategy**
- **Backend Optimization Pipelines**: Standard `Pageable` interfaces are natively integrated into endpoints managing `MenuItem`, `Restaurant`, and `OrderHistory` collections to ensure constant, predictable memory complexity at scale.
- **Sorting Dimensions**: Exposes composite sorting predicates allowing users to chronologically or heuristically rank restaurants (e.g., "Composite Rating", "Estimated Delivery Time"), alongside menu items primarily driven by "Unit Price" constraints.
- **Client Implementation Paradigm**: Translates paginated backend data into continuous discovery feeds utilizing infinite scrolling event listeners or explicit heuristic "Load More" action paradigms within the React frontend.

**1.8. Security and Access Validation Context**
- **Authentication Infrastructure**: Robust JDBC-backed authentication utilizing rigorous BCrypt cryptographic hashing for entity password storage.
- **Role Hierarchy Enforcement**: Strict bipartite privilege segregation comprising standard `USER` (transactional capabilities limit) and `ADMIN` (global vendor provisioning and system oversight) authorities.
- **Protective Features Ecosystem**: Encompasses customized gateway portals, secure session termination directives, and rigorous stateless endpoint authorization governed cryptographically via JSON Web Tokens (JWT).

---

### Phase 2: Advanced Architectural Requirements

**2.1. Microservices Architecture Topology**
The initial monolithic foundation is strategically decoupled into three independently deployable and highly cohesive domain services:
- **Identity Service**: The centralized authority governing user identity management, persistent authentication, and cryptographic token issuance lifecycles.
- **Restaurant Service**: The primary domain service authoritative for handling complex menu taxonomies, product inventory availability, and structural vendor categorization frameworks.
- **AI Aggregator Service**: The intelligent integration choreography layer managing localized LLM interactions and asynchronous data federation across downstream sibling domain services.

**2.2. Infrastructure and Orchestration Implementations**

| Architectural Requirement | Implementation Strategy |
| :--- | :--- |
| **Centralized Configuration** | Kubernetes `ConfigMap` integrations actively externalize environment variables (e.g., LLM parameters, database DNS routing); Kubernetes `Secret` definitions securely manage and inject obfuscated AI provider keys into the cluster state. |
| **Service Discovery** | Native Kubernetes underlying DNS resolution systematically facilitates intra-cluster communication without hardcoded IPs (e.g., the AI service routing generically to `http://restaurant-service`). |
| **Load Balancing** | Multi-replica deployment infrastructure (3x persistent pods) established for the Restaurant Service, featuring implicit Layer 4 load balancing governed natively by the corresponding Kubernetes `ClusterIP` service boundaries. |
| **API Gateway Layer** | Implementation of Spring Cloud Gateway abstracts internal topology vectors, acting as a single, unified ingress point while enforcing token bucket rate-limiting policies specifically shielding compute-heavy AI endpoints. |
| **System Observability** | Prometheus instances periodically scrape `/actuator/prometheus` exposure endpoints, systematically aggregating multidimensional latency metrics that are robustly visualized via real-time customized Grafana dashboards. |
| **Distributed Security Context** | Implements token-based decentralized validation topologies. The Identity Service cryptographically signs the JWT payloads, whilst subsequent downstream services concurrently verify underlying claims seamlessly against a centrally trusted pre-shared public key constraint. |
| **Fault Tolerance & Resilience** | Circuit breaking topologies strategically implemented via `Resilience4j`. Latency violations or unresponsiveness detected in the localized LLM (Ollama) will instantaneously trigger graceful functional degradation patterns resolving to deterministic keyword-based heuristic querying pipelines. |
| **Architectural Design Patterns** | Widespread adoption of the standardized Aggregator Microservice Pattern. The AI contextualizer service asynchronously gathers, normalizes, and maps disparate restaurant data representations, formulating unified context windows precursory to LLM prompt generation operations. |
| **Distributed Caching (NoSQL)** | Ephemeral Redis integration establishes a highly concurrent rapid memory proxy layer, actively caching recurring query executions (e.g., "Top Rated Restaurants" algorithms) and short-term contextual AI conversation sessions to massively depreciate TTFB (Time To First Byte) latencies. |

---

### Phase 3: Artificial Intelligence and CI/CD Integrations

- **Runtime AI Agent Integration**:
  - Leverages Spring AI frameworks to reliably invoke locally provisioned inferencing engines (e.g., Llama 4 8B, Gemma 3) strictly governed via the underlying Ollama architecture.
  - Core integration includes sophisticated Natural Language Processing (NLP) designed to semantically interpret arbitrary unstructured string requests (e.g., "I want something vegan and spicy") directly into structured JSON query predicates efficiently processed by standard database layers.
  
- **Development AI Agent Implementation**:
  - Integrates structured development patterns documenting the rigorous use of GitHub Copilot contextual assistants designed to substantially accelerate the programmatic generation of boilerplate JPA domain abstractions and the formulation of highly optimized, native SQL composite join patterns.

- **Continuous Integration / Continuous Deployment (CI/CD)**:
  - An automated, declarative continuous integration pipeline governed via parameterized GitHub Actions enforces rigorous build lifecycles. This directly encompasses standard JVM source compilations to executable JAR artifacts, strictly enforced execution of the generalized automated testing framework, combined with generation and remote pushing routines for immutable, standardized Docker OCI-compliant container runtime images universally triggered via protected branch lifecycle version control pushes.

- **Containerized Deployment Architecture**:
  - The comprehensive framework enables full distributed cluster deployment mechanisms, ensuring functionality, configuration parity, and reproducibility within localized development Kubernetes ecosystems (primarily targeted at Minikube variants or pure Kind abstraction contexts), meticulously defined entirely inside declarative native YAML infrastructural manifests.
