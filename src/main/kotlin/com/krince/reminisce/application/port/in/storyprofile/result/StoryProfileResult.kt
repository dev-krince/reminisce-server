package com.krince.reminisce.application.port.`in`.storyprofile.result

import com.krince.reminisce.domain.model.storyprofile.StoryProfile
import java.time.LocalDateTime

class InterestTopicResult(
    val category: String,
    val tags: List<String>,
)

class ProfileFindingResult(
    val title: String,
    val description: String,
)

class SpeechAreaAnalysisResult(
    val area: String,
    val summary: String,
    val keywords: List<String>,
    val feature: String,
    val evidenceUtterance: String?,
    val strength: String,
    val improvement: String,
)

class StoryProfileResult(
    val childId: String,
    val interviewId: String,
    val interestTopics: List<InterestTopicResult>,
    val strengths: List<ProfileFindingResult>,
    val practicePoints: List<ProfileFindingResult>,
    val speechAnalyses: List<SpeechAreaAnalysisResult>,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(profile: StoryProfile): StoryProfileResult = StoryProfileResult(
            childId = profile.childId.value,
            interviewId = profile.interviewId.value,
            interestTopics = profile.interestTopics.map { InterestTopicResult(it.category, it.tags) },
            strengths = profile.strengths.map { ProfileFindingResult(it.title, it.description) },
            practicePoints = profile.practicePoints.map { ProfileFindingResult(it.title, it.description) },
            speechAnalyses = profile.speechAnalyses.map {
                SpeechAreaAnalysisResult(
                    area = it.area,
                    summary = it.summary,
                    keywords = it.keywords,
                    feature = it.feature,
                    evidenceUtterance = it.evidenceUtterance,
                    strength = it.strength,
                    improvement = it.improvement,
                )
            },
            createdAt = profile.createdAt,
        )
    }
}
