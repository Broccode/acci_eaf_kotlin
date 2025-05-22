package com.acci.eaf.iam.application.port.api

import java.util.UUID
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

/**
 * Service-Interface für die Verwaltung von Rollen.
 */
interface RoleService {

    /**
     * Erstellt eine neue Rolle.
     *
     * @param command Das Kommandoobjekt mit den Rollen-Daten
     * @return Die erstellte Rolle als DTO
     * @throws RoleAlreadyExistsException wenn eine Rolle mit diesem Namen bereits existiert
     */
    fun createRole(command: CreateRoleCommand): RoleDto

    /**
     * Aktualisiert eine bestehende Rolle.
     *
     * @param command Das Kommandoobjekt mit den aktualisierten Rollen-Daten
     * @return Die aktualisierte Rolle als DTO
     * @throws RoleNotFoundException wenn die Rolle nicht gefunden wird
     * @throws RoleAlreadyExistsException wenn eine andere Rolle mit diesem Namen bereits existiert
     */
    fun updateRole(command: UpdateRoleCommand): RoleDto

    /**
     * Löscht eine Rolle anhand ihrer ID.
     *
     * @param roleId Die ID der zu löschenden Rolle
     * @throws RoleNotFoundException wenn die Rolle nicht gefunden wird
     */
    fun deleteRole(roleId: UUID)

    /**
     * Fügt einer Rolle eine Berechtigung hinzu.
     *
     * @param roleId Die ID der Rolle
     * @param permissionId Die ID der Berechtigung
     * @return Die aktualisierte Rolle als DTO
     * @throws RoleNotFoundException wenn die Rolle nicht gefunden wird
     * @throws PermissionNotFoundException wenn die Berechtigung nicht gefunden wird
     */
    fun addPermissionToRole(roleId: UUID, permissionId: UUID): RoleDto

    /**
     * Entfernt eine Berechtigung von einer Rolle.
     *
     * @param roleId Die ID der Rolle
     * @param permissionId Die ID der Berechtigung
     * @return Die aktualisierte Rolle als DTO
     * @throws RoleNotFoundException wenn die Rolle nicht gefunden wird
     * @throws PermissionNotFoundException wenn die Berechtigung nicht gefunden wird
     */
    fun removePermissionFromRole(roleId: UUID, permissionId: UUID): RoleDto

    /**
     * Holt eine Rolle anhand ihrer ID.
     *
     * @param roleId Die ID der Rolle
     * @return Die Rolle als DTO
     * @throws RoleNotFoundException wenn die Rolle nicht gefunden wird
     */
    fun getRoleById(roleId: UUID): RoleDto?

    /**
     * Listet alle systemweiten Rollen auf (tenantId = null).
     *
     * @param pageable Das Pageable für die Paginierung
     * @return Eine Page mit Rollen-DTOs
     */
    fun getGlobalRoles(pageable: Pageable): Page<RoleDto>

    /**
     * Listet alle Rollen für einen bestimmten Tenant auf.
     *
     * @param tenantId Die ID des Tenants
     * @param pageable Das Pageable für die Paginierung
     * @return Eine Page mit Rollen-DTOs
     */
    fun getRolesByTenant(tenantId: UUID, pageable: Pageable): Page<RoleDto>

    /**
     * Listet alle für einen Tenant verfügbaren Rollen auf
     * (systemweite Rollen + tenant-spezifische Rollen).
     *
     * @param tenantId Die ID des Tenants
     * @param pageable Das Pageable für die Paginierung
     * @return Eine Page mit Rollen-DTOs
     */
    fun getAvailableRolesForTenant(tenantId: UUID, pageable: Pageable): Page<RoleDto>

    /**
     * Listet alle Rollen für einen bestimmten Benutzer auf.
     *
     * @param userId Die ID des Benutzers
     * @return Eine Liste mit Rollen-DTOs
     */
    fun getRolesByUser(userId: String, tenantId: UUID?): List<RoleDto>

    /**
     * Weist einem Benutzer eine Rolle zu.
     *
     * @param userId Die ID des Benutzers
     * @param roleId Die ID der Rolle
     * @throws UserNotFoundException wenn der Benutzer nicht gefunden wird
     * @throws RoleNotFoundException wenn die Rolle nicht gefunden wird
     */
    fun assignRoleToUser(
        userId: String,
        roleId: UUID,
        tenantId: UUID?,
    )

    /**
     * Entfernt eine Rolle von einem Benutzer.
     *
     * @param userId Die ID des Benutzers
     * @param roleId Die ID der Rolle
     * @throws UserNotFoundException wenn der Benutzer nicht gefunden wird
     * @throws RoleNotFoundException wenn die Rolle nicht gefunden wird
     */
    fun removeRoleFromUser(
        userId: String,
        roleId: UUID,
        tenantId: UUID?,
    )
}
