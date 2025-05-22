package com.acci.eaf.iam.application.port.api

import com.acci.eaf.iam.domain.model.Role
import java.util.UUID

/**
 * DTO für die Rolle.
 * Wird für die Kommunikation zwischen Adapter- und Anwendungsschicht verwendet.
 */
data class RoleDto(
    val roleId: UUID,
    val name: String,
    val description: String?,
    val tenantId: UUID?,
    val permissions: List<PermissionDto> = emptyList(),
) {
    companion object {
        /**
         * Konvertiert eine Role-Entity in ein RoleDto mit allen Berechtigungen.
         *
         * @param role Die zu konvertierende Role-Entity
         * @return Das erzeugte RoleDto
         */
        fun fromEntity(role: Role): RoleDto =
            RoleDto(
                roleId = role.roleId,
                name = role.name,
                description = role.description,
                tenantId = role.tenantId,
                permissions = role.permissions.map { PermissionDto.fromEntity(it) }
            )

        /**
         * Konvertiert eine Role-Entity in ein RoleDto ohne Berechtigungen.
         *
         * @param role Die zu konvertierende Role-Entity
         * @return Das erzeugte RoleDto ohne Berechtigungen
         */
        fun fromEntityWithoutPermissions(role: Role): RoleDto =
            RoleDto(
                roleId = role.roleId,
                name = role.name,
                description = role.description,
                tenantId = role.tenantId
            )
    }
}
