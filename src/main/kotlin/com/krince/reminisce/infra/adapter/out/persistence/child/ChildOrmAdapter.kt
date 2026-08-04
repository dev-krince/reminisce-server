package com.krince.reminisce.infra.adapter.out.persistence.child

import com.krince.reminisce.application.port.out.child.CommandChildPort
import com.krince.reminisce.application.port.out.child.LoadChildPort
import com.krince.reminisce.domain.model.child.Child
import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.user.vo.UserId
import com.krince.reminisce.infra.adapter.out.persistence.child.entity.ChildOrmEntity
import com.krince.reminisce.infra.adapter.out.persistence.child.mapper.ChildMapper
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class ChildOrmAdapter(
    private val repository: ChildRepository,
) : LoadChildPort, CommandChildPort {

    override fun findById(childId: ChildId): Child? {
        val childOrmEntity: ChildOrmEntity = repository.findByIdOrNull(childId.value) ?: return null

        return ChildMapper.toDomain(childOrmEntity)
    }

    override fun findAllByGuardianId(guardianId: UserId): List<Child> =
        repository.findAllByGuardianId(guardianId.value).map { ChildMapper.toDomain(it) }

    override fun countByGuardianId(guardianId: UserId): Long = repository.countByGuardianId(guardianId.value)

    override fun save(child: Child): Child {
        val childOrmEntity: ChildOrmEntity = ChildMapper.toEntity(child)
        val savedEntity: ChildOrmEntity = repository.saveAndFlush(childOrmEntity)

        return ChildMapper.toDomain(savedEntity)
    }
}
