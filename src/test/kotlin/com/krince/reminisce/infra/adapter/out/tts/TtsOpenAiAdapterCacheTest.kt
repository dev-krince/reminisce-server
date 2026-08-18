package com.krince.reminisce.infra.adapter.out.tts

import com.krince.reminisce.application.port.out.file.StoreFilePort
import com.krince.reminisce.application.port.out.tts.CommandTtsCachePort
import com.krince.reminisce.application.port.out.tts.LoadTtsCachePort
import com.krince.reminisce.domain.model.ttscache.TtsCache
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.springframework.web.client.RestClient

@Tags("test", "unitTest")
@DisplayName("TtsOpenAiAdapter 캐시 단위테스트")
class TtsOpenAiAdapterCacheTest : FunSpec({

    lateinit var server: MockWebServer
    lateinit var storeFilePort: StoreFilePort
    lateinit var loadTtsCachePort: LoadTtsCachePort
    lateinit var commandTtsCachePort: CommandTtsCachePort
    lateinit var adapter: TtsOpenAiAdapter

    beforeEach {
        server = MockWebServer()
        server.start()
        storeFilePort = mockk()
        loadTtsCachePort = mockk()
        commandTtsCachePort = mockk(relaxed = true)
        adapter = TtsOpenAiAdapter(
            apiKey = "test-key",
            model = "gpt-4o-mini-tts",
            voice = "nova",
            baseUrl = server.url("").toString().trimEnd('/'),
            storeFilePort = storeFilePort,
            loadTtsCachePort = loadTtsCachePort,
            commandTtsCachePort = commandTtsCachePort,
            restClientBuilder = RestClient.builder(),
        )
    }

    afterEach { server.shutdown() }

    fun audioResponse(): MockResponse =
        MockResponse()
            .setResponseCode(200)
            .addHeader("Content-Type", "audio/mpeg")
            .setBody("fake-audio-bytes")

    context("synthesize 캐시") {
        test("같은 (text, voiceProfile) 두 번째 호출은 합성 없이 캐시 URL을 반환한다") {
            server.enqueue(audioResponse())
            val cache = HashMap<String, String>()
            every { loadTtsCachePort.findFileUrlByCacheKey(any()) } answers { cache[firstArg()] }
            every { commandTtsCachePort.save(any()) } answers {
                val saved: TtsCache = firstArg()
                cache[saved.cacheKey] = saved.fileUrl
            }
            every { storeFilePort.saveAudioBytes(any(), any()) } returns "audio://opening"

            val first: String? = adapter.synthesize("며느리예요", "young_woman_gentle")
            val second: String? = adapter.synthesize("며느리예요", "young_woman_gentle")

            first shouldBe "audio://opening"
            second shouldBe "audio://opening"
            server.requestCount shouldBe 1
            verify(exactly = 1) { storeFilePort.saveAudioBytes(any(), any()) }
        }

        test("voiceProfile이 다르면 캐시 키가 달라 다시 합성한다") {
            server.enqueue(audioResponse())
            server.enqueue(audioResponse())
            every { loadTtsCachePort.findFileUrlByCacheKey(any()) } returns null
            every { storeFilePort.saveAudioBytes(any(), any()) } returnsMany listOf("audio://a", "audio://b")

            val keySlot = slot<TtsCache>()
            val keys = mutableListOf<String>()
            every { commandTtsCachePort.save(capture(keySlot)) } answers { keys.add(keySlot.captured.cacheKey) }

            val female: String? = adapter.synthesize("며느리예요", "young_woman_gentle")
            val male: String? = adapter.synthesize("며느리예요", "elderly_man_warm")

            female shouldBe "audio://a"
            male shouldBe "audio://b"
            server.requestCount shouldBe 2
            (keys[0] == keys[1]) shouldBe false
        }

        test("캐시 히트면 OpenAI 호출도 파일 저장도 하지 않는다") {
            every { loadTtsCachePort.findFileUrlByCacheKey(any()) } returns "audio://cached"

            val result: String? = adapter.synthesize("며느리예요", "young_woman_gentle")

            result shouldBe "audio://cached"
            server.requestCount shouldBe 0
            verify(exactly = 0) { storeFilePort.saveAudioBytes(any(), any()) }
            verify(exactly = 0) { commandTtsCachePort.save(any()) }
        }

        test("합성 실패(빈 오디오)면 캐시에 기록하지 않고 null을 반환한다") {
            server.enqueue(MockResponse().setResponseCode(500))
            every { loadTtsCachePort.findFileUrlByCacheKey(any()) } returns null

            val result: String? = adapter.synthesize("며느리예요", "young_woman_gentle")

            result shouldBe null
            verify(exactly = 0) { commandTtsCachePort.save(any()) }
        }

        test("합성 요청 body에 프로필별 목소리 연출 지시(instructions)를 담는다") {
            server.enqueue(audioResponse())
            every { loadTtsCachePort.findFileUrlByCacheKey(any()) } returns null
            every { storeFilePort.saveAudioBytes(any(), any()) } returns "audio://styled"

            adapter.synthesize("며느리예요", "young_woman_gentle")

            val requestBody: String = server.takeRequest().body.readUtf8()
            requestBody.contains("instructions") shouldBe true
            requestBody.contains("동화 속 젊은 며느리") shouldBe true
        }
    }
})
