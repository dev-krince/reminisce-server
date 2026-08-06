package com.krince.reminisce.infra.adapter.`in`.dto.postactivity.response

import com.krince.reminisce.application.port.`in`.postactivity.result.RetellingResult
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(title = "RetellingResultResponse", description = "이야기 재구성 발화 제출 결과 응답")
data class RetellingResultResponse(
    @field:Schema(description = "재구성 발화 텍스트", required = true)
    val retellingText: String,

    @field:Schema(description = "완료 일시", required = true)
    val completedAt: LocalDateTime,

    @field:Schema(description = "세션 상태", required = true)
    val status: String,
)

fun retellingResultResponse(result: RetellingResult): RetellingResultResponse = RetellingResultResponse(
    retellingText = result.retellingText,
    completedAt = result.completedAt,
    status = result.status.name,
)
