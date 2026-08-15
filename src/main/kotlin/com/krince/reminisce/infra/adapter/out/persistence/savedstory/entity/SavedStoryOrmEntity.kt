package com.krince.reminisce.infra.adapter.out.persistence.savedstory.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.Comment
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(
    name = "saved_story",
    indexes = [
        Index(name = "idx_saved_story_child_id", columnList = "child_id"),
    ],
    uniqueConstraints = [
        UniqueConstraint(name = "uk_saved_story_child_id_story_id", columnNames = ["child_id", "story_id"]),
    ],
)
@EntityListeners(AuditingEntityListener::class)
class SavedStoryOrmEntity(
    @Id
    @Column(name = "saved_story_id", nullable = false, unique = true, updatable = false)
    @Comment("찜한 이야기 고유 식별자 (PK)")
    val savedStoryId: String,

    @Column(name = "child_id", nullable = false, updatable = false)
    @Comment("이야기를 찜한 아이 식별자 (FK 참조)")
    val childId: String,

    @Column(name = "story_id", nullable = false, updatable = false)
    @Comment("찜한 이야기 식별자 (FK 참조)")
    val storyId: String,
) {
    @Column(name = "created_date", nullable = false, updatable = false)
    @CreatedDate
    @Comment("생성일시")
    var createdDate: LocalDateTime? = null
}
