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

    test("start는 첫 활성 단계(기본: 자유대화)·진행중 상태·턴 0으로 인터뷰를 만든다") {
        val interview = ProfileInterview.start(childId, startedAt)

        interview.status shouldBe ProfileInterviewStatus.IN_PROGRESS
        interview.currentStage shouldBe InterviewStage.FREE_TALK
        interview.stageChildTurnCount shouldBe 0
        interview.totalChildTurnCount shouldBe 0
        interview.startedAt shouldBe startedAt
    }

    test("기본 설정(1/1/0/0/0/1)에서는 답할 때마다 0인 단계를 건너뛰며 자유대화→경험→아이질문→마무리로 진행한다") {
        var interview = ProfileInterview.start(childId, startedAt)

        interview = interview.advanceOnChildTurn(startedAt.plusMinutes(1))
        interview.currentStage shouldBe InterviewStage.EXPERIENCE

        interview = interview.advanceOnChildTurn(startedAt.plusMinutes(2))
        interview.currentStage shouldBe InterviewStage.CHILD_QUESTION

        interview = interview.advanceOnChildTurn(startedAt.plusMinutes(3))
        interview.currentStage shouldBe InterviewStage.CLOSING
        interview.totalChildTurnCount shouldBe 3
        interview.status shouldBe ProfileInterviewStatus.IN_PROGRESS
    }

    test("설정으로 단계 턴 수를 늘리면 그 수만큼 답해야 다음 단계로 넘어간다") {
        val stageTurns = mapOf(InterviewStage.FREE_TALK to 2)
        var interview = ProfileInterview.start(childId, startedAt, stageTurns)

        interview = interview.advanceOnChildTurn(startedAt.plusMinutes(1), stageTurns)
        interview.currentStage shouldBe InterviewStage.FREE_TALK
        interview.stageChildTurnCount shouldBe 1

        interview = interview.advanceOnChildTurn(startedAt.plusMinutes(2), stageTurns)
        interview.currentStage shouldBe InterviewStage.EXPERIENCE
        interview.stageChildTurnCount shouldBe 0
    }

    test("첫 단계가 0이면 시작 단계 자체가 다음 활성 단계다") {
        val stageTurns = mapOf(InterviewStage.FREE_TALK to 0, InterviewStage.EXPERIENCE to 1)

        val interview = ProfileInterview.start(childId, startedAt, stageTurns)

        interview.currentStage shouldBe InterviewStage.EXPERIENCE
    }

    test("totalTargetTurns는 설정을 반영한 전체 답 횟수를 계산한다 (기본 3)") {
        ProfileInterview.totalTargetTurns() shouldBe 3
        ProfileInterview.totalTargetTurns(
            mapOf(InterviewStage.STORY_LISTENING to 2, InterviewStage.CHILD_QUESTION to 0),
        ) shouldBe 4
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
