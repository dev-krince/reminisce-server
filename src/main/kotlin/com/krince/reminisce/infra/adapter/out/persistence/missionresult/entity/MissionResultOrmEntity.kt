package com.krince.reminisce.infra.adapter.out.persistence.missionresult.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import org.hibernate.annotations.Comment
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(
    name = "mission_results",
    indexes = [
        Index(name = "idx_mission_results_session_id_scene_id", columnList = "session_id, scene_id"),
    ],
)
@EntityListeners(AuditingEntityListener::class)
class MissionResultOrmEntity(
    @Id
    @Column(name = "id", nullable = false, unique = true, updatable = false)
    @Comment("미션 결과 고유 식별자 (PK)")
    val id: String,

    @Column(name = "session_id", nullable = false, updatable = false)
    @Comment("말하기 세션 식별자 (FK 참조)")
    val sessionId: String,

    @Column(name = "scene_id", nullable = false, updatable = false)
    @Comment("미션 장면 식별자 (FK 참조)")
    val sceneId: String,

    @Column(name = "completed", nullable = false)
    @Comment("미션 완료 여부")
    val completed: Boolean,

    @Column(name = "attempt_count", nullable = false)
    @Comment("시도 횟수")
    val attemptCount: Int,

    @Column(name = "completed_at", nullable = true)
    @Comment("완료 시각")
    val completedAt: LocalDateTime? = null,
) {
    @Column(name = "created_date", nullable = false, updatable = false)
    @CreatedDate
    @Comment("생성일시")
    var createdDate: LocalDateTime? = null

    @Column(name = "modified_date", nullable = false)
    @LastModifiedDate
    @Comment("마지막 수정일시")
    var modifiedDate: LocalDateTime? = null
}
