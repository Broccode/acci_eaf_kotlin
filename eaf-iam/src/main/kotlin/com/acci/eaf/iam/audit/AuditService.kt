package com.acci.eaf.iam.audit

import com.acci.eaf.iam.domain.model.Permission
import com.acci.eaf.iam.domain.model.Role
import com.acci.eaf.iam.domain.model.User
import java.time.OffsetDateTime
import java.util.*
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

    // ========================================
    // Service Account Audit Logging Methods
    // ========================================

    /**
     * Protokolliert die Erstellung eines Service Accounts.
     */
    fun logServiceAccountCreated(
        serviceAccountId: UUID,
        clientId: String,
        tenantId: UUID,
        description: String?,
        expiresAt: OffsetDateTime?,
        initiatedBy: String,
    ) {
        println(
            "AUDIT: SERVICE_ACCOUNT_CREATED - " +
                "Actor: $initiatedBy, " +
                "ServiceAccountId: $serviceAccountId, " +
                "ClientId: $clientId, " +
                "TenantId: $tenantId, " +
                "Description: ${description ?: "null"}, " +
                "ExpiresAt: ${expiresAt ?: "never"}"
        )
        // TODO: Integrate with central audit log
    }

    /**
     * Protokolliert die Aktualisierung von Service Account Details.
     */
    fun logServiceAccountDetailsUpdated(
        serviceAccountId: UUID,
        clientId: String,
        tenantId: UUID,
        updatedFields: Map<String, Any?>,
        initiatedBy: String,
    ) {
        val fieldsInfo = updatedFields.map { "${it.key}: ${it.value}" }.joinToString(", ")
        println(
            "AUDIT: SERVICE_ACCOUNT_DETAILS_UPDATED - " +
                "Actor: $initiatedBy, " +
                "ServiceAccountId: $serviceAccountId, " +
                "ClientId: $clientId, " +
                "TenantId: $tenantId, " +
                "UpdatedFields: [$fieldsInfo]"
        )
        // TODO: Integrate with central audit log
    }

    /**
     * Protokolliert die Rotation des Service Account Secrets.
     */
    fun logServiceAccountSecretRotated(
        serviceAccountId: UUID,
        clientId: String,
        tenantId: UUID,
        initiatedBy: String,
    ) {
        println(
            "AUDIT: SERVICE_ACCOUNT_SECRET_ROTATED - " +
                "Actor: $initiatedBy, " +
                "ServiceAccountId: $serviceAccountId, " +
                "ClientId: $clientId, " +
                "TenantId: $tenantId"
        )
        // TODO: Integrate with central audit log
    }

    /**
     * Protokolliert die Deaktivierung eines Service Accounts.
     */
    fun logServiceAccountDeactivated(
        serviceAccountId: UUID,
        clientId: String,
        tenantId: UUID,
        initiatedBy: String,
    ) {
        println(
            "AUDIT: SERVICE_ACCOUNT_DEACTIVATED - " +
                "Actor: $initiatedBy, " +
                "ServiceAccountId: $serviceAccountId, " +
                "ClientId: $clientId, " +
                "TenantId: $tenantId"
        )
        // TODO: Integrate with central audit log
    }

    /**
     * Protokolliert die Aktivierung eines Service Accounts.
     */
    fun logServiceAccountActivated(
        serviceAccountId: UUID,
        clientId: String,
        tenantId: UUID,
        initiatedBy: String,
    ) {
        println(
            "AUDIT: SERVICE_ACCOUNT_ACTIVATED - " +
                "Actor: $initiatedBy, " +
                "ServiceAccountId: $serviceAccountId, " +
                "ClientId: $clientId, " +
                "TenantId: $tenantId"
        )
        // TODO: Integrate with central audit log
    }

    /**
     * Protokolliert die Zuweisung von Rollen zu einem Service Account.
     */
    fun logServiceAccountRolesAssigned(
        serviceAccountId: UUID,
        clientId: String,
        tenantId: UUID,
        roleIds: Set<UUID>,
        initiatedBy: String,
    ) {
        println(
            "AUDIT: SERVICE_ACCOUNT_ROLES_ASSIGNED - " +
                "Actor: $initiatedBy, " +
                "ServiceAccountId: $serviceAccountId, " +
                "ClientId: $clientId, " +
                "TenantId: $tenantId, " +
                "RoleIds: [${roleIds.joinToString(", ")}]"
        )
        // TODO: Integrate with central audit log
    }

    /**
     * Protokolliert das Entfernen von Rollen von einem Service Account.
     */
    fun logServiceAccountRolesRemoved(
        serviceAccountId: UUID,
        clientId: String,
        tenantId: UUID,
        roleIds: Set<UUID>,
        initiatedBy: String,
    ) {
        println(
            "AUDIT: SERVICE_ACCOUNT_ROLES_REMOVED - " +
                "Actor: $initiatedBy, " +
                "ServiceAccountId: $serviceAccountId, " +
                "ClientId: $clientId, " +
                "TenantId: $tenantId, " +
                "RoleIds: [${roleIds.joinToString(", ")}]"
        )
        // TODO: Integrate with central audit log
    }

    /**
     * Protokolliert eine erfolgreiche Service Account Authentifizierung.
     */
    fun logServiceAccountAuthenticationSuccess(
        clientId: String,
        tenantId: UUID,
        sourceIp: String? = null,
    ) {
        val ipInfo = sourceIp?.let { " from IP: $it" } ?: ""
        println("AUDIT: SERVICE_ACCOUNT_AUTH_SUCCESS - ClientId: $clientId, TenantId: $tenantId$ipInfo")
        // TODO: Integrate with central audit log
    }

    /**
     * Protokolliert eine fehlgeschlagene Service Account Authentifizierung.
     */
    fun logServiceAccountAuthenticationFailure(
        clientId: String?,
        tenantId: UUID?,
        reason: String,
        sourceIp: String? = null,
    ) {
        val ipInfo = sourceIp?.let { " from IP: $it" } ?: ""
        println(
            "AUDIT: SERVICE_ACCOUNT_AUTH_FAILURE - " +
                "ClientId: ${clientId ?: "unknown"}, " +
                "TenantId: ${tenantId ?: "unknown"}, " +
                "Reason: $reason$ipInfo"
        )
        // TODO: Integrate with central audit log
    }
}
