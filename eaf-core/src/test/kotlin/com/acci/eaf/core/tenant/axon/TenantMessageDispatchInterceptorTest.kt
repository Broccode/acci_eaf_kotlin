package com.acci.eaf.core.tenant.axon

import com.acci.eaf.core.tenant.TenantContextHolder
import java.util.UUID
import org.axonframework.messaging.Message
import org.axonframework.messaging.MetaData
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class TenantMessageDispatchInterceptorTest {

    @Mock
    private lateinit var message: Message<String>

    @Mock
    private lateinit var resultMessage: Message<String>

    private lateinit var interceptor: TenantMessageDispatchInterceptor<Message<String>>

    @BeforeEach
    fun setUp() {
        interceptor = TenantMessageDispatchInterceptor()
        Mockito.lenient().`when`(message.payloadType).thenReturn(String::class.java)

        // Einfache Stub-Konfiguration für alle Tests
        Mockito.lenient().`when`(message.withMetaData(Mockito.any())).thenReturn(resultMessage)
    }

    @AfterEach
    fun tearDown() {
        TenantContextHolder.clear()
    }

    @Test
    fun `should add tenant ID to message metadata when tenant context is set`() {
        // Given
        val tenantId = UUID.randomUUID()
        TenantContextHolder.setTenantId(tenantId)

        // MetaData für das Ergebnis-Mock konfigurieren
        val expectedMetadata = MetaData.with(TenantMessageDispatchInterceptor.TENANT_KEY, tenantId.toString())
        Mockito.`when`(resultMessage.metaData).thenReturn(expectedMetadata)

        // When
        val resultFunction = interceptor.handle(emptyList())
        val actualMessage = resultFunction.apply(0, message)

        // Then
        assertEquals(tenantId.toString(), actualMessage.metaData[TenantMessageDispatchInterceptor.TENANT_KEY])

        // Überprüfe, dass withMetaData mit einer Map aufgerufen wurde, die den Tenant-ID-Schlüssel enthält
        Mockito.verify(message).withMetaData(
            Mockito.argThat { metadata: Map<String, Any> ->
                metadata.containsKey(TenantMessageDispatchInterceptor.TENANT_KEY) &&
                    metadata[TenantMessageDispatchInterceptor.TENANT_KEY] == tenantId.toString()
            }
        )
    }

    @Test
    fun `should not add tenant ID to message metadata when tenant context is not set`() {
        // Given - Kein Tenant ID gesetzt

        // Leere MetaData für den Test zurückgeben
        val emptyMetadata = MetaData.emptyInstance()
        Mockito.`when`(message.metaData).thenReturn(emptyMetadata)

        // When
        val resultFunction = interceptor.handle(emptyList())
        val actualMessage = resultFunction.apply(0, message)

        // Then
        assertNull(actualMessage.metaData[TenantMessageDispatchInterceptor.TENANT_KEY])

        // Statt zu verifizieren, dass withMetaData nicht aufgerufen wird, überprüfen wir,
        // dass die ursprüngliche Nachricht unverändert zurückgegeben wird
        Mockito.verify(message, Mockito.times(0)).withMetaData(Mockito.any())
    }
}
