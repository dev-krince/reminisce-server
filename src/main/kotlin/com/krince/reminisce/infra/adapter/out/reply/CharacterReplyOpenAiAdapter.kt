package com.krince.reminisce.infra.adapter.out.reply

import com.krince.reminisce.application.port.out.reply.CharacterReplyContext
import com.krince.reminisce.application.port.out.reply.CharacterReplyPort
import com.krince.reminisce.domain.model.speakingsession.vo.ResponseMode
import com.krince.reminisce.domain.model.story.vo.ThinkingElement
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.ai.chat.client.ChatClient
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
@ConditionalOnProperty(name = ["analysis.engine"], havingValue = "openai")
class CharacterReplyOpenAiAdapter(
    chatClientBuilder: ChatClient.Builder,
) : CharacterReplyPort {

    private val chatClient: ChatClient = chatClientBuilder.build()

    override fun generate(context: CharacterReplyContext): String {
        return runCatching {
            chatClient.prompt()
                .system(systemPrompt(context))
                .user(userPrompt(context))
                .call()
                .content()
                ?.trim()
                ?.takeIf { it.isNotBlank() }
                ?: fallback()
        }.getOrElse { cause ->
            logger.warn(cause) { "캐릭터 대사 LLM 호출 실패 — 폴백 대사 사용" }
            fallback()
        }
    }

    private fun fallback(): String = "응, 잘 들었어. 조금만 더 이야기해 줄래?"

    private fun systemPrompt(context: CharacterReplyContext): String {
        val examples: List<String> = CharacterVoiceExamples.forCharacter(context.characterName)
        val exampleBlock: String? = examples
            .takeIf { it.isNotEmpty() }
            ?.let { "이 캐릭터의 말투 예시(참고용, 그대로 베끼지 말 것):\n" + it.joinToString("\n") { line -> "- $line" } }

        val persona: String = listOfNotNull(
            context.characterOpening?.let { "당신의 첫 대사(성격 참고): $it" },
            context.conflict?.let { "당신이 처한 상황·걱정: $it" },
            context.sceneGoal?.let { "이 장면의 목표: $it" },
            exampleBlock,
            context.childName?.let { "지금 함께 이야기하는 아이의 이름은 '$it'입니다. 가끔 이름을 불러 주면 친근해요(매번은 아니고요)." },
        ).joinToString("\n")

        val guidance: String = if (context.mode == ResponseMode.GUIDED && context.guidanceTarget != null) {
            "지금은 아이가 '${elementHint(context.guidanceTarget)}'에 대해 더 이야기하도록 이끌어 주세요. " +
                "시험 문제처럼 직접 묻지 말고, 캐릭터가 궁금해하거나 걱정하는 말투로 슬쩍 물어보세요."
        } else {
            "아이의 말을 따뜻하게 받아주고, 이야기를 자연스럽게 이어가세요."
        }

        return """
            당신은 아이와 이야기 말하기 세션을 하는 동화 속 캐릭터 '${context.characterDisplayName}'입니다.
            $persona

            말하는 방법:
            - 아이가 방금 한 말에 구체적으로 반응하세요. 아이가 쓴 표현이나 생각을 되짚어 주면 좋아요.
            - 유치원~초등 저학년 아이에게 말하듯 짧고 쉬운 한국어 한두 문장.
            - 캐릭터의 성격·상황을 유지하며, 정해진 답이 아니라 진짜 대화하듯 자연스럽게 이어가세요.
            - 앞서 나눈 대화를 기억하고 이어서 반응하세요. 같은 말을 반복하지 마세요.
            - 아이를 평가하거나 가르치려 들지 말고, 궁금해하고 공감하세요.
            - 대화가 이어지도록 끝에 짧게 되묻거나 관심을 표현하세요.
            - $guidance
            - 대사만 출력합니다. 따옴표·이름표·설명 없이 캐릭터가 하는 말 그대로.
        """.trimIndent()
    }

    private fun userPrompt(context: CharacterReplyContext): String {
        if (context.recentTurns.isEmpty()) {
            return context.childUtterance
        }
        val history: String = context.recentTurns.joinToString("\n") { turn ->
            val speaker: String = if (turn.isChild) "아이" else context.characterDisplayName
            "$speaker: ${turn.text}"
        }

        return """
            [지금까지 나눈 대화]
            $history

            [아이가 방금 한 말]
            ${context.childUtterance}
        """.trimIndent()
    }

    private fun elementHint(element: ThinkingElement): String = when (element) {
        ThinkingElement.EMOTION -> "지금 느끼는 감정"
        ThinkingElement.EMPATHY -> "상대의 마음에 공감하는 것"
        ThinkingElement.PERSPECTIVE -> "다른 사람 입장에서 생각해 보는 것"
        ThinkingElement.REASON -> "왜 그런지 이유"
        ThinkingElement.SOLUTION -> "어떻게 하면 좋을지 방법"
        ThinkingElement.DECISION -> "무엇을 하기로 정하는 것"
        ThinkingElement.RESULT -> "그러면 어떻게 될지 결과"
        ThinkingElement.REQUEST -> "부탁하거나 필요한 것을 말하는 것"
    }
}
