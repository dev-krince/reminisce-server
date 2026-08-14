package com.krince.reminisce.infra.adapter.out.analysis

import com.krince.reminisce.application.port.out.analysis.SpeechAnalysisPort
import com.krince.reminisce.application.port.out.conversation.ConversationTurn
import com.krince.reminisce.domain.model.utteranceanalysis.RawUtteranceAnalysis
import com.krince.reminisce.domain.model.utteranceanalysis.vo.ChildIntent
import com.krince.reminisce.domain.model.utteranceanalysis.vo.UtteranceValidity
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.openai.OpenAiChatOptions
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
@ConditionalOnProperty(name = ["analysis.engine"], havingValue = "openai")
class SpeechAnalysisOpenAiAdapter(
    chatClientBuilder: ChatClient.Builder,
    @Value("\${analysis.openai.model}") model: String,
    @Value("\${analysis.openai.temperature}") temperature: Double,
) : SpeechAnalysisPort {

    private val chatClient: ChatClient = chatClientBuilder.build()
    private val options: OpenAiChatOptions = OpenAiChatOptions.builder()
        .model(model)
        .temperature(temperature)
        .build()

    override fun analyze(text: String, recentTurns: List<ConversationTurn>): RawUtteranceAnalysis {
        return runCatching {
            chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(userPrompt(text, recentTurns))
                .options(options)
                .call()
                .entity(AnalysisLlmResult::class.java)
                ?.toRawUtteranceAnalysis()
                ?: unclear()
        }.getOrElse { cause ->
            logger.warn(cause) { "발화 분석 LLM 호출 실패 — UNCLEAR로 폴백" }
            unclear()
        }
    }

    private fun userPrompt(text: String, recentTurns: List<ConversationTurn>): String {
        if (recentTurns.isEmpty()) {
            return text
        }
        val history: String = recentTurns.joinToString("\n") { turn ->
            val speaker: String = if (turn.isChild) "아이" else "캐릭터"
            "$speaker: ${turn.text}"
        }

        return """
            [직전 대화]
            $history

            [분석할 아이 발화]
            $text
        """.trimIndent()
    }

    private fun unclear(): RawUtteranceAnalysis =
        RawUtteranceAnalysis(
            childIntent = ChildIntent.UNCLEAR,
            mainPoint = null,
            detectedElements = emptyList(),
            validity = UtteranceValidity.UNCLEAR,
        )

    companion object {
        private val SYSTEM_PROMPT: String = """
            당신은 유치원~초등 저학년 아이의 '이야기 말하기 세션' 발화를 분석하는 도우미입니다.
            아이가 캐릭터에게 한 말(한국어)을 읽고 사고 요소를 추출하세요. 간결하게 판단합니다.

            childIntent — 발화 의도. 다음 중 하나만:
            QUESTION, OPINION, REASONING, SOLUTION, DECISION, PERSPECTIVE, EMOTION, REQUEST, CHALLENGE, PLAYFUL, OFF_TOPIC, SHORT_RESPONSE, UNCLEAR

            mainPoint — 아이가 말한 핵심을 짧은 한 구절로. 없으면 null.

            detectedElements — 발화에 나타난 사고 요소 목록. 각 항목:
            - type: EMOTION, EMPATHY, PERSPECTIVE, REASON, SOLUTION, DECISION, RESULT, REQUEST 중 하나
            - evidence: 그 근거가 된 '아이 발화 원문의 부분 문자열 그대로'. 실제 발화에 등장한 표현만. 지어내지 말 것.
            없는 요소는 넣지 않습니다.

            validity — 발화 유효성. 다음 중 하나만: VALID, SHORT, UNCLEAR, OFF_TOPIC, PLAYFUL

            직전 대화가 함께 주어지면 짧거나 생략된 발화(예: "응", "그거", "몰라")를 그 맥락으로 해석하세요.
            단 detectedElements의 evidence는 반드시 '분석할 아이 발화' 원문에 실제로 등장한 표현이어야 합니다. 직전 대화의 표현을 evidence로 쓰지 마세요.
        """.trimIndent()
    }
}
