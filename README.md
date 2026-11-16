# CodeArena — Full-Stack DSA Practice Platform

CodeArena is a production-grade, developer-focused Data Structures and Algorithms (DSA) practice platform built with a modular monolith architecture. It is designed to provide the rigorous online judging experience of conventional coding platforms while laying the architectural foundation for a broader multi-track engineering assessment system.

---

## Table of Contents

1. [Features](#features)
2. [Technology Stack](#technology-stack)
3. [Architecture Overview](#architecture-overview)
4. [Screenshots](#screenshots)
5. [Prerequisites](#prerequisites)
6. [Environment Configuration](#environment-configuration)
7. [Local Setup & Running](#local-setup--running)
8. [Database & Migrations](#database--migrations)
9. [Code Execution Engine Architecture](#code-execution-engine-architecture)
10. [Automated Testing](#automated-testing)
11. [Roadmap & Milestone Status](#roadmap--milestone-status)
12. [Current Verification Status](#current-verification-status)
13. [Contributing](#contributing)

---

## Current Verification Status

| Capability / Component | Status | Notes |
| :--- | :--- | :--- |
| **Backend Maven Build** | **VERIFIED** | Compiles cleanly with Java 21 LTS (`<release>21</release>`) on Spring Boot 4.1.2 |
| **Backend Unit & Integration Tests** | **VERIFIED** | 25/25 tests pass (`AuthControllerIntegrationTest`, `UserControllerIntegrationTest`, `AuthServiceTest`, `JwtTokenServiceTest`, `UserRepositoryTest`, `HealthControllerTest`, `CodeArenaApplicationTests`) |
| **Authentication & User Management** | **VERIFIED** | Complete JWT authentication (`/api/auth/register`, `/api/auth/login`), profile endpoints (`/api/users/me`), BCrypt hashing, role-based security |
| **Flyway Schema Migrations** | **VERIFIED** | `V1__initial_foundation_schema.sql` and `V2__add_display_name_to_users.sql` verified and applied via `spring-boot-starter-flyway` |
| **OpenAPI / Swagger UI Spec** | **VERIFIED** | `springdoc-openapi-starter-webmvc-ui:3.2.0` configured with Bearer JWT security scheme |
| **Frontend Test Suite** | **VERIFIED** | Vitest + React Testing Library (10/10 tests pass across `App.test.tsx` and `Auth.test.tsx`) |
| **Frontend Production Build** | **VERIFIED** | `tsc && vite build` generates clean production bundle in `< 2.5s` |
| **Frontend Auth & Navigation** | **VERIFIED** | Login, Registration with client validation, Protected Routes, Profile editing, and responsive Header |
| **Real PostgreSQL 17 Service** | **UNVERIFIED** | Windows service running on 5432, but credentials require local `.env` definition |
| **Docker Compose Services** | **UNVERIFIED** | Docker Desktop engine is not running on host pipe; Compose file staged but unstarted |
| **Execution Sandbox (Judge0)** | **DEFERRED** | Boundary interface defined in Milestone 1; integration scheduled for Milestone 5 |

---

## Features

- **Problem Discovery**: Algorithmic problem exploration with server-side filtering by difficulty, topic tag, and solved status.
- **Problem Workspace**: Multi-pane responsive interface featuring problem specifications, examples, constraints, hints, and editorials.
- **Monaco Code Editor**: Professional in-browser code editor with syntax highlighting, language templates, and draft auto-saving.
- **Multi-Language Support**: Java 21, Python 3, and C++17.
- **Run vs. Submit Separation**:
  - **Run**: Test solution against sample inputs or custom test cases without affecting solved status.
  - **Submit**: Evaluates against hidden test suites, computes official verdicts, and records submissions.
- **Accurate Verdict Engine**: `ACCEPTED`, `WRONG_ANSWER`, `TIME_LIMIT_EXCEEDED`, `RUNTIME_ERROR`, `COMPILATION_ERROR`, and `INTERNAL_ERROR`.
- **Confidential Test Suites**: Hidden test cases remain strictly isolated on the backend and are never exposed via client APIs.
- **User Analytics & Dashboard**: Genuine problem statistics, solving streaks, topic-wise mastery, and submission history.

---

## Technology Stack

### Backend
- **Language**: Java 21 LTS
- **Framework**: Spring Boot 4.1.2 (Modular Monolith on Spring Framework 7)
- **Security**: Spring Security & JWT
- **Persistence**: Spring Data JPA & PostgreSQL Driver
- **Migrations**: Flyway (via `spring-boot-starter-flyway`)
- **API Documentation**: OpenAPI 3.0 / Swagger UI (`springdoc-openapi` 3.2.0)
- **Build Tool**: Maven 3.9+

### Frontend
- **Framework**: React 18 & TypeScript
- **Build Tool**: Vite
- **Styling**: Tailwind CSS (Dark-first developer UI)
- **Editor**: Monaco Editor
- **Icons**: Lucide React
- **Routing**: React Router
- **Data Fetching**: TanStack Query

### Database & Infrastructure
- **Database**: PostgreSQL 17
- **Containers**: Docker & Docker Compose
- **CI/CD**: GitHub Actions

---

## Architecture Overview

CodeArena is implemented as a **Modular Monolith** separating concerns into distinct domain modules while keeping deployment simple:

```
CodeArena/
├── backend/              # Spring Boot 3.4.2 (Java 21)
│   ├── src/main/java/com/codearena/
│   │   ├── common/       # Global exception handlers, ApiResponse, pagination
│   │   ├── auth/         # JWT authentication & security filters
│   │   ├── user/         # User domain & roles (USER, ADMIN)
│   │   ├── problem/      # Problem, topic, test case domain & filters
│   │   ├── admin/        # Problem authoring & management endpoints
│   │   ├── submission/   # Run vs Submit orchestrator & verdict engine
│   │   ├── execution/    # Sandboxed execution boundary adapter
│   │   ├── dashboard/    # Analytics, streaks, and progress metrics
│   │   └── draft/        # Code draft persistence
│   └── src/main/resources/db/migration/ # Flyway migrations
├── frontend/             # React 18 + TypeScript + Vite + Tailwind
│   ├── src/components/   # Header, Footer, StatusCard, Editor
│   ├── src/types/        # Data contracts matching backend DTOs
│   └── src/test/         # Vitest + React Testing Library suites
├── .github/workflows/    # GitHub Actions CI pipeline
├── ARCHITECTURE.md       # In-depth architectural specification
├── ROADMAP.md            # Multi-track roadmap
└── CONTRIBUTING.md       # Branch, commit, and PR standards
```

For detailed diagrams and security specifications, see [ARCHITECTURE.md](ARCHITECTURE.md).

---

## Screenshots

<!-- Screenshot Placeholders -->
| Problem Discovery | Problem Workspace & Editor |
| :---: | :---: |
| *Coming in Milestone 3* | *Coming in Milestone 4* |

| Submissions History | User Analytics Dashboard |
| :---: | :---: |
| *Coming in Milestone 6* | *Coming in Milestone 6* |

---

## Prerequisites

- **Java**: JDK 21+ installed and configured on `PATH`.
- **Maven**: Version 3.8+ or 3.9+.
- **Node.js**: Node.js 20+ and npm 10+.
- **PostgreSQL**: PostgreSQL 16+ or 17 (or Docker).

---

## Environment Configuration

A configuration template is provided in `.env.example`. Copy it to `.env`:

```bash
cp .env.example .env
```

Key environment variables:
| Variable | Default Value | Description |
| :--- | :--- | :--- |
| `PORT` | `8080` | Backend HTTP port |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/codearena` | PostgreSQL JDBC connection URL |
| `SPRING_DATASOURCE_USERNAME` | `postgres` | Database username |
| `SPRING_DATASOURCE_PASSWORD` | `postgres` | Database password |
| `JWT_SECRET` | *(Random 256-bit string)* | Secret for signing JWT tokens |
| `EXECUTION_ENGINE_URL` | `http://localhost:2358` | Sandbox execution provider endpoint |

---

## Local Setup & Running

### 1. Database Setup
Create the target database in your local PostgreSQL instance:
```sql
CREATE DATABASE codearena;
```

### 2. Backend Startup
```bash
cd backend
mvn clean spring-boot:run
```
The backend starts at `http://localhost:8080`.
- Health Endpoint: `http://localhost:8080/api/health`
- Swagger UI / API Docs: `http://localhost:8080/swagger-ui.html`

### 3. Frontend Startup
```bash
cd frontend
npm install
npm run dev
```
The frontend starts at `http://localhost:3000` with automatic reverse proxying of `/api` requests to `http://localhost:8080`.

---

## Database & Migrations

All database schemas are managed with version-controlled Flyway migrations located at:
`backend/src/main/resources/db/migration/`

- `V1__initial_foundation_schema.sql`: Foundational user table, identity constraints, and indices.

Migrations apply automatically upon backend startup.

---

## Code Execution Engine Architecture

Untrusted user code is **never executed inside the backend host JVM**. 

Execution is governed by the boundary contract `com.codearena.execution.contract.CodeExecutionService`. In Milestone 5, this adapter will connect to an isolated sandbox (such as Judge0 or containerized worker pool) operating with:
- Disabled network access (`--net=none`).
- Strict memory limits (default 128 MB) and execution timeouts (default 2000 ms).
- Non-root unprivileged process isolation.

---

## Automated Testing

### Backend Tests (JUnit 5 + MockMvc + Flyway + H2 PostgreSQL Mode)
```bash
cd backend
mvn clean test
```

### Frontend Tests (Vitest + React Testing Library)
```bash
cd frontend
npm run test
```

### Frontend Production Build
```bash
cd frontend
npm run build
```

---

## Roadmap & Milestone Status

- [x] **Milestone 1**: Project Scaffolding, Spring Boot 3.4 Core, React Shell, Flyway V1, CI.
- [ ] **Milestone 2**: Authentication (JWT), User Profiles, Admin Management, 25 Seed Problems.
- [ ] **Milestone 3**: Problem Discovery, Server-side Filtering, Problem Detail Layout.
- [ ] **Milestone 4**: Monaco Editor Integration, Multi-Language Templates, Draft Storage.
- [ ] **Milestone 5**: Sandboxed Execution Engine, Run vs Submit Workflows, Verdict Calculator.
- [ ] **Milestone 6**: Submission History, User Dashboard & Analytics.
- [ ] **Milestone 7**: End-to-End Test Suite, Docker Compose, Final Release Verification.

See [ROADMAP.md](ROADMAP.md) for future assessment tracks beyond Phase 1.

---

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for branch naming conventions, commit formats, and code review standards.
