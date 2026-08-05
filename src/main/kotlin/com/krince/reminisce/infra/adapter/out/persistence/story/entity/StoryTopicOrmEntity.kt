package com.krince.reminisce.infra.adapter.out.persistence.story.entity

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
    name = "story_topics",
    indexes = [
        Index(name = "idx_story_topics_story_id", columnList = "story_id"),
    ],
)
@EntityListeners(AuditingEntityListener::class)
class StoryTopicOrmEntity(
    @Id
    @Column(name = "id", nullable = false, unique = true, updatable = false)
    @Comment("이야기 주제 고유 식별자 (PK)")
    val id: String,

    @Column(name = "story_id", nullable = false, updatable = false)
    @Comment("소속 이야기 식별자 (FK 참조)")
    val storyId: String,

    @Column(nullable = false)
    @Comment("이야기 주제")
    val topic: String,
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
