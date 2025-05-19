package com.acci.eaf.controlplane.api.mapper

import com.acci.eaf.controlplane.api.dto.CreateTenantRequestDto
import com.acci.eaf.controlplane.api.dto.PagedTenantsResponseDto
import com.acci.eaf.controlplane.api.dto.TenantResponseDto
import com.acci.eaf.controlplane.api.dto.UpdateTenantRequestDto
import com.acci.eaf.multitenancy.dto.CreateTenantDto
import com.acci.eaf.multitenancy.dto.TenantDto
import com.acci.eaf.multitenancy.dto.UpdateTenantDto
import org.springframework.data.domain.Page

/**
 * Interface for mapping Tenant DTOs.
 */
interface TenantMapperInterface {

    fun toServiceDto(requestDto: CreateTenantRequestDto): CreateTenantDto

    fun toServiceDto(requestDto: UpdateTenantRequestDto): UpdateTenantDto

    fun toResponseDto(tenantDto: TenantDto): TenantResponseDto

    fun toPagedResponseDto(tenantPage: Page<TenantDto>): PagedTenantsResponseDto
}
