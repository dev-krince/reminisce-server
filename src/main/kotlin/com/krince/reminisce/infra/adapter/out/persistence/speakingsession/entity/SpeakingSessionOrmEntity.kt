package com.krince.reminisce.infra.adapter.out.persistence.speakingsession.entity

import com.krince.reminisce.domain.model.story.vo.ThinkingElement
import com.krince.reminisce.infra.adapter.out.persistence.speakingsession.converter.AccumulatedElementsConverter
import jakarta.persistence.Column
import jakarta.persistence.Convert
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
    name = "story_sessions",
    indexes = [
        Index(name = "idx_story_sessions_child_id_story_id", columnList = "child_id, story_id"),
    ],
)
@EntityListeners(AuditingEntityListener::class)
class SpeakingSessionOrmEntity(
    @Id
    @Column(name = "session_id", nullable = false, unique = true, updatable = false)
    @Comment("말하기 세션 고유 식별자 (PK)")
    val sessionId: String,

    @Column(name = "child_id", nullable = false, updatable = false)
    @Comment("세션 소유 아이 식별자 (FK 참조)")
    val childId: String,

    @Column(name = "story_id", nullable = false, updatable = false)
    @Comment("진행 중인 이야기 식별자 (FK 참조)")
    val storyId: String,

    @Column(name = "current_scene_id", nullable = true)
    @Comment("현재 장면 식별자 (FK 참조)")
    val currentSceneId: String? = null,

    @Column(name = "status", nullable = false)
    @Comment("세션 상태")
    val status: String,

    @Column(name = "started_at", nullable = false, updatable = false)
    @Comment("세션 시작 시각")
    val startedAt: LocalDateTime,

    @Column(name = "last_activity_at", nullable = false)
    @Comment("마지막 활동 시각")
    val lastActivityAt: LocalDateTime,

    @Column(name = "accumulated_elements", columnDefinition = "text")
    @Convert(converter = AccumulatedElementsConverter::class)
    @Comment("현재 장면에서 확인된 사고 요소 누적 (type 합집합)")
    val accumulatedElements: List<ThinkingElement>? = emptyList(),
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
