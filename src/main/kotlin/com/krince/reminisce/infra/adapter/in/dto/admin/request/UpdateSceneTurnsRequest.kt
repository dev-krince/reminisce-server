package com.krince.reminisce.infra.adapter.`in`.dto.admin.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank

@Schema(title = "UpdateSceneTurnsRequest", description = "이야기 대화 장면의 최소·최대 아이 발화 횟수 변경 요청. 보낸 값만 변경됩니다.")
class UpdateSceneTurnsRequest(
    @field:Schema(description = "관리키 (고정값: reminisce). 일치하지 않으면 403", example = "reminisce", required = true)
    @field:NotBlank(message = "관리키는 비어있을 수 없습니다.")
    val adminKey: String,

    @field:Schema(description = "최소 발화(preferredTurns) — 목표를 충족했을 때 장면을 통과시키기 위한 최소 아이 발화 횟수. 1~10, 생략하면 기존 값 유지", example = "2", required = false)
    @field:Min(1) @field:Max(10)
    val preferredTurns: Int? = null,

    @field:Schema(description = "최대 발화(maxTurns) — 목표 미달이어도 장면을 끝내는 최대 아이 발화 횟수. 1~10, 생략하면 기존 값 유지. 최소 발화보다 작을 수 없음", example = "4", required = false)
    @field:Min(1) @field:Max(10)
    val maxTurns: Int? = null,
)
