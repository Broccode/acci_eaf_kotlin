package com.acci.eaf.iam.adapter.rest.dto

/**
 * DTO für die Antwort nach erfolgreicher Benutzerauthentifizierung.
 */
data class LoginResponseDTO(
    /**
     * Der Access Token (JWT), der für nachfolgende Anfragen verwendet werden kann.
     */
    val accessToken: String,

    /**
     * Optional: Der Refresh Token, der verwendet werden kann, um einen neuen Access Token zu erhalten.
     */
    val refreshToken: String? = null,

    /**
     * Der Typ des Tokens, typischerweise "Bearer".
     */
    val tokenType: String = "Bearer",

    /**
     * Die Gültigkeitsdauer des Access Tokens in Sekunden.
     */
    val expiresIn: Long,
)
