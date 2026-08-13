package com.krince.reminisce.infra.adapter.out.tts

import com.krince.reminisce.application.port.out.file.StoreFilePort
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldNotBe
import org.springframework.web.client.RestClient
import org.springframework.web.multipart.MultipartFile
import java.io.File

class TtsOpenAiManualTest : FunSpec({

    val key: String = System.getenv("B9_OPENAI_KEY").orEmpty()
    val model: String = System.getenv("B11_TTS_MODEL") ?: "gpt-4o-mini-tts"
    val voice: String = System.getenv("B11_TTS_VOICE") ?: "nova"
    val outDir: String = System.getenv("B11_TTS_OUT") ?: System.getProperty("java.io.tmpdir")

    test("실제 OpenAI TTS로 대사를 음성 파일로 (수동 — B9_OPENAI_KEY 있을 때만)").config(enabled = key.isNotBlank()) {
        val saved = mutableListOf<File>()
        val store = object : StoreFilePort {
            override fun saveImage(file: MultipartFile?): String? = null
            override fun saveImageOrThrows(file: MultipartFile?): String = error("unused")
            override fun saveAudioOrThrows(file: MultipartFile?): String = error("unused")
            override fun saveAudioBytes(bytes: ByteArray, extension: String): String {
                val f = File(outDir, "b11-tts-sample.$extension")
                f.parentFile?.mkdirs()
                f.writeBytes(bytes)
                saved.add(f)
                return f.absolutePath
            }

            override fun deleteFile(fileUrl: String?) {}
        }

        val adapter = TtsOpenAiAdapter(
            apiKey = key,
            model = model,
            voice = voice,
            baseUrl = "https://api.openai.com",
            storeFilePort = store,
            restClientBuilder = RestClient.builder(),
        )

        val url: String? = adapter.synthesize("며느리예요. 방귀 때문에 걱정이 많지만, 너와 이야기하니 힘이 나요.")

        val bytes: Long = saved.firstOrNull()?.length() ?: 0L
        File(outDir, "b11-tts-report.txt").writeText(
            "model=$model voice=$voice\nurl=$url\nfile=${saved.firstOrNull()?.absolutePath}\nbytes=$bytes\n",
        )

        url shouldNotBe null
        bytes.toInt() shouldBeGreaterThan 0
    }
})
