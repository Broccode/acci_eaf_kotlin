package com.acci.eaf.iam.application.service

import com.acci.eaf.iam.application.port.api.AssignRolesRequest
import com.acci.eaf.iam.application.port.api.CreateServiceAccountRequest
import com.acci.eaf.iam.application.port.api.RemoveRolesRequest
import com.acci.eaf.iam.application.port.api.RotateSecretRequest
import com.acci.eaf.iam.application.port.api.ServiceAccountStatusDto
import com.acci.eaf.iam.application.port.api.UpdateServiceAccountRequest
import com.acci.eaf.iam.domain.command.ActivateServiceAccountCommand
import com.acci.eaf.iam.domain.command.AssignRolesToServiceAccountCommand
import com.acci.eaf.iam.domain.command.CreateServiceAccountCommand
import com.acci.eaf.iam.domain.command.DeactivateServiceAccountCommand
import com.acci.eaf.iam.domain.command.RemoveRolesFromServiceAccountCommand
import com.acci.eaf.iam.domain.command.RotateServiceAccountSecretCommand
import com.acci.eaf.iam.domain.command.UpdateServiceAccountDetailsCommand
import com.acci.eaf.iam.domain.model.ServiceAccount
import com.acci.eaf.iam.domain.model.ServiceAccountStatus
import com.acci.eaf.iam.domain.repository.ServiceAccountRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.mockk.*
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.match
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import java.time.OffsetDateTime
import java.util.*
import org.axonframework.commandhandling.gateway.CommandGateway
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import io.mockk.impl.annotations.MockK

class DefaultServiceAccountManagementServiceTest {

    private val commandGateway: CommandGateway = mockk()
    private val serviceAccountRepository: ServiceAccountRepository = mockk()

    private lateinit var service: DefaultServiceAccountManagementService

    private val testTenantId = UUID.randomUUID()
    private val testServiceAccountId = UUID.randomUUID()
    private val testRoleId = UUID.randomUUID()
    private val testInitiatedBy = "test-user@example.com"

    @BeforeEach
    fun setup() {
        clearAllMocks()
        service = DefaultServiceAccountManagementService(
            commandGateway = commandGateway,
            serviceAccountRepository = serviceAccountRepository
        )
    }

    @Nested
    inner class CreateServiceAccount {

        @Test
        fun `should create service account successfully`() {
            // Arrange
            val request = CreateServiceAccountRequest(
                tenantId = testTenantId,
                description = "Test Service Account",
                expiresAt = null,
                requestedRoles = setOf(testRoleId),
                initiatedBy = testInitiatedBy
            )

            val expectedServiceAccount = createTestServiceAccount()

            every { commandGateway.sendAndWait<Unit>(any()) } just runs
            every { serviceAccountRepository.findById(any()) } returns expectedServiceAccount

            // Act
            val result = service.createServiceAccount(request)

            // Assert
            result.serviceAccount.serviceAccountId shouldBe testServiceAccountId
            result.serviceAccount.tenantId shouldBe testTenantId
            result.serviceAccount.description shouldBe "Test Service Account"
            result.clientSecret shouldBe "CLIENT_SECRET_PLACEHOLDER"

            verify {
                commandGateway.sendAndWait<Unit>(
                    match<CreateServiceAccountCommand> { cmd ->
                        cmd.tenantId == testTenantId &&
                            cmd.description == "Test Service Account" &&
                            cmd.requestedRoles == setOf(testRoleId) &&
                            cmd.initiatedBy == testInitiatedBy
                    }
                )
            }
        }

        @Test
        fun `should create service account with custom expiration`() {
            // Arrange
            val customExpiration = OffsetDateTime.now().plusDays(180)
            val request = CreateServiceAccountRequest(
                tenantId = testTenantId,
                description = "Test Service Account",
                expiresAt = customExpiration,
                requestedRoles = setOf(testRoleId),
                initiatedBy = testInitiatedBy
            )

            val expectedServiceAccount = createTestServiceAccount()

            every { commandGateway.sendAndWait<Unit>(any()) } just runs
            every { serviceAccountRepository.findById(any()) } returns expectedServiceAccount

            // Act
            val result = service.createServiceAccount(request)

            // Assert
            result.serviceAccount.serviceAccountId shouldBe testServiceAccountId

            verify {
                commandGateway.sendAndWait<Unit>(
                    match<CreateServiceAccountCommand> { cmd ->
                        cmd.requestedExpiresAt == customExpiration
                    }
                )
            }
        }

        @Test
        fun `should handle repository returning null after creation`() {
            // Arrange
            val request = CreateServiceAccountRequest(
                tenantId = testTenantId,
                description = "Test Service Account",
                expiresAt = null,
                requestedRoles = setOf(),
                initiatedBy = testInitiatedBy
            )

            every { commandGateway.sendAndWait<Unit>(any()) } just runs
            every { serviceAccountRepository.findById(any()) } returns null

            // Act & Assert
            val exception = shouldThrow<RuntimeException> {
                service.createServiceAccount(request)
            }

            exception.message shouldContain "Service account creation failed"
        }
    }

