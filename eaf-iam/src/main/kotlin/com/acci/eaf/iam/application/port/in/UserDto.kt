package com.acci.eaf.iam.application.port.input

import com.acci.eaf.iam.domain.model.User
import com.acci.eaf.iam.domain.model.UserStatus
import java.time.Instant
import java.util.UUID

/**
 * Data Transfer Object (DTO) für Benutzer.
 *
 * Diese Klasse repräsentiert einen Benutzer in der Anwendungsschicht und wird
 * für die Kommunikation zwischen den Ports verwendet. Das DTO enthält keine
 * sicherheitsrelevanten Informationen wie das Passwort-Hash.
 */
data class UserDto(
    /**
     * Die eindeutige ID des Benutzers.
     */
    val id: UUID,

    /**
     * Die ID des Tenants, zu dem der Benutzer gehört.
     */
    val tenantId: UUID,

    /**
     * Der Benutzername, der zur Anmeldung verwendet wird.
     */
    val username: String,

    /**
     * Die E-Mail-Adresse des Benutzers. Kann null sein.
     */
    val email: String?,

    /**
     * Der aktuelle Status des Benutzers.
     */
    val status: UserStatus,

    /**
     * Der Zeitpunkt, zu dem der Benutzer erstellt wurde.
     */
    val createdAt: Instant,

    /**
     * Der Zeitpunkt, zu dem der Benutzer zuletzt aktualisiert wurde.
     */
    val updatedAt: Instant,
) {
    companion object {
        /**
         * Konvertiert eine [User] Entity in ein [UserDto].
         *
         * @param user die zu konvertierende Entity
         * @return das erzeugte DTO
         */
        fun fromEntity(user: User): UserDto =
            UserDto(
                id = user.id,
                tenantId = user.tenantId,
                username = user.username,
                email = user.email,
                status = user.status,
                createdAt = user.createdAt,
                updatedAt = user.updatedAt
            )
    }
}
