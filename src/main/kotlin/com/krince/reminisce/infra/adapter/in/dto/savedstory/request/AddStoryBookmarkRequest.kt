package com.krince.reminisce.infra.adapter.`in`.dto.savedstory.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(title = "AddStoryBookmarkRequest", description = "이야기 찜 추가 요청")
class AddStoryBookmarkRequest(
    @field:Schema(description = "찜할 이야기 고유 식별자", example = "s_banggui_daughter_in_law_001", required = true)
    @field:NotBlank(message = "이야기 식별자는 비어있을 수 없습니다.")
    val storyId: String,
)
