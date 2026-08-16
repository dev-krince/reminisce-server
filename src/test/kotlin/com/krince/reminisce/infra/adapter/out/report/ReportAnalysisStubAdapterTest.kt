package com.krince.reminisce.infra.adapter.out.report

import com.krince.reminisce.application.port.out.report.ReportAnalysisContext
import com.krince.reminisce.application.port.out.report.ReportSceneContext
import com.krince.reminisce.application.port.out.report.ReportTurnContext
import com.krince.reminisce.application.port.out.report.ReportUtteranceContext
import com.krince.reminisce.domain.model.story.vo.ThinkingElement
import com.krince.reminisce.domain.model.utteranceanalysis.DetectedElement
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeBlank

@Tags("test", "unitTest")
@DisplayName("ReportAnalysisStubAdapter 단위테스트")
class ReportAnalysisStubAdapterTest : FunSpec({

    val adapter = ReportAnalysisStubAdapter()

    fun childTurn(sceneId: String, turnOrder: Long, messageId: String, text: String): ReportTurnContext =
        ReportTurnContext(sceneId = sceneId, turnOrder = turnOrder, isChild = true, text = text, messageId = messageId)

    fun characterTurn(sceneId: String, turnOrder: Long): ReportTurnContext =
        ReportTurnContext(
            sceneId = sceneId,
            turnOrder = turnOrder,
            isChild = false,
            text = "캐릭터 응답 $turnOrder",
            messageId = null,
        )

    fun analysis(messageId: String, vararg types: ThinkingElement): ReportUtteranceContext =
        ReportUtteranceContext(
            messageId = messageId,
            detectedElements = types.map { DetectedElement(type = it, evidence = "근거-${it.name}") },
        )

    fun context(): ReportAnalysisContext = ReportAnalysisContext(
        childName = "토토",
        storyTitle = "방귀쟁이 며느리",
        scenes = listOf(
            ReportSceneContext(sceneId = "scene-1", description = "며느리를 만나는 장면", goal = "마음 이해하기"),
            ReportSceneContext(sceneId = "scene-2", description = "며느리를 돕는 장면", goal = null),
        ),
        turns = listOf(
            childTurn("scene-1", 1, "msg-1", "며느리가 힘들었을 것 같아요"),
            characterTurn("scene-1", 2),
            childTurn("scene-1", 3, "msg-2", "며느리 입장에서 생각하면 마음이 아파요"),
            childTurn("scene-2", 4, "msg-3", "제가 도와줄래요"),
        ),
        analyses = listOf(
            analysis("msg-1", ThinkingElement.EMOTION),
            analysis("msg-2", ThinkingElement.PERSPECTIVE, ThinkingElement.EMPATHY, ThinkingElement.DECISION),
            analysis("msg-3", ThinkingElement.SOLUTION),
        ),
    )

    test("같은 컨텍스트로 두 번 호출하면 동일한 출력을 만든다") {
        val first = adapter.analyze(context())
        val second = adapter.analyze(context())

        second.overall shouldBe first.overall
        second.participation shouldBe first.participation
        second.speechAnalyses shouldBe first.speechAnalyses
        second.sceneHighlights shouldBe first.sceneHighlights
        second.representative shouldBe first.representative
        second.homeGuide shouldBe first.homeGuide
    }

    test("전 섹션을 채운다 - participation 3개, speech 3영역, overall 칩 2개, homeGuide 질문 3+3과 팁") {
        val result = adapter.analyze(context())

        result.overall.headline.shouldNotBeBlank()
        result.overall.description.shouldNotBeBlank()
        result.overall.chips.size shouldBe 2
        result.participation.size shouldBe 3
        result.participation.forEach {
            it.title.shouldNotBeBlank()
            it.description.shouldNotBeBlank()
        }
        result.speechAnalyses.map { it.area } shouldContainExactly listOf("어휘", "표현", "논리")
        result.speechAnalyses.forEach {
            it.summary.shouldNotBeBlank()
            it.feature.shouldNotBeBlank()
            it.strength.shouldNotBeBlank()
            it.improvement.shouldNotBeBlank()
        }
        result.homeGuide.direction.headline.shouldNotBeBlank()
        result.homeGuide.storyQuestions.size shouldBe 3
        result.homeGuide.dailyQuestions.size shouldBe 3
        result.homeGuide.guardianTip.shouldNotBeBlank()
        result.representative.reason.shouldNotBeBlank()
    }

    test("장면 하이라이트 messageId는 각 장면의 turnOrder 최대 아이 발화다") {
        val result = adapter.analyze(context())

        result.sceneHighlights.map { it.sceneId to it.messageId } shouldContainExactly
            listOf("scene-1" to "msg-2", "scene-2" to "msg-3")
    }

    test("대표 발화 messageId는 detectedElements가 가장 많은 컨텍스트 실재 아이 발화다") {
        val result = adapter.analyze(context())

        result.representative.messageId shouldBe "msg-2"
    }

    test("지정한 messageId는 모두 컨텍스트에 실재하는 아이 발화다") {
        val result = adapter.analyze(context())
        val childMessageIds = setOf("msg-1", "msg-2", "msg-3")

        result.sceneHighlights.forEach { (it.messageId in childMessageIds) shouldBe true }
        (result.representative.messageId in childMessageIds) shouldBe true
    }

    test("아이 발화가 없으면 하이라이트는 비고 대표 messageId는 null이며 예외가 없다") {
        val emptyContext = ReportAnalysisContext(
            childName = null,
            storyTitle = "방귀쟁이 며느리",
            scenes = emptyList(),
            turns = emptyList(),
            analyses = emptyList(),
        )

        val result = adapter.analyze(emptyContext)

        result.sceneHighlights shouldBe emptyList()
        result.representative.messageId shouldBe null
        result.participation.size shouldBe 3
    }
})
