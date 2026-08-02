package com.krince.reminisce.infra.security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class CustomUserDetails(
    private val id: String,
    private val role: String,
) : UserDetails {
    fun getId(): String = id

    fun getRole(): String = role

    @Deprecated(message = "쓰지마세요.")
    override fun getAuthorities(): Collection<GrantedAuthority> = listOf(SimpleGrantedAuthority(role))

    @Deprecated(message = "쓰지마세요.")
    override fun getPassword(): String = "The Password is not being entered"

    @Deprecated(message = "쓰지마세요.")
    override fun getUsername(): String = "The username is not being entered."
}