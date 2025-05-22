package com.acci.eaf.iam.adapter.persistence

import com.acci.eaf.iam.domain.model.ServiceAccountStatus
import com.acci.eaf.iam.domain.repository.ServiceAccountRepository
import java.time.Instant
import java.util.*
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.ClientAuthenticationMethod
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings
import org.springframework.stereotype.Service

/**
 * Service that implements RegisteredClientRepository to provide OAuth2 Client Details
 * for Service Account authentication using Client Credentials Grant Flow.
 */
@Service
class ServiceAccountClientDetailsService(
    private val serviceAccountRepository: ServiceAccountRepository,
    private val passwordEncoder: PasswordEncoder,
) : RegisteredClientRepository {

    override fun save(registeredClient: RegisteredClient): Unit =
        throw UnsupportedOperationException("Service accounts are managed through the management API")

    override fun findById(id: String): RegisteredClient? {
        // ID in this context is the clientId
        return findByClientId(id)
    }

    override fun findByClientId(clientId: String): RegisteredClient? {
        val serviceAccount = serviceAccountRepository.findByClientId(clientId)
            ?: return null

        // Check if service account is active and not expired
        if (serviceAccount.status != ServiceAccountStatus.ACTIVE) {
            return null
        }

        val now = Instant.now()
        if (serviceAccount.expiresAt != null && serviceAccount.expiresAt.toInstant().isBefore(now)) {
            return null
        }

        // Convert roles to authorities
        val authorities = serviceAccount.roles.map { roleId ->
            SimpleGrantedAuthority("ROLE_SERVICE_ACCOUNT_$roleId")
        }.toSet<GrantedAuthority>()

        return RegisteredClient.withId(serviceAccount.serviceAccountId.toString())
            .clientId(serviceAccount.clientId)
            .clientSecret(serviceAccount.clientSecretHash) // This is the hashed version
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
            .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
            .scope("api") // Default scope for service accounts
            .clientSettings(
                ClientSettings.builder()
                    .requireAuthorizationConsent(false)
                    .requireProofKey(false)
                    .build()
            )
            .tokenSettings(
                TokenSettings.builder()
                    .accessTokenTimeToLive(java.time.Duration.ofHours(1)) // 1 hour default
                    .refreshTokenTimeToLive(java.time.Duration.ZERO) // No refresh tokens for client credentials
                    .build()
            )
            .build()
    }

    /**
     * Validates the client credentials against the stored hash.
     */
    fun validateClientCredentials(clientId: String, clientSecret: String): Boolean {
        val serviceAccount = serviceAccountRepository.findByClientId(clientId)
            ?: return false

        // Check if service account is active and not expired
        if (serviceAccount.status != ServiceAccountStatus.ACTIVE) {
            return false
        }

        val now = Instant.now()
        if (serviceAccount.expiresAt != null && serviceAccount.expiresAt.toInstant().isBefore(now)) {
            return false
        }

        return passwordEncoder.matches(clientSecret, serviceAccount.clientSecretHash)
    }

    /**
     * Gets the authorities (roles) for a given client ID.
     */
    fun getClientAuthorities(clientId: String): Set<GrantedAuthority> {
        val serviceAccount = serviceAccountRepository.findByClientId(clientId)
            ?: return emptySet()

        return serviceAccount.roles.map { roleId ->
            SimpleGrantedAuthority("ROLE_SERVICE_ACCOUNT_$roleId")
        }.toSet()
    }

    /**
     * Gets service account details for token customization.
     */
    fun getServiceAccountDetails(clientId: String): ServiceAccountTokenDetails? {
        val serviceAccount = serviceAccountRepository.findByClientId(clientId)
            ?: return null

        return ServiceAccountTokenDetails(
            serviceAccountId = serviceAccount.serviceAccountId,
            clientId = serviceAccount.clientId,
            tenantId = serviceAccount.tenantId,
            roles = serviceAccount.roles,
            expiresAt = serviceAccount.expiresAt
        )
    }
}

/**
 * Data class for Service Account details used in token customization.
 */
data class ServiceAccountTokenDetails(
    val serviceAccountId: UUID,
    val clientId: String,
    val tenantId: UUID,
    val roles: Set<UUID>,
    val expiresAt: java.time.OffsetDateTime?,
)
