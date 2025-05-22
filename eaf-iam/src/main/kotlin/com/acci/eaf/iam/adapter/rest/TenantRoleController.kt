package com.acci.eaf.iam.adapter.rest

import com.acci.eaf.iam.adapter.rest.dto.RoleRequest
import com.acci.eaf.iam.application.port.api.CreateRoleCommand
import com.acci.eaf.iam.application.port.api.PermissionDto
import com.acci.eaf.iam.application.port.api.PermissionService
import com.acci.eaf.iam.application.port.api.RoleDto
import com.acci.eaf.iam.application.port.api.RoleService
import com.acci.eaf.iam.application.port.api.UpdateRoleCommand
import jakarta.validation.Valid
import java.util.UUID
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Controller für die API-Endpunkte zur Verwaltung von tenant-spezifischen Rollen.
 * Diese Endpunkte sind für Tenant-Administratoren zugänglich.
 */
@RestController
@RequestMapping("/api/controlplane/tenants/{tenantId}/roles")
class TenantRoleController(private val roleService: RoleService, private val permissionService: PermissionService) {

    /**
     * Listet alle Rollen für einen bestimmten Tenant auf.
     *
     * @param tenantId Die ID des Tenants
     * @param pageable Paginierungsinformationen
     * @return Eine Page mit Rollen-DTOs
     */
    @GetMapping
    @PreAuthorize("hasAuthority('role:read') and @tenantSecurity.isUserInTenant(#tenantId)")
    fun getRolesByTenant(@PathVariable tenantId: UUID, pageable: Pageable): ResponseEntity<Page<RoleDto>> =
        ResponseEntity.ok(roleService.getRolesByTenant(tenantId, pageable))

    /**
     * Listet alle für einen Tenant verfügbaren Rollen auf (systemweite + tenant-spezifische).
     *
     * @param tenantId Die ID des Tenants
     * @param pageable Paginierungsinformationen
     * @return Eine Page mit Rollen-DTOs
     */
    @GetMapping("/available")
    @PreAuthorize("hasAuthority('role:read') and @tenantSecurity.isUserInTenant(#tenantId)")
    fun getAvailableRolesForTenant(@PathVariable tenantId: UUID, pageable: Pageable): ResponseEntity<Page<RoleDto>> =
        ResponseEntity.ok(roleService.getAvailableRolesForTenant(tenantId, pageable))

    /**
     * Holt eine tenant-spezifische Rolle anhand ihrer ID.
     *
     * @param tenantId Die ID des Tenants
     * @param roleId Die ID der Rolle
     * @return Die Rolle als DTO
     */
    @GetMapping("/{roleId}")
    @PreAuthorize("hasAuthority('role:read') and @tenantSecurity.isUserInTenant(#tenantId)")
    fun getRoleById(@PathVariable tenantId: UUID, @PathVariable roleId: UUID): ResponseEntity<RoleDto> {
        val role = roleService.getRoleById(roleId)
            ?: return ResponseEntity.notFound().build()

        // Prüfen, ob die Rolle zu diesem Tenant gehört
        if (role.tenantId != tenantId) {
            return ResponseEntity.notFound().build()
        }

        return ResponseEntity.ok(role)
    }

