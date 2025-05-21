package com.acci.eaf.iam.application.port.input

import com.acci.eaf.iam.domain.model.UserStatus
import java.util.UUID
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

/**
 * Service-Interface für die Verwaltung von Benutzern.
 *
 * Dieses Interface definiert die Kernfunktionen für die Benutzerverwaltung,
 * wie Erstellen, Abrufen, Aktualisieren und Suchen von Benutzern.
 */
interface UserService {

    /**
     * Erstellt einen neuen Benutzer mit den angegebenen Daten.
     *
     * @param command das Command mit den Benutzerdaten
     * @return das DTO des erstellten Benutzers
     * @throws UsernameTakenException wenn der Benutzername im Tenant bereits vergeben ist
     * @throws PasswordComplexityException wenn das Passwort die Komplexitätsanforderungen nicht erfüllt
     */
    fun createLocalUser(command: CreateUserCommand): UserDto

    /**
     * Ruft einen Benutzer anhand seiner ID ab.
     *
     * @param userId die ID des gesuchten Benutzers
     * @return das DTO des Benutzers, falls gefunden
     * @throws UserNotFoundException wenn kein Benutzer mit dieser ID existiert
     */
    fun getUserById(userId: UUID): UserDto

    /**
     * Aktualisiert einen vorhandenen Benutzer.
     *
     * @param command das Command mit den zu aktualisierenden Daten
     * @return das DTO des aktualisierten Benutzers
     * @throws UserNotFoundException wenn kein Benutzer mit dieser ID existiert
     */
    fun updateUser(command: UpdateUserCommand): UserDto

    /**
     * Aktualisiert den Status eines Benutzers.
     *
     * @param userId die ID des zu aktualisierenden Benutzers
     * @param tenantId die ID des Tenants
     * @param newStatus der neue Status des Benutzers
     * @return das DTO des aktualisierten Benutzers
     * @throws UserNotFoundException wenn kein Benutzer mit dieser ID existiert
     */
    fun updateUserStatus(
        userId: UUID,
        tenantId: UUID,
        newStatus: UserStatus,
    ): UserDto

    /**
     * Setzt das Passwort eines Benutzers.
     *
     * @param command das Command mit der Benutzer-ID und dem neuen Passwort
     * @throws UserNotFoundException wenn kein Benutzer mit dieser ID existiert
     * @throws PasswordComplexityException wenn das neue Passwort die Komplexitätsanforderungen nicht erfüllt
     */
    fun setPassword(command: SetPasswordCommand)

    /**
     * Findet alle Benutzer eines Tenants.
     *
     * @param tenantId die ID des Tenants
     * @param pageable die Paginierungsparameter
     * @return eine paginierte Liste von Benutzer-DTOs
     */
    fun findUsersByTenant(tenantId: UUID, pageable: Pageable): Page<UserDto>

    /**
     * Sucht nach Benutzern in einem Tenant anhand eines Suchbegriffs.
     *
     * @param tenantId die ID des Tenants
     * @param searchTerm der Suchbegriff (wird mit Benutzername und E-Mail verglichen)
     * @param pageable die Paginierungsparameter
     * @return eine paginierte Liste von Benutzer-DTOs, die den Suchkriterien entsprechen
     */
    fun searchUsers(
        tenantId: UUID,
        searchTerm: String,
        pageable: Pageable,
    ): Page<UserDto>

    /**
     * Sucht nach Benutzern in einem Tenant anhand eines Status.
     *
     * @param tenantId die ID des Tenants
     * @param status der Status der Benutzer
     * @param pageable die Paginierungsparameter
     * @return eine paginierte Liste von Benutzer-DTOs, die den Status haben
     */
    fun findUsersByStatus(
        tenantId: UUID,
        status: UserStatus,
        pageable: Pageable,
    ): Page<UserDto>
}
