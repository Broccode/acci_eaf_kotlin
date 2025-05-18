package com.acci.eaf.multitenancy.exception

import java.util.UUID

/**
 * Base exception for all tenant-related exceptions.
 */
sealed class TenantException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

/**
 * Exception thrown when a tenant with the specified name already exists.
 */
class TenantNameAlreadyExistsException(name: String) : TenantException("Tenant with name '$name' already exists")

/**
 * Exception thrown when a tenant with the specified ID is not found.
 */
class TenantNotFoundException(tenantId: UUID) : TenantException("Tenant with ID '$tenantId' not found")

/**
 * Exception thrown when a tenant with the specified name is not found.
 */
class TenantNotFoundByNameException(name: String) : TenantException("Tenant with name '$name' not found")

/**
 * Exception thrown when an invalid tenant status transition is attempted.
 */
class InvalidTenantStatusTransitionException(currentStatus: String, attemptedStatus: String) :
    TenantException("Cannot transition tenant from status '$currentStatus' to '$attemptedStatus'")

/**
 * Exception thrown when tenant name validation fails.
 */
class InvalidTenantNameException(name: String, reason: String) : TenantException("Tenant name '$name' is invalid: $reason")
