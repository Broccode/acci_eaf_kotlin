package com.acci.eaf.core.tenant

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

@Execution(ExecutionMode.SAME_THREAD) // Ensure tests run in the same thread to prevent ThreadLocal leakage
class TenantContextHolderTest {
    
    @AfterEach
    fun tearDown() {
        // Clear the tenant context after each test
        TenantContextHolder.clear()
    }
    
    @Test
    fun `should set and get tenant ID correctly`() {
        // Given
        val tenantId = UUID.randomUUID()
        
        // When
        TenantContextHolder.setTenantId(tenantId)
        
        // Then
        assertEquals(tenantId, TenantContextHolder.getCurrentTenantId())
        assertTrue(TenantContextHolder.hasTenantId())
    }
    
    @Test
    fun `should return null when tenant ID is not set`() {
        // When no tenant ID is set
        
        // Then
        assertNull(TenantContextHolder.getCurrentTenantId())
        assertFalse(TenantContextHolder.hasTenantId())
    }
    
    @Test
    fun `should clear tenant ID correctly`() {
        // Given
        val tenantId = UUID.randomUUID()
        TenantContextHolder.setTenantId(tenantId)
        
        // When
        TenantContextHolder.clear()
        
        // Then
        assertNull(TenantContextHolder.getCurrentTenantId())
        assertFalse(TenantContextHolder.hasTenantId())
    }
    
    @Test
    fun `should isolate tenant ID between threads`() {
        // Given
        val mainThreadTenantId = UUID.randomUUID()
        TenantContextHolder.setTenantId(mainThreadTenantId)
        
        val childThreadTenantId = AtomicReference<UUID?>()
        val latch = CountDownLatch(1)
        
        // When
        val executor = Executors.newSingleThreadExecutor()
        executor.submit {
            try {
                // Read tenant ID from child thread (should be null)
                childThreadTenantId.set(TenantContextHolder.getCurrentTenantId())
            } finally {
                latch.countDown()
            }
        }
        
        // Then
        assertTrue(latch.await(1, TimeUnit.SECONDS))
        executor.shutdown()
        
        assertEquals(mainThreadTenantId, TenantContextHolder.getCurrentTenantId(), "Main thread should keep its tenant ID")
        assertNull(childThreadTenantId.get(), "Child thread should not inherit tenant ID")
    }
    
    @Test
    fun `should allow separate tenant IDs in different threads`() {
        // Given
        val mainThreadTenantId = UUID.randomUUID()
        val childThreadTenantId = UUID.randomUUID()
        
        TenantContextHolder.setTenantId(mainThreadTenantId)
        
        val childTenantIdAfterSet = AtomicReference<UUID?>()
        val latch = CountDownLatch(1)
        
        // When
        val executor = Executors.newSingleThreadExecutor()
        executor.submit {
            try {
                // Set a different tenant ID in child thread
                TenantContextHolder.setTenantId(childThreadTenantId)
                childTenantIdAfterSet.set(TenantContextHolder.getCurrentTenantId())
            } finally {
                latch.countDown()
            }
        }
        
        // Then
        assertTrue(latch.await(1, TimeUnit.SECONDS))
        executor.shutdown()
        
        assertEquals(mainThreadTenantId, TenantContextHolder.getCurrentTenantId(), "Main thread should keep its tenant ID")
        assertEquals(childThreadTenantId, childTenantIdAfterSet.get(), "Child thread should have its own tenant ID")
    }
} 