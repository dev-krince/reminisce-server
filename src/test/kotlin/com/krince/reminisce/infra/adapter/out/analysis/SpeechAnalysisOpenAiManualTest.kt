package com.krince.reminisce.infra.adapter.out.analysis

import io.kotest.core.spec.style.FunSpec
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.openai.OpenAiChatModel
import org.springframework.ai.openai.OpenAiChatOptions
import org.springframework.ai.openai.api.OpenAiApi
import java.io.File

class SpeechAnalysisOpenAiManualTest : FunSpec({

    val key: String = System.getenv("B9_OPENAI_KEY").orEmpty()
    val model: String = System.getenv("B9_OPENAI_MODEL") ?: "gpt-4o-mini"
    val outPath: String? = System.getenv("B9_OPENAI_OUT")

    test("실제 모델로 발화 분석 (수동 — B9_OPENAI_KEY 있을 때만)").config(enabled = key.isNotBlank()) {
        val chatModel = OpenAiChatModel.builder()
            .openAiApi(OpenAiApi.builder().apiKey(key).build())
            .defaultOptions(OpenAiChatOptions.builder().model(model).build())
            .build()
        val adapter = SpeechAnalysisOpenAiAdapter(ChatClient.builder(chatModel))

        val utterances = listOf(
            "며느리가 방귀 뀌어서 쫓겨나면 너무 불쌍해요. 그러니까 우리가 도와줘야 해요.",
            "몰라",
            "방귀가 힘이 세니까 오히려 좋은 점이 될 수 있어요. 배를 세게 밀어서 높은 나무 열매를 딸 수 있잖아요.",
        )

        val report = StringBuilder("model=$model\n\n")
        utterances.forEach { utterance ->
            val result = adapter.analyze(utterance, emptyList())
            report.appendLine("발화: $utterance")
            report.appendLine("  의도=${result.childIntent}  유효성=${result.validity}")
            report.appendLine("  핵심=${result.mainPoint}")
            report.appendLine("  요소=" + result.detectedElements.joinToString { "${it.type}(${it.evidence})" })
            report.appendLine()
        }

        val text = report.toString()
        if (outPath != null) File(outPath).writeText(text) else println(text)
    }
})
