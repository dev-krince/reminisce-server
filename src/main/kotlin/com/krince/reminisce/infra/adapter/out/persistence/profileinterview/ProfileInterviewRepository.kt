package com.krince.reminisce.infra.adapter.out.persistence.profileinterview

import com.krince.reminisce.infra.adapter.out.persistence.profileinterview.entity.ProfileInterviewOrmEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface ProfileInterviewRepository : JpaRepository<ProfileInterviewOrmEntity, String> {
    fun findFirstByChildIdAndStatusOrderByStartedAtDesc(childId: String, status: String): ProfileInterviewOrmEntity?

    fun deleteAllByChildIdIn(childIds: List<String>)

    @Query("select entity.interviewId from ProfileInterviewOrmEntity entity where entity.childId in :childIds")
    fun findInterviewIdsByChildIdIn(@Param("childIds") childIds: List<String>): List<String>
}
