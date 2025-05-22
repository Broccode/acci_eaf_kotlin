package com.acci.eaf.iam.application.service

import com.acci.eaf.iam.application.port.input.CreateUserCommand
import com.acci.eaf.iam.application.port.input.SetPasswordCommand
import com.acci.eaf.iam.application.port.input.UpdateUserCommand
import com.acci.eaf.iam.application.port.out.UserRepository
import com.acci.eaf.iam.audit.AuditLogger
import com.acci.eaf.iam.domain.exception.PasswordValidationException
import com.acci.eaf.iam.domain.exception.UserAlreadyExistsException
import com.acci.eaf.iam.domain.exception.UserNotFoundException
import com.acci.eaf.iam.domain.model.User
import com.acci.eaf.iam.domain.model.UserStatus
import com.acci.eaf.iam.domain.service.PasswordValidator
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.util.Optional
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.security.crypto.password.PasswordEncoder

@DisplayName("UserServiceImpl Tests")
class UserServiceImplTest {

    private lateinit var userRepository: UserRepository
    private lateinit var passwordEncoder: PasswordEncoder
    private lateinit var passwordValidator: PasswordValidator
    private lateinit var auditLogger: AuditLogger
    private lateinit var userService: UserServiceImpl

    @BeforeEach
    fun setUp() {
        userRepository = mockk()
        passwordEncoder = mockk()
        passwordValidator = mockk()
        auditLogger = mockk(relaxed = true) // relaxed, damit wir nicht jeden Aufruf mocken müssen
        userService = UserServiceImpl(userRepository, passwordEncoder, passwordValidator, auditLogger)
    }

    @Nested
    @DisplayName("createLocalUser Tests")
    inner class CreateLocalUserTests {

        @Test
        @DisplayName("Sollte einen neuen Benutzer erfolgreich erstellen, wenn alle Eingaben gültig sind")
        fun shouldCreateUserWhenAllInputsAreValid() {
            // Arrange
            val tenantId = UUID.randomUUID()
            val userId = UUID.randomUUID()
            val command = CreateUserCommand(
                tenantId = tenantId,
                username = "testuser",
                password = "ValidPassword123!",
                email = "test@example.com",
                status = UserStatus.ACTIVE
            )

            // Mock repository to return that username doesn't exist
            every { userRepository.existsByUsernameAndTenantId(command.username, command.tenantId) } returns false

            // Mock password validator to return valid
            every { passwordValidator.validate(command.password) } returns PasswordValidator.ValidationResult(true, emptyList())

            // Mock password encoder
            every { passwordEncoder.encode(command.password) } returns "hashedPassword"

            // Mock saving the user
            val userSlot = slot<User>()
            val savedUser = User(
                id = userId,
                tenantId = command.tenantId,
                username = command.username,
                email = command.email,
                passwordHash = "hashedPassword",
                status = command.status
            )
            every { userRepository.save(capture(userSlot)) } returns savedUser

            // Act
            val result = userService.createLocalUser(command)

            // Assert
            assertNotNull(result)
            assertEquals(userId, result.id)
            assertEquals(command.tenantId, result.tenantId)
            assertEquals(command.username, result.username)
            assertEquals(command.email, result.email)
            assertEquals(command.status, result.status)

            // Verify interactions
            verify { userRepository.existsByUsernameAndTenantId(command.username, command.tenantId) }
            verify { passwordValidator.validate(command.password) }
            verify { passwordEncoder.encode(command.password) }
            verify { userRepository.save(any()) }

            // Check that the captured user has the correct values
            with(userSlot.captured) {
                assertEquals(command.tenantId, tenantId)
                assertEquals(command.username, username)
                assertEquals(command.email, email)
                assertEquals("hashedPassword", passwordHash)
                assertEquals(command.status, status)
            }
        }

        @Test
        @DisplayName("Sollte UserAlreadyExistsException werfen, wenn der Benutzername bereits existiert")
        fun shouldThrowUserAlreadyExistsExceptionWhenUsernameAlreadyExists() {
            // Arrange
            val tenantId = UUID.randomUUID()
            val command = CreateUserCommand(
                tenantId = tenantId,
                username = "existinguser",
                password = "ValidPassword123!",
                email = "test@example.com"
            )

            // Mock repository to return that username exists
            every { userRepository.existsByUsernameAndTenantId(command.username, command.tenantId) } returns true

            // Act & Assert
            assertThrows<UserAlreadyExistsException> {
                userService.createLocalUser(command)
            }

            // Verify interactions
            verify { userRepository.existsByUsernameAndTenantId(command.username, command.tenantId) }
            verify(exactly = 0) { passwordValidator.validate(any()) }
            verify(exactly = 0) { passwordEncoder.encode(any()) }
            verify(exactly = 0) { userRepository.save(any()) }
        }

        @Test
        @DisplayName("Sollte PasswordValidationException werfen, wenn das Passwort ungültig ist")
        fun shouldThrowPasswordValidationExceptionWhenPasswordIsInvalid() {
            // Arrange
            val tenantId = UUID.randomUUID()
            val command = CreateUserCommand(
                tenantId = tenantId,
                username = "testuser",
                password = "weak",
                email = "test@example.com"
            )

            // Mock repository to return that username doesn't exist
            every { userRepository.existsByUsernameAndTenantId(command.username, command.tenantId) } returns false

            // Mock password validator to return invalid
            val errors = listOf("Das Passwort muss mindestens 12 Zeichen lang sein")
            every { passwordValidator.validate(command.password) } returns PasswordValidator.ValidationResult(false, errors)

            // Act & Assert
            val exception = assertThrows<PasswordValidationException> {
                userService.createLocalUser(command)
            }

            // Verify that the exception contains the right errors
            assertEquals(errors, exception.errors)

            // Verify interactions
            verify { userRepository.existsByUsernameAndTenantId(command.username, command.tenantId) }
            verify { passwordValidator.validate(command.password) }
            verify(exactly = 0) { passwordEncoder.encode(any()) }
            verify(exactly = 0) { userRepository.save(any()) }
        }
    }

