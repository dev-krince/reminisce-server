package com.krince.reminisce.infra.adapter.`in`.dto.admin.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank

@Schema(title = "UpdateInterviewStageTurnsRequest", description = "큐미 인터뷰 단계별 아이 답 횟수 변경 요청. 각 단계 0~10, 0이면 그 단계를 건너뜁니다. 전부 0은 거부됩니다.")
class UpdateInterviewStageTurnsRequest(
    @field:Schema(description = "관리키 (고정값: reminisce). 일치하지 않으면 403", example = "reminisce", required = true)
    @field:NotBlank(message = "관리키는 비어있을 수 없습니다.")
    val adminKey: String,

    @field:Schema(description = "1단계 자유롭게 이야기하기 — 큐미가 아이의 관심사를 묻는 단계(\"어떤 이야기를 좋아해?\")의 아이 답 횟수. 0~10, 0이면 건너뜀", example = "1", required = true)
    @field:Min(0) @field:Max(10)
    val freeTalk: Int,

    @field:Schema(description = "2단계 경험 이야기하기 — 관심사와 관련된 실제 경험을 묻는 단계(\"실제로 본 적 있어?\")의 아이 답 횟수. 0~10, 0이면 건너뜀", example = "1", required = true)
    @field:Min(0) @field:Max(10)
    val experience: Int,

    @field:Schema(description = "3단계 짧은 이야기 듣기 — 큐미가 관심사를 소재로 즉석 이야기를 들려주고 이해를 확인하는 단계의 아이 답 횟수. 0~10, 0이면 건너뜀", example = "0", required = true)
    @field:Min(0) @field:Max(10)
    val storyListening: Int,

    @field:Schema(description = "4단계 등장인물 마음 생각하기 — 이야기 속 인물의 기분과 이유를 묻는 단계의 아이 답 횟수. 0~10, 0이면 건너뜀 (3단계를 건너뛰면 함께 0 권장)", example = "0", required = true)
    @field:Min(0) @field:Max(10)
    val characterFeeling: Int,

    @field:Schema(description = "5단계 이야기 이어가기 — 다음 장면을 상상하게 하는 단계(\"그다음엔 무슨 일이 생길까?\")의 아이 답 횟수. 0~10, 0이면 건너뜀 (3단계를 건너뛰면 함께 0 권장)", example = "0", required = true)
    @field:Min(0) @field:Max(10)
    val storyContinuation: Int,

    @field:Schema(description = "6단계 아이가 질문하기 — 아이가 큐미에게 질문할 차례를 주는 단계의 아이 답 횟수. 0~10, 0이면 건너뜀", example = "1", required = true)
    @field:Min(0) @field:Max(10)
    val childQuestion: Int,
)
