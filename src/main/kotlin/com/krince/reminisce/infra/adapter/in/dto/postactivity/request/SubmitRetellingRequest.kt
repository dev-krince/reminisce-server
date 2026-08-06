package com.krince.reminisce.infra.adapter.`in`.dto.postactivity.request

import io.swagger.v3.oas.annotations.media.Schema

@Schema(title = "SubmitRetellingRequest", description = "이야기 재구성 발화 제출 요청")
class SubmitRetellingRequest(
    @field:Schema(description = "아이 재구성 발화 오디오(스텁 단계에서는 텍스트로 대체)", example = "방귀쟁이 며느리는 시아버지 덕분에 방귀를 뀔 수 있었어요", required = true)
    val audio: String,
)
