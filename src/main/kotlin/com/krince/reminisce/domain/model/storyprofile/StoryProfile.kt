package com.krince.reminisce.domain.model.storyprofile

import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.profileinterview.vo.ProfileInterviewId
import com.krince.reminisce.domain.model.storyprofile.vo.StoryProfileId
import com.krince.reminisce.shared.util.UuidGenerator
import java.time.LocalDateTime

data class InterestTopic(
    val category: String,
    val tags: List<String>,
)

data class ProfileFinding(
    val title: String,
    val description: String,
)

data class SpeechAreaAnalysis(
    val area: String,
    val summary: String,
    val keywords: List<String>,
    val feature: String,
    val evidenceUtterance: String?,
    val strength: String,
    val improvement: String,
)

class StoryProfile(
    val profileId: StoryProfileId,
    val childId: ChildId,
    val interviewId: ProfileInterviewId,
    val interestTopics: List<InterestTopic>,
    val strengths: List<ProfileFinding>,
    val practicePoints: List<ProfileFinding>,
    val speechAnalyses: List<SpeechAreaAnalysis>,
    val createdAt: LocalDateTime,
) {
    fun interestTags(): List<String> = interestTopics.flatMap { it.tags }

    companion object {
        fun create(
            childId: ChildId,
            interviewId: ProfileInterviewId,
            interestTopics: List<InterestTopic>,
            strengths: List<ProfileFinding>,
            practicePoints: List<ProfileFinding>,
            speechAnalyses: List<SpeechAreaAnalysis>,
            at: LocalDateTime,
        ): StoryProfile = StoryProfile(
            profileId = StoryProfileId(UuidGenerator.generate()),
            childId = childId,
            interviewId = interviewId,
            interestTopics = interestTopics,
            strengths = strengths,
            practicePoints = practicePoints,
            speechAnalyses = speechAnalyses,
            createdAt = at,
        )
    }
}
