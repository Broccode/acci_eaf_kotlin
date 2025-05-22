package com.acci.eaf.iam.adapter.rest.dto

import jakarta.validation.constraints.NotBlank

/**
 * DTO für die Anfrage zur Benutzerauthentifizierung.
 */
data class LoginRequestDTO(
    /**
     * Der Benutzername oder die E-Mail-Adresse des Benutzers.
     * Kann in dem Format "benutzer@tenant" sein, um den Tenant anzugeben.
     */
    @field:NotBlank(message = "Username/Email ist erforderlich")
    val usernameOrEmail: String,

    /**
     * Das Passwort des Benutzers.
     */
    @field:NotBlank(message = "Passwort ist erforderlich")
    val password: String,

    /**
     * Optionale Tenant-Kennung, falls nicht im Benutzernamen enthalten.
     */
    val tenantHint: String? = null,
)
