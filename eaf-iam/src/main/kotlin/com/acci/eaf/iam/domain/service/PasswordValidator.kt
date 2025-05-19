package com.acci.eaf.iam.domain.service

import com.acci.eaf.iam.config.PasswordPolicyConfig
import org.springframework.stereotype.Component

/**
 * Validierungskomponente für Passwörter, die die Einhaltung der konfigurierten
 * Komplexitätsregeln überprüft.
 */
@Component
class PasswordValidator(private val passwordPolicy: PasswordPolicyConfig) {

    /**
     * Validiert ein Passwort gegen die konfigurierten Komplexitätsregeln.
     *
     * @param password Das zu validierende Passwort
     * @return Eine ValidationResult Instanz, die das Ergebnis der Validierung enthält
     */
    fun validate(password: String): ValidationResult {
        val errors = mutableListOf<String>()

        // Überprüfe die Passwortlänge
        if (password.length < passwordPolicy.minLength) {
            errors.add("Das Passwort muss mindestens ${passwordPolicy.minLength} Zeichen lang sein.")
        }

        // Überprüfe, ob Großbuchstaben erforderlich sind
        if (passwordPolicy.requireUppercase && !password.any { it.isUpperCase() }) {
            errors.add("Das Passwort muss mindestens einen Großbuchstaben enthalten.")
        }

        // Überprüfe, ob Kleinbuchstaben erforderlich sind
        if (passwordPolicy.requireLowercase && !password.any { it.isLowerCase() }) {
            errors.add("Das Passwort muss mindestens einen Kleinbuchstaben enthalten.")
        }

        // Überprüfe, ob Ziffern erforderlich sind
        if (passwordPolicy.requireDigit && !password.any { it.isDigit() }) {
            errors.add("Das Passwort muss mindestens eine Ziffer enthalten.")
        }

        // Überprüfe, ob Sonderzeichen erforderlich sind
        if (passwordPolicy.requireSpecialChar && !password.any { passwordPolicy.specialChars.contains(it) }) {
            errors.add("Das Passwort muss mindestens ein Sonderzeichen enthalten (${passwordPolicy.specialChars}).")
        }

        // Überprüfe auf zu viele aufeinanderfolgende identische Zeichen
        if (hasConsecutiveIdenticalChars(password, passwordPolicy.maxConsecutiveIdenticalChars)) {
            errors.add("Das Passwort darf nicht mehr als ${passwordPolicy.maxConsecutiveIdenticalChars} identische Zeichen hintereinander enthalten.")
        }

        return if (errors.isEmpty()) {
            ValidationResult(true, emptyList())
        } else {
            ValidationResult(false, errors)
        }
    }

    /**
     * Überprüft, ob ein Passwort zu viele aufeinanderfolgende identische Zeichen enthält.
     *
     * @param password Das zu überprüfende Passwort
     * @param maxConsecutive Die maximale Anzahl aufeinanderfolgender identischer Zeichen
     * @return true, wenn das Passwort zu viele aufeinanderfolgende identische Zeichen enthält
     */
    private fun hasConsecutiveIdenticalChars(password: String, maxConsecutive: Int): Boolean {
        if (password.length <= maxConsecutive) {
            return false
        }

        var consecutiveCount = 1
        var previousChar = password[0]

        for (i in 1 until password.length) {
            if (password[i] == previousChar) {
                consecutiveCount++
                if (consecutiveCount > maxConsecutive) {
                    return true
                }
            } else {
                consecutiveCount = 1
                previousChar = password[i]
            }
        }

        return false
    }

    /**
     * Datenklasse zur Repräsentation des Validierungsergebnisses.
     *
     * @property valid Gibt an, ob das Passwort gültig ist
     * @property errors Liste der Fehler, wenn das Passwort ungültig ist
     */
    data class ValidationResult(val valid: Boolean, val errors: List<String>)
}
