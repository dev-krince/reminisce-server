package com.krince.reminisce.domain.model.profileinterview

import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.profileinterview.vo.InterviewStage
import com.krince.reminisce.domain.model.profileinterview.vo.ProfileInterviewStatus
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDateTime

@Tags("test", "unitTest")
@DisplayName("ProfileInterview 도메인 단위테스트")
class ProfileInterviewTest : FunSpec({

    val childId = ChildId("child-1")
    val startedAt = LocalDateTime.of(2026, 8, 17, 10, 0)

    test("start는 자유대화 단계·진행중 상태·턴 0으로 인터뷰를 만든다") {
        val interview = ProfileInterview.start(childId, startedAt)

        interview.status shouldBe ProfileInterviewStatus.IN_PROGRESS
        interview.currentStage shouldBe InterviewStage.FREE_TALK
        interview.stageChildTurnCount shouldBe 0
        interview.totalChildTurnCount shouldBe 0
        interview.startedAt shouldBe startedAt
    }

    test("자유대화 단계에서 아이가 2번 답하면 경험 단계로 넘어가고 단계 턴 수가 초기화된다") {
        var interview = ProfileInterview.start(childId, startedAt)

        interview = interview.advanceOnChildTurn(startedAt.plusMinutes(1))
        interview.currentStage shouldBe InterviewStage.FREE_TALK
        interview.stageChildTurnCount shouldBe 1

        interview = interview.advanceOnChildTurn(startedAt.plusMinutes(2))

        interview.currentStage shouldBe InterviewStage.EXPERIENCE
        interview.stageChildTurnCount shouldBe 0
        interview.totalChildTurnCount shouldBe 2
    }

    test("단계별 목표 턴을 모두 채우면 마무리 단계에 도달한다 (총 10턴)") {
        var interview = ProfileInterview.start(childId, startedAt)
        val totalTurns = InterviewStage.entries.sumOf { it.targetChildTurns }

        repeat(totalTurns) { interview = interview.advanceOnChildTurn(startedAt.plusMinutes(it + 1L)) }

        totalTurns shouldBe 10
        interview.currentStage shouldBe InterviewStage.CLOSING
        interview.totalChildTurnCount shouldBe 10
        interview.status shouldBe ProfileInterviewStatus.IN_PROGRESS
    }

    test("complete는 상태만 완료로 바꾸고 진행 기록을 보존한다") {
        var interview = ProfileInterview.start(childId, startedAt)
        interview = interview.advanceOnChildTurn(startedAt.plusMinutes(1))
        val completedAt = startedAt.plusMinutes(10)

        val completed = interview.complete(completedAt)

        completed.status shouldBe ProfileInterviewStatus.COMPLETED
        completed.totalChildTurnCount shouldBe 1
        completed.lastActivityAt shouldBe completedAt
        completed.interviewId shouldBe interview.interviewId
    }

    test("단계 순서는 자유대화→경험→이야기듣기→감정이해→이야기잇기→아이질문→마무리다") {
        InterviewStage.FREE_TALK.next() shouldBe InterviewStage.EXPERIENCE
        InterviewStage.EXPERIENCE.next() shouldBe InterviewStage.STORY_LISTENING
        InterviewStage.STORY_LISTENING.next() shouldBe InterviewStage.CHARACTER_FEELING
        InterviewStage.CHARACTER_FEELING.next() shouldBe InterviewStage.STORY_CONTINUATION
        InterviewStage.STORY_CONTINUATION.next() shouldBe InterviewStage.CHILD_QUESTION
        InterviewStage.CHILD_QUESTION.next() shouldBe InterviewStage.CLOSING
        InterviewStage.CLOSING.next() shouldBe null
    }
})
