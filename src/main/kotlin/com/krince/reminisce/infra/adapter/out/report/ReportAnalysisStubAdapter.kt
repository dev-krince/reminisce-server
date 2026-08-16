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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(name = ["analysis.engine"], havingValue = "stub", matchIfMissing = true)
class ReportAnalysisStubAdapter : ReportAnalysisPort {

    override fun analyze(context: ReportAnalysisContext): ReportAnalysisResult = ReportAnalysisResult(
        overall = ReportOverall(
            headline = OVERALL_HEADLINE,
            description = OVERALL_DESCRIPTION,
            chips = OVERALL_CHIPS,
        ),
        participation = PARTICIPATION_ITEMS,
        speechAnalyses = speechAnalyses(context),
        sceneHighlights = sceneHighlights(context),
        representative = representative(context),
        homeGuide = HOME_GUIDE,
    )

    private fun speechAnalyses(context: ReportAnalysisContext): List<ReportSpeechAnalysis> {
        val evidence: String? = firstChildTurn(context)?.text

        return listOf(
            ReportSpeechAnalysis(
                area = AREA_VOCABULARY,
                summary = "상황에 맞는 낱말을 골라 사용했어요.",
                keywords = listOf("낱말 고르기"),
                feature = "이야기 상황에 어울리는 낱말을 사용했어요.",
                evidenceUtterance = evidence,
                strength = "아는 낱말을 자기 문장에 자연스럽게 담았어요.",
                improvement = "비슷한 뜻의 다른 낱말도 함께 써 보면 좋아요.",
            ),
            ReportSpeechAnalysis(
                area = AREA_EXPRESSION,
                summary = "인물의 마음을 헤아리는 말을 건넸어요.",
                keywords = listOf("마음 헤아리기"),
                feature = "등장인물의 기분을 살펴 말했어요.",
                evidenceUtterance = evidence,
                strength = "상대의 마음에 어울리는 말을 골라 했어요.",
                improvement = "기분을 나타내는 말을 더 다양하게 써 보면 좋아요.",
            ),
            ReportSpeechAnalysis(
                area = AREA_LOGIC,
                summary = "생각의 까닭을 붙여 이야기했어요.",
                keywords = listOf("까닭 말하기"),
                feature = "왜 그런지 이유를 붙여 말했어요.",
                evidenceUtterance = evidence,
                strength = "문제 상황에서 해결 방법을 떠올렸어요.",
                improvement = "이유를 한 가지 더 붙여 말해 보면 좋아요.",
            ),
        )
    }

    private fun sceneHighlights(context: ReportAnalysisContext): List<SceneHighlight> {
        val childTurns: List<ReportTurnContext> = childTurns(context)
        val orderedSceneIds: List<String> = childTurns.sortedBy { it.turnOrder }.map { it.sceneId }.distinct()

        return orderedSceneIds.map { sceneId ->
            val lastChildTurn: ReportTurnContext = childTurns.filter { it.sceneId == sceneId }.maxBy { it.turnOrder }

            SceneHighlight(
                sceneId = sceneId,
                messageId = requireNotNull(lastChildTurn.messageId),
                featureSentence = HIGHLIGHT_SENTENCE,
                featureChips = HIGHLIGHT_CHIPS,
            )
        }
    }

    private fun representative(context: ReportAnalysisContext): RepresentativeSelection = RepresentativeSelection(
        messageId = representativeMessageId(context),
        situation = "이야기 속 인물과 마음을 나누던 장면이에요.",
        reason = "이야기 흐름에 맞게 자기 생각이 자연스럽게 이어진 발화예요.",
        strengths = "자기 생각을 문장으로 완성해 말했어요.",
        practiceTip = "같은 생각을 다른 낱말로도 말해 보면 좋아요.",
        commentary = "이야기에 마음을 담아 참여한 발화예요.",
        chips = REPRESENTATIVE_CHIPS,
    )

