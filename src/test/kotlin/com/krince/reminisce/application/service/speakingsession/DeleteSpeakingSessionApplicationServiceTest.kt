package com.krince.reminisce.application.service.speakingsession

import com.krince.reminisce.application.port.access.child.ChildAccessPort
import com.krince.reminisce.application.port.`in`.speakingsession.command.DeleteSpeakingSessionCommand
import com.krince.reminisce.application.port.out.file.StoreFilePort
import com.krince.reminisce.application.port.out.speakingsession.CommandSpeakingSessionPort
import com.krince.reminisce.application.port.out.speakingsession.LoadSpeakingSessionPort
import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.speakingsession.SpeakingSession
import com.krince.reminisce.domain.model.speakingsession.vo.SpeakingSessionId
import com.krince.reminisce.domain.model.story.vo.StoryId
import com.krince.reminisce.domain.model.user.vo.UserId
import com.krince.reminisce.shared.exception.NotFoundException
import com.krince.reminisce.shared.response.ExceptionResponseCode.NOT_FOUND
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import java.time.LocalDateTime

@Tags("test", "unitTest")
@DisplayName("DeleteSpeakingSessionApplicationService 단위테스트")
class DeleteSpeakingSessionApplicationServiceTest : FunSpec({

    val loadSpeakingSessionPort = mockk<LoadSpeakingSessionPort>()
    val commandSpeakingSessionPort = mockk<CommandSpeakingSessionPort>()
    val childAccessPort = mockk<ChildAccessPort>()
    val sessionCascadePurger = mockk<SpeakingSessionCascadePurger>()
    val storeFilePort = mockk<StoreFilePort>()
    val service = DeleteSpeakingSessionApplicationService(
        loadSpeakingSessionPort = loadSpeakingSessionPort,
        commandSpeakingSessionPort = commandSpeakingSessionPort,
        childAccessPort = childAccessPort,
        sessionCascadePurger = sessionCascadePurger,
        storeFilePort = storeFilePort,
    )

    beforeEach { clearAllMocks() }

    val guardianIdStr = "guardian-uuid-1"
    val childId = ChildId("child-uuid-1")
    val guardianId = UserId(guardianIdStr)
    val startedAt = LocalDateTime.of(2026, 8, 17, 10, 0)

    fun session(): SpeakingSession = SpeakingSession.start(childId, StoryId("story-1"), startedAt)

    fun command(sessionId: String): DeleteSpeakingSessionCommand =
        DeleteSpeakingSessionCommand(guardianId = guardianIdStr, sessionId = sessionId)

    context("성공") {
        test("세션 종속 데이터 파기 후 세션을 삭제하고 음성 파일을 지운다") {
            val target = session()
            val audioUrls = listOf("/files/utterance-1.m4a", "/files/retelling-1.webm")
            every { loadSpeakingSessionPort.findById(target.sessionId) } returns target
            every { childAccessPort.findGuardianId(childId) } returns guardianId
            every { sessionCascadePurger.purgeBySessionIds(listOf(target.sessionId.value)) } returns audioUrls
            every { commandSpeakingSessionPort.deleteById(target.sessionId) } returns Unit
            every { storeFilePort.deleteFile(any()) } returns Unit

            service.execute(command(target.sessionId.value))

            verifyOrder {
                sessionCascadePurger.purgeBySessionIds(listOf(target.sessionId.value))
                commandSpeakingSessionPort.deleteById(target.sessionId)
            }
            verify(exactly = 1) { storeFilePort.deleteFile("/files/utterance-1.m4a") }
            verify(exactly = 1) { storeFilePort.deleteFile("/files/retelling-1.webm") }
        }

        test("음성 파일이 없으면 deleteFile을 호출하지 않는다") {
            val target = session()
            every { loadSpeakingSessionPort.findById(target.sessionId) } returns target
            every { childAccessPort.findGuardianId(childId) } returns guardianId
            every { sessionCascadePurger.purgeBySessionIds(any()) } returns emptyList()
            every { commandSpeakingSessionPort.deleteById(target.sessionId) } returns Unit

            service.execute(command(target.sessionId.value))

            verify(exactly = 0) { storeFilePort.deleteFile(any()) }
        }

        test("완료된 세션도 상태 제한 없이 삭제한다") {
            val target = session().enterPostActivity(startedAt.plusMinutes(5)).complete(startedAt.plusMinutes(10))
            every { loadSpeakingSessionPort.findById(target.sessionId) } returns target
            every { childAccessPort.findGuardianId(childId) } returns guardianId
            every { sessionCascadePurger.purgeBySessionIds(any()) } returns emptyList()
            every { commandSpeakingSessionPort.deleteById(target.sessionId) } returns Unit

            service.execute(command(target.sessionId.value))

            verify(exactly = 1) { commandSpeakingSessionPort.deleteById(target.sessionId) }
        }
    }

    context("예외케이스") {
        test("세션이 없으면 NOT_FOUND를 던지고 아무것도 삭제하지 않는다") {
            every { loadSpeakingSessionPort.findById(SpeakingSessionId("unknown")) } returns null

            val exception = shouldThrow<NotFoundException> { service.execute(command("unknown")) }

            exception.exceptionResponseCode shouldBe NOT_FOUND
            verify(exactly = 0) { sessionCascadePurger.purgeBySessionIds(any()) }
        }

        test("다른 보호자의 아이 세션이면 NOT_FOUND로 은닉한다") {
            val target = session()
            every { loadSpeakingSessionPort.findById(target.sessionId) } returns target
            every { childAccessPort.findGuardianId(childId) } returns UserId("other-guardian")

            val exception = shouldThrow<NotFoundException> { service.execute(command(target.sessionId.value)) }

            exception.exceptionResponseCode shouldBe NOT_FOUND
            verify(exactly = 0) { sessionCascadePurger.purgeBySessionIds(any()) }
            verify(exactly = 0) { commandSpeakingSessionPort.deleteById(any()) }
        }
    }
})