    @Nested
    @DisplayName("getUserById Tests")
    inner class GetUserByIdTests {

        @Test
        @DisplayName("Sollte einen Benutzer erfolgreich zurückgeben, wenn die ID existiert")
        fun shouldReturnUserWhenIdExists() {
            // Arrange
            val userId = UUID.randomUUID()
            val tenantId = UUID.randomUUID()
            val user = User(
                id = userId,
                tenantId = tenantId,
                username = "testuser",
                email = "test@example.com",
                passwordHash = "hashedPassword",
                status = UserStatus.ACTIVE
            )

            every { userRepository.findById(userId) } returns Optional.of(user)

            // Act
            val result = userService.getUserById(userId)

            // Assert
            assertNotNull(result)
            assertEquals(userId, result.id)
            assertEquals(tenantId, result.tenantId)
            assertEquals("testuser", result.username)
            assertEquals("test@example.com", result.email)
            assertEquals(UserStatus.ACTIVE, result.status)

            // Verify interactions
            verify { userRepository.findById(userId) }
        }

        @Test
        @DisplayName("Sollte UserNotFoundException werfen, wenn die ID nicht existiert")
        fun shouldThrowUserNotFoundExceptionWhenIdDoesNotExist() {
            // Arrange
            val userId = UUID.randomUUID()
            every { userRepository.findById(userId) } returns Optional.empty()

            // Act & Assert
            val exception = assertThrows<UserNotFoundException> {
                userService.getUserById(userId)
            }

            // Check that the exception contains the right message
            assertTrue(exception.message!!.contains(userId.toString()))

            // Verify interactions
            verify { userRepository.findById(userId) }
        }
    }

