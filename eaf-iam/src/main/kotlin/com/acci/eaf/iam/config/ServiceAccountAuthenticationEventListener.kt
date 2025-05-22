package com.acci.eaf.iam.config

import com.acci.eaf.iam.adapter.persistence.ServiceAccountClientDetailsService
import java.time.Instant
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent
import org.springframework.security.authentication.event.AuthenticationSuccessEvent
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken
import org.springframework.stereotype.Component

/**
 * Event listener for Service Account authentication events.
 * Handles audit logging for both successful and failed authentication attempts.
 */
@Component
class ServiceAccountAuthenticationEventListener(private val serviceAccountClientDetailsService: ServiceAccountClientDetailsService) {

    private val logger = LoggerFactory.getLogger(ServiceAccountAuthenticationEventListener::class.java)
    private val auditLogger = LoggerFactory.getLogger("AUDIT.SERVICE_ACCOUNT_AUTH")

    @EventListener
    fun onAuthenticationSuccess(event: AuthenticationSuccessEvent) {
        val authentication = event.authentication

        if (authentication is OAuth2ClientAuthenticationToken) {
            val clientId = authentication.registeredClient.clientId
            val serviceAccountDetails = serviceAccountClientDetailsService.getServiceAccountDetails(clientId)

            if (serviceAccountDetails != null) {
                val auditEvent = mapOf(
                    "timestamp" to Instant.now(),
                    "eventType" to "SERVICE_ACCOUNT_AUTH_SUCCESS",
                    "clientId" to clientId,
                    "serviceAccountId" to serviceAccountDetails.serviceAccountId,
                    "tenantId" to serviceAccountDetails.tenantId,
                    "sourceIp" to getSourceIp(),
                    "userAgent" to getUserAgent(),
                    "outcome" to "SUCCESS"
                )

                auditLogger.info("Service account authentication successful: {}", auditEvent)
                logger.debug("Service account {} authenticated successfully", clientId)
            }
        }
    }

    @EventListener
    fun onAuthenticationFailure(event: AbstractAuthenticationFailureEvent) {
        val authentication = event.authentication
        val exception = event.exception

        // Try to extract client ID from the authentication attempt
        val clientId = when (authentication) {
            is OAuth2ClientAuthenticationToken -> authentication.registeredClient?.clientId
            else -> {
                // Try to extract from authentication name or details
                authentication.name ?: "unknown"
            }
        }

        val auditEvent = mapOf(
            "timestamp" to Instant.now(),
            "eventType" to "SERVICE_ACCOUNT_AUTH_FAILURE",
            "clientId" to clientId,
            "sourceIp" to getSourceIp(),
            "userAgent" to getUserAgent(),
            "outcome" to "FAILURE",
            "failureReason" to exception.javaClass.simpleName,
            "errorMessage" to (exception.message ?: "Authentication failed")
        )

        auditLogger.warn("Service account authentication failed: {}", auditEvent)
        logger.warn(
            "Service account authentication failed for client: {}, reason: {}",
            clientId, exception.message
        )
    }

    private fun getSourceIp(): String {
        // In a real implementation, this would extract the IP from the request context
        // For now, we'll use a placeholder
        return "unknown"
    }

    private fun getUserAgent(): String {
        // In a real implementation, this would extract the User-Agent from the request context
        // For now, we'll use a placeholder
        return "unknown"
    }
}
