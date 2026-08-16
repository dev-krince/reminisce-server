package com.krince.reminisce.infra.adapter.out.profileanalysis

import com.krince.reminisce.application.port.out.storyprofile.ProfileAnalysisContext
import com.krince.reminisce.application.port.out.storyprofile.ProfileAnalysisPort
import com.krince.reminisce.application.port.out.storyprofile.ProfileAnalysisReport
import com.krince.reminisce.domain.model.storyprofile.InterestTopic
import com.krince.reminisce.domain.model.storyprofile.ProfileFinding
import com.krince.reminisce.domain.model.storyprofile.SpeechAreaAnalysis
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.openai.OpenAiChatOptions
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
@ConditionalOnProperty(name = ["analysis.engine"], havingValue = "openai")
class ProfileAnalysisOpenAiAdapter(
    chatClientBuilder: ChatClient.Builder,
    @Value("\${analysis.openai.model}") model: String,
    @Value("\${analysis.openai.temperature}") temperature: Double,
) : ProfileAnalysisPort {

    private val chatClient: ChatClient = chatClientBuilder.build()
    private val options: OpenAiChatOptions = OpenAiChatOptions.builder()
        .model(model)
        .temperature(temperature)
        .build()

    override fun analyze(context: ProfileAnalysisContext): ProfileAnalysisReport {
        val llmResult: ProfileAnalysisLlmResult = runCatching {
            chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(userPrompt(context))
                .options(options)
                .call()
                .entity(ProfileAnalysisLlmResult::class.java)
        }.getOrElse { cause ->
            logger.warn(cause) { "이야기 프로필 분석 LLM 호출 실패" }
            null
        } ?: throw IllegalStateException("이야기 프로필 분석에 실패했습니다.")

        return llmResult.toReport()
    }

    private fun userPrompt(context: ProfileAnalysisContext): String {
        val nameLine: String = context.childName?.let { "아이 이름: $it" }.orEmpty()
        val history: String = context.turns.joinToString("\n") { turn ->
            val speaker: String = if (turn.isChild) "아이" else "큐미"
            "$speaker: ${turn.text}"
        }

        return """
            $nameLine
            [큐미와 아이가 나눈 인터뷰 대화 전체]
            $history
        """.trimIndent()
    }

    companion object {
        private val SYSTEM_PROMPT: String = """
            당신은 유치원~초등 저학년 아이의 언어 발달을 살피는 분석가입니다.
            큐미(로봇 친구)와 아이가 나눈 인터뷰 대화 전체를 읽고, 아이의 '이야기 프로필'을 만듭니다.
            보호자가 읽을 자료이므로 따뜻하고 쉬운 한국어로, 아이를 평가·훈계하지 않고 관찰한 사실을 씁니다.
            반드시 대화에 실제로 나타난 내용만 근거로 삼고, 지어내지 않습니다.

            출력 JSON 필드:

            interestTopics — 아이가 좋아하거나 관심을 보인 주제. 각 항목:
            - category: 주제 묶음 이름 (예: 관계, 자연, 감정, 동물, 놀이 등 자유롭게)
            - tags: 그 묶음에 속한 짧은 관심 태그 목록 (예: 친구, 토끼, 술래잡기)
            대화에서 확인된 것만, 1~4개 카테고리.

            strengths — 아이가 잘하는 이야기 방식 3개. 각 항목:
            - title: 짧은 제목 (예: 생각을 표현해요)
            - description: 대화 속 근거를 담은 한두 문장 설명

            practicePoints — 조금 더 연습하면 좋은 점 3개. 각 항목:
            - title: 짧은 제목 (예: 경험을 순서대로 이야기하기)
            - description: 비난 없이, 함께 연습하면 늘 수 있다는 톤의 한두 문장

            speechAnalyses — 말하기 분석 3개 영역. area는 정확히 "어휘", "표현", "논리" 하나씩. 각 항목:
            - area: 어휘 | 표현 | 논리
            - summary: 그 영역을 한 문장으로 요약
            - keywords: 아이 발화에서 뽑은 관련 키워드 1~3개
            - feature: 이번 대화에서 나타난 특징 한 문장
            - evidenceUtterance: 근거가 된 아이 발화 원문 그대로 (없으면 null)
            - strength: 잘한 점 한 문장
            - improvement: 다음에 연습하면 좋은 점 한 문장
        """.trimIndent()
    }
}

data class ProfileAnalysisLlmResult(
    val interestTopics: List<InterestTopicLlm> = emptyList(),
    val strengths: List<FindingLlm> = emptyList(),
    val practicePoints: List<FindingLlm> = emptyList(),
    val speechAnalyses: List<SpeechAreaLlm> = emptyList(),
) {
    fun toReport(): ProfileAnalysisReport = ProfileAnalysisReport(
        interestTopics = interestTopics
            .filter { !it.category.isNullOrBlank() }
            .map { InterestTopic(category = it.category.orEmpty(), tags = it.tags.filter(String::isNotBlank)) },
        strengths = strengths.mapNotNull { it.toFinding() },
        practicePoints = practicePoints.mapNotNull { it.toFinding() },
        speechAnalyses = speechAnalyses.mapNotNull { it.toAnalysis() },
    )
}

data class InterestTopicLlm(
    val category: String? = null,
    val tags: List<String> = emptyList(),
)

data class FindingLlm(
    val title: String? = null,
    val description: String? = null,
) {
    fun toFinding(): ProfileFinding? {
        val resolvedTitle: String = title?.takeIf { it.isNotBlank() } ?: return null

        return ProfileFinding(title = resolvedTitle, description = description.orEmpty())
    }
}

data class SpeechAreaLlm(
    val area: String? = null,
    val summary: String? = null,
    val keywords: List<String> = emptyList(),
    val feature: String? = null,
    val evidenceUtterance: String? = null,
    val strength: String? = null,
    val improvement: String? = null,
) {
    fun toAnalysis(): SpeechAreaAnalysis? {
        val resolvedArea: String = area?.takeIf { it.isNotBlank() } ?: return null

        return SpeechAreaAnalysis(
            area = resolvedArea,
            summary = summary.orEmpty(),
            keywords = keywords.filter(String::isNotBlank),
            feature = feature.orEmpty(),
            evidenceUtterance = evidenceUtterance?.takeIf { it.isNotBlank() },
            strength = strength.orEmpty(),
            improvement = improvement.orEmpty(),
        )
    }
}
