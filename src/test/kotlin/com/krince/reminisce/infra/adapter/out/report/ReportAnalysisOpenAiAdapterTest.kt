package com.krince.reminisce.infra.adapter.out.report

import com.krince.reminisce.application.port.out.report.ReportAnalysisContext
import com.krince.reminisce.application.port.out.report.ReportSceneContext
import com.krince.reminisce.application.port.out.report.ReportTurnContext
import com.krince.reminisce.application.port.out.report.ReportUtteranceContext
import com.krince.reminisce.domain.model.story.vo.ThinkingElement
import com.krince.reminisce.domain.model.utteranceanalysis.DetectedElement
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.ai.chat.client.ChatClient

@Tags("test", "unitTest")
@DisplayName("ReportAnalysisOpenAiAdapter 단위테스트")
class ReportAnalysisOpenAiAdapterTest : FunSpec({

    val requestSpec = mockk<ChatClient.ChatClientRequestSpec>(relaxed = true)
    val callResponseSpec = mockk<ChatClient.CallResponseSpec>()
    val chatClient = mockk<ChatClient>()
    val chatClientBuilder = mockk<ChatClient.Builder>()

    every { chatClientBuilder.build() } returns chatClient
    every { chatClient.prompt() } returns requestSpec
    every { requestSpec.system(any<String>()) } returns requestSpec
    every { requestSpec.user(any<String>()) } returns requestSpec
    every { requestSpec.options(any()) } returns requestSpec
    every { requestSpec.call() } returns callResponseSpec

    val adapter = ReportAnalysisOpenAiAdapter(chatClientBuilder, model = "gpt-4o-mini", temperature = 0.2)

    fun context(): ReportAnalysisContext = ReportAnalysisContext(
        childName = "토토",
        storyTitle = "방귀쟁이 며느리",
        scenes = listOf(ReportSceneContext(sceneId = "scene-1", description = "며느리를 만나는 장면", goal = "마음 이해하기")),
        turns = listOf(
            ReportTurnContext(sceneId = "scene-1", turnOrder = 1, isChild = true, text = "며느리가 힘들었을 것 같아요", messageId = "msg-1"),
            ReportTurnContext(sceneId = "scene-1", turnOrder = 2, isChild = false, text = "고마워", messageId = null),
        ),
        analyses = listOf(
            ReportUtteranceContext(
                messageId = "msg-1",
                detectedElements = listOf(DetectedElement(type = ThinkingElement.EMPATHY, evidence = "며느리가 힘들었을 것 같아요")),
            ),
        ),
    )

    fun stubEntity(result: ReportAnalysisLlmResult?) {
        every { callResponseSpec.entity(ReportAnalysisLlmResult::class.java) } returns result
    }

    context("analyze") {
        test("LLM 응답 전체를 도메인 결과로 매핑한다") {
            stubEntity(
                ReportAnalysisLlmResult(
                    overall = ReportOverallLlm(
                        headline = "마음을 헤아리며 참여했어요.",
                        description = "이야기 흐름을 잘 따라갔어요.",
                        chips = listOf("마음 헤아리기", "생각 넓히기"),
                    ),
                    participation = listOf(
                        ParticipationLlm(title = "귀 기울이기", description = "이야기를 잘 들었어요."),
                    ),
                    speechAnalyses = listOf(
                        ReportSpeechLlm(
                            area = "어휘",
                            summary = "낱말을 잘 골랐어요.",
                            keywords = listOf("힘들다"),
                            feature = "상황에 맞는 낱말을 썼어요.",
                            evidenceUtterance = "며느리가 힘들었을 것 같아요",
                            strength = "자기 문장에 낱말을 담았어요.",
                            improvement = "다른 낱말도 써 보면 좋아요.",
                        ),
                    ),
                    sceneHighlights = listOf(
                        SceneHighlightLlm(
                            sceneId = "scene-1",
                            messageId = "msg-1",
                            featureSentence = "마음에 맞는 말을 건넸어요.",
                            featureChips = listOf("마음 나누기"),
                        ),
                    ),
                    representative = RepresentativeLlm(
                        messageId = "msg-1",
                        situation = "며느리를 만난 장면이에요.",
                        reason = "마음을 담아 말한 발화예요.",
                        strengths = "공감하는 말을 했어요.",
                        practiceTip = "이유도 붙여 말해 보면 좋아요.",
                        commentary = "따뜻한 발화예요.",
                        chips = listOf("공감"),
                    ),
                    homeGuide = HomeGuideLlm(
                        direction = GuideDirectionLlm(headline = "마음 대화를 이어 가요.", description = "집에서도 이야기해 보세요."),
                        storyQuestions = listOf(GuideQuestionLlm(label = "마음", question = "며느리는 어떤 마음이었을까?", helper = "기다려 주세요.")),
                        dailyQuestions = listOf(GuideQuestionLlm(label = "기분", question = "오늘 기분은 어땠어?", helper = "이름 붙여 보게 해 주세요.")),
                        guardianTip = "끝까지 들어 주세요.",
                    ),
                ),
            )

            val result = adapter.analyze(context())

            result.overall.headline shouldBe "마음을 헤아리며 참여했어요."
            result.overall.chips shouldBe listOf("마음 헤아리기", "생각 넓히기")
            result.participation.map { it.title } shouldBe listOf("귀 기울이기")
            result.speechAnalyses.map { it.area } shouldBe listOf("어휘")
            result.speechAnalyses.first().evidenceUtterance shouldBe "며느리가 힘들었을 것 같아요"
            result.sceneHighlights.map { it.sceneId to it.messageId } shouldBe listOf("scene-1" to "msg-1")
            result.representative.messageId shouldBe "msg-1"
            result.representative.reason shouldBe "마음을 담아 말한 발화예요."
            result.homeGuide.direction.headline shouldBe "마음 대화를 이어 가요."
            result.homeGuide.storyQuestions.map { it.question } shouldBe listOf("며느리는 어떤 마음이었을까?")
            result.homeGuide.guardianTip shouldBe "끝까지 들어 주세요."
        }

        test("null·blank 필드는 안전하게 걸러 매핑한다") {
            stubEntity(
                ReportAnalysisLlmResult(
                    overall = ReportOverallLlm(headline = null, description = null, chips = listOf(" ", "칩")),
                    participation = listOf(
                        ParticipationLlm(title = " ", description = "버려진다"),
                        ParticipationLlm(title = "남는다", description = null),
                    ),
                    speechAnalyses = listOf(
                        ReportSpeechLlm(area = " "),
                        ReportSpeechLlm(area = "표현", evidenceUtterance = " "),
                    ),
                    sceneHighlights = listOf(
                        SceneHighlightLlm(sceneId = null, messageId = "msg-1"),
                        SceneHighlightLlm(sceneId = "scene-1", messageId = null),
                    ),
                    representative = RepresentativeLlm(messageId = " "),
                    homeGuide = HomeGuideLlm(
                        direction = null,
                        storyQuestions = listOf(GuideQuestionLlm(label = "마음", question = " ", helper = "버려진다")),
                        dailyQuestions = emptyList(),
                        guardianTip = null,
                    ),
                ),
            )

            val result = adapter.analyze(context())

            result.overall.headline shouldBe ""
            result.overall.chips shouldBe listOf("칩")
            result.participation.map { it.title } shouldBe listOf("남는다")
            result.participation.first().description shouldBe ""
            result.speechAnalyses.map { it.area } shouldBe listOf("표현")
            result.speechAnalyses.first().evidenceUtterance shouldBe null
            result.sceneHighlights.map { it.sceneId } shouldBe listOf("scene-1")
            result.representative.messageId shouldBe null
            result.homeGuide.direction.headline shouldBe ""
            result.homeGuide.storyQuestions shouldBe emptyList()
            result.homeGuide.guardianTip shouldBe ""
        }

        test("LLM 응답이 null이면 IllegalStateException을 던진다") {
            stubEntity(null)

            shouldThrow<IllegalStateException> { adapter.analyze(context()) }
        }

        test("LLM 호출이 예외를 던지면 IllegalStateException으로 감싼다") {
            every { callResponseSpec.entity(ReportAnalysisLlmResult::class.java) } throws RuntimeException("boom")

            shouldThrow<IllegalStateException> { adapter.analyze(context()) }
        }
    }
})
