package com.acci.eaf.controlplane.api.mapper

import com.acci.eaf.controlplane.api.dto.CreateTenantRequestDto
import com.acci.eaf.controlplane.api.dto.PagedTenantsResponseDto
import com.acci.eaf.controlplane.api.dto.TenantResponseDto
import com.acci.eaf.controlplane.api.dto.UpdateTenantRequestDto
import com.acci.eaf.multitenancy.dto.CreateTenantDto
import com.acci.eaf.multitenancy.dto.TenantDto
import com.acci.eaf.multitenancy.dto.UpdateTenantDto
import org.springframework.data.domain.Page
import org.springframework.stereotype.Component

/**
 * Mapper for converting between Tenant DTOs in the multitenancy module and the API DTOs.
 */
@Component
class TenantMapper {

    /**
     * Convert a CreateTenantRequestDto to a CreateTenantDto used by the service layer.
     */
    fun toServiceDto(requestDto: CreateTenantRequestDto): CreateTenantDto {
        return CreateTenantDto(
            name = requestDto.name,
            status = requestDto.status
        )
    }

    /**
     * Convert an UpdateTenantRequestDto to an UpdateTenantDto used by the service layer.
     */
    fun toServiceDto(requestDto: UpdateTenantRequestDto): UpdateTenantDto {
        return UpdateTenantDto(
            name = requestDto.name,
            status = requestDto.status
        )
    }

    /**
     * Convert a TenantDto from the service layer to a TenantResponseDto for the API.
     */
    fun toResponseDto(tenantDto: TenantDto): TenantResponseDto {
        return TenantResponseDto(
            tenantId = tenantDto.tenantId,
            name = tenantDto.name,
            status = tenantDto.status,
            createdAt = tenantDto.createdAt,
            updatedAt = tenantDto.updatedAt
        )
    }

    /**
     * Convert a Page of TenantDto to a PagedTenantsResponseDto for the API.
     */
    fun toPagedResponseDto(tenantPage: Page<TenantDto>): PagedTenantsResponseDto {
        return PagedTenantsResponseDto(
            tenants = tenantPage.content.map { toResponseDto(it) },
            page = tenantPage.number,
            size = tenantPage.size,
            totalElements = tenantPage.totalElements,
            totalPages = tenantPage.totalPages
        )
    }
} 