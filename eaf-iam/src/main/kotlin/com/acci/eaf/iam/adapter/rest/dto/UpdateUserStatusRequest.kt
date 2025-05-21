package com.acci.eaf.iam.adapter.rest.dto

import com.acci.eaf.iam.domain.model.UserStatus
import jakarta.validation.constraints.NotNull

/**
 * Request-DTO für die Aktualisierung des Status eines Benutzers.
 *
 * Diese Klasse repräsentiert die JSON-Anfrage zum Aktualisieren des Status eines Benutzers.
 */
data class UpdateUserStatusRequest(
    /**
     * Der neue Status des Benutzers.
     */
    @field:NotNull(message = "Status is required")
    val status: UserStatus,
)
