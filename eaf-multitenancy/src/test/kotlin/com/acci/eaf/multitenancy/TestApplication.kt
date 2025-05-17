package com.acci.eaf.multitenancy

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

/**
 * Test application configuration for integration tests.
 */
@SpringBootApplication
@EnableJpaRepositories(basePackages = ["com.acci.eaf.multitenancy.repository"])
@EntityScan(basePackages = ["com.acci.eaf.multitenancy.domain"])
class TestApplication

fun main(args: Array<String>) {
    runApplication<TestApplication>(*args)
} 