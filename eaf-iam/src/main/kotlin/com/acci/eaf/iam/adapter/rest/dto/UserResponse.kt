package com.acci.eaf.iam.adapter.rest.dto

import com.acci.eaf.iam.application.port.input.UserDto
import com.acci.eaf.iam.domain.model.UserStatus
import java.time.Instant
import java.util.UUID

/**
 * Response-DTO für API-Antworten, die Benutzerinformationen enthalten.
 *
 * Diese Klasse repräsentiert die JSON-Antwort mit Benutzerdetails.
 * Sie enthält keine sicherheitskritischen Informationen wie Passworte.
 */
data class UserResponse(
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
     * Die E-Mail-Adresse des Benutzers.
     */
    val email: String?,

    /**
     * Der Vorname des Benutzers.
     */
    val firstName: String?,

    /**
     * Der Nachname des Benutzers.
     */
    val lastName: String?,

    /**
     * Der Anzeigename des Benutzers, basierend auf Vor- und Nachname oder Benutzername.
     */
    val displayName: String,

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

    /**
     * Der Zeitpunkt der letzten Anmeldung, falls vorhanden.
     */
    val lastLoginAt: Instant? = null,
) {
    companion object {
        /**
         * Konvertiert ein [UserDto] in eine [UserResponse].
         *
         * @param dto das zu konvertierende DTO
         * @return die erzeugte Response
         */
        fun fromDto(dto: UserDto): UserResponse =
            UserResponse(
                id = dto.id,
                tenantId = dto.tenantId,
                username = dto.username,
                email = dto.email,
                firstName = dto.firstName,
                lastName = dto.lastName,
                displayName = dto.displayName,
                status = dto.status,
                createdAt = dto.createdAt,
                updatedAt = dto.updatedAt,
                lastLoginAt = dto.lastLoginAt
            )
    }
}
