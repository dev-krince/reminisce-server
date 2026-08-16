package com.krince.reminisce.application.port.`in`.profileinterview.result

import com.krince.reminisce.domain.model.profileinterview.ProfileInterview

class ProfileInterviewResult(
    val interviewId: String,
    val childId: String,
    val status: String,
    val stage: String,
    val qumiText: String,
    val qumiAudio: String?,
    val created: Boolean,
) {
    companion object {
        fun from(
            interview: ProfileInterview,
            qumiText: String,
            qumiAudio: String?,
            created: Boolean,
        ): ProfileInterviewResult = ProfileInterviewResult(
            interviewId = interview.interviewId.value,
            childId = interview.childId.value,
            status = interview.status.name,
            stage = interview.currentStage.name,
            qumiText = qumiText,
            qumiAudio = qumiAudio,
            created = created,
        )
    }
}
