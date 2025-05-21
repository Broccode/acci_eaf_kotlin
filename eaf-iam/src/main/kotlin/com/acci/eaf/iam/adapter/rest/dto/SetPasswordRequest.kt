package com.acci.eaf.iam.adapter.rest.dto

import jakarta.validation.constraints.NotBlank

/**
 * Request-DTO für das Ändern des Passworts eines Benutzers.
 *
 * Diese Klasse repräsentiert die JSON-Anfrage zum Ändern eines Benutzerpassworts.
 */
data class SetPasswordRequest(
    /**
     * Das neue Passwort im Klartext.
     * Wird niemals persistiert, sondern nur für die Passwort-Änderung gehasht.
     */
    @field:NotBlank(message = "New password is required")
    val newPassword: String,
)
