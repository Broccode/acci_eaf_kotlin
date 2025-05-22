package com.acci.eaf.iam.config

import java.time.Duration
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "eaf.iam.service-account")
data class ServiceAccountProperties(
    /**
     * Default expiration period for new service accounts.
     * Uses ISO-8601 duration format (e.g., P1Y for 1 year, P30D for 30 days).
     * Default is 1 year.
     */
    var defaultExpiration: Duration = Duration.ofDays(365),

    /**
     * Maximum allowed expiration period that can be set for a service account.
     * Uses ISO-8601 duration format.
     * Default is 5 years. A value of PT0S or less implies no maximum limit from configuration,
     * though administrative policies might still apply.
     */
    var maxExpiration: Duration = Duration.ofDays(365 * 5),

    /**
     * Whether service accounts can be created without an expiration date.
     * Defaults to false, meaning an expiration date is always required unless explicitly overridden by this flag.
     */
    var allowNoExpiration: Boolean = false,
)
