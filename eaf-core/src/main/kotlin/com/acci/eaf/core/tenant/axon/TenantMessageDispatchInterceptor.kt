package com.acci.eaf.core.tenant.axon

import com.acci.eaf.core.tenant.TenantContextHolder
import org.axonframework.messaging.Message
import org.axonframework.messaging.MessageDispatchInterceptor
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.function.BiFunction

/**
 * Interceptor für ausgehende Axon-Nachrichten, der die aktuelle Tenant-ID in die Message-Metadaten einfügt.
 * 
 * Dieser Interceptor wird auf alle ausgehenden Command-, Event- und Query-Nachrichten angewendet
 * und sorgt dafür, dass die Tenant-ID über Prozessgrenzen hinweg propagiert wird.
 */
@Component
class TenantMessageDispatchInterceptor<T : Message<*>> : MessageDispatchInterceptor<T> {
    
    private val logger = LoggerFactory.getLogger(TenantMessageDispatchInterceptor::class.java)
    
    companion object {
        const val TENANT_KEY = "tenantId"
    }
    
    /**
     * Fügt die aktuelle Tenant-ID als Metadaten zu einer Nachricht hinzu.
     * 
     * @param messages Liste der auszusendenden Nachrichten
     * @return Eine Funktion, die eine Nachricht mit hinzugefügten Tenant-Metadaten zurückgibt
     */
    override fun handle(messages: List<T>): BiFunction<Int, T, T> {
        return BiFunction { index, message ->
            val tenantId = TenantContextHolder.getCurrentTenantId()
            
            if (tenantId != null) {
                logger.debug("Adding tenant ID {} to message metadata of type {}", tenantId, message.payloadType.simpleName)
                message.withMetaData(mapOf(TENANT_KEY to tenantId.toString())) as T
            } else {
                logger.debug("No tenant ID available in context for message of type {}", message.payloadType.simpleName)
                message
            }
        }
    }
} 