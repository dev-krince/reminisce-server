package com.krince.reminisce.infra.adapter.out.mission

import com.krince.reminisce.application.port.out.mission.MissionJudgePort
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import org.springframework.ai.chat.client.ChatClient
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Tags("test", "unitTest")
@DisplayName("MissionJudge 게이팅 배선 단위테스트")
class MissionJudgeWiringTest : FunSpec({

    val runner = ApplicationContextRunner()
        .withUserConfiguration(MissionJudgeWiringTestConfig::class.java)
        .withUserConfiguration(MissionJudgeOpenAiAdapter::class.java, MissionJudgeStubAdapter::class.java)

    test("analysis.engine=openai면 OpenAI 어댑터가 주입된다") {
        runner.withPropertyValues(
            "analysis.engine=openai",
            "analysis.openai.model=gpt-4o-mini",
            "analysis.openai.temperature=0.2",
        ).run { ctx ->
            ctx.getBean(MissionJudgePort::class.java).shouldBeInstanceOf<MissionJudgeOpenAiAdapter>()
        }
    }

    test("analysis.engine=stub이면 stub 어댑터가 주입된다") {
        runner.withPropertyValues("analysis.engine=stub").run { ctx ->
            ctx.getBean(MissionJudgePort::class.java).shouldBeInstanceOf<MissionJudgeStubAdapter>()
        }
    }

    test("analysis.engine 미설정이면 stub 어댑터가 주입된다") {
        runner.run { ctx ->
            ctx.getBean(MissionJudgePort::class.java).shouldBeInstanceOf<MissionJudgeStubAdapter>()
        }
    }
})

@Configuration
@ConditionalOnProperty(name = ["analysis.engine"], havingValue = "openai")
private open class MissionJudgeWiringTestConfig {

    @Bean
    open fun chatClientBuilder(): ChatClient.Builder {
        val builder = mockk<ChatClient.Builder>()
        every { builder.build() } returns mockk<ChatClient>()

        return builder
    }
}
