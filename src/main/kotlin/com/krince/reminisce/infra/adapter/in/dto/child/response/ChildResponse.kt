package com.krince.reminisce.infra.adapter.`in`.dto.child.response

import com.fasterxml.jackson.annotation.JsonFormat
import com.krince.reminisce.application.port.`in`.child.result.ChildResult
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(title = "ChildResponse", description = "아이 프로필 응답")
class ChildResponse(
    @field:Schema(description = "아이 고유 식별자", example = "e443e5c3-0243-4d28-ba79-37cf3b923023", required = true)
    val childId: String,

    @field:Schema(description = "아이 애칭", example = "토토", required = true)
    val nickname: String,

    @field:Schema(description = "출생연도", example = "2019", required = true)
    val birthYear: Int,

    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @field:Schema(description = "생성일시", example = "2026-01-09 14:30:25", required = true)
    val createdDate: LocalDateTime,
)

fun childResponse(childResult: ChildResult): ChildResponse = ChildResponse(
    childId = childResult.childId,
    nickname = childResult.nickname,
    birthYear = childResult.birthYear,
    createdDate = childResult.createdDate,
)
