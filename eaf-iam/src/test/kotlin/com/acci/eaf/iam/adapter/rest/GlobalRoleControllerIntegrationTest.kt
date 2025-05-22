package com.acci.eaf.iam.adapter.rest

import com.acci.eaf.iam.adapter.rest.dto.RoleRequest
import com.acci.eaf.iam.application.port.api.PermissionDto
import com.acci.eaf.iam.application.port.api.PermissionService
import com.acci.eaf.iam.application.port.api.RoleDto
import com.acci.eaf.iam.application.port.api.RoleService
import com.acci.eaf.iam.domain.exception.RoleAlreadyExistsException
import com.acci.eaf.iam.domain.exception.RoleNotFoundException
import com.fasterxml.jackson.databind.ObjectMapper
import java.util.UUID
import org.junit.jupiter.api.Test
import org.mockito.Mockito.any
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(GlobalRoleController::class)
class GlobalRoleControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var roleService: RoleService

    @MockBean
    private lateinit var permissionService: PermissionService

    private val baseUrl = "/api/controlplane/roles"

    @Test
    @WithMockUser(authorities = ["role:read"])
    fun `getAllGlobalRoles should return all global roles`() {
        // Given
        val roles = listOf(
            RoleDto(
                roleId = UUID.randomUUID(),
                name = "System Admin",
                description = "System administrator role",
                tenantId = null,
                permissions = emptyList()
            ),
            RoleDto(
                roleId = UUID.randomUUID(),
                name = "Tenant Admin",
                description = "Tenant administrator role",
                tenantId = null,
                permissions = emptyList()
            )
        )

        val page = PageImpl(roles)

        `when`(roleService.getGlobalRoles(any(Pageable::class.java))).thenReturn(page)

        // When/Then
        mockMvc.perform(
            get(baseUrl)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.content[0].name").value("System Admin"))
            .andExpect(jsonPath("$.content[1].name").value("Tenant Admin"))
    }

    @Test
    @WithMockUser(authorities = ["role:read"])
    fun `getGlobalRoleById should return role when it exists`() {
        // Given
        val roleId = UUID.randomUUID()
        val roleDto = RoleDto(
            roleId = roleId,
            name = "System Admin",
            description = "System administrator role",
            tenantId = null,
            permissions = emptyList()
        )

        `when`(roleService.getRoleById(roleId)).thenReturn(roleDto)

        // When/Then
        mockMvc.perform(
            get("$baseUrl/$roleId")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.roleId").value(roleId.toString()))
            .andExpect(jsonPath("$.name").value("System Admin"))
            .andExpect(jsonPath("$.description").value("System administrator role"))
            .andExpect(jsonPath("$.tenantId").isEmpty)
    }

    @Test
    @WithMockUser(authorities = ["role:read"])
    fun `getGlobalRoleById should return 404 when role does not exist`() {
        // Given
        val roleId = UUID.randomUUID()

        `when`(roleService.getRoleById(roleId)).thenThrow(RoleNotFoundException(roleId))

        // When/Then
        mockMvc.perform(
            get("$baseUrl/$roleId")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isNotFound)
    }

    @Test
    @WithMockUser(authorities = ["role:create"])
    fun `createGlobalRole should create and return a global role`() {
        // Given
        val roleRequest = RoleRequest(
            name = "New Global Role",
            description = "New global role description"
        )

        val createdRoleDto = RoleDto(
            roleId = UUID.randomUUID(),
            name = roleRequest.name,
            description = roleRequest.description,
            tenantId = null,
            permissions = emptyList()
        )

        `when`(roleService.createRole(any())).thenReturn(createdRoleDto)

        // When/Then
        mockMvc.perform(
            post(baseUrl)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roleRequest))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.name").value(roleRequest.name))
            .andExpect(jsonPath("$.description").value(roleRequest.description))
            .andExpect(jsonPath("$.tenantId").isEmpty)
    }

    @Test
    @WithMockUser(authorities = ["role:create"])
    fun `createGlobalRole should return 409 when role with same name already exists`() {
        // Given
        val roleRequest = RoleRequest(
            name = "Existing Role",
            description = "Role with existing name"
        )

        `when`(roleService.createRole(any()))
            .thenThrow(RoleAlreadyExistsException("Existing Role", null))

        // When/Then
        mockMvc.perform(
            post(baseUrl)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roleRequest))
        )
            .andExpect(status().isConflict)
    }

    @Test
    @WithMockUser(authorities = ["role:update"])
    fun `updateGlobalRole should update and return the global role`() {
        // Given
        val roleId = UUID.randomUUID()
        val roleRequest = RoleRequest(
            name = "Updated Role Name",
            description = "Updated role description"
        )

        val updatedRoleDto = RoleDto(
            roleId = roleId,
            name = roleRequest.name,
            description = roleRequest.description,
            tenantId = null,
            permissions = emptyList()
        )

        `when`(roleService.updateRole(any())).thenReturn(updatedRoleDto)

        // When/Then
        mockMvc.perform(
            put("$baseUrl/$roleId")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roleRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.roleId").value(roleId.toString()))
            .andExpect(jsonPath("$.name").value(roleRequest.name))
            .andExpect(jsonPath("$.description").value(roleRequest.description))
    }

    @Test
    @WithMockUser(authorities = ["role:delete"])
    fun `deleteGlobalRole should delete the role and return 204`() {
        // Given
        val roleId = UUID.randomUUID()

        doNothing().`when`(roleService).deleteRole(roleId)

        // When/Then
        mockMvc.perform(
            delete("$baseUrl/$roleId")
                .with(csrf())
        )
            .andExpect(status().isNoContent)
    }

    @Test
    @WithMockUser(authorities = ["role:update"])
    fun `addPermissionToRole should add permission to role successfully`() {
        // Given
        val roleId = UUID.randomUUID()
        val permissionId = UUID.randomUUID()

        val permissionDto = com.acci.eaf.iam.application.port.api.PermissionDto(
            permissionId = permissionId,
            name = "user:create",
            description = "Create user permission"
        )

        val updatedRoleDto = RoleDto(
            roleId = roleId,
            name = "System Admin",
            description = "System administrator role",
            tenantId = null,
            permissions = listOf(permissionDto)
        )

        `when`(roleService.addPermissionToRole(roleId, permissionId)).thenReturn(updatedRoleDto)
        // We don't mock permissionService.getPermissionById here as the controller doesn't call it directly for this operation.
        // The RoleService handles fetching the permission.

        // When/Then
        mockMvc.perform(
            post("$baseUrl/$roleId/permissions/$permissionId")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.roleId").value(roleId.toString()))
            .andExpect(jsonPath("$.name").value("System Admin"))
            .andExpect(jsonPath("$.permissions").isArray)
            .andExpect(jsonPath("$.permissions[0].permissionId").value(permissionId.toString()))
    }

    @Test
    @WithMockUser(authorities = ["role:update"])
    fun `removePermissionFromRole should remove permission from role successfully`() {
        // Given
        val roleId = UUID.randomUUID()
        val permissionId = UUID.randomUUID()

        val updatedRoleDto = RoleDto(
            roleId = roleId,
            name = "System Admin",
            description = "System administrator role",
            tenantId = null,
            permissions = emptyList() // Expect empty list after removal
        )

        `when`(roleService.removePermissionFromRole(roleId, permissionId)).thenReturn(updatedRoleDto)

        // When/Then
        mockMvc.perform(
            delete("$baseUrl/$roleId/permissions/$permissionId")
                .with(csrf())
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.roleId").value(roleId.toString()))
            .andExpect(jsonPath("$.name").value("System Admin"))
            .andExpect(jsonPath("$.permissions").isArray)
            .andExpect(jsonPath("$.permissions").isEmpty)
    }

    @Test
    @WithMockUser(authorities = ["role:read"])
    fun `getPermissionsForRole should return all permissions for a role`() {
        // Given
        val roleId = UUID.randomUUID()
        val permission1 = com.acci.eaf.iam.application.port.api.PermissionDto(
            permissionId = UUID.randomUUID(),
            name = "user:create",
            description = "Create user permission"
        )
        val permission2 = com.acci.eaf.iam.application.port.api.PermissionDto(
            permissionId = UUID.randomUUID(),
            name = "user:read",
            description = "Read user permission"
        )

        val permissionsList = listOf(permission1, permission2)

        `when`(permissionService.getPermissionsByRole(roleId)).thenReturn(permissionsList)

        // When/Then
        mockMvc.perform(
            get("$baseUrl/$roleId/permissions")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].name").value("user:create"))
            .andExpect(jsonPath("$[1].name").value("user:read"))
    }

    @Test
    fun `endpoints should return 401 without authentication`() {
        // When/Then - GetAllRoles
        mockMvc.perform(get(baseUrl).with(csrf()))
            .andExpect(status().isUnauthorized)

        // When/Then - CreateRole
        mockMvc.perform(
            post(baseUrl)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Test Role\",\"description\":\"Test\"}")
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    @WithMockUser(authorities = ["other:permission"])
    fun `endpoints should return 403 with insufficient permissions`() {
        // When/Then - GetAllRoles (requires role:read)
        mockMvc.perform(get(baseUrl).with(csrf()))
            .andExpect(status().isForbidden)

        // When/Then - CreateRole (requires role:create)
        mockMvc.perform(
            post(baseUrl)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Test Role\",\"description\":\"Test\"}")
        )
            .andExpect(status().isForbidden)
    }
}
