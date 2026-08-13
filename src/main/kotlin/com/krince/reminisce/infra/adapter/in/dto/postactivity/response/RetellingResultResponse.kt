package com.krince.reminisce.infra.adapter.`in`.dto.postactivity.response

import com.krince.reminisce.application.port.`in`.postactivity.result.RetellingResult
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(title = "RetellingResultResponse", description = "이야기 재구성 발화 제출 결과 응답")
data class RetellingResultResponse(
    @field:Schema(description = "재구성 발화 텍스트", required = true)
    val retellingText: String,

    @field:Schema(description = "재구성 녹음 음성 파일 URL. 음성이 제출되지 않으면 null")
    val retellingAudioUrl: String?,

    @field:Schema(description = "완료 일시", required = true)
    val completedAt: LocalDateTime,

    @field:Schema(description = "세션 상태", required = true)
    val status: String,

    @field:Schema(description = "장면 순서대로 분절한 재구성 텍스트. 없으면 null", required = false)
    val retellingSegments: List<String>?,
)

fun retellingResultResponse(result: RetellingResult): RetellingResultResponse = RetellingResultResponse(
    retellingText = result.retellingText,
    retellingAudioUrl = result.retellingAudioUrl,
    completedAt = result.completedAt,
    status = result.status.name,
    retellingSegments = result.retellingSegments,
)
