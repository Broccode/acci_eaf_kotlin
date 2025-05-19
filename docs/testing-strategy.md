# 14. Overall Testing Strategy
>
> This document is a granulated shard from the main "ACCI-EAF-Architecture.md" focusing on "Overall Testing Strategy".

This section outlines the project\'s comprehensive testing strategy, which all AI-generated and human-written code must adhere to. It complements the testing tools selected in the "Definitive Tech Stack Selections" and the NFRs regarding test coverage. A multi-layered testing approach will be adopted to ensure software quality.

* **Primary Testing Tools & Frameworks:**
  * **Backend (Kotlin/JVM):**
    * **Unit & Integration Tests:** JUnit Jupiter (`5.12.2`), MockK (`1.14.2`) for mocking, Kotest (`5.9.1`) or AssertJ for assertions.
    * **Axon Specific Tests:** Axon Test Fixture (version aligned with Axon Framework `4.11.2`).
    * **Integration Test Dependencies:** Testcontainers (`1.21.0` for Java) to manage Dockerized dependencies like PostgreSQL.
    * **Code Coverage:** JaCoCo Gradle plugin.
  * **Frontend (Control Plane UI - React):**
    * **Unit & Component Tests:** Jest (`29.7.0`), React Testing Library (`16.3.x`).
    * **End-to-End (E2E) Tests:** Playwright (`1.52.x`).
  * **CI Integration:** All automated tests (Unit, Integration, relevant E2E) will be executed as part of the CI/CD pipeline (GitHub Actions) for every pull request and main branch commit. Build failures will occur if tests fail or if coverage drops below defined thresholds.

