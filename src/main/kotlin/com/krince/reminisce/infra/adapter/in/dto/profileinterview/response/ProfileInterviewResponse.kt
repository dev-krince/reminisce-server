package com.krince.reminisce.infra.adapter.`in`.dto.profileinterview.response

import com.krince.reminisce.application.port.`in`.profileinterview.result.ProfileInterviewResult
import io.swagger.v3.oas.annotations.media.Schema

@Schema(title = "ProfileInterviewResponse", description = "프로필 인터뷰 응답")
class ProfileInterviewResponse(
    @field:Schema(description = "프로필 인터뷰 고유 식별자", example = "01920000-0000-7000-8000-000000000020", required = true)
    val interviewId: String,

    @field:Schema(description = "아이 고유 식별자", example = "e443e5c3-0243-4d28-ba79-37cf3b923023", required = true)
    val childId: String,

    @field:Schema(description = "인터뷰 상태", example = "IN_PROGRESS", required = true)
    val status: String,

    @field:Schema(description = "현재 진행 단계", example = "FREE_TALK", required = true)
    val stage: String,

    @field:Schema(description = "큐미가 아이에게 건네는 말", example = "안녕 민서야! 나는 큐미야! 오늘은 민서랑 재미있는 이야기를 만들어 볼 거야. 민서는 어떤 이야기를 좋아해?", required = true)
    val qumiText: String,

    @field:Schema(description = "큐미 발화 음성 참조", required = false)
    val qumiAudio: String?,
)

fun profileInterviewResponse(result: ProfileInterviewResult): ProfileInterviewResponse = ProfileInterviewResponse(
    interviewId = result.interviewId,
    childId = result.childId,
    status = result.status,
    stage = result.stage,
    qumiText = result.qumiText,
    qumiAudio = result.qumiAudio,
)
