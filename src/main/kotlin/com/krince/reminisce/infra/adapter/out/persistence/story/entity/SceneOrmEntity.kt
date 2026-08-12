package com.krince.reminisce.infra.adapter.out.persistence.story.entity

import com.krince.reminisce.domain.model.story.CharacterVoice
import com.krince.reminisce.domain.model.story.Mission
import com.krince.reminisce.domain.model.story.vo.ThinkingElement
import com.krince.reminisce.infra.adapter.out.persistence.story.converter.CharacterVoiceConverter
import com.krince.reminisce.infra.adapter.out.persistence.story.converter.MissionConverter
import com.krince.reminisce.infra.adapter.out.persistence.story.converter.RequiredElementsConverter
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
    name = "story_scenes",
    indexes = [
        Index(name = "idx_story_scenes_story_id_scene_order", columnList = "story_id, scene_order"),
    ],
)
@EntityListeners(AuditingEntityListener::class)
class SceneOrmEntity(
    @Id
    @Column(name = "scene_id", nullable = false, unique = true, updatable = false)
    @Comment("장면 고유 식별자 (PK)")
    val sceneId: String,

    @Column(name = "story_id", nullable = false, updatable = false)
    @Comment("소속 이야기 식별자 (FK 참조)")
    val storyId: String,

    @Column(name = "scene_order", nullable = false)
    @Comment("이야기 안에서 장면이 진행되는 순서")
    val sceneOrder: Short,

    @Column(name = "scene_type", nullable = false)
    @Comment("장면 종류 (NARRATION/DIALOGUE)")
    val sceneType: String,

    @Column(name = "scene_description", nullable = false, columnDefinition = "text")
    @Comment("장면 상황과 대화 맥락")
    val sceneDescription: String,

    @Column(name = "character_name")
    @Comment("대화 캐릭터 코드")
    val characterName: String?,

    @Column(name = "character_display_name")
    @Comment("대화 캐릭터 표시명")
    val characterDisplayName: String?,

    @Column(name = "character_opening", columnDefinition = "text")
    @Comment("장면 시작 시 캐릭터 고정 첫 대사")
    val characterOpening: String?,

    @Column(name = "character_closing", columnDefinition = "text")
    @Comment("장면 종료 시 캐릭터 고정 마지막 대사")
    val characterClosing: String?,

    @Column(columnDefinition = "text")
    @Comment("장면의 갈등·고민 요약")
    val conflict: String?,

    @Column(name = "scene_goal", columnDefinition = "text")
    @Comment("장면에서 이끌어내고자 하는 발화 목표")
    val sceneGoal: String?,

    @Column(name = "required_elements", columnDefinition = "text")
    @Convert(converter = RequiredElementsConverter::class)
    @Comment("장면 목표 충족에 필요한 사고 요소 (JSON)")
    val requiredElements: List<ThinkingElement>?,

    @Column(name = "preferred_turns")
    @Comment("목표 충족으로 종료하기 위한 최소 아이 발화 횟수")
    val preferredTurns: Short?,

    @Column(name = "max_turns")
    @Comment("장면에서 허용하는 최대 아이 발화 횟수")
    val maxTurns: Short?,

    @Column(name = "mission", columnDefinition = "text")
    @Convert(converter = MissionConverter::class)
    @Comment("장면 미션 메타 (목표·예시 힌트, JSON, DIALOGUE 전용 선택)")
    val mission: Mission? = null,

    @Column(name = "character_voice", columnDefinition = "text")
    @Convert(converter = CharacterVoiceConverter::class)
    @Comment("캐릭터 음성 메타 (성별·연령대·프로파일 키, JSON, DIALOGUE 전용 선택)")
    val characterVoice: CharacterVoice? = null,
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