    @Nested
    @DisplayName("updateUser Tests")
    inner class UpdateUserTests {

        @Test
        @DisplayName("Sollte Benutzer-Email erfolgreich aktualisieren")
        fun shouldUpdateUserEmailSuccessfully() {
            // Arrange
            val userId = UUID.randomUUID()
            val tenantId = UUID.randomUUID()
            val user = User(
                id = userId,
                tenantId = tenantId,
                username = "testuser",
                email = "old@example.com",
                passwordHash = "hashedPassword",
                status = UserStatus.ACTIVE
            )

            val newEmail = "new@example.com"
            val command = UpdateUserCommand(
                userId = userId,
                tenantId = tenantId,
                email = newEmail
            )

            every { userRepository.findById(userId) } returns Optional.of(user)
            every { userRepository.save(any()) } answers { firstArg() }

            // Act
            val result = userService.updateUser(command)

            // Assert
            assertNotNull(result)
            assertEquals(newEmail, result.email)
            assertEquals(UserStatus.ACTIVE, result.status) // Status unverändert

            // Verify interactions
            verify { userRepository.findById(userId) }
            verify { userRepository.save(user) }

            // Ensure the user object was updated correctly
            assertEquals(newEmail, user.email)
            assertEquals(UserStatus.ACTIVE, user.status)
        }

        @Test
        @DisplayName("Sollte Benutzer-Status erfolgreich aktualisieren")
        fun shouldUpdateUserStatusSuccessfully() {
            // Arrange
            val userId = UUID.randomUUID()
            val tenantId = UUID.randomUUID()
            val user = User(
                id = userId,
                tenantId = tenantId,
                username = "testuser",
                email = "test@example.com",
                passwordHash = "hashedPassword",
                status = UserStatus.ACTIVE
            )

            val newStatus = UserStatus.LOCKED_BY_ADMIN
            val command = UpdateUserCommand(
                userId = userId,
                tenantId = tenantId,
                status = newStatus
            )

            every { userRepository.findById(userId) } returns Optional.of(user)
            every { userRepository.save(any()) } answers { firstArg() }

            // Act
            val result = userService.updateUser(command)

            // Assert
            assertNotNull(result)
            assertEquals("test@example.com", result.email) // Email unverändert
            assertEquals(newStatus, result.status)

            // Verify interactions
            verify { userRepository.findById(userId) }
            verify { userRepository.save(user) }

            // Ensure the user object was updated correctly
            assertEquals("test@example.com", user.email)
            assertEquals(newStatus, user.status)
        }

        @Test
        @DisplayName("Sollte beide Felder gleichzeitig aktualisieren, wenn angegeben")
        fun shouldUpdateBothFieldsWhenProvided() {
            // Arrange
            val userId = UUID.randomUUID()
            val tenantId = UUID.randomUUID()
            val user = User(
                id = userId,
                tenantId = tenantId,
                username = "testuser",
                email = "old@example.com",
                passwordHash = "hashedPassword",
                status = UserStatus.ACTIVE
            )

            val newEmail = "new@example.com"
            val newStatus = UserStatus.DISABLED_BY_ADMIN
            val command = UpdateUserCommand(
                userId = userId,
                tenantId = tenantId,
                email = newEmail,
                status = newStatus
            )

            every { userRepository.findById(userId) } returns Optional.of(user)
            every { userRepository.save(any()) } answers { firstArg() }

            // Act
            val result = userService.updateUser(command)

            // Assert
            assertNotNull(result)
            assertEquals(newEmail, result.email)
            assertEquals(newStatus, result.status)

            // Verify interactions
            verify { userRepository.findById(userId) }
            verify { userRepository.save(user) }

            // Ensure the user object was updated correctly
            assertEquals(newEmail, user.email)
            assertEquals(newStatus, user.status)
        }

        @Test
        @DisplayName("Sollte UserNotFoundException werfen, wenn die ID nicht existiert")
        fun shouldThrowUserNotFoundExceptionWhenIdDoesNotExist() {
            // Arrange
            val userId = UUID.randomUUID()
            val tenantId = UUID.randomUUID()
            val command = UpdateUserCommand(
                userId = userId,
                tenantId = tenantId,
                email = "new@example.com"
            )

            every { userRepository.findById(userId) } returns Optional.empty()

            // Act & Assert
            val exception = assertThrows<UserNotFoundException> {
                userService.updateUser(command)
            }

            // Check that the exception contains the right message
            assertTrue(exception.message!!.contains(userId.toString()))

            // Verify interactions
            verify { userRepository.findById(userId) }
            verify(exactly = 0) { userRepository.save(any()) }
        }

        @Test
        @DisplayName("Sollte UserNotFoundException werfen, wenn der Benutzer zu einem anderen Tenant gehört")
        fun shouldThrowUserNotFoundExceptionWhenUserBelongsToDifferentTenant() {
            // Arrange
            val userId = UUID.randomUUID()
            val tenantId = UUID.randomUUID()
            val differentTenantId = UUID.randomUUID()
            val user = User(
                id = userId,
                tenantId = differentTenantId, // Anderer Tenant als in Command
                username = "testuser",
                email = "test@example.com",
                passwordHash = "hashedPassword",
                status = UserStatus.ACTIVE
            )

            val command = UpdateUserCommand(
                userId = userId,
                tenantId = tenantId, // Dieser Tenant ist anders als der des Benutzers
                email = "new@example.com"
            )

            every { userRepository.findById(userId) } returns Optional.of(user)

            // Act & Assert
            val exception = assertThrows<UserNotFoundException> {
                userService.updateUser(command)
            }

            // Check that the exception contains the right message
            assertTrue(exception.message!!.contains(userId.toString()))

            // Verify interactions
            verify { userRepository.findById(userId) }
            verify(exactly = 0) { userRepository.save(any()) }
        }
    }

