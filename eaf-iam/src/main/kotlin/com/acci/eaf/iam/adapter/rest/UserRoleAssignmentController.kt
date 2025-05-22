package com.acci.eaf.iam.adapter.rest

import com.acci.eaf.iam.application.port.api.RoleDto
import com.acci.eaf.iam.application.port.api.RoleService
import java.util.UUID
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Controller für die API-Endpunkte zur Zuweisung von Rollen an Benutzer innerhalb eines Tenants.
 */
@RestController
@RequestMapping("/api/controlplane/tenants/{tenantId}/users/{userId}/roles")
class UserRoleAssignmentController(private val roleService: RoleService) {

    /**
     * Listet alle Rollen auf, die einem Benutzer zugewiesen sind.
     *
     * @param tenantId Die ID des Tenants
     * @param userId Die ID des Benutzers
     * @return Eine Liste mit Rollen-DTOs
     */
    @GetMapping
    @PreAuthorize("hasAuthority('role:read') and @tenantSecurity.isUserInTenant(#tenantId)")
    fun getRolesByUser(@PathVariable tenantId: UUID, @PathVariable userId: UUID): ResponseEntity<List<RoleDto>> {
        // Wir gehen hier davon aus, dass der UserService sicherstellt, dass der Benutzer zum Tenant gehört
        return ResponseEntity.ok(roleService.getRolesByUser(userId = userId.toString(), tenantId = tenantId))
    }

    /**
     * Weist einem Benutzer eine Rolle zu.
     *
     * @param tenantId Die ID des Tenants
     * @param userId Die ID des Benutzers
     * @param roleId Die ID der Rolle
     * @return HTTP 204 No Content
     */
    @PostMapping("/{roleId}")
    @PreAuthorize("hasAuthority('role:assign') and @tenantSecurity.isUserInTenant(#tenantId)")
    fun assignRoleToUser(
        @PathVariable tenantId: UUID,
        @PathVariable userId: UUID,
        @PathVariable roleId: UUID,
    ): ResponseEntity<Void> {
        // Wir gehen hier davon aus, dass der RoleService sicherstellt, dass die Rolle für den Tenant verfügbar ist
        roleService.assignRoleToUser(userId = userId.toString(), roleId = roleId, tenantId = tenantId)
        return ResponseEntity.noContent().build()
    }

    /**
     * Entfernt eine Rolle von einem Benutzer.
     *
     * @param tenantId Die ID des Tenants
     * @param userId Die ID des Benutzers
     * @param roleId Die ID der Rolle
     * @return HTTP 204 No Content
     */
    @DeleteMapping("/{roleId}")
    @PreAuthorize("hasAuthority('role:assign') and @tenantSecurity.isUserInTenant(#tenantId)")
    fun removeRoleFromUser(
        @PathVariable tenantId: UUID,
        @PathVariable userId: UUID,
        @PathVariable roleId: UUID,
    ): ResponseEntity<Void> {
        roleService.removeRoleFromUser(userId = userId.toString(), roleId = roleId, tenantId = tenantId)
        return ResponseEntity.noContent().build()
    }
}
