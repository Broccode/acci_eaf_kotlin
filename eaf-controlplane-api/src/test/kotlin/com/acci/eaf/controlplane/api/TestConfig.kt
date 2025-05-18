package com.acci.eaf.controlplane.api

import org.mockito.Mockito.mock
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder

/**
 * Test configuration class for the controlplane API.
 * Provides mocked beans for testing.
 */
@TestConfiguration
class TestConfig {

    /**
     * Provides a mocked JWT decoder for testing security.
     */
    @Bean
    fun jwtDecoder(): JwtDecoder {
        return mock(NimbusJwtDecoder::class.java)
    }
} 