    @Nested
    @DisplayName("setPassword Tests")
    inner class SetPasswordTests {

        @Test
        @DisplayName("Sollte Passwort erfolgreich aktualisieren, wenn Passwort gültig ist")
        fun shouldUpdatePasswordSuccessfullyWhenPasswordIsValid() {
            // Arrange
            val userId = UUID.randomUUID()
            val tenantId = UUID.randomUUID()
            val user = User(
                id = userId,
                tenantId = tenantId,
                username = "testuser",
                email = "test@example.com",
                passwordHash = "oldHashedPassword",
                status = UserStatus.ACTIVE
            )

            val newPassword = "NewValidPassword123!"
            val command = SetPasswordCommand(
                userId = userId,
                tenantId = tenantId,
                newPassword = newPassword
            )

            every { userRepository.findById(userId) } returns Optional.of(user)
            every { passwordValidator.validate(newPassword) } returns PasswordValidator.ValidationResult(true, emptyList())
            every { passwordEncoder.encode(newPassword) } returns "newHashedPassword"
            every { userRepository.save(any()) } answers { firstArg() }

            // Act
            userService.setPassword(command)

            // Assert & Verify
            verify { userRepository.findById(userId) }
            verify { passwordValidator.validate(newPassword) }
            verify { passwordEncoder.encode(newPassword) }
            verify { userRepository.save(user) }

            // Ensure the password hash was updated
            assertEquals("newHashedPassword", user.passwordHash)
        }

        @Test
        @DisplayName("Sollte UserNotFoundException werfen, wenn die ID nicht existiert")
        fun shouldThrowUserNotFoundExceptionWhenIdDoesNotExist() {
            // Arrange
            val userId = UUID.randomUUID()
            val tenantId = UUID.randomUUID()
            val command = SetPasswordCommand(
                userId = userId,
                tenantId = tenantId,
                newPassword = "ValidPassword123!"
            )

            every { userRepository.findById(userId) } returns Optional.empty()

            // Act & Assert
            val exception = assertThrows<UserNotFoundException> {
                userService.setPassword(command)
            }

            // Check that the exception contains the right message
            assertTrue(exception.message!!.contains(userId.toString()))

            // Verify interactions
            verify { userRepository.findById(userId) }
            verify(exactly = 0) { passwordValidator.validate(any()) }
            verify(exactly = 0) { passwordEncoder.encode(any()) }
            verify(exactly = 0) { userRepository.save(any()) }
        }

        @Test
        @DisplayName("Sollte UserNotFoundException werfen, wenn der Benutzer zu einem anderen Tenant gehört")
        fun shouldThrowUserNotFoundExceptionWhenUserBelongsToDifferentTenant() {
            // Arrange
            val userId = UUID.randomUUID()
            val tenantId = UUID.randomUUID()
            val differentTenantId = UUID.randomUUID()
            val user = User(
                id = userId,
                tenantId = differentTenantId, // Anderer Tenant als in Command
                username = "testuser",
                email = "test@example.com",
                passwordHash = "oldHashedPassword",
                status = UserStatus.ACTIVE
            )

            val newPassword = "NewValidPassword123!"
            val command = SetPasswordCommand(
                userId = userId,
                tenantId = tenantId, // Dieser Tenant ist anders als der des Benutzers
                newPassword = newPassword
            )

            every { userRepository.findById(userId) } returns Optional.of(user)

            // Act & Assert
            val exception = assertThrows<UserNotFoundException> {
                userService.setPassword(command)
            }

            // Check that the exception contains the right message
            assertTrue(exception.message!!.contains(userId.toString()))

            // Verify interactions
            verify { userRepository.findById(userId) }
            verify(exactly = 0) { passwordValidator.validate(any()) }
            verify(exactly = 0) { passwordEncoder.encode(any()) }
            verify(exactly = 0) { userRepository.save(any()) }
        }

        @Test
        @DisplayName("Sollte PasswordValidationException werfen, wenn das Passwort ungültig ist")
        fun shouldThrowPasswordValidationExceptionWhenPasswordIsInvalid() {
            // Arrange
            val userId = UUID.randomUUID()
            val tenantId = UUID.randomUUID()
            val user = User(
                id = userId,
                tenantId = tenantId,
                username = "testuser",
                email = "test@example.com",
                passwordHash = "oldHashedPassword",
                status = UserStatus.ACTIVE
            )

            val newPassword = "weak"
            val command = SetPasswordCommand(
                userId = userId,
                tenantId = tenantId,
                newPassword = newPassword
            )

            // Mock repository to return the user
            every { userRepository.findById(userId) } returns Optional.of(user)

            // Mock password validator to return invalid
            val errors = listOf("Das Passwort muss mindestens 12 Zeichen lang sein")
            every { passwordValidator.validate(newPassword) } returns PasswordValidator.ValidationResult(false, errors)

            // Act & Assert
            val exception = assertThrows<PasswordValidationException> {
                userService.setPassword(command)
            }

            // Verify that the exception contains the right errors
            assertEquals(errors, exception.errors)

            // Verify interactions
            verify { userRepository.findById(userId) }
            verify { passwordValidator.validate(newPassword) }
            verify(exactly = 0) { passwordEncoder.encode(any()) }
            verify(exactly = 0) { userRepository.save(any()) }
        }
    }

