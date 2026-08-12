package com.krince.reminisce.infra.adapter.out.persistence.wordbook.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import org.hibernate.annotations.Comment
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(
    name = "wordbook",
    indexes = [
        Index(name = "idx_wordbook_child_id", columnList = "child_id"),
    ],
)
@EntityListeners(AuditingEntityListener::class)
class SavedWordOrmEntity(
    @Id
    @Column(name = "saved_word_id", nullable = false, unique = true, updatable = false)
    @Comment("저장 단어 고유 식별자 (PK)")
    val savedWordId: String,

    @Column(name = "child_id", nullable = false, updatable = false)
    @Comment("단어를 저장한 아이 식별자 (FK 참조)")
    val childId: String,

    @Column(name = "word", nullable = false)
    @Comment("저장한 단어")
    val word: String,

    @Column(name = "meaning", nullable = true)
    @Comment("단어의 쉬운 뜻")
    val meaning: String? = null,

    @Column(name = "source_scene_id", nullable = true)
    @Comment("단어를 만난 출처 장면 식별자 (FK 참조)")
    val sourceSceneId: String? = null,
) {
    @Column(name = "created_date", nullable = false, updatable = false)
    @CreatedDate
    @Comment("생성일시")
    var createdDate: LocalDateTime? = null
}
