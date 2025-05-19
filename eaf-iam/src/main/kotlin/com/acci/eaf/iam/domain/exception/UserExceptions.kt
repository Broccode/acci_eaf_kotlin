package com.acci.eaf.iam.domain.exception

/**
 * Exception, die geworfen wird, wenn ein Benutzer nicht gefunden wurde.
 *
 * @property userId Die ID des Benutzers, der nicht gefunden wurde, oder null, wenn nach anderen Kriterien gesucht wurde.
 * @property message Eine detaillierte Nachricht, die den Fehler beschreibt.
 */
class UserNotFoundException(val userId: String? = null, override val message: String) : RuntimeException(message)

/**
 * Exception, die geworfen wird, wenn ein Benutzer bereits existiert.
 *
 * @property username Der Benutzername, der bereits existiert.
 * @property tenantId Die ID des Tenants, in dem der Benutzer bereits existiert.
 * @property message Eine detaillierte Nachricht, die den Fehler beschreibt.
 */
class UserAlreadyExistsException(
    val username: String,
    val tenantId: String,
    override val message: String = "Benutzer mit Benutzername '$username' existiert bereits in Tenant '$tenantId'",
) : RuntimeException(message)

/**
 * Exception, die geworfen wird, wenn ein Passwort die Validierungsregeln nicht erfüllt.
 *
 * @property errors Eine Liste von Fehlermeldungen, die beschreiben, welche Validierungsregeln nicht erfüllt wurden.
 * @property message Eine zusammenfassende Nachricht, die den Fehler beschreibt.
 */
class PasswordValidationException(
    val errors: List<String>,
    override val message: String = "Das Passwort erfüllt nicht die Komplexitätsanforderungen: ${errors.joinToString("; ")}",
) : RuntimeException(message)
