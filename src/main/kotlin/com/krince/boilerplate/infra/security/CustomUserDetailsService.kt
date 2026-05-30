package com.krince.boilerplate.infra.security

import com.krince.boilerplate.application.port.access.user.UserAccessPort
import com.krince.boilerplate.application.port.access.user.snapshot.UserSnapshot
import com.krince.boilerplate.domain.model.user.vo.UserId
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class CustomUserDetailsService(private val userAccessPort: UserAccessPort) : UserDetailsService {

    override fun loadUserByUsername(loginId: String): CustomUserDetails {
        val user: UserSnapshot = userAccessPort.findByLoginId(loginId)
        val roleValue: String = user.role

        return CustomUserDetails(id = user.userId.value, role = roleValue)
    }

    fun loadUserById(id: String): CustomUserDetails {
        val user: UserSnapshot = userAccessPort.findByUserId(UserId(id))
        val role: String = user.role

        return CustomUserDetails(id = user.userId.value, role = role)
    }
}