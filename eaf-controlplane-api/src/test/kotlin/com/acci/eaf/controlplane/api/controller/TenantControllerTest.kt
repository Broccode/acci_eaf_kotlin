package com.acci.eaf.controlplane.api.controller

import com.acci.eaf.controlplane.api.audit.AuditLogger
import com.acci.eaf.controlplane.api.dto.CreateTenantRequestDto
import com.acci.eaf.controlplane.api.dto.PagedTenantsResponseDto
import com.acci.eaf.controlplane.api.dto.TenantPageParams
import com.acci.eaf.controlplane.api.dto.TenantResponseDto
import com.acci.eaf.controlplane.api.dto.UpdateTenantRequestDto
import com.acci.eaf.controlplane.api.mapper.TenantMapperInterface
import com.acci.eaf.controlplane.api.service.TenantPageService
import com.acci.eaf.core.tenant.TenantContextHolder
import com.acci.eaf.multitenancy.domain.TenantStatus
import com.acci.eaf.multitenancy.dto.CreateTenantDto
import com.acci.eaf.multitenancy.dto.TenantDto
import com.acci.eaf.multitenancy.dto.UpdateTenantDto
import com.acci.eaf.multitenancy.service.TenantService
import com.fasterxml.jackson.databind.ObjectMapper
import java.time.Instant
import java.util.UUID
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

/**
 * Einfache Unit-Tests für den TenantController.
 *
 * Diese Tests verwenden einen direkten Ansatz ohne Spring-Kontext,
 * indem alle Dependencies gemockt werden und der Controller direkt aufgerufen wird.
 */
class TenantControllerTest {

    private lateinit var tenantService: TenantService
    private lateinit var tenantPageService: TenantPageService
    private lateinit var tenantMapper: TenantMapperInterface
    private lateinit var auditLogger: AuditLogger
    private lateinit var controller: TenantController
    private lateinit var objectMapper: ObjectMapper

    private val testTenantId = UUID.randomUUID()
    private val now = Instant.now()
    private lateinit var testTenantDto: TenantDto
    private lateinit var testResponseDto: TenantResponseDto

    @BeforeEach
    fun setUp() {
        // Mocks erstellen
        tenantService = Mockito.mock(TenantService::class.java)
        tenantPageService = Mockito.mock(TenantPageService::class.java)
        tenantMapper = Mockito.mock(TenantMapperInterface::class.java)
        auditLogger = Mockito.mock(AuditLogger::class.java)
        objectMapper = ObjectMapper()

        // Testdaten vorbereiten
        testTenantDto = TenantDto(
            tenantId = testTenantId,
            name = "test-tenant",
            status = TenantStatus.ACTIVE,
            createdAt = now,
            updatedAt = now
        )

        testResponseDto = TenantResponseDto(
            tenantId = testTenantId,
            name = "test-tenant",
            status = TenantStatus.ACTIVE,
            createdAt = now,
            updatedAt = now
        )

        // Controller erstellen
        controller = TenantController(
            tenantService = tenantService,
            tenantPageService = tenantPageService,
            tenantMapper = tenantMapper,
            auditLogger = auditLogger
        )

        // Standard-Mocks konfigurieren
        `when`(tenantService.getTenantById(testTenantId)).thenReturn(testTenantDto)
        `when`(tenantMapper.toResponseDto(testTenantDto)).thenReturn(testResponseDto)

        // TenantContext setzen
        TenantContextHolder.setTenantId(testTenantId)

        // ServletRequest konfigurieren für Tests mit ServletUriComponentsBuilder
        val mockRequest = MockHttpServletRequest()
        mockRequest.scheme = "http"
        mockRequest.serverName = "localhost"
        mockRequest.serverPort = 8080
        mockRequest.contextPath = ""
        mockRequest.servletPath = "/tenants"
        RequestContextHolder.setRequestAttributes(ServletRequestAttributes(mockRequest))
    }

    @AfterEach
    fun tearDown() {
        TenantContextHolder.clear()
        RequestContextHolder.resetRequestAttributes()
    }

