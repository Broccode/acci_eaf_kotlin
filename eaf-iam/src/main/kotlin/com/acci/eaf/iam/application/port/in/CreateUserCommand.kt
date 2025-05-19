package com.acci.eaf.iam.application.port.input

import com.acci.eaf.iam.domain.model.UserStatus
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.util.UUID

/**
 * Command für die Erstellung eines neuen Benutzers.
 *
 * Dieses Command enthält alle erforderlichen Attribute, um einen neuen Benutzer
 * im System zu erstellen, einschließlich des Passworts im Klartext.
 */
data class CreateUserCommand(
    /**
     * Die ID des Tenants, zu dem der Benutzer gehören soll.
     */
    @field:NotNull(message = "Tenant ID ist erforderlich")
    val tenantId: UUID,

    /**
     * Der Benutzername für den neuen Benutzer.
     * Muss für den Tenant einzigartig sein.
     */
    @field:NotBlank(message = "Benutzername ist erforderlich")
    @field:Size(min = 3, max = 255, message = "Benutzername muss zwischen 3 und 255 Zeichen lang sein")
    val username: String,

    /**
     * Das Passwort im Klartext für den neuen Benutzer.
     * Wird später gehasht und niemals gespeichert.
     */
    @field:NotBlank(message = "Passwort ist erforderlich")
    val password: String,

    /**
     * Die E-Mail-Adresse des Benutzers.
     * Optional, aber wenn angegeben, muss sie ein gültiges Format haben.
     */
    @field:Email(message = "Ungültiges E-Mail-Format")
    val email: String? = null,

    /**
     * Der anfängliche Status des Benutzers.
     * Standardmäßig auf ACTIVE gesetzt.
     */
    val status: UserStatus = UserStatus.ACTIVE,
)
