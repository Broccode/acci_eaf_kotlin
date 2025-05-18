package com.acci.eaf.controlplane.api.service

import com.acci.eaf.controlplane.api.dto.TenantPageParams
import com.acci.eaf.multitenancy.domain.Tenant
import com.acci.eaf.multitenancy.domain.TenantStatus
import com.acci.eaf.multitenancy.dto.TenantDto
import com.acci.eaf.multitenancy.repository.TenantRepository
import jakarta.persistence.criteria.Predicate
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Service that extends the TenantService functionality with pagination and filtering capabilities.
 */
@Service
class TenantPageService(private val tenantRepository: TenantRepository) {

    /**
     * Get a paginated list of tenants with optional filtering.
     *
     * @param pageParams Parameters for pagination and filtering
     * @return Page of tenant DTOs
     */
    @Transactional(readOnly = true)
    fun getTenants(pageParams: TenantPageParams): Page<TenantDto> {
        val pageable = PageRequest.of(
            pageParams.page,
            pageParams.size,
            Sort.by(Sort.Direction.ASC, "name")
        )

        val specification = createSpecification(pageParams)

        return tenantRepository.findAll(specification, pageable)
            .map { tenant ->
                TenantDto(
                    tenantId = tenant.tenantId,
                    name = tenant.name,
                    status = tenant.status,
                    createdAt = tenant.createdAt,
                    updatedAt = tenant.updatedAt
                )
            }
    }

    /**
     * Create a JPA Specification for filtering tenants based on the page parameters.
     */
    private fun createSpecification(pageParams: TenantPageParams): Specification<Tenant> =
        Specification { root, query, criteriaBuilder ->
            val predicates = mutableListOf<Predicate>()

            // Filter by status if specified
            pageParams.status?.let {
                predicates.add(
                    criteriaBuilder.equal(root.get<TenantStatus>("status"), it)
                )
            }

            // Filter by name if specified
            pageParams.nameContains?.let {
                if (it.isNotBlank()) {
                    predicates.add(
                        criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("name")),
                            "%${it.lowercase()}%"
                        )
                    )
                }
            }

            // Combine all predicates with AND
            if (predicates.isEmpty()) {
                null
            } else {
                criteriaBuilder.and(*predicates.toTypedArray())
            }
        }
}
