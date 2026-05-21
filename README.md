# 🚀 Enterprise CRM Microservices Platform

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Kafka](https://img.shields.io/badge/Apache_Kafka-Event--Driven-231F20?style=for-the-badge&logo=apachekafka&logoColor=white)
![Elasticsearch](https://img.shields.io/badge/Elasticsearch-Advanced_Search-005571?style=for-the-badge&logo=elasticsearch&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-Distributed_Lock-DC382D?style=for-the-badge&logo=redis&logoColor=white)
![Keycloak](https://img.shields.io/badge/Keycloak-OAuth2--OIDC-232F3E?style=for-the-badge&logo=keycloak&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-ACID-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)
![gRPC](https://img.shields.io/badge/gRPC-Inter--Service-244C5A?style=for-the-badge&logo=grpc&logoColor=white)

An ultra-scalable, robust, and highly available Enterprise Customer Relationship Management (CRM) platform built with modern **Microservices Architecture**. Designed to handle high throughput, ensuring zero data loss and maximal observability.

---

## 🌟 Core Architectural Patterns

This project acts as a technical showcase for Enterprise-grade Java engineering, implementing the following advanced patterns:

- **CQRS (Command Query Responsibility Segregation) & Event Sourcing**: Separating read and write workloads. Commands mutate state in PostgreSQL and publish events to **Apache Kafka**. Queries are served at lightning speed from **Elasticsearch** and **Redis**.
- **Event-Driven Architecture**: Decoupled services communicating asynchronously via Apache Kafka streams.
- **Distributed Locks**: Handling high-concurrency race conditions (e.g., inventory deduction, invoice generation) using **Redisson** (Redis-based distributed locks).
- **Centralized Identity Management**: Zero-trust architecture using **Keycloak** as the Identity Provider (IdP) for OAuth2.0 and OpenID Connect (OIDC).
- **Service Mesh & API Gateway**: Centralized routing, rate limiting, and security via **Spring Cloud Gateway** (replacing Kong).
- **High-Performance Inter-service Communication**: Utilizing **gRPC** and Protobuf instead of traditional REST for low-latency internal communication.
- **Database Versioning**: Schema evolution managed seamlessly with **Flyway**.
- **Resiliency & Fault Tolerance**: Implementing Circuit Breakers, Bulkheads, Retries, and Time Limiters using **Resilience4j**.

---

## 🏗 System Architecture Diagram

```mermaid
graph TD
    Client[Web/Mobile Client] --> API_Gateway[Spring Cloud Gateway]
    API_Gateway -. OAuth2 / OIDC .-> Keycloak[Keycloak Identity Provider]
    
    API_Gateway --> Auth[Auth Service]
    API_Gateway --> Customer[Customer Service]
    API_Gateway --> Sales[Sales Service]
    API_Gateway --> Notification[Notification Service]
    API_Gateway --> AI[AI Service - Python]

    subgraph "Core Domain (CQRS & Event Driven)"
        Customer --> DB_Customer[(PostgreSQL: Customer)]
        Customer -. Publish Event .-> Kafka((Apache Kafka Broker))
        
        Sales --> DB_Sales[(PostgreSQL: Sales)]
        Sales -. Publish Event .-> Kafka
        
        Kafka -. Consume .-> Sync_Worker[Search Sync Worker]
        Sync_Worker --> ES[(Elasticsearch)]
        Customer --> ES
        Sales --> ES
    end

    subgraph "High Concurrency & Caching"
        Customer --> Redis[(Redis: Cache & Redisson Locks)]
        Sales --> Redis
    end

    subgraph "Inter-service Communication"
        Customer <==>|gRPC| Sales
        Sales <==>|gRPC| Notification
    end

    subgraph "Observability & Monitoring"
        All_Services(All Microservices) -. Traces .-> OpenTelemetry[OpenTelemetry Collector]
        OpenTelemetry --> Jaeger[Jaeger / Zipkin]
        All_Services -. Metrics .-> Prometheus[Prometheus]
        Prometheus --> Grafana[Grafana Dashboards]
        All_Services -. Logs .-> Fluentd[Fluent Bit]
        Fluentd --> ELK[(Elasticsearch / Logstash / Kibana)]
    end
```

---

## 🛠 Tech Stack Deep Dive

### Backend & Core
- **Java 21**: Virtual Threads, Records, Pattern Matching.
- **Spring Boot 3.3.x**: Spring Web, Spring Data JPA, Spring Cloud.
- **gRPC & Protobuf**: High-throughput RPC framework.
- **MapStruct & Lombok**: Boilerplate reduction.
- **Resilience4j**: Resilience patterns.

### Data Storage & Messaging
- **PostgreSQL 17**: Primary ACID-compliant relational datastore.
- **Apache Kafka**: Distributed event streaming platform.
- **Elasticsearch 8.x**: Full-text search and distributed document store.
- **Redis (Redisson)**: In-memory data structure store, used for caching and distributed locks.
- **Flyway**: Database migration.

### Infrastructure & Observability
- **Keycloak**: Open Source Identity and Access Management.
- **Docker & Docker Compose / Kubernetes**: Containerization and Orchestration.
- **Prometheus & Grafana**: System and application metrics monitoring.
- **ELK Stack (Elasticsearch, Logstash, Kibana)**: Centralized logging.
- **Micrometer + OpenTelemetry + Jaeger**: Distributed tracing.

---

## 🚀 Getting Started

### Prerequisites
- Docker & Docker Compose (v2)
- Java 21+
- Maven 3.9+

### Spin up the infrastructure
Run the entire infrastructure (Databases, Message Broker, Observability, IAM) with a single command:
```bash
docker-compose -f docker-compose.infra.yml up -d
```

### Run the Microservices
*Note: Ensure the infrastructure is healthy before starting the services.*
```bash
docker-compose up -d --build
```

---

## 💡 Technical Highlights (For Technical Reviewers)

1. **Why Kafka + CQRS?** 
   By segregating the write (Postgres) and read (Elasticsearch) paths, the system achieves extreme read scalability. Complex search queries on Customer and Sales data don't impact the transactional database.
2. **Why Redisson over standard Redis?**
   Standard Redis is great for caching, but Redisson provides production-ready distributed Java objects and services. We heavily rely on `RLock` to prevent race conditions during high-concurrency order placements.
3. **Why gRPC internally?**
   While external clients use REST/GraphQL via the API Gateway, internal microservices use gRPC over HTTP/2. The compact binary payload of Protobuf ensures minimal network overhead and blazing-fast serialization compared to JSON.
4. **Complete Observability**
   In a microservice jungle, logs are not enough. Distributed tracing allows us to see exactly how long a request spent in the Gateway, the Auth Service, the Database, and the gRPC call to the Notification service, making bottleneck identification trivial.
