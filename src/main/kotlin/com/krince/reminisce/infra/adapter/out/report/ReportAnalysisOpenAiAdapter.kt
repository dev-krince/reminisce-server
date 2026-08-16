package com.krince.reminisce.infra.adapter.out.report

import com.krince.reminisce.application.port.out.report.ReportAnalysisContext
import com.krince.reminisce.application.port.out.report.ReportAnalysisPort
import com.krince.reminisce.application.port.out.report.ReportAnalysisResult
import com.krince.reminisce.application.port.out.report.ReportTurnContext
import com.krince.reminisce.application.port.out.report.ReportUtteranceContext
import com.krince.reminisce.application.port.out.report.RepresentativeSelection
import com.krince.reminisce.domain.model.report.GuideDirection
import com.krince.reminisce.domain.model.report.GuideQuestion
import com.krince.reminisce.domain.model.report.HomeGuide
import com.krince.reminisce.domain.model.report.ParticipationItem
import com.krince.reminisce.domain.model.report.ReportOverall
import com.krince.reminisce.domain.model.report.ReportSpeechAnalysis
import com.krince.reminisce.domain.model.report.SceneHighlight
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.openai.OpenAiChatOptions
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(name = ["analysis.engine"], havingValue = "openai")
class ReportAnalysisOpenAiAdapter(
    chatClientBuilder: ChatClient.Builder,
    @Value("\${analysis.openai.model}") model: String,
    @Value("\${analysis.openai.temperature}") temperature: Double,
) : ReportAnalysisPort {

    private val chatClient: ChatClient = chatClientBuilder.build()
    private val options: OpenAiChatOptions = OpenAiChatOptions.builder()
        .model(model)
        .temperature(temperature)
        .build()

    override fun analyze(context: ReportAnalysisContext): ReportAnalysisResult {
        val llmResult: ReportAnalysisLlmResult = runCatching {
            chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(userPrompt(context))
                .options(options)
                .call()
                .entity(ReportAnalysisLlmResult::class.java)
        }.getOrElse { cause ->
            throw IllegalStateException(ANALYSIS_FAILED_MESSAGE, cause)
        } ?: throw IllegalStateException(ANALYSIS_FAILED_MESSAGE)

        return llmResult.toResult()
    }

    private fun userPrompt(context: ReportAnalysisContext): String {
        val nameLine: String = context.childName?.let { "아이 이름: $it" }.orEmpty()
        val scenesBlock: String = context.scenes.joinToString("\n") { scene ->
            val goalPart: String = scene.goal?.let { " / 장면 목표: $it" }.orEmpty()
            "- 장면 ${scene.sceneId}: ${scene.description}$goalPart"
        }
        val turnsBlock: String = context.turns.joinToString("\n") { turnLine(it) }
        val analysesBlock: String = context.analyses.joinToString("\n") { analysisLine(it) }

        return """
            $nameLine
            [이야기 제목]
            ${context.storyTitle}
            [장면 정보]
            $scenesBlock
            [대화 전문]
            $turnsBlock
            [아이 발화 분석]
            $analysesBlock
        """.trimIndent()
    }

    private fun turnLine(turn: ReportTurnContext): String {
        val messageId: String = turn.messageId
            ?: return "[장면 ${turn.sceneId} / turnOrder ${turn.turnOrder}] 캐릭터: ${turn.text}"

        return "[장면 ${turn.sceneId} / turnOrder ${turn.turnOrder} / messageId=$messageId] 아이: ${turn.text}"
    }

    private fun analysisLine(analysis: ReportUtteranceContext): String {
        val elements: String = analysis.detectedElements
            .joinToString(", ") { "${it.type.name}(근거: ${it.evidence})" }

        return "- messageId=${analysis.messageId}: $elements"
    }

    companion object {
        private const val ANALYSIS_FAILED_MESSAGE: String = "보호자 리포트 분석에 실패했습니다."
        private val SYSTEM_PROMPT: String = """
            당신은 유치원~초등 저학년 아이의 말하기 활동을 보호자에게 전하는 리포트 작성자입니다.
            아이와 이야기 속 캐릭터가 나눈 세션 대화 전문과 발화 분석을 읽고, 보호자 리포트 생성물을 만듭니다.

            작성 원칙:
            - 반드시 대화에 실제로 나타난 내용만 근거로 삼고, 지어내지 않습니다.
            - 보호자가 읽을 따뜻하고 쉬운 한국어로 쓰고, 잘한 점을 먼저 씁니다.
            - "못한다", "부족하다" 같은 단정적인 부정 표현을 쓰지 않습니다.
            - DECISION, REASON 같은 내부 영문 태그를 출력 문구에 그대로 노출하지 않습니다.
            - 질문은 보호자가 아이에게 건네는 자연스러운 대화 형태로 씁니다.
            - 장면 특징과 대표 발화의 messageId는 반드시 [대화 전문]에 표기된 아이 발화 messageId 중에서 지정합니다.
            - 장면 특징(sceneHighlights)의 기준 발화는 그 장면에서 turnOrder가 가장 큰 마지막 아이 발화입니다.

            출력 JSON 필드:

            overall — 총평.
            - headline: 총평 한 문장
            - description: 이번 활동 모습을 담은 한두 문장
            - chips: 칩 2개 (강점 칩 1개 + 확장 칩 1개)

            participation — 참여 모습 3개. 각 항목:
            - title: 짧은 제목
            - description: 대화 속 근거를 담은 한두 문장

            speechAnalyses — 말하기 분석 3개 영역. area는 정확히 "어휘", "표현", "논리" 하나씩. 각 항목:
            - area: 어휘 | 표현 | 논리
            - summary: 그 영역을 한 문장으로 요약
            - keywords: 아이 발화에서 뽑은 관련 키워드 1~3개
            - feature: 이번 대화에서 나타난 특징 한 문장
            - evidenceUtterance: 근거가 된 아이 발화 원문 그대로 (없으면 null)
            - strength: 잘한 점 한 문장
            - improvement: 다음에 연습하면 좋은 점 한 문장

            sceneHighlights — 아이 발화가 있는 대화 장면별 항목. 각 항목:
            - sceneId: 장면 식별자
            - messageId: 그 장면의 마지막 아이 발화 messageId
            - featureSentence: 그 장면에서 보인 특징 한 문장
            - featureChips: 특징 칩 목록

            representative — 대표 발화 1개.
            - messageId: 대표로 고른 아이 발화 messageId
            - situation: 그 발화가 나온 이야기 상황 한 줄
            - reason: 선정 이유
            - strengths: 발견한 강점 문구
            - practiceTip: 이어서 연습하면 좋은 점
            - commentary: 한 줄 해설
            - chips: 칩 목록

            homeGuide — 가정 연계 가이드.
            - direction: headline(방향 한 문장)·description(설명)
            - storyQuestions: 이야기 이어가기 질문 3개 (각 label·question·helper)
            - dailyQuestions: 일상 연결 질문 3개 (각 label·question·helper)
            - guardianTip: 보호자를 위한 팁 한 문장
        """.trimIndent()
    }
}

