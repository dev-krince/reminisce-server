package com.krince.reminisce.infra.security

import com.krince.reminisce.application.port.access.user.UserAccessPort
import com.krince.reminisce.application.port.access.user.snapshot.UserSnapshot
import com.krince.reminisce.domain.model.user.vo.UserId
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class CustomUserDetailsService(private val userAccessPort: UserAccessPort) : UserDetailsService {

    override fun loadUserByUsername(email: String): CustomUserDetails {
        val user: UserSnapshot = userAccessPort.findByEmail(email)
        val roleValue: String = user.role

        return CustomUserDetails(id = user.userId.value, role = roleValue)
    }

    fun loadUserById(id: String): CustomUserDetails {
        val user: UserSnapshot = userAccessPort.findByUserId(UserId(id))
        val role: String = user.role

        return CustomUserDetails(id = user.userId.value, role = role)
    }
}
