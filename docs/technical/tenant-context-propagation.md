# Tenant Context Propagation

This document describes the tenant context propagation mechanism implemented in the EAF (Enterprise Application Framework).

## Overview

The tenant context propagation mechanism allows EAF applications to maintain awareness of the current tenant throughout the entire request lifecycle, including synchronous and asynchronous processing, ensuring proper multi-tenancy isolation.

## How It Works

1. **Tenant ID Source**: The tenant ID is extracted from the HTTP header `X-Tenant-ID` at the beginning of each request.
2. **Validation**: The tenant ID is validated for format (must be a valid UUID) and existence (tenant must exist and be active).
3. **Storage**: The validated tenant ID is stored in a ThreadLocal variable via the `TenantContextHolder` for the duration of the request.
4. **Access**: Application code can access the current tenant ID from anywhere using `TenantContextHolder.getCurrentTenantId()`.
5. **Cleanup**: The tenant ID is automatically cleared from the ThreadLocal after request completion.
6. **Propagation**: Special support is provided for propagating the tenant context in:
   - Kotlin Coroutines (via `TenantCoroutineContext`)
   - Spring @Async methods (via `TenantTaskDecorator`)
   - Axon Framework messages (via message metadata and interceptors)

## Using Tenant Context in Your Code

### Accessing the Current Tenant ID

```kotlin
// In any service, repository, or other component
fun someBusinessLogic() {
    val tenantId = TenantContextHolder.getCurrentTenantId()
    if (tenantId != null) {
        // Perform tenant-specific operations
    } else {
        // Handle case where tenant context is not available
    }
}
```

### Propagating Tenant Context in Coroutines

```kotlin
// Capture current tenant context for coroutines
val tenantContext = TenantCoroutineContext.capture()

// Launch a coroutine with tenant context
launch(Dispatchers.IO + tenantContext) {
    // The tenant context is automatically available here
    val tenantId = TenantContextHolder.getCurrentTenantId()
    // Use tenantId...
}
```

### Using @Async with Tenant Context

```kotlin
// Use the tenantAwareTaskExecutor for @Async methods
@Async("tenantAwareTaskExecutor")
fun performAsyncOperation() {
    // Tenant context is propagated from the caller
    val tenantId = TenantContextHolder.getCurrentTenantId()
    // Use tenantId...
}
```

### Using Axon Framework with Tenant Context

With Axon Framework, the tenant context is automatically propagated through the message metadata:

```kotlin
// When sending commands
// The tenant ID is automatically added to message metadata
commandGateway.send(YourCommand(...))

// In command handlers
// The tenant ID is automatically extracted from message metadata and available in TenantContextHolder
@CommandHandler
fun handle(command: YourCommand) {
    val tenantId = TenantContextHolder.getCurrentTenantId()
    // Use tenantId...
}

// Same for event handlers
@EventHandler
fun on(event: YourEvent) {
    val tenantId = TenantContextHolder.getCurrentTenantId()
    // Use tenantId...
}

// And query handlers
@QueryHandler
fun handle(query: YourQuery): QueryResult {
    val tenantId = TenantContextHolder.getCurrentTenantId()
    // Use tenantId...
    return QueryResult(...)
}
```

## Best Practices

1. **Never set the tenant ID manually** unless you have a specific reason (e.g., in tests). Let the framework handle it.
2. **Always check for null** when retrieving the tenant ID, as it might not be available in all contexts.
3. **Clear the tenant context** after manually setting it, especially in background processes.
4. **Use the provided mechanisms** for coroutines, @Async methods, and Axon messages to ensure proper context propagation.
5. **Be cautious with manually created threads** as they won't automatically inherit the tenant context.

## Common Pitfalls

1. **Lost Context in New Threads**: Manual threads don't inherit ThreadLocal values. Use `TenantTaskDecorator` or capture and set the tenant ID explicitly.
2. **Incorrect Context in Background Jobs**: Jobs not initiated by a request may not have a tenant context. Set explicitly if needed.
3. **Context Leakage**: Failing to clear the tenant context can lead to incorrect tenant association in subsequent operations.
4. **WebFlux Compatibility**: The current implementation is for servlet-based applications. WebFlux requires a different approach.
5. **Axon Framework Saga**: In long-running Sagas, ensure the tenant context is explicitly stored and restored as part of the Saga state if needed beyond the initial handler.

## Error Handling

When a tenant context issue occurs, the framework behaves as follows:

- Missing tenant ID header: HTTP 400 Bad Request
- Invalid tenant ID format: HTTP 400 Bad Request
- Non-existent tenant: HTTP 403 Forbidden
- Inactive tenant: HTTP 403 Forbidden
- Invalid tenant in Axon message: TenantNotActiveException or InvalidTenantIdException

## Testing

When writing tests that involve tenant context:

```kotlin
// Set tenant context for test
@Before
fun setup() {
    TenantContextHolder.setTenantId(UUID.fromString("11111111-1111-1111-1111-111111111111"))
}

@After
fun cleanup() {
    TenantContextHolder.clear()
}
```

For testing coroutines:

```kotlin
runTest {
    val tenantId = UUID.fromString("11111111-1111-1111-1111-111111111111")
    TenantContextHolder.setTenantId(tenantId)
    
    withContext(TenantCoroutineContext.capture()) {
        // Your test code that uses tenant context
    }
}
```

For testing Axon message handlers:

```kotlin
// Create message with tenant ID in metadata
val message = GenericMessage(YourCommand(...))
    .withMetaData(mapOf("tenantId" to tenantId.toString()))

// Execute handler with simulated message handling
tenantMessageHandlerInterceptor.handle(unitOfWork, interceptorChain)

// Verify expected behavior
```
