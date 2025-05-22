package com.acci.eaf.iam.domain.exception

/**
 * Exception thrown when a service account is not found.
 */
class ServiceAccountNotFoundException(message: String) : RuntimeException(message)

/**
 * Exception thrown when service account validation fails.
 */
class ServiceAccountValidationException(message: String) : RuntimeException(message)
