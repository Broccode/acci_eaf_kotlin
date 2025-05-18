package com.acci.eaf.multitenancy.integration

import com.acci.eaf.core.tenant.TenantContextHolder
import com.acci.eaf.multitenancy.domain.TenantStatus
import com.acci.eaf.multitenancy.dto.CreateTenantDto
import com.acci.eaf.multitenancy.service.TenantService
import java.time.OffsetDateTime
import java.util.UUID
import javax.sql.DataSource
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class RlsIntegrationTest {

    companion object {
        @Container
        val postgresContainer = PostgreSQLContainer<Nothing>("postgres:14").apply {
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
            registry.add("spring.liquibase.enabled") { "true" }
        }
    }

    @Autowired
    private lateinit var tenantService: TenantService

    @Autowired
    private lateinit var dataSource: DataSource

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    private lateinit var tenant1Id: UUID
    private lateinit var tenant2Id: UUID

    @BeforeEach
    fun setUp() {
        // Create test tenants
        val tenant1 = tenantService.createTenant(
            CreateTenantDto("test-tenant-1-${UUID.randomUUID()}", TenantStatus.ACTIVE)
        )
        val tenant2 = tenantService.createTenant(
            CreateTenantDto("test-tenant-2-${UUID.randomUUID()}", TenantStatus.ACTIVE)
        )

        tenant1Id = tenant1.tenantId
        tenant2Id = tenant2.tenantId

        // Insert test data for both tenants
        insertTestData()

        // Create database users for testing BYPASSRLS
        createDatabaseUsers()
    }

    @AfterEach
    fun tearDown() {
        // Clear tenant context and cleanup test data
        TenantContextHolder.clear()
        cleanupTestData()
    }

    private fun insertTestData() {
        // Insert data for tenant 1
        TenantContextHolder.setTenantId(tenant1Id)
        insertExampleNote("Tenant 1 Note 1", "This note belongs to tenant 1")
        insertExampleNote("Tenant 1 Note 2", "Another note for tenant 1")
        insertExampleTask("Tenant 1 Task 1", "Task for tenant 1", "IN_PROGRESS")
        TenantContextHolder.clear()

        // Insert data for tenant 2
        TenantContextHolder.setTenantId(tenant2Id)
        insertExampleNote("Tenant 2 Note 1", "This note belongs to tenant 2")
        insertExampleTask("Tenant 2 Task 1", "Task for tenant 2", "OPEN")
        insertExampleTask("Tenant 2 Task 2", "Another task for tenant 2", "COMPLETED")
        TenantContextHolder.clear()
    }

    private fun createDatabaseUsers() {
        jdbcTemplate.execute(
            """
            DO $$
            BEGIN
                -- Create test users if they don't exist
                IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'test_app_user') THEN
                    CREATE ROLE test_app_user LOGIN PASSWORD 'app_password';
                    GRANT eaf_app TO test_app_user;
                END IF;
                
                IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'test_admin_user') THEN
                    CREATE ROLE test_admin_user LOGIN PASSWORD 'admin_password';
                    GRANT eaf_admin TO test_admin_user;
                END IF;
                
                -- Grant necessary privileges
                GRANT USAGE ON SCHEMA public TO test_app_user, test_admin_user;
                GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO test_app_user, test_admin_user;
                GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO test_app_user, test_admin_user;
            END
            $$;
            """.trimIndent()
        )
    }

    private fun cleanupTestData() {
        // Direct cleanup bypassing RLS for test cleanup
        jdbcTemplate.execute("DELETE FROM example_notes")
        jdbcTemplate.execute("DELETE FROM example_tasks")
    }

    private fun insertExampleNote(title: String, content: String) {
        val id = UUID.randomUUID()
        val sql = """
            INSERT INTO example_notes (id, tenant_id, title, content, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?)
        """.trimIndent()

        val now = OffsetDateTime.now()

        jdbcTemplate.update(sql, id, TenantContextHolder.getCurrentTenantId(), title, content, now, now)
    }

    private fun insertExampleTask(
        name: String,
        description: String,
        status: String,
    ) {
        val id = UUID.randomUUID()
        val sql = """
            INSERT INTO example_tasks (id, tenant_id, name, description, status, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()

        val now = OffsetDateTime.now()

        jdbcTemplate.update(sql, id, TenantContextHolder.getCurrentTenantId(), name, description, status, now, now)
    }

    /**
     * Creates a new JdbcTemplate using a specific database user for testing RLS bypass
     */
    private fun createJdbcTemplateForUser(username: String, password: String): JdbcTemplate {
        val ds = DriverManagerDataSource().apply {
            setDriverClassName("org.postgresql.Driver")
            url = postgresContainer.jdbcUrl
            this.username = username
            this.password = password
        }
        return JdbcTemplate(ds)
    }

    @Test
    fun `test SELECT with RLS - should only return current tenant data`() {
        // Set context to tenant 1
        TenantContextHolder.setTenantId(tenant1Id)

        // Query notes
        val tenant1Notes = jdbcTemplate.queryForList("SELECT * FROM example_notes")

        // Assert only tenant 1 notes are returned
        assertEquals(2, tenant1Notes.size)
        tenant1Notes.forEach { note ->
            assertEquals(tenant1Id, note["tenant_id"])
        }

        // Change context to tenant 2
        TenantContextHolder.setTenantId(tenant2Id)

        // Query notes again
        val tenant2Notes = jdbcTemplate.queryForList("SELECT * FROM example_notes")

        // Assert only tenant 2 notes are returned
        assertEquals(1, tenant2Notes.size)
        tenant2Notes.forEach { note ->
            assertEquals(tenant2Id, note["tenant_id"])
        }
    }

    @Test
    fun `test INSERT with RLS - should prevent inserting with incorrect tenant_id`() {
        // Set context to tenant 1
        TenantContextHolder.setTenantId(tenant1Id)

        // Try to insert a record with tenant2's ID (violates RLS policy)
        val noteId = UUID.randomUUID()
        val now = OffsetDateTime.now()

        // This should fail because the tenant_id doesn't match the current tenant context
        assertThrows<DataIntegrityViolationException> {
            jdbcTemplate.update(
                """
                INSERT INTO example_notes (id, tenant_id, title, content, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?)
                """.trimIndent(),
                noteId, tenant2Id, "Cross-Tenant Insert Attempt", "This should fail", now, now
            )
        }

        // Verify with correct tenant_id works
        val validNoteId = UUID.randomUUID()
        jdbcTemplate.update(
            """
            INSERT INTO example_notes (id, tenant_id, title, content, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            validNoteId, tenant1Id, "Valid Insert", "This should succeed", now, now
        )

        // Query the inserted note to verify it exists
        val insertedNote = jdbcTemplate.queryForMap("SELECT * FROM example_notes WHERE id = ?", validNoteId)
        assertEquals(tenant1Id, insertedNote["tenant_id"])
    }

    @Test
    fun `test UPDATE with RLS - should only update current tenant data`() {
        // Set context to tenant 1 and get a note id
        TenantContextHolder.setTenantId(tenant1Id)
        val tenant1NoteId = jdbcTemplate.queryForObject(
            "SELECT id FROM example_notes LIMIT 1",
            UUID::class.java
        )

        // Change to tenant 2 context
        TenantContextHolder.setTenantId(tenant2Id)

        // Try to update tenant 1's note
        val updateCount = jdbcTemplate.update(
            "UPDATE example_notes SET title = 'Attempted Cross-Tenant Update' WHERE id = ?",
            tenant1NoteId
        )

        // Verify no rows were updated
        assertEquals(0, updateCount)

        // Switch back to tenant 1
        TenantContextHolder.setTenantId(tenant1Id)

        // Verify the note was not updated
        val note = jdbcTemplate.queryForMap("SELECT title FROM example_notes WHERE id = ?", tenant1NoteId)
        assertNotEquals("Attempted Cross-Tenant Update", note["title"])
    }

    @Test
    fun `test DELETE with RLS - should only delete current tenant data`() {
        // Set context to tenant 1 and get a note id
        TenantContextHolder.setTenantId(tenant1Id)
        val tenant1NoteId = jdbcTemplate.queryForObject(
            "SELECT id FROM example_notes LIMIT 1",
            UUID::class.java
        )

        // Change to tenant 2 context
        TenantContextHolder.setTenantId(tenant2Id)

        // Try to delete tenant 1's note
        val deleteCount = jdbcTemplate.update(
            "DELETE FROM example_notes WHERE id = ?",
            tenant1NoteId
        )

        // Verify no rows were deleted
        assertEquals(0, deleteCount)

        // Switch back to tenant 1 and verify the note still exists
        TenantContextHolder.setTenantId(tenant1Id)
        val noteExists = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM example_notes WHERE id = ?",
            Int::class.java,
            tenant1NoteId
        )

        assertEquals(1, noteExists)
    }

    @Test
    fun `test no tenant context - should not see any data`() {
        // Clear tenant context
        TenantContextHolder.clear()

        // Query notes without tenant context
        val notes = jdbcTemplate.queryForList("SELECT * FROM example_notes")

        // Verify no data is returned
        assertEquals(0, notes.size)

        // Query tasks without tenant context
        val tasks = jdbcTemplate.queryForList("SELECT * FROM example_tasks")

        // Verify no data is returned
        assertEquals(0, tasks.size)
    }

    @Test
    fun `test cross-tenant JOIN - should only return data for current tenant`() {
        // First insert some related data
        TenantContextHolder.setTenantId(tenant1Id)
        val noteId = UUID.randomUUID()
        val taskId = UUID.randomUUID()
        val now = OffsetDateTime.now()

        // Insert note and task with same ID prefix to test join
        jdbcTemplate.update(
            """
            INSERT INTO example_notes (id, tenant_id, title, content, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            noteId, tenant1Id, "Related Note", "Note related to task", now, now
        )

        jdbcTemplate.update(
            """
            INSERT INTO example_tasks (id, tenant_id, name, description, status, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            taskId, tenant1Id, "Related Task", "Task related to note", "OPEN", now, now
        )

        // Insert similar data for tenant 2
        TenantContextHolder.setTenantId(tenant2Id)
        val noteId2 = UUID.randomUUID()
        val taskId2 = UUID.randomUUID()

        jdbcTemplate.update(
            """
            INSERT INTO example_notes (id, tenant_id, title, content, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            noteId2, tenant2Id, "Related Note T2", "Note related to task T2", now, now
        )

        jdbcTemplate.update(
            """
            INSERT INTO example_tasks (id, tenant_id, name, description, status, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            taskId2, tenant2Id, "Related Task T2", "Task related to note T2", "OPEN", now, now
        )

        // Query with JOIN as tenant 1
        TenantContextHolder.setTenantId(tenant1Id)
        val joinResults = jdbcTemplate.queryForList(
            """
            SELECT n.title as note_title, t.name as task_name 
            FROM example_notes n
            JOIN example_tasks t ON t.tenant_id = n.tenant_id
            WHERE n.title LIKE 'Related%' AND t.name LIKE 'Related%'
            """.trimIndent()
        )

        // Should only see tenant 1's data
        assertEquals(1, joinResults.size)
        assertEquals("Related Note", joinResults[0]["note_title"])
        assertEquals("Related Task", joinResults[0]["task_name"])
    }

    @Test
    fun `test BYPASSRLS - admin role should be able to see all tenant data`() {
        // Create JDBC templates for both user types
        val appUserJdbc = createJdbcTemplateForUser("test_app_user", "app_password")
        val adminUserJdbc = createJdbcTemplateForUser("test_admin_user", "admin_password")

        // 1. First test that app user with tenant 1 context can only see tenant 1 data
        jdbcTemplate.execute("SET app.current_tenant_id TO '$tenant1Id'")
        val appUserNotes = appUserJdbc.queryForList("SELECT * FROM example_notes")

        assertEquals(2, appUserNotes.size)
        appUserNotes.forEach { note ->
            assertEquals(tenant1Id, note["tenant_id"])
        }

        // 2. Test that admin user can see ALL tenants' data regardless of tenant context
        jdbcTemplate.execute("SET app.current_tenant_id TO '$tenant1Id'")
        val adminNotes = adminUserJdbc.queryForList("SELECT * FROM example_notes")

        // Should see both tenant 1's (2) and tenant 2's (1) notes = 3 total
        assertEquals(3, adminNotes.size)

        // Verify we have notes from both tenants in the results
        val tenant1NoteCount = adminNotes.count { it["tenant_id"] == tenant1Id }
        val tenant2NoteCount = adminNotes.count { it["tenant_id"] == tenant2Id }

        assertEquals(2, tenant1NoteCount, "Admin should see all 2 notes from tenant 1")
        assertEquals(1, tenant2NoteCount, "Admin should see the 1 note from tenant 2")

        // 3. Test that admin can perform cross-tenant operations
        // Try to update a tenant 1 note while in tenant 2 context
        val tenant1NoteId = jdbcTemplate.queryForObject(
            "SELECT id FROM example_notes WHERE tenant_id = ? LIMIT 1",
            UUID::class.java,
            tenant1Id
        )

        jdbcTemplate.execute("SET app.current_tenant_id TO '$tenant2Id'")

        // When admin tries to update a note from tenant 1 while in tenant 2 context
        val updateCount = adminUserJdbc.update(
            "UPDATE example_notes SET title = 'Admin Cross-Tenant Update' WHERE id = ?",
            tenant1NoteId
        )

        // Verify admin could update across tenant boundaries
        assertEquals(1, updateCount, "Admin should be able to update data across tenant boundaries")

        // Check that the update was successful
        val updatedNoteTitle = adminUserJdbc.queryForObject(
            "SELECT title FROM example_notes WHERE id = ?",
            String::class.java,
            tenant1NoteId
        )

        assertEquals(
            "Admin Cross-Tenant Update", updatedNoteTitle,
            "Admin should have successfully updated the note title across tenant boundaries"
        )
    }
}
