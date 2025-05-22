package com.acci.eaf.iam.domain.repository

import com.acci.eaf.iam.domain.model.Permission
import java.util.UUID
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface PermissionRepository : JpaRepository<Permission, UUID> {
    fun findByNameContainingIgnoreCase(name: String, pageable: Pageable): Page<Permission>
}
