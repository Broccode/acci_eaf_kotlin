package com.acci.eaf.iam.domain.exception

/**
 * Exception, die bei Authentifizierungsproblemen geworfen wird.
 * Diese Exception wird verwendet, wenn ein Benutzer sich nicht authentifizieren kann,
 * z.B. aufgrund von falschen Anmeldedaten oder gesperrtem Konto.
 */
class AuthenticationException(message: String) : RuntimeException(message)
