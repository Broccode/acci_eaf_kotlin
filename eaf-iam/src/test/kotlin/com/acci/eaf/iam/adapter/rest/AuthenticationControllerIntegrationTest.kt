package com.acci.eaf.iam.adapter.rest

import com.acci.eaf.iam.adapter.persistence.UserRepository
import com.acci.eaf.iam.adapter.rest.dto.LoginRequestDTO
import com.acci.eaf.iam.adapter.rest.dto.LoginResponseDTO
import com.acci.eaf.iam.application.service.AccountLockoutService
import com.acci.eaf.iam.config.IamTestApplication
import com.acci.eaf.iam.config.JwtTokenProvider
import com.acci.eaf.iam.domain.model.User
import com.acci.eaf.iam.domain.model.UserStatus
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

@SpringBootTest(
    classes = [IamTestApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthenticationControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var jwtTokenProvider: JwtTokenProvider

    @Autowired
    private lateinit var accountLockoutService: AccountLockoutService

    @Value("\${app.security.lockout.max-attempts}")
    private lateinit var maxFailedAttemptsStr: String

    private val testTenantId = UUID.randomUUID()
    private val testUsername = "testuser"
    private val testPassword = "Password123!"
    private val testEmail = "testuser@example.com"

    @BeforeEach
    fun setup() {
        // Erstelle einen Testbenutzer
        val user = User(
            tenantId = testTenantId,
            username = testUsername,
            email = testEmail,
            passwordHash = passwordEncoder.encode(testPassword),
            status = UserStatus.ACTIVE
        )
        userRepository.save(user)
    }

    @AfterEach
    fun cleanup() {
        // Lösche alle Testbenutzer
        userRepository.deleteAll()
    }

    @Test
    fun `when logging in with valid credentials, then return 200 and tokens`() {
        // Given
        val loginRequest = LoginRequestDTO(
            usernameOrEmail = testUsername,
            password = testPassword,
            tenantHint = testTenantId.toString()
        )

        // When/Then
        val result = mockMvc.perform(
            post("/api/iam/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").isString)
            .andExpect(jsonPath("$.refreshToken").isString)
            .andExpect(jsonPath("$.tokenType").value("Bearer"))
            .andExpect(jsonPath("$.expiresIn").isNumber)
            .andReturn()

        // Validiere, dass der Token gültig ist und die richtigen Claims enthält
        val response = objectMapper.readValue(
            result.response.contentAsString,
            LoginResponseDTO::class.java
        )

        assert(jwtTokenProvider.validateToken(response.accessToken))
        assert(jwtTokenProvider.getUsernameFromToken(response.accessToken) == testUsername)
        assert(jwtTokenProvider.getTenantIdFromToken(response.accessToken) == testTenantId)
    }

    @Test
    fun `when logging in with invalid password, then return 401`() {
        // Given
        val loginRequest = LoginRequestDTO(
            usernameOrEmail = testUsername,
            password = "WrongPassword123!",
            tenantHint = testTenantId.toString()
        )

        // When/Then
        mockMvc.perform(
            post("/api/iam/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andDo(print())
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `when logging in with non-existent user, then return 401`() {
        // Given
        val loginRequest = LoginRequestDTO(
            usernameOrEmail = "nonexistentuser",
            password = testPassword,
            tenantHint = testTenantId.toString()
        )

        // When/Then
        mockMvc.perform(
            post("/api/iam/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andDo(print())
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `when logging in with invalid tenant, then return 401`() {
        // Given
        val loginRequest = LoginRequestDTO(
            usernameOrEmail = testUsername,
            password = testPassword,
            tenantHint = UUID.randomUUID().toString()
        )

        // When/Then
        mockMvc.perform(
            post("/api/iam/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andDo(print())
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `when logging in with inactive user, then return 401`() {
        // Given
        val inactiveUser = User(
            tenantId = testTenantId,
            username = "inactiveuser",
            email = "inactive@example.com",
            passwordHash = passwordEncoder.encode(testPassword),
            status = UserStatus.DISABLED_BY_ADMIN
        )
        userRepository.save(inactiveUser)

        val loginRequest = LoginRequestDTO(
            usernameOrEmail = "inactiveuser",
            password = testPassword,
            tenantHint = testTenantId.toString()
        )

        // When/Then
        mockMvc.perform(
            post("/api/iam/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andDo(print())
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `when too many failed logins, account should be locked`() {
        // Given
        val loginRequest = LoginRequestDTO(
            usernameOrEmail = testUsername,
            password = "WrongPassword123!",
            tenantHint = testTenantId.toString()
        )

        // In der Testkonfiguration auf 3 gesetzt
        val maxAttempts = maxFailedAttemptsStr.toInt()

        // When/Then - Mehrere fehlgeschlagene Anmeldeversuche
        for (i in 1..maxAttempts) {
            mockMvc.perform(
                post("/api/iam/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest))
            )
                .andExpect(status().isUnauthorized)
        }

        // Prüfen, ob das Konto gesperrt ist
        val user = userRepository.findByUsernameAndTenantId(testUsername, testTenantId).get()
        assert(user.status == UserStatus.LOCKED_BY_SYSTEM)

        // Versuche, mit den richtigen Anmeldedaten anzumelden - sollte auch fehlschlagen
        val correctLoginRequest = LoginRequestDTO(
            usernameOrEmail = testUsername,
            password = testPassword,
            tenantHint = testTenantId.toString()
        )

        mockMvc.perform(
            post("/api/iam/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(correctLoginRequest))
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `when logging in with email instead of username, then succeed`() {
        // Given
        val loginRequest = LoginRequestDTO(
            usernameOrEmail = testEmail,
            password = testPassword,
            tenantHint = testTenantId.toString()
        )

        // When/Then
        mockMvc.perform(
            post("/api/iam/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").isString)
    }

    @Test
    fun `when logging in with username@tenant format, then succeed`() {
        // Given
        val loginRequest = LoginRequestDTO(
            usernameOrEmail = "$testUsername@${testTenantId}",
            password = testPassword
        )

        // When/Then
        mockMvc.perform(
            post("/api/iam/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").isString)
    }

    @Test
    fun `when refreshing with valid token, then return 200 and new tokens`() {
        // Given
        val loginRequest = LoginRequestDTO(
            usernameOrEmail = testUsername,
            password = testPassword,
            tenantHint = testTenantId.toString()
        )

        // Zuerst einloggen, um einen gültigen Refresh-Token zu erhalten
        val loginResult = mockMvc.perform(
            post("/api/iam/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andExpect(status().isOk)
            .andReturn()

        val loginResponse = objectMapper.readValue(
            loginResult.response.contentAsString,
            LoginResponseDTO::class.java
        )

        // When/Then - Token aktualisieren mit dem Refresh-Token
        mockMvc.perform(
            post("/api/iam/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\": \"${loginResponse.refreshToken}\"}")
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").isString)
            .andExpect(jsonPath("$.refreshToken").isString)
            .andExpect(jsonPath("$.tokenType").value("Bearer"))
            .andExpect(jsonPath("$.expiresIn").isNumber)
    }

    @Test
    fun `when refreshing with invalid token, then return 401`() {
        // Given
        val invalidRefreshToken = "invalid_refresh_token"

        // When/Then
        mockMvc.perform(
            post("/api/iam/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\": \"$invalidRefreshToken\"}")
        )
            .andDo(print())
            .andExpect(status().isUnauthorized)
    }
}
