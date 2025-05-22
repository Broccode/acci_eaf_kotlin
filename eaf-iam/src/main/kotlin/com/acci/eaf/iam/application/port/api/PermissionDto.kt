package com.acci.eaf.iam.application.port.api

import com.acci.eaf.iam.domain.model.Permission
import java.util.UUID

/**
 * DTO für die Berechtigung.
 * Wird für die Kommunikation zwischen Adapter- und Anwendungsschicht verwendet.
 */
data class PermissionDto(val permissionId: UUID, val name: String, val description: String?) {
    companion object {
        /**
         * Konvertiert eine Permission-Entity in ein PermissionDto.
         *
         * @param permission Die zu konvertierende Permission-Entity
         * @return Das erzeugte PermissionDto
         */
        fun fromEntity(permission: Permission): PermissionDto =
            PermissionDto(
                permissionId = permission.permissionId,
                name = permission.name,
                description = permission.description
            )
    }
}
