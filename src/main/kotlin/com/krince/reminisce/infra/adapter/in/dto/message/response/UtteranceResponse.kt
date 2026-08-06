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
)

fun utteranceResponse(result: UtteranceResult): UtteranceResponse = UtteranceResponse(
    messageId = result.messageId,
    sceneId = result.sceneId,
    speakerType = result.speakerType,
    turnOrder = result.turnOrder,
    text = result.text,
    createdAt = result.createdAt,
)
