package com.acci.eaf.iam.config

import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

/**
 * Testkonfiguration für Integration-Tests im IAM-Modul.
 *
 * Diese Konfiguration sorgt dafür, dass Spring Boot bei Tests die nötigen Komponenten,
 * Repositories und Entities aus dem IAM-Modul findet und korrekt initialisiert.
 */
@SpringBootApplication
@EnableAutoConfiguration
@EntityScan(basePackages = ["com.acci.eaf.iam.domain.model"])
@EnableJpaRepositories(basePackages = ["com.acci.eaf.iam.application.port.out"])
@ComponentScan(basePackages = ["com.acci.eaf.iam"])
class TestConfig
