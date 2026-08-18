package com.krince.reminisce.infra.adapter.`in`.dto.admin.response

import com.krince.reminisce.application.port.`in`.admin.result.InterviewStageTurnsResult
import com.krince.reminisce.domain.model.profileinterview.vo.InterviewStage
import io.swagger.v3.oas.annotations.media.Schema

@Schema(title = "InterviewStageTurnsResponse", description = "큐미 인터뷰 단계별 아이 답 횟수 설정")
class InterviewStageTurnsResponse(
    @field:Schema(description = "1단계 자유롭게 이야기하기(관심사 묻기) 아이 답 횟수", example = "1", required = true)
    val freeTalk: Int,

    @field:Schema(description = "2단계 경험 이야기하기(실제 경험 묻기) 아이 답 횟수", example = "1", required = true)
    val experience: Int,

    @field:Schema(description = "3단계 짧은 이야기 듣기(즉석 이야기·이해 확인) 아이 답 횟수", example = "0", required = true)
    val storyListening: Int,

    @field:Schema(description = "4단계 등장인물 마음 생각하기(감정·이유) 아이 답 횟수", example = "0", required = true)
    val characterFeeling: Int,

    @field:Schema(description = "5단계 이야기 이어가기(상상) 아이 답 횟수", example = "0", required = true)
    val storyContinuation: Int,

    @field:Schema(description = "6단계 아이가 질문하기 아이 답 횟수", example = "1", required = true)
    val childQuestion: Int,

    @field:Schema(description = "인터뷰 전체 아이 답 횟수(합계). 이 수만큼 답하면 인터뷰가 완료됨", example = "3", required = true)
    val totalChildTurns: Int,
)

fun interviewStageTurnsResponse(result: InterviewStageTurnsResult): InterviewStageTurnsResponse =
    InterviewStageTurnsResponse(
        freeTalk = result.stageTurns[InterviewStage.FREE_TALK] ?: 0,
        experience = result.stageTurns[InterviewStage.EXPERIENCE] ?: 0,
        storyListening = result.stageTurns[InterviewStage.STORY_LISTENING] ?: 0,
        characterFeeling = result.stageTurns[InterviewStage.CHARACTER_FEELING] ?: 0,
        storyContinuation = result.stageTurns[InterviewStage.STORY_CONTINUATION] ?: 0,
        childQuestion = result.stageTurns[InterviewStage.CHILD_QUESTION] ?: 0,
        totalChildTurns = result.totalChildTurns,
    )
