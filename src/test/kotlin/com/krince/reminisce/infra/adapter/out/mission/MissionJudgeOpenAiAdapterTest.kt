package com.krince.reminisce.infra.adapter.out.mission

import com.krince.reminisce.application.port.out.mission.MissionJudgeContext
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeBlank
import io.mockk.every
import io.mockk.mockk
import org.springframework.ai.chat.client.ChatClient

@Tags("test", "unitTest")
@DisplayName("MissionJudgeOpenAiAdapter 단위테스트")
class MissionJudgeOpenAiAdapterTest : FunSpec({

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

    val adapter = MissionJudgeOpenAiAdapter(chatClientBuilder, model = "gpt-4o-mini", temperature = 0.2)

    fun context(text: String): MissionJudgeContext =
        MissionJudgeContext(
            goal = "안전하게 배 떨어뜨리기",
            examples = listOf("무엇을 사용할지"),
            text = text,
        )

    fun stubEntity(result: MissionJudgeLlmResult?) {
        every { callResponseSpec.entity(MissionJudgeLlmResult::class.java) } returns result
    }

    context("judge") {
        test("충족 답이면 passed=true를 반환한다") {
            stubEntity(
                MissionJudgeLlmResult(
                    passed = true,
                    coveredCriteria = listOf("무엇을 사용할지"),
                    missingCriteria = emptyList(),
                    hint = "",
                ),
            )

            val judgement = adapter.judge(context("긴 막대기로 나무를 밀어서 배를 떨어뜨려요"))

            judgement.passed shouldBe true
        }

        test("미충족 답이면 passed=false와 빈 문자열 아닌 힌트를 반환한다") {
            stubEntity(
                MissionJudgeLlmResult(
                    passed = false,
                    coveredCriteria = emptyList(),
                    missingCriteria = listOf("무엇을 사용할지"),
                    hint = "무엇을 사용해서 배를 떨어뜨릴지 이야기해 볼까요?",
                ),
            )

            val judgement = adapter.judge(context("몰라요"))

            judgement.passed shouldBe false
            judgement.hint.orEmpty().shouldNotBeBlank()
        }

        test("힌트가 비어 있으면 빠진 기준으로 힌트를 조립한다") {
            stubEntity(
                MissionJudgeLlmResult(
                    passed = false,
                    coveredCriteria = emptyList(),
                    missingCriteria = listOf("무엇을 사용할지"),
                    hint = "   ",
                ),
            )

            val judgement = adapter.judge(context("몰라요"))

            judgement.passed shouldBe false
            judgement.hint.orEmpty().shouldNotBeBlank()
        }

        test("LLM 응답이 null이면 미통과로 폴백하고 기본 힌트를 준다") {
            stubEntity(null)

            val judgement = adapter.judge(context("몰라요"))

            judgement.passed shouldBe false
            judgement.hint shouldBe MissionJudgeLlmResult.DEFAULT_HINT
        }

        test("LLM 호출이 예외를 던지면 미통과로 폴백한다") {
            every { callResponseSpec.entity(MissionJudgeLlmResult::class.java) } throws RuntimeException("boom")

            val judgement = adapter.judge(context("몰라요"))

            judgement.passed shouldBe false
            judgement.hint shouldBe MissionJudgeLlmResult.DEFAULT_HINT
        }
    }
})
