package com.krince.reminisce.domain.model.report

import com.krince.reminisce.domain.model.story.vo.ThinkingElement
import com.krince.reminisce.domain.model.utteranceanalysis.DetectedElement
import com.krince.reminisce.domain.model.utteranceanalysis.UtteranceAnalysis

object GuardianReportComposer {
    fun compose(analyses: List<UtteranceAnalysis>): GuardianReportAreas {
        val detectedElements: List<DetectedElement> = analyses.flatMap { it.detectedElements }
        val detectedTypes: Set<ThinkingElement> = detectedElements.map { it.type }.toSet()
        val competencyAnalysis: CompetencyAnalysis = composeCompetencyAnalysis(detectedElements, detectedTypes)
        val representativeUtterance: RepresentativeUtterance = composeRepresentativeUtterance(analyses)
        val homeConversationGuide: HomeConversationGuide = composeHomeConversationGuide(detectedTypes)

        return GuardianReportAreas(
            competencyAnalysis = competencyAnalysis,
            representativeUtterance = representativeUtterance,
            homeConversationGuide = homeConversationGuide,
        )
    }

    private fun composeCompetencyAnalysis(
        detectedElements: List<DetectedElement>,
        detectedTypes: Set<ThinkingElement>,
    ): CompetencyAnalysis = CompetencyAnalysis(
        vocabulary = composeVocabularyItem(detectedElements),
        perspectiveEmpathy = composeCategoryItem(PERSPECTIVE_EMPATHY, detectedElements, detectedTypes),
        emotion = composeCategoryItem(EMOTION_CATEGORY, detectedElements, detectedTypes),
        interaction = composeCategoryItem(INTERACTION, detectedElements, detectedTypes),
        thoughtReason = composeCategoryItem(THOUGHT_REASON, detectedElements, detectedTypes),
        resultSolution = composeCategoryItem(RESULT_SOLUTION, detectedElements, detectedTypes),
    )

    private fun composeCategoryItem(
        category: Category,
        detectedElements: List<DetectedElement>,
        detectedTypes: Set<ThinkingElement>,
    ): CompetencyItem {
        val hasElement: Boolean = category.elements.any { it in detectedTypes }
        val evidence: String? = firstEvidenceOf(category.elements, detectedElements)

        return CompetencyItem(
            label = category.label,
            feature = if (hasElement) category.strongFeature else category.weakFeature,
            evidenceUtterance = evidence,
            strength = if (hasElement) category.strengthWhenPresent else category.strengthWhenAbsent,
            improvement = if (hasElement) category.improvementWhenPresent else category.improvementWhenAbsent,
        )
    }

    private fun firstEvidenceOf(
        elements: List<ThinkingElement>,
        detectedElements: List<DetectedElement>,
    ): String? = detectedElements.firstOrNull { it.type in elements }?.evidence

    private fun composeVocabularyItem(detectedElements: List<DetectedElement>): CompetencyItem {
        val words: List<String> = extractWords(detectedElements)
        val hasWords: Boolean = words.isNotEmpty()

        return CompetencyItem(
            label = VOCABULARY_LABEL,
            feature = if (hasWords) VOCABULARY_STRONG_FEATURE else VOCABULARY_WEAK_FEATURE,
            evidenceUtterance = detectedElements.firstOrNull()?.evidence,
            strength = if (hasWords) vocabularyStrength(words) else VOCABULARY_STRENGTH_WHEN_ABSENT,
            improvement = VOCABULARY_IMPROVEMENT,
        )
    }

    private fun vocabularyStrength(words: List<String>): String =
        "$VOCABULARY_STRENGTH_PREFIX${words.joinToString(WORD_SEPARATOR)}$VOCABULARY_STRENGTH_SUFFIX"

    private fun extractWords(detectedElements: List<DetectedElement>): List<String> =
        detectedElements
            .map { it.evidence }
            .flatMap { it.split(*WORD_DELIMITERS) }
            .map { it.trim() }
            .filter { it.length >= MIN_WORD_LENGTH }
            .distinct()
            .take(MAX_VOCABULARY_WORDS)

