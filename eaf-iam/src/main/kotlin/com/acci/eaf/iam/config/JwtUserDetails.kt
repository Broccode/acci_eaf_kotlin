package com.acci.eaf.iam.config

import java.util.UUID
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

/**
 * Implementierung von UserDetails für die JWT-basierte Authentifizierung.
 * Diese Klasse wird als Principal in Spring Security verwendet und enthält
 * die Benutzerinformationen aus dem JWT-Token.
 */
class JwtUserDetails(val id: UUID, val tenantId: UUID, private val username: String, private val permissions: List<String>) : UserDetails {

    override fun getAuthorities(): Collection<GrantedAuthority> = permissions.map { SimpleGrantedAuthority(it) }

    override fun getPassword(): String? = null

    override fun getUsername(): String = username

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = true
}
