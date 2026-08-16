package com.krince.reminisce.domain.model.profileinterview

import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.profileinterview.vo.InterviewStage
import com.krince.reminisce.domain.model.profileinterview.vo.ProfileInterviewId
import com.krince.reminisce.domain.model.profileinterview.vo.ProfileInterviewStatus
import com.krince.reminisce.shared.util.UuidGenerator
import java.time.LocalDateTime

class ProfileInterview(
    val interviewId: ProfileInterviewId,
    val childId: ChildId,
    val status: ProfileInterviewStatus,
    val currentStage: InterviewStage,
    val stageChildTurnCount: Int,
    val totalChildTurnCount: Int,
    val startedAt: LocalDateTime,
    val lastActivityAt: LocalDateTime,
) {

    fun advanceOnChildTurn(at: LocalDateTime): ProfileInterview {
        val advancedStageTurns: Int = stageChildTurnCount + 1
        val stageFinished: Boolean = advancedStageTurns >= currentStage.targetChildTurns
        val nextStage: InterviewStage = if (stageFinished) {
            currentStage.next() ?: currentStage
        } else {
            currentStage
        }

        return ProfileInterview(
            interviewId = interviewId,
            childId = childId,
            status = status,
            currentStage = nextStage,
            stageChildTurnCount = if (stageFinished) 0 else advancedStageTurns,
            totalChildTurnCount = totalChildTurnCount + 1,
            startedAt = startedAt,
            lastActivityAt = at,
        )
    }

    fun complete(at: LocalDateTime): ProfileInterview = ProfileInterview(
        interviewId = interviewId,
        childId = childId,
        status = ProfileInterviewStatus.COMPLETED,
        currentStage = currentStage,
        stageChildTurnCount = stageChildTurnCount,
        totalChildTurnCount = totalChildTurnCount,
        startedAt = startedAt,
        lastActivityAt = at,
    )

    companion object {
        fun start(childId: ChildId, at: LocalDateTime): ProfileInterview = ProfileInterview(
            interviewId = ProfileInterviewId(UuidGenerator.generate()),
            childId = childId,
            status = ProfileInterviewStatus.IN_PROGRESS,
            currentStage = InterviewStage.FREE_TALK,
            stageChildTurnCount = 0,
            totalChildTurnCount = 0,
            startedAt = at,
            lastActivityAt = at,
        )
    }
}
