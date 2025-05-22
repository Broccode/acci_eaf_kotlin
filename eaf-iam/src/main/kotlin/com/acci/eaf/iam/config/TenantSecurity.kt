package com.acci.eaf.iam.config

import java.util.UUID
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

/**
 * Komponente zur Überprüfung der Tenant-bezogenen Zugriffsrechte eines Benutzers.
 * Diese Klasse wird in den @PreAuthorize-Annotationen der Controller verwendet,
 * um sicherzustellen, dass ein Benutzer nur auf Ressourcen seines eigenen Tenants zugreifen kann.
 */
@Component
class TenantSecurity {

    /**
     * Prüft, ob der aktuell authentifizierte Benutzer dem angegebenen Tenant angehört.
     *
     * @param tenantId Die zu prüfende Tenant-ID
     * @return true, wenn der Benutzer zum Tenant gehört, sonst false
     */
    fun isUserInTenant(tenantId: UUID): Boolean {
        val authentication = SecurityContextHolder.getContext().authentication ?: return false

        // Super-Admin (mit der Berechtigung 'tenant:admin') kann auf alle Tenants zugreifen
        if (authentication.authorities.any { it.authority == "tenant:admin" }) {
            return true
        }

        val principal = authentication.principal
        if (principal is JwtUserDetails) {
            return principal.tenantId == tenantId
        }

        return false
    }
}
