package com.krince.reminisce.domain.model.utteranceanalysis

import com.krince.reminisce.domain.model.message.vo.MessageId
import com.krince.reminisce.domain.model.story.vo.ThinkingElement
import com.krince.reminisce.domain.model.utteranceanalysis.vo.ChildIntent
import com.krince.reminisce.domain.model.utteranceanalysis.vo.UtteranceValidity
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe

@Tags("test", "unitTest")
@DisplayName("RawUtteranceAnalysis.verifyAgainst 단위테스트")
class RawUtteranceAnalysisTest : FunSpec({

    val messageId = MessageId("message-uuid-1")
    val text = "며느리가 참 힘들었겠어요"

    fun raw(detectedElements: List<DetectedElement>): RawUtteranceAnalysis = RawUtteranceAnalysis(
        childIntent = ChildIntent.PERSPECTIVE,
        mainPoint = "며느리가 힘들었을 것이다",
        detectedElements = detectedElements,
        validity = UtteranceValidity.VALID,
    )

    test("evidence가 원문의 부분문자열인 요소는 남는다") {
        val analysis = raw(listOf(DetectedElement(ThinkingElement.EMOTION, "힘들"))).verifyAgainst(text, messageId)

        analysis.detectedElements.map { it.type } shouldContainExactly listOf(ThinkingElement.EMOTION)
        analysis.messageId shouldBe messageId
        analysis.childIntent shouldBe ChildIntent.PERSPECTIVE
        analysis.validity shouldBe UtteranceValidity.VALID
    }

    test("evidence가 원문에 없는 요소는 폐기된다") {
        val analysis = raw(listOf(DetectedElement(ThinkingElement.PERSPECTIVE, "임금님 입장"))).verifyAgainst(text, messageId)

        analysis.detectedElements shouldBe emptyList()
    }

    test("근거 있는 요소만 남기고 근거 없는 요소는 폐기한다") {
        val detectedElements = listOf(
            DetectedElement(ThinkingElement.EMOTION, "힘들"),
            DetectedElement(ThinkingElement.PERSPECTIVE, "존재하지 않는 근거"),
        )

        val analysis = raw(detectedElements).verifyAgainst(text, messageId)

        analysis.detectedElements.map { it.type } shouldContainExactly listOf(ThinkingElement.EMOTION)
    }

    test("공백 정규화 후 부분문자열이면 남는다") {
        val spacedText = "  며느리가   참   힘들었겠어요  "

        val analysis = raw(listOf(DetectedElement(ThinkingElement.EMOTION, " 참 힘들었겠어요 ")))
            .verifyAgainst(spacedText, messageId)

        analysis.detectedElements.map { it.type } shouldContainExactly listOf(ThinkingElement.EMOTION)
    }

    test("evidence가 빈 문자열이거나 공백뿐이면 폐기된다") {
        val detectedElements = listOf(
            DetectedElement(ThinkingElement.EMOTION, ""),
            DetectedElement(ThinkingElement.PERSPECTIVE, "   "),
        )

        val analysis = raw(detectedElements).verifyAgainst(text, messageId)

        analysis.detectedElements shouldBe emptyList()
    }
})
