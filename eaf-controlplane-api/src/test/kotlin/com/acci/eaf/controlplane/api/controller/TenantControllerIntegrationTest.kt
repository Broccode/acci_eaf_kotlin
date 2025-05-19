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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyMap
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

/**
 * Integrationstests für den TenantController mit WebMvcTest.
 *
 * Diese Tests verwenden einen WebMvcTest-Ansatz, der besser für Controller-Tests geeignet ist
 * und die Probleme mit ServletUriComponentsBuilder umgeht.
 *
 * HINWEIS: Dieser Test ist deaktiviert, da er Probleme mit JPA und Persistence-Abhängigkeiten hat.
 * Stattdessen sollte der TenantControllerTest verwendet werden, der ohne Spring-Kontext arbeitet
 * und dieselbe Funktionalität testet.
 */
@Disabled("Dieser Test ist deaktiviert aufgrund von Problemen mit JPA und Persistence-Abhängigkeiten. Verwende stattdessen TenantControllerTest.")
@WebMvcTest(TenantController::class)
@Import(WebMvcTestConfig::class)
class TenantControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var tenantService: TenantService

    @MockBean
    private lateinit var tenantPageService: TenantPageService

    @MockBean
    private lateinit var tenantMapper: TenantMapperInterface

    @MockBean
    private lateinit var auditLogger: AuditLogger

    private val testTenantId = UUID.randomUUID()
    private val now = Instant.now()
    private lateinit var testTenantDto: TenantDto
    private lateinit var testResponseDto: TenantResponseDto

    @BeforeEach
    fun setUp() {
        // Testdaten vorbereiten
        testTenantDto = TenantDto(
            tenantId = testTenantId,
            name = "test-tenant-integration",
            status = TenantStatus.ACTIVE,
            createdAt = now,
            updatedAt = now
        )

        testResponseDto = TenantResponseDto(
            tenantId = testTenantId,
            name = "test-tenant-integration",
            status = TenantStatus.ACTIVE,
            createdAt = now,
            updatedAt = now
        )

        // Standard-Mocks konfigurieren
        `when`(tenantService.getTenantById(eq(testTenantId))).thenReturn(testTenantDto)
        `when`(tenantMapper.toResponseDto(any(TenantDto::class.java))).thenReturn(testResponseDto)

        // TenantContext setzen
        TenantContextHolder.setTenantId(testTenantId)

        // ServletRequest konfigurieren für Tests (nicht mehr benötigt für MockMvc)
        val mockRequest = MockHttpServletRequest()
        mockRequest.scheme = "http"
        mockRequest.serverName = "localhost"
        mockRequest.serverPort = 8080
        mockRequest.contextPath = ""
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
        `when`(tenantMapper.toServiceDto(any(CreateTenantRequestDto::class.java))).thenReturn(createTenantDto)
        `when`(tenantService.createTenant(any(CreateTenantDto::class.java))).thenReturn(createdTenantDto)
        `when`(tenantMapper.toResponseDto(any(TenantDto::class.java))).thenReturn(responseDto)

        // Testdurchführung mit MockMvc
        mockMvc.perform(
            MockMvcRequestBuilders.post("/tenants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequestDto))
        )
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.jsonPath("$.tenantId").value(testTenantId.toString()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("test-tenant"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("PENDING_VERIFICATION"))
            .andExpect(MockMvcResultMatchers.header().exists("Location"))

        // Verify-Aufrufe
        verify(tenantMapper).toServiceDto(any(CreateTenantRequestDto::class.java))
        verify(tenantService).createTenant(any(CreateTenantDto::class.java))
        verify(tenantMapper).toResponseDto(any(TenantDto::class.java))
        verify(auditLogger).logTenantCreation(eq(responseDto.tenantId), eq(responseDto.name))
    }

    @Test
    fun testGetTenants() {
        // Testdaten
        val pagedResponse = PagedTenantsResponseDto(
            tenants = listOf(testResponseDto),
            page = 0,
            size = 10,
            totalElements = 1,
            totalPages = 1
        )

        val tenantPage = PageImpl(
            listOf(testTenantDto),
            PageRequest.of(0, 10),
            1
        )

        // Mocks konfigurieren
        `when`(tenantPageService.getTenants(any(TenantPageParams::class.java))).thenReturn(tenantPage)
        `when`(tenantMapper.toPagedResponseDto(any())).thenReturn(pagedResponse)

        // Testdurchführung mit MockMvc
        mockMvc.perform(
            MockMvcRequestBuilders.get("/tenants")
                .param("page", "0")
                .param("size", "10")
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.tenants[0].tenantId").value(testTenantId.toString()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.tenants[0].name").value("test-tenant-integration"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.page").value(0))
            .andExpect(MockMvcResultMatchers.jsonPath("$.size").value(10))
            .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements").value(1))

        // Verify-Aufrufe
        verify(tenantPageService).getTenants(any(TenantPageParams::class.java))
        verify(tenantMapper).toPagedResponseDto(any())
    }

    @Test
    fun testGetTenantById() {
        // Testdurchführung mit MockMvc
        mockMvc.perform(
            MockMvcRequestBuilders.get("/tenants/$testTenantId")
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.tenantId").value(testTenantId.toString()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("test-tenant-integration"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("ACTIVE"))

        // Verify-Aufrufe
        verify(tenantService).getTenantById(eq(testTenantId))
        verify(tenantMapper).toResponseDto(eq(testTenantDto))
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
        `when`(tenantMapper.toServiceDto(any(UpdateTenantRequestDto::class.java))).thenReturn(updateTenantDto)
        `when`(tenantService.updateTenant(eq(testTenantId), any(UpdateTenantDto::class.java))).thenReturn(updatedTenantDto)
        `when`(tenantMapper.toResponseDto(eq(updatedTenantDto))).thenReturn(updatedResponseDto)

        // Testdurchführung mit MockMvc
        mockMvc.perform(
            MockMvcRequestBuilders.put("/tenants/$testTenantId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequestDto))
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.tenantId").value(testTenantId.toString()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(updatedName))
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("ACTIVE"))

        // Verify-Aufrufe
        verify(tenantMapper).toServiceDto(any(UpdateTenantRequestDto::class.java))
        verify(tenantService).updateTenant(eq(testTenantId), any(UpdateTenantDto::class.java))
        verify(tenantMapper).toResponseDto(eq(updatedTenantDto))
        verify(auditLogger).logTenantUpdate(eq(testTenantId), anyString(), anyMap())
    }

    @Test
    fun testDeleteTenant() {
        // Testdaten
        val archivedTenantDto = testTenantDto.copy(status = TenantStatus.ARCHIVED)

        // Mocks konfigurieren
        `when`(tenantService.deleteTenant(eq(testTenantId))).thenReturn(archivedTenantDto)

        // Testdurchführung mit MockMvc
        mockMvc.perform(
            MockMvcRequestBuilders.delete("/tenants/$testTenantId")
        )
            .andExpect(MockMvcResultMatchers.status().isNoContent)

        // Verify-Aufrufe
        verify(tenantService).getTenantById(eq(testTenantId))
        verify(tenantService).deleteTenant(eq(testTenantId))
        verify(auditLogger).logTenantDeletion(eq(testTenantId), eq(testTenantDto.name))
    }
}
