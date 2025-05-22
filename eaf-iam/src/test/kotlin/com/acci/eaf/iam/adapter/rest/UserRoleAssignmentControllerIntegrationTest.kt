package com.acci.eaf.iam.adapter.rest

import com.acci.eaf.iam.application.port.api.RoleDto
import com.acci.eaf.iam.application.port.api.RoleService
import com.acci.eaf.iam.domain.exception.RoleNotFoundException
import com.acci.eaf.iam.domain.exception.UserNotFoundException
import com.fasterxml.jackson.databind.ObjectMapper
import java.util.UUID
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(UserRoleAssignmentController::class)
class UserRoleAssignmentControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var roleService: RoleService

    private val tenantId = UUID.randomUUID()
    private val userId = UUID.randomUUID()
    private val roleId1 = UUID.randomUUID()
    private val roleId2 = UUID.randomUUID()

    private val baseUrl = "/api/controlplane/tenants/$tenantId/users/$userId/roles"

    @Test
    @WithMockUser(authorities = ["role:read"], username = "testuser")
    fun `getUserRoles should return all roles assigned to user`() {
        // Given
        val roles = listOf(
            RoleDto(
                roleId = roleId1,
                name = "Tenant Admin",
                description = "Tenant administrator role",
                tenantId = tenantId,
                permissions = emptyList()
            ),
            RoleDto(
                roleId = roleId2,
                name = "Content Manager",
                description = "Content management role",
                tenantId = tenantId,
                permissions = emptyList()
            )
        )

        `when`(roleService.getRolesByUser(userId.toString(), tenantId)).thenReturn(roles)

        // When/Then
        mockMvc.perform(
            get(baseUrl)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].roleId").value(roleId1.toString()))
            .andExpect(jsonPath("$[0].name").value("Tenant Admin"))
            .andExpect(jsonPath("$[1].roleId").value(roleId2.toString()))
            .andExpect(jsonPath("$[1].name").value("Content Manager"))
    }

    @Test
    @WithMockUser(authorities = ["role:read"], username = "testuser")
    fun `getUserRoles should return 404 when user not found by service`() {
        // Given
        `when`(roleService.getRolesByUser(userId.toString(), tenantId))
            .thenThrow(UserNotFoundException(userId.toString()))

        // When/Then
        mockMvc.perform(
            get(baseUrl)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isNotFound)
    }

    @Test
    @WithMockUser(authorities = ["role:assign"], username = "testuser")
    fun `assignRoleToUser should assign role to user and return 204`() {
        // Given
        doNothing().`when`(roleService).assignRoleToUser(userId.toString(), roleId1, tenantId)

        // When/Then
        mockMvc.perform(
            post("$baseUrl/$roleId1")
                .with(csrf())
        )
            .andExpect(status().isNoContent)
    }

    @Test
    @WithMockUser(authorities = ["role:assign"], username = "testuser")
    fun `assignRoleToUser should return 404 when role not found by service`() {
        // Given
        doThrow(RoleNotFoundException(roleId1))
            .`when`(roleService).assignRoleToUser(userId.toString(), roleId1, tenantId)

        // When/Then
        mockMvc.perform(
            post("$baseUrl/$roleId1")
                .with(csrf())
        )
            .andExpect(status().isNotFound)
    }

    @Test
    @WithMockUser(authorities = ["role:assign"], username = "testuser")
    fun `assignRoleToUser should return 404 when user not found by service`() {
        // Given
        doThrow(UserNotFoundException(userId.toString()))
            .`when`(roleService).assignRoleToUser(userId.toString(), roleId1, tenantId)

        // When/Then
        mockMvc.perform(
            post("$baseUrl/$roleId1")
                .with(csrf())
        )
            .andExpect(status().isNotFound)
    }

    @Test
    @WithMockUser(authorities = ["role:assign"], username = "testuser")
    fun `revokeRoleFromUser should revoke role from user and return 204`() {
        // Given
        doNothing().`when`(roleService).removeRoleFromUser(userId.toString(), roleId1, tenantId)

        // When/Then
        mockMvc.perform(
            delete("$baseUrl/$roleId1")
                .with(csrf())
        )
            .andExpect(status().isNoContent)
    }

    @Test
    @WithMockUser(authorities = ["role:assign"], username = "testuser")
    fun `revokeRoleFromUser should return 404 when role or user not found by service`() {
        // Given
        doThrow(RoleNotFoundException(roleId1))
            .`when`(roleService).removeRoleFromUser(userId.toString(), roleId1, tenantId)

        // When/Then
        mockMvc.perform(
            delete("$baseUrl/$roleId1")
                .with(csrf())
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun `endpoints should return 401 without authentication`() {
        // When/Then - GetUserRoles
        mockMvc.perform(get(baseUrl).with(csrf()))
            .andExpect(status().isUnauthorized)

        // When/Then - AssignRoleToUser
        mockMvc.perform(post("$baseUrl/$roleId1").with(csrf()))
            .andExpect(status().isUnauthorized)

        // When/Then - RevokeRoleFromUser
        mockMvc.perform(delete("$baseUrl/$roleId1").with(csrf()))
            .andExpect(status().isUnauthorized)
    }

    @Test
    @WithMockUser(authorities = ["other:permission"], username = "testuser")
    fun `endpoints should return 403 with insufficient permissions`() {
        // When/Then - GetUserRoles (requires role:read)
        mockMvc.perform(get(baseUrl).with(csrf()))
            .andExpect(status().isForbidden)

        // When/Then - AssignRoleToUser (requires role:assign)
        mockMvc.perform(post("$baseUrl/$roleId1").with(csrf()))
            .andExpect(status().isForbidden)

        // When/Then - RevokeRoleFromUser (requires role:assign)
        mockMvc.perform(delete("$baseUrl/$roleId1").with(csrf()))
            .andExpect(status().isForbidden)
    }
}