    @Nested
    @DisplayName("findUsersByTenant Tests")
    inner class FindUsersByTenantTests {

        @Test
        @DisplayName("Sollte leere Liste zurückgeben, wenn keine Benutzer existieren")
        fun shouldReturnEmptyListWhenNoUsersExist() {
            // Arrange
            val tenantId = UUID.randomUUID()
            val pageable = PageRequest.of(0, 10)

            every { userRepository.findByTenantId(tenantId, pageable) } returns Page.empty()

            // Act
            val result = userService.findUsersByTenant(tenantId, pageable)

            // Assert
            assertTrue(result.isEmpty())

            // Verify interactions
            verify { userRepository.findByTenantId(tenantId, pageable) }
        }

        @Test
        @DisplayName("Sollte alle Benutzer eines Tenants zurückgeben")
        fun shouldReturnAllUsersOfTenant() {
            // Arrange
            val tenantId = UUID.randomUUID()
            val pageable = PageRequest.of(0, 10)

            val user1 = User(
                id = UUID.randomUUID(),
                tenantId = tenantId,
                username = "user1",
                email = "user1@example.com",
                passwordHash = "hash1",
                status = UserStatus.ACTIVE
            )

            val user2 = User(
                id = UUID.randomUUID(),
                tenantId = tenantId,
                username = "user2",
                email = "user2@example.com",
                passwordHash = "hash2",
                status = UserStatus.ACTIVE
            )

            val users = listOf(user1, user2)
            val page = PageImpl(users, pageable, users.size.toLong())

            every { userRepository.findByTenantId(tenantId, pageable) } returns page

            // Act
            val result = userService.findUsersByTenant(tenantId, pageable)

            // Assert
            assertEquals(2, result.totalElements)
            assertEquals("user1", result.content[0].username)
            assertEquals("user2", result.content[1].username)

            // Verify interactions
            verify { userRepository.findByTenantId(tenantId, pageable) }
        }

        @Test
        @DisplayName("Sollte Pagination korrekt anwenden")
        fun shouldApplyPaginationCorrectly() {
            // Arrange
            val tenantId = UUID.randomUUID()
            val pageable = PageRequest.of(1, 2) // Second page, 2 items per page

            val users = listOf(
                User(
                    id = UUID.randomUUID(),
                    tenantId = tenantId,
                    username = "user3",
                    email = "user3@example.com",
                    passwordHash = "hash3",
                    status = UserStatus.ACTIVE
                ),
                User(
                    id = UUID.randomUUID(),
                    tenantId = tenantId,
                    username = "user4",
                    email = "user4@example.com",
                    passwordHash = "hash4",
                    status = UserStatus.ACTIVE
                )
            )

            // Mock a page that would be the second page of results
            val page = PageImpl(users, pageable, 5) // 5 total elements

            every { userRepository.findByTenantId(tenantId, pageable) } returns page

            // Act
            val result = userService.findUsersByTenant(tenantId, pageable)

            // Assert
            assertEquals(5, result.totalElements) // Total count
            assertEquals(3, result.totalPages) // Total pages (5 items, 2 per page = 3 pages)
            assertEquals(1, result.number) // Current page number (0-based)
            assertEquals(2, result.size) // Page size
            assertEquals(2, result.numberOfElements) // Current page element count

            // Verify interactions
            verify { userRepository.findByTenantId(tenantId, pageable) }
        }
    }