    private fun representativeMessageId(context: ReportAnalysisContext): String? {
        val turnOrderByMessageId: Map<String, Long> = childTurns(context)
            .associate { requireNotNull(it.messageId) to it.turnOrder }

        return context.analyses
            .filter { it.messageId in turnOrderByMessageId }
            .sortedWith(richestFirst(turnOrderByMessageId))
            .firstOrNull()
            ?.messageId
    }

    private fun richestFirst(turnOrderByMessageId: Map<String, Long>): Comparator<ReportUtteranceContext> =
        compareByDescending<ReportUtteranceContext> { it.detectedElements.size }
            .thenBy { turnOrderByMessageId.getValue(it.messageId) }

    private fun childTurns(context: ReportAnalysisContext): List<ReportTurnContext> =
        context.turns.filter { it.isChild && it.messageId != null }

    private fun firstChildTurn(context: ReportAnalysisContext): ReportTurnContext? =
        childTurns(context).minByOrNull { it.turnOrder }

    companion object {
        private const val AREA_VOCABULARY: String = "어휘"
        private const val AREA_EXPRESSION: String = "표현"
        private const val AREA_LOGIC: String = "논리"

        private const val OVERALL_HEADLINE: String = "이야기 속 인물의 마음을 헤아리며 끝까지 참여했어요."
        private const val OVERALL_DESCRIPTION: String = "이번 활동에서 자기 생각을 말로 표현하며 이야기를 따라갔어요."
        private val OVERALL_CHIPS: List<String> = listOf("마음 헤아리기", "생각 넓히기")

        private val PARTICIPATION_ITEMS: List<ParticipationItem> = listOf(
            ParticipationItem(title = "이야기에 귀 기울였어요", description = "장면마다 이야기 흐름을 따라가며 들었어요."),
            ParticipationItem(title = "생각을 말로 표현했어요", description = "질문에 자기 생각을 소리 내어 말했어요."),
            ParticipationItem(title = "끝까지 함께했어요", description = "이야기 활동을 끝까지 마무리했어요."),
        )

        private const val HIGHLIGHT_SENTENCE: String = "이 장면에서 인물의 마음에 맞는 말을 골라 건넸어요."
        private val HIGHLIGHT_CHIPS: List<String> = listOf("마음 나누기", "장면 이해")

        private val REPRESENTATIVE_CHIPS: List<String> = listOf("생각 표현")

        private val HOME_GUIDE: HomeGuide = HomeGuide(
            direction = GuideDirection(
                headline = "이야기 속 마음을 일상 대화로 이어 가요.",
                description = "아이가 이야기에서 느낀 마음을 집에서도 편하게 말해 보게 도와주세요.",
            ),
            storyQuestions = listOf(
                GuideQuestion(
                    label = "마음 묻기",
                    question = "이야기 속 인물은 그때 어떤 마음이었을까?",
                    helper = "인물의 기분을 함께 상상해 보게 해 주세요.",
                ),
                GuideQuestion(
                    label = "이유 묻기",
                    question = "인물은 왜 그렇게 했을까?",
                    helper = "까닭을 말해 보도록 기다려 주세요.",
                ),
                GuideQuestion(
                    label = "상상 묻기",
                    question = "너라면 그 다음에 어떻게 했을 것 같아?",
                    helper = "정답 없이 아이의 생각을 그대로 들어 주세요.",
                ),
            ),
            dailyQuestions = listOf(
                GuideQuestion(
                    label = "기분 나누기",
                    question = "오늘 가장 기분 좋았던 순간은 언제였어?",
                    helper = "기분에 이름을 붙여 말해 보게 해 주세요.",
                ),
                GuideQuestion(
                    label = "마음 헤아리기",
                    question = "오늘 친구는 어떤 기분이었을 것 같아?",
                    helper = "다른 사람의 마음을 함께 짐작해 보세요.",
                ),
                GuideQuestion(
                    label = "생각 잇기",
                    question = "내일 해 보고 싶은 일은 뭐야?",
                    helper = "이유까지 이어 말하면 칭찬해 주세요.",
                ),
            ),
            guardianTip = "아이의 말을 끝까지 들은 뒤 한 문장으로 되돌려 말해 주면 표현이 더 자라요.",
        )
    }
}
