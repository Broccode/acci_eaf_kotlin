package com.acci.eaf.multitenancy.integration

import com.acci.eaf.multitenancy.domain.TenantStatus
import com.acci.eaf.multitenancy.dto.CreateTenantDto
import com.acci.eaf.multitenancy.dto.UpdateTenantDto
import com.acci.eaf.multitenancy.exception.TenantNameAlreadyExistsException
import com.acci.eaf.multitenancy.exception.TenantNotFoundException
import com.acci.eaf.multitenancy.repository.TenantRepository
import com.acci.eaf.multitenancy.service.TenantService
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class TenantIntegrationTest {

    companion object {
        @Container
        val postgresContainer = PostgreSQLContainer<Nothing>("postgres:14").apply {
            withDatabaseName("testdb")
            withUsername("testuser")
            withPassword("testpass")
        }

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgresContainer::getJdbcUrl)
            registry.add("spring.datasource.username", postgresContainer::getUsername)
            registry.add("spring.datasource.password", postgresContainer::getPassword)
            registry.add("spring.jpa.hibernate.ddl-auto") { "none" }
            registry.add("spring.flyway.enabled") { "true" }
        }
    }

    @Autowired
    private lateinit var tenantService: TenantService

    @Autowired
    private lateinit var tenantRepository: TenantRepository

    @Test
    fun `should create tenant with valid data`() {
        // Given
        val createTenantDto = CreateTenantDto(
            name = "test-tenant-${UUID.randomUUID().toString().substring(0, 8)}",
            status = TenantStatus.ACTIVE
        )

        // When
        val createdTenant = tenantService.createTenant(createTenantDto)

        // Then
        assertNotNull(createdTenant.tenantId)
        assertEquals(createTenantDto.name, createdTenant.name)
        assertEquals(createTenantDto.status, createdTenant.status)

        // Verify tenant is persisted in the database
        val tenantFromDb = tenantService.getTenantById(createdTenant.tenantId)
        assertEquals(createdTenant.tenantId, tenantFromDb.tenantId)
        assertEquals(createdTenant.name, tenantFromDb.name)
        assertEquals(createdTenant.status, tenantFromDb.status)
    }

    @Test
    fun `should throw exception when creating tenant with duplicate name`() {
        // Given
        val name = "unique-tenant-${UUID.randomUUID().toString().substring(0, 8)}"
        val firstTenant = CreateTenantDto(name = name, status = TenantStatus.ACTIVE)
        val duplicateTenant = CreateTenantDto(name = name, status = TenantStatus.ACTIVE)

        // When
        tenantService.createTenant(firstTenant)

        // Then
        assertThrows<TenantNameAlreadyExistsException> {
            tenantService.createTenant(duplicateTenant)
        }
    }

    @Test
    fun `should update tenant with valid data`() {
        // Given
        val createTenantDto = CreateTenantDto(
            name = "original-${UUID.randomUUID().toString().substring(0, 8)}",
            status = TenantStatus.PENDING_VERIFICATION
        )
        val createdTenant = tenantService.createTenant(createTenantDto)

        val updateTenantDto = UpdateTenantDto(
            name = "updated-${UUID.randomUUID().toString().substring(0, 8)}",
            status = TenantStatus.ACTIVE
        )

        // When
        val updatedTenant = tenantService.updateTenant(createdTenant.tenantId, updateTenantDto)

        // Then
        assertEquals(createdTenant.tenantId, updatedTenant.tenantId)
        assertEquals(updateTenantDto.name, updatedTenant.name)
        assertEquals(updateTenantDto.status, updatedTenant.status)

        // Verify tenant is updated in the database
        val tenantFromDb = tenantService.getTenantById(createdTenant.tenantId)
        assertEquals(updatedTenant.name, tenantFromDb.name)
        assertEquals(updatedTenant.status, tenantFromDb.status)
    }

    @Test
    fun `should delete tenant by changing status to ARCHIVED`() {
        // Given
        val createTenantDto = CreateTenantDto(
            name = "to-delete-${UUID.randomUUID().toString().substring(0, 8)}",
            status = TenantStatus.ACTIVE
        )
        val createdTenant = tenantService.createTenant(createTenantDto)

        // When
        val deletedTenant = tenantService.deleteTenant(createdTenant.tenantId)

        // Then
        assertEquals(createdTenant.tenantId, deletedTenant.tenantId)
        assertEquals(TenantStatus.ARCHIVED, deletedTenant.status)

        // Verify tenant is archived in the database
        val tenantFromDb = tenantService.getTenantById(createdTenant.tenantId)
        assertEquals(TenantStatus.ARCHIVED, tenantFromDb.status)
    }

    @Test
    fun `should throw exception when attempting to get non-existent tenant`() {
        // Given
        val nonExistentId = UUID.randomUUID()

        // When / Then
        assertThrows<TenantNotFoundException> {
            tenantService.getTenantById(nonExistentId)
        }
    }
}
