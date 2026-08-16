package com.krince.reminisce.infra.adapter.out.persistence.storyprofile.entity

import com.krince.reminisce.domain.model.storyprofile.InterestTopic
import com.krince.reminisce.domain.model.storyprofile.ProfileFinding
import com.krince.reminisce.domain.model.storyprofile.SpeechAreaAnalysis
import com.krince.reminisce.infra.adapter.out.persistence.storyprofile.converter.InterestTopicsConverter
import com.krince.reminisce.infra.adapter.out.persistence.storyprofile.converter.ProfileFindingsConverter
import com.krince.reminisce.infra.adapter.out.persistence.storyprofile.converter.SpeechAreaAnalysesConverter
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
    name = "story_profiles",
    indexes = [
        Index(name = "uk_story_profiles_child_id", columnList = "child_id", unique = true),
    ],
)
@EntityListeners(AuditingEntityListener::class)
class StoryProfileOrmEntity(
    @Id
    @Column(name = "profile_id", nullable = false, unique = true, updatable = false)
    @Comment("이야기 프로필 고유 식별자 (PK)")
    val profileId: String,

    @Column(name = "child_id", nullable = false, updatable = false)
    @Comment("프로필 대상 아이 식별자 (FK 참조, 아이당 1개)")
    val childId: String,

    @Column(name = "interview_id", nullable = false, updatable = false)
    @Comment("근거가 된 프로필 인터뷰 식별자 (FK 참조)")
    val interviewId: String,

    @Convert(converter = InterestTopicsConverter::class)
    @Column(name = "interest_topics", nullable = false, columnDefinition = "text")
    @Comment("관심 주제 (카테고리+태그 JSON)")
    val interestTopics: List<InterestTopic>,

    @Convert(converter = ProfileFindingsConverter::class)
    @Column(name = "strengths", nullable = false, columnDefinition = "text")
    @Comment("잘하는 이야기 방식 (JSON)")
    val strengths: List<ProfileFinding>,

    @Convert(converter = ProfileFindingsConverter::class)
    @Column(name = "practice_points", nullable = false, columnDefinition = "text")
    @Comment("연습하면 좋은 점 (JSON)")
    val practicePoints: List<ProfileFinding>,

    @Convert(converter = SpeechAreaAnalysesConverter::class)
    @Column(name = "speech_analyses", nullable = false, columnDefinition = "text")
    @Comment("말하기 분석 3영역 (어휘·표현·논리 JSON)")
    val speechAnalyses: List<SpeechAreaAnalysis>,

    @Column(name = "created_at", nullable = false, updatable = false)
    @Comment("프로필 생성 시각")
    val createdAt: LocalDateTime,
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