    /**
     * Erstellt eine neue tenant-spezifische Rolle.
     *
     * @param tenantId Die ID des Tenants
     * @param request Die Anfrage mit den Rollendaten
     * @return Die erstellte Rolle als DTO
     */
    @PostMapping
    @PreAuthorize("hasAuthority('role:create') and @tenantSecurity.isUserInTenant(#tenantId)")
    fun createTenantRole(@PathVariable tenantId: UUID, @Valid @RequestBody request: RoleRequest): ResponseEntity<RoleDto> {
        val command = CreateRoleCommand(
            name = request.name,
            description = request.description,
            tenantId = tenantId
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(roleService.createRole(command))
    }

    /**
     * Aktualisiert eine bestehende tenant-spezifische Rolle.
     *
     * @param tenantId Die ID des Tenants
     * @param roleId Die ID der Rolle
     * @param request Die Anfrage mit den aktualisierten Rollendaten
     * @return Die aktualisierte Rolle als DTO
     */
    @PutMapping("/{roleId}")
    @PreAuthorize("hasAuthority('role:update') and @tenantSecurity.isUserInTenant(#tenantId)")
    fun updateTenantRole(
        @PathVariable tenantId: UUID,
        @PathVariable roleId: UUID,
        @Valid @RequestBody request: RoleRequest,
    ): ResponseEntity<RoleDto> {
        // Prüfen, ob die Rolle zu diesem Tenant gehört
        val role = roleService.getRoleById(roleId)
            ?: return ResponseEntity.notFound().build()
        if (role!!.tenantId != tenantId) {
            return ResponseEntity.notFound().build()
        }

        val command = UpdateRoleCommand(
            roleId = roleId,
            name = request.name,
            description = request.description
        )
        return ResponseEntity.ok(roleService.updateRole(command))
    }

    /**
     * Löscht eine tenant-spezifische Rolle.
     *
     * @param tenantId Die ID des Tenants
     * @param roleId Die ID der Rolle
     * @return HTTP 204 No Content
     */
    @DeleteMapping("/{roleId}")
    @PreAuthorize("hasAuthority('role:delete') and @tenantSecurity.isUserInTenant(#tenantId)")
    fun deleteTenantRole(@PathVariable tenantId: UUID, @PathVariable roleId: UUID): ResponseEntity<Void> {
        // Prüfen, ob die Rolle zu diesem Tenant gehört
        val role = roleService.getRoleById(roleId)
            ?: return ResponseEntity.notFound().build()
        if (role!!.tenantId != tenantId) {
            return ResponseEntity.notFound().build()
        }

        roleService.deleteRole(roleId)
        return ResponseEntity.noContent().build()
    }

    /**
     * Listet alle Berechtigungen für eine bestimmte Rolle auf.
     *
     * @param tenantId Die ID des Tenants
     * @param roleId Die ID der Rolle
     * @return Eine Liste mit Berechtigungs-DTOs
     */
    @GetMapping("/{roleId}/permissions")
    @PreAuthorize("hasAuthority('role:read') and @tenantSecurity.isUserInTenant(#tenantId)")
    fun getPermissionsForRole(@PathVariable tenantId: UUID, @PathVariable roleId: UUID): ResponseEntity<List<PermissionDto>> {
        // Prüfen, ob die Rolle zu diesem Tenant gehört
        val role = roleService.getRoleById(roleId)
            ?: return ResponseEntity.notFound().build()
        if (role!!.tenantId != tenantId) {
            return ResponseEntity.notFound().build()
        }

        return ResponseEntity.ok(permissionService.getPermissionsByRole(roleId))
    }

    /**
     * Fügt eine Berechtigung zu einer tenant-spezifischen Rolle hinzu.
     *
     * @param tenantId Die ID des Tenants
     * @param roleId Die ID der Rolle
     * @param permissionId Die ID der Berechtigung
     * @return Die aktualisierte Rolle als DTO
     */
    @PostMapping("/{roleId}/permissions/{permissionId}")
    @PreAuthorize("hasAuthority('role:update') and @tenantSecurity.isUserInTenant(#tenantId)")
    fun addPermissionToRole(
        @PathVariable tenantId: UUID,
        @PathVariable roleId: UUID,
        @PathVariable permissionId: UUID,
    ): ResponseEntity<RoleDto> {
        // Prüfen, ob die Rolle zu diesem Tenant gehört
        val role = roleService.getRoleById(roleId)
            ?: return ResponseEntity.notFound().build()
        if (role!!.tenantId != tenantId) {
            return ResponseEntity.notFound().build()
        }

        return ResponseEntity.ok(roleService.addPermissionToRole(roleId, permissionId))
    }

    /**
     * Entfernt eine Berechtigung von einer tenant-spezifischen Rolle.
     *
     * @param tenantId Die ID des Tenants
     * @param roleId Die ID der Rolle
     * @param permissionId Die ID der Berechtigung
     * @return Die aktualisierte Rolle als DTO
     */
    @DeleteMapping("/{roleId}/permissions/{permissionId}")
    @PreAuthorize("hasAuthority('role:update') and @tenantSecurity.isUserInTenant(#tenantId)")
    fun removePermissionFromRole(
        @PathVariable tenantId: UUID,
        @PathVariable roleId: UUID,
        @PathVariable permissionId: UUID,
    ): ResponseEntity<RoleDto> {
        // Prüfen, ob die Rolle zu diesem Tenant gehört
        val role = roleService.getRoleById(roleId)
            ?: return ResponseEntity.notFound().build()
        if (role!!.tenantId != tenantId) {
            return ResponseEntity.notFound().build()
        }
        return ResponseEntity.ok(roleService.removePermissionFromRole(roleId, permissionId))
    }
}
