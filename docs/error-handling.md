# 12. Error Handling Strategy
>
> This document is a granulated shard from the main "ACCI-EAF-Architecture.md" focusing on "Error Handling Strategy".

A robust error handling strategy is crucial for the stability, maintainability, and diagnosability of the ACCI EAF and applications built upon it. This section outlines the general approach, logging practices, and specific error handling patterns.

* **General Approach:**
  * **Exceptions as Primary Mechanism:** Exceptions will be the primary mechanism for signaling and propagating errors within the application code. Kotlin\'s standard exceptions and Java\'s exception hierarchy will be used.
  * **Custom Exception Hierarchy:** A custom exception hierarchy will be defined, extending standard exceptions (e.g., `RuntimeException`, `IllegalArgumentException`). This hierarchy will include:
    * A base application exception (e.g., `AcciEafException`).
    * Specific business exceptions related to different domains (e.g., `TenantNotFoundException` from `eaf-multitenancy`, `UserAuthenticationException` from `eaf-iam`, `LicenseValidationException` from `eaf-licensing`).
    * Technical/integration exceptions (e.g., `ExternalServiceUnavailableException`, `ConfigurationException`).
  * **Clear Error Messages:** Exceptions should carry clear, concise messages intended for developers/logs, and potentially unique error codes for easier tracking and reference.
  * **Fail Fast:** For unrecoverable errors or invalid states, the system should fail fast to prevent further inconsistent processing.

* **Logging:**
  * **Library/Method:** **Logback** (provided by default with Spring Boot) will be the primary logging framework. It will be configured for **structured logging in JSON format** to facilitate easier parsing, searching, and analysis by log management systems. The `logstash-logback-encoder` library can be used to enhance JSON formatting and include custom fields.
  * **Log Levels:** Standard log levels will be used consistently:
    * `ERROR`: Critical errors that prevent normal operation or lead to data inconsistency. Significant failures requiring immediate attention. Includes stack traces.
    * `WARN`: Potential problems or unusual situations that do not (yet) halt processing but might indicate future issues or require investigation (e.g., retrying an operation, configuration issues, deprecation warnings).
    * `INFO`: High-level messages tracking the application\'s lifecycle and significant business operations (e.g., application startup, major service calls, tenant creation, successful license activation).
    * `DEBUG`: Fine-grained information useful for developers during debugging (e.g., method entry/exit, variable values, detailed flow tracing). Should be disabled in production by default but configurable.
    * `TRACE`: Extremely detailed diagnostic information, typically only enabled for specific troubleshooting scenarios.
  * **Contextual Information in Logs:** All log entries (especially `INFO`, `WARN`, `ERROR`) should include crucial contextual information:
    * **Timestamp** (ISO 8601 format).
    * **Log Level**.
    * **Thread Name**.
    * **Logger Name** (typically the class name).
    * **Message**.
    * **Stack Trace** (for exceptions at `ERROR` and optionally `WARN` level).
    * **Correlation ID (Trace ID):** A unique ID generated at the start of a request (e.g., incoming API call) and propagated through all subsequent service calls and log messages related to that request. This is critical for tracing distributed operations. Spring Cloud Sleuth (even without Zipkin for tracing if not used) or a similar mechanism (e.g., MDC) will be used.
    * **Tenant ID:** (If applicable to the context and not sensitive in the log message itself).
    * **User ID / Principal Name:** (If applicable, ensuring PII is handled according to security policies).
    * **Operation Name / Service Name:** Identifying the specific operation or component.
    * **Key Parameters (Sanitized):** Relevant input parameters or identifiers, ensuring sensitive data (passwords, secrets, PII) is masked or omitted.

* **Specific Handling Patterns:**
  * **External API Calls / Integrations (HTTP, LDAP, SMTP, etc.):**
    * **Timeouts:** Configure appropriate connection and read timeouts for all external calls to prevent indefinite blocking. Libraries like `OkHttp`, `RestTemplate` (with configuration), or specific protocol libraries (e.g., JavaMail, UnboundID LDAP SDK) provide mechanisms for this.
    * **Retries:** For transient network issues or temporary unavailability of external services, implement automatic retry mechanisms with exponential backoff and jitter. **Spring Retry** (`@Retryable`) is the preferred library for this.
    * **Circuit Breakers:** For integrations that are prone to failures or high latency, a Circuit Breaker pattern will be implemented using **Resilience4j**. This prevents cascading failures by stopping requests to a failing service for a period. Fallback mechanisms (e.g., returning cached data, default values, or a specific error response) should be considered.
    * **Error Mapping:** Errors from external services (e.g., HTTP 4xx/5xx status codes, LDAP error codes, SMTP exceptions) will be caught and mapped to specific internal `AcciEafException` subtypes, providing a consistent error handling approach within the EAF. Sensitive details from external errors should not be directly exposed to end-users.
  * **Internal Business Logic Exceptions:**
    * Custom domain-specific exceptions (e.g., `InvalidTenantStatusException`, `DuplicateUsernameException`, `LicenseExpiredException`) will be thrown by business logic in aggregates or domain services.
    * **API Layer Error Handling (e.g., in `eaf-controlplane-api`, `eaf-license-server`):** Spring Boot\'s `@ControllerAdvice` and `@ExceptionHandler` mechanisms will be used to globally handle these custom exceptions (and standard Spring exceptions). These handlers will:
      * Log the full error with stack trace at `ERROR` level.
      * Transform the exception into a standardized JSON error response for the API client, including a user-friendly message, a unique error code/ID (for support), and appropriate HTTP status code (e.g., 400 for validation errors, 404 for not found, 403 for forbidden, 409 for conflicts, 500 for unexpected server errors).
  * **Axon Framework Command Handling:**
    * Exceptions thrown by `@CommandHandler` methods in Aggregates will be propagated back to the `CommandGateway` caller.
    * These exceptions should be specific business exceptions. The API layer (or service layer dispatching the command) will then handle these as described above.
    * Axon also allows for `CommandDispatchInterceptor` and `CommandHandlerInterceptor` to add cross-cutting error handling if needed.
  * **Axon Framework Event Handling / Projections:**
    * Errors occurring within `@EventHandler` methods (e.g., when updating read models) require careful consideration. Axon Framework provides configurable error handlers for event processors (e.g., `ListenerInvocationErrorHandler`, `ErrorHandler`).
    * **Strategy:**
      * For transient errors (e.g., database connection issue during read model update), a retry mechanism might be configured for the event processor.
      * For non-transient errors (e.g., an event that consistently fails to be processed due to a bug in the handler or unexpected data), the event should typically be moved to a Dead-Letter Queue (DLQ) after a few failed attempts, or the event processor might be stopped to prevent blocking further event processing. This requires monitoring of the DLQ or processor status.
      * Logging of such event processing failures is critical.
  * **Transaction Management:**
    * **Local Transactions (e.g., Read Model Updates):** Standard Spring `@Transactional` annotations will be used to manage ACID transactions for database operations within event projectors or services interacting directly with PostgreSQL for stateful data. If an event handler processes multiple updates, these should ideally be within a single transaction.
    * **Distributed Transactions / Sagas (Consistency across Aggregates):** For business processes that span multiple aggregates and require eventual consistency, **Axon Sagas** will be used. Sagas listen to events and dispatch new commands to orchestrate the process. Sagas must implement compensation logic (compensating actions/commands) to handle failures in any step of the distributed transaction, ensuring the system can be brought back to a consistent state.
