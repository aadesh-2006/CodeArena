# Contributing to CodeArena

Thank you for contributing to CodeArena! As a rigorous engineering platform, we enforce consistent architectural standards, clean Git hygiene, and disciplined testing practices.

---

## 1. Development Principles

1. **Modular Monolith Integrity**: Maintain strict encapsulation between packages. Keep business logic inside `@Service` classes, avoid fat controllers, and never leak JPA entities to clients.
2. **Security by Design**: Untrusted user code execution must remain isolated. Never evaluate submitted code directly inside the host JVM.
3. **Reproducible Infrastructure**: All schema changes must be driven via Flyway migrations (`src/main/resources/db/migration/`).

---

## 2. Branch Naming Conventions

Branches should follow structured naming patterns reflecting the nature of the contribution:

- **Features**: `feat/<issue-id>-<short-description>` (e.g., `feat/CA-12-jwt-authentication`)
- **Fixes**: `fix/<issue-id>-<short-description>` (e.g., `fix/CA-44-testcase-timeout`)
- **Refactoring**: `refactor/<short-description>` (e.g., `refactor/submission-dto-cleanup`)
- **Documentation**: `docs/<short-description>` (e.g., `docs/update-architecture-spec`)
- **Chore/Tooling**: `chore/<short-description>` (e.g., `chore/upgrade-tailwind-config`)

---

## 3. Commit Message Conventions

We adhere to the Conventional Commits specification:

```
<type>(<scope>): <subject>

[optional body explaining WHY, not WHAT]

[optional footer(s)]
```

### Types:
- `feat`: A new user-facing or platform capability.
- `fix`: A bug fix.
- `docs`: Documentation modifications only.
- `test`: Adding or correcting unit/integration tests.
- `refactor`: Code change that neither fixes a bug nor adds a feature.
- `perf`: Code change that improves performance.
- `chore`: Build process, dependencies, or auxiliary tool changes.

### Examples:
```
feat(auth): implement JWT token provider and security filter chain
fix(submission): prevent concurrent verdict overwrite during bulk runs
test(problem): add edge case validation for two-pointer seed questions
```

---

## 4. Pull Request Workflow

1. **Fork or Branch**: Create your feature branch from the latest `main`.
2. **Implement & Verify**: Ensure all quality gates pass locally before opening a PR:
   - Backend: `cd backend && mvn clean test`
   - Frontend: `cd frontend && npm run test && npm run build`
3. **Open Pull Request**:
   - Provide a concise summary of the architectural changes.
   - Reference the relevant issue or task.
   - Document any database migrations or configuration changes.
   - Include test output or screenshots demonstrating verification.
4. **Code Review Expectations**:
   - Every PR requires review and approval from at least one core maintainer.
   - Automated CI tests on GitHub Actions must pass before merging.
   - Rebase or squash merge as directed by maintainers.

---

## 5. Testing Requirements

- **Backend**: Every service and controller endpoint must have unit tests (JUnit 5 + Mockito) or integration tests (MockMvc + H2/PostgreSQL mode).
- **Frontend**: Component changes must include tests verifying rendering, interactions, and error states using Vitest and React Testing Library.
- **Coverage**: Do not lower overall test coverage.
