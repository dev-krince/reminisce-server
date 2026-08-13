package com.krince.reminisce.infra.adapter.`in`.dto.postactivity.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(title = "SubmitRetellingRequest", description = "이야기 재구성 발화 제출 요청")
class SubmitRetellingRequest(
    @field:NotBlank(message = "재구성 발화 텍스트는 비어있을 수 없습니다.")
    @field:Schema(description = "기기 STT로 확정한 아이 재구성 발화 텍스트", example = "방귀쟁이 며느리는 시아버지 덕분에 방귀를 뀔 수 있었어요", required = true)
    val text: String,

    @field:Schema(description = "장면 순서대로 분절한 재구성 텍스트. 선택 — 미제공 시 null", required = false)
    val sceneSegments: List<String>? = null,
)
