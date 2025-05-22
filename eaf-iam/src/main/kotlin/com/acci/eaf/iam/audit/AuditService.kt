package com.acci.eaf.iam.audit

import com.acci.eaf.iam.domain.model.Permission
import com.acci.eaf.iam.domain.model.Role
import com.acci.eaf.iam.domain.model.User
import org.springframework.stereotype.Service

/**
 * Service zur Erstellung von Audit-Log-Einträgen für RBAC-bezogene Aktionen.
 */
@Service
class AuditService {

    /**
     * Protokolliert die Erstellung einer neuen Rolle.
     */
    fun logRoleCreated(role: Role) {
        // TODO: Implementierung der tatsächlichen Audit-Logik (z.B. Speichern in Datenbank, Senden an Log-Service)
        println("AUDIT: Rolle '${role.name}' wurde erstellt (Tenant: ${role.tenantId ?: "global"})")
    }

    /**
     * Protokolliert die Aktualisierung einer Rolle.
     */
    fun logRoleUpdated(role: Role, previousName: String) {
        println("AUDIT: Rolle '$previousName' wurde zu '${role.name}' aktualisiert (Tenant: ${role.tenantId ?: "global"})")
    }

    /**
     * Protokolliert die Löschung einer Rolle.
     */
    fun logRoleDeleted(role: Role) {
        println("AUDIT: Rolle '${role.name}' wurde gelöscht (Tenant: ${role.tenantId ?: "global"})")
    }

    /**
     * Protokolliert das Hinzufügen einer Berechtigung zu einer Rolle.
     */
    fun logPermissionAddedToRole(role: Role, permission: Permission) {
        println("AUDIT: Berechtigung '${permission.name}' wurde zur Rolle '${role.name}' hinzugefügt (Tenant: ${role.tenantId ?: "global"})")
    }

    /**
     * Protokolliert das Entfernen einer Berechtigung von einer Rolle.
     */
    fun logPermissionRemovedFromRole(role: Role, permission: Permission) {
        println("AUDIT: Berechtigung '${permission.name}' wurde von der Rolle '${role.name}' entfernt (Tenant: ${role.tenantId ?: "global"})")
    }

    /**
     * Protokolliert die Zuweisung einer Rolle zu einem Benutzer.
     */
    fun logRoleAssignedToUser(user: User, role: Role) {
        println("AUDIT: Rolle '${role.name}' wurde dem Benutzer '${user.username}' (${user.id}) zugewiesen (Tenant: ${user.tenantId})")
    }

    /**
     * Protokolliert das Entfernen einer Rolle von einem Benutzer.
     */
    fun logRoleRemovedFromUser(user: User, role: Role) {
        println("AUDIT: Rolle '${role.name}' wurde vom Benutzer '${user.username}' (${user.id}) entfernt (Tenant: ${user.tenantId})")
    }
}
