package com.krince.reminisce.application.port.out.storyprofile

import com.krince.reminisce.application.port.out.conversation.ConversationTurn
import com.krince.reminisce.domain.model.storyprofile.InterestTopic
import com.krince.reminisce.domain.model.storyprofile.ProfileFinding
import com.krince.reminisce.domain.model.storyprofile.SpeechAreaAnalysis

class ProfileAnalysisContext(
    val childName: String?,
    val turns: List<ConversationTurn>,
)

class ProfileAnalysisReport(
    val interestTopics: List<InterestTopic>,
    val strengths: List<ProfileFinding>,
    val practicePoints: List<ProfileFinding>,
    val speechAnalyses: List<SpeechAreaAnalysis>,
)

interface ProfileAnalysisPort {
    fun analyze(context: ProfileAnalysisContext): ProfileAnalysisReport
}
