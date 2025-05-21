package com.acci.eaf.iam.adapter.rest

import com.acci.eaf.iam.adapter.rest.dto.CreateUserRequest
import com.acci.eaf.iam.adapter.rest.dto.SetPasswordRequest
import com.acci.eaf.iam.adapter.rest.dto.UpdateUserRequest
import com.acci.eaf.iam.adapter.rest.dto.UpdateUserStatusRequest
import com.acci.eaf.iam.adapter.rest.dto.UserResponse
import com.acci.eaf.iam.application.port.input.CreateUserCommand
import com.acci.eaf.iam.application.port.input.SetPasswordCommand
import com.acci.eaf.iam.application.port.input.UpdateUserCommand
import com.acci.eaf.iam.application.port.input.UserService
import com.acci.eaf.iam.domain.model.UserStatus
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import java.util.UUID
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

/**
 * REST-Controller für das Management von Benutzern innerhalb eines Tenants.
 *
 * Diese Klasse stellt API-Endpunkte bereit, um Benutzer zu erstellen, zu lesen,
 * zu aktualisieren, und deren Status zu ändern.
 */
@RestController
@RequestMapping("/api/controlplane/tenants/{tenantId}/users")
@Tag(name = "User Management", description = "API für das Management von Benutzern innerhalb eines Tenants")
class UserManagementController(private val userService: UserService) {

