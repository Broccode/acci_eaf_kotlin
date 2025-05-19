package com.acci.eaf.iam.domain.service

import com.acci.eaf.iam.config.PasswordPolicyConfig
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

@DisplayName("PasswordValidator Tests")
class PasswordValidatorTest {

    private lateinit var passwordPolicyConfig: PasswordPolicyConfig
    private lateinit var passwordValidator: PasswordValidator

    @BeforeEach
    fun setUp() {
        passwordPolicyConfig = PasswordPolicyConfig().apply {
            minLength = 12
            requireUppercase = true
            requireLowercase = true
            requireDigit = true
            requireSpecialChar = true
            specialChars = "!@#$%^&*()_-+=[{]}|:;,<.>/?"
            maxConsecutiveIdenticalChars = 3
        }
        passwordValidator = PasswordValidator(passwordPolicyConfig)
    }

    @Test
    @DisplayName("Sollte ein gültiges Passwort akzeptieren")
    fun shouldAcceptValidPassword() {
        // Arrange
        val password = "ValidP@ssw0rd"

        // Act
        val result = passwordValidator.validate(password)

        // Assert
        assertTrue(result.valid)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    @DisplayName("Sollte ein zu kurzes Passwort ablehnen")
    fun shouldRejectTooShortPassword() {
        // Arrange
        val password = "Short1!"

        // Act
        val result = passwordValidator.validate(password)

        // Assert
        assertFalse(result.valid)
        assertTrue(result.errors.any { it.contains("mindestens ${passwordPolicyConfig.minLength} Zeichen") })
    }

    @Test
    @DisplayName("Sollte ein Passwort ohne Großbuchstaben ablehnen, wenn requireUppercase true ist")
    fun shouldRejectPasswordWithoutUppercaseWhenRequired() {
        // Arrange
        val password = "nouppercase123!"

        // Act
        val result = passwordValidator.validate(password)

        // Assert
        assertFalse(result.valid)
        assertTrue(result.errors.any { it.contains("Großbuchstaben") })
    }

    @Test
    @DisplayName("Sollte ein Passwort ohne Kleinbuchstaben ablehnen, wenn requireLowercase true ist")
    fun shouldRejectPasswordWithoutLowercaseWhenRequired() {
        // Arrange
        val password = "NOLOWERCASE123!"

        // Act
        val result = passwordValidator.validate(password)

        // Assert
        assertFalse(result.valid)
        assertTrue(result.errors.any { it.contains("Kleinbuchstaben") })
    }

    @Test
    @DisplayName("Sollte ein Passwort ohne Ziffern ablehnen, wenn requireDigit true ist")
    fun shouldRejectPasswordWithoutDigitWhenRequired() {
        // Arrange
        val password = "NoDigitsHere!"

        // Act
        val result = passwordValidator.validate(password)

        // Assert
        assertFalse(result.valid)
        assertTrue(result.errors.any { it.contains("Ziffer") })
    }

    @Test
    @DisplayName("Sollte ein Passwort ohne Sonderzeichen ablehnen, wenn requireSpecialChar true ist")
    fun shouldRejectPasswordWithoutSpecialCharWhenRequired() {
        // Arrange
        val password = "NoSpecialChars123"

        // Act
        val result = passwordValidator.validate(password)

        // Assert
        assertFalse(result.valid)
        assertTrue(result.errors.any { it.contains("Sonderzeichen") })
    }

    @ParameterizedTest
    @ValueSource(strings = ["aaaa1234ABCD!", "1234aaaaBCDE!", "ABCDEaaaaa123!"])
    @DisplayName("Sollte ein Passwort mit zu vielen aufeinanderfolgenden identischen Zeichen ablehnen")
    fun shouldRejectPasswordWithTooManyConsecutiveIdenticalChars(password: String) {
        // Act
        val result = passwordValidator.validate(password)

        // Assert
        assertFalse(result.valid)
        assertTrue(result.errors.any { it.contains("identische Zeichen hintereinander") })
    }

    @Test
    @DisplayName("Sollte mehrere Fehler gleichzeitig zurückgeben, wenn ein Passwort mehrere Regeln verletzt")
    fun shouldReturnMultipleErrorsWhenPasswordViolatesMultipleRules() {
        // Arrange
        val password = "aa" // Zu kurz, keine Großbuchstaben, keine Ziffern, keine Sonderzeichen

        // Act
        val result = passwordValidator.validate(password)

        // Assert
        assertFalse(result.valid)
        assertEquals(4, result.errors.size)
        assertTrue(result.errors.any { it.contains("mindestens ${passwordPolicyConfig.minLength} Zeichen") })
        assertTrue(result.errors.any { it.contains("Großbuchstaben") })
        assertTrue(result.errors.any { it.contains("Ziffer") })
        assertTrue(result.errors.any { it.contains("Sonderzeichen") })
    }

    @Test
    @DisplayName("Sollte ein Passwort akzeptieren, wenn die entsprechenden Anforderungen ausgeschaltet sind")
    fun shouldAcceptPasswordWhenRequirementsAreTurnedOff() {
        // Arrange
        val customConfig = PasswordPolicyConfig().apply {
            minLength = 8
            requireUppercase = false
            requireLowercase = false
            requireDigit = false
            requireSpecialChar = false
        }
        val customValidator = PasswordValidator(customConfig)
        val password = "password" // Keine Großbuchstaben, keine Ziffern, keine Sonderzeichen, aber lang genug

        // Act
        val result = customValidator.validate(password)

        // Assert
        assertTrue(result.valid)
        assertTrue(result.errors.isEmpty())
    }
}
