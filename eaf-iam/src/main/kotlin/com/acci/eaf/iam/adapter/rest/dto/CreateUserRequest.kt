package com.acci.eaf.iam.adapter.rest.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * Request-DTO für die Erstellung eines neuen Benutzers.
 *
 * Diese Klasse repräsentiert die JSON-Anfrage zum Erstellen eines neuen Benutzers
 * und enthält Validierungsannotationen zur Sicherstellung der Datenintegrität.
 */
data class CreateUserRequest(
    /**
     * Der Benutzername für den neuen Benutzer.
     * Muss für den Tenant einzigartig sein.
     */
    @field:NotBlank(message = "Username is required")
    @field:Size(min = 3, max = 255, message = "Username must be between 3 and 255 characters")
    val username: String,

    /**
     * Das Passwort im Klartext für den neuen Benutzer.
     * Wird niemals persistiert, sondern nur für die Erstellung gehasht.
     */
    @field:NotBlank(message = "Password is required")
    val password: String,

    /**
     * Die E-Mail-Adresse des Benutzers.
     */
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    val email: String,

    /**
     * Der Vorname des Benutzers (optional).
     */
    @field:Size(max = 255, message = "First name must be at most 255 characters")
    val firstName: String? = null,

    /**
     * Der Nachname des Benutzers (optional).
     */
    @field:Size(max = 255, message = "Last name must be at most 255 characters")
    val lastName: String? = null,
)
