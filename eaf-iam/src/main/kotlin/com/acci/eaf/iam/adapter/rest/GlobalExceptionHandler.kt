package com.acci.eaf.iam.adapter.rest

import com.acci.eaf.iam.domain.exception.PasswordValidationException
import com.acci.eaf.iam.domain.exception.UserAlreadyExistsException
import com.acci.eaf.iam.domain.exception.UserNotFoundException
import jakarta.validation.ConstraintViolationException
import java.net.URI
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.NativeWebRequest

/**
 * Globaler Exception-Handler für die REST-API.
 *
 * Diese Klasse wandelt verschiedene Ausnahmen in standardisierte
 * RFC 7807 Problem Details Antworten um.
 */
@ControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    /**
     * Behandelt Validierungsfehler von Jakarta Validation.
     *
     * @param ex die geworfene Exception
     * @param request der aktuelle Web-Request
     * @return eine ResponseEntity mit dem Problemdetail
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationErrors(ex: MethodArgumentNotValidException, request: NativeWebRequest): ResponseEntity<ProblemDetail> {
        logger.debug("Validierungsfehler: {}", ex.message)

        val problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            "Validierungsfehler in den Anfragedaten"
        )
        problemDetail.type = URI.create("https://acci.com/problems/validation-error")
        problemDetail.title = "Validierungsfehler"

        val fieldErrors = ex.bindingResult.fieldErrors.map { error ->
            mapOf(
                "field" to error.field,
                "message" to (error.defaultMessage ?: "Validierungsfehler"),
                "rejectedValue" to (error.rejectedValue?.toString() ?: "")
            )
        }

        problemDetail.setProperty("errors", fieldErrors)
        return ResponseEntity.badRequest()
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .body(problemDetail)
    }

    /**
     * Behandelt Constraint-Validierungsfehler.
     *
     * @param ex die geworfene Exception
     * @param request der aktuelle Web-Request
     * @return eine ResponseEntity mit dem Problemdetail
     */
    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(ex: ConstraintViolationException, request: NativeWebRequest): ResponseEntity<ProblemDetail> {
        logger.debug("Constraint-Verletzung: {}", ex.message)

        val problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            "Validierungsfehler in den Anfragedaten"
        )
        problemDetail.type = URI.create("https://acci.com/problems/validation-error")
        problemDetail.title = "Validierungsfehler"

        val errors = ex.constraintViolations.map { violation ->
            mapOf(
                "field" to violation.propertyPath.toString(),
                "message" to violation.message,
                "rejectedValue" to (violation.invalidValue?.toString() ?: "")
            )
        }

        problemDetail.setProperty("errors", errors)
        return ResponseEntity.badRequest()
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .body(problemDetail)
    }

    /**
     * Behandelt den Fall, dass ein Benutzer nicht gefunden wurde.
     *
     * @param ex die geworfene Exception
     * @param request der aktuelle Web-Request
     * @return eine ResponseEntity mit dem Problemdetail
     */
    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFound(ex: UserNotFoundException, request: NativeWebRequest): ResponseEntity<ProblemDetail> {
        logger.debug("Benutzer nicht gefunden: {}", ex.message)

        val problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND,
            ex.message ?: "Benutzer wurde nicht gefunden"
        )
        problemDetail.type = URI.create("https://acci.com/problems/resource-not-found")
        problemDetail.title = "Ressource nicht gefunden"
        problemDetail.setProperty("userId", ex.userId)

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .body(problemDetail)
    }

    /**
     * Behandelt den Fall, dass ein Benutzer bereits existiert.
     *
     * @param ex die geworfene Exception
     * @param request der aktuelle Web-Request
     * @return eine ResponseEntity mit dem Problemdetail
     */
    @ExceptionHandler(UserAlreadyExistsException::class)
    fun handleUserAlreadyExists(ex: UserAlreadyExistsException, request: NativeWebRequest): ResponseEntity<ProblemDetail> {
        logger.debug("Benutzer existiert bereits: {}", ex.message)

        val problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT,
            ex.message ?: "Ein Benutzer mit diesem Benutzernamen existiert bereits im Tenant"
        )
        problemDetail.type = URI.create("https://acci.com/problems/resource-already-exists")
        problemDetail.title = "Ressource existiert bereits"
        problemDetail.setProperty("username", ex.username)
        problemDetail.setProperty("tenantId", ex.tenantId)

        return ResponseEntity.status(HttpStatus.CONFLICT)
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .body(problemDetail)
    }

    /**
     * Behandelt Validierungsfehler bei Passwörtern.
     *
     * @param ex die geworfene Exception
     * @param request der aktuelle Web-Request
     * @return eine ResponseEntity mit dem Problemdetail
     */
    @ExceptionHandler(PasswordValidationException::class)
    fun handlePasswordValidation(ex: PasswordValidationException, request: NativeWebRequest): ResponseEntity<ProblemDetail> {
        logger.debug("Passwortvalidierungsfehler: {}", ex.message)

        val problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            "Das angegebene Passwort erfüllt nicht die Komplexitätsanforderungen"
        )
        problemDetail.type = URI.create("https://acci.com/problems/password-validation")
        problemDetail.title = "Passwortvalidierungsfehler"
        problemDetail.setProperty("errors", ex.errors)

        return ResponseEntity.badRequest()
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .body(problemDetail)
    }

    /**
     * Fallback-Handler für alle anderen Ausnahmen.
     *
     * @param ex die geworfene Exception
     * @param request der aktuelle Web-Request
     * @return eine ResponseEntity mit dem Problemdetail
     */
    @ExceptionHandler(Exception::class)
    fun handleGeneralError(ex: Exception, request: NativeWebRequest): ResponseEntity<ProblemDetail> {
        logger.error("Unbehandelte Ausnahme:", ex)

        val problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Es ist ein interner Serverfehler aufgetreten"
        )
        problemDetail.type = URI.create("https://acci.com/problems/server-error")
        problemDetail.title = "Serverfehler"

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .body(problemDetail)
    }
}
