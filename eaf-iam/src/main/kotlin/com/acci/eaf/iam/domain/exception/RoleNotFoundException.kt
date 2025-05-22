package com.acci.eaf.iam.domain.exception

import java.util.UUID

/**
 * Ausnahme, die geworfen wird, wenn eine Rolle nicht gefunden wurde.
 */
class RoleNotFoundException : RuntimeException {
    constructor(roleId: UUID) : super("Rolle mit ID $roleId nicht gefunden")
    constructor(name: String, tenantId: UUID?) : super("Rolle mit Name '$name' und TenantId ${tenantId ?: "null"} nicht gefunden")
}
