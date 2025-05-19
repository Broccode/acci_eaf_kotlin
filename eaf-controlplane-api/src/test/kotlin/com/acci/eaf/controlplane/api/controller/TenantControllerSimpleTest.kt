package com.acci.eaf.controlplane.api.controller

import com.acci.eaf.controlplane.api.audit.AuditLogger
import com.acci.eaf.controlplane.api.dto.CreateTenantRequestDto
import com.acci.eaf.controlplane.api.dto.TenantResponseDto
import com.acci.eaf.controlplane.api.mapper.TenantMapperInterface
import com.acci.eaf.multitenancy.domain.TenantStatus
import com.acci.eaf.multitenancy.dto.CreateTenantDto
import com.acci.eaf.multitenancy.dto.TenantDto
import com.acci.eaf.multitenancy.service.TenantService
import com.fasterxml.jackson.databind.ObjectMapper
import java.time.Instant
import java.util.UUID
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

/**
 * Ein vereinfachter Test zum Prüfen des TenantController.createTenant Endpunkts
 *
 * HINWEIS: Dieser Test ist deaktiviert, da er Probleme mit JPA und Persistence-Abhängigkeiten hat.
 * Als Alternative kann der TenantControllerTest verwendet werden, der ohne Spring-Kontext
 * arbeitet und eine bessere Stabilität bietet. Siehe README.md in diesem Verzeichnis.
 */
@Disabled("Dieser Test ist deaktiviert aufgrund von Problemen mit JPA und Persistence-Abhängigkeiten. Verwende stattdessen TenantControllerTest.")
@WebMvcTest(TenantController::class)
@Import(WebMvcTestConfig::class)
class TenantControllerSimpleTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var tenantService: TenantService

    @MockBean
    private lateinit var tenantMapperInterface: TenantMapperInterface

    @MockBean
    private lateinit var auditLogger: AuditLogger

    @MockBean
    private lateinit var tenantPageService: com.acci.eaf.controlplane.api.service.TenantPageService

    private val testTenantId = UUID.randomUUID()
    private val now = Instant.now()

    @BeforeEach
    fun setUp() {
        // Setze einen Mock-Request für ServletUriComponentsBuilder
        val mockRequest = MockHttpServletRequest()
        mockRequest.scheme = "http"
        mockRequest.serverName = "localhost"
        mockRequest.serverPort = 8080
        mockRequest.contextPath = ""
        RequestContextHolder.setRequestAttributes(ServletRequestAttributes(mockRequest))

        // Bereite die Testdaten vor
        val createTenantRequestDto = CreateTenantRequestDto(
            name = "test-tenant",
            status = TenantStatus.PENDING_VERIFICATION,
            adminEmail = "admin@test.com"
        )

        val createTenantDto = CreateTenantDto(
            name = "test-tenant",
            status = TenantStatus.PENDING_VERIFICATION
        )

        val tenantDto = TenantDto(
            tenantId = testTenantId,
            name = "test-tenant",
            status = TenantStatus.ACTIVE,
            createdAt = now,
            updatedAt = now
        )

        val tenantResponseDto = TenantResponseDto(
            tenantId = testTenantId,
            name = "test-tenant",
            status = TenantStatus.ACTIVE,
            createdAt = now,
            updatedAt = now
        )

        // Mock tenantMapper
        `when`(tenantMapperInterface.toServiceDto(Mockito.any(CreateTenantRequestDto::class.java)))
            .thenReturn(createTenantDto)
        `when`(tenantMapperInterface.toResponseDto(Mockito.any(TenantDto::class.java)))
            .thenReturn(tenantResponseDto)

        // Mock tenantService
        `when`(tenantService.createTenant(Mockito.any(CreateTenantDto::class.java)))
            .thenReturn(tenantDto)
    }

    @Test
    @WithMockUser(username = "admin", roles = ["ADMIN"])
    fun testCreateTenant() {
        val requestDto = CreateTenantRequestDto(
            name = "test-tenant",
            status = TenantStatus.PENDING_VERIFICATION,
            adminEmail = "admin@test.com"
        )

        val result = mockMvc.perform(
            post("/tenants")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isCreated)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.name").value("test-tenant"))
            .andExpect(jsonPath("$.status").value(TenantStatus.ACTIVE.toString()))

        // Überprüfe, ob der Location-Header korrekt gesetzt wurde
        result.andExpect(header().string("Location", "http://localhost/tenants/$testTenantId"))
    }
}
