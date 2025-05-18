package com.acci.eaf.core.tenant

import com.acci.eaf.core.interfaces.TenantInfo
import com.acci.eaf.core.interfaces.TenantServiceApi
import com.acci.eaf.core.interfaces.TenantStatus
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor

@ExtendWith(MockitoExtension::class)
class TenantContextPropagationIntegrationTest {

    @Mock
    private lateinit var tenantService: TenantServiceApi

    private lateinit var tenantContextInterceptor: TenantContextInterceptor
    private lateinit var tenantAwareTaskExecutor: ThreadPoolTaskExecutor
    private lateinit var request: MockHttpServletRequest
    private lateinit var response: MockHttpServletResponse

    @BeforeEach
    fun setUp() {
        tenantContextInterceptor = TenantContextInterceptor(tenantService)

        tenantAwareTaskExecutor = ThreadPoolTaskExecutor()
        tenantAwareTaskExecutor.corePoolSize = 5
        tenantAwareTaskExecutor.maxPoolSize = 10
        tenantAwareTaskExecutor.queueCapacity = 25
        tenantAwareTaskExecutor.setThreadNamePrefix("tenant-exec-")
        tenantAwareTaskExecutor.setTaskDecorator(TenantTaskDecorator())
        tenantAwareTaskExecutor.initialize()

        request = MockHttpServletRequest()
        response = MockHttpServletResponse()
    }

    @AfterEach
    fun tearDown() {
        TenantContextHolder.clear()
        tenantAwareTaskExecutor.shutdown()
    }

    @Test
    fun `should propagate tenant context through entire request lifecycle`() {
        // Given
        val tenantId = UUID.randomUUID()
        request.addHeader(TenantContextInterceptor.TENANT_HEADER, tenantId.toString())

        val activeTenant = TenantInfo(
            tenantId = tenantId,
            status = TenantStatus.ACTIVE
        )

        `when`(tenantService.getTenantById(tenantId)).thenReturn(activeTenant)

        // When - Simulate request handling
        val interceptorResult = tenantContextInterceptor.preHandle(request, response, Any())

        // Then - Verify context is set and interceptor allowed the request
        assertTrue(interceptorResult)
        assertEquals(tenantId, TenantContextHolder.getCurrentTenantId())

        // When - Simulate business logic in a service
        val serviceResult = simulateServiceCall()

        // Then - Verify tenant ID is correctly accessible in service
        assertEquals(tenantId, serviceResult)

        // When - Simulate async operation
        val asyncResult = CompletableFuture<UUID?>()
        tenantAwareTaskExecutor.submit {
            asyncResult.complete(TenantContextHolder.getCurrentTenantId())
        }

        // Then - Verify tenant ID is propagated to async thread
        assertEquals(tenantId, asyncResult.get(1, TimeUnit.SECONDS))

        // When - Simulate coroutine operations
        val coroutineResult = runBlocking {
            withContext(Dispatchers.Default + TenantCoroutineContext.capture()) {
                TenantContextHolder.getCurrentTenantId()
            }
        }

        // Then - Verify tenant ID is propagated to coroutine
        assertEquals(tenantId, coroutineResult)

        // When - Simulate request completion
        tenantContextInterceptor.afterCompletion(request, response, Any(), null)

        // Then - Verify context is cleared
        assertNull(TenantContextHolder.getCurrentTenantId())
    }

    @Test
    fun `should handle complex nested async and coroutine operations`() {
        // Given
        val tenantId = UUID.randomUUID()
        TenantContextHolder.setTenantId(tenantId)

        val latch = CountDownLatch(3) // Wait for 3 operations
        val results = mutableListOf<UUID?>()

        // When - Simulate complex nested operations with coroutines and async
        runBlocking {
            // Capture tenant context
            val tenantContext = TenantCoroutineContext.capture()

            // Start coroutine with tenant context
            launch(Dispatchers.Default + tenantContext) {
                // Start async operation from coroutine
                val asyncJob = async {
                    val asyncResult = CompletableFuture<UUID?>()

                    // Execute in task executor
                    tenantAwareTaskExecutor.submit {
                        // Capture result from task executor thread
                        results.add(TenantContextHolder.getCurrentTenantId())
                        latch.countDown()

                        // Start another coroutine from task executor
                        runBlocking {
                            withContext(Dispatchers.IO + TenantCoroutineContext.capture()) {
                                results.add(TenantContextHolder.getCurrentTenantId())
                                latch.countDown()

                                // Complete future with result
                                asyncResult.complete(TenantContextHolder.getCurrentTenantId())
                            }
                        }
                    }

                    // Wait for async result
                    asyncResult.get(2, TimeUnit.SECONDS)
                }

                // Capture result from initial coroutine
                results.add(TenantContextHolder.getCurrentTenantId())
                latch.countDown()

                // Wait for async job to complete
                asyncJob.await()
            }
        }

        // Then
        assertTrue(latch.await(3, TimeUnit.SECONDS))

        // All results should have the tenant ID
        for (result in results) {
            assertEquals(tenantId, result)
        }
    }

    private fun simulateServiceCall(): UUID? {
        // Simulate some business logic in a service
        return TenantContextHolder.getCurrentTenantId()
    }
} 
