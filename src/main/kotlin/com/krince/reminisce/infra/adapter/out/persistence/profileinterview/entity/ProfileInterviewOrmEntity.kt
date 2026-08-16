package com.krince.reminisce.infra.adapter.out.persistence.profileinterview.entity

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
    name = "profile_interviews",
    indexes = [
        Index(name = "idx_profile_interviews_child_id", columnList = "child_id"),
    ],
)
@EntityListeners(AuditingEntityListener::class)
class ProfileInterviewOrmEntity(
    @Id
    @Column(name = "interview_id", nullable = false, unique = true, updatable = false)
    @Comment("프로필 인터뷰 고유 식별자 (PK)")
    val interviewId: String,

    @Column(name = "child_id", nullable = false, updatable = false)
    @Comment("인터뷰 대상 아이 식별자 (FK 참조)")
    val childId: String,

    @Column(name = "status", nullable = false)
    @Comment("인터뷰 상태 (IN_PROGRESS/COMPLETED)")
    val status: String,

    @Column(name = "current_stage", nullable = false)
    @Comment("현재 진행 단계 (7단계)")
    val currentStage: String,

    @Column(name = "stage_child_turn_count", nullable = false)
    @Comment("현재 단계에서 아이가 답한 횟수")
    val stageChildTurnCount: Int,

    @Column(name = "total_child_turn_count", nullable = false)
    @Comment("인터뷰 전체에서 아이가 답한 횟수")
    val totalChildTurnCount: Int,

    @Column(name = "started_at", nullable = false, updatable = false)
    @Comment("인터뷰 시작 시각")
    val startedAt: LocalDateTime,

    @Column(name = "last_activity_at", nullable = false)
    @Comment("마지막 활동 시각")
    val lastActivityAt: LocalDateTime,
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