data class ReportAnalysisLlmResult(
    val overall: ReportOverallLlm? = null,
    val participation: List<ParticipationLlm> = emptyList(),
    val speechAnalyses: List<ReportSpeechLlm> = emptyList(),
    val sceneHighlights: List<SceneHighlightLlm> = emptyList(),
    val representative: RepresentativeLlm? = null,
    val homeGuide: HomeGuideLlm? = null,
) {
    fun toResult(): ReportAnalysisResult = ReportAnalysisResult(
        overall = ReportOverall(
            headline = overall?.headline.orEmpty(),
            description = overall?.description.orEmpty(),
            chips = overall?.chips.orEmpty().filter(String::isNotBlank),
        ),
        participation = participation.mapNotNull { it.toItem() },
        speechAnalyses = speechAnalyses.mapNotNull { it.toAnalysis() },
        sceneHighlights = sceneHighlights.mapNotNull { it.toHighlight() },
        representative = RepresentativeSelection(
            messageId = representative?.messageId?.takeIf { it.isNotBlank() },
            situation = representative?.situation.orEmpty(),
            reason = representative?.reason.orEmpty(),
            strengths = representative?.strengths.orEmpty(),
            practiceTip = representative?.practiceTip.orEmpty(),
            commentary = representative?.commentary.orEmpty(),
            chips = representative?.chips.orEmpty().filter(String::isNotBlank),
        ),
        homeGuide = HomeGuide(
            direction = GuideDirection(
                headline = homeGuide?.direction?.headline.orEmpty(),
                description = homeGuide?.direction?.description.orEmpty(),
            ),
            storyQuestions = homeGuide?.storyQuestions.orEmpty().mapNotNull { it.toQuestion() },
            dailyQuestions = homeGuide?.dailyQuestions.orEmpty().mapNotNull { it.toQuestion() },
            guardianTip = homeGuide?.guardianTip.orEmpty(),
        ),
    )
}

data class ReportOverallLlm(
    val headline: String? = null,
    val description: String? = null,
    val chips: List<String> = emptyList(),
)

data class ParticipationLlm(
    val title: String? = null,
    val description: String? = null,
) {
    fun toItem(): ParticipationItem? {
        val resolvedTitle: String = title?.takeIf { it.isNotBlank() } ?: return null

        return ParticipationItem(title = resolvedTitle, description = description.orEmpty())
    }
}

data class ReportSpeechLlm(
    val area: String? = null,
    val summary: String? = null,
    val keywords: List<String> = emptyList(),
    val feature: String? = null,
    val evidenceUtterance: String? = null,
    val strength: String? = null,
    val improvement: String? = null,
) {
    fun toAnalysis(): ReportSpeechAnalysis? {
        val resolvedArea: String = area?.takeIf { it.isNotBlank() } ?: return null

        return ReportSpeechAnalysis(
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

data class SceneHighlightLlm(
    val sceneId: String? = null,
    val messageId: String? = null,
    val featureSentence: String? = null,
    val featureChips: List<String> = emptyList(),
) {
    fun toHighlight(): SceneHighlight? {
        val resolvedSceneId: String = sceneId?.takeIf { it.isNotBlank() } ?: return null

        return SceneHighlight(
            sceneId = resolvedSceneId,
            messageId = messageId.orEmpty(),
            featureSentence = featureSentence.orEmpty(),
            featureChips = featureChips.filter(String::isNotBlank),
        )
    }
}

data class RepresentativeLlm(
    val messageId: String? = null,
    val situation: String? = null,
    val reason: String? = null,
    val strengths: String? = null,
    val practiceTip: String? = null,
    val commentary: String? = null,
    val chips: List<String> = emptyList(),
)

data class HomeGuideLlm(
    val direction: GuideDirectionLlm? = null,
    val storyQuestions: List<GuideQuestionLlm> = emptyList(),
    val dailyQuestions: List<GuideQuestionLlm> = emptyList(),
    val guardianTip: String? = null,
)

data class GuideDirectionLlm(
    val headline: String? = null,
    val description: String? = null,
)

data class GuideQuestionLlm(
    val label: String? = null,
    val question: String? = null,
    val helper: String? = null,
) {
    fun toQuestion(): GuideQuestion? {
        val resolvedQuestion: String = question?.takeIf { it.isNotBlank() } ?: return null

        return GuideQuestion(label = label.orEmpty(), question = resolvedQuestion, helper = helper.orEmpty())
    }
}
