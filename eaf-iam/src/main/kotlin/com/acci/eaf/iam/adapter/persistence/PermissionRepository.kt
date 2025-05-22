package com.acci.eaf.iam.adapter.persistence

import com.acci.eaf.iam.domain.model.Permission
import java.util.Optional
import java.util.UUID
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface PermissionRepository : JpaRepository<Permission, UUID> {

    /**
     * Findet eine Berechtigung anhand ihres Namens.
     */
    fun findByName(name: String): Optional<Permission>

    /**
     * Findet alle Berechtigungen, deren Name den angegebenen String enthält.
     */
    fun findByNameContainingIgnoreCase(namePart: String, pageable: Pageable): Page<Permission>

    /**
     * Findet alle Berechtigungen, die einer bestimmten Rolle zugeordnet sind.
     */
    @Query("SELECT p FROM Permission p JOIN p.roles r WHERE r.roleId = :roleId")
    fun findByRoleId(@Param("roleId") roleId: UUID): List<Permission>

    /**
     * Findet alle effektiven Berechtigungen eines Benutzers anhand seiner Rollen.
     */
    @Query("SELECT DISTINCT p FROM Permission p JOIN p.roles r JOIN r.users u WHERE u.id = :userId")
    fun findEffectivePermissionsByUserId(@Param("userId") userId: UUID): List<Permission>
}
