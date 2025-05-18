package com.acci.eaf.controlplane.api.controller

import com.acci.eaf.controlplane.api.TestConfig
import com.acci.eaf.controlplane.api.audit.AuditLogger
import com.acci.eaf.controlplane.api.dto.CreateTenantRequestDto
import com.acci.eaf.controlplane.api.dto.TenantPageParams
import com.acci.eaf.controlplane.api.dto.UpdateTenantRequestDto
import com.acci.eaf.controlplane.api.mapper.TenantMapper
import com.acci.eaf.controlplane.api.service.TenantPageService
import com.acci.eaf.multitenancy.domain.TenantStatus
import com.acci.eaf.multitenancy.dto.TenantDto
import com.acci.eaf.multitenancy.service.TenantService
import com.fasterxml.jackson.databind.ObjectMapper
import java.time.Instant
import java.util.UUID
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageImpl
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    properties = [
        "spring.main.allow-bean-definition-overriding=true",
        "axon.axonserver.enabled=false"
    ]
)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestConfig::class)
class TenantControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var tenantService: TenantService

    @MockBean
    private lateinit var tenantPageService: TenantPageService

    @MockBean
    private lateinit var tenantMapper: TenantMapper

    @MockBean
    private lateinit var auditLogger: AuditLogger

    private val testTenantId = UUID.randomUUID()
    private val testTenantName = "test-tenant"
    private val now = Instant.now()

    private lateinit var testTenantDto: TenantDto

    @BeforeEach
    fun setUp() {
        // Set up test data
        testTenantDto = TenantDto(
            tenantId = testTenantId,
            name = testTenantName,
            status = TenantStatus.ACTIVE,
            createdAt = now,
            updatedAt = now
        )

        // Configure mapper
        `when`(tenantMapper.toResponseDto(any())).thenCallRealMethod()
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `should create a tenant`() {
        // Arrange
        val createRequest = CreateTenantRequestDto(
            name = testTenantName,
            status = TenantStatus.PENDING_VERIFICATION
        )

        `when`(tenantMapper.toServiceDto(any<CreateTenantRequestDto>())).thenCallRealMethod()
        `when`(tenantService.createTenant(any())).thenReturn(testTenantDto)
        doNothing().`when`(auditLogger).logTenantCreation(any(), any())

        // Act & Assert
        mockMvc.perform(
            post("/tenants")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.tenantId").value(testTenantId.toString()))
            .andExpect(jsonPath("$.name").value(testTenantName))
            .andExpect(header().exists("Location"))

        verify(tenantService, times(1)).createTenant(any())
        verify(auditLogger, times(1)).logTenantCreation(any(), any())
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `should get a tenant by id`() {
        // Arrange
        `when`(tenantService.getTenantById(eq(testTenantId))).thenReturn(testTenantDto)

        // Act & Assert
        mockMvc.perform(get("/tenants/{tenantId}", testTenantId))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.tenantId").value(testTenantId.toString()))
            .andExpect(jsonPath("$.name").value(testTenantName))

        verify(tenantService, times(1)).getTenantById(eq(testTenantId))
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `should get paginated list of tenants`() {
        // Arrange
        val tenants = listOf(testTenantDto)
        val page = PageImpl(tenants)

        `when`(tenantPageService.getTenants(any())).thenReturn(page)
        `when`(tenantMapper.toPagedResponseDto(eq(page))).thenCallRealMethod()

        // Act & Assert
        mockMvc.perform(
            get("/tenants")
                .param("page", "0")
                .param("size", "10")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.tenants").isArray)
            .andExpect(jsonPath("$.tenants[0].tenantId").value(testTenantId.toString()))
            .andExpect(jsonPath("$.tenants[0].name").value(testTenantName))

        verify(tenantPageService, times(1)).getTenants(any<TenantPageParams>())
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `should update a tenant`() {
        // Arrange
        val updateRequest = UpdateTenantRequestDto(
            name = "updated-tenant",
            status = TenantStatus.ACTIVE
        )

        val updatedTenantDto = testTenantDto.copy(name = "updated-tenant")

        `when`(tenantMapper.toServiceDto(any<UpdateTenantRequestDto>())).thenCallRealMethod()
        `when`(tenantService.updateTenant(eq(testTenantId), any())).thenReturn(updatedTenantDto)
        doNothing().`when`(auditLogger).logTenantUpdate(any(), any(), any())

        // Act & Assert
        mockMvc.perform(
            put("/tenants/{tenantId}", testTenantId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.tenantId").value(testTenantId.toString()))
            .andExpect(jsonPath("$.name").value("updated-tenant"))

        verify(tenantService, times(1)).updateTenant(eq(testTenantId), any())
        verify(auditLogger, times(1)).logTenantUpdate(any(), any(), any())
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `should delete a tenant`() {
        // Arrange
        `when`(tenantService.getTenantById(eq(testTenantId))).thenReturn(testTenantDto)
        `when`(tenantService.deleteTenant(eq(testTenantId))).thenReturn(testTenantDto)
        doNothing().`when`(auditLogger).logTenantDeletion(any(), any())

        // Act & Assert
        mockMvc.perform(
            delete("/tenants/{tenantId}", testTenantId)
                .with(csrf())
        )
            .andExpect(status().isNoContent)

        verify(tenantService, times(1)).deleteTenant(eq(testTenantId))
        verify(auditLogger, times(1)).logTenantDeletion(any(), any())
    }

    @Test
    fun `should return 401 when unauthorized`() {
        // Act & Assert
        mockMvc.perform(get("/tenants"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    @WithMockUser(roles = ["USER"])
    fun `should return 403 when not an admin`() {
        // Act & Assert
        mockMvc.perform(get("/tenants"))
            .andExpect(status().isForbidden)
    }
}
