package com.acci.eaf.iam.adapter.rest.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * DTO für die Anfrage zum Erstellen oder Aktualisieren einer Rolle.
 */
data class RoleRequest(
    /**
     * Der Name der Rolle, muss einzigartig sein (pro Tenant oder global).
     */
    @field:NotBlank(message = "Der Rollenname darf nicht leer sein")
    @field:Size(min = 3, max = 50, message = "Der Rollenname muss zwischen 3 und 50 Zeichen lang sein")
    val name: String,

    /**
     * Die Beschreibung der Rolle, optional.
     */
    @field:Size(max = 255, message = "Die Beschreibung darf maximal 255 Zeichen lang sein")
    val description: String? = null,
)
