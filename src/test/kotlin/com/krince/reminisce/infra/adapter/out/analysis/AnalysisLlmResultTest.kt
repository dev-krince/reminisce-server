package com.krince.reminisce.infra.adapter.out.analysis

import com.krince.reminisce.domain.model.story.vo.ThinkingElement
import com.krince.reminisce.domain.model.utteranceanalysis.vo.ChildIntent
import com.krince.reminisce.domain.model.utteranceanalysis.vo.UtteranceValidity
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe

class AnalysisLlmResultTest : FunSpec({

    test("정상 LLM 결과를 도메인으로 매핑한다") {
        val result = AnalysisLlmResult(
            childIntent = "EMOTION",
            mainPoint = "  무서웠어  ",
            detectedElements = listOf(
                DetectedElementLlm(type = "EMOTION", evidence = "무서웠어"),
                DetectedElementLlm(type = "REASON", evidence = "밤이라서"),
            ),
            validity = "VALID",
        )

        val domain = result.toRawUtteranceAnalysis()

        domain.childIntent shouldBe ChildIntent.EMOTION
        domain.mainPoint shouldBe "무서웠어"
        domain.validity shouldBe UtteranceValidity.VALID
        domain.detectedElements shouldHaveSize 2
        domain.detectedElements[0].type shouldBe ThinkingElement.EMOTION
        domain.detectedElements[1].type shouldBe ThinkingElement.REASON
    }

    test("소문자·공백 섞인 enum도 관대하게 파싱한다") {
        val result = AnalysisLlmResult(childIntent = " emotion ", validity = "valid")

        val domain = result.toRawUtteranceAnalysis()

        domain.childIntent shouldBe ChildIntent.EMOTION
        domain.validity shouldBe UtteranceValidity.VALID
    }

    test("공백을 언더스코어로 보정해 파싱한다") {
        AnalysisLlmResult(childIntent = "short response").toRawUtteranceAnalysis()
            .childIntent shouldBe ChildIntent.SHORT_RESPONSE
    }

    test("알 수 없는 값은 UNCLEAR로 폴백한다") {
        val result = AnalysisLlmResult(childIntent = "NONSENSE", validity = "???")

        val domain = result.toRawUtteranceAnalysis()

        domain.childIntent shouldBe ChildIntent.UNCLEAR
        domain.validity shouldBe UtteranceValidity.UNCLEAR
    }

    test("알 수 없는 요소 타입·빈 evidence 요소는 버린다") {
        val result = AnalysisLlmResult(
            detectedElements = listOf(
                DetectedElementLlm(type = "UNKNOWN", evidence = "x"),
                DetectedElementLlm(type = "EMOTION", evidence = "   "),
                DetectedElementLlm(type = "REASON", evidence = "때문에"),
            ),
        )

        val domain = result.toRawUtteranceAnalysis()

        domain.detectedElements shouldHaveSize 1
        domain.detectedElements[0].type shouldBe ThinkingElement.REASON
        domain.detectedElements[0].evidence shouldBe "때문에"
    }

    test("빈 mainPoint는 null로 정리한다") {
        AnalysisLlmResult(mainPoint = "   ").toRawUtteranceAnalysis().mainPoint shouldBe null
    }

    test("null 필드는 안전한 기본값으로 채운다") {
        val domain = AnalysisLlmResult().toRawUtteranceAnalysis()

        domain.childIntent shouldBe ChildIntent.UNCLEAR
        domain.validity shouldBe UtteranceValidity.UNCLEAR
        domain.mainPoint shouldBe null
        domain.detectedElements shouldHaveSize 0
    }
})
