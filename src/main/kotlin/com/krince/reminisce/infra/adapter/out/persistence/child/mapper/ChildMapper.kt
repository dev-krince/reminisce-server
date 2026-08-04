package com.krince.reminisce.infra.adapter.out.persistence.child.mapper

import com.krince.reminisce.domain.model.child.Child
import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.child.vo.ChildNickname
import com.krince.reminisce.domain.model.user.vo.UserId
import com.krince.reminisce.infra.adapter.out.persistence.child.entity.ChildOrmEntity

object ChildMapper {
    fun toDomain(ormEntity: ChildOrmEntity): Child = Child(
        childId = ChildId(ormEntity.childId),
        guardianId = UserId(ormEntity.guardianId),
        nickname = ChildNickname(ormEntity.nickname),
        createdDate = ormEntity.createdDate,
        modifiedDate = ormEntity.modifiedDate,
    )

    fun toEntity(domain: Child): ChildOrmEntity = ChildOrmEntity(
        childId = domain.childId.value,
        guardianId = domain.guardianId.value,
        nickname = domain.nickname.value,
    ).apply {
        createdDate = domain.createdDate
        modifiedDate = domain.modifiedDate
    }
}
