# 🏦 Digital Banking System — Microservices

A **Digital Banking System** built using **Spring Boot and Microservices Architecture**, designed to handle account management, money transfers, payments, fraud detection, and notifications.

The system uses **Apache Kafka** for event-driven communication, **Redis** for real-time fraud detection and rate limiting, and **Docker** for running the supporting infrastructure.

---

## 📌 Overview

This project demonstrates how a modern banking backend can be designed using independent microservices that communicate through both **REST APIs** and **asynchronous Kafka events**.

The system supports:

* 👤 Account management
* 💰 Balance management
* 💸 Money transfers
* 💳 Payment processing
* 🔍 Real-time fraud detection
* 🚨 Fraud event handling
* 🔔 Transaction and fraud notifications
* 🚦 API rate limiting
* 📡 Event-driven communication using Apache Kafka
* ⚡ Redis-based fraud detection patterns

---

# 🏗️ Architecture

```text
                         ┌─────────────────┐
                         │      Client     │
                         └────────┬────────┘
                                  │
                                  ▼
                    ┌─────────────────────────┐
                    │      API Gateway        │
                    │        Port 8080        │
                    │   Rate Limiting /       │
                    │   Request Routing       │
                    └────────────┬────────────┘
                                 │
              ┌──────────────────┼──────────────────┐
              │                  │                  │
              ▼                  ▼                  ▼
       ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
       │   Account   │    │ Transaction │    │   Payment   │
       │   Service   │    │   Service   │    │   Service   │
       │    8081     │    │    8082     │    │    8083     │
       └──────┬──────┘    └──────┬──────┘    └──────┬──────┘
              │                  │                  │
              │                  │                  │
              │                  ▼                  ▼
              │          ┌─────────────────────────────────┐
              │          │          Apache Kafka            │
              │          │        Event Communication       │
              │          └──────────────┬──────────────────┘
              │                         │
              │              ┌──────────┴──────────┐
              │              ▼                     ▼
              │      ┌─────────────────┐   ┌──────────────────┐
              │      │ Fraud Detection │   │   Notification   │
              │      │     Service     │   │     Service      │
              │      │      8084       │   │       8085       │
              │      └────────┬────────┘   └──────────────────┘
              │               │
              │               ▼
              │         ┌───────────┐
              │         │   Redis   │
              │         │   Fraud   │
              │         │  Patterns │
              │         └───────────┘
              │
              └──────────────────────┐
                                     │
                                     ▼
                              Account Updates
```

---

# 🧩 Microservices

| Service                     |   Port | Responsibility                                        |
| --------------------------- | -----: | ----------------------------------------------------- |
| **API Gateway**             | `8080` | Single entry point, request routing and rate limiting |
| **Account Service**         | `8081` | Account management and balance operations             |
| **Transaction Service**     | `8082` | Money transfers and transaction management            |
| **Payment Service**         | `8083` | Payment processing and Razorpay integration           |
| **Fraud Detection Service** | `8084` | Real-time fraud detection using Redis                 |
| **Notification Service**    | `8085` | Transaction and fraud notifications                   |

---

# 🔄 Service Communication

The project uses two communication approaches:

### Synchronous Communication

REST APIs are used when an immediate response is required.

```text
Client
   │
   ▼
API Gateway
   │
   ▼
Microservice
   │
   ▼
Immediate Response
```

### Asynchronous Communication

Apache Kafka is used for event-driven communication between services.

```text
Transaction Service
        │
        ▼
      Kafka
        │
   ┌────┴─────┐
   ▼          ▼
Fraud       Other
Detection   Consumers
```

This allows services to process events independently without tightly coupling the entire system.

---

# 📡 Kafka Topics

| Topic                   | Publisher               | Consumer                              |
| ----------------------- | ----------------------- | ------------------------------------- |
| `transaction.initiated` | Transaction Service     | Fraud Detection Service               |
| `fraud.check.result`    | Fraud Detection Service | Transaction Service                   |
| `transaction.completed` | Transaction Service     | Account Service, Notification Service |
| `fraud.detected`        | Fraud Detection Service | Account Service, Notification Service |
| `payment.completed`     | Payment Service         | Notification Service                  |

---

# 🔍 Fraud Detection Flow

When a transaction is initiated, the transaction service publishes an event to Kafka.

```text
Transaction Initiated
        │
        ▼
Transaction Service
        │
        │ transaction.initiated
        ▼
Apache Kafka
        │
        ▼
Fraud Detection Service
        │
        ▼
Redis
        │
        ▼
Fraud Analysis
        │
     ┌──┴───┐
     │      │
   Safe   Fraud
     │      │
     ▼      ▼
Continue   Block /
Transaction Fraud Event
```

The Fraud Detection Service uses **Redis-based patterns** to evaluate transaction activity in real time.

---

# 🚦 API Rate Limiting

The API Gateway provides rate limiting to protect backend services from excessive requests.

```text
Client
  │
  │ Requests
  ▼
API Gateway
  │
  ▼
Rate Limiter
  │
  ▼
Backend Services
```

Redis is used as part of the rate-limiting mechanism.

---

# 🛠️ Technologies Used

### Backend

* Java
* Spring Boot
* Spring Cloud Gateway
* Spring Data JPA
* Spring Data Redis
* Spring Kafka
* REST APIs

