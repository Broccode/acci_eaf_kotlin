package com.acci.eaf.iam.application.service

import com.acci.eaf.iam.application.port.input.CreateUserCommand
import com.acci.eaf.iam.application.port.input.SetPasswordCommand
import com.acci.eaf.iam.application.port.input.UpdateUserCommand
import com.acci.eaf.iam.application.port.input.UserDto
import com.acci.eaf.iam.application.port.input.UserService
import com.acci.eaf.iam.application.port.out.UserRepository
import com.acci.eaf.iam.domain.exception.PasswordValidationException
import com.acci.eaf.iam.domain.exception.UserAlreadyExistsException
import com.acci.eaf.iam.domain.exception.UserNotFoundException
import com.acci.eaf.iam.domain.model.User
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
            status = command.status
        )

        val savedUser = userRepository.save(user)
        logger.info("Lokaler Benutzer '{}' wurde erfolgreich für Tenant '{}' erstellt", command.username, command.tenantId)

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

        // Aktualisiere nur die Felder, die im Command gesetzt sind
        command.email?.let { user.email = it }
        command.status?.let { user.status = it }

        val updatedUser = userRepository.save(user)
        logger.info("Benutzer mit ID '{}' wurde erfolgreich aktualisiert", command.userId)

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
}
