package com.acci.eaf.controlplane.api.dto

import java.util.UUID

/**
 * DTO for returning client credentials (Client ID and Secret) to the administrator.
 * The secret is only displayed once after creation or rotation.
 */
data class ServiceAccountSecretResponse(
    val serviceAccountId: UUID,
    val clientId: String,
    // This is the actual secret, to be displayed only once.
    val clientSecret: String,
)
