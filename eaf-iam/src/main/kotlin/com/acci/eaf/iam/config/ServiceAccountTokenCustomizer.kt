package com.acci.eaf.iam.config

import com.acci.eaf.iam.adapter.persistence.ServiceAccountClientDetailsService
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer
import org.springframework.stereotype.Component

/**
 * Customizes JWT tokens for Service Account authentication.
 * Adds service account specific claims like serviceAccountId, clientId, tenantId, and roles.
 */
@Component
class ServiceAccountTokenCustomizer(private val serviceAccountClientDetailsService: ServiceAccountClientDetailsService) :
    OAuth2TokenCustomizer<JwtEncodingContext> {

    override fun customize(context: JwtEncodingContext) {
        if (context.tokenType.value == "access_token") {
            val clientId = context.registeredClient.clientId

            // Get service account details
            val serviceAccount = serviceAccountClientDetailsService.findByClientId(clientId)
                ?.let { registeredClient ->
                    // Extract additional claims from our service account data
                    serviceAccountClientDetailsService.getServiceAccountDetails(clientId)
                }

            if (serviceAccount != null) {
                val claims = context.claims

                // Add service account specific claims
                claims.claim("serviceAccountId", serviceAccount.serviceAccountId.toString())
                claims.claim("clientId", serviceAccount.clientId)
                claims.claim("tenantId", serviceAccount.tenantId.toString())
                claims.claim("roles", serviceAccount.roles.map { "ROLE_SERVICE_ACCOUNT_$it" })
                claims.claim("type", "service_account")

                // TODO: In a full implementation, we would resolve the actual permissions
                // from the roles and include them in the token for fine-grained access control
                // For now, we include the role IDs which can be used for authorization

                // Ensure token expiry doesn't exceed service account expiry
                if (serviceAccount.expiresAt != null) {
                    val serviceAccountExpiry = serviceAccount.expiresAt.toInstant()
                    val currentExpiry = claims.build().expiresAt

                    if (currentExpiry != null && serviceAccountExpiry.isBefore(currentExpiry)) {
                        claims.expiresAt(serviceAccountExpiry)
                    }
                }
            }
        }
    }
}
