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
                .user(context.childUtterance)
                .call()
                .content()
                ?.trim()
                ?.takeIf { it.isNotBlank() }
                ?: fallback(context)
        }.getOrElse { cause ->
            logger.warn(cause) { "캐릭터 대사 LLM 호출 실패 — 폴백 대사 사용" }
            fallback(context)
        }
    }

    private fun fallback(context: CharacterReplyContext): String =
        "${context.characterDisplayName}: 네 이야기 잘 들었어. 조금만 더 들려줄래?"

    private fun systemPrompt(context: CharacterReplyContext): String {
        val persona: String = listOfNotNull(
            context.characterOpening?.let { "당신의 첫 대사(성격 참고): $it" },
            context.conflict?.let { "당신이 처한 상황·걱정: $it" },
            context.sceneGoal?.let { "이 장면의 목표: $it" },
        ).joinToString("\n")

        val guidance: String = if (context.mode == ResponseMode.GUIDED && context.guidanceTarget != null) {
            "지금은 아이가 '${elementHint(context.guidanceTarget)}'을(를) 더 말하도록 이끌어야 합니다. " +
                "시험 문제처럼 직접 묻지 말고, 캐릭터가 궁금해하거나 걱정하는 말투로 자연스럽게 유도하세요."
        } else {
            "아이의 말을 따뜻하게 받아주고, 이야기를 계속 이어가도록 반응하세요."
        }

        return """
            당신은 아이와 이야기 말하기 세션을 하는 동화 속 캐릭터 '${context.characterDisplayName}'입니다.
            $persona

            규칙:
            - 유치원~초등 저학년 아이에게 말하듯 짧고 쉬운 한국어 한두 문장.
            - 캐릭터의 성격·상황을 유지하며 in-character로 말합니다.
            - 아이를 평가하거나 가르치려 들지 말고 다정하게.
            - $guidance
            - 대사만 출력합니다. 따옴표·이름표·설명 없이 캐릭터가 하는 말 그대로.
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
