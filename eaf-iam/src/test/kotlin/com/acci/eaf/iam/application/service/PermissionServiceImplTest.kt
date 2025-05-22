package com.acci.eaf.iam.application.service

import com.acci.eaf.iam.adapter.persistence.PermissionRepository
import com.acci.eaf.iam.application.port.api.PermissionDto
import com.acci.eaf.iam.domain.exception.PermissionNotFoundException
import com.acci.eaf.iam.domain.model.Permission
import java.util.Optional
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable

@ExtendWith(MockitoExtension::class)
class PermissionServiceImplTest {

    @Mock
    private lateinit var permissionRepository: PermissionRepository

    private lateinit var permissionService: PermissionServiceImpl

    private val permissionId = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        permissionService = PermissionServiceImpl(permissionRepository)
    }

    @Test
    fun `getAllPermissions should return all permissions`() {
        // Given
        val permissions = listOf(
            Permission(
                permissionId = UUID.randomUUID(),
                name = "permission:create",
                description = "Create permission"
            ),
            Permission(
                permissionId = UUID.randomUUID(),
                name = "permission:read",
                description = "Read permission"
            )
        )
        val expectedDtos = permissions.map { PermissionDto.fromEntity(it) }

        val pageable = mock(Pageable::class.java)
        val page = PageImpl(permissions)
        val expectedPageDto = PageImpl(expectedDtos, pageable, permissions.size.toLong())

        `when`(permissionRepository.findAll(pageable)).thenReturn(page)

        // When
        val result = permissionService.getAllPermissions(pageable)

        // Then
        assertEquals(expectedPageDto.content, result.content)
        assertEquals(expectedPageDto.totalPages, result.totalPages)
        assertEquals(expectedPageDto.totalElements, result.totalElements)
    }

    @Test
    fun `getPermissionById should return permission when it exists`() {
        // Given
        val permission = Permission(
            permissionId = permissionId,
            name = "permission:read",
            description = "Read permission"
        )
        val expectedDto = PermissionDto.fromEntity(permission)

        `when`(permissionRepository.findById(permissionId)).thenReturn(Optional.of(permission))

        // When
        val result = permissionService.getPermissionById(permissionId)

        // Then
        assertEquals(expectedDto, result)
    }

    @Test
    fun `getPermissionById should throw exception when permission does not exist`() {
        // Given
        `when`(permissionRepository.findById(permissionId)).thenReturn(Optional.empty())

        // When/Then
        assertFailsWith<PermissionNotFoundException> {
            permissionService.getPermissionById(permissionId)
        }
    }

    @Test
    fun `searchPermissionsByName should return permissions matching the search term`() {
        // Given
        val searchTerm = "user"
        val permissions = listOf(
            Permission(
                permissionId = UUID.randomUUID(),
                name = "user:create",
                description = "Create user"
            ),
            Permission(
                permissionId = UUID.randomUUID(),
                name = "user:read",
                description = "Read user"
            )
        )
        val expectedDtos = permissions.map { PermissionDto.fromEntity(it) }

        val pageable = mock(Pageable::class.java)
        val page = PageImpl(permissions)
        val expectedPageDto = PageImpl(expectedDtos, pageable, permissions.size.toLong())

        `when`(permissionRepository.findByNameContainingIgnoreCase(searchTerm, pageable)).thenReturn(page)

        // When
        val result = permissionService.searchPermissionsByName(searchTerm, pageable)

        // Then
        assertEquals(expectedPageDto.content, result.content)
        assertEquals(expectedPageDto.totalPages, result.totalPages)
        assertEquals(expectedPageDto.totalElements, result.totalElements)
    }
}
