package com.krince.reminisce.infra.adapter.`in`.dto.wordbook.response

import com.fasterxml.jackson.annotation.JsonFormat
import com.krince.reminisce.application.port.`in`.wordbook.result.SavedWordResult
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(title = "WordbookResponse", description = "단어장 단어 응답")
class WordbookResponse(
    @field:Schema(description = "저장 단어 고유 식별자", example = "0194b2f0-1a2b-7c3d-9e4f-5a6b7c8d9e0f", required = true)
    val savedWordId: String,

    @field:Schema(description = "저장한 단어", example = "며느리", required = true)
    val word: String,

    @field:Schema(description = "단어의 쉬운 뜻", example = "아들의 아내를 부르는 말", required = false)
    val meaning: String?,

    @field:Schema(description = "단어를 만난 출처 장면 식별자", example = "sc-1-s_banggui_daughter_in_law_001", required = false)
    val sourceSceneId: String?,

    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @field:Schema(description = "생성일시", example = "2026-01-09 14:30:25", required = false)
    val createdAt: LocalDateTime?,
)

fun wordbookResponse(result: SavedWordResult): WordbookResponse = WordbookResponse(
    savedWordId = result.savedWordId,
    word = result.word,
    meaning = result.meaning,
    sourceSceneId = result.sourceSceneId,
    createdAt = result.createdAt,
)
