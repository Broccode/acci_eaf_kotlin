package com.acci.eaf.controlplane.api.controller

import com.acci.eaf.controlplane.api.TestConfig
import com.acci.eaf.controlplane.api.dto.CreateTenantRequestDto
import com.acci.eaf.controlplane.api.dto.UpdateTenantRequestDto
import com.acci.eaf.multitenancy.domain.TenantStatus
import com.fasterxml.jackson.databind.ObjectMapper
import java.util.UUID
import org.axonframework.commandhandling.CommandBus
import org.axonframework.commandhandling.gateway.CommandGateway
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

/**
 * Integration tests for the TenantController.
 *
 * Note: This is a simple test skeleton and should be extended with more comprehensive tests.
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    properties = [
        "spring.main.allow-bean-definition-overriding=true",
        "axon.axonserver.enabled=false"
    ]
)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(TestConfig::class)
class TenantControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    // Mock Axon Components
    @MockBean
    private lateinit var commandBus: CommandBus

    @MockBean
    private lateinit var commandGateway: CommandGateway

    /**
     * Test creating a new tenant with valid data.
     */
    @Test
    @WithMockUser(username = "admin", roles = ["ADMIN"])
    fun testCreateTenant() {
        val requestDto = CreateTenantRequestDto(
            name = "test-tenant",
            status = TenantStatus.PENDING_VERIFICATION,
            adminEmail = "admin@test.com"
        )

        mockMvc.perform(
            post("/tenants")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
        )
            .andExpect(status().isCreated)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.name").value(requestDto.name))
            .andExpect(jsonPath("$.status").value(requestDto.status.toString()))
            .andExpect(header().exists("Location"))
    }

    /**
     * Test listing tenants with pagination.
     */
    @Test
    @WithMockUser(username = "admin", roles = ["ADMIN"])
    fun testGetTenants() {
        mockMvc.perform(
            get("/tenants")
                .param("page", "0")
                .param("size", "10")
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.page").exists())
            .andExpect(jsonPath("$.size").exists())
            .andExpect(jsonPath("$.totalElements").exists())
            .andExpect(jsonPath("$.totalPages").exists())
            .andExpect(jsonPath("$.tenants").isArray)
    }

    /**
     * Test getting a tenant by ID.
     *
     * Note: This test assumes a tenant with the given ID exists.
     * In a real test, you would first create the tenant.
     */
    @Test
    @WithMockUser(username = "admin", roles = ["ADMIN"])
    fun testGetTenantById() {
        // This is just a placeholder - real tests would use test data setup
        val tenantId = UUID.randomUUID()

        mockMvc.perform(get("/tenants/{tenantId}", tenantId))
            .andExpect(status().isNotFound) // Expected since the tenant doesn't exist
    }

    /**
     * Test updating a tenant.
     *
     * Note: This test assumes a tenant with the given ID exists.
     * In a real test, you would first create the tenant.
     */
    @Test
    @WithMockUser(username = "admin", roles = ["ADMIN"])
    fun testUpdateTenant() {
        // This is just a placeholder - real tests would use test data setup
        val tenantId = UUID.randomUUID()
        val requestDto = UpdateTenantRequestDto(
            name = "updated-tenant",
            status = TenantStatus.ACTIVE
        )

        mockMvc.perform(
            put("/tenants/{tenantId}", tenantId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
        )
            .andExpect(status().isNotFound) // Expected since the tenant doesn't exist
    }

    /**
     * Test deleting a tenant.
     *
     * Note: This test assumes a tenant with the given ID exists.
     * In a real test, you would first create the tenant.
     */
    @Test
    @WithMockUser(username = "admin", roles = ["ADMIN"])
    fun testDeleteTenant() {
        // This is just a placeholder - real tests would use test data setup
        val tenantId = UUID.randomUUID()

        mockMvc.perform(
            delete("/tenants/{tenantId}", tenantId)
                .with(csrf())
        )
            .andExpect(status().isNotFound) // Expected since the tenant doesn't exist
    }
} 
