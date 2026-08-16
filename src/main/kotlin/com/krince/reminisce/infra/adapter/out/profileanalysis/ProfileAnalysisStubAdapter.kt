package com.krince.reminisce.infra.adapter.out.profileanalysis

import com.krince.reminisce.application.port.out.storyprofile.ProfileAnalysisContext
import com.krince.reminisce.application.port.out.storyprofile.ProfileAnalysisPort
import com.krince.reminisce.application.port.out.storyprofile.ProfileAnalysisReport
import com.krince.reminisce.domain.model.storyprofile.InterestTopic
import com.krince.reminisce.domain.model.storyprofile.ProfileFinding
import com.krince.reminisce.domain.model.storyprofile.SpeechAreaAnalysis
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(name = ["analysis.engine"], havingValue = "stub", matchIfMissing = true)
class ProfileAnalysisStubAdapter : ProfileAnalysisPort {

    override fun analyze(context: ProfileAnalysisContext): ProfileAnalysisReport = ProfileAnalysisReport(
        interestTopics = listOf(
            InterestTopic(category = "관계", tags = listOf("친구", "동물")),
            InterestTopic(category = "감정", tags = listOf("기쁨")),
        ),
        strengths = listOf(
            ProfileFinding(title = "생각을 표현해요", description = "자신의 생각과 감정을 말할 수 있어요."),
            ProfileFinding(title = "등장인물의 마음을 생각해요", description = "인물이 왜 그런 기분인지 이야기할 수 있었어요."),
            ProfileFinding(title = "상황에 맞는 해결 방법을 생각해요", description = "어려운 상황에서 어떻게 도와줄지 생각할 수 있었어요."),
        ),
        practicePoints = listOf(
            ProfileFinding(title = "경험을 순서대로 이야기하기", description = "하나의 이야기로 연결하는 연습을 더 해보면 좋아요."),
            ProfileFinding(title = "생각을 구체적으로 설명하기", description = "왜 그렇게 생각했는지 자세히 설명하는 연습이 도움이 돼요."),
            ProfileFinding(title = "이야기 속 세부 내용 기억하기", description = "세부 정보를 기억하고 연결하는 경험을 더 해보면 좋아요."),
        ),
        speechAnalyses = listOf(
            SpeechAreaAnalysis(
                area = "어휘",
                summary = "상황에 맞는 단어를 자연스럽게 사용했어요.",
                keywords = listOf("귀엽다", "재미있다"),
                feature = "이야기 상황에 맞는 단어를 사용했어요.",
                evidenceUtterance = null,
                strength = "아는 단어를 자신의 문장에 자연스럽게 사용했어요.",
                improvement = "같은 뜻의 다른 표현도 함께 사용해보면 좋아요.",
            ),
            SpeechAreaAnalysis(
                area = "표현",
                summary = "인물의 마음을 이해하고 따뜻한 말을 건넸어요.",
                keywords = listOf("감정 이해", "위로되는 말"),
                feature = "등장인물의 기분을 헤아려 말했어요.",
                evidenceUtterance = null,
                strength = "상대의 마음에 맞는 말을 골라 했어요.",
                improvement = "기분을 나타내는 말을 더 다양하게 써보면 좋아요.",
            ),
            SpeechAreaAnalysis(
                area = "논리",
                summary = "이유와 해결 방법을 이야기했어요.",
                keywords = listOf("생각과 이유", "해결 방법"),
                feature = "왜 그런지 이유를 붙여 말했어요.",
                evidenceUtterance = null,
                strength = "문제 상황에서 해결 방법을 떠올렸어요.",
                improvement = "생각의 이유를 한 가지 더 붙여 말해보면 좋아요.",
            ),
        ),
    )
}
