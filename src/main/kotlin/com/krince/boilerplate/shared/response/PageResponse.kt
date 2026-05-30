package com.krince.boilerplate.shared.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "페이징 응답")
@JsonIgnoreProperties("first", "last")
data class PageResponse<T>(
    @field:Schema(description = "데이터 리스트", required = true)
    val content: List<T>,

    @field:Schema(description = "현재 페이지 (1-based)", example = "1", required = true)
    val page: Long,

    @field:Schema(description = "페이지 크기", example = "10", required = true)
    val size: Long,

    @field:Schema(description = "전체 데이터 개수", example = "100", required = true)
    val totalElements: Long,

    @field:Schema(description = "전체 페이지 수", example = "10", required = true)
    val totalPages: Long,

    @field:Schema(description = "다음 페이지 존재 여부", example = "true", required = true)
    val hasNext: Boolean,

    @field:Schema(description = "이전 페이지 존재 여부", example = "false", required = true)
    val hasPrevious: Boolean,

    @field:Schema(description = "첫 페이지 여부", example = "true", required = true)
    @field:JsonProperty("isFirst")
    val isFirst: Boolean,

    @field:Schema(description = "마지막 페이지 여부", example = "false", required = true)
    @field:JsonProperty("isLast")
    val isLast: Boolean,
)

fun <T> pageResponse(
    content: List<T>,
    page: Long,
    size: Long,
    totalElements: Long,
): PageResponse<T> {
    val totalPages = if (size == 0L) 0L else (totalElements + size - 1) / size
    val hasNext: Boolean = page < totalPages
    val hasPrevious: Boolean = page > 1
    val isFirst: Boolean = page == 1L
    val isLast: Boolean = page >= totalPages || totalPages == 1L

    return PageResponse(
        content = content,
        page = page,
        size = size,
        totalElements = totalElements,
        totalPages = totalPages,
        hasNext = hasNext,
        hasPrevious = hasPrevious,
        isFirst = isFirst,
        isLast = isLast,
    )
}