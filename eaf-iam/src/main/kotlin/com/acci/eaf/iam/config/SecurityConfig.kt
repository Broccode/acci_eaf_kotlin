package com.acci.eaf.iam.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.DelegatingPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder
import org.springframework.security.crypto.scrypt.SCryptPasswordEncoder

/**
 * Konfigurationsklasse für sicherheitsrelevante Aspekte des IAM-Moduls.
 *
 * Diese Klasse stellt sicher, dass sichere Passwort-Hashing-Algorithmen verwendet werden
 * und ermöglicht die nahtlose Migration zwischen verschiedenen Algorithmen durch die
 * Verwendung des DelegatingPasswordEncoder.
 */
@Configuration
class SecurityConfig {

    /**
     * Konfiguriert einen [DelegatingPasswordEncoder], der mehrere sichere
     * Hashing-Algorithmen unterstützt und einen optimalen Algorithmus als Standard verwendet.
     *
     * Nutzt aktuell Argon2id als Standard-Algorithmus, mit BCrypt, PBKDF2 und SCrypt als Fallback-Optionen.
     * Diese Konfiguration ermöglicht die nahtlose Migration zwischen verschiedenen Algorithmen
     * und bietet Zukunftssicherheit bei Entdeckung von Schwachstellen in einzelnen Algorithmen.
     *
     * @return der konfigurierte [PasswordEncoder] für die Anwendung
     */
    @Bean
    fun passwordEncoder(): PasswordEncoder {
        // Definiere die verfügbaren Encoder
        val encoders = mutableMapOf<String, PasswordEncoder>()

        // Argon2id - aktuell empfohlener Standard für hohe Sicherheit
        // Einstellungen: 16MB RAM, 2 Iterationen, 1 Parallelismus
        encoders["argon2id"] = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8()

        // BCrypt - weit verbreitet, gut unterstützt, Stärke 12
        encoders["bcrypt"] = BCryptPasswordEncoder(12)

        // PBKDF2 - FIPS 140-2/3 konform, wichtig für einige regulierte Umgebungen
        encoders["pbkdf2"] = Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_8()

        // SCrypt - Memory-Hard-Funktion, komplexer zu knacken
        encoders["scrypt"] = SCryptPasswordEncoder.defaultsForSpringSecurity_v5_8()

        // Nutze DelegatingPasswordEncoder mit Argon2id als Standard
        return DelegatingPasswordEncoder("argon2id", encoders)
    }
}
