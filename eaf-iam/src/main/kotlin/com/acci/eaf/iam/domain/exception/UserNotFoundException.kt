package com.acci.eaf.iam.domain.exception

import java.util.UUID

/**
 * Ausnahme, die geworfen wird, wenn ein Benutzer nicht gefunden wurde.
 */
class UserNotFoundException(message: String, val userId: String? = null) : RuntimeException(message) {
    // Secondary constructor for compatibility if needed, though primary is now more flexible
    constructor(userId: UUID) : this("Benutzer mit ID $userId nicht gefunden", userId.toString())
    constructor(username: String, tenantId: UUID) : this("Benutzer mit Benutzername '$username' und TenantId $tenantId nicht gefunden")
}
