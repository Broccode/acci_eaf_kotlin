package com.acci.eaf.iam.domain.exception

import java.util.UUID

/**
 * Ausnahme, die geworfen wird, wenn versucht wird, eine Rolle zu erstellen, die bereits existiert.
 */
class RoleAlreadyExistsException(name: String, tenantId: UUID?) :
    RuntimeException("Rolle mit Name '$name' existiert bereits für Tenant ${tenantId ?: "null"}")
