package com.krince.reminisce.infra.adapter.out.mission

import com.krince.reminisce.application.port.out.mission.MissionJudgeContext
import com.krince.reminisce.application.port.out.mission.MissionJudgePort
import com.krince.reminisce.application.port.out.mission.MissionJudgement
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.openai.OpenAiChatOptions
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
@ConditionalOnProperty(name = ["analysis.engine"], havingValue = "openai")
class MissionJudgeOpenAiAdapter(
    chatClientBuilder: ChatClient.Builder,
    @Value("\${analysis.openai.model}") model: String,
    @Value("\${analysis.openai.temperature}") temperature: Double,
) : MissionJudgePort {

    private val chatClient: ChatClient = chatClientBuilder.build()
    private val options: OpenAiChatOptions = OpenAiChatOptions.builder()
        .model(model)
        .temperature(temperature)
        .build()

    override fun judge(context: MissionJudgeContext): MissionJudgement {
        return runCatching {
            chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(userPrompt(context))
                .options(options)
                .call()
                .entity(MissionJudgeLlmResult::class.java)
                ?.toMissionJudgement()
                ?: fallback()
        }.getOrElse { cause ->
            logger.warn(cause) { "미션 판정 LLM 호출 실패 — 미통과로 폴백" }
            fallback()
        }
    }

    private fun fallback(): MissionJudgement =
        MissionJudgement(passed = false, hint = MissionJudgeLlmResult.DEFAULT_HINT)

    private fun userPrompt(context: MissionJudgeContext): String {
        val criteria: String = context.examples
            .takeIf { it.isNotEmpty() }
            ?.joinToString("\n") { "- $it" }
            ?: NO_CRITERIA

        return """
            [미션 목표]
            ${context.goal}

            [판정 기준]
            $criteria

            [아이 답]
            ${context.text}
        """.trimIndent()
    }

    companion object {
        private const val NO_CRITERIA = "- (제시된 세부 기준 없음. 미션 목표를 기준으로 판단)"
        private val SYSTEM_PROMPT: String = """
            당신은 유치원~초등 저학년 아이의 '이야기 말하기 세션'에서 아이의 답이 미션을 충족했는지 판정하는 도우미입니다.
            아이의 답(한국어)을 [미션 목표]와 [판정 기준]에 비추어 평가하세요. 아이 눈높이에 맞춰 관대하게 판단합니다.

            판정 기준은 전부 채워야 하는 체크리스트가 아니라 참고 방향입니다. 각 기준이 아이 답에 드러났는지는 확인하되, 통과 여부를 기준 개수로 정하지 마세요.

            출력은 다음 JSON 형태로만 반환합니다.
            - passed: 아이 답이 판정 기준 중 하나 이상을 다루었거나, 미션 목표에 맞는 자기 생각·방법을 말했다면 true. 짧고 단순한 답이어도 목표 방향이면 true. 목표와 무관하거나 장난·무의미한 답일 때만 false.
            - coveredCriteria: 아이 답에서 이미 다뤄진 판정 기준 항목들의 목록.
            - missingCriteria: 아이 답에서 아직 빠진 판정 기준 항목들의 목록.
            - hint: 미통과일 때 빠진 항목(missingCriteria)에 맞춰 아이가 다음에 무엇을 말하면 좋을지 알려 주는 쉬운 한국어 한 문장. 통과했다면 빈 문자열.

            hint는 아이를 다그치지 말고, 무엇을 더 이야기하면 좋을지 다정하게 안내하는 한 문장으로 작성하세요.
        """.trimIndent()
    }
}
