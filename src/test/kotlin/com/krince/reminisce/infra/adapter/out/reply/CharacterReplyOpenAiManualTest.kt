package com.krince.reminisce.infra.adapter.out.reply

import com.krince.reminisce.application.port.out.reply.CharacterReplyContext
import com.krince.reminisce.domain.model.speakingsession.vo.ResponseMode
import com.krince.reminisce.domain.model.story.vo.ThinkingElement
import io.kotest.core.spec.style.FunSpec
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.openai.OpenAiChatModel
import org.springframework.ai.openai.OpenAiChatOptions
import org.springframework.ai.openai.api.OpenAiApi
import java.io.File

class CharacterReplyOpenAiManualTest : FunSpec({

    val key: String = System.getenv("B9_OPENAI_KEY").orEmpty()
    val model: String = System.getenv("B9_OPENAI_MODEL") ?: "gpt-4o-mini"
    val outPath: String? = System.getenv("B10_OPENAI_OUT")

    test("실제 모델로 캐릭터 대사 생성 (수동 — B9_OPENAI_KEY 있을 때만)").config(enabled = key.isNotBlank()) {
        val chatModel = OpenAiChatModel.builder()
            .openAiApi(OpenAiApi.builder().apiKey(key).build())
            .defaultOptions(OpenAiChatOptions.builder().model(model).build())
            .build()
        val adapter = CharacterReplyOpenAiAdapter(ChatClient.builder(chatModel))

        fun context(mode: ResponseMode, utterance: String, target: ThinkingElement?) =
            CharacterReplyContext(
                characterDisplayName = "며느리",
                mode = mode,
                childUtterance = utterance,
                guidanceTarget = target,
                characterOpening = "흑흑, 저는 방귀를 너무 크게 뀌어서 집에서 쫓겨날 위기에 처했어요.",
                conflict = "큰 방귀 때문에 며느리가 집에서 쫓겨날 위기에 놓였어요.",
                sceneGoal = "며느리의 방귀가 특별한 장점이 될 수 있도록 아이가 함께 생각해 보게 한다.",
            )

        val cases = listOf(
            Triple(ResponseMode.NORMAL, "며느리 아줌마 너무 불쌍해요.", null),
            Triple(ResponseMode.GUIDED, "몰라요.", ThinkingElement.SOLUTION),
            Triple(ResponseMode.GUIDED, "방귀 뀌면 안 돼요.", ThinkingElement.PERSPECTIVE),
        )

        val report = StringBuilder("model=$model\n\n")
        cases.forEach { (mode, utterance, target) ->
            val reply = adapter.generate(context(mode, utterance, target))
            report.appendLine("[$mode${target?.let { "/$it" } ?: ""}] 아이: $utterance")
            report.appendLine("  -> 며느리: $reply")
            report.appendLine()
        }

        val text = report.toString()
        if (outPath != null) File(outPath).writeText(text) else println(text)
    }
})
