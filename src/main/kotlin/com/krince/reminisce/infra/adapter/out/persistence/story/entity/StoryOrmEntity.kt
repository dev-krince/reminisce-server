package com.krince.reminisce.infra.adapter.out.persistence.story.entity

import com.krince.reminisce.domain.model.story.vo.PostActivityConfig
import com.krince.reminisce.infra.adapter.out.persistence.story.converter.PostActivityConfigConverter
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Comment
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(name = "stories")
@EntityListeners(AuditingEntityListener::class)
class StoryOrmEntity(
    @Id
    @Column(name = "story_id", nullable = false, unique = true, updatable = false)
    @Comment("이야기 고유 식별자 (PK)")
    val storyId: String,

    @Column(nullable = false)
    @Comment("이야기 제목")
    val title: String,

    @Column(nullable = false, columnDefinition = "text")
    @Comment("이야기 목록·상세 화면에 표시할 소개")
    val summary: String,

    @Column(nullable = false, columnDefinition = "text")
    @Comment("이야기 도입")
    val intro: String,

    @Column(columnDefinition = "text")
    @Comment("이야기 상황")
    val situation: String?,

    @Column(name = "child_role")
    @Comment("아이 역할")
    val childRole: String?,

    @Column(nullable = false)
    @Comment("이야기 난이도")
    val difficulty: String,

    @Column(name = "estimated_minutes")
    @Comment("예상 활동 시간(분)")
    val estimatedMinutes: Short?,

    @Column(name = "representative_image_url")
    @Comment("대표 이미지 URL")
    val representativeImageUrl: String?,

    @Column(nullable = false)
    @Comment("이야기 공개 및 운영 상태")
    val status: String,

    @Column(name = "story_genre")
    @Comment("이야기 장르 (전래동화·창작동화)")
    val storyGenre: String? = null,

    @Column(name = "post_activity_config", columnDefinition = "text")
    @Convert(converter = PostActivityConfigConverter::class)
    @Comment("말하기 후 활동 설정 (JSON)")
    val postActivityConfig: PostActivityConfig?,
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
