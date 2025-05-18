package com.acci.eaf.controlplane.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

/**
 * Main application class for the EAF Control Plane API.
 * This API provides CRUD operations for tenant management.
 */
@SpringBootApplication
@ComponentScan(basePackages = ["com.acci.eaf"])
@EntityScan(basePackages = ["com.acci.eaf.multitenancy.domain"])
@EnableJpaRepositories(basePackages = ["com.acci.eaf.multitenancy.repository"])
class EafControlPlaneApiApplication

/**
 * Main function to start the EAF Control Plane API application.
 */
fun main(args: Array<String>) {
    runApplication<EafControlPlaneApiApplication>(*args)
} 
