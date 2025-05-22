package com.acci.eaf.iam.config

import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Primary
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

/**
 * Testkonfiguration für Integration-Tests im IAM-Modul.
 *
 * Diese Konfiguration sorgt dafür, dass Spring Boot bei Tests die nötigen Komponenten,
 * Repositories und Entities aus dem IAM-Modul findet und korrekt initialisiert.
 */
@SpringBootApplication
@EnableAutoConfiguration
@EntityScan(basePackages = ["com.acci.eaf.iam.domain.model"])
@EnableJpaRepositories(basePackages = ["com.acci.eaf.iam.adapter.persistence"])
@ComponentScan(basePackages = ["com.acci.eaf.iam"])
class IamTestApplication {

    /**
     * Stellt einen einfachen PasswordEncoder für Tests bereit.
     * Im Gegensatz zum produktiven PasswordEncoder wird hier ein einfacher BCrypt-Encoder
     * mit niedriger Strength verwendet, um die Tests zu beschleunigen.
     */
    @Bean
    @Primary
    fun testPasswordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder(4) // Niedrige Strength für schnellere Tests
    }
}
