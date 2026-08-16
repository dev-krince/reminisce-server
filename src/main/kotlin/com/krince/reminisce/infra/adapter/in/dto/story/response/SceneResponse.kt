package com.krince.reminisce.infra.adapter.`in`.dto.story.response

import com.krince.reminisce.application.port.`in`.story.result.SceneResult
import io.swagger.v3.oas.annotations.media.Schema

@Schema(title = "CharacterVoiceResponse", description = "캐릭터 음성 메타 응답")
class CharacterVoiceResponse(
    @field:Schema(description = "성별", example = "FEMALE", required = true)
    val gender: String,

    @field:Schema(description = "연령대", example = "ADULT", required = true)
    val ageGroup: String,

    @field:Schema(description = "음성 프로파일 키", example = "young_woman_gentle", required = true)
    val voiceProfile: String,
)

@Schema(title = "WordCardResponse", description = "단어카드 응답")
class WordCardResponse(
    @field:Schema(description = "카드 단어", required = true)
    val text: String,

    @field:Schema(description = "정답 순서", required = true)
    val correctOrder: Int,
)

@Schema(title = "MissionResponse", description = "장면 미션 응답")
class MissionResponse(
    @field:Schema(description = "미션 목표", required = true)
    val goal: String,

    @field:Schema(description = "미션 예시 힌트 목록", required = true)
    val examples: List<String>,

    @field:Schema(description = "미션 유형", example = "SPEAKING", required = true)
    val type: String,

    @field:Schema(description = "단어카드 목록 (WORD_ORDER 전용 선택)", required = false)
    val wordCards: List<WordCardResponse>?,
)

@Schema(title = "SceneResponse", description = "이야기 장면 응답")
class SceneResponse(
    @field:Schema(description = "장면 고유 식별자", example = "sc_banggui_01", required = true)
    val sceneId: String,

    @field:Schema(description = "이야기 안에서 장면이 진행되는 순서", example = "1", required = true)
    val sceneOrder: Int,

    @field:Schema(description = "장면 종류", example = "NARRATION", required = true)
    val sceneType: String,

    @field:Schema(description = "장면 상황과 대화 맥락", example = "옛날 어느 마을에 방귀를 아주 크게 뀌는 며느리가 살았습니다.", required = true)
    val sceneDescription: String,

    @field:Schema(description = "대화 캐릭터 코드", example = "ch_banggui_daughter_in_law", required = false)
    val characterName: String?,

    @field:Schema(description = "대화 캐릭터 표시명", example = "방귀쟁이 며느리", required = false)
    val characterDisplayName: String?,

    @field:Schema(description = "장면 시작 시 캐릭터 고정 첫 대사", example = "ㅇㅇ아, 내 방귀가 너무 크다는 걸 알면 가족들이 나를 이상하게 생각하지 않을까?", required = false)
    val characterOpening: String?,

    @field:Schema(description = "장면 종료 시 캐릭터 고정 마지막 대사", example = "그래도 아직은 못 말하겠어. 조금만 더 참아 볼게.", required = false)
    val characterClosing: String?,

    @field:Schema(description = "캐릭터 첫 대사 음성 참조", required = false)
    val characterOpeningAudio: String?,

    @field:Schema(description = "캐릭터 마지막 대사 음성 참조", required = false)
    val characterClosingAudio: String?,

    @field:Schema(description = "내레이션 음성 참조 (NARRATION 장면 전용)", required = false)
    val narrationAudio: String?,

    @field:Schema(description = "장면의 갈등·고민 요약", example = "방귀를 참을지 솔직하게 말할지 고민한다.", required = false)
    val conflict: String?,

    @field:Schema(description = "장면에서 이끌어내고자 하는 발화 목표", example = "며느리의 입장을 이해하고 공감해준다.", required = false)
    val sceneGoal: String?,

    @field:Schema(description = "장면 목표 충족에 필요한 사고 요소", example = "[\"PERSPECTIVE\", \"EMOTION\"]", required = false)
    val requiredElements: List<String>?,

    @field:Schema(description = "목표 충족으로 종료하기 위한 최소 아이 발화 횟수", example = "3", required = false)
    val preferredTurns: Int?,

    @field:Schema(description = "장면에서 허용하는 최대 아이 발화 횟수", example = "4", required = false)
    val maxTurns: Int?,

    @field:Schema(description = "장면 미션 (DIALOGUE 전용 선택)", required = false)
    val mission: MissionResponse?,

    @field:Schema(description = "캐릭터 음성 메타 (DIALOGUE 전용 선택)", required = false)
    val characterVoice: CharacterVoiceResponse?,

    @field:Schema(description = "장면 이미지 URL", example = "/files/banggui-scene-01.png", required = false)
    val imageUrl: String?,

    @field:Schema(
        description = "대화 캐릭터 아바타 이미지 URL (DIALOGUE 전용 선택)",
        example = "/files/char-ch_banggui_daughter_in_law.png",
        required = false,
    )
    val characterImageUrl: String?,

    @field:Schema(description = "나레이션~대화 한 묶음을 나타내는 챕터 번호", example = "1", required = true)
    val chapter: Int,
)

fun sceneResponse(result: SceneResult): SceneResponse = SceneResponse(
    sceneId = result.sceneId,
    sceneOrder = result.sceneOrder,
    sceneType = result.sceneType.name,
    sceneDescription = result.sceneDescription,
    characterName = result.characterName,
    characterDisplayName = result.characterDisplayName,
    characterOpening = result.characterOpening,
    characterClosing = result.characterClosing,
    characterOpeningAudio = result.characterOpeningAudio,
    characterClosingAudio = result.characterClosingAudio,
    narrationAudio = result.narrationAudio,
    conflict = result.conflict,
    sceneGoal = result.sceneGoal,
    requiredElements = result.requiredElements?.map { it.name },
    preferredTurns = result.preferredTurns,
    maxTurns = result.maxTurns,
    mission = result.mission?.let {
        MissionResponse(
            goal = it.goal,
            examples = it.examples,
            type = it.type.name,
            wordCards = it.wordCards?.map { wordCard ->
                WordCardResponse(text = wordCard.text, correctOrder = wordCard.correctOrder)
            },
        )
    },
    characterVoice = result.characterVoice?.let {
        CharacterVoiceResponse(gender = it.gender.name, ageGroup = it.ageGroup.name, voiceProfile = it.voiceProfile)
    },
    imageUrl = result.imageUrl,
    characterImageUrl = result.characterImageUrl,
    chapter = result.chapter,
)
