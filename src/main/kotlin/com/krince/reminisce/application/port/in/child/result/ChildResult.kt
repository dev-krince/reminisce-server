package com.krince.reminisce.application.port.`in`.child.result

import com.krince.reminisce.domain.model.child.Child
import java.time.LocalDateTime

class ChildResult(
    val childId: String,
    val nickname: String,
    val birthYear: Int,
    val createdDate: LocalDateTime,
) {
    companion object {
        fun from(child: Child): ChildResult = ChildResult(
            childId = child.childId.value,
            nickname = child.nickname.value,
            birthYear = child.birthYear.value,
            createdDate = requireNotNull(child.createdDate),
        )
    }
}
