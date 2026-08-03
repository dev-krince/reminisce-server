package com.krince.reminisce.application.port.out.user

import com.krince.reminisce.domain.model.user.User

interface CommandUserPort {
    fun save(user: User): User
}
