package com.acci.eaf.iam.application.port.input

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.util.UUID

/**
 * Command zum Setzen eines neuen Passworts für einen Benutzer.
 *
 * Dieses Command enthält die ID des Benutzers und das neue Passwort im Klartext.
 */
data class SetPasswordCommand(
    /**
     * Die ID des Benutzers, dessen Passwort geändert werden soll.
     */
    @field:NotNull(message = "Benutzer-ID ist erforderlich")
    val userId: UUID,

    /**
     * Das neue Passwort im Klartext.
     * Wird später gehasht und niemals gespeichert.
     */
    @field:NotBlank(message = "Passwort ist erforderlich")
    val newPassword: String,
)
