package com.krince.reminisce.domain.model.postactivityresult

import com.krince.reminisce.domain.model.postactivityresult.vo.PostActivityResultId
import com.krince.reminisce.domain.model.speakingsession.vo.SpeakingSessionId
import com.krince.reminisce.shared.util.UuidGenerator
import java.time.LocalDateTime

data class PostActivityResult(
    val id: PostActivityResultId,
    val sessionId: SpeakingSessionId,
    val submittedOrder: List<String>,
    val isOrderCorrect: Boolean?,
    val attemptCount: Int,
    val retellingText: String? = null,
    val retellingAudioUrl: String? = null,
    val completedAt: LocalDateTime? = null,
    val createdDate: LocalDateTime? = null,
    val modifiedDate: LocalDateTime? = null,
) {
    fun resubmit(submittedOrder: List<String>, isOrderCorrect: Boolean): PostActivityResult =
        copy(
            submittedOrder = submittedOrder,
            isOrderCorrect = isOrderCorrect,
            attemptCount = attemptCount + 1,
        )

    fun completeWith(retellingText: String, retellingAudioUrl: String?, at: LocalDateTime): PostActivityResult =
        copy(retellingText = retellingText, retellingAudioUrl = retellingAudioUrl, completedAt = at)

    companion object {
        fun firstSubmission(
            sessionId: SpeakingSessionId,
            submittedOrder: List<String>,
            isOrderCorrect: Boolean,
        ): PostActivityResult = PostActivityResult(
            id = PostActivityResultId(UuidGenerator.generate()),
            sessionId = sessionId,
            submittedOrder = submittedOrder,
            isOrderCorrect = isOrderCorrect,
            attemptCount = 1,
        )
    }
}
