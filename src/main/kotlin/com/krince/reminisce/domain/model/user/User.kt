package com.krince.reminisce.domain.model.user

import com.krince.reminisce.domain.model.user.vo.LoginId
import com.krince.reminisce.domain.model.user.vo.Role
import com.krince.reminisce.domain.model.user.vo.UserId
import java.time.LocalDateTime

class User(
    val userId: UserId,
    val loginId: LoginId,
    val role: Role,
    val createdDate: LocalDateTime? = null,
    val modifiedDate: LocalDateTime? = null,
)