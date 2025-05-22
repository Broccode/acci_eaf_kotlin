package com.acci.eaf.iam.domain.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.ManyToMany
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.util.UUID

@Entity
@Table(name = "permissions", uniqueConstraints = [UniqueConstraint(name = "uk_permission_name", columnNames = ["name"])])
class Permission(
    @Id
    @Column(name = "permission_id")
    val permissionId: UUID = UUID.randomUUID(),

    @Column(name = "name", nullable = false)
    val name: String,

    @Column(name = "description")
    var description: String? = null,

    @ManyToMany(mappedBy = "permissions", fetch = FetchType.LAZY)
    val roles: MutableSet<Role> = mutableSetOf(),
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Permission
        return permissionId == other.permissionId
    }

    override fun hashCode(): Int = permissionId.hashCode()

    override fun toString(): String = "Permission(permissionId=$permissionId, name='$name')"
}
