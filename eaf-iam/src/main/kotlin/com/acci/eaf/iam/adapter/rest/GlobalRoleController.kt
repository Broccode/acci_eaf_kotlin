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
 * Controller für die API-Endpunkte zur Verwaltung von systemweiten Rollen.
 * Diese Endpunkte sind nur für EAF Super-Admins zugänglich.
 */
@RestController
@RequestMapping("/api/controlplane/roles")
class GlobalRoleController(private val roleService: RoleService, private val permissionService: PermissionService) {

    /**
     * Listet alle systemweiten Rollen auf.
     *
     * @param pageable Paginierungsinformationen
     * @return Eine Page mit Rollen-DTOs
     */
    @GetMapping
    @PreAuthorize("hasAuthority('role:read')")
    fun getGlobalRoles(pageable: Pageable): ResponseEntity<Page<RoleDto>> = ResponseEntity.ok(roleService.getGlobalRoles(pageable))

    /**
     * Holt eine systemweite Rolle anhand ihrer ID.
     *
     * @param roleId Die ID der Rolle
     * @return Die Rolle als DTO
     */
    @GetMapping("/{roleId}")
    @PreAuthorize("hasAuthority('role:read')")
    fun getRoleById(@PathVariable roleId: UUID): ResponseEntity<RoleDto> = ResponseEntity.ok(roleService.getRoleById(roleId))

    /**
     * Erstellt eine neue systemweite Rolle.
     *
     * @param request Die Anfrage mit den Rollendaten
     * @return Die erstellte Rolle als DTO
     */
    @PostMapping
    @PreAuthorize("hasAuthority('role:create')")
    fun createGlobalRole(@Valid @RequestBody request: RoleRequest): ResponseEntity<RoleDto> {
        val command = CreateRoleCommand(
            name = request.name,
            description = request.description,
            tenantId = null // null für systemweite Rollen
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(roleService.createRole(command))
    }

    /**
     * Aktualisiert eine bestehende systemweite Rolle.
     *
     * @param roleId Die ID der Rolle
     * @param request Die Anfrage mit den aktualisierten Rollendaten
     * @return Die aktualisierte Rolle als DTO
     */
    @PutMapping("/{roleId}")
    @PreAuthorize("hasAuthority('role:update')")
    fun updateGlobalRole(@PathVariable roleId: UUID, @Valid @RequestBody request: RoleRequest): ResponseEntity<RoleDto> {
        val command = UpdateRoleCommand(
            roleId = roleId,
            name = request.name,
            description = request.description
        )
        return ResponseEntity.ok(roleService.updateRole(command))
    }

    /**
     * Löscht eine systemweite Rolle.
     *
     * @param roleId Die ID der Rolle
     * @return HTTP 204 No Content
     */
    @DeleteMapping("/{roleId}")
    @PreAuthorize("hasAuthority('role:delete')")
    fun deleteGlobalRole(@PathVariable roleId: UUID): ResponseEntity<Void> {
        roleService.deleteRole(roleId)
        return ResponseEntity.noContent().build()
    }

    /**
     * Listet alle Berechtigungen für eine bestimmte Rolle auf.
     *
     * @param roleId Die ID der Rolle
     * @return Eine Liste mit Berechtigungs-DTOs
     */
    @GetMapping("/{roleId}/permissions")
    @PreAuthorize("hasAuthority('role:read')")
    fun getPermissionsForRole(@PathVariable roleId: UUID): ResponseEntity<List<PermissionDto>> =
        ResponseEntity.ok(permissionService.getPermissionsByRole(roleId))

    /**
     * Fügt eine Berechtigung zu einer Rolle hinzu.
     *
     * @param roleId Die ID der Rolle
     * @param permissionId Die ID der Berechtigung
     * @return Die aktualisierte Rolle als DTO
     */
    @PostMapping("/{roleId}/permissions/{permissionId}")
    @PreAuthorize("hasAuthority('role:update')")
    fun addPermissionToRole(@PathVariable roleId: UUID, @PathVariable permissionId: UUID): ResponseEntity<RoleDto> =
        ResponseEntity.ok(roleService.addPermissionToRole(roleId, permissionId))

    /**
     * Entfernt eine Berechtigung von einer Rolle.
     *
     * @param roleId Die ID der Rolle
     * @param permissionId Die ID der Berechtigung
     * @return Die aktualisierte Rolle als DTO
     */
    @DeleteMapping("/{roleId}/permissions/{permissionId}")
    @PreAuthorize("hasAuthority('role:update')")
    fun removePermissionFromRole(@PathVariable roleId: UUID, @PathVariable permissionId: UUID): ResponseEntity<RoleDto> =
        ResponseEntity.ok(roleService.removePermissionFromRole(roleId, permissionId))
}
