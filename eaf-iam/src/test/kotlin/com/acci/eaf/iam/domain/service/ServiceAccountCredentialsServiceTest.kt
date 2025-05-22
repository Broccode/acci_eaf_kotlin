package com.acci.eaf.iam.domain.service

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldMatch
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.password.PasswordEncoder

class ServiceAccountCredentialsServiceTest {

    private val passwordEncoder: PasswordEncoder = mockk()

    private lateinit var service: DefaultServiceAccountCredentialsService

    @BeforeEach
    fun setup() {
        clearAllMocks()
        service = DefaultServiceAccountCredentialsService(passwordEncoder)
    }

    @Nested
    inner class ClientIdGeneration {

        @Test
        fun `should generate client ID with correct format`() {
            // Act
            val clientId = service.generateClientId()

            // Assert
            clientId shouldNotBe null
            clientId.isNotBlank() shouldBe true
            // Client ID should be base64-url encoded
            clientId shouldMatch Regex("^[A-Za-z0-9_-]+$") // Base64-url characters only
        }

        @Test
        fun `should generate unique client IDs`() {
            // Act
            val clientId1 = service.generateClientId()
            val clientId2 = service.generateClientId()

            // Assert
            clientId1 shouldNotBe clientId2
        }

        @Test
        fun `should generate client IDs with sufficient entropy`() {
            // Act
            val clientIds = (1..100).map { service.generateClientId() }

            // Assert
            // All client IDs should be unique (very high probability with sufficient entropy)
            clientIds.toSet().size shouldBe 100
        }
    }

    @Nested
    inner class ClientSecretGeneration {

        @Test
        fun `should generate client secret with correct format`() {
            // Act
            val clientSecret = service.generateClientSecret()

            // Assert
            clientSecret shouldNotBe null
            clientSecret.isNotBlank() shouldBe true
            // Client secret should be base64 encoded
            clientSecret shouldMatch Regex("^[A-Za-z0-9+/]+$") // Base64 characters
        }

        @Test
        fun `should generate unique client secrets`() {
            // Act
            val secret1 = service.generateClientSecret()
            val secret2 = service.generateClientSecret()

            // Assert
            secret1 shouldNotBe secret2
        }

        @Test
        fun `should generate client secret with sufficient entropy`() {
            // Act
            val secrets = (1..100).map { service.generateClientSecret() }

            // Assert
            // All secrets should be unique (very high probability with sufficient entropy)
            secrets.toSet().size shouldBe 100
        }
    }

    @Nested
    inner class PasswordHashing {

        @Test
        fun `should hash client secret and return hash with salt`() {
            // Arrange
            val clientSecret = "test-client-secret"
            val expectedHash = "hashed-password"

            every { passwordEncoder.encode(clientSecret) } returns expectedHash

            // Act
            val result = service.hashClientSecret(clientSecret)

            // Assert
            result.first shouldBe expectedHash // hash
            result.second shouldNotBe null // salt
            result.second.isNotBlank() shouldBe true
            verify { passwordEncoder.encode(clientSecret) }
        }

        @Test
        fun `should generate different salts for same client secret`() {
            // Arrange
            val clientSecret = "test-client-secret"
            val expectedHash = "hashed-password"

            every { passwordEncoder.encode(clientSecret) } returns expectedHash

            // Act
            val result1 = service.hashClientSecret(clientSecret)
            val result2 = service.hashClientSecret(clientSecret)

            // Assert
            result1.first shouldBe expectedHash
            result2.first shouldBe expectedHash
            result1.second shouldNotBe result2.second // Different salts
        }

        @Test
        fun `should verify client secret correctly`() {
            // Arrange
            val clientSecret = "test-client-secret"
            val salt = "test-salt"
            val hashedSecret = "hashed-password"

            every { passwordEncoder.matches(clientSecret, hashedSecret) } returns true

            // Act
            val result = service.verifyClientSecret(clientSecret, salt, hashedSecret)

            // Assert
            result shouldBe true
            verify { passwordEncoder.matches(clientSecret, hashedSecret) }
        }

        @Test
        fun `should reject invalid client secret`() {
            // Arrange
            val clientSecret = "wrong-client-secret"
            val salt = "test-salt"
            val hashedSecret = "hashed-password"

            every { passwordEncoder.matches(clientSecret, hashedSecret) } returns false

            // Act
            val result = service.verifyClientSecret(clientSecret, salt, hashedSecret)

            // Assert
            result shouldBe false
            verify { passwordEncoder.matches(clientSecret, hashedSecret) }
        }
    }

    @Nested
    inner class EndToEndCredentialFlow {

        @Test
        fun `should handle complete credential generation and verification flow`() {
            // Arrange
            val originalSecret = "generated-secret"
            val hashedValue = "bcrypt-hash-value"

            // Mock the password encoder for hashing
            every { passwordEncoder.encode(originalSecret) } returns hashedValue
            every { passwordEncoder.matches(originalSecret, hashedValue) } returns true
            every { passwordEncoder.matches(not(originalSecret), hashedValue) } returns false

            // Act - Generate and hash
            val clientId = service.generateClientId()
            val clientSecret = service.generateClientSecret()
            val (hash, salt) = service.hashClientSecret(originalSecret)

            // Act - Verify correct secret
            val validVerification = service.verifyClientSecret(originalSecret, salt, hash)

            // Act - Verify wrong secret
            val invalidVerification = service.verifyClientSecret("wrong-secret", salt, hash)

            // Assert
            clientId shouldNotBe null
            clientSecret shouldNotBe null
            hash shouldBe hashedValue
            salt shouldNotBe null
            validVerification shouldBe true
            invalidVerification shouldBe false
        }
    }

    @Nested
    inner class SecurityProperties {

        @Test
        fun `should generate client IDs and secrets with appropriate length`() {
            // Act
            val clientId = service.generateClientId()
            val clientSecret = service.generateClientSecret()

            // Assert
            // Client ID should have reasonable length (32 bytes -> ~43 chars base64url)
            clientId.length shouldBe 43 // 32 bytes -> 43 chars without padding

            // Client secret should have reasonable length (48 bytes -> 64 chars base64)
            clientSecret.length shouldBe 64 // 48 bytes -> 64 chars without padding
        }
    }
}
