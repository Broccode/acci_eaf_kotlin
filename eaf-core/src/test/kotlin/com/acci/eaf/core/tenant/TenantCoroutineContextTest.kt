package com.acci.eaf.core.tenant

import java.util.UUID
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class TenantCoroutineContextTest {

    @AfterEach
    fun tearDown() {
        // Clear the tenant context after each test
        TenantContextHolder.clear()
    }

    @Test
    fun `should propagate tenant ID in coroutine context`() =
        runTest {
            // Given
            val tenantId = UUID.randomUUID()
            TenantContextHolder.setTenantId(tenantId)

            // When
            val tenantContext = TenantCoroutineContext.capture()
            val result = withContext(Dispatchers.Default + tenantContext) {
                TenantContextHolder.getCurrentTenantId()
            }

            // Then
            assertEquals(tenantId, result)
        }

    @Test
    fun `should not propagate tenant ID without explicit context element`() =
        runTest {
            // Given
            val tenantId = UUID.randomUUID()
            TenantContextHolder.setTenantId(tenantId)

            // When
            val result = withContext(Dispatchers.Default) {
                TenantContextHolder.getCurrentTenantId()
            }

            // Then
            assertNull(result)
        }

    @Test
    fun `should propagate tenant ID in nested coroutines`() =
        runTest {
            // Given
            val tenantId = UUID.randomUUID()
            TenantContextHolder.setTenantId(tenantId)
            val tenantContext = TenantCoroutineContext.capture()

            // When
            val result = withContext(Dispatchers.Default + tenantContext) {
                withContext(Dispatchers.IO) {
                    TenantContextHolder.getCurrentTenantId()
                }
            }

            // Then
            assertEquals(tenantId, result)
        }

    @Test
    fun `should capture null tenant ID when none is set`() =
        runTest {
            // Given
            // No tenant ID set

            // When
            val tenantContext = TenantCoroutineContext.capture()
            val result = withContext(Dispatchers.Default + tenantContext) {
                TenantContextHolder.getCurrentTenantId()
            }

            // Then
            assertNull(result)
        }

    @Test
    fun `should restore previous tenant ID after coroutine execution`() =
        runTest {
            // Given
            val originalTenantId = UUID.randomUUID()
            TenantContextHolder.setTenantId(originalTenantId)

            val nestedTenantId = UUID.randomUUID()
            val tenantContext = TenantCoroutineContext(nestedTenantId)

            // When
            withContext(Dispatchers.Default + tenantContext) {
                // Nested coroutine should have nestedTenantId
                assertEquals(nestedTenantId, TenantContextHolder.getCurrentTenantId())
            }

            // Then
            // After coroutine completes, original context should be restored
            assertEquals(originalTenantId, TenantContextHolder.getCurrentTenantId())
        }

    @Test
    fun `should work with multiple concurrent coroutines`() =
        runBlocking {
            // Given
            val mainTenantId = UUID.randomUUID()
            val coroutine1TenantId = UUID.randomUUID()
            val coroutine2TenantId = UUID.randomUUID()

            TenantContextHolder.setTenantId(mainTenantId)

            val result1 = AtomicReference<UUID?>()
            val result2 = AtomicReference<UUID?>()

            // When
            val job1 = launch(Dispatchers.Default + TenantCoroutineContext(coroutine1TenantId)) {
                result1.set(TenantContextHolder.getCurrentTenantId())
            }

            val job2 = launch(Dispatchers.Default + TenantCoroutineContext(coroutine2TenantId)) {
                result2.set(TenantContextHolder.getCurrentTenantId())
            }

            job1.join()
            job2.join()

            // Then
            assertEquals(coroutine1TenantId, result1.get())
            assertEquals(coroutine2TenantId, result2.get())
            assertEquals(mainTenantId, TenantContextHolder.getCurrentTenantId())
        }

    @Test
    fun `should propagate tenant ID to async operations`() =
        runTest {
            // Given
            val tenantId = UUID.randomUUID()
            TenantContextHolder.setTenantId(tenantId)
            val tenantContext = TenantCoroutineContext.capture()

            // When
            val deferred = async(Dispatchers.Default + tenantContext) {
                TenantContextHolder.getCurrentTenantId()
            }

            // Then
            assertEquals(tenantId, deferred.await())
        }
}