    @Nested
    inner class ListServiceAccounts {

        @Test
        fun `should list service accounts for tenant`() {
            // Arrange
            val pageable = PageRequest.of(0, 10)
            val serviceAccounts = listOf(
                createTestServiceAccount(),
                createTestServiceAccount(UUID.randomUUID())
            )
            val page = PageImpl(serviceAccounts, pageable, serviceAccounts.size.toLong())

            every { serviceAccountRepository.findAllByTenantId(testTenantId, pageable) } returns page

            // Act
            val result = service.listServiceAccounts(testTenantId, pageable)

            // Assert
            result.content.size shouldBe 2
            result.content.all { it.tenantId == testTenantId } shouldBe true
        }

        @Test
        fun `should return empty page for tenant with no service accounts`() {
            // Arrange
            val pageable = PageRequest.of(0, 10)
            val emptyPage = PageImpl<ServiceAccount>(emptyList(), pageable, 0)

            every { serviceAccountRepository.findAllByTenantId(testTenantId, pageable) } returns emptyPage

            // Act
            val result = service.listServiceAccounts(testTenantId, pageable)

            // Assert
            result.content shouldBe emptyList()
        }
    }

    @Nested
    inner class GetServiceAccount {

        @Test
        fun `should get service account by id`() {
            // Arrange
            val serviceAccount = createTestServiceAccount()
            every {
                serviceAccountRepository.findByTenantIdAndId(testTenantId, testServiceAccountId)
            } returns serviceAccount

            // Act
            val result = service.getServiceAccount(testTenantId, testServiceAccountId)

            // Assert
            result shouldNotBe null
            result?.serviceAccountId shouldBe testServiceAccountId
            result?.tenantId shouldBe testTenantId
        }

        @Test
        fun `should return null for non-existent service account`() {
            // Arrange
            every {
                serviceAccountRepository.findByTenantIdAndId(testTenantId, testServiceAccountId)
            } returns null

            // Act
            val result = service.getServiceAccount(testTenantId, testServiceAccountId)

            // Assert
            result shouldBe null
        }
    }

    @Nested
    inner class UpdateServiceAccount {

        @Test
        fun `should update service account successfully`() {
            // Arrange
            val request = UpdateServiceAccountRequest(
                tenantId = testTenantId,
                serviceAccountId = testServiceAccountId,
                description = "Updated Description",
                status = ServiceAccountStatusDto.INACTIVE,
                expiresAt = null,
                initiatedBy = testInitiatedBy
            )

            val updatedServiceAccount = createTestServiceAccount().copy(
                description = "Updated Description",
                status = ServiceAccountStatus.INACTIVE
            )

            every { commandGateway.sendAndWait<Unit>(any()) } just runs
            every { serviceAccountRepository.findByTenantIdAndId(testTenantId, testServiceAccountId) } returns updatedServiceAccount

            // Act
            val result = service.updateServiceAccount(request)

            // Assert
            result shouldNotBe null
            result?.description shouldBe "Updated Description"
            result?.status shouldBe ServiceAccountStatusDto.INACTIVE

            verify {
                commandGateway.sendAndWait<Unit>(
                    match<UpdateServiceAccountDetailsCommand> { cmd ->
                        cmd.serviceAccountId == testServiceAccountId &&
                            cmd.tenantId == testTenantId &&
                            cmd.description == "Updated Description" &&
                            cmd.status == ServiceAccountStatus.INACTIVE &&
                            cmd.initiatedBy == testInitiatedBy
                    }
                )
            }
        }

        @Test
        fun `should return null for non-existent service account update`() {
            // Arrange
            val request = UpdateServiceAccountRequest(
                tenantId = testTenantId,
                serviceAccountId = testServiceAccountId,
                description = "Updated Description",
                status = null,
                expiresAt = null,
                initiatedBy = testInitiatedBy
            )

            every { commandGateway.sendAndWait<Unit>(any()) } just runs
            every { serviceAccountRepository.findByTenantIdAndId(testTenantId, testServiceAccountId) } returns null

            // Act
            val result = service.updateServiceAccount(request)

            // Assert
            result shouldBe null
        }
    }

