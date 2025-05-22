package com.acci.eaf.iam.application.port.api

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.util.UUID

/**
 * Kommandoobjekt für die Aktualisierung einer bestehenden Rolle.
 */
data class UpdateRoleCommand(
    /**
     * ID der zu aktualisierenden Rolle.
     */
    val roleId: UUID,

    /**
     * Neuer Name der Rolle, muss einzigartig innerhalb eines Tenants sein.
     */
    @field:NotBlank(message = "Der Rollenname darf nicht leer sein")
    @field:Size(min = 3, max = 50, message = "Der Rollenname muss zwischen 3 und 50 Zeichen lang sein")
    val name: String,

    /**
     * Neue Beschreibung der Rolle.
     */
    @field:Size(max = 255, message = "Die Beschreibung darf maximal 255 Zeichen lang sein")
    val description: String?,
)
