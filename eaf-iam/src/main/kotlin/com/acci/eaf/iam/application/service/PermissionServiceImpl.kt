package com.acci.eaf.iam.application.service

import com.acci.eaf.iam.adapter.persistence.PermissionRepository
import com.acci.eaf.iam.application.port.api.PermissionDto
import com.acci.eaf.iam.application.port.api.PermissionService
import com.acci.eaf.iam.domain.model.Permission
import java.util.UUID
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class PermissionServiceImpl(private val permissionRepository: PermissionRepository) : PermissionService {

    @Transactional(readOnly = true)
    override fun getPermissionById(permissionId: UUID): PermissionDto? =
        permissionRepository.findById(permissionId)
            .map { toDto(it) }
            .orElse(null)

    @Transactional(readOnly = true)
    override fun getPermissionByName(name: String): PermissionDto? =
        permissionRepository.findByName(name)
            .map { toDto(it) }
            .orElse(null)

    @Transactional(readOnly = true)
    override fun getAllPermissions(pageable: Pageable): Page<PermissionDto> = permissionRepository.findAll(pageable).map { toDto(it) }

    @Transactional(readOnly = true)
    override fun getPermissionsByRole(roleId: UUID): List<PermissionDto> = permissionRepository.findByRoleId(roleId).map { toDto(it) }

    @Transactional(readOnly = true)
    override fun getEffectivePermissionsByUser(userId: String): Set<String> =
        permissionRepository.findEffectivePermissionsByUserId(UUID.fromString(userId))
            .map { it.name }
            .toSet()

    @Transactional(readOnly = true)
    override fun searchPermissionsByName(namePart: String, pageable: Pageable): Page<PermissionDto> =
        permissionRepository.findByNameContainingIgnoreCase(namePart, pageable).map { toDto(it) }

    /**
     * Konvertiert eine Permission-Entity in ein PermissionDto.
     */
    private fun toDto(permission: Permission): PermissionDto =
        PermissionDto(
            permissionId = permission.permissionId,
            name = permission.name,
            description = permission.description
        )
}
