package com.acci.eaf.iam.adapter.rest

import com.acci.eaf.iam.application.port.api.PermissionDto
import com.acci.eaf.iam.application.port.api.PermissionService
import com.fasterxml.jackson.databind.ObjectMapper
import java.util.UUID
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.eq
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(PermissionController::class)
class PermissionControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var permissionService: PermissionService

    private val baseUrl = "/api/controlplane/permissions"

    @Test
    @WithMockUser(authorities = ["permission:read"])
    fun `getAllPermissions should return all permissions`() {
        // Given
        val permissions = listOf(
            PermissionDto(
                UUID.randomUUID(),
                "user:create",
                "Create user permission"
            ),
            PermissionDto(
                UUID.randomUUID(),
                "user:read",
                "Read user permission"
            )
        )

        val page = PageImpl(permissions)

        org.mockito.Mockito.`when`(permissionService.getAllPermissions(any(Pageable::class.java))).thenReturn(page)

        // When/Then
        mockMvc.perform(
            get(baseUrl)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.content[0].name").value("user:create"))
            .andExpect(jsonPath("$.content[1].name").value("user:read"))
    }

    @Test
    @WithMockUser(authorities = ["permission:read"])
    fun `getPermissionById should return permission when it exists`() {
        // Given
        val permissionId = UUID.randomUUID()
        val permission = PermissionDto(
            permissionId,
            "user:create",
            "Create user permission"
        )

        org.mockito.Mockito.`when`(permissionService.getPermissionById(permissionId)).thenReturn(permission)

        // When/Then
        mockMvc.perform(
            get("$baseUrl/$permissionId")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(permissionId.toString()))
            .andExpect(jsonPath("$.name").value("user:create"))
            .andExpect(jsonPath("$.description").value("Create user permission"))
    }

    @Test
    @WithMockUser(authorities = ["permission:read"])
    fun `searchPermissions should return permissions matching the search term`() {
        // Given
        val searchTerm = "user"
        val permissions = listOf(
            PermissionDto(
                UUID.randomUUID(),
                "user:create",
                "Create user permission"
            ),
            PermissionDto(
                UUID.randomUUID(),
                "user:read",
                "Read user permission"
            )
        )

        val page = PageImpl(permissions)

        org.mockito.Mockito.`when`(permissionService.searchPermissionsByName(eq(searchTerm), any(Pageable::class.java))).thenReturn(page)

        // When/Then
        mockMvc.perform(
            get("$baseUrl/search")
                .param("namePart", searchTerm)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.content[0].name").value("user:create"))
            .andExpect(jsonPath("$.content[1].name").value("user:read"))
    }

    @Test
    fun `getAllPermissions should return 401 without authentication`() {
        // When/Then
        mockMvc.perform(
            get(baseUrl)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    @WithMockUser(authorities = ["other:permission"])
    fun `getAllPermissions should return 403 with insufficient permissions`() {
        // When/Then
        mockMvc.perform(
            get(baseUrl)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isForbidden)
    }
}
