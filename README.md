# Smart Ticket Issue & Management System – Dockerized Microservices Architecture

A scalable, modular issue management platform built using Spring Boot microservices and an Angular frontend. It demonstrates centralized authentication, role-based access control (RBAC), service discovery, inter-service communication via Feign clients, Kafka-based event streaming, and real-time notifications.

---

## Overview
The Smart Ticket Issue & Management System provides organizations with a cloud-native solution for managing support tickets and operational workflows. It enables:
- **Centralized authentication:** Secure JWT login and role-based access.
- **Ticket lifecycle:** Create, assign, resolve, escalate with SLA enforcement.
- **Real-time notifications:** Kafka-powered events and email alerts.
- **Dashboards & analytics:** Agent performance, category trends, and SLA compliance.
- **Modular microservices:** Independent deployment, scaling, and clear boundaries.

---

## Roles and responsibilities

### Admin
- **User and configuration management:** Create/manage users, agents, categories, and SLAs.
- **Governance:** Define escalation policies, notification channels, and reporting parameters.
- **Compliance monitoring:** Review SLA adherence and system-wide metrics.
- **Security oversight:** Assign roles and enforce access control.

### Manager
- **Team oversight:** View team workloads, reassign tickets, and balance queues.
- **SLA stewardship:** Monitor breaches, approve escalations, and enforce response targets.
- **Performance reviews:** Track agent efficiency, resolution times, and backlog health.
- **Operational decisions:** Prioritize categories, drive process improvements, and coordinate with admins.

### Agent
- **Ticket resolution:** Work assigned tickets, update status, and close with clear notes.
- **Escalation handling:** Escalate when SLAs risk breach; collaborate on complex cases.
- **Personal dashboard:** Monitor workload, resolution metrics, and SLA adherence.
- **Communication:** Maintain ticket history and engage with users/managers.

### Customer/User
- **Issue reporting:** Create tickets with category, priority, and description.
- **Progress tracking:** Follow status updates and resolution timeline.
- **Feedback:** Rate and comment after resolution; reopen if needed.
- **Notifications:** Receive updates on assignment, escalation, and closure.

### System components
- **Notification service:** Sends real-time alerts (Kafka → email or in-app).
- **Assignment & escalation service:** Automates routing and manages SLA-driven escalations.
- **Dashboard service:** Aggregates metrics for admins, managers, and agents.
- **Auth service:** Login, registration, role management, and JWT issuance.
- **API gateway:** Single entry point, routing, and token validation.
- **Service registry & config server:** Discovery and centralized configuration.

---

## Microservices overview

### Auth/User service
- **Authentication:** Register, login, JWT issuance.
- **Directory:** User profiles, roles (ROLE_USER, ROLE_AGENT, ROLE_MANAGER, ROLE_ADMIN).
- **Validation:** Provide user lookup for inter-service validation.

### API gateway (Spring Cloud Gateway)
- **Security:** Global JWT validation filter.
- **Routing:** Forwards to ticket, assignment/escalation, notification, and dashboard services.
- **RBAC:** Enforces role-based access rules at the edge.

### Ticket service
- **Core domain:** Create/update/resolve tickets; maintain history.
- **Model:** ticketId, categoryId, agentId, status, priority, SLA fields.
- **Integration:** Publishes lifecycle events (TICKET_CREATED, UPDATED, ESCALATED) via Kafka.

### Assignment & escalation service
- **Routing:** Assigns tickets based on agent workload/category.
- **SLA logic:** Detects breaches; triggers escalations to managers/admins.
- **Events:** Consumes ticket events; emits assignment/escalation updates.

### Notification service
- **Event-driven alerts:** Listens to Kafka topics; sends email or in-app notifications.
- **Templates:** Configurable messages for creation, assignment, escalation, resolution.

### Dashboard service
- **Analytics:** SLA compliance, category trends, agent/manager performance, backlog insights.
- **Access:** ROLE_MANAGER/ROLE_ADMIN for team/org analytics; ROLE_AGENT for personal stats.

### Service registry (Eureka)
- **Discovery:** Registers services for dynamic resolution and health.

### Config server
- **Central configuration:** Externalizes properties; supports environment-specific configs.

---

## Technology Stack

| Layer                | Technology |
|----------------------|------------|
| **Frontend**         | Angular 17, TypeScript |
| **Backend**          | Spring Boot (Reactive), Java 17 |
| **Database**         | MongoDB |
| **Messaging**        | Apache Kafka, Zookeeper |
| **Service Discovery**| Eureka |
| **Config Management**| Spring Cloud Config |
| **Gateway**          | Spring Cloud Gateway |
| **CI/CD**            | Jenkins, Docker, Docker Compose |
| **Deployment**       | Netlify (Frontend), Dockerized microservices |

---

##  Basic Setup Instructions

### 1. Prerequisites
- Java 17  
- Maven 3.x  
- Node.js (>=18) & Angular CLI  
- Docker & Docker Compose  
- Jenkins (optional, for CI/CD pipeline execution)

### 2. Clone the Repository
```bash
git clone https://github.com/Pranitha161/Smart-Ticket-Issue-Management-System.git
cd Smart-Ticket-Issue-Management-System
```
### 3. Create the .env file
```bash
MONGO_URI=mongodb://mongo:27017/smartticket
JWT_SECRET=your-secret-key
KAFKA_BROKER=kafka:9092
```
### 4. Builde Backend Microservices
```bash
cd Backend
mvn clean package -DskipTests
```
### 5. Run with Docker Compose
```bash
docker-compose up -d --build
```
### 6. Run with Docker Compose
```bash
cd angular-ui 
npm install 
ng serve -o
```
### 7. Access the Application
```bash

Frontend UI → http://localhost:4200

API Gateway → http://localhost:8765

Service Registry (Eureka) → http://localhost:8761

Config Server → http://localhost:8888

```