    @Test
    fun testCreateTenant() {
        // Testdaten
        val createRequestDto = CreateTenantRequestDto(
            name = "test-tenant",
            status = TenantStatus.PENDING_VERIFICATION,
            adminEmail = "admin@test.com"
        )

        val createTenantDto = CreateTenantDto(
            name = "test-tenant",
            status = TenantStatus.PENDING_VERIFICATION
        )

        val createdTenantDto = TenantDto(
            tenantId = testTenantId,
            name = "test-tenant",
            status = TenantStatus.PENDING_VERIFICATION,
            createdAt = now,
            updatedAt = now
        )

        val responseDto = TenantResponseDto(
            tenantId = testTenantId,
            name = "test-tenant",
            status = TenantStatus.PENDING_VERIFICATION,
            createdAt = now,
            updatedAt = now
        )

        // Mocks konfigurieren
        `when`(tenantMapper.toServiceDto(createRequestDto)).thenReturn(createTenantDto)
        `when`(tenantService.createTenant(createTenantDto)).thenReturn(createdTenantDto)
        `when`(tenantMapper.toResponseDto(createdTenantDto)).thenReturn(responseDto)

        // Testdurchführung
        val response = controller.createTenant(createRequestDto)

        // Überprüfungen
        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertEquals(responseDto, response.body)
        assertNotNull(response.headers.location)

        // Anstatt die genaue URL zu testen, prüfen wir, ob sie den testTenantId enthält
        val locationUrl = response.headers.location.toString()
        assert(locationUrl.contains(testTenantId.toString())) {
            "Die Location-URL '$locationUrl' enthält nicht die erwartete Tenant-ID '$testTenantId'"
        }

        // Verify-Aufrufe
        verify(tenantMapper).toServiceDto(createRequestDto)
        verify(tenantService).createTenant(createTenantDto)
        verify(tenantMapper).toResponseDto(createdTenantDto)
        verify(auditLogger).logTenantCreation(responseDto.tenantId, responseDto.name)
    }

    @Test
    fun testGetTenants() {
        // Testdaten
        val pageParams = TenantPageParams(
            page = 0,
            size = 10,
            status = null,
            nameContains = null
        )

        val tenantPage = PageImpl(
            listOf(testTenantDto),
            PageRequest.of(0, 10),
            1
        )

        val pagedResponse = PagedTenantsResponseDto(
            tenants = listOf(testResponseDto),
            page = 0,
            size = 10,
            totalElements = 1,
            totalPages = 1
        )

        // Mocks konfigurieren
        `when`(tenantPageService.getTenants(pageParams)).thenReturn(tenantPage)
        `when`(tenantMapper.toPagedResponseDto(tenantPage)).thenReturn(pagedResponse)

        // Testdurchführung
        val response = controller.getTenants(0, 10, null, null)

        // Überprüfungen
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(pagedResponse, response.body)

        // Verify-Aufrufe
        verify(tenantPageService).getTenants(pageParams)
        verify(tenantMapper).toPagedResponseDto(tenantPage)
    }

    @Test
    fun testGetTenantById() {
        // Testdurchführung
        val response = controller.getTenantById(testTenantId)

        // Überprüfungen
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(testResponseDto, response.body)

        // Verify-Aufrufe
        verify(tenantService).getTenantById(testTenantId)
        verify(tenantMapper).toResponseDto(testTenantDto)
    }

    @Test
    fun testUpdateTenant() {
        // Testdaten
        val updatedName = "updated-tenant-name"
        val updateRequestDto = UpdateTenantRequestDto(
            name = updatedName,
            status = TenantStatus.ACTIVE
        )

        val updateTenantDto = UpdateTenantDto(
            name = updatedName,
            status = TenantStatus.ACTIVE
        )

        val updatedTenantDto = testTenantDto.copy(name = updatedName)
        val updatedResponseDto = testResponseDto.copy(name = updatedName)

        // Mocks konfigurieren
        `when`(tenantMapper.toServiceDto(updateRequestDto)).thenReturn(updateTenantDto)
        `when`(tenantService.updateTenant(testTenantId, updateTenantDto)).thenReturn(updatedTenantDto)
        `when`(tenantMapper.toResponseDto(updatedTenantDto)).thenReturn(updatedResponseDto)

        // Testdurchführung
        val response = controller.updateTenant(testTenantId, updateRequestDto)

        // Überprüfungen
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(updatedResponseDto, response.body)

        // Verify-Aufrufe
        verify(tenantMapper).toServiceDto(updateRequestDto)
        verify(tenantService).updateTenant(testTenantId, updateTenantDto)
        verify(tenantMapper).toResponseDto(updatedTenantDto)
        verify(auditLogger).logTenantUpdate(testTenantId, updatedName, mapOf("name" to updatedName, "status" to TenantStatus.ACTIVE))
    }

    @Test
    fun testDeleteTenant() {
        // Testdaten
        val archivedTenantDto = testTenantDto.copy(status = TenantStatus.ARCHIVED)

        // Mocks konfigurieren
        `when`(tenantService.deleteTenant(testTenantId)).thenReturn(archivedTenantDto)

        // Testdurchführung
        val response = controller.deleteTenant(testTenantId)

        // Überprüfungen
        assertEquals(HttpStatus.NO_CONTENT, response.statusCode)

        // Verify-Aufrufe
        verify(tenantService).getTenantById(testTenantId)
        verify(tenantService).deleteTenant(testTenantId)
        verify(auditLogger).logTenantDeletion(testTenantId, testTenantDto.name)
    }
}
