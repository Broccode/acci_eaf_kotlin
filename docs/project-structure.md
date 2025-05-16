# 6. Project Structure
>
> This document is a granulated shard from the main "ACCI-EAF-Architecture.md" focusing on "Project Structure".

The ACCI EAF is developed as a Monorepo managed with Git. Gradle is used as the build system, orchestrating the build of all modules (Gradle sub-projects). The following diagram and descriptions outline the proposed project folder structure. This structure is designed to support the modular monolith architecture and separate concerns effectively.

```plaintext
ACCI-EAF/
├── .github/                    # CI/CD workflows (e.g., GitHub Actions)
│   └── workflows/
│       └── ci.yml
├── .vscode/                    # VSCode settings (optional)
│   └── settings.json
├── build-logic/                # Central Gradle build logic (convention plugins)
│   └── src/main/kotlin/        # Custom tasks, plugins for build consistency
├── docs/                       # Project documentation
│   ├── ACCI-EAF-PRD.md
│   ├── ACCI-EAF-Architecture.md
│   └── adr/                    # Architectural Decision Records
├── gradle/                     # Gradle wrapper files
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── eaf-core/                   # Core EAF module (foundational APIs, CQRS/ES base)
│   ├── src/main/kotlin/        # Kotlin source code
│   ├── src/main/resources/     # Resource files
│   ├── src/test/kotlin/        # Unit and integration tests for this module
│   └── build.gradle.kts        # Module-specific Gradle build script
├── eaf-iam/                    # Identity & Access Management module
│   └── ... (similar structure to eaf-core)
├── eaf-multitenancy/           # Multitenancy module
│   └── ...
├── eaf-licensing/              # Licensing module
│   └── ...
├── eaf-observability/          # Observability module
│   └── ...
├── eaf-internationalization/   # Internationalization (i18n) module
│   └── ...
├── eaf-plugin-system/          # Plugin system infrastructure module
│   └── ...
├── eaf-cli/                    # Optional: CLI tools for EAF developers
│   └── ...
├── app-example-module/         # Optional: Example application/module using the EAF
│   └── ...
├── eaf-controlplane-api/       # Optional: Backend API for the Control Plane (Spring Boot application)
│   ├── src/main/kotlin/        # Kotlin source for the API application
│   ├── src/main/resources/
│   ├── src/test/kotlin/
│   └── build.gradle.kts
├── eaf-license-server/         # Optional: License Server application (built on EAF)
│   ├── src/main/kotlin/        # Kotlin source for the License Server application
│   ├── src/main/resources/
│   ├── src/test/kotlin/
│   └── build.gradle.kts
├── controlplane-ui/            # Optional: Frontend React application for the Control Plane
│   ├── public/
│   ├── src/                    # React component sources, etc.
│   ├── package.json
│   ├── tsconfig.json
│   └── build.gradle.kts        # Optional: Gradle build for frontend (e.g., using Node plugin for integration)
├── test/                       # Top-level tests (e.g., E2E tests spanning multiple modules) - (Optional)
│   └── e2e/
├── .env.example                # Example environment variables
├── .gitattributes              # Git attributes for line endings, etc.
├── .gitignore                  # Specifies intentionally untracked files that Git should ignore
├── build.gradle.kts            # Root Gradle build file (common configurations, plugin versions)
├── settings.gradle.kts         # Gradle settings file (declares all modules/sub-projects)
├── gradlew                     # Gradle wrapper script for Unix-like systems
├── gradlew.bat                 # Gradle wrapper script for Windows
└── README.md                   # Project overview, setup, and usage instructions
```

### 6.1 Key Directory Descriptions

* **`ACCI-EAF/`**: The root directory of the monorepo.
* **`.github/workflows/`**: Contains CI/CD pipeline definitions (e.g., for GitHub Actions), including build, test, and potentially release workflows.
* **`build-logic/`**: Houses custom Gradle convention plugins and shared build configurations to ensure consistency across all modules (e.g., Kotlin versions, common dependencies, compiler options). This is a standard Gradle approach for managing complex builds.
* **`docs/`**: Contains all project-related documentation, including this Architecture Document, the PRD, and potentially ADRs (Architectural Decision Records) in the `docs/adr/` subdirectory, diagrams, etc.
* **`gradle/wrapper/`**: Contains the Gradle Wrapper, allowing developers to build the project with a consistent Gradle version without needing a system-wide installation.
* **`eaf-*` (e.g., `eaf-core/`, `eaf-iam/`)**: These directories represent the individual modules (Gradle sub-projects) of the ACCI EAF. Each module typically follows a standard Kotlin/Gradle project structure:
  * **`src/main/kotlin/`**: Contains the main Kotlin source code for the module.
    * Within this, Hexagonal Architecture principles should be applied by organizing code into packages like `domain` (core business logic, entities, value objects, domain events), `application` (use cases, application services orchestrating domain logic), and `adapters` (or `infrastructure`) for implementations of ports (e.g., REST controllers, database repositories, event listeners).
  * **`src/main/resources/`**: Contains resource files for the module (e.g., Spring Boot `application.yml` specific to a module if it's a runnable app, database migration scripts, i18n bundles).
  * **`src/test/kotlin/`**: Contains unit and integration tests for the module's code. Test file organization should mirror the `src/main/kotlin/` structure.
  * **`build.gradle.kts`**: The Gradle build script specific to this module, declaring its dependencies, plugins, and build configurations.
* **`eaf-controlplane-api/`**: A specific Spring Boot application module providing the backend for the administrative UI.
* **`eaf-license-server/`**: Another specific Spring Boot application module, built using EAF principles, to handle online license operations.
* **`controlplane-ui/`**: Contains the source code for the React-based frontend of the Control Plane. This is a standard Node.js project structure (e.g., created with Create React App or similar). Its detailed internal structure would be defined in a separate frontend architecture document if one were created. It might have its own `build.gradle.kts` if its build is integrated into the main Gradle build.
* **`test/e2e/`**: (Optional) For end-to-end tests that validate workflows across multiple modules or the entire system.
* **`build.gradle.kts` (root)**: The main Gradle build file at the root of the project. It typically defines common configurations, plugin versions (using the plugin management block), and applies plugins to sub-projects.
* **`settings.gradle.kts`**: The Gradle settings file. It is crucial as it declares all the sub-projects (modules like `eaf-core`, `eaf-iam`, etc.) that are part of the multi-project build.

### 6.2 Notes

* **Build Output:** Compiled artifacts (JARs, WARs if any) for each module will typically be found in their respective `build/libs/` directories. Runnable applications (like `eaf-controlplane-api` or `eaf-license-server`) will produce executable JARs.
* **IDE Integration:** This structure is standard for Gradle multi-project builds and should be well-supported by IDEs like IntelliJ IDEA (with excellent Kotlin and Gradle support).
* **Hexagonal Structure within Modules:** As mentioned in "Key Directory Descriptions", it's expected that individual EAF modules (especially those containing significant business logic like `eaf-iam` or `eaf-licensing`) will internally adopt a package structure reflecting Hexagonal Architecture (e.g., `com.axians.accieaf.[module].domain`, `com.axians.accieaf.[module].application`, `com.axians.accieaf.[module].adapter.rest`, `com.axians.accieaf.[module].adapter.persistence`). This will be further detailed in the "Coding Standards" section.
