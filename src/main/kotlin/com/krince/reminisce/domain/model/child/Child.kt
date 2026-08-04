package com.krince.reminisce.domain.model.child

import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.child.vo.ChildNickname
import com.krince.reminisce.domain.model.user.vo.UserId
import com.krince.reminisce.shared.util.UuidGenerator
import java.time.LocalDateTime

class Child(
    val childId: ChildId,
    val guardianId: UserId,
    val nickname: ChildNickname,
    val createdDate: LocalDateTime? = null,
    val modifiedDate: LocalDateTime? = null,
) {
    companion object {
        fun register(guardianId: UserId, nickname: ChildNickname): Child = Child(
            childId = ChildId(UuidGenerator.generate()),
            guardianId = guardianId,
            nickname = nickname,
        )
    }
}