### Messaging

* Apache Kafka

### Database / Storage

* MySQL
* Redis

### Payment

* Razorpay

### Infrastructure

* Docker
* Docker Compose

### Build Tool

* Maven

### Development Tools

* Git
* GitHub
* Eclipse / Spring Tool Suite
* Postman

---

# 📁 Project Structure

```text
Banking System/
│
├── account-service/
│   ├── pom.xml
│   └── src/
│       └── main/
│
├── api-gateway/
│   ├── pom.xml
│   └── src/
│       └── main/
│
├── fraud-detection-service/
│   ├── pom.xml
│   └── src/
│       └── main/
│
├── notification-service/
│   ├── pom.xml
│   └── src/
│       └── main/
│
├── payment-service/
│   ├── pom.xml
│   └── src/
│       └── main/
│
├── transaction-service/
│   ├── pom.xml
│   └── src/
│       └── main/
│
├── docker-compose.yaml
├── README.md
└── .gitignore
```

---

# 🚀 Getting Started

## Prerequisites

Make sure the following are installed:

* Java
* Maven
* Docker
* Docker Compose
* MySQL
* Git

---

## 1. Clone the Repository

```bash
git clone <your-repository-url>
cd "Banking System"
```

---

## 2. Start Infrastructure

Start the required infrastructure using Docker Compose:

```bash
docker-compose up -d
```

Check running containers:

```bash
docker ps
```

---

# ▶️ Start the Services

Start each Spring Boot service separately.

### Account Service

```bash
cd account-service
mvn spring-boot:run
```

### Transaction Service

```bash
cd transaction-service
mvn spring-boot:run
```

### Payment Service

```bash
cd payment-service
mvn spring-boot:run
```

### Fraud Detection Service

```bash
cd fraud-detection-service
mvn spring-boot:run
```

### Notification Service

```bash
cd notification-service
mvn spring-boot:run
```

### API Gateway

```bash
cd api-gateway
mvn spring-boot:run
```

---

# 🔌 Service Ports

| Service                 |   Port | URL                     |
| ----------------------- | -----: | ----------------------- |
| API Gateway             | `8080` | `http://localhost:8080` |
| Account Service         | `8081` | `http://localhost:8081` |
| Transaction Service     | `8082` | `http://localhost:8082` |
| Payment Service         | `8083` | `http://localhost:8083` |
| Fraud Detection Service | `8084` | `http://localhost:8084` |
| Notification Service    | `8085` | `http://localhost:8085` |

For normal client requests, use the **API Gateway on port `8080`** rather than calling individual services directly.

---

# 🔁 Transaction Flow

A typical money transfer follows this flow:

```text
Client
  │
  ▼
API Gateway
  │
  ▼
Transaction Service
  │
  │ transaction.initiated
  ▼
Kafka
  │
  ▼
Fraud Detection Service
  │
  ▼
Redis
  │
  ▼
Fraud Check Result
  │
  ▼
Kafka
  │
  ▼
Transaction Service
  │
  ├───────────────┐
  │               │
 Safe            Fraud
  │               │
  ▼               ▼
Complete       Block Transaction
  │
  ▼
Kafka
  │
  ├──► Account Service
  │
  └──► Notification Service
```

---

# 💳 Payment Flow

```text
Client
   │
   ▼
API Gateway
   │
   ▼
Payment Service
   │
   ▼
Razorpay
   │
   ▼
Payment Webhook
   │
   ▼
Payment Service
   │
   │ payment.completed
   ▼
Kafka
   │
   ▼
Notification Service
```

---

# 🔐 Security Considerations

This project is intended as a learning and demonstration project.

When running it in a real environment:

* Never commit database passwords.
* Never commit API keys or payment credentials.
* Store secrets using environment variables or a secret manager.
* Use HTTPS in production.
* Configure proper authentication and authorization.
* Use production-grade database credentials.
* Configure Kafka and Redis security before deployment.

---

# 🐳 Stop Infrastructure

To stop the Docker infrastructure:

```bash
docker-compose down
```

To stop and remove volumes:

```bash
docker-compose down -v
```

> Use `-v` carefully because it removes Docker volumes and can delete persisted local data.

---

# 📚 Key Concepts Demonstrated

This project demonstrates several important backend and distributed-system concepts:

* Microservices Architecture
* API Gateway
* REST API communication
* Event-driven architecture
* Apache Kafka
* Kafka producers and consumers
* Redis
* Rate limiting
* Real-time fraud detection
* Database-backed services
* Payment integration
* Webhooks
* Asynchronous communication
* Service-to-service communication
* Docker Compose
* Distributed transaction processing

---

# 🎯 Project Goal

The goal of this project is to understand how a **real-world banking backend can be decomposed into independent microservices** and how those services can communicate using both synchronous REST APIs and asynchronous Kafka events.

It also provides practical experience with **Spring Boot, Kafka, Redis, Docker, database integration, payment processing, and distributed-system design**.

---

## ⭐ Support

If you found this project useful, consider giving the repository a ⭐ star.

---

## 👨‍💻 Author

**Aniket Vishwakarma**

Built as a hands-on implementation of a Digital Banking System using Spring Boot Microservices.
