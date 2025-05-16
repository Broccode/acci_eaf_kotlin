# Epic 1: EAF Foundational Setup & Core Infrastructure
>
> This document is a granulated shard from the main "ACCI-EAF-PRD.md" focusing on "Epic 1: EAF Foundational Setup & Core Infrastructure".

*Description:* Establishes the initial ACCI EAF project structure within the Gradle monorepo, including `build-logic`, core configurations, CI/CD pipeline setup (initially for a simple target environment), and the basic `eaf-core` module with initial Axon Framework configuration. This Epic delivers a runnable, albeit functionally empty, framework foundation.
*Value:* Foundational for all further development, enables early CI/CD and target environment validation.

**Story 1.1: Monorepo Project Setup with Gradle**

* **As a** Developer, **I want** a new Gradle monorepo initialized for the ACCI EAF project, **so that** all EAF modules can be managed centrally and consistently.
* **Acceptance Criteria (ACs):**
    1. A Git repository for the ACCI EAF project is locally initialized, and a comprehensive `.gitignore` file (ignoring typical IDE, OS, build artifacts, and sensitive files) is present.
    2. A Gradle root project is set up with a `settings.gradle.kts` file declaring `build-logic` and the initial `eaf-core` module as sub-projects. The Gradle version is set to a current, stable version.
    3. A `build-logic` module is created with a basic structure for Gradle Convention Plugins or Version Catalogs to centrally and consistently manage shared build configurations (e.g., Java/Kotlin version, compiler options, standard plugins like Checkstyle/Klint) and dependency versions.
    4. The `eaf-core` module is created as a valid Gradle sub-project (Kotlin-based) and configured to potentially use configurations/dependencies from `build-logic`.
    5. The command `./gradlew build` (using the Gradle Wrapper) executes successfully without compilation errors or significant warnings indicating misconfiguration. All defined linting and style checks (if initially configured) pass. Expected build artifacts (e.g., empty JAR files, if applicable) are produced.
    6. The Gradle Wrapper files (`gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar`, `gradle-wrapper.properties`) are correctly configured, present in the repository, and executable, ensuring consistent builds across different development environments. The wrapper is configured to use a specific Gradle version suitable for the project.
    7. A basic `README.md` file is present in the root directory, containing the project name, a brief description of the EAF, development environment prerequisites (JDK version, etc.), and basic instructions for cloning, building, and running the project.
    8. The setup is reproducible on a clean development environment (with the correctly installed JDK as per project requirements and without global Gradle installations that might interfere) and results in a successful build.

**Story 1.2: Basic `eaf-core` Module with Spring Boot & Axon Configuration**

* **As a** Developer, **I want** the `eaf-core` module to be a runnable Spring Boot application with initial Axon Framework configuration, **so that** the foundational CQRS/ES capabilities can be established and tested.
* **Acceptance Criteria (ACs):**
    1. The `eaf-core` module includes the necessary dependencies for Spring Boot (e.g., `spring-boot-starter-web` for web capabilities, `spring-boot-starter-actuator` for monitoring endpoints), managed via `build-logic`.
    2. The `eaf-core` module includes the core Axon Framework dependencies (e.g., `axon-spring-boot-starter`), managed via `build-logic`. The Axon version is set to a current, stable version.
    3. A basic Axon configuration is present: Command Bus, Query Bus, and Event Bus/Event Store (initially configured for in-memory operation for simple tests or a basic, local PostgreSQL connection for development tests) are correctly initialized as Spring Beans. Errors during the initialization of these Axon components (e.g., missing configuration, connection problems to the Event Store) lead to informative error messages at application startup and a failing health check.
    4. The `eaf-core` application can be started successfully without runtime errors using `./gradlew :eaf-core:bootRun` (or equivalent).
    5. The Spring Boot Actuator Health endpoint (`/actuator/health`) is available, reports an `UP` status, and includes basic health information for Axon components (e.g., Event Store connectivity, status of Event Processors if any are already defined).
    6. A simple test Command (e.g., `PingCommand`), a corresponding Command Handler (e.g., producing a `PongEvent`), and an Event Handler (e.g., logging the `PongEvent`) are implemented in the `eaf-core` module. Sending the command (e.g., via a test REST endpoint or an integration test) results in the expected event publication and processing. Errors in command processing (e.g., no handler found, validation error in handler) lead to a defined, traceable error response (e.g., standardized JSON error structure with error code and message) or a logged exception.
    7. The `application.properties` or `application.yml` file in the `eaf-core` module is structured (e.g., using Spring Profiles for `dev`, `test`, `prod`), contains placeholders or environment-specific configurations for Axon, database connection, etc., and is well-commented.
    8. A basic logging configuration (e.g., via Logback, configured in `eaf-observability` and used by `eaf-core`) is present and outputs informative, structured (e.g., JSON) log information during startup and execution of the test command/event, including timestamp, log level, thread name, and logger name.

**Story 1.3: Initial CI/CD Pipeline Setup**

* **As a** Development Team, **I want** a basic CI/CD pipeline configured for the ACCI EAF monorepo, **so that** code changes are automatically built, tested (initially unit tests and code quality checks), and a deployable artifact (e.g., Docker image for `eaf-core`) is produced.
* **Acceptance Criteria (ACs):**
    1. The CI pipeline (e.g., GitHub Actions Workflow, Jenkinsfile, GitLab CI YAML) is versioned as code within the monorepo and is automatically triggered on every push to the main branch (e.g., `main` or `develop`) and on every pull/merge request to this branch.
    2. The pipeline executes the command `./gradlew build cleanCheck` (or equivalent Gradle tasks) to compile all modules, perform static code analysis (linting, style checks via Checkstyle/Klint), and run unit tests. The pipeline fails if tests fail or configured quality gates (e.g., minimum test coverage, no critical code smells) are not met. Detailed test results and analysis reports are available as pipeline artifacts.
    3. The pipeline generates versioned build artifacts (e.g., JAR files for the modules) that include a manifest file with build information (e.g., Git commit ID, build timestamp, semantic version based on Git tags or build parameters).
    4. (Optional for initial setup, but recommended) The pipeline builds a Docker image for the `eaf-core` module using a multi-stage Dockerfile (optimized for minimal size, using a non-root user, correct layer structure). The image is tagged with the build version and published to an internal Docker registry (authentication to the registry is handled securely via secrets). A basic vulnerability scan of the Docker image (e.g., with Trivy) is performed, and critical findings lead to pipeline failure.
    5. The status of the pipeline (success/failure of each step) is clearly signaled in the respective CI/CD system and ideally also in the Git repository (e.g., as a commit status or pull request check). Notifications for failures are sent to the development team.
    6. Detailed and structured logs of the pipeline execution are accessible for at least 7 days to enable efficient troubleshooting.
    7. The pipeline is configured to use caching mechanisms (e.g., for Gradle dependencies, Docker layers) to optimize build times without compromising correctness.
    8. The pipeline handles errors in individual steps (e.g., temporary failure of the Docker registry) robustly (e.g., through retry mechanisms for certain operations) and reports them clearly.

---
