package com.acci.eaf.iam.domain.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.Instant
import java.util.UUID

/**
 * Repräsentiert einen Benutzer im IAM-System.
 *
 * Diese Entity bildet die grundlegende Struktur für alle Benutzer ab, die sich lokal in der EAF-Plattform
 * authentifizieren können. Der Benutzer ist einem Tenant zugeordnet und hat einen eindeutigen Benutzernamen
 * innerhalb dieses Tenants.
 */
@Entity
@Table(
    name = "local_users",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_tenant_username", columnNames = ["tenant_id", "username"])
    ]
)
class User(
    /**
     * Die eindeutige ID des Benutzers. Wird automatisch vom System generiert.
     */
    @Id
    @Column(name = "user_id")
    val id: UUID = UUID.randomUUID(),

    /**
     * Die ID des Tenants, zu dem der Benutzer gehört.
     */
    @Column(name = "tenant_id", nullable = false)
    val tenantId: UUID,

    /**
     * Der Benutzername, der zur Anmeldung verwendet wird. Muss einzigartig innerhalb eines Tenants sein.
     */
    @Column(name = "username", nullable = false)
    var username: String,

    /**
     * Die E-Mail-Adresse des Benutzers. Optional, wird für Benachrichtigungen verwendet.
     */
    @Column(name = "email")
    var email: String? = null,

    /**
     * Der Vorname des Benutzers. Optional.
     */
    @Column(name = "first_name")
    var firstName: String? = null,

    /**
     * Der Nachname des Benutzers. Optional.
     */
    @Column(name = "last_name")
    var lastName: String? = null,

    /**
     * Der gehashte Passwort-Wert. Wird für die Authentifizierung verwendet.
     * Das Passwort wird niemals im Klartext gespeichert.
     */
    @Column(name = "password_hash", nullable = false, length = 512)
    var passwordHash: String,

    /**
     * Der aktuelle Status des Benutzers, der bestimmt, ob der Benutzer sich anmelden kann.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: UserStatus = UserStatus.ACTIVE,

    /**
     * Der Zeitpunkt, zu dem der Benutzer erstellt wurde.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    /**
     * Der Zeitpunkt, zu dem der Benutzer zuletzt aktualisiert wurde.
     */
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),

    /**
     * Der Zeitpunkt der letzten Anmeldung des Benutzers. Optional.
     */
    @Column(name = "last_login_at")
    var lastLoginAt: Instant? = null,

    /**
     * Die Rollen, die dem Benutzer zugewiesen sind.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_roles",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "role_id")]
    )
    val roles: MutableSet<Role> = mutableSetOf(),
) {
    /**
     * Prüft, ob der Benutzer aktiv ist und sich anmelden kann.
     *
     * @return true, wenn der Benutzer sich anmelden kann, sonst false
     */
    fun isActive(): Boolean = status == UserStatus.ACTIVE

    /**
     * Aktualisiert den 'updatedAt' Zeitstempel bei jeder Änderung des Benutzers.
     */
    @PreUpdate
    fun onUpdate() {
        updatedAt = Instant.now()
    }

    /**
     * Gibt den Anzeigenamen des Benutzers zurück, basierend auf Vor- und Nachname oder Benutzername.
     *
     * @return der Anzeigename des Benutzers
     */
    fun getDisplayName(): String {
        val names = listOfNotNull(firstName, lastName)
        return if (names.isNotEmpty()) names.joinToString(" ") else username
    }

    /**
     * Fügt eine Rolle dem Benutzer hinzu.
     *
     * @param role Die hinzuzufügende Rolle
     * @return Der Benutzer selbst für Method Chaining
     */
    fun addRole(role: Role): User {
        roles.add(role)
        return this
    }

    /**
     * Entfernt eine Rolle vom Benutzer.
     *
     * @param role Die zu entfernende Rolle
     * @return Der Benutzer selbst für Method Chaining
     */
    fun removeRole(role: Role): User {
        roles.remove(role)
        return this
    }

    /**
     * Prüft, ob der Benutzer eine bestimmte Rolle hat.
     *
     * @param roleName Der Name der zu prüfenden Rolle
     * @return true, wenn der Benutzer die Rolle hat, sonst false
     */
    fun hasRole(roleName: String): Boolean = roles.any { it.name == roleName }

    /**
     * Prüft, ob der Benutzer eine bestimmte Berechtigung hat.
     *
     * @param permissionName Der Name der zu prüfenden Berechtigung
     * @return true, wenn der Benutzer eine Rolle mit dieser Berechtigung hat, sonst false
     */
    fun hasPermission(permissionName: String): Boolean = roles.any { it.hasPermission(permissionName) }
}
