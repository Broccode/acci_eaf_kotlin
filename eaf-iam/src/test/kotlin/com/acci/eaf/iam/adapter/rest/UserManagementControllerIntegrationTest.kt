package com.acci.eaf.iam.adapter.rest

import com.acci.eaf.iam.adapter.rest.dto.CreateUserRequest
import com.acci.eaf.iam.adapter.rest.dto.SetPasswordRequest
import com.acci.eaf.iam.adapter.rest.dto.UpdateUserRequest
import com.acci.eaf.iam.adapter.rest.dto.UpdateUserStatusRequest
import com.acci.eaf.iam.application.port.input.UserDto
import com.acci.eaf.iam.application.port.input.UserService
import com.acci.eaf.iam.domain.model.UserStatus
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.mockk.every
import io.mockk.mockk
import java.time.Instant
import java.util.UUID
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class UserManagementControllerIntegrationTest {

    private lateinit var mockMvc: MockMvc
    private lateinit var userService: UserService
    private lateinit var objectMapper: ObjectMapper

    private val tenantId = UUID.randomUUID()
    private val userId = UUID.randomUUID()
    private val now = Instant.now()

    private fun createObjectMapper(): ObjectMapper =
        ObjectMapper()
            .registerModule(KotlinModule.Builder().build())
            .registerModule(JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)

    @BeforeEach
    fun setup() {
        userService = mockk(relaxed = true)
        objectMapper = createObjectMapper()

        val jacksonConverter = MappingJackson2HttpMessageConverter(objectMapper)

        val controller = UserManagementController(userService)
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setMessageConverters(jacksonConverter)
            .setCustomArgumentResolvers(PageableHandlerMethodArgumentResolver())
            .build()
    }

    @Test
    fun `should create a user successfully`() {
        // Vorbereitung
        val createUserRequest = CreateUserRequest(
            username = "testuser",
            email = "test@example.com",
            password = "Password123!",
            firstName = "Test",
            lastName = "User"
        )

        val userDto = UserDto(
            id = userId,
            tenantId = tenantId,
            username = "testuser",
            email = "test@example.com",
            firstName = "Test",
            lastName = "User",
            displayName = "Test User",
            status = UserStatus.ACTIVE,
            createdAt = now,
            updatedAt = now
        )

        every { userService.createLocalUser(any()) } returns userDto

        // Ausführung & Überprüfung
        mockMvc.perform(
            post("/api/controlplane/tenants/$tenantId/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createUserRequest))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(userId.toString()))
            .andExpect(jsonPath("$.username").value("testuser"))
            .andExpect(jsonPath("$.email").value("test@example.com"))
            .andExpect(jsonPath("$.firstName").value("Test"))
            .andExpect(jsonPath("$.lastName").value("User"))
            .andExpect(jsonPath("$.displayName").value("Test User"))
            .andExpect(jsonPath("$.status").value("ACTIVE"))
    }

    @Test
    fun `should get a list of users`() {
        // Vorbereitung
        val user1 = UserDto(
            id = UUID.randomUUID(),
            tenantId = tenantId,
            username = "user1",
            email = "user1@example.com",
            firstName = "User",
            lastName = "One",
            displayName = "User One",
            status = UserStatus.ACTIVE,
            createdAt = now,
            updatedAt = now
        )

        val user2 = UserDto(
            id = UUID.randomUUID(),
            tenantId = tenantId,
            username = "user2",
            email = "user2@example.com",
            firstName = "User",
            lastName = "Two",
            displayName = "User Two",
            status = UserStatus.ACTIVE,
            createdAt = now,
            updatedAt = now
        )

        val pageable = PageRequest.of(0, 20)
        val userPage = PageImpl(listOf(user1, user2), pageable, 2)

        // Bereite die gemockte Antwort vor
        every {
            userService.findUsersByTenant(eq(tenantId), any<Pageable>())
        } returns userPage

        // Ausführung & Überprüfung
        mockMvc.perform(
            get("/api/controlplane/tenants/$tenantId/users")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.content[0].username").value("user1"))
            .andExpect(jsonPath("$.content[1].username").value("user2"))
    }

    @Test
    fun `should get a specific user`() {
        // Vorbereitung
        val userDto = UserDto(
            id = userId,
            tenantId = tenantId,
            username = "testuser",
            email = "test@example.com",
            firstName = "Test",
            lastName = "User",
            displayName = "Test User",
            status = UserStatus.ACTIVE,
            createdAt = now,
            updatedAt = now
        )

        every { userService.getUserById(userId) } returns userDto

        // Ausführung & Überprüfung
        mockMvc.perform(
            get("/api/controlplane/tenants/$tenantId/users/$userId")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(userId.toString()))
            .andExpect(jsonPath("$.username").value("testuser"))
    }

    @Test
    fun `should return 404 when accessing user from another tenant`() {
        // Vorbereitung
        val otherTenantId = UUID.randomUUID()
        val userDto = UserDto(
            id = userId,
            tenantId = otherTenantId, // Benutzer gehört zu einem anderen Tenant
            username = "testuser",
            email = "test@example.com",
            firstName = "Test",
            lastName = "User",
            displayName = "Test User",
            status = UserStatus.ACTIVE,
            createdAt = now,
            updatedAt = now
        )

        every { userService.getUserById(userId) } returns userDto

        // Ausführung & Überprüfung
        mockMvc.perform(
            get("/api/controlplane/tenants/$tenantId/users/$userId")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun `should update a user successfully`() {
        // Vorbereitung
        val updateUserRequest = UpdateUserRequest(
            email = "updated@example.com",
            firstName = "Updated",
            lastName = "Name"
        )

        val updatedUserDto = UserDto(
            id = userId,
            tenantId = tenantId,
            username = "testuser",
            email = "updated@example.com",
            firstName = "Updated",
            lastName = "Name",
            displayName = "Updated Name",
            status = UserStatus.ACTIVE,
            createdAt = now,
            updatedAt = now
        )

        every { userService.updateUser(any()) } returns updatedUserDto

        // Ausführung & Überprüfung
        mockMvc.perform(
            put("/api/controlplane/tenants/$tenantId/users/$userId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateUserRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.email").value("updated@example.com"))
            .andExpect(jsonPath("$.firstName").value("Updated"))
            .andExpect(jsonPath("$.lastName").value("Name"))
    }

    @Test
    fun `should set a password successfully`() {
        // Vorbereitung
        val setPasswordRequest = SetPasswordRequest(
            newPassword = "NewPassword123!"
        )

        every { userService.setPassword(any()) } returns Unit

        // Ausführung & Überprüfung
        mockMvc.perform(
            post("/api/controlplane/tenants/$tenantId/users/$userId/set-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(setPasswordRequest))
        )
            .andExpect(status().isNoContent)
    }

    @Test
    fun `should update user status successfully`() {
        // Vorbereitung
        val updateStatusRequest = UpdateUserStatusRequest(
            status = UserStatus.LOCKED_BY_ADMIN
        )

        val updatedUserDto = UserDto(
            id = userId,
            tenantId = tenantId,
            username = "testuser",
            email = "test@example.com",
            firstName = "Test",
            lastName = "User",
            displayName = "Test User",
            status = UserStatus.LOCKED_BY_ADMIN,
            createdAt = now,
            updatedAt = now
        )

        every { userService.updateUserStatus(userId, tenantId, UserStatus.LOCKED_BY_ADMIN) } returns updatedUserDto

        // Ausführung & Überprüfung
        mockMvc.perform(
            put("/api/controlplane/tenants/$tenantId/users/$userId/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateStatusRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("LOCKED_BY_ADMIN"))
    }

    @Test
    fun `should filter users by status`() {
        // Vorbereitung
        val user = UserDto(
            id = UUID.randomUUID(),
            tenantId = tenantId,
            username = "lockeduser",
            email = "locked@example.com",
            firstName = "Locked",
            lastName = "User",
            displayName = "Locked User",
            status = UserStatus.LOCKED_BY_ADMIN,
            createdAt = now,
            updatedAt = now
        )

        val pageable = PageRequest.of(0, 20)
        val userPage = PageImpl(listOf(user), pageable, 1)

        every {
            userService.findUsersByStatus(eq(tenantId), eq(UserStatus.LOCKED_BY_ADMIN), any())
        } returns userPage

        // Ausführung & Überprüfung
        mockMvc.perform(
            get("/api/controlplane/tenants/$tenantId/users?status=LOCKED_BY_ADMIN")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].status").value("LOCKED_BY_ADMIN"))
    }

    @Test
    fun `should search users by username`() {
        // Vorbereitung
        val user = UserDto(
            id = UUID.randomUUID(),
            tenantId = tenantId,
            username = "searchuser",
            email = "search@example.com",
            firstName = "Search",
            lastName = "User",
            displayName = "Search User",
            status = UserStatus.ACTIVE,
            createdAt = now,
            updatedAt = now
        )

        val pageable = PageRequest.of(0, 20)
        val userPage = PageImpl(listOf(user), pageable, 1)

        every {
            userService.searchUsers(eq(tenantId), eq("search"), any())
        } returns userPage

        // Ausführung & Überprüfung
        mockMvc.perform(
            get("/api/controlplane/tenants/$tenantId/users?username=search")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].username").value("searchuser"))
    }
}
