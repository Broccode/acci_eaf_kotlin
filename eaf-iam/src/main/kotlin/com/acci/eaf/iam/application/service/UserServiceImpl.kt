package com.acci.eaf.iam.application.service

import com.acci.eaf.iam.application.port.input.CreateUserCommand
import com.acci.eaf.iam.application.port.input.SetPasswordCommand
import com.acci.eaf.iam.application.port.input.UpdateUserCommand
import com.acci.eaf.iam.application.port.input.UserDto
import com.acci.eaf.iam.application.port.input.UserService
import com.acci.eaf.iam.application.port.out.UserRepository
import com.acci.eaf.iam.audit.AuditLogger
import com.acci.eaf.iam.domain.exception.PasswordValidationException
import com.acci.eaf.iam.domain.exception.UserAlreadyExistsException
import com.acci.eaf.iam.domain.exception.UserNotFoundException
import com.acci.eaf.iam.domain.model.User
import com.acci.eaf.iam.domain.model.UserStatus
import com.acci.eaf.iam.domain.service.PasswordValidator
import java.util.UUID
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Implementierung des [UserService] Interfaces.
 *
 * Diese Klasse stellt die Kernfunktionalität der Benutzerverwaltung bereit.
 */
@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val passwordValidator: PasswordValidator,
    private val auditLogger: AuditLogger,
) : UserService {

    private val logger = LoggerFactory.getLogger(UserServiceImpl::class.java)

    /**
     * Erstellt einen neuen lokalen Benutzer im System.
     *
     * @param command das Command mit den Benutzerinformationen
     * @return ein UserDto mit den Informationen des erstellten Benutzers
     * @throws UserAlreadyExistsException wenn ein Benutzer mit diesem Benutzernamen im Tenant bereits existiert
     * @throws PasswordValidationException wenn das Passwort die Komplexitätsanforderungen nicht erfüllt
     */
    @Transactional
    override fun createLocalUser(command: CreateUserCommand): UserDto {
        logger.debug("Erstelle neuen lokalen Benutzer '{}' für Tenant '{}'", command.username, command.tenantId)

        // Prüfe, ob ein Benutzer mit diesem Benutzernamen im Tenant bereits existiert
        if (userRepository.existsByUsernameAndTenantId(command.username, command.tenantId)) {
            logger.warn("Benutzer '{}' existiert bereits in Tenant '{}'", command.username, command.tenantId)
            throw UserAlreadyExistsException(command.username, command.tenantId.toString())
        }

        // Validiere das Passwort gegen Komplexitätsregeln
        val passwordValidationResult = passwordValidator.validate(command.password)
        if (!passwordValidationResult.valid) {
            logger.warn("Passwortvalidierung fehlgeschlagen für Benutzer '{}' in Tenant '{}'", command.username, command.tenantId)
            throw PasswordValidationException(passwordValidationResult.errors)
        }

        // Hashe das Passwort und erstelle den Benutzer
        val hashedPassword = passwordEncoder.encode(command.password)

        val user = User(
            tenantId = command.tenantId,
            username = command.username,
            passwordHash = hashedPassword,
            email = command.email,
            firstName = command.firstName,
            lastName = command.lastName,
            status = command.status
        )

        val savedUser = userRepository.save(user)
        logger.info("Lokaler Benutzer '{}' wurde erfolgreich für Tenant '{}' erstellt", command.username, command.tenantId)

        // Audit-Logging
        auditLogger.logUserCreation(savedUser.id, savedUser.username, savedUser.tenantId)

        return UserDto.fromEntity(savedUser)
    }

    /**
     * Ruft einen Benutzer anhand seiner ID ab.
     *
     * @param userId die ID des gesuchten Benutzers
     * @return ein UserDto mit den Informationen des gefundenen Benutzers
     * @throws UserNotFoundException wenn kein Benutzer mit dieser ID gefunden wurde
     */
    @Transactional(readOnly = true)
    override fun getUserById(userId: UUID): UserDto {
        logger.debug("Suche Benutzer mit ID '{}'", userId)

        val user = userRepository.findById(userId)
            .orElseThrow {
                logger.warn("Benutzer mit ID '{}' wurde nicht gefunden", userId)
                UserNotFoundException(userId.toString(), "Benutzer mit ID '$userId' wurde nicht gefunden")
            }

        return UserDto.fromEntity(user)
    }

    /**
     * Aktualisiert die Informationen eines Benutzers.
     *
     * @param command das Command mit den zu aktualisierenden Benutzerinformationen
     * @return ein UserDto mit den aktualisierten Benutzerinformationen
     * @throws UserNotFoundException wenn kein Benutzer mit dieser ID gefunden wurde
     */
    @Transactional
    override fun updateUser(command: UpdateUserCommand): UserDto {
        logger.debug("Aktualisiere Benutzer mit ID '{}'", command.userId)

        val user = userRepository.findById(command.userId)
            .orElseThrow {
                logger.warn("Benutzer mit ID '{}' wurde nicht gefunden", command.userId)
                UserNotFoundException(command.userId.toString(), "Benutzer mit ID '${command.userId}' wurde nicht gefunden")
            }

        // Prüfe, ob der Benutzer zum angegebenen Tenant gehört
        if (user.tenantId != command.tenantId) {
            logger.warn("Benutzer mit ID '{}' gehört nicht zum Tenant '{}'", command.userId, command.tenantId)
            throw UserNotFoundException(command.userId.toString(), "Benutzer mit ID '${command.userId}' wurde nicht gefunden")
        }

        // Erfasse die aktualisierten Felder für Audit-Logging
        val updatedFields = mutableMapOf<String, Any?>()

        command.email?.let {
            if (it != user.email) {
                updatedFields["email"] = it
                user.email = it
            }
        }
        command.firstName?.let {
            if (it != user.firstName) {
                updatedFields["firstName"] = it
                user.firstName = it
            }
        }
        command.lastName?.let {
            if (it != user.lastName) {
                updatedFields["lastName"] = it
                user.lastName = it
            }
        }
        command.status?.let {
            if (it != user.status) {
                updatedFields["status"] = it
                user.status = it
            }
        }

        val updatedUser = userRepository.save(user)
        logger.info("Benutzer mit ID '{}' wurde erfolgreich aktualisiert", command.userId)

        // Audit-Logging nur wenn tatsächlich Änderungen vorgenommen wurden
        if (updatedFields.isNotEmpty()) {
            auditLogger.logUserUpdate(
                updatedUser.id,
                updatedUser.username,
                updatedUser.tenantId,
                updatedFields
            )
        }

        return UserDto.fromEntity(updatedUser)
    }

    /**
     * Setzt ein neues Passwort für einen Benutzer.
     *
     * @param command das Command mit der Benutzer-ID und dem neuen Passwort
     * @throws UserNotFoundException wenn kein Benutzer mit dieser ID gefunden wurde
     * @throws PasswordValidationException wenn das Passwort die Komplexitätsanforderungen nicht erfüllt
     */
    @Transactional
    override fun setPassword(command: SetPasswordCommand) {
        logger.debug("Setze neues Passwort für Benutzer mit ID '{}'", command.userId)

        val user = userRepository.findById(command.userId)
            .orElseThrow {
                logger.warn("Benutzer mit ID '{}' wurde nicht gefunden", command.userId)
                UserNotFoundException(command.userId.toString(), "Benutzer mit ID '${command.userId}' wurde nicht gefunden")
            }

        // Prüfe, ob der Benutzer zum angegebenen Tenant gehört
        if (user.tenantId != command.tenantId) {
            logger.warn("Benutzer mit ID '{}' gehört nicht zum Tenant '{}'", command.userId, command.tenantId)
            throw UserNotFoundException(command.userId.toString(), "Benutzer mit ID '${command.userId}' wurde nicht gefunden")
        }

        // Validiere das neue Passwort gegen Komplexitätsregeln
        val passwordValidationResult = passwordValidator.validate(command.newPassword)
        if (!passwordValidationResult.valid) {
            logger.warn("Passwortvalidierung fehlgeschlagen für Benutzer mit ID '{}'", command.userId)
            throw PasswordValidationException(passwordValidationResult.errors)
        }

        // Hashe das neue Passwort und speichere es
        val hashedPassword = passwordEncoder.encode(command.newPassword)
        user.passwordHash = hashedPassword

        userRepository.save(user)
        logger.info("Passwort für Benutzer mit ID '{}' wurde erfolgreich aktualisiert", command.userId)

        // Audit-Logging
        auditLogger.logPasswordChange(user.id, user.username, user.tenantId)
    }

    /**
     * Aktualisiert den Status eines Benutzers.
     *
     * @param userId die ID des Benutzers
     * @param tenantId die ID des Tenants
     * @param newStatus der neue Status des Benutzers
     * @return ein UserDto mit den aktualisierten Benutzerinformationen
     * @throws UserNotFoundException wenn kein Benutzer mit dieser ID gefunden wurde
     */
    @Transactional
    override fun updateUserStatus(
        userId: UUID,
        tenantId: UUID,
        newStatus: UserStatus,
    ): UserDto {
        logger.debug("Aktualisiere Status für Benutzer mit ID '{}' zu '{}'", userId, newStatus)

        val user = userRepository.findById(userId)
            .orElseThrow {
                logger.warn("Benutzer mit ID '{}' wurde nicht gefunden", userId)
                UserNotFoundException(userId.toString(), "Benutzer mit ID '$userId' wurde nicht gefunden")
            }

        // Prüfe, ob der Benutzer zum angegebenen Tenant gehört
        if (user.tenantId != tenantId) {
            logger.warn("Benutzer mit ID '{}' gehört nicht zum Tenant '{}'", userId, tenantId)
            throw UserNotFoundException(userId.toString(), "Benutzer mit ID '$userId' wurde nicht gefunden")
        }

        // Status nur aktualisieren, wenn er sich geändert hat
        if (user.status != newStatus) {
            val oldStatus = user.status
            user.status = newStatus
            val updatedUser = userRepository.save(user)
            logger.info("Status für Benutzer mit ID '{}' wurde erfolgreich von '{}' auf '{}' aktualisiert",
                userId, oldStatus, newStatus)

            // Audit-Logging
            auditLogger.logUserStatusChange(user.id, user.username, user.tenantId, newStatus.toString())

            return UserDto.fromEntity(updatedUser)
        }

        logger.debug("Statusänderung für Benutzer mit ID '{}' übersprungen, da Status bereits '{}'", userId, newStatus)
        return UserDto.fromEntity(user)
    }

    /**
     * Findet alle Benutzer eines Tenants mit Paginierung.
     *
     * @param tenantId die ID des Tenants
     * @param pageable die Paginierungsparameter
     * @return eine Page mit UserDtos für den Tenant
     */
    @Transactional(readOnly = true)
    override fun findUsersByTenant(tenantId: UUID, pageable: Pageable): Page<UserDto> {
        logger.debug("Suche Benutzer für Tenant '{}'", tenantId)

        return userRepository.findByTenantId(tenantId, pageable)
            .map { UserDto.fromEntity(it) }
    }

    /**
     * Sucht nach Benutzern in einem Tenant anhand eines Suchbegriffs.
     *
     * @param tenantId die ID des Tenants
     * @param searchTerm der Suchbegriff
     * @param pageable die Paginierungsparameter
     * @return eine Page mit UserDtos, die dem Suchbegriff entsprechen
     */
    @Transactional(readOnly = true)
    override fun searchUsers(
        tenantId: UUID,
        searchTerm: String,
        pageable: Pageable,
    ): Page<UserDto> {
        logger.debug("Suche Benutzer für Tenant '{}' mit Suchbegriff '{}'", tenantId, searchTerm)

        return userRepository.searchByTenantId(tenantId, searchTerm, pageable)
            .map { UserDto.fromEntity(it) }
    }

    /**
     * Findet Benutzer in einem Tenant mit einem bestimmten Status.
     *
     * @param tenantId die ID des Tenants
     * @param status der Status der Benutzer
     * @param pageable die Paginierungsparameter
     * @return eine Page mit UserDtos, die dem Status entsprechen
     */
    @Transactional(readOnly = true)
    override fun findUsersByStatus(
        tenantId: UUID,
        status: UserStatus,
        pageable: Pageable,
    ): Page<UserDto> {
        logger.debug("Suche Benutzer für Tenant '{}' mit Status '{}'", tenantId, status)

        return userRepository.findByTenantIdAndStatus(tenantId, status, pageable)
            .map { UserDto.fromEntity(it) }
    }
}
