package com.acci.eaf.iam.application.service

import com.acci.eaf.iam.adapter.persistence.PermissionRepository
import com.acci.eaf.iam.adapter.persistence.RoleRepository
import com.acci.eaf.iam.adapter.persistence.UserRepository
import com.acci.eaf.iam.application.port.api.CreateRoleCommand
import com.acci.eaf.iam.application.port.api.PermissionDto
import com.acci.eaf.iam.application.port.api.RoleDto
import com.acci.eaf.iam.application.port.api.RoleService
import com.acci.eaf.iam.application.port.api.UpdateRoleCommand
import com.acci.eaf.iam.audit.AuditService
import com.acci.eaf.iam.domain.exception.PermissionNotFoundException
import com.acci.eaf.iam.domain.exception.RoleAlreadyExistsException
import com.acci.eaf.iam.domain.exception.RoleNotFoundException
import com.acci.eaf.iam.domain.exception.UserNotFoundException
import com.acci.eaf.iam.domain.model.Role
import java.util.UUID
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class RoleServiceImpl(
    private val roleRepository: RoleRepository,
    private val permissionRepository: PermissionRepository,
    private val userRepository: UserRepository,
    private val auditService: AuditService,
) : RoleService {

    override fun createRole(command: CreateRoleCommand): RoleDto {
        // Prüfen, ob eine Rolle mit dem Namen bereits existiert
        if (roleRepository.existsByNameAndTenantId(command.name, command.tenantId)) {
            throw RoleAlreadyExistsException(command.name, command.tenantId)
        }

        val role = Role(
            name = command.name,
            description = command.description,
            tenantId = command.tenantId
        )

        val savedRole = roleRepository.save(role)

        // Audit-Log-Eintrag erstellen
        auditService.logRoleCreated(savedRole)

        return toDto(savedRole)
    }

    override fun updateRole(command: UpdateRoleCommand): RoleDto {
        val role = roleRepository.findById(command.roleId)
            .orElseThrow { RoleNotFoundException("Role not found: $command.roleId", command.roleId) }

        // Wenn der Name geändert wird, prüfen, ob er bereits verwendet wird
        if (command.name != role.name &&
            roleRepository.existsByNameAndTenantId(command.name, role.tenantId)
        ) {
            throw RoleAlreadyExistsException(command.name, role.tenantId)
        }

        val previousName = role.name
        role.name = command.name
        role.description = command.description

        val updatedRole = roleRepository.save(role)

        // Audit-Log-Eintrag erstellen
        auditService.logRoleUpdated(updatedRole, previousName)

        return toDto(updatedRole)
    }

    override fun deleteRole(roleId: UUID) {
        val role = roleRepository.findById(roleId)
            .orElseThrow { RoleNotFoundException("Role not found: $roleId", roleId) }

        roleRepository.delete(role)

        // Audit-Log-Eintrag erstellen
        auditService.logRoleDeleted(role)
    }

    override fun addPermissionToRole(roleId: UUID, permissionId: UUID): RoleDto {
        val role = roleRepository.findById(roleId)
            .orElseThrow { RoleNotFoundException("Role not found: $roleId", roleId) }

        val permission = permissionRepository.findById(permissionId)
            .orElseThrow { PermissionNotFoundException(permissionId) }

        role.addPermission(permission)

        val updatedRole = roleRepository.save(role)

        // Audit-Log-Eintrag erstellen
        auditService.logPermissionAddedToRole(role, permission)

        return toDto(updatedRole)
    }

    override fun removePermissionFromRole(roleId: UUID, permissionId: UUID): RoleDto {
        val role = roleRepository.findById(roleId)
            .orElseThrow { RoleNotFoundException("Role not found: $roleId", roleId) }

        val permission = permissionRepository.findById(permissionId)
            .orElseThrow { PermissionNotFoundException(permissionId) }

        role.removePermission(permission)

        val updatedRole = roleRepository.save(role)

        // Audit-Log-Eintrag erstellen
        auditService.logPermissionRemovedFromRole(role, permission)

        return toDto(updatedRole)
    }

    @Transactional(readOnly = true)
    override fun getRoleById(roleId: UUID): RoleDto {
        val role = roleRepository.findById(roleId)
            .orElseThrow { RoleNotFoundException("Role not found: $roleId", roleId) }
        return toDto(role)
    }

    @Transactional(readOnly = true)
    override fun getGlobalRoles(pageable: Pageable): Page<RoleDto> = roleRepository.findByTenantId(null, pageable).map { toDto(it) }

    @Transactional(readOnly = true)
    override fun getRolesByTenant(tenantId: UUID, pageable: Pageable): Page<RoleDto> =
        roleRepository.findByTenantId(tenantId, pageable).map { toDto(it) }

    @Transactional(readOnly = true)
    override fun getAvailableRolesForTenant(tenantId: UUID, pageable: Pageable): Page<RoleDto> =
        roleRepository.findAllAvailableForTenant(tenantId, pageable).map { toDto(it) }

    @Transactional(readOnly = true)
    override fun getRolesByUser(userId: String, tenantId: UUID?): List<RoleDto> {
        // TODO: Implement filtering by tenantId if necessary and confirm repository method.
        // Assuming roleRepository.findByUserId still expects a UUID.
        return roleRepository.findByUserId(UUID.fromString(userId)).map { toDto(it) }
    }

    override fun assignRoleToUser(
        userId: String,
        roleId: UUID,
        tenantId: UUID?,
    ) {
        val user = userRepository.findById(UUID.fromString(userId))
            .orElseThrow { UserNotFoundException("User not found: $userId", userId) }

        val role = roleRepository.findById(roleId)
            .orElseThrow { RoleNotFoundException("Role not found: $roleId", roleId) }

        // Prüfen, ob die Rolle für den Tenant des Benutzers verfügbar ist
        // The tenantId parameter for this method might be used here for additional validation if role.tenantId is null (global role)
        if (role.tenantId != null && role.tenantId != user.tenantId) {
            throw IllegalArgumentException("Die Rolle ${role.name} ist nicht für den Tenant ${user.tenantId} verfügbar")
        }
        if (role.tenantId == null && tenantId != null && user.tenantId != tenantId) {
            // Global role assignment attempt in a specific tenant context not matching user's tenant
            throw IllegalArgumentException(
                "Globale Rolle ${role.name} kann nicht dem Benutzer in Tenant ${user.tenantId} " +
                    "zugewiesen werden, wenn der Kontext Tenant $tenantId ist."
            )
        }

        user.addRole(role)
        userRepository.save(user)

        // Audit-Log-Eintrag erstellen
        auditService.logRoleAssignedToUser(user, role)
    }

    override fun removeRoleFromUser(
        userId: String,
        roleId: UUID,
        tenantId: UUID?,
    ) {
        val user = userRepository.findById(UUID.fromString(userId))
            .orElseThrow { UserNotFoundException("User not found: $userId", userId) }

        val role = roleRepository.findById(roleId)
            .orElseThrow { RoleNotFoundException("Role not found: $roleId", roleId) }

        // Optional: Add validation based on tenantId similar to assignRoleToUser if needed

        user.removeRole(role)
        userRepository.save(user)

        // Audit-Log-Eintrag erstellen
        auditService.logRoleRemovedFromUser(user, role)
    }

    /**
     * Konvertiert eine Role-Entity in ein RoleDto.
     */
    private fun toDto(role: Role): RoleDto =
        RoleDto(
            roleId = role.roleId,
            name = role.name,
            description = role.description,
            tenantId = role.tenantId,
            permissions = role.permissions.map { permission ->
                PermissionDto(
                    permissionId = permission.permissionId,
                    name = permission.name,
                    description = permission.description
                )
            }
        )
}
