package com.acci.eaf.core.tenant.axon

import com.acci.eaf.core.interfaces.TenantServiceApi
import com.acci.eaf.core.interfaces.TenantStatus
import com.acci.eaf.core.tenant.TenantContextHolder
import org.axonframework.messaging.InterceptorChain
import org.axonframework.messaging.Message
import org.axonframework.messaging.MessageHandlerInterceptor
import org.axonframework.messaging.unitofwork.UnitOfWork
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.UUID

/**
 * Interceptor für eingehende Axon-Nachrichten, der die Tenant-ID aus den 
 * Message-Metadaten extrahiert und im TenantContextHolder speichert.
 * 
 * Dieser Interceptor stellt sicher, dass beim Verarbeiten von Command-, Event- und
 * Query-Nachrichten der Tenant-Kontext korrekt gesetzt ist, unabhängig davon, ob
 * die Nachricht vom lokalen Prozess oder von einem entfernten Prozess stammt.
 */
@Component
class TenantMessageHandlerInterceptor(private val tenantService: TenantServiceApi) : MessageHandlerInterceptor<Message<*>> {
    
    private val logger = LoggerFactory.getLogger(TenantMessageHandlerInterceptor::class.java)
    
    companion object {
        const val TENANT_KEY = "tenantId"
    }
    
    /**
     * Extrahiert die Tenant-ID aus den Nachrichtenmetadaten, validiert sie und
     * speichert sie im TenantContextHolder für die Dauer der Nachrichtenverarbeitung.
     * 
     * @param unitOfWork Die UnitOfWork
     * @param interceptorChain Die InterceptorChain
     * @return Das Ergebnis der Nachrichtenverarbeitung
     * @throws Exception wenn die Tenant-ID fehlt, ungültig ist oder der Tenant nicht aktiv ist
     */
    override fun handle(unitOfWork: UnitOfWork<out Message<*>>, interceptorChain: InterceptorChain): Any {
        val message = unitOfWork.message
        val tenantIdStr = message.metaData[TENANT_KEY] as? String
        
        if (tenantIdStr == null) {
            logger.warn("No tenant ID found in message metadata for message type: {}", message.payloadType.simpleName)
            return interceptorChain.proceed()
        }
        
        try {
            val tenantId = UUID.fromString(tenantIdStr)
            logger.debug("Found tenant ID {} in message metadata for message type: {}", tenantId, message.payloadType.simpleName)
            
            // Validate tenant
            val tenant = tenantService.getTenantById(tenantId)
            if (tenant.status != TenantStatus.ACTIVE) {
                throw TenantNotActiveException("Tenant with ID $tenantId is not active (status: ${tenant.status})")
            }
            
            // Set tenant ID in thread-local context
            TenantContextHolder.setTenantId(tenantId)
            logger.debug("Set tenant ID {} in context for message type: {}", tenantId, message.payloadType.simpleName)
            
            try {
                // Process the message with tenant context set
                return interceptorChain.proceed()
            } finally {
                // Clear tenant context after message handling
                logger.debug("Clearing tenant context after message handling")
                TenantContextHolder.clear()
            }
            
        } catch (e: IllegalArgumentException) {
            // Invalid UUID format
            logger.error("Invalid tenant ID format in message metadata: {}", tenantIdStr, e)
            throw e
        } catch (e: Exception) {
            // Tenant validation failed
            logger.error("Error validating tenant from message metadata", e)
            throw e
        }
    }
}

/**
 * Exception thrown when a message references a tenant that is not in the ACTIVE state.
 */
class TenantNotActiveException(message: String) : RuntimeException(message)

/**
 * Exception thrown when a message references a tenant that doesn't exist.
 */
class TenantNotFoundException(message: String) : RuntimeException(message) 