package com.acci.eaf.controlplane.api.controller

import com.acci.eaf.controlplane.api.dto.ServiceAccountCreateRequest
import com.acci.eaf.controlplane.api.dto.ServiceAccountStatusResponse
import com.acci.eaf.controlplane.api.dto.ServiceAccountUpdateRequest
import com.acci.eaf.iam.domain.command.CreateServiceAccountCommand
import com.fasterxml.jackson.databind.ObjectMapper
import java.time.OffsetDateTime
import java.util.UUID
import org.axonframework.commandhandling.gateway.CommandGateway
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.WebApplicationContext

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestEntityManager
@Transactional
class ServiceAccountControllerIntegrationTest {

    @Autowired
    private lateinit var webApplicationContext: WebApplicationContext

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var commandGateway: CommandGateway

    private lateinit var mockMvc: MockMvc

    // Repository removed as we're using CQRS/ES approach with CommandGateway

    private val testTenantId = UUID.randomUUID()
    private val testServiceAccountId = UUID.randomUUID()

    @Test
    fun `should create service account successfully`() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build()

        val createRequest = ServiceAccountCreateRequest(
            description = "Test Service Account",
            expiresAt = OffsetDateTime.now().plusYears(1),
            roles = setOf(UUID.randomUUID())
        )

        mockMvc.perform(
            post("/api/controlplane/tenants/$testTenantId/service-accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
        )
            .andExpect(status().isCreated)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.clientId").exists())
            .andExpect(jsonPath("$.clientSecret").exists())
    }

    @Test
    fun `should return validation error for invalid create request`() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build()

        val createRequest = ServiceAccountCreateRequest(
            description = null,
            expiresAt = null, // Use null for invalid data simulation
            roles = setOf()
        )

        val invalidRequestJson = """{
            "description": null,
            "expiresAt": "invalid-date-format",
            "roles": []
        }"""

        mockMvc.perform(
            post("/api/controlplane/tenants/$testTenantId/service-accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequestJson)
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `should list service accounts for tenant`() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build()

        // First create a service account
        createTestServiceAccount()

        mockMvc.perform(
            get("/api/controlplane/tenants/$testTenantId/service-accounts")
                .param("page", "0")
                .param("size", "10")
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content").isArray)
    }

    @Test
    fun `should get service account details by ID`() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build()

        createTestServiceAccount()

        mockMvc.perform(
            get("/api/controlplane/tenants/$testTenantId/service-accounts/$testServiceAccountId")
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.serviceAccountId").value(testServiceAccountId.toString()))
            .andExpect(jsonPath("$.clientId").exists())
            .andExpect(jsonPath("$.clientSecret").doesNotExist()) // Should not return the secret
    }

    @Test
    fun `should return 404 for non-existent service account`() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build()

        val nonExistentId = UUID.randomUUID()

        mockMvc.perform(
            get("/api/controlplane/tenants/$testTenantId/service-accounts/$nonExistentId")
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun `should update service account successfully`() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build()

        createTestServiceAccount()

        val updateRequest = ServiceAccountUpdateRequest(
            description = "Updated Description",
            status = ServiceAccountStatusResponse.INACTIVE,
            expiresAt = null,
            roles = setOf(UUID.randomUUID())
        )

        mockMvc.perform(
            put("/api/controlplane/tenants/$testTenantId/service-accounts/$testServiceAccountId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.description").value("Updated Description"))
            .andExpect(jsonPath("$.status").value("INACTIVE"))
    }

    @Test
    fun `should handle expiration date update`() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build()

        createTestServiceAccount()

        val newExpiration = OffsetDateTime.now().plusYears(2)
        val updateRequest = ServiceAccountUpdateRequest(
            description = null,
            status = null,
            expiresAt = newExpiration,
            roles = null
        )

        mockMvc.perform(
            put("/api/controlplane/tenants/$testTenantId/service-accounts/$testServiceAccountId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.expiresAt").exists())
    }

    @Test
    fun `should return validation error for invalid status`() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build()

        createTestServiceAccount()

        val invalidRequestJson = """{
            "description": null,
            "status": "INVALID_STATUS",
            "expiresAt": null
        }"""

        mockMvc.perform(
            put("/api/controlplane/tenants/$testTenantId/service-accounts/$testServiceAccountId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequestJson)
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `should delete (deactivate) service account successfully`() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build()

        createTestServiceAccount()

        mockMvc.perform(
            delete("/api/controlplane/tenants/$testTenantId/service-accounts/$testServiceAccountId")
        )
            .andExpect(status().isNoContent)

        // Verify the service account is deactivated
        // Note: Since we're using CQRS/ES, we'd need to check via the query side
        // This test would need to be adapted based on how the read model is implemented
    }

    @Test
    fun `should rotate service account secret successfully`() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build()

        createTestServiceAccount()

        mockMvc.perform(
            post("/api/controlplane/tenants/$testTenantId/service-accounts/$testServiceAccountId/rotate-secret")
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.clientSecret").exists())
    }

    @Test
    fun `should handle expiration beyond maximum allowed period`() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build()

        val createRequest = ServiceAccountCreateRequest(
            description = "Service Account with long expiration",
            expiresAt = OffsetDateTime.now().plusYears(10), // Beyond max allowed
            roles = setOf()
        )

        mockMvc.perform(
            post("/api/controlplane/tenants/$testTenantId/service-accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
        )
            .andExpect(status().isBadRequest)
    }

    // Helper method to create a test service account via command
    private fun createTestServiceAccount() {
        val command = CreateServiceAccountCommand(
            serviceAccountId = testServiceAccountId,
            tenantId = testTenantId,
            description = "Test Service Account",
            requestedExpiresAt = OffsetDateTime.now().plusYears(1),
            requestedRoles = setOf(UUID.randomUUID()),
            initiatedBy = "test-user"
        )

        commandGateway.sendAndWait<Any>(command)
    }
}
