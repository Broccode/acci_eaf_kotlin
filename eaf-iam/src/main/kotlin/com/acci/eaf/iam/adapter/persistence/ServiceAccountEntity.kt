package com.acci.eaf.iam.adapter.persistence

import com.acci.eaf.iam.domain.model.ServiceAccountStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "service_accounts")
class ServiceAccountEntity(
    @Id
    @Column(name = "service_account_id")
    var serviceAccountId: UUID = UUID.randomUUID(),

    @Column(name = "tenant_id", nullable = false)
    var tenantId: UUID,

    @Column(name = "client_id", nullable = false)
    var clientId: String,

    @Column(name = "client_secret_hash", nullable = false)
    var clientSecretHash: String,

    @Column(name = "salt", nullable = false)
    var salt: String,

    @Column(name = "description", length = 1024)
    var description: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: ServiceAccountStatus,

    @Column(name = "created_at", nullable = false)
    var createdAt: OffsetDateTime,

    @Column(name = "expires_at")
    var expiresAt: OffsetDateTime? = null,
) {
    // Default constructor for JPA
    constructor() : this(
        serviceAccountId = UUID.randomUUID(),
        tenantId = UUID.randomUUID(),
        clientId = "",
        clientSecretHash = "",
        salt = "",
        status = ServiceAccountStatus.ACTIVE,
        createdAt = OffsetDateTime.now()
    )
}
