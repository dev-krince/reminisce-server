package com.krince.reminisce.application.service.child

import com.krince.reminisce.application.port.out.message.CommandMessagePort
import com.krince.reminisce.application.port.out.message.LoadMessagePort
import com.krince.reminisce.application.port.out.missionresult.CommandMissionResultPort
import com.krince.reminisce.application.port.out.postactivityresult.CommandPostActivityResultPort
import com.krince.reminisce.application.port.out.postactivityresult.LoadPostActivityResultPort
import com.krince.reminisce.application.port.out.report.CommandReportPort
import com.krince.reminisce.application.port.out.savedstory.CommandSavedStoryPort
import com.krince.reminisce.application.port.out.speakingsession.CommandSpeakingSessionPort
import com.krince.reminisce.application.port.out.speakingsession.LoadSpeakingSessionPort
import com.krince.reminisce.application.port.out.utteranceanalysis.CommandUtteranceAnalysisPort
import com.krince.reminisce.application.port.out.wordbook.CommandSavedWordPort
import com.krince.reminisce.domain.model.child.vo.ChildId
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder

@Tags("test", "unitTest")
@DisplayName("ChildLearningDataPurger 단위테스트")
class ChildLearningDataPurgerTest : FunSpec({

    val loadSpeakingSessionPort = mockk<LoadSpeakingSessionPort>()
    val loadMessagePort = mockk<LoadMessagePort>()
    val loadPostActivityResultPort = mockk<LoadPostActivityResultPort>()
    val commandSpeakingSessionPort = mockk<CommandSpeakingSessionPort>()
    val commandMessagePort = mockk<CommandMessagePort>()
    val commandReportPort = mockk<CommandReportPort>()
    val commandPostActivityResultPort = mockk<CommandPostActivityResultPort>()
    val commandMissionResultPort = mockk<CommandMissionResultPort>()
    val commandUtteranceAnalysisPort = mockk<CommandUtteranceAnalysisPort>()
    val commandSavedWordPort = mockk<CommandSavedWordPort>()
    val commandSavedStoryPort = mockk<CommandSavedStoryPort>()
    val purger = ChildLearningDataPurger(
        loadSpeakingSessionPort = loadSpeakingSessionPort,
        loadMessagePort = loadMessagePort,
        loadPostActivityResultPort = loadPostActivityResultPort,
        commandSpeakingSessionPort = commandSpeakingSessionPort,
        commandMessagePort = commandMessagePort,
        commandReportPort = commandReportPort,
        commandPostActivityResultPort = commandPostActivityResultPort,
        commandMissionResultPort = commandMissionResultPort,
        commandUtteranceAnalysisPort = commandUtteranceAnalysisPort,
        commandSavedWordPort = commandSavedWordPort,
        commandSavedStoryPort = commandSavedStoryPort,
    )

    beforeEach { clearAllMocks() }

    val childIds = listOf(ChildId("child-1"), ChildId("child-2"))
    val sessionIds = listOf("session-1", "session-2")
    val messageIds = listOf("message-1", "message-2")
    val audioUrls = listOf("/files/retelling-1.m4a", "/files/retelling-2.webm")

    context("세션 계열·단어장 파기") {
        test("발화분석→메시지→리포트→후속활동→미션결과→세션→단어→찜 순으로 leaf→root 파기하고 재구성 음성 URL을 반환한다") {
            every { loadSpeakingSessionPort.findSessionIdsByChildIds(childIds) } returns sessionIds
            every { loadPostActivityResultPort.findRetellingAudioUrlsBySessionIds(sessionIds) } returns audioUrls
            every { loadMessagePort.findMessageIdsBySessionIds(sessionIds) } returns messageIds
            every { commandUtteranceAnalysisPort.deleteAllByMessageIds(messageIds) } returns Unit
            every { commandMessagePort.deleteAllBySessionIds(sessionIds) } returns Unit
            every { commandReportPort.deleteAllBySessionIds(sessionIds) } returns Unit
            every { commandPostActivityResultPort.deleteAllBySessionIds(sessionIds) } returns Unit
            every { commandMissionResultPort.deleteAllBySessionIds(sessionIds) } returns Unit
            every { commandSpeakingSessionPort.deleteAllByChildIds(childIds) } returns Unit
            every { commandSavedWordPort.deleteAllByChildIds(childIds) } returns Unit
            every { commandSavedStoryPort.deleteAllByChildIds(childIds) } returns Unit

            val result = purger.purge(childIds)

            result shouldBe audioUrls
            verifyOrder {
                commandUtteranceAnalysisPort.deleteAllByMessageIds(messageIds)
                commandMessagePort.deleteAllBySessionIds(sessionIds)
                commandReportPort.deleteAllBySessionIds(sessionIds)
                commandPostActivityResultPort.deleteAllBySessionIds(sessionIds)
                commandMissionResultPort.deleteAllBySessionIds(sessionIds)
                commandSpeakingSessionPort.deleteAllByChildIds(childIds)
                commandSavedWordPort.deleteAllByChildIds(childIds)
                commandSavedStoryPort.deleteAllByChildIds(childIds)
            }
        }

        test("세션이 없으면 세션 계열 삭제를 전혀 호출하지 않고 단어·찜만 삭제한다") {
            every { loadSpeakingSessionPort.findSessionIdsByChildIds(childIds) } returns emptyList()
            every { commandSavedWordPort.deleteAllByChildIds(childIds) } returns Unit
            every { commandSavedStoryPort.deleteAllByChildIds(childIds) } returns Unit

            val result = purger.purge(childIds)

            result shouldBe emptyList()
            verify(exactly = 0) { loadMessagePort.findMessageIdsBySessionIds(any()) }
            verify(exactly = 0) { commandUtteranceAnalysisPort.deleteAllByMessageIds(any()) }
            verify(exactly = 0) { commandMessagePort.deleteAllBySessionIds(any()) }
            verify(exactly = 0) { commandReportPort.deleteAllBySessionIds(any()) }
            verify(exactly = 0) { commandPostActivityResultPort.deleteAllBySessionIds(any()) }
            verify(exactly = 0) { commandMissionResultPort.deleteAllBySessionIds(any()) }
            verify(exactly = 0) { commandSpeakingSessionPort.deleteAllByChildIds(any()) }
            verify(exactly = 1) { commandSavedWordPort.deleteAllByChildIds(childIds) }
            verify(exactly = 1) { commandSavedStoryPort.deleteAllByChildIds(childIds) }
        }

        test("메시지가 없으면 발화분석 삭제는 건너뛰고 나머지 세션 계열은 삭제한다") {
            every { loadSpeakingSessionPort.findSessionIdsByChildIds(childIds) } returns sessionIds
            every { loadPostActivityResultPort.findRetellingAudioUrlsBySessionIds(sessionIds) } returns emptyList()
            every { loadMessagePort.findMessageIdsBySessionIds(sessionIds) } returns emptyList()
            every { commandMessagePort.deleteAllBySessionIds(sessionIds) } returns Unit
            every { commandReportPort.deleteAllBySessionIds(sessionIds) } returns Unit
            every { commandPostActivityResultPort.deleteAllBySessionIds(sessionIds) } returns Unit
            every { commandMissionResultPort.deleteAllBySessionIds(sessionIds) } returns Unit
            every { commandSpeakingSessionPort.deleteAllByChildIds(childIds) } returns Unit
            every { commandSavedWordPort.deleteAllByChildIds(childIds) } returns Unit
            every { commandSavedStoryPort.deleteAllByChildIds(childIds) } returns Unit

            purger.purge(childIds)

            verify(exactly = 0) { commandUtteranceAnalysisPort.deleteAllByMessageIds(any()) }
            verify(exactly = 1) { commandMessagePort.deleteAllBySessionIds(sessionIds) }
            verify(exactly = 1) { commandSpeakingSessionPort.deleteAllByChildIds(childIds) }
            verify(exactly = 1) { commandSavedStoryPort.deleteAllByChildIds(childIds) }
        }

        test("아이 목록이 비면 세션 조회조차 하지 않고 단어·찜 삭제도 하지 않으며 빈 목록을 반환한다") {
            val result = purger.purge(emptyList())

            result shouldBe emptyList()
            verify(exactly = 0) { loadSpeakingSessionPort.findSessionIdsByChildIds(any()) }
            verify(exactly = 0) { commandSavedWordPort.deleteAllByChildIds(any()) }
            verify(exactly = 0) { commandSavedStoryPort.deleteAllByChildIds(any()) }
        }
    }
})
