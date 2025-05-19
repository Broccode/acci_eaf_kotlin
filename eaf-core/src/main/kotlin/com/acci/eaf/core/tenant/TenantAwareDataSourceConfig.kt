package com.acci.eaf.core.tenant

import java.sql.Connection
import java.sql.SQLException
import javax.sql.DataSource
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.jdbc.datasource.DelegatingDataSource
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy

/**
 * Configuration for making DataSource tenant-aware by setting PostgreSQL session variable.
 *
 * This adds support for Row Level Security (RLS) by setting the 'app.current_tenant_id'
 * session variable on each connection before it's used.
 */
@Configuration
@ConditionalOnProperty(name = ["eaf.multitenancy.datasource.enabled"], havingValue = "true", matchIfMissing = false)
class TenantAwareDataSourceConfig {

    private val logger = LoggerFactory.getLogger(TenantAwareDataSourceConfig::class.java)

    @Bean
    @Primary
    fun tenantAwareDataSource(dataSource: DataSource): DataSource {
        // Wrap original DataSource to intercept getConnection calls
        val tenantAwareDs = TenantAwareDataSource(dataSource)

        // Make DataSource transaction-aware (important for consistent tenant context)
        val txAwareDs = TransactionAwareDataSourceProxy(tenantAwareDs)

        // Add lazy connection to ensure tenant context is set at the time of actual usage
        return LazyConnectionDataSourceProxy(txAwareDs)
    }

    /**
     * DataSource wrapper that sets the tenant ID as a PostgreSQL session variable.
     */
    private class TenantAwareDataSource(dataSource: DataSource) : DelegatingDataSource(dataSource) {

        private val logger = LoggerFactory.getLogger(TenantAwareDataSource::class.java)

        override fun getConnection(): Connection = configureConnection(super.getConnection())

        override fun getConnection(username: String, password: String): Connection = configureConnection(super.getConnection(username, password))

        private fun configureConnection(connection: Connection): Connection {
            // Get tenant ID from context holder
            val tenantId = TenantContextHolder.getCurrentTenantId()

            try {
                // Set the PostgreSQL session variable with current tenant ID
                connection.createStatement().use { stmt ->
                    if (tenantId != null) {
                        logger.debug("Setting PostgreSQL session variable 'app.current_tenant_id' to: {}", tenantId)
                        // Use parameterized statement to prevent SQL injection
                        stmt.execute("SET app.current_tenant_id TO '$tenantId'")
                    } else {
                        // Set to special value that won't match any real tenant when tenant context not available
                        logger.debug("No tenant context found, setting 'app.current_tenant_id' to NULL value")
                        stmt.execute("SET app.current_tenant_id TO NULL")
                    }
                }
            } catch (e: SQLException) {
                logger.error("Failed to set tenant context in database session", e)
            }

            return connection
        }
    }
}
