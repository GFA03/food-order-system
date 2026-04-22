# OmniEats Local Docker Infrastructure

This directory contains the `docker-compose.yml` required to quickly spin up the local development databases and caches (PostgreSQL and Redis) without needing to install them directly on your machine.

## Prerequisites
- Docker
- Docker Compose

---

## 🚀 Quick Start

All commands should be run from inside this `docker/` directory.

### 1. Start the Containers
Start PostgreSQL and Redis silently in the background:
```bash
docker compose up -d
```

### 2. Access the Application
Once the containers have finished bootstrapping, open your browser:
- **Web Frontend (React)**: [http://localhost:3000](http://localhost:3000)
- **API Gateway**: [http://localhost:8080](http://localhost:8080)

### 3. Verify Health
Check that the containers are healthy and running:
```bash
docker compose ps
```
*(The PostgreSQL container will automatically execute `init-db.sql` on its very first boot to provision the `identity_db` and `restaurant_db` schemas that Hibernate expects).*

### 3. Viewing Logs
If a microservice is failing to connect to a database, you can inspect the PostgreSQL or Redis logs:
```bash
docker compose logs -f postgres
```

### 4. Stopping and Teardown
To simply pause the containers (keeping your local database table data intact):
```bash
docker compose stop
```

To entirely **destroy** the containers and wipe your local database data (starting fresh on the next boot):
```bash
docker compose down -v
```

---

## 📊 Monitoring

The stack includes Prometheus and Grafana for observability.

### Access

| Service | URL | Credentials |
|---|---|---|
| Prometheus | [http://localhost:9090](http://localhost:9090) | — |
| Grafana | [http://localhost:3001](http://localhost:3001) | admin / admin |

### Dashboards

Three dashboards are auto-provisioned when Grafana starts:

| Dashboard | Description |
|---|---|
| **OmniEats — HTTP Overview** | Request rate, latency (p50/p95/p99), and HTTP error rate (4xx/5xx) per service |
| **OmniEats — JVM Metrics** | Heap used/committed, GC pause time, live threads, and CPU usage per service |
| **OmniEats — Gateway Metrics** | Requests per route, rate limiter denials (429s), and response time by route |

### Prerequisites for Metrics

Each backend service must expose `/actuator/prometheus`. Add to each service's `pom.xml`:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

And in `application.yml`:
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,prometheus
```