    @Nested
    @DisplayName("searchUsers Tests")
    inner class SearchUsersTests {

        @Test
        @DisplayName("Sollte Benutzer nach Username-Teilstring finden")
        fun shouldFindUsersByUsernameSubstring() {
            // Arrange
            val tenantId = UUID.randomUUID()
            val searchTerm = "admin"
            val pageable = PageRequest.of(0, 10)

            val user = User(
                id = UUID.randomUUID(),
                tenantId = tenantId,
                username = "admin_user",
                email = "admin@example.com",
                passwordHash = "hash",
                status = UserStatus.ACTIVE
            )

            val users = listOf(user)
            val page = PageImpl(users, pageable, users.size.toLong())

            every { userRepository.searchByTenantId(tenantId, searchTerm, pageable) } returns page

            // Act
            val result = userService.searchUsers(tenantId, searchTerm, pageable)

            // Assert
            assertEquals(1, result.totalElements)
            assertEquals("admin_user", result.content[0].username)

            // Verify interactions
            verify { userRepository.searchByTenantId(tenantId, searchTerm, pageable) }
        }

        @Test
        @DisplayName("Sollte Benutzer nach Email-Teilstring finden")
        fun shouldFindUsersByEmailSubstring() {
            // Arrange
            val tenantId = UUID.randomUUID()
            val searchTerm = "support"
            val pageable = PageRequest.of(0, 10)

            val user = User(
                id = UUID.randomUUID(),
                tenantId = tenantId,
                username = "support_agent",
                email = "support@example.com",
                passwordHash = "hash",
                status = UserStatus.ACTIVE
            )

            val users = listOf(user)
            val page = PageImpl(users, pageable, users.size.toLong())

            every { userRepository.searchByTenantId(tenantId, searchTerm, pageable) } returns page

            // Act
            val result = userService.searchUsers(tenantId, searchTerm, pageable)

            // Assert
            assertEquals(1, result.totalElements)
            assertEquals("support_agent", result.content[0].username)
            assertEquals("support@example.com", result.content[0].email)

            // Verify interactions
            verify { userRepository.searchByTenantId(tenantId, searchTerm, pageable) }
        }

        @Test
        @DisplayName("Sollte leere Liste zurückgeben, wenn keine Übereinstimmungen gefunden wurden")
        fun shouldReturnEmptyListWhenNoMatchesFound() {
            // Arrange
            val tenantId = UUID.randomUUID()
            val searchTerm = "nonexistent"
            val pageable = PageRequest.of(0, 10)

            every { userRepository.searchByTenantId(tenantId, searchTerm, pageable) } returns Page.empty()

            // Act
            val result = userService.searchUsers(tenantId, searchTerm, pageable)

            // Assert
            assertTrue(result.isEmpty())

            // Verify interactions
            verify { userRepository.searchByTenantId(tenantId, searchTerm, pageable) }
        }

        @Test
        @DisplayName("Sollte Pagination korrekt anwenden")
        fun shouldApplyPaginationCorrectly() {
            // Arrange
            val tenantId = UUID.randomUUID()
            val searchTerm = "user"
            val pageable = PageRequest.of(1, 2) // Second page, 2 items per page

            val users = listOf(
                User(
                    id = UUID.randomUUID(),
                    tenantId = tenantId,
                    username = "user3",
                    email = "user3@example.com",
                    passwordHash = "hash3",
                    status = UserStatus.ACTIVE
                ),
                User(
                    id = UUID.randomUUID(),
                    tenantId = tenantId,
                    username = "user4",
                    email = "user4@example.com",
                    passwordHash = "hash4",
                    status = UserStatus.ACTIVE
                )
            )

            // Mock a page that would be the second page of results
            val page = PageImpl(users, pageable, 5) // 5 total elements

            every { userRepository.searchByTenantId(tenantId, searchTerm, pageable) } returns page

            // Act
            val result = userService.searchUsers(tenantId, searchTerm, pageable)

            // Assert
            assertEquals(5, result.totalElements) // Total count
            assertEquals(3, result.totalPages) // Total pages (5 items, 2 per page = 3 pages)
            assertEquals(1, result.number) // Current page number (0-based)
            assertEquals(2, result.size) // Page size
            assertEquals(2, result.numberOfElements) // Current page element count

            // Verify interactions
            verify { userRepository.searchByTenantId(tenantId, searchTerm, pageable) }
        }
    }
}