    private fun composeRepresentativeUtterance(analyses: List<UtteranceAnalysis>): RepresentativeUtterance {
        val richest: UtteranceAnalysis? = analyses
            .filter { it.detectedElements.isNotEmpty() }
            .maxWithOrNull(richnessComparator)
        val text: String? = richest?.detectedElements?.firstOrNull()?.evidence

        return RepresentativeUtterance(
            text = text,
            reason = if (text == null) REPRESENTATIVE_REASON_WHEN_ABSENT else REPRESENTATIVE_REASON_WHEN_PRESENT,
        )
    }

    private val richnessComparator: Comparator<UtteranceAnalysis> = compareBy<UtteranceAnalysis> {
        it.detectedElements.size
    }.thenByDescending { it.messageId.value }

    private fun composeHomeConversationGuide(detectedTypes: Set<ThinkingElement>): HomeConversationGuide {
        val weakCategories: List<Category> = ALL_CATEGORIES.filterNot { category ->
            category.elements.any { it in detectedTypes }
        }
        val targetCategories: List<Category> = weakCategories.ifEmpty { ALL_CATEGORIES }

        return HomeConversationGuide(
            storyThemeQuestions = targetCategories.map { it.storyThemeQuestion },
            dailyLifeQuestions = targetCategories.map { it.dailyLifeQuestion },
        )
    }

    private data class Category(
        val label: String,
        val elements: List<ThinkingElement>,
        val strongFeature: String,
        val weakFeature: String,
        val strengthWhenPresent: String,
        val strengthWhenAbsent: String,
        val improvementWhenPresent: String,
        val improvementWhenAbsent: String,
        val storyThemeQuestion: String,
        val dailyLifeQuestion: String,
    )

    private const val VOCABULARY_LABEL: String = "어휘"
    private const val VOCABULARY_STRONG_FEATURE: String = "이번 활동에서 다양한 낱말을 활용해 자기 생각을 표현했어요."
    private const val VOCABULARY_WEAK_FEATURE: String = "이번 활동에서는 짧은 표현으로 참여했어요."
    private const val VOCABULARY_STRENGTH_PREFIX: String = "\""
    private const val VOCABULARY_STRENGTH_SUFFIX: String = "\" 같은 낱말을 자연스럽게 사용했어요."
    private const val VOCABULARY_STRENGTH_WHEN_ABSENT: String = "짧게라도 자기 생각을 소리 내어 표현해 본 점이 좋아요."
    private const val VOCABULARY_IMPROVEMENT: String = "다양한 낱말을 함께 쓰다 보면 표현이 더 풍부해질 거예요."
    private const val WORD_SEPARATOR: String = ", "
    private const val MIN_WORD_LENGTH: Int = 2
    private const val MAX_VOCABULARY_WORDS: Int = 5

    private const val REPRESENTATIVE_REASON_WHEN_PRESENT: String =
        "이야기 맥락에 맞게 생각이 자연스럽게 이어진 발화라 대표로 골랐어요."
    private const val REPRESENTATIVE_REASON_WHEN_ABSENT: String =
        "이번 활동에서는 대표로 소개할 발화를 아직 고르지 못했어요."

    private val WORD_DELIMITERS: CharArray = charArrayOf(' ', ',', '.', '!', '?', '\n')

    private val PERSPECTIVE_EMPATHY: Category = Category(
        label = "관점·공감",
        elements = listOf(ThinkingElement.PERSPECTIVE, ThinkingElement.EMPATHY),
        strongFeature = "상대의 입장을 헤아리며 마음을 함께 느끼는 모습을 보였어요.",
        weakFeature = "이번 활동에서는 상대의 입장을 살피는 표현이 드물었어요.",
        strengthWhenPresent = "다른 사람의 마음을 헤아려 공감하는 말을 건넸어요.",
        strengthWhenAbsent = "이야기에 관심을 갖고 끝까지 참여한 점이 좋아요.",
        improvementWhenPresent = "공감한 이유를 한마디 덧붙이면 더 깊은 대화가 돼요.",
        improvementWhenAbsent = "상대가 어떤 마음일지 함께 상상해 보면 좋겠어요.",
        storyThemeQuestion = "며느리는 그때 어떤 마음이었을까? 함께 이야기해 볼까요?",
        dailyLifeQuestion = "오늘 친구가 속상해했다면 어떤 마음이었을지 같이 생각해 볼까요?",
    )

