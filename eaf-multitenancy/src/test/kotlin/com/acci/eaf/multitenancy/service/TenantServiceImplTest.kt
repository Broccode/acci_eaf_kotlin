package com.acci.eaf.multitenancy.service

import com.acci.eaf.multitenancy.domain.Tenant
import com.acci.eaf.multitenancy.domain.TenantStatus
import com.acci.eaf.multitenancy.dto.CreateTenantDto
import com.acci.eaf.multitenancy.dto.UpdateTenantDto
import com.acci.eaf.multitenancy.exception.InvalidTenantNameException
import com.acci.eaf.multitenancy.exception.InvalidTenantStatusTransitionException
import com.acci.eaf.multitenancy.exception.TenantNameAlreadyExistsException
import com.acci.eaf.multitenancy.exception.TenantNotFoundException
import com.acci.eaf.multitenancy.repository.TenantRepository
import com.acci.eaf.multitenancy.service.impl.TenantServiceImpl
import jakarta.validation.Validation
import jakarta.validation.Validator
import java.time.Instant
import java.util.Optional
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class TenantServiceImplTest {

    private lateinit var tenantRepository: TenantRepository
    private lateinit var validator: Validator
    private lateinit var tenantService: TenantService

    @BeforeEach
    fun setUp() {
        tenantRepository = mock(TenantRepository::class.java)
        validator = Validation.buildDefaultValidatorFactory().validator
        tenantService = TenantServiceImpl(tenantRepository, validator)
    }

    @Test
    fun `createTenant should create and return tenant when input is valid`() {
        // Given
        val createTenantDto = CreateTenantDto(name = "test-tenant", status = TenantStatus.ACTIVE)
        val savedTenant = Tenant(
            tenantId = UUID.randomUUID(),
            name = createTenantDto.name,
            status = createTenantDto.status
        )

        `when`(tenantRepository.existsByName(createTenantDto.name)).thenReturn(false)
        `when`(tenantRepository.save(org.mockito.ArgumentMatchers.any(Tenant::class.java))).thenReturn(savedTenant)

        // When
        val result = tenantService.createTenant(createTenantDto)

        // Then
        assertEquals(savedTenant.tenantId, result.tenantId)
        assertEquals(savedTenant.name, result.name)
        assertEquals(savedTenant.status, result.status)
        verify(tenantRepository, times(1)).existsByName(createTenantDto.name)
        verify(tenantRepository, times(1)).save(org.mockito.ArgumentMatchers.any(Tenant::class.java))
    }

    @Test
    fun `createTenant should throw TenantNameAlreadyExistsException when name already exists`() {
        // Given
        val createTenantDto = CreateTenantDto(name = "existing-tenant")
        `when`(tenantRepository.existsByName(createTenantDto.name)).thenReturn(true)

        // When / Then
        assertThrows<TenantNameAlreadyExistsException> {
            tenantService.createTenant(createTenantDto)
        }

        verify(tenantRepository, times(1)).existsByName(createTenantDto.name)
        verify(tenantRepository, times(0)).save(org.mockito.ArgumentMatchers.any(Tenant::class.java))
    }

    @Test
    fun `createTenant should throw InvalidTenantNameException when name is invalid`() {
        // Given
        val createTenantDto = CreateTenantDto(name = "in valid@tenant")

        // When / Then
        assertThrows<InvalidTenantNameException> {
            tenantService.createTenant(createTenantDto)
        }

        verify(tenantRepository, times(0)).existsByName(anyString())
        verify(tenantRepository, times(0)).save(org.mockito.ArgumentMatchers.any(Tenant::class.java))
    }

    @Test
    fun `getTenantById should return tenant when tenant exists`() {
        // Given
        val tenantId = UUID.randomUUID()
        val tenant = Tenant(
            tenantId = tenantId,
            name = "test-tenant",
            status = TenantStatus.ACTIVE,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        `when`(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant))

        // When
        val result = tenantService.getTenantById(tenantId)

        // Then
        assertEquals(tenant.tenantId, result.tenantId)
        assertEquals(tenant.name, result.name)
        assertEquals(tenant.status, result.status)
        verify(tenantRepository, times(1)).findById(tenantId)
    }

    @Test
    fun `getTenantById should throw TenantNotFoundException when tenant does not exist`() {
        // Given
        val tenantId = UUID.randomUUID()
        `when`(tenantRepository.findById(tenantId)).thenReturn(Optional.empty())

        // When / Then
        assertThrows<TenantNotFoundException> {
            tenantService.getTenantById(tenantId)
        }

        verify(tenantRepository, times(1)).findById(tenantId)
    }

    @Test
    fun `updateTenant should update and return tenant when input is valid`() {
        // Given
        val tenantId = UUID.randomUUID()
        val tenant = Tenant(
            tenantId = tenantId,
            name = "original-name",
            status = TenantStatus.PENDING_VERIFICATION
        )
        val updateTenantDto = UpdateTenantDto(
            name = "updated-name",
            status = TenantStatus.ACTIVE
        )

        `when`(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant))
        `when`(tenantRepository.existsByName(updateTenantDto.name!!)).thenReturn(false)
        `when`(tenantRepository.save(tenant)).thenReturn(tenant)

        // When
        val result = tenantService.updateTenant(tenantId, updateTenantDto)

        // Then
        assertEquals(tenant.tenantId, result.tenantId)
        assertEquals(updateTenantDto.name, result.name)
        assertEquals(updateTenantDto.status, result.status)
        verify(tenantRepository, times(1)).findById(tenantId)
        verify(tenantRepository, times(1)).existsByName(updateTenantDto.name!!)
        verify(tenantRepository, times(1)).save(tenant)
    }

    @Test
    fun `deleteTenant should archive tenant and return it`() {
        // Given
        val tenantId = UUID.randomUUID()
        val tenant = Tenant(
            tenantId = tenantId,
            name = "tenant-to-delete",
            status = TenantStatus.ACTIVE
        )

        `when`(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant))
        `when`(tenantRepository.save(tenant)).thenReturn(tenant)

        // When
        val result = tenantService.deleteTenant(tenantId)

        // Then
        assertEquals(tenant.tenantId, result.tenantId)
        assertEquals(TenantStatus.ARCHIVED, result.status)
        verify(tenantRepository, times(1)).findById(tenantId)
        verify(tenantRepository, times(1)).save(tenant)
    }

    @Test
    fun `existsByName should return true when tenant with name exists`() {
        // Given
        val name = "existing-tenant"
        `when`(tenantRepository.existsByName(name)).thenReturn(true)

        // When
        val result = tenantService.existsByName(name)

        // Then
        assertTrue(result)
        verify(tenantRepository, times(1)).existsByName(name)
    }

    @Test
    fun `existsByName should return false when tenant with name does not exist`() {
        // Given
        val name = "non-existing-tenant"
        `when`(tenantRepository.existsByName(name)).thenReturn(false)

        // When
        val result = tenantService.existsByName(name)

        // Then
        assertFalse(result)
        verify(tenantRepository, times(1)).existsByName(name)
    }

    @Test
    fun `updateTenant should throw InvalidTenantStatusTransitionException when status transition is invalid`() {
        // Given
        val tenantId = UUID.randomUUID()
        val tenant = Tenant(
            tenantId = tenantId,
            name = "test-tenant",
            status = TenantStatus.ARCHIVED
        )
        val updateTenantDto = UpdateTenantDto(status = TenantStatus.ACTIVE)

        `when`(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant))

        // When / Then
        assertThrows<InvalidTenantStatusTransitionException> {
            tenantService.updateTenant(tenantId, updateTenantDto)
        }

        verify(tenantRepository, times(1)).findById(tenantId)
        verify(tenantRepository, times(0)).save(org.mockito.ArgumentMatchers.any(Tenant::class.java))
    }
} 
