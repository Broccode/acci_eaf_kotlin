package com.acci.eaf.iam.application.port.input

import com.acci.eaf.iam.domain.model.UserStatus
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.util.UUID

/**
 * Command zum Aktualisieren eines vorhandenen Benutzers.
 *
 * Dieses Command enthält die ID des zu aktualisierenden Benutzers sowie
 * die Felder, die aktualisiert werden können (Email, Status, Vor- und Nachname).
 */
data class UpdateUserCommand(
    /**
     * Die ID des zu aktualisierenden Benutzers.
     */
    @field:NotNull(message = "Benutzer-ID ist erforderlich")
    val userId: UUID,

    /**
     * Die Tenant-ID des Benutzers.
     */
    @field:NotNull(message = "Tenant-ID ist erforderlich")
    val tenantId: UUID,

    /**
     * Die neue E-Mail-Adresse des Benutzers (optional).
     * Wenn null, wird die E-Mail-Adresse nicht geändert.
     */
    @field:Email(message = "Ungültiges E-Mail-Format")
    val email: String? = null,

    /**
     * Der neue Vorname des Benutzers (optional).
     * Wenn null, wird der Vorname nicht geändert.
     */
    @field:Size(max = 255, message = "Vorname darf maximal 255 Zeichen lang sein")
    val firstName: String? = null,

    /**
     * Der neue Nachname des Benutzers (optional).
     * Wenn null, wird der Nachname nicht geändert.
     */
    @field:Size(max = 255, message = "Nachname darf maximal 255 Zeichen lang sein")
    val lastName: String? = null,

    /**
     * Der neue Status des Benutzers (optional).
     * Wenn null, wird der Status nicht geändert.
     */
    val status: UserStatus? = null,
)
