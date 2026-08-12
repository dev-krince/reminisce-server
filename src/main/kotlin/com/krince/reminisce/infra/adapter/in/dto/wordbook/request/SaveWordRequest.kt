package com.krince.reminisce.infra.adapter.`in`.dto.wordbook.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(title = "SaveWordRequest", description = "단어장 단어 저장 요청")
class SaveWordRequest(
    @field:Schema(description = "저장할 단어", example = "며느리", required = true)
    @field:NotBlank(message = "단어는 비어있을 수 없습니다.")
    val word: String,

    @field:Schema(description = "단어의 쉬운 뜻", example = "아들의 아내를 부르는 말", required = false)
    val meaning: String?,

    @field:Schema(description = "단어를 만난 출처 장면 식별자", example = "sc-1-s_banggui_daughter_in_law_001", required = false)
    val sourceSceneId: String?,
)