    /**
     * Erstellt einen neuen Benutzer für einen Tenant.
     *
     * @param tenantId die ID des Tenants, zu dem der Benutzer gehören soll
     * @param request die Request-Daten mit den Benutzerinformationen
     * @return die Antwort mit den Benutzerdetails im Body und dem Statuscode 201 Created
     */
    @PostMapping
    @Operation(
        summary = "Erstellt einen neuen Benutzer",
        description = "Erstellt einen neuen Benutzer für den angegebenen Tenant mit den bereitgestellten Informationen"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Benutzer erfolgreich erstellt"),
            ApiResponse(responseCode = "400", description = "Ungültige Anfrage"),
            ApiResponse(responseCode = "403", description = "Keine Berechtigung"),
            ApiResponse(responseCode = "409", description = "Benutzer existiert bereits")
        ]
    )
    fun createUser(
        @Parameter(description = "ID des Tenants") @PathVariable tenantId: UUID,
        @Valid @RequestBody request: CreateUserRequest,
    ): ResponseEntity<UserResponse> {
        val command = CreateUserCommand(
            tenantId = tenantId,
            username = request.username,
            password = request.password,
            email = request.email,
            firstName = request.firstName,
            lastName = request.lastName
        )

        val createdUser = userService.createLocalUser(command)
        val userResponse = UserResponse.fromDto(createdUser)

        val location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{userId}")
            .buildAndExpand(createdUser.id)
            .toUri()

        return ResponseEntity.created(location).body(userResponse)
    }

    /**
     * Ruft eine Liste von Benutzern für einen Tenant ab.
     *
     * @param tenantId die ID des Tenants
     * @param status optionaler Filter für den Benutzerstatus
     * @param username optionaler Filter für den Benutzernamen
     * @param pageable Paginierungsparameter
     * @return eine paginierte Liste von Benutzern
     */
    @GetMapping
    @Operation(
        summary = "Ruft eine Liste von Benutzern ab",
        description = "Ruft eine paginierte Liste von Benutzern für den angegebenen Tenant ab, " +
            "optional gefiltert nach Status oder Benutzername"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Liste erfolgreich abgerufen"),
            ApiResponse(responseCode = "403", description = "Keine Berechtigung")
        ]
    )
    fun getUsers(
        @Parameter(description = "ID des Tenants") @PathVariable tenantId: UUID,
        @Parameter(description = "Status-Filter") @RequestParam(required = false) status: UserStatus?,
        @Parameter(description = "Benutzername-Filter") @RequestParam(required = false) username: String?,
        @PageableDefault(size = 20) pageable: Pageable,
    ): Page<UserResponse> {
        val users = when {
            status != null -> userService.findUsersByStatus(tenantId, status, pageable)
            username != null -> userService.searchUsers(tenantId, username, pageable)
            else -> userService.findUsersByTenant(tenantId, pageable)
        }

        return users.map { UserResponse.fromDto(it) }
    }

    /**
     * Ruft die Details eines bestimmten Benutzers ab.
     *
     * @param tenantId die ID des Tenants
     * @param userId die ID des Benutzers
     * @return die Benutzerdetails oder 404, wenn der Benutzer nicht gefunden wurde
     */
    @GetMapping("/{userId}")
    @Operation(
        summary = "Ruft einen Benutzer ab",
        description = "Ruft die Details eines bestimmten Benutzers für den angegebenen Tenant ab"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Benutzer erfolgreich abgerufen",
                content = [Content(schema = Schema(implementation = UserResponse::class))]
            ),
            ApiResponse(responseCode = "403", description = "Keine Berechtigung"),
            ApiResponse(responseCode = "404", description = "Benutzer nicht gefunden")
        ]
    )
    fun getUser(
        @Parameter(description = "ID des Tenants") @PathVariable tenantId: UUID,
        @Parameter(description = "ID des Benutzers") @PathVariable userId: UUID,
    ): ResponseEntity<UserResponse> {
        val user = userService.getUserById(userId)

        // Prüfen, ob der Benutzer zum angegebenen Tenant gehört
        if (user.tenantId != tenantId) {
            return ResponseEntity.notFound().build()
        }

        return ResponseEntity.ok(UserResponse.fromDto(user))
    }

    /**
     * Aktualisiert die Informationen eines Benutzers.
     *
     * @param tenantId die ID des Tenants
     * @param userId die ID des Benutzers
     * @param request die zu aktualisierenden Informationen
     * @return die aktualisierten Benutzerdetails oder 404, wenn der Benutzer nicht gefunden wurde
     */
    @PutMapping("/{userId}")
    @Operation(
        summary = "Aktualisiert einen Benutzer",
        description = "Aktualisiert die Informationen eines bestimmten Benutzers für den angegebenen Tenant"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Benutzer erfolgreich aktualisiert"),
            ApiResponse(responseCode = "400", description = "Ungültige Anfrage"),
            ApiResponse(responseCode = "403", description = "Keine Berechtigung"),
            ApiResponse(responseCode = "404", description = "Benutzer nicht gefunden")
        ]
    )
    fun updateUser(
        @Parameter(description = "ID des Tenants") @PathVariable tenantId: UUID,
        @Parameter(description = "ID des Benutzers") @PathVariable userId: UUID,
        @Valid @RequestBody request: UpdateUserRequest,
    ): ResponseEntity<UserResponse> {
        val command = UpdateUserCommand(
            userId = userId,
            tenantId = tenantId,
            email = request.email,
            firstName = request.firstName,
            lastName = request.lastName,
            status = request.status
        )

        val updatedUser = userService.updateUser(command)
        return ResponseEntity.ok(UserResponse.fromDto(updatedUser))
    }

    /**
     * Setzt ein neues Passwort für einen Benutzer.
     *
     * @param tenantId die ID des Tenants
     * @param userId die ID des Benutzers
     * @param request das neue Passwort
     * @return der Statuscode 204 No Content bei Erfolg
     */
    @PostMapping("/{userId}/set-password")
    @Operation(
        summary = "Setzt ein neues Passwort",
        description = "Setzt ein neues Passwort für einen bestimmten Benutzer"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Passwort erfolgreich geändert"),
            ApiResponse(responseCode = "400", description = "Ungültige Anfrage"),
            ApiResponse(responseCode = "403", description = "Keine Berechtigung"),
            ApiResponse(responseCode = "404", description = "Benutzer nicht gefunden")
        ]
    )
    fun setPassword(
        @Parameter(description = "ID des Tenants") @PathVariable tenantId: UUID,
        @Parameter(description = "ID des Benutzers") @PathVariable userId: UUID,
        @Valid @RequestBody request: SetPasswordRequest,
    ): ResponseEntity<Void> {
        val command = SetPasswordCommand(
            userId = userId,
            tenantId = tenantId,
            newPassword = request.newPassword
        )

        userService.setPassword(command)
        return ResponseEntity.noContent().build()
    }

    /**
     * Aktualisiert den Status eines Benutzers.
     *
     * @param tenantId die ID des Tenants
     * @param userId die ID des Benutzers
     * @param request der neue Status
     * @return die aktualisierten Benutzerdetails oder 404, wenn der Benutzer nicht gefunden wurde
     */
    @PutMapping("/{userId}/status")
    @Operation(
        summary = "Aktualisiert den Status eines Benutzers",
        description = "Aktualisiert den Status eines bestimmten Benutzers für den angegebenen Tenant"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Status erfolgreich aktualisiert"),
            ApiResponse(responseCode = "400", description = "Ungültige Anfrage"),
            ApiResponse(responseCode = "403", description = "Keine Berechtigung"),
            ApiResponse(responseCode = "404", description = "Benutzer nicht gefunden")
        ]
    )
    fun updateUserStatus(
        @Parameter(description = "ID des Tenants") @PathVariable tenantId: UUID,
        @Parameter(description = "ID des Benutzers") @PathVariable userId: UUID,
        @Valid @RequestBody request: UpdateUserStatusRequest,
    ): ResponseEntity<UserResponse> {
        val updatedUser = userService.updateUserStatus(userId, tenantId, request.status)
        return ResponseEntity.ok(UserResponse.fromDto(updatedUser))
    }
}
