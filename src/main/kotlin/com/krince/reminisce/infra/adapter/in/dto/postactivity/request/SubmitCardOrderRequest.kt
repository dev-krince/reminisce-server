package com.krince.reminisce.infra.adapter.`in`.dto.postactivity.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotEmpty

@Schema(title = "SubmitCardOrderRequest", description = "카드 순서 제출 요청")
data class SubmitCardOrderRequest(
    @field:Schema(description = "제출한 카드 id 순서 목록", required = true)
    @field:NotEmpty
    val order: List<String>,
)
