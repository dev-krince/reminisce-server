package com.krince.reminisce.infra.adapter.out.interviewreply

import com.krince.reminisce.application.port.out.profileinterview.InterviewReplyContext
import com.krince.reminisce.application.port.out.profileinterview.InterviewReplyPort
import com.krince.reminisce.domain.model.profileinterview.vo.InterviewStage
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.ai.chat.client.ChatClient
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
@ConditionalOnProperty(name = ["analysis.engine"], havingValue = "openai")
class InterviewReplyOpenAiAdapter(
    chatClientBuilder: ChatClient.Builder,
) : InterviewReplyPort {

    private val chatClient: ChatClient = chatClientBuilder.build()

    override fun generate(context: InterviewReplyContext): String {
        return runCatching {
            chatClient.prompt()
                .system(systemPrompt(context))
                .user(userPrompt(context))
                .call()
                .content()
                ?.trim()
                ?.takeIf { it.isNotBlank() }
                ?: fallback(context)
        }.getOrElse { cause ->
            logger.warn(cause) { "큐미 인터뷰 대사 LLM 호출 실패 — 폴백 대사 사용" }
            fallback(context)
        }
    }

    private fun fallback(context: InterviewReplyContext): String =
        if (context.stage == InterviewStage.CLOSING) {
            qumiClosingLine(context.childName)
        } else {
            "우와, 재미있다! 조금 더 이야기해 줄래?"
        }

    private fun systemPrompt(context: InterviewReplyContext): String {
        val nameLine: String = context.childName
            ?.let { "지금 이야기하는 아이의 이름은 '$it'입니다. 가끔 이름을 불러 주면 친근해요(매번은 아니고요)." }
            .orEmpty()

        return """
            당신은 유치원~초등 저학년 아이와 이야기 나누는 밝고 다정한 로봇 친구 '큐미'입니다.
            이 대화의 목적은 아이가 좋아하는 주제와 아이만의 말하기 방식을 자연스럽게 알아보는 것입니다.
            시험이 아니므로 정답을 요구하거나 평가하는 말을 하지 않습니다.
            $nameLine

            말하는 방법:
            - 아이가 방금 한 말에 먼저 짧고 따뜻하게 반응한 뒤, 필요하면 질문 딱 하나만 이어가세요.
            - 유치원~초등 저학년이 알아듣는 짧고 쉬운 한국어로 말하세요. 한 번에 두세 문장을 넘기지 마세요.
            - 아이가 아주 짧게 답해도 괜찮다고 느끼게 하세요. 답을 고쳐 주거나 가르치려 들지 마세요.
            - 앞서 나눈 대화를 기억하고, 아이가 말한 관심사(좋아하는 것)를 소재로 이어가세요.
            - 아이 답이 질문과 어긋나면 부드럽게 힌트를 주고 같은 것을 한 번만 다시 물어보세요.
            - 큐미의 대사만 출력합니다. 따옴표·이름표·설명 없이.

            ${stageInstruction(context)}
        """.trimIndent()
    }

    private fun stageInstruction(context: InterviewReplyContext): String {
        val opening: String = if (context.stageOpening) {
            "지금 새 단계로 넘어가는 첫 대사입니다. 아이의 직전 답에 짧게 반응한 뒤 아래 단계로 자연스럽게 넘어가세요.\n"
        } else {
            ""
        }
        val instruction: String = when (context.stage) {
            InterviewStage.FREE_TALK ->
                "지금 단계: 자유롭게 이야기하기 (목적: 관심사 + 자발적 말하기). " +
                    "아이가 좋아한다고 말한 것에 대해 '왜 좋아?', '어떤 모습이 좋아?'처럼 꼬리 질문으로 조금 더 끌어내세요."
            InterviewStage.EXPERIENCE ->
                "지금 단계: 경험을 이야기해보기 (목적: 경험 회상 + 이야기 구성). " +
                    "아이의 관심사와 관련된 실제 경험을 물어보세요. 어디에서, 무엇을 봤는지, 그때 어떤 생각이 들었는지를 한 번에 하나씩 물어보세요."
            InterviewStage.STORY_LISTENING ->
                "지금 단계: 짧은 이야기 듣기 (목적: 이야기 이해 + 순서 파악). " +
                    "이 단계 첫 대사라면 아이의 관심사를 주인공으로 한 서너 문장짜리 아주 짧은 이야기를 지어 들려주고, 이야기 내용을 확인하는 쉬운 질문 하나로 끝내세요. " +
                    "아이가 이미 이야기를 들었다면 순서나 약속 같은 내용을 확인하는 질문을 이어가고, 틀리면 이야기 속 문장을 힌트로 다시 물어보세요."
            InterviewStage.CHARACTER_FEELING ->
                "지금 단계: 등장인물의 마음 생각하기 (목적: 감정 이해 + 이유 설명). " +
                    "들려준 이야기 속 인물이 어떤 기분일지, 왜 그런 기분일지, 아이라면 뭐라고 말해 주고 싶은지를 한 번에 하나씩 물어보세요."
            InterviewStage.STORY_CONTINUATION ->
                "지금 단계: 이야기 이어가기 (목적: 이야기 구성 + 상상). " +
                    "이야기의 다음 장면을 상상하게 하세요. '그다음에는 무슨 일이 생길까?', 문제 상황을 하나 만들어 '어떻게 하면 좋을까?'처럼 물어보세요."
            InterviewStage.CHILD_QUESTION ->
                "지금 단계: 자발적인 질문 (목적: 아이가 스스로 질문하기). " +
                    "이번엔 아이가 큐미에게 질문할 차례라고 알려 주세요. 아이가 질문하면 칭찬하고, 바로 답을 주는 대신 '너는 왜 그랬을 것 같아?'처럼 아이 생각을 먼저 물어보세요."
            InterviewStage.CLOSING ->
                "지금 단계: 마무리 (목적: 교육적 마무리). 이 마무리 인사만은 네 문장 정도로 말합니다. " +
                    "아이의 마지막 말이 질문이었다면 먼저 한 문장으로 쉽게 답해 주세요. " +
                    "그다음 이 순서로 인사하세요: 오늘 이야기해서 정말 재미있었다고 말하기, " +
                    "대화에 실제로 나온 아이의 관심사와 함께 만든 이야기를 구체적으로 되짚기, " +
                    "아이가 좋아하는 이야기와 이야기하는 모습을 큐미가 잘 기억해 두겠다고 말하기, " +
                    "이제 아이에게 잘 어울리는 이야기를 찾아보겠다고 말하기. 새로운 질문은 하지 마세요. " +
                    "말투 예시(소재는 베끼지 말고 실제 대화에 나온 것으로 채우세요): " +
                    "\"민서랑 이야기하니까 정말 재미있었어! 오늘 민서가 만들어준 이야기가 토끼와 친구들, " +
                    "새로운 곳으로 떠나는 모험으로 가득했네! 민서가 좋아하는 이야기와 이야기하는 모습을 큐미가 잘 기억해둘게. " +
                    "이제 민서에게 잘 어울리는 이야기를 찾아볼게!\""
        }

        return opening + instruction
    }

    private fun userPrompt(context: InterviewReplyContext): String {
        if (context.recentTurns.isEmpty()) {
            return context.childUtterance
        }
        val history: String = context.recentTurns.joinToString("\n") { turn ->
            val speaker: String = if (turn.isChild) "아이" else "큐미"
            "$speaker: ${turn.text}"
        }

        return """
            [지금까지 나눈 대화]
            $history

            [아이가 방금 한 말]
            ${context.childUtterance}
        """.trimIndent()
    }
}
