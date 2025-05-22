package com.acci.eaf.iam.domain.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.util.UUID

@Entity
@Table(
    name = "roles",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_role_name_tenant", columnNames = ["name", "tenant_id"])
    ]
)
class Role(
    @Id
    @Column(name = "role_id")
    val roleId: UUID = UUID.randomUUID(),

    @Column(name = "name", nullable = false)
    var name: String,

    @Column(name = "description")
    var description: String? = null,

    @Column(name = "tenant_id")
    val tenantId: UUID? = null,

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "role_permissions",
        joinColumns = [JoinColumn(name = "role_id")],
        inverseJoinColumns = [JoinColumn(name = "permission_id")]
    )
    val permissions: MutableSet<Permission> = mutableSetOf(),

    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    val users: MutableSet<User> = mutableSetOf(),
) {
    // Helper methods for permission management
    fun addPermission(permission: Permission): Role {
        permissions.add(permission)
        return this
    }

    fun removePermission(permission: Permission): Role {
        permissions.remove(permission)
        return this
    }

    fun hasPermission(permissionName: String): Boolean = permissions.any { it.name == permissionName }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Role
        return roleId == other.roleId
    }

    override fun hashCode(): Int = roleId.hashCode()

    override fun toString(): String = "Role(roleId=$roleId, name='$name', tenantId=$tenantId)"
}
