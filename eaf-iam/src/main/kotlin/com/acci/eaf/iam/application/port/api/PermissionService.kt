package com.acci.eaf.iam.application.port.api

import java.util.UUID
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

/**
 * Service-Interface für die Verwaltung von Berechtigungen.
 * Berechtigungen werden initial vom System definiert und können nicht
 * von Tenant-Administratoren erstellt werden.
 */
interface PermissionService {

    /**
     * Holt eine Berechtigung anhand ihrer ID.
     *
     * @param permissionId Die ID der Berechtigung
     * @return Die Berechtigung als DTO
     * @throws PermissionNotFoundException wenn die Berechtigung nicht gefunden wird
     */
    fun getPermissionById(permissionId: UUID): PermissionDto?

    /**
     * Holt eine Berechtigung anhand ihres Namens.
     *
     * @param name Der Name der Berechtigung
     * @return Die Berechtigung als DTO
     * @throws PermissionNotFoundException wenn die Berechtigung nicht gefunden wird
     */
    fun getPermissionByName(name: String): PermissionDto?

    /**
     * Listet alle verfügbaren Berechtigungen auf.
     *
     * @param pageable Das Pageable für die Paginierung
     * @return Eine Page mit Berechtigungs-DTOs
     */
    fun getAllPermissions(pageable: Pageable): Page<PermissionDto>

    /**
     * Listet alle Berechtigungen auf, die einer Rolle zugewiesen sind.
     *
     * @param roleId Die ID der Rolle
     * @return Eine Liste mit Berechtigungs-DTOs
     */
    fun getPermissionsByRole(roleId: UUID): List<PermissionDto>

    /**
     * Listet alle effektiven Berechtigungen eines Benutzers auf, basierend auf seinen Rollen.
     *
     * @param userId Die ID des Benutzers
     * @return Eine Liste mit Berechtigungs-DTOs
     */
    fun getEffectivePermissionsByUser(userId: String): Set<String> // Returns set of permission names

    /**
     * Sucht nach Berechtigungen, deren Name einen bestimmten String enthält.
     *
     * @param nameFragment Teil des Berechtigungsnamens
     * @param pageable Das Pageable für die Paginierung
     * @return Eine Page mit Berechtigungs-DTOs
     */
    fun searchPermissionsByName(nameFragment: String, pageable: Pageable): Page<PermissionDto>
}
