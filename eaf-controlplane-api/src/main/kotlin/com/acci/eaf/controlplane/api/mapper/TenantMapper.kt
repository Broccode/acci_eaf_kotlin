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
open class TenantMapper : TenantMapperInterface {

    /**
     * Convert a CreateTenantRequestDto to a CreateTenantDto used by the service layer.
     */
    override fun toServiceDto(requestDto: CreateTenantRequestDto): CreateTenantDto =
        CreateTenantDto(
            name = requestDto.name,
            status = requestDto.status
        )

    /**
     * Convert an UpdateTenantRequestDto to an UpdateTenantDto used by the service layer.
     */
    override fun toServiceDto(requestDto: UpdateTenantRequestDto): UpdateTenantDto =
        UpdateTenantDto(
            name = requestDto.name,
            status = requestDto.status
        )

    /**
     * Convert a TenantDto from the service layer to a TenantResponseDto for the API.
     */
    override fun toResponseDto(tenantDto: TenantDto): TenantResponseDto =
        TenantResponseDto(
            tenantId = tenantDto.tenantId,
            name = tenantDto.name,
            status = tenantDto.status,
            createdAt = tenantDto.createdAt,
            updatedAt = tenantDto.updatedAt
        )

    /**
     * Convert a Page of TenantDto to a PagedTenantsResponseDto for the API.
     */
    override fun toPagedResponseDto(tenantPage: Page<TenantDto>): PagedTenantsResponseDto =
        PagedTenantsResponseDto(
            tenants = tenantPage.content.map { toResponseDto(it) },
            page = tenantPage.number,
            size = tenantPage.size,
            totalElements = tenantPage.totalElements,
            totalPages = tenantPage.totalPages
        )
}
