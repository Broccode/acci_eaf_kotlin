package com.acci.eaf.core.tenant.axon

import com.acci.eaf.core.interfaces.TenantInfo
import com.acci.eaf.core.interfaces.TenantServiceApi
import com.acci.eaf.core.interfaces.TenantStatus
import com.acci.eaf.core.tenant.TenantContextHolder
import org.axonframework.messaging.InterceptorChain
import org.axonframework.messaging.Message
import org.axonframework.messaging.MetaData
import org.axonframework.messaging.unitofwork.UnitOfWork
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.Mockito.times
import org.mockito.Mockito.lenient
import org.mockito.junit.jupiter.MockitoExtension
import java.util.UUID
import org.junit.jupiter.api.Assertions.*

@ExtendWith(MockitoExtension::class)
class TenantMessageHandlerInterceptorTest {
    
    @Mock
    private lateinit var tenantService: TenantServiceApi
    
    @Mock
    private lateinit var message: Message<Any>
    
    @Mock
    private lateinit var unitOfWork: UnitOfWork<Message<Any>>
    
    @Mock
    private lateinit var interceptorChain: InterceptorChain
    
    private lateinit var interceptor: TenantMessageHandlerInterceptor
    
    @BeforeEach
    fun setUp() {
        interceptor = TenantMessageHandlerInterceptor(tenantService)
        
        // Grundlegende Mock-Konfiguration mit lenient(), damit unnötige Stubs nicht zu Fehlern führen
        lenient().`when`(unitOfWork.message).thenReturn(message)
        // Wichtig: Mock für getPayloadType(), um NullPointerException zu vermeiden
        lenient().`when`(message.payloadType).thenReturn(Any::class.java) 
        lenient().`when`(interceptorChain.proceed()).thenReturn("result")
    }
    
    @AfterEach
    fun tearDown() {
        TenantContextHolder.clear()
    }
    
    @Test
    fun `should proceed with message handling if no tenant ID in metadata`() {
        // Given
        `when`(message.metaData).thenReturn(MetaData.emptyInstance())
        
        // When
        val result = interceptor.handle(unitOfWork, interceptorChain)
        
        // Then
        assertEquals("result", result)
        assertNull(TenantContextHolder.getCurrentTenantId())
        verify(interceptorChain, times(1)).proceed()
    }
    
    @Test
    fun `should set tenant context and proceed with valid tenant ID`() {
        // Given
        val tenantId = UUID.randomUUID()
        val metadata = MetaData.with(TenantMessageHandlerInterceptor.TENANT_KEY, tenantId.toString())
        
        `when`(message.metaData).thenReturn(metadata)
        
        val activeTenant = TenantInfo(
            tenantId = tenantId,
            status = TenantStatus.ACTIVE
        )
        
        `when`(tenantService.getTenantById(tenantId)).thenReturn(activeTenant)
        
        // When
        val result = interceptor.handle(unitOfWork, interceptorChain)
        
        // Then
        assertEquals("result", result)
        assertNull(TenantContextHolder.getCurrentTenantId()) // Should be cleared after handling
        verify(interceptorChain, times(1)).proceed()
    }
    
    @Test
    fun `should throw exception when tenant is not active`() {
        // Given
        val tenantId = UUID.randomUUID()
        val metadata = MetaData.with(TenantMessageHandlerInterceptor.TENANT_KEY, tenantId.toString())
        
        `when`(message.metaData).thenReturn(metadata)
        
        val inactiveTenant = TenantInfo(
            tenantId = tenantId,
            status = TenantStatus.INACTIVE
        )
        
        `when`(tenantService.getTenantById(tenantId)).thenReturn(inactiveTenant)
        
        // When/Then
        assertThrows(TenantNotActiveException::class.java) {
            interceptor.handle(unitOfWork, interceptorChain)
        }
        
        assertNull(TenantContextHolder.getCurrentTenantId())
        verify(interceptorChain, times(0)).proceed()
    }
    
    @Test
    fun `should throw exception when tenant ID is invalid UUID`() {
        // Given
        val metadata = MetaData.with(TenantMessageHandlerInterceptor.TENANT_KEY, "invalid-uuid")
        
        `when`(message.metaData).thenReturn(metadata)
        
        // When/Then
        assertThrows(IllegalArgumentException::class.java) {
            interceptor.handle(unitOfWork, interceptorChain)
        }
        
        assertNull(TenantContextHolder.getCurrentTenantId())
        verify(interceptorChain, times(0)).proceed()
    }
    
    @Test
    fun `should throw exception when tenant service throws exception`() {
        // Given
        val tenantId = UUID.randomUUID()
        val metadata = MetaData.with(TenantMessageHandlerInterceptor.TENANT_KEY, tenantId.toString())
        
        `when`(message.metaData).thenReturn(metadata)
        
        `when`(tenantService.getTenantById(tenantId)).thenThrow(RuntimeException("Tenant not found"))
        
        // When/Then
        assertThrows(RuntimeException::class.java) {
            interceptor.handle(unitOfWork, interceptorChain)
        }
        
        assertNull(TenantContextHolder.getCurrentTenantId())
        verify(interceptorChain, times(0)).proceed()
    }
} 