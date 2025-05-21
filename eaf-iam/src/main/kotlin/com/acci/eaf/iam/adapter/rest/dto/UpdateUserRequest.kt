package com.acci.eaf.iam.adapter.rest.dto

import com.acci.eaf.iam.domain.model.UserStatus
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Size

/**
 * Request-DTO für die Aktualisierung eines vorhandenen Benutzers.
 *
 * Diese Klasse repräsentiert die JSON-Anfrage zum Aktualisieren eines Benutzers.
 * Alle Felder sind optional, da nur geänderte Felder aktualisiert werden sollen.
 */
data class UpdateUserRequest(
    /**
     * Die E-Mail-Adresse des Benutzers.
     */
    @field:Email(message = "Invalid email format")
    val email: String? = null,

    /**
     * Der Vorname des Benutzers.
     */
    @field:Size(max = 255, message = "First name must be at most 255 characters")
    val firstName: String? = null,

    /**
     * Der Nachname des Benutzers.
     */
    @field:Size(max = 255, message = "Last name must be at most 255 characters")
    val lastName: String? = null,

    /**
     * Der Status des Benutzers.
     */
    val status: UserStatus? = null,
)
