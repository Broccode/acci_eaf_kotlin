package com.acci.eaf.iam.adapter.persistence

import com.acci.eaf.iam.domain.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

/**
 * Repository für den Zugriff auf User-Entitäten in der Datenbank.
 */
@Repository
interface UserRepository : JpaRepository<User, UUID> {

    /**
     * Findet einen Benutzer anhand des Benutzernamens und der Tenant-ID.
     *
     * @param username der Benutzername
     * @param tenantId die ID des Tenants
     * @return ein Optional, das den gefundenen Benutzer enthält oder leer ist
     */
    fun findByUsernameAndTenantId(username: String, tenantId: UUID): Optional<User>

    /**
     * Findet einen Benutzer anhand der E-Mail-Adresse und der Tenant-ID.
     *
     * @param email die E-Mail-Adresse
     * @param tenantId die ID des Tenants
     * @return ein Optional, das den gefundenen Benutzer enthält oder leer ist
     */
    fun findByEmailAndTenantId(email: String, tenantId: UUID): Optional<User>
}
