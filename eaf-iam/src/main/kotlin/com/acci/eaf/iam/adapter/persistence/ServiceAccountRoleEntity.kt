package com.acci.eaf.iam.adapter.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "service_account_roles")
class ServiceAccountRoleEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    val id: UUID = UUID.randomUUID(),

    @Column(name = "service_account_id", nullable = false)
    val serviceAccountId: UUID,

    @Column(name = "role_id", nullable = false)
    val roleId: UUID,
) {
    // Default constructor for JPA
    constructor() : this(
        id = UUID.randomUUID(),
        serviceAccountId = UUID.randomUUID(),
        roleId = UUID.randomUUID()
    )
}
