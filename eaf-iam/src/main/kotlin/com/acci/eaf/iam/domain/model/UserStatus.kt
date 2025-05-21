package com.acci.eaf.iam.domain.model

/**
 * Repräsentiert den Status eines Benutzers im IAM-System.
 * Der Status bestimmt, ob ein Benutzer sich anmelden kann und welche Aktionen er ausführen darf.
 */
enum class UserStatus {
    /**
     * Der Benutzer ist aktiv und kann sich anmelden.
     */
    ACTIVE,

    /**
     * Der Benutzer wurde erstellt, aber die E-Mail-Adresse wurde noch nicht verifiziert.
     * Eine Anmeldung ist erst nach Verifikation möglich.
     */
    EMAIL_VERIFICATION_PENDING,

    /**
     * Der Benutzer wurde von einem Administrator gesperrt.
     * Eine Anmeldung ist nicht möglich, bis der Administrator die Sperre aufhebt.
     */
    LOCKED_BY_ADMIN,

    /**
     * Der Benutzer wurde von einem Administrator deaktiviert.
     * Eine Anmeldung ist nicht möglich, bis der Administrator den Benutzer wieder aktiviert.
     */
    DISABLED_BY_ADMIN,

    /**
     * Das Passwort des Benutzers ist abgelaufen.
     * Der Benutzer muss sein Passwort ändern, bevor er sich wieder anmelden kann.
     */
    PASSWORD_EXPIRED,

    /**
     * Der Benutzer wurde vom System gesperrt, aufgrund zu vieler fehlgeschlagener Anmeldeversuche.
     * Eine Anmeldung ist erst nach Ablauf der Sperrzeit wieder möglich.
     */
    LOCKED_BY_SYSTEM,
}
