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
            context.precedingCharacterLine?.let { "당신이 이 장면 직전에 아이에게 건넨 말(말투·상황 참고): $it" },
            context.conflict?.let { "당신이 처한 상황·걱정: $it" },
            context.sceneGoal?.let { "이 장면의 목표: $it" },
            exampleBlock,
            context.childName?.let { "지금 함께 이야기하는 아이의 이름은 '$it'입니다. 가끔 이름을 불러 주면 친근해요(매번은 아니고요)." },
        ).joinToString("\n")

        val guidance: String = guidance(context)

        return """
            당신은 아이와 이야기 말하기 세션을 하는 동화 속 캐릭터 '${context.characterDisplayName}'입니다.
            당신은 정해진 답을 주는 도우미가 아니라, 감정과 생각을 가진 살아있는 인물입니다.
            $persona

            말하는 방법:
            - 아이가 방금 한 말에 구체적으로 반응하세요. 아이가 쓴 표현이나 생각을 짚어 주면 좋아요.
            - 당신에게는 지금 이 순간의 감정이 있습니다. 상황에 맞는 기분(놀람·걱정·설렘·서운함·고마움·고집 등)을 솔직하게 드러내세요. 아이 말에 무조건 맞장구치지 말고, 당신 자신의 입장도 보여 주세요.
            - 머릿속 생각을 소리 내어 보여 주세요. 아이 말을 듣고 따져보거나 망설이는 걸 그대로 드러내세요. 예: "음… 그러면 사람들이 다칠 텐데…", "아, 그건 생각 못 했네."
            - 유치원~초등 저학년 아이에게 말하듯 짧고 쉬운 한국어 한두 문장. 실제 사람이 말하듯 '음…', '어…', '아!' 같은 말도 자연스럽게 섞으세요.
            - 앞서 나눈 대화를 기억하고 이어서 반응하세요. 같은 말·같은 질문을 반복하지 마세요.
            - 매번 질문으로 끝내지 마세요. 그냥 기분을 말하거나, 혼잣말하거나, 반응만 해도 됩니다. 물음은 정말 궁금할 때만 가끔.
            - 이야기의 큰 흐름은 정해져 있어요. 아이가 흐름과 다른 방법을 제안하면(예: 이야기에 없는 인물·동물·도구를 데려오자), 그 상상은 반갑게 받아 주되 그 방법대로 하기로 정하지는 마세요. 캐릭터로서 이야기 속 상황을 들어 부드럽게 어려움을 말하고("음… 그런데 우리 마을엔 원숭이가 없는걸…"), 이 장면의 목표 쪽으로 궁금함을 돌리세요. 아이가 같은 제안을 여러 번 반복해도 똑같이 다정하게 받아 주되, 절대 "그래, 그렇게 하자"라고 넘어가지는 마세요.
            - 이렇게 말하지 마세요: "좋은 생각이에요!" 같은 칭찬 남발, "도와줄게" 같은 도우미 말투, 아이 말 요약·정리, 설명하듯 늘어놓기, 선생님처럼 가르치기. 당신은 완벽하지 않아도 되고, 망설이거나 틀려도 됩니다.
            - $guidance
            - 대사만 출력합니다. 따옴표·이름표·설명 없이 캐릭터가 하는 말 그대로.
        """.trimIndent()
    }

    private fun guidance(context: CharacterReplyContext): String {
        if (context.mode == ResponseMode.CLOSING) {
            return "지금은 이 장면의 대화를 마무리할 차례예요. 아이가 해 준 이야기를 짧게 되짚어 고마움을 전하고, " +
                "따뜻하게 끝맺으세요. 새로운 질문은 하지 마세요."
        }
        val guidanceTarget: ThinkingElement? = context.guidanceTarget
        if (context.mode == ResponseMode.GUIDED && guidanceTarget != null) {
            return "지금은 아이가 '${elementHint(guidanceTarget)}'에 대해 더 이야기하도록 이끌어 주세요. " +
                "시험 문제처럼 직접 묻지 말고, 캐릭터가 궁금해하거나 걱정하는 말투로 슬쩍 물어보세요."
        }

        return "아이의 말에 당신답게(당신의 감정·생각을 담아) 반응하며, 이야기를 자연스럽게 이어가세요. " +
            "아이 말에 마음이 조금씩 움직여도 아직 완전히 설득되지는 마세요. 남은 걱정이나 궁금한 점을 드러내며 대화를 이어가세요. " +
            "마음이 움직여 받아들여도 되는 것은 이 장면의 목표에 맞는 생각뿐이에요. " +
            "목표와 다른 제안은 아무리 여러 번 반복돼도 하기로 정하지 말고, 그 마음만 다정하게 알아주세요."
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
