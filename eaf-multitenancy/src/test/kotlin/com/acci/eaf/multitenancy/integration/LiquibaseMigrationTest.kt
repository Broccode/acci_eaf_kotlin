package com.acci.eaf.multitenancy.integration

import kotlin.test.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class LiquibaseMigrationTest {

    companion object {
        @Container
        private val postgresContainer = PostgreSQLContainer<Nothing>("postgres:14-alpine").apply {
            withDatabaseName("testdb")
            withUsername("testuser")
            withPassword("testpass")
        }

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgresContainer::getJdbcUrl)
            registry.add("spring.datasource.username", postgresContainer::getUsername)
            registry.add("spring.datasource.password", postgresContainer::getPassword)
        }
    }

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Test
    fun `verify liquibase migration creates expected tables`() {
        // Check if tables were created
        val tables = listOf("tenants", "example_notes", "example_tasks", "databasechangelog", "databasechangeloglock")

        tables.forEach { tableName ->
            val count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM pg_tables WHERE tablename = ?",
                Int::class.java,
                tableName
            )
            assertTrue(count!! > 0, "Table $tableName should exist")
        }

        // Check if DATABASECHANGELOG contains our changesets
        val changeSets = jdbcTemplate.queryForList(
            "SELECT id, author, filename FROM databasechangelog ORDER BY orderexecuted",
            Map::class.java
        )

        assertTrue(changeSets.isNotEmpty(), "There should be change sets in the DATABASECHANGELOG table")

        // Check if the expected changesets are present
        val changeSetIds = changeSets.map { it["id"] as String }
        assertTrue(changeSetIds.contains("1"), "Changeset ID 1 (create tenants table) should exist")
        assertTrue(changeSetIds.contains("2"), "Changeset ID 2 (create db roles) should exist")
        assertTrue(changeSetIds.contains("3"), "Changeset ID 3 (create example tables) should exist")
        assertTrue(changeSetIds.contains("4"), "Changeset ID 4 (create RLS policies) should exist")
    }

    @Test
    fun `verify database roles were created`() {
        val roles = jdbcTemplate.queryForList(
            "SELECT rolname FROM pg_roles WHERE rolname IN ('eaf_app', 'eaf_admin')",
            String::class.java
        )

        assertTrue(roles.contains("eaf_app"), "Role 'eaf_app' should exist")
        assertTrue(roles.contains("eaf_admin"), "Role 'eaf_admin' should exist")
    }

    @Test
    fun `verify RLS is enabled on tables`() {
        val rlsTables = jdbcTemplate.queryForList(
            """
            SELECT tablename 
            FROM pg_tables t
            JOIN pg_class c ON c.relname = t.tablename
            WHERE c.relrowsecurity = true
            AND t.tablename IN ('example_notes', 'example_tasks')
            """,
            String::class.java
        )

        assertTrue(rlsTables.contains("example_notes"), "RLS should be enabled on 'example_notes' table")
        assertTrue(rlsTables.contains("example_tasks"), "RLS should be enabled on 'example_tasks' table")
    }
}
