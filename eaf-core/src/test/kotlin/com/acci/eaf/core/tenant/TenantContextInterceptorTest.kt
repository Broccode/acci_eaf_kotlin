package com.acci.eaf.core.tenant

import com.acci.eaf.core.interfaces.TenantInfo
import com.acci.eaf.core.interfaces.TenantServiceApi
import com.acci.eaf.core.interfaces.TenantStatus
import java.util.UUID
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse

@ExtendWith(MockitoExtension::class)
class TenantContextInterceptorTest {

    @Mock
    private lateinit var tenantService: TenantServiceApi

    private lateinit var tenantContextInterceptor: TenantContextInterceptor
    private lateinit var request: MockHttpServletRequest
    private lateinit var response: MockHttpServletResponse

    @BeforeEach
    fun setUp() {
        tenantContextInterceptor = TenantContextInterceptor(tenantService)
        request = MockHttpServletRequest()
        response = MockHttpServletResponse()
    }

    @AfterEach
    fun tearDown() {
        TenantContextHolder.clear()
    }

    @Test
    fun `should accept request with valid active tenant ID header`() {
        // Given
        val tenantId = UUID.randomUUID()
        request.addHeader(TenantContextInterceptor.TENANT_HEADER, tenantId.toString())

        val activeTenant = TenantInfo(
            tenantId = tenantId,
            status = TenantStatus.ACTIVE
        )

        `when`(tenantService.getTenantById(tenantId)).thenReturn(activeTenant)

        // When
        val result = tenantContextInterceptor.preHandle(request, response, Any())

        // Then
        assertTrue(result)
        assertEquals(tenantId, TenantContextHolder.getCurrentTenantId())
        assertEquals(200, response.status)
    }

    @Test
    fun `should reject request with missing tenant ID header`() {
        // Given - No header added

        // When
        val result = tenantContextInterceptor.preHandle(request, response, Any())

        // Then
        assertFalse(result)
        assertNull(TenantContextHolder.getCurrentTenantId())
        assertEquals(400, response.status)
        assertTrue(response.contentAsString.contains("Missing tenant ID"))
    }

    @Test
    fun `should reject request with invalid UUID format`() {
        // Given
        request.addHeader(TenantContextInterceptor.TENANT_HEADER, "invalid-uuid")

        // When
        val result = tenantContextInterceptor.preHandle(request, response, Any())

        // Then
        assertFalse(result)
        assertNull(TenantContextHolder.getCurrentTenantId())
        assertEquals(400, response.status)
        assertTrue(response.contentAsString.contains("Invalid tenant ID format"))
    }

    @Test
    fun `should reject request with inactive tenant ID`() {
        // Given
        val tenantId = UUID.randomUUID()
        request.addHeader(TenantContextInterceptor.TENANT_HEADER, tenantId.toString())

        val inactiveTenant = TenantInfo(
            tenantId = tenantId,
            status = TenantStatus.INACTIVE
        )

        `when`(tenantService.getTenantById(tenantId)).thenReturn(inactiveTenant)

        // When
        val result = tenantContextInterceptor.preHandle(request, response, Any())

        // Then
        assertFalse(result)
        assertNull(TenantContextHolder.getCurrentTenantId())
        assertEquals(403, response.status)
        assertTrue(response.contentAsString.contains("Tenant is not active"))
    }

    @Test
    fun `should reject request with non-existent tenant ID`() {
        // Given
        val tenantId = UUID.randomUUID()
        request.addHeader(TenantContextInterceptor.TENANT_HEADER, tenantId.toString())

        `when`(tenantService.getTenantById(tenantId)).thenThrow(RuntimeException("Tenant not found"))

        // When
        val result = tenantContextInterceptor.preHandle(request, response, Any())

        // Then
        assertFalse(result)
        assertNull(TenantContextHolder.getCurrentTenantId())
        assertEquals(403, response.status)
        assertTrue(response.contentAsString.contains("Invalid tenant ID"))
    }

    @Test
    fun `should clear tenant context after request completion`() {
        // Given
        val tenantId = UUID.randomUUID()
        TenantContextHolder.setTenantId(tenantId)

        // When
        tenantContextInterceptor.afterCompletion(request, response, Any(), null)

        // Then
        assertNull(TenantContextHolder.getCurrentTenantId())
    }
} 
