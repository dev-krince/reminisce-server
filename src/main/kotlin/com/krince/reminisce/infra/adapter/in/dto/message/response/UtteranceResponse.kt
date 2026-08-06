package com.krince.reminisce.infra.adapter.`in`.dto.message.response

import com.fasterxml.jackson.annotation.JsonFormat
import com.krince.reminisce.application.port.`in`.message.result.UtteranceResult
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(title = "UtteranceResponse", description = "아이 발화 저장 응답")
class UtteranceResponse(
    @field:Schema(description = "메시지 고유 식별자", example = "01920000-0000-7000-8000-000000000100", required = true)
    val messageId: String,

    @field:Schema(description = "발화가 발생한 장면 식별자", example = "sc_banggui_03", required = true)
    val sceneId: String,

    @field:Schema(description = "발화 주체", example = "CHILD", required = true)
    val speakerType: String,

    @field:Schema(description = "세션 전체에서 메시지가 발생한 순서", example = "1", required = true)
    val turnOrder: Long,

    @field:Schema(description = "확정 텍스트", example = "며느리가 참 힘들었겠어요", required = true)
    val text: String,

    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @field:Schema(description = "생성 시각", example = "2026-01-09 14:30:25", required = true)
    val createdAt: LocalDateTime,

    @field:Schema(description = "이번 발화의 중심 의도", example = "PERSPECTIVE", required = true)
    val childIntent: String,

    @field:Schema(description = "아이 말의 핵심 뜻", example = "며느리가 힘들었을 것이다")
    val mainPoint: String?,

    @field:Schema(description = "이번 발화의 유효성", example = "VALID", required = true)
    val validity: String,

    @field:Schema(description = "이번 발화에서 확인된 사고 요소와 근거", required = true)
    val detectedElements: List<DetectedElementResponse>,

    @field:Schema(description = "현재 장면에서 확인된 사고 요소 누적", example = "[\"EMOTION\"]", required = true)
    val accumulatedElements: List<String>,

    @field:Schema(description = "아직 확인되지 않은 필수 사고 요소", example = "[\"PERSPECTIVE\"]", required = true)
    val missingElements: List<String>,

    @field:Schema(description = "이번 발화에 대한 확정 응답 모드", example = "NORMAL", required = true)
    val mode: String,

    @field:Schema(description = "장면 종료 이유", example = "GOAL_MET")
    val sceneEndReason: String?,

    @field:Schema(description = "현재 장면 목표 충족 여부", example = "false", required = true)
    val sceneGoalMet: Boolean,

    @field:Schema(description = "GUIDED 모드에서 유도할 사고 요소", example = "PERSPECTIVE")
    val guidanceTarget: String?,

    @field:Schema(description = "이번 발화에 대한 캐릭터 대사", required = true)
    val characterReply: CharacterReplyResponse,
)

@Schema(title = "DetectedElementResponse", description = "확인된 사고 요소와 근거")
class DetectedElementResponse(
    @field:Schema(description = "사고 요소 유형", example = "EMOTION", required = true)
    val type: String,

    @field:Schema(description = "발화 원문에서 확인된 근거", example = "힘들", required = true)
    val evidence: String,
)

@Schema(title = "CharacterReplyResponse", description = "아이 발화에 대한 캐릭터 대사")
class CharacterReplyResponse(
    @field:Schema(description = "캐릭터 메시지 고유 식별자", example = "01920000-0000-7000-8000-000000000101", required = true)
    val messageId: String,

    @field:Schema(description = "발화 주체", example = "CHARACTER", required = true)
    val speakerType: String,

    @field:Schema(description = "세션 전체에서 메시지가 발생한 순서", example = "2", required = true)
    val turnOrder: Long,

    @field:Schema(description = "캐릭터 대사 텍스트", example = "네 이야기를 잘 들었어.", required = true)
    val text: String,
)

fun utteranceResponse(result: UtteranceResult): UtteranceResponse = UtteranceResponse(
    messageId = result.messageId,
    sceneId = result.sceneId,
    speakerType = result.speakerType,
    turnOrder = result.turnOrder,
    text = result.text,
    createdAt = result.createdAt,
    childIntent = result.childIntent,
    mainPoint = result.mainPoint,
    validity = result.validity,
    detectedElements = result.detectedElements.map { DetectedElementResponse(type = it.type, evidence = it.evidence) },
    accumulatedElements = result.accumulatedElements,
    missingElements = result.missingElements,
    mode = result.mode,
    sceneEndReason = result.sceneEndReason,
    sceneGoalMet = result.sceneGoalMet,
    guidanceTarget = result.guidanceTarget,
    characterReply = CharacterReplyResponse(
        messageId = result.characterReply.messageId,
        speakerType = result.characterReply.speakerType,
        turnOrder = result.characterReply.turnOrder,
        text = result.characterReply.text,
    ),
)
