package com.acci.eaf.iam.config

import com.acci.eaf.iam.domain.model.User
import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.security.Key
import java.util.Date
import java.util.UUID

/**
 * Service für die Verwaltung von JWT-Tokens.
 * Verantwortlich für die Generierung, Validierung und Analyse von JWTs.
 */
@Component
class JwtTokenProvider(
    @Value("\${app.jwt.secret}") private val jwtSecret: String,
    @Value("\${app.jwt.expiration}") private val jwtExpirationInMs: Long,
    @Value("\${app.jwt.refresh-expiration:604800000}") private val refreshExpirationInMs: Long
) {

    private val key: Key = Keys.hmacShaKeyFor(jwtSecret.toByteArray())

    /**
     * Generiert einen JWT Access Token für einen Benutzer.
     *
     * @param user der Benutzer, für den der Token generiert wird
     * @param roles die Rollen des Benutzers (optional)
     * @return der generierte JWT als String
     */
    fun generateAccessToken(user: User, roles: List<String> = emptyList()): String {
        val now = Date()
        val expiryDate = Date(now.time + jwtExpirationInMs)

        return Jwts.builder()
            .subject(user.id.toString())
            .claim("tenantId", user.tenantId.toString())
            .claim("username", user.username)
            .claim("roles", roles)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(key)
            .compact()
    }

    /**
     * Generiert einen JWT Refresh Token für einen Benutzer.
     *
     * @param user der Benutzer, für den der Token generiert wird
     * @return der generierte JWT als String
     */
    fun generateRefreshToken(user: User): String {
        val now = Date()
        val expiryDate = Date(now.time + refreshExpirationInMs)

        return Jwts.builder()
            .subject(user.id.toString())
            .claim("tokenType", "refresh")
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(key)
            .compact()
    }

    /**
     * Validiert einen JWT Token.
     *
     * @param token der zu validierende JWT
     * @return true, wenn der Token gültig ist, sonst false
     */
    fun validateToken(token: String): Boolean {
        try {
            Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseSignedClaims(token)
            return true
        } catch (ex: JwtException) {
            return false
        } catch (ex: IllegalArgumentException) {
            return false
        }
    }

    /**
     * Extrahiert die Benutzer-ID aus einem JWT.
     *
     * @param token der JWT
     * @return die Benutzer-ID als UUID
     */
    fun getUserIdFromToken(token: String): UUID {
        val claims = getClaimsFromToken(token)
        return UUID.fromString(claims.subject)
    }

    /**
     * Extrahiert die Tenant-ID aus einem JWT.
     *
     * @param token der JWT
     * @return die Tenant-ID als UUID
     */
    fun getTenantIdFromToken(token: String): UUID {
        val claims = getClaimsFromToken(token)
        return UUID.fromString(claims.get("tenantId", String::class.java))
    }

    /**
     * Extrahiert den Benutzernamen aus einem JWT.
     *
     * @param token der JWT
     * @return der Benutzername
     */
    fun getUsernameFromToken(token: String): String {
        val claims = getClaimsFromToken(token)
        return claims.get("username", String::class.java)
    }

    /**
     * Extrahiert die Rollen aus einem JWT.
     *
     * @param token der JWT
     * @return die Liste der Rollen
     */
    @Suppress("UNCHECKED_CAST")
    fun getRolesFromToken(token: String): List<String> {
        val claims = getClaimsFromToken(token)
        return claims.get("roles", List::class.java) as? List<String> ?: emptyList()
    }

    /**
     * Gibt die Ablaufzeit des Access Tokens in Sekunden zurück.
     *
     * @return die Ablaufzeit in Sekunden
     */
    fun getAccessTokenExpirationInSeconds(): Long {
        return jwtExpirationInMs / 1000
    }

    /**
     * Extrahiert die Claims aus einem JWT.
     *
     * @param token der JWT
     * @return die Claims
     */
    private fun getClaimsFromToken(token: String): Claims {
        return Jwts.parser()
            .setSigningKey(key)
            .build()
            .parseSignedClaims(token)
            .payload
    }
}