    @Nested
    inner class DeleteServiceAccount {

        @Test
        fun `should delete service account successfully`() {
            // Arrange
            every { commandGateway.sendAndWait<Unit>(any()) } just runs

            // Act
            val result = service.deleteServiceAccount(testTenantId, testServiceAccountId, testInitiatedBy)

            // Assert
            result shouldBe true

            verify {
                commandGateway.sendAndWait<Unit>(
                    match<DeactivateServiceAccountCommand> { cmd ->
                        cmd.serviceAccountId == testServiceAccountId &&
                            cmd.tenantId == testTenantId &&
                            cmd.initiatedBy == testInitiatedBy
                    }
                )
            }
        }

        @Test
        fun `should handle command gateway exception in delete`() {
            // Arrange
            every { commandGateway.sendAndWait<Unit>(any()) } throws RuntimeException("Command execution failed")

            // Act
            val result = service.deleteServiceAccount(testTenantId, testServiceAccountId, testInitiatedBy)

            // Assert
            result shouldBe false
        }
    }

    @Nested
    inner class RotateSecret {

        @Test
        fun `should rotate service account secret successfully`() {
            // Arrange
            val request = RotateSecretRequest(
                tenantId = testTenantId,
                serviceAccountId = testServiceAccountId,
                initiatedBy = testInitiatedBy
            )

            val serviceAccount = createTestServiceAccount()

            every { commandGateway.sendAndWait<Unit>(any()) } just runs
            every { serviceAccountRepository.findByTenantIdAndId(testTenantId, testServiceAccountId) } returns serviceAccount

            // Act
            val result = service.rotateSecret(request)

            // Assert
            result shouldNotBe null
            result?.serviceAccountId shouldBe testServiceAccountId
            result?.clientSecret shouldBe "NEW_CLIENT_SECRET_PLACEHOLDER"

            verify {
                commandGateway.sendAndWait<Unit>(
                    match<RotateServiceAccountSecretCommand> { cmd ->
                        cmd.serviceAccountId == testServiceAccountId &&
                            cmd.tenantId == testTenantId &&
                            cmd.initiatedBy == testInitiatedBy
                    }
                )
            }
        }

        @Test
        fun `should return null for non-existent service account secret rotation`() {
            // Arrange
            val request = RotateSecretRequest(
                tenantId = testTenantId,
                serviceAccountId = testServiceAccountId,
                initiatedBy = testInitiatedBy
            )

            every { commandGateway.sendAndWait<Unit>(any()) } just runs
            every { serviceAccountRepository.findByTenantIdAndId(testTenantId, testServiceAccountId) } returns null

            // Act
            val result = service.rotateSecret(request)

            // Assert
            result shouldBe null
        }
    }

    @Nested
    inner class RoleManagement {

        @Test
        fun `should assign roles to service account`() {
            // Arrange
            val request = AssignRolesRequest(
                tenantId = testTenantId,
                serviceAccountId = testServiceAccountId,
                roleIds = setOf(testRoleId, UUID.randomUUID()),
                initiatedBy = testInitiatedBy
            )

            every { commandGateway.sendAndWait<Unit>(any()) } just runs

            // Act
            service.assignRoles(request)

            // Assert
            verify {
                commandGateway.sendAndWait<Unit>(
                    match<AssignRolesToServiceAccountCommand> { cmd ->
                        cmd.serviceAccountId == testServiceAccountId &&
                            cmd.tenantId == testTenantId &&
                            cmd.rolesToAssign == request.roleIds &&
                            cmd.initiatedBy == testInitiatedBy
                    }
                )
            }
        }

        @Test
        fun `should remove roles from service account`() {
            // Arrange
            val request = RemoveRolesRequest(
                tenantId = testTenantId,
                serviceAccountId = testServiceAccountId,
                roleIds = setOf(testRoleId),
                initiatedBy = testInitiatedBy
            )

            every { commandGateway.sendAndWait<Unit>(any()) } just runs

            // Act
            service.removeRoles(request)

            // Assert
            verify {
                commandGateway.sendAndWait<Unit>(
                    match<RemoveRolesFromServiceAccountCommand> { cmd ->
                        cmd.serviceAccountId == testServiceAccountId &&
                            cmd.tenantId == testTenantId &&
                            cmd.rolesToRemove == request.roleIds &&
                            cmd.initiatedBy == testInitiatedBy
                    }
                )
            }
        }
    }

