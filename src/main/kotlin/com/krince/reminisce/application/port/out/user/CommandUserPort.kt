package com.krince.reminisce.application.port.out.user

import com.krince.reminisce.domain.model.user.User
import com.krince.reminisce.domain.model.user.vo.UserId

interface CommandUserPort {
    fun save(user: User): User

    fun delete(userId: UserId)
}
