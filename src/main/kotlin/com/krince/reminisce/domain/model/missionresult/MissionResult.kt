package com.krince.reminisce.domain.model.missionresult

import com.krince.reminisce.domain.model.missionresult.vo.MissionResultId
import com.krince.reminisce.domain.model.speakingsession.vo.SpeakingSessionId
import com.krince.reminisce.shared.util.UuidGenerator
import java.time.LocalDateTime

data class MissionResult(
    val id: MissionResultId,
    val sessionId: SpeakingSessionId,
    val sceneId: String,
    val completed: Boolean,
    val attemptCount: Int,
    val completedAt: LocalDateTime? = null,
    val createdDate: LocalDateTime? = null,
    val modifiedDate: LocalDateTime? = null,
) {
    fun resubmit(passed: Boolean, at: LocalDateTime): MissionResult =
        copy(
            completed = completed || passed,
            attemptCount = attemptCount + 1,
            completedAt = resolveCompletedAt(passed, at),
        )

    private fun resolveCompletedAt(passed: Boolean, at: LocalDateTime): LocalDateTime? {
        if (completed) {
            return completedAt
        }
        if (passed) {
            return at
        }

        return null
    }

    companion object {
        fun firstSubmission(
            sessionId: SpeakingSessionId,
            sceneId: String,
            completed: Boolean,
            at: LocalDateTime,
        ): MissionResult = MissionResult(
            id = MissionResultId(UuidGenerator.generate()),
            sessionId = sessionId,
            sceneId = sceneId,
            completed = completed,
            attemptCount = 1,
            completedAt = completedAtOnFirst(completed, at),
        )

        private fun completedAtOnFirst(completed: Boolean, at: LocalDateTime): LocalDateTime? {
            if (completed) {
                return at
            }

            return null
        }
    }
}
