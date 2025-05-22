package com.acci.eaf.iam.adapter.rest

import com.acci.eaf.iam.application.port.api.PermissionDto
import com.acci.eaf.iam.application.port.api.PermissionService
import java.util.UUID
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * Controller für die API-Endpunkte zur Verwaltung von Berechtigungen.
 * Berechtigungen können nur aufgelistet, aber nicht erstellt, aktualisiert oder gelöscht werden,
 * da sie vom System vordefiniert sind.
 */
@RestController
@RequestMapping("/api/controlplane/permissions")
class PermissionController(private val permissionService: PermissionService) {

    /**
     * Listet alle verfügbaren Berechtigungen auf.
     *
     * @param pageable Paginierungsinformationen
     * @return Eine Page mit Berechtigungs-DTOs
     */
    @GetMapping
    @PreAuthorize("hasAuthority('permission:read')")
    fun getAllPermissions(pageable: Pageable): ResponseEntity<Page<PermissionDto>> = ResponseEntity.ok(permissionService.getAllPermissions(pageable))

    /**
     * Holt eine Berechtigung anhand ihrer ID.
     *
     * @param permissionId Die ID der Berechtigung
     * @return Die Berechtigung als DTO
     */
    @GetMapping("/{permissionId}")
    @PreAuthorize("hasAuthority('permission:read')")
    fun getPermissionById(@PathVariable permissionId: UUID): ResponseEntity<PermissionDto> =
        ResponseEntity.ok(permissionService.getPermissionById(permissionId))

    /**
     * Sucht nach Berechtigungen, deren Name einen bestimmten String enthält.
     *
     * @param namePart Teil des Berechtigungsnamens
     * @param pageable Paginierungsinformationen
     * @return Eine Page mit Berechtigungs-DTOs
     */
    @GetMapping("/search")
    @PreAuthorize("hasAuthority('permission:read')")
    fun searchPermissions(
        @RequestParam(required = false, defaultValue = "") namePart: String,
        pageable: Pageable,
    ): ResponseEntity<Page<PermissionDto>> = ResponseEntity.ok(permissionService.searchPermissionsByName(namePart, pageable))
}