    private val EMOTION_CATEGORY: Category = Category(
        label = "감정",
        elements = listOf(ThinkingElement.EMOTION, ThinkingElement.REASON),
        strongFeature = "자신이 느낀 감정을 이유와 함께 표현했어요.",
        weakFeature = "이번 활동에서는 감정을 드러내는 표현이 드물었어요.",
        strengthWhenPresent = "느낀 감정을 솔직하게 말로 표현했어요.",
        strengthWhenAbsent = "이야기를 차분히 따라가며 들은 점이 좋아요.",
        improvementWhenPresent = "감정을 느낀 까닭까지 말하면 표현이 더 또렷해져요.",
        improvementWhenAbsent = "지금 기분이 어떤지 이름 붙여 말해 보면 좋겠어요.",
        storyThemeQuestion = "이 장면에서 너라면 어떤 기분이 들었을까요?",
        dailyLifeQuestion = "오늘 하루 중 가장 기분 좋았던 순간은 언제였나요?",
    )

    private val INTERACTION: Category = Category(
        label = "상호작용",
        elements = listOf(ThinkingElement.REQUEST),
        strongFeature = "필요한 것을 상대에게 자연스럽게 요청했어요.",
        weakFeature = "이번 활동에서는 상대에게 무언가를 부탁하는 표현이 드물었어요.",
        strengthWhenPresent = "원하는 것을 예의 있게 부탁하며 대화를 이어갔어요.",
        strengthWhenAbsent = "상대의 말을 귀 기울여 들은 점이 좋아요.",
        improvementWhenPresent = "부탁과 함께 이유를 말하면 상대가 더 잘 이해해요.",
        improvementWhenAbsent = "필요한 것을 말로 부탁해 보는 연습을 하면 좋겠어요.",
        storyThemeQuestion = "며느리에게 무엇을 부탁하고 싶었는지 이야기해 볼까요?",
        dailyLifeQuestion = "도움이 필요할 때 어떻게 부탁하면 좋을지 같이 연습해 볼까요?",
    )

    private val THOUGHT_REASON: Category = Category(
        label = "생각·이유",
        elements = listOf(ThinkingElement.DECISION, ThinkingElement.REASON),
        strongFeature = "자신의 생각을 정하고 그 까닭을 함께 말했어요.",
        weakFeature = "이번 활동에서는 생각의 까닭을 설명하는 표현이 드물었어요.",
        strengthWhenPresent = "자기 생각을 이유와 함께 또렷하게 밝혔어요.",
        strengthWhenAbsent = "이야기 속 상황을 잘 이해하며 참여한 점이 좋아요.",
        improvementWhenPresent = "다른 이유도 떠올려 보면 생각이 더 넓어져요.",
        improvementWhenAbsent = "\"왜냐하면\"을 붙여 까닭을 말해 보면 좋겠어요.",
        storyThemeQuestion = "며느리가 왜 그렇게 행동했다고 생각하나요?",
        dailyLifeQuestion = "오늘 네가 내린 결정 중 하나와 그 이유를 말해 볼까요?",
    )

    private val RESULT_SOLUTION: Category = Category(
        label = "결과·해결",
        elements = listOf(ThinkingElement.RESULT, ThinkingElement.SOLUTION),
        strongFeature = "문제 상황의 결과를 예상하고 해결 방법을 떠올렸어요.",
        weakFeature = "이번 활동에서는 해결 방법을 떠올리는 표현이 드물었어요.",
        strengthWhenPresent = "어떻게 하면 좋을지 나름의 해결책을 제안했어요.",
        strengthWhenAbsent = "이야기의 흐름을 끝까지 따라간 점이 좋아요.",
        improvementWhenPresent = "그 방법의 결과가 어떨지도 함께 상상해 보면 좋아요.",
        improvementWhenAbsent = "\"이렇게 하면 어떨까\" 하고 해결 방법을 떠올려 보면 좋겠어요.",
        storyThemeQuestion = "며느리의 고민을 어떻게 도와주면 좋을지 이야기해 볼까요?",
        dailyLifeQuestion = "요즘 풀고 싶은 고민이 있다면 어떤 방법이 있을지 같이 찾아볼까요?",
    )

    private val ALL_CATEGORIES: List<Category> = listOf(
        PERSPECTIVE_EMPATHY,
        EMOTION_CATEGORY,
        INTERACTION,
        THOUGHT_REASON,
        RESULT_SOLUTION,
    )
}
