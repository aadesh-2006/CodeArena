# CodeArena — Product & Feature Roadmap

CodeArena is designed to grow from a dedicated, high-performance Data Structures & Algorithms (DSA) practice judge into a comprehensive, multi-track engineering assessment platform.

Future tracks will be implemented through focused, independent GitHub Pull Requests.

---

## Current Track: Data Structures & Algorithms (Phase 1 — In Progress)

- [x] **Milestone 1**: Architecture Foundation, Spring Boot Modular Monolith, React + Vite Shell, Flyway Baseline, CI Pipeline.
- [ ] **Milestone 2**: JWT Authentication, User Profiles, RBAC (`USER`, `ADMIN`), Admin Problem Authoring, 25 Original Seed Problems.
- [ ] **Milestone 3**: Problem Discovery Page, Server-side Filters, Search, Problem Detail Page with Multi-pane Layout.
- [ ] **Milestone 4**: Monaco Code Editor Integration, Multi-Language Templates (Java, Python 3, C++17), Draft Auto-save.
- [ ] **Milestone 5**: Sandboxed Execution Service Integration (Judge0), Run vs. Submit Lifecycles, Verdict Calculation Engine.
- [ ] **Milestone 6**: Submission History, User Analytics Dashboard, Solving Streaks, Topic Mastery Tracking.
- [ ] **Milestone 7**: Automated End-to-End Test Suite, Docker Compose Orchestration, Release Readiness Verification.

---

## Future Assessment Tracks (Post-Phase 1 Expansion)

The following tracks represent planned extensions for future Pull Requests. They are architecturally planned but deliberately deferred from the Phase 1 release:

### 1. Development & Full-Stack Challenges
- Users implement real-world RESTful routes, controllers, services, and middlewares.
- Automatic integration test harnesses evaluate API contract adherence, error handling, and validation.

### 2. Backend Engineering & System Challenges
- High-concurrency simulations, caching implementations (Redis), and distributed lock challenges.
- Benchmarked evaluation measuring throughput, latency under load, and database query optimization.

### 3. Project-Based Timed Contests
- Synchronous coding competitions where participants receive a unified specification and time window.
- Git-based repository submissions with multi-tier automated test pipelines and peer review rounds.

### 4. Debugging & Code Archaeology
- Pre-existing codebases containing subtle race conditions, memory leaks, off-by-one errors, or security flaws.
- Candidates diagnose root causes, produce minimal fixes, and ensure non-regression.

### 5. AI / Machine Learning Challenges
- Tabular and text datasets provided via cloud storage.
- Candidates submit data exploration notebooks, feature engineering pipelines, and scikit-learn/XGBoost models evaluated against holdout metrics (F1-score, RMSE).

### 6. Deep Learning Challenges
- PyTorch/TensorFlow pipelines for computer vision and NLP models.
- Model weights evaluation against validation datasets with parameter and inference budget constraints.

### 7. SQL & Database Optimization
- Relational schema challenges spanning complex JOINs, window functions, recursive CTEs, and index optimization.
- Query plan verification (`EXPLAIN ANALYZE`) to reward efficient query cost.

### 8. Engineering Conceptual Quizzes
- Timed, adaptive conceptual assessments testing distributed systems, computer architecture, networking, and algorithmic complexity.
