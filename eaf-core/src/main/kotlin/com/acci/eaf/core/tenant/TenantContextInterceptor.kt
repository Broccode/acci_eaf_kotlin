package com.acci.eaf.core.tenant

import com.acci.eaf.core.interfaces.TenantServiceApi
import com.acci.eaf.core.interfaces.TenantStatus
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.util.UUID
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.ModelAndView

/**
 * Interceptor that extracts, validates, and sets the tenant ID for each request.
 *
 * This interceptor is responsible for:
 * 1. Extracting the tenant ID from the HTTP header
 * 2. Validating that the tenant exists and is active
 * 3. Setting the tenant ID in the TenantContextHolder for the duration of the request
 * 4. Clearing the tenant ID after the request is completed
 */
@Component
class TenantContextInterceptor(private val tenantService: TenantServiceApi) : HandlerInterceptor {

    private val logger = LoggerFactory.getLogger(TenantContextInterceptor::class.java)

    companion object {
        const val TENANT_HEADER = "X-Tenant-ID"
        const val ERROR_ATTRIBUTE = "tenantError"
    }

    /**
     * Processes the request before the handler is executed.
     *
     * Extracts the tenant ID from the request header, validates it,
     * and sets it in the TenantContextHolder.
     *
     * @param request The HTTP request
     * @param response The HTTP response
     * @param handler The handler to be executed
     * @return true if the execution chain should proceed, false otherwise
     */
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        val tenantHeader = request.getHeader(TENANT_HEADER)

        if (tenantHeader.isNullOrBlank()) {
            logger.warn("Missing tenant ID header")
            response.status = HttpServletResponse.SC_BAD_REQUEST
            response.writer.write("Missing tenant ID header")
            return false
        }

        try {
            val tenantId = UUID.fromString(tenantHeader)
            logger.debug("Found tenant ID in header: {}", tenantId)

            // Validate if tenant exists and is active
            try {
                val tenant = tenantService.getTenantById(tenantId)

                if (tenant.status != TenantStatus.ACTIVE) {
                    logger.warn("Tenant is not active: {} - Status: {}", tenantId, tenant.status)
                    response.status = HttpServletResponse.SC_FORBIDDEN
                    response.writer.write("Tenant is not active")
                    return false
                }

                // Set tenant ID in context holder
                TenantContextHolder.setTenantId(tenantId)
                logger.debug("Set tenant ID in context: {}", tenantId)
                return true
            } catch (e: Exception) {
                logger.warn("Invalid tenant ID: {}", tenantId, e)
                response.status = HttpServletResponse.SC_FORBIDDEN
                response.writer.write("Invalid tenant ID")
                return false
            }
        } catch (e: IllegalArgumentException) {
            logger.warn("Invalid UUID format for tenant ID: {}", tenantHeader, e)
            response.status = HttpServletResponse.SC_BAD_REQUEST
            response.writer.write("Invalid tenant ID format")
            return false
        }
    }

    /**
     * Processes the request after the handler has been executed and the ModelAndView has been set.
     *
     * @param request The HTTP request
     * @param response The HTTP response
     * @param handler The handler that was executed
     * @param modelAndView The ModelAndView that was set
     */
    override fun postHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        modelAndView: ModelAndView?,
    ) {
        // No action needed in postHandle
    }

    /**
     * Cleans up the tenant context after the request has been completed.
     *
     * @param request The HTTP request
     * @param response The HTTP response
     * @param handler The handler that was executed
     * @param ex Any exception that occurred during the request
     */
    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?,
    ) {
        logger.debug("Clearing tenant context after request completion")
        TenantContextHolder.clear()
    }
} 
