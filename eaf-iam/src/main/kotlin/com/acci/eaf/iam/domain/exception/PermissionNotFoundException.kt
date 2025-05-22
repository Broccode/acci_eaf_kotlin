package com.acci.eaf.iam.domain.exception

import java.util.UUID

/**
 * Ausnahme, die geworfen wird, wenn eine Berechtigung nicht gefunden wurde.
 */
class PermissionNotFoundException : RuntimeException {
    constructor(permissionId: UUID) : super("Berechtigung mit ID $permissionId nicht gefunden")
    constructor(name: String) : super("Berechtigung mit Name '$name' nicht gefunden")
}
