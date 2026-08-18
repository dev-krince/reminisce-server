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

    fun advanceOnChildTurn(at: LocalDateTime, stageTurns: Map<InterviewStage, Int> = emptyMap()): ProfileInterview {
        val advancedStageTurns: Int = stageChildTurnCount + 1
        val stageFinished: Boolean = advancedStageTurns >= targetTurns(currentStage, stageTurns)
        val nextStage: InterviewStage = if (stageFinished) {
            nextActiveStage(currentStage, stageTurns)
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
        fun start(
            childId: ChildId,
            at: LocalDateTime,
            stageTurns: Map<InterviewStage, Int> = emptyMap(),
        ): ProfileInterview = ProfileInterview(
            interviewId = ProfileInterviewId(UuidGenerator.generate()),
            childId = childId,
            status = ProfileInterviewStatus.IN_PROGRESS,
            currentStage = firstActiveStage(stageTurns),
            stageChildTurnCount = 0,
            totalChildTurnCount = 0,
            startedAt = at,
            lastActivityAt = at,
        )

        fun firstActiveStage(stageTurns: Map<InterviewStage, Int> = emptyMap()): InterviewStage =
            InterviewStage.entries.firstOrNull { it != InterviewStage.CLOSING && targetTurns(it, stageTurns) > 0 }
                ?: InterviewStage.CLOSING

        fun totalTargetTurns(stageTurns: Map<InterviewStage, Int> = emptyMap()): Int =
            InterviewStage.entries.filter { it != InterviewStage.CLOSING }.sumOf { targetTurns(it, stageTurns) }

        private fun targetTurns(stage: InterviewStage, stageTurns: Map<InterviewStage, Int>): Int =
            stageTurns[stage] ?: stage.targetChildTurns

        private fun nextActiveStage(current: InterviewStage, stageTurns: Map<InterviewStage, Int>): InterviewStage {
            var candidate: InterviewStage = current.next() ?: return current
            while (candidate != InterviewStage.CLOSING && targetTurns(candidate, stageTurns) <= 0) {
                candidate = candidate.next() ?: return InterviewStage.CLOSING
            }

            return candidate
        }
    }
}
