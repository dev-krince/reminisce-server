package com.krince.reminisce.infra.adapter.`in`.dto.savedstory.response

import com.fasterxml.jackson.annotation.JsonFormat
import com.krince.reminisce.application.port.`in`.savedstory.result.BookmarkedStoryResult
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(title = "BookmarkedStoryResponse", description = "찜한 이야기 응답")
class BookmarkedStoryResponse(
    @field:Schema(description = "찜한 이야기 고유 식별자", example = "0194b2f0-1a2b-7c3d-9e4f-5a6b7c8d9e0f", required = true)
    val savedStoryId: String,

    @field:Schema(description = "찜한 이야기 식별자", example = "s_banggui_daughter_in_law_001", required = true)
    val storyId: String,

    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @field:Schema(description = "생성일시", example = "2026-01-09 14:30:25", required = false)
    val createdAt: LocalDateTime?,
)

fun bookmarkedStoryResponse(result: BookmarkedStoryResult): BookmarkedStoryResponse = BookmarkedStoryResponse(
    savedStoryId = result.savedStoryId,
    storyId = result.storyId,
    createdAt = result.createdAt,
)
