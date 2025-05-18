package com.acci.eaf.controlplane.api.error

import com.acci.eaf.multitenancy.exception.InvalidTenantNameException
import com.acci.eaf.multitenancy.exception.InvalidTenantStatusTransitionException
import com.acci.eaf.multitenancy.exception.TenantNameAlreadyExistsException
import com.acci.eaf.multitenancy.exception.TenantNotFoundException
import java.net.URI
import java.time.Instant
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

/**
 * Global exception handler for the Control Plane API.
 * Uses RFC 7807 Problem Details for HTTP APIs format.
 */
@RestControllerAdvice
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {

    /**
     * Handle validation errors.
     */
    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any> {
        val fieldErrors = ex.bindingResult.fieldErrors.map {
            "${it.field}: ${it.defaultMessage}"
        }

        val detail = "Validation failed: ${fieldErrors.joinToString("; ")}"
        val problemDetail = createProblemDetail(
            "Validation Error",
            detail,
            HttpStatus.BAD_REQUEST,
            "https://eaf.acci.com/errors/validation-error"
        )

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(problemDetail)
    }

    /**
     * Handle tenant not found exceptions.
     */
    @ExceptionHandler(TenantNotFoundException::class)
    fun handleTenantNotFound(ex: TenantNotFoundException): ResponseEntity<ProblemDetail> {
        val problemDetail = createProblemDetail(
            "Tenant Not Found",
            ex.message ?: "The requested tenant was not found",
            HttpStatus.NOT_FOUND,
            "https://eaf.acci.com/errors/tenant-not-found"
        )

        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(problemDetail)
    }

    /**
     * Handle tenant name already exists exceptions.
     */
    @ExceptionHandler(TenantNameAlreadyExistsException::class)
    fun handleTenantNameAlreadyExists(ex: TenantNameAlreadyExistsException): ResponseEntity<ProblemDetail> {
        val problemDetail = createProblemDetail(
            "Tenant Name Already Exists",
            ex.message ?: "A tenant with this name already exists",
            HttpStatus.CONFLICT,
            "https://eaf.acci.com/errors/tenant-name-conflict"
        )

        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(problemDetail)
    }

    /**
     * Handle invalid tenant name exceptions.
     */
    @ExceptionHandler(InvalidTenantNameException::class)
    fun handleInvalidTenantName(ex: InvalidTenantNameException): ResponseEntity<ProblemDetail> {
        val problemDetail = createProblemDetail(
            "Invalid Tenant Name",
            ex.message ?: "The tenant name is invalid",
            HttpStatus.BAD_REQUEST,
            "https://eaf.acci.com/errors/invalid-tenant-name"
        )

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(problemDetail)
    }

    /**
     * Handle invalid tenant status transition exceptions.
     */
    @ExceptionHandler(InvalidTenantStatusTransitionException::class)
    fun handleInvalidStatusTransition(ex: InvalidTenantStatusTransitionException): ResponseEntity<ProblemDetail> {
        val problemDetail = createProblemDetail(
            "Invalid Status Transition",
            ex.message ?: "The requested status transition is not allowed",
            HttpStatus.BAD_REQUEST,
            "https://eaf.acci.com/errors/invalid-status-transition"
        )

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(problemDetail)
    }

    /**
     * Handle all other exceptions.
     */
    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ProblemDetail> {
        val problemDetail = createProblemDetail(
            "Internal Server Error",
            "An unexpected error occurred",
            HttpStatus.INTERNAL_SERVER_ERROR,
            "https://eaf.acci.com/errors/internal-error"
        )

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(problemDetail)
    }

    /**
     * Helper method to create a standardized ProblemDetail.
     */
    private fun createProblemDetail(
        title: String,
        detail: String,
        status: HttpStatus,
        type: String,
    ): ProblemDetail {
        val problemDetail = ProblemDetail.forStatus(status)
        problemDetail.title = title
        problemDetail.detail = detail
        problemDetail.type = URI.create(type)
        problemDetail.setProperty("timestamp", Instant.now())

        return problemDetail
    }
}
