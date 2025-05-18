package com.acci.eaf.multitenancy.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.time.Instant
import java.util.UUID
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp

/**
 * Represents a tenant in the EAF multitenancy system.
 *
 * A tenant is an isolated unit within the system with its own data, users and configuration.
 * Each tenant is identified by a unique ID and has a unique name.
 */
@Entity
@Table(
    name = "tenants",
    indexes = [
        Index(name = "idx_tenant_name", columnList = "name", unique = true)
    ]
)
class Tenant(
    /**
     * The unique identifier for this tenant.
     * Generated automatically on creation and immutable.
     */
    @Id
    @Column(name = "tenant_id", updatable = false, nullable = false)
    val tenantId: UUID = UUID.randomUUID(),

    /**
     * The unique name of this tenant.
     * Must be 3-100 characters, alphanumeric with hyphens.
     */
    @field:NotBlank
    @field:Size(min = 3, max = 100, message = "Tenant name must be between 3 and 100 characters")
    @field:Pattern(
        regexp = "^[a-zA-Z0-9\\-]+$",
        message = "Tenant name can only contain alphanumeric characters and hyphens"
    )
    @Column(unique = true, nullable = false)
    var name: String,

    /**
     * The current status of this tenant.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: TenantStatus = TenantStatus.PENDING_VERIFICATION,

    /**
     * Timestamp of when this tenant was created.
     * Set automatically on creation.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    /**
     * Timestamp of when this tenant was last updated.
     * Updated automatically on modification.
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),
)

/**
 * Possible statuses for a tenant.
 *
 * PENDING_VERIFICATION: Initial state, tenant is created but not yet verified/activated
 * ACTIVE: Tenant is active and can be used
 * INACTIVE: Tenant is temporarily deactivated but can be reactivated
 * SUSPENDED: Tenant is suspended due to policy violation or other issues
 * ARCHIVED: Tenant is archived (soft-deleted)
 */
enum class TenantStatus {
    PENDING_VERIFICATION,
    ACTIVE,
    INACTIVE,
    SUSPENDED,
    ARCHIVED,
} 