* **Unit Tests:**
  * **Scope:** Test individual functions, methods, classes, or small, isolated modules (e.g., a single Spring service, a domain entity\'s logic, a utility function, an Axon Aggregate\'s command/event handlers in isolation). The focus is on verifying business logic, algorithms, transformation rules, and boundary conditions in isolation from external dependencies.
  * **Location & Naming (Kotlin - Backend):**
    * As defined in "Coding Standards": Unit test files must be located in the `src/test/kotlin/` directory of their respective module. The package structure within `src/test/kotlin/` must mirror the package structure of the code being tested.
    * Test class files must be named after the class they are testing, appended with `Test`. For example, a class `com.axians.accieaf.iam.application.TenantService` located in `eaf-iam/src/main/kotlin/com/axians/accieaf/iam/application/TenantService.kt` will have its corresponding test class as `com.axians.accieaf.iam.application.TenantServiceTest` in `eaf-iam/src/test/kotlin/com/axians/accieaf/iam/application/TenantServiceTest.kt`.
  * **Mocking/Stubbing (Backend):**
    * **MockK** is the preferred library for creating mocks, stubs, and spies in Kotlin unit tests.
    * All external dependencies (e.g., other services, database repositories, network clients, file system interactions, system time if relevant) must be mocked or stubbed to ensure tests are isolated and run quickly.
    * **Axon Test Fixture:** Use for testing Axon Aggregates by providing a given-when-then style of test for command handlers and event sourcing logic.
    * **Additional Mockito Guidelines (if Mockito is used instead of MockK):**
      * Mockito is not the preferred library for new Kotlin tests (MockK SHOULD be used). However, legacy tests using Mockito still exist. If Mockito is used, STRICT STUBBING **MUST** be enabled via `@ExtendWith(MockitoExtension::class)` (JUnit 5) without further configuration – this activates Mockito's `Strictness.WARN` mode which fails the test on unnecessary stubbing and unverified interactions.
      * Use `lenient()` only as an explicit, *well-commented* exception when a particular stub really cannot be verified (e.g., setup in a reusable `@BeforeEach` where not every test uses the stubbed call). Blanket `lenient()` on whole test classes is **prohibited**.
      * Prefer the type-safe `argumentCaptor` / `argThat` APIs over `any()` with generics to avoid *Cannot infer type for this parameter* compilation errors in Kotlin. When the generic type is required, use the variant with an explicit type parameter, e.g. `ArgumentMatchers.any<Map<String, Any>>()`.
      * Always verify interactions on mocks with the most restrictive matcher possible (e.g., `verify(mock).someCall(expectedValue)` instead of a broad `any()` matcher).
      * After each test, call `verifyNoMoreInteractions(mock1, mock2, …)` (or enable `Strictness.STRICT_STUBS`) to catch forgotten verifications early.
      * Never stub methods that are never called – remove them or convert to `verify()` if the intention is to ensure they are **not** called. Unnecessary stubbing leads to `UnnecessaryStubbingException` when strictness is active.
      * When migrating a Mockito test that uses generics to Kotlin, avoid the raw `any()` import clash with Kotlin's `any{}` by using `ArgumentMatchers.any()` or adding `@file:Suppress("UNCHECKED_CAST")` as a last resort (and document why).
      * Consider gradually migrating Mockito based tests to MockK to leverage its Kotlin-idiomatic API and to reduce boilerplate.
  * **AI Agent Responsibility:** AI Agents tasked with code generation or modification must generate comprehensive unit tests covering all public methods of new/modified classes, significant logic paths (including happy paths and edge cases), and error conditions.

  ### Controller Tests (Backend)

  Controller logic in Spring MVC often interacts with the Servlet context (e.g., `ServletUriComponentsBuilder`). We distinguish two strategies:
  * **Direct unit tests**: Invoke controller methods directly with mocked dependencies. Configure `RequestContextHolder` with a `MockHttpServletRequest` to support URI building. Avoids loading the Spring context entirely and prevents infrastructure errors. For example, see `TenantControllerTest.kt`.
  * **WebMvcTest with MockMvc**: Use `@WebMvcTest` and `MockMvc` to simulate HTTP requests at the MVC layer. Provides full validation of serialization, request mapping, and filters. Requires mocking of all framework dependencies (JPA, security, interceptors) via `@MockBean` or custom test configurations. May encounter JPA/Persistence context errors; such tests can be disabled or require extensive mocking. In this project, `TenantControllerIntegrationTest.kt` is disabled due to these issues.

* **Integration Tests (Backend):**
  * **Scope:** Test the interaction and collaboration between several components or services within the application\'s boundary, or between the application and external infrastructure it directly controls (like a database). Examples:
    * API endpoint through to the service layer and (test) database.
    * Interaction between an Axon Command Handler, Event Store, and an Event Handler updating a read model.
    * Communication between different internal EAF modules if they have direct synchronous or asynchronous interfaces (beyond core event bus interactions for CQRS).
  * **Location & Naming (Backend):**
    * Typically reside in `src/test/kotlin/` alongside unit tests but may be distinguished by:
      * A specific naming convention (e.g., `*IntegrationTest.kt`).
      * Being placed in a dedicated package (e.g., `com.axians.accieaf.[module].integration`).
    * Alternatively, a separate Gradle source set (e.g., `src/integrationTest/kotlin`) can be configured if a stronger separation is desired.
  * **Environment & Dependencies:**
    * **Testcontainers:** Use Testcontainers to manage lifecycles of external dependencies like PostgreSQL instances for integration tests. This ensures tests run against a real database engine in a clean, isolated environment.
    * Spring Boot\'s testing support (`@SpringBootTest`) will be used to load application contexts for testing service interactions.
  * **AI Agent Responsibility:** AI Agents may be tasked with generating integration tests for key service interactions or API endpoints based on defined specifications, particularly where component collaboration is critical.

* **End-to-End (E2E) Tests (Primarily for Control Plane UI):**
  * **Scope:** Validate complete user flows or critical paths through the system from the end-user\'s perspective. For the ACCI EAF, this primarily applies to the Control Plane UI and its interaction with the `eaf-controlplane-api`.
  * **Tools:** **Playwright (`1.52.x`)** will be used for E2E testing of the Control Plane UI.
  * **Test Scenarios:** Based on user stories and acceptance criteria for the Control Plane UI features (e.g., logging in, creating a tenant, assigning a license, configuring an IdP).
  * **Execution:** E2E tests are more resource-intensive and will typically run less frequently than unit/integration tests (e.g., nightly builds, pre-release pipelines) but critical smoke tests might run on every PR.
  * **AI Agent Responsibility:** AI Agents may be tasked with generating E2E test stubs or scripts (e.g., Playwright page object models, basic test scenarios) based on user stories or UI specifications.

* **Test Coverage:**
  * **Target (as per PRD NFR 4a):**
    * Core EAF modules (`eaf-*`) aim for **100% unit test coverage for critical business logic**.
    * For new business logic developed within the EAF or applications based on it, a high unit test coverage of **>80% (line and branch)** is targeted.
  * **Measurement:** **JaCoCo** Gradle plugin will be used to measure and report code coverage for Kotlin/JVM code. Coverage reports will be generated as part of the CI build.
  * **Quality over Quantity:** While coverage targets are important, the quality, relevance, and effectiveness of tests are paramount. Tests must be meaningful and verify actual behavior and requirements.

* **Mocking/Stubbing Strategy (General):**
  * **Clarity and Maintainability:** Prefer test doubles (fakes, stubs) that improve the clarity and maintainability of tests over overly complex mocking setups with extensive behavior verification if the latter makes tests brittle or hard to understand.
  * **Focus:** Mocks are primarily for isolating the unit under test and verifying interactions with its direct collaborators. Stubs provide controlled inputs. Fakes provide lightweight implementations of dependencies.
  * **Speed and Reliability:** Strive for tests that are fast, reliable (no flakiness), and isolated from each other.

* **Test Data Management:**
  * **Unit Tests:** Test data should be created directly within the test methods or setup methods (`@BeforeEach`) and be specific to the test case. Use helper functions or builder patterns for creating complex test objects.
  * **Integration/E2E Tests:**
    * **Fixtures/Factories:** Develop test data factories or fixture loading mechanisms to create consistent and reusable test data sets.
    * **Database Seeding:** For integration tests involving databases, use mechanisms like SQL scripts run via Testcontainers, or application-level data seeding before test execution.
    * **Isolation:** Ensure test data is isolated between test runs and tests (e.g., by cleaning and re-initializing the database or test containers before each test class or method). Spring Boot\'s `@DirtiesContext` can be used where appropriate if application context state is modified.
    * **Liquibase for Test Schemas:** Liquibase can also be used to manage schemas for test databases used in integration tests.

* **Special for `eaf-iam` Story 3.1:** The initial Spring Boot/Liquibase integration test was replaced by a standalone JPA/Testcontainers integration test (`UserRepositoryJpaTest.kt`) that uses Hibernate `create-drop` DDL for schema auto-generation to avoid external schema dependencies; it validates CRUD operations, queries, constraints, pagination, and search on the `local_users` table.