    @Nested
    inner class ActivateDeactivateServiceAccount {

        @Test
        fun `should activate service account`() {
            // Arrange
            every { commandGateway.sendAndWait<Unit>(any()) } just runs

            // Act
            service.activateServiceAccount(testTenantId, testServiceAccountId, testInitiatedBy)

            // Assert
            verify {
                commandGateway.sendAndWait<Unit>(
                    match<ActivateServiceAccountCommand> { cmd ->
                        cmd.serviceAccountId == testServiceAccountId &&
                            cmd.tenantId == testTenantId &&
                            cmd.initiatedBy == testInitiatedBy
                    }
                )
            }
        }

        @Test
        fun `should deactivate service account`() {
            // Arrange
            every { commandGateway.sendAndWait<Unit>(any()) } just runs

            // Act
            service.deactivateServiceAccount(testTenantId, testServiceAccountId, testInitiatedBy)

            // Assert
            verify {
                commandGateway.sendAndWait<Unit>(
                    match<DeactivateServiceAccountCommand> { cmd ->
                        cmd.serviceAccountId == testServiceAccountId &&
                            cmd.tenantId == testTenantId &&
                            cmd.initiatedBy == testInitiatedBy
                    }
                )
            }
        }
    }

    @Nested
    inner class GetServiceAccountByClientId {

        @Test
        fun `should get service account by client id`() {
            // Arrange
            val testClientId = "test-client-id"
            val serviceAccount = createTestServiceAccount()
            every {
                serviceAccountRepository.findByTenantIdAndClientId(testTenantId, testClientId)
            } returns serviceAccount

            // Act
            val result = service.getServiceAccountByClientId(testTenantId, testClientId)

            // Assert
            result shouldNotBe null
            result?.serviceAccountId shouldBe testServiceAccountId
            result?.tenantId shouldBe testTenantId
        }

        @Test
        fun `should return null for non-existent client id`() {
            // Arrange
            val testClientId = "non-existent-client-id"
            every {
                serviceAccountRepository.findByTenantIdAndClientId(testTenantId, testClientId)
            } returns null

            // Act
            val result = service.getServiceAccountByClientId(testTenantId, testClientId)

            // Assert
            result shouldBe null
        }
    }

    @Nested
    inner class ErrorHandling {

        @Test
        fun `should handle command gateway exceptions gracefully`() {
            // Arrange
            val request = CreateServiceAccountRequest(
                tenantId = testTenantId,
                description = "Test Service Account",
                expiresAt = null,
                requestedRoles = setOf(),
                initiatedBy = testInitiatedBy
            )

            every { commandGateway.sendAndWait<Unit>(any()) } throws RuntimeException("Command execution failed")

            // Act & Assert
            shouldThrow<RuntimeException> {
                service.createServiceAccount(request)
            }
        }

        @Test
        fun `should handle repository exceptions gracefully`() {
            // Arrange
            every {
                serviceAccountRepository.findByTenantIdAndId(testServiceAccountId, testTenantId)
            } throws RuntimeException("Database connection failed")

            // Act & Assert
            shouldThrow<RuntimeException> {
                service.getServiceAccount(testTenantId, testServiceAccountId)
            }
        }
    }

    // Helper methods
    private fun createTestServiceAccount(serviceAccountId: UUID = testServiceAccountId): ServiceAccount =
        ServiceAccount(
            serviceAccountId = serviceAccountId,
            tenantId = testTenantId,
            clientId = "test-client-id",
            clientSecretHash = "hashed-secret",
            salt = "salt",
            description = "Test Service Account",
            status = ServiceAccountStatus.ACTIVE,
            roles = setOf(testRoleId),
            createdAt = OffsetDateTime.now(),
            expiresAt = OffsetDateTime.now().plusYears(1)
        )
}
