package com.acci.eaf.iam.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

/**
 * Konfigurationsklasse für Passwort-Komplexitätsregeln.
 *
 * Diese Klasse lädt Konfigurationswerte aus der application.yml Datei
 * unter dem Präfix "eaf.iam.password-policy" und stellt sie für die
 * Validierung von Passwörtern zur Verfügung.
 */
@Configuration
@ConfigurationProperties(prefix = "eaf.iam.password-policy")
class PasswordPolicyConfig {
    /**
     * Minimale Passwortlänge.
     * Standardmäßig auf 12 Zeichen gesetzt.
     */
    var minLength: Int = 12

    /**
     * Gibt an, ob das Passwort mindestens einen Großbuchstaben enthalten muss.
     * Standardmäßig aktiviert.
     */
    var requireUppercase: Boolean = true

    /**
     * Gibt an, ob das Passwort mindestens einen Kleinbuchstaben enthalten muss.
     * Standardmäßig aktiviert.
     */
    var requireLowercase: Boolean = true

    /**
     * Gibt an, ob das Passwort mindestens eine Ziffer enthalten muss.
     * Standardmäßig aktiviert.
     */
    var requireDigit: Boolean = true

    /**
     * Gibt an, ob das Passwort mindestens ein Sonderzeichen enthalten muss.
     * Standardmäßig aktiviert.
     */
    var requireSpecialChar: Boolean = true

    /**
     * Liste von Sonderzeichen, die als gültig betrachtet werden.
     * Standardmäßig auf gängige Sonderzeichen gesetzt.
     */
    var specialChars: String = "!@#$%^&*()_-+=[{]}|:;,<.>/?"

    /**
     * Maximale Anzahl von aufeinanderfolgenden identischen Zeichen.
     * Standardmäßig auf 3 Zeichen begrenzt (z.B. "aaa" würde abgelehnt).
     */
    var maxConsecutiveIdenticalChars: Int = 3

    /**
     * Anzahl der zu prüfenden häufigen Passwörter.
     * Wenn auf 0 gesetzt, wird diese Prüfung deaktiviert.
     * Standardmäßig deaktiviert, da hierfür eine externe Ressource erforderlich wäre.
     */
    var checkCommonPasswords: Int = 0
}
