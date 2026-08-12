package com.krince.reminisce.application.service.user

import com.krince.reminisce.application.port.`in`.user.command.WithdrawGuardianCommand
import com.krince.reminisce.application.port.out.auth.RefreshTokenPort
import com.krince.reminisce.application.port.out.child.CommandChildPort
import com.krince.reminisce.application.port.out.child.LoadChildPort
import com.krince.reminisce.application.port.out.childconsent.CommandChildConsentPort
import com.krince.reminisce.application.port.out.file.StoreFilePort
import com.krince.reminisce.application.port.out.message.CommandMessagePort
import com.krince.reminisce.application.port.out.message.LoadMessagePort
import com.krince.reminisce.application.port.out.postactivityresult.CommandPostActivityResultPort
import com.krince.reminisce.application.port.out.postactivityresult.LoadPostActivityResultPort
import com.krince.reminisce.application.port.out.report.CommandReportPort
import com.krince.reminisce.application.port.out.speakingsession.CommandSpeakingSessionPort
import com.krince.reminisce.application.port.out.speakingsession.LoadSpeakingSessionPort
import com.krince.reminisce.application.port.out.user.CommandUserPort
import com.krince.reminisce.application.port.out.user.LoadUserPort
import com.krince.reminisce.application.port.out.utteranceanalysis.CommandUtteranceAnalysisPort
import com.krince.reminisce.application.service.auth.AccessTokenBlacklister
import com.krince.reminisce.domain.model.child.Child
import com.krince.reminisce.domain.model.child.vo.BirthYear
import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.child.vo.ChildNickname
import com.krince.reminisce.domain.model.user.User
import com.krince.reminisce.domain.model.user.vo.AuthProvider
import com.krince.reminisce.domain.model.user.vo.Nickname
import com.krince.reminisce.domain.model.user.vo.Role
import com.krince.reminisce.domain.model.user.vo.UserId
import com.krince.reminisce.shared.exception.NotFoundException
import com.krince.reminisce.shared.response.ExceptionResponseCode.NOT_FOUND_USER
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

@Tags("test", "unitTest")
@DisplayName("WithdrawGuardianApplicationService 단위테스트")
class WithdrawGuardianApplicationServiceTest : FunSpec({

    val loadUserPort = mockk<LoadUserPort>()
    val loadChildPort = mockk<LoadChildPort>()
    val loadSpeakingSessionPort = mockk<LoadSpeakingSessionPort>()
    val loadMessagePort = mockk<LoadMessagePort>()
    val loadPostActivityResultPort = mockk<LoadPostActivityResultPort>()
    val commandChildConsentPort = mockk<CommandChildConsentPort>()
    val commandChildPort = mockk<CommandChildPort>()
    val commandUserPort = mockk<CommandUserPort>()
    val commandSpeakingSessionPort = mockk<CommandSpeakingSessionPort>()
    val commandMessagePort = mockk<CommandMessagePort>()
    val commandReportPort = mockk<CommandReportPort>()
    val commandPostActivityResultPort = mockk<CommandPostActivityResultPort>()
    val commandUtteranceAnalysisPort = mockk<CommandUtteranceAnalysisPort>()
    val storeFilePort = mockk<StoreFilePort>()
    val refreshTokenPort = mockk<RefreshTokenPort>()
    val accessTokenBlacklister = mockk<AccessTokenBlacklister>()
    val service = WithdrawGuardianApplicationService(
        loadUserPort = loadUserPort,
        loadChildPort = loadChildPort,
        loadSpeakingSessionPort = loadSpeakingSessionPort,
        loadMessagePort = loadMessagePort,
        loadPostActivityResultPort = loadPostActivityResultPort,
        commandChildConsentPort = commandChildConsentPort,
        commandChildPort = commandChildPort,
        commandUserPort = commandUserPort,
        commandSpeakingSessionPort = commandSpeakingSessionPort,
        commandMessagePort = commandMessagePort,
        commandReportPort = commandReportPort,
        commandPostActivityResultPort = commandPostActivityResultPort,
        commandUtteranceAnalysisPort = commandUtteranceAnalysisPort,
        storeFilePort = storeFilePort,
        refreshTokenPort = refreshTokenPort,
        accessTokenBlacklister = accessTokenBlacklister,
    )

    beforeEach { clearAllMocks() }

    val guardianIdStr = "guardian-uuid-1"
    val providedAccess = "Bearer access-token"

    fun kakaoGuardian(): User = User(
        userId = UserId(guardianIdStr),
        email = null,
        nickname = Nickname("보호자"),
        provider = AuthProvider.KAKAO,
        role = Role.user(),
        providerId = "kakao-1",
    )

    fun child(childIdStr: String): Child = Child(
        childId = ChildId(childIdStr),
        guardianId = UserId(guardianIdStr),
        nickname = ChildNickname("토토"),
        birthYear = BirthYear(2019),
    )

    context("하드 삭제 순서·조건") {
        test("동의→아이→유저 순으로 각 1회 삭제하고 유저를 조회한다") {
            val children = listOf(child("child-1"), child("child-2"))
            every { loadUserPort.findByUserId(UserId(guardianIdStr)) } returns kakaoGuardian()
            every { loadChildPort.findAllByGuardianId(UserId(guardianIdStr)) } returns children
            every { loadSpeakingSessionPort.findSessionIdsByChildIds(any()) } returns emptyList()
            every { commandChildConsentPort.deleteAllByChildIds(any()) } returns Unit
            every { commandChildPort.deleteAllByGuardianId(UserId(guardianIdStr)) } returns Unit
            every { commandUserPort.delete(UserId(guardianIdStr)) } returns Unit
            every { refreshTokenPort.delete(guardianIdStr) } returns Unit
            every { accessTokenBlacklister.blacklist(null) } returns Unit

            service.execute(WithdrawGuardianCommand(userId = guardianIdStr, accessToken = null))

            verifyOrder {
                commandChildConsentPort.deleteAllByChildIds(children.map { it.childId })
                commandChildPort.deleteAllByGuardianId(UserId(guardianIdStr))
                commandUserPort.delete(UserId(guardianIdStr))
            }
            verify(exactly = 1) { commandChildConsentPort.deleteAllByChildIds(any()) }
            verify(exactly = 1) { commandChildPort.deleteAllByGuardianId(UserId(guardianIdStr)) }
            verify(exactly = 1) { commandUserPort.delete(UserId(guardianIdStr)) }
        }

        test("아이가 없으면 동의 삭제를 호출하지 않고 아이·유저만 삭제한다") {
            every { loadUserPort.findByUserId(UserId(guardianIdStr)) } returns kakaoGuardian()
            every { loadChildPort.findAllByGuardianId(UserId(guardianIdStr)) } returns emptyList()
            every { commandChildPort.deleteAllByGuardianId(UserId(guardianIdStr)) } returns Unit
            every { commandUserPort.delete(UserId(guardianIdStr)) } returns Unit
            every { refreshTokenPort.delete(guardianIdStr) } returns Unit
            every { accessTokenBlacklister.blacklist(null) } returns Unit

            service.execute(WithdrawGuardianCommand(userId = guardianIdStr, accessToken = null))

            verify(exactly = 0) { commandChildConsentPort.deleteAllByChildIds(any()) }
            verify(exactly = 1) { commandChildPort.deleteAllByGuardianId(UserId(guardianIdStr)) }
            verify(exactly = 1) { commandUserPort.delete(UserId(guardianIdStr)) }
        }

        test("유저가 존재하지 않으면 NOT_FOUND_USER를 던지고 아무것도 삭제하지 않는다") {
            every { loadUserPort.findByUserId(UserId(guardianIdStr)) } returns null

            val exception = shouldThrow<NotFoundException> {
                service.execute(WithdrawGuardianCommand(userId = guardianIdStr, accessToken = providedAccess))
            }

            exception.exceptionResponseCode shouldBe NOT_FOUND_USER
            verify(exactly = 0) { commandChildConsentPort.deleteAllByChildIds(any()) }
            verify(exactly = 0) { commandChildPort.deleteAllByGuardianId(any()) }
            verify(exactly = 0) { commandUserPort.delete(any()) }
        }
    }

    context("세션 계열 하드 삭제 조율") {
        val children = listOf(child("child-1"), child("child-2"))
        val childIds = children.map { it.childId }
        val sessionIds = listOf("session-1", "session-2")
        val messageIds = listOf("message-1", "message-2")

        test("발화분석→메시지→리포트→후속활동→세션 순으로 leaf→root 삭제하고 세션 계열이 동의·아이보다 먼저 지워진다") {
            every { loadUserPort.findByUserId(UserId(guardianIdStr)) } returns kakaoGuardian()
            every { loadChildPort.findAllByGuardianId(UserId(guardianIdStr)) } returns children
            every { loadSpeakingSessionPort.findSessionIdsByChildIds(childIds) } returns sessionIds
            every { loadPostActivityResultPort.findRetellingAudioUrlsBySessionIds(sessionIds) } returns emptyList()
            every { loadMessagePort.findMessageIdsBySessionIds(sessionIds) } returns messageIds
            every { commandUtteranceAnalysisPort.deleteAllByMessageIds(messageIds) } returns Unit
            every { commandMessagePort.deleteAllBySessionIds(sessionIds) } returns Unit
            every { commandReportPort.deleteAllBySessionIds(sessionIds) } returns Unit
            every { commandPostActivityResultPort.deleteAllBySessionIds(sessionIds) } returns Unit
            every { commandSpeakingSessionPort.deleteAllByChildIds(childIds) } returns Unit
            every { commandChildConsentPort.deleteAllByChildIds(childIds) } returns Unit
            every { commandChildPort.deleteAllByGuardianId(UserId(guardianIdStr)) } returns Unit
            every { commandUserPort.delete(UserId(guardianIdStr)) } returns Unit
            every { refreshTokenPort.delete(guardianIdStr) } returns Unit
            every { accessTokenBlacklister.blacklist(null) } returns Unit

            service.execute(WithdrawGuardianCommand(userId = guardianIdStr, accessToken = null))

            verifyOrder {
                commandUtteranceAnalysisPort.deleteAllByMessageIds(messageIds)
                commandMessagePort.deleteAllBySessionIds(sessionIds)
                commandReportPort.deleteAllBySessionIds(sessionIds)
                commandPostActivityResultPort.deleteAllBySessionIds(sessionIds)
                commandSpeakingSessionPort.deleteAllByChildIds(childIds)
                commandChildConsentPort.deleteAllByChildIds(childIds)
                commandChildPort.deleteAllByGuardianId(UserId(guardianIdStr))
                commandUserPort.delete(UserId(guardianIdStr))
            }
        }

        test("세션이 없으면 세션 계열 삭제를 전혀 호출하지 않고 메시지 조회도 하지 않는다") {
            every { loadUserPort.findByUserId(UserId(guardianIdStr)) } returns kakaoGuardian()
            every { loadChildPort.findAllByGuardianId(UserId(guardianIdStr)) } returns children
            every { loadSpeakingSessionPort.findSessionIdsByChildIds(childIds) } returns emptyList()
            every { commandChildConsentPort.deleteAllByChildIds(childIds) } returns Unit
            every { commandChildPort.deleteAllByGuardianId(UserId(guardianIdStr)) } returns Unit
            every { commandUserPort.delete(UserId(guardianIdStr)) } returns Unit
            every { refreshTokenPort.delete(guardianIdStr) } returns Unit
            every { accessTokenBlacklister.blacklist(null) } returns Unit

            service.execute(WithdrawGuardianCommand(userId = guardianIdStr, accessToken = null))

            verify(exactly = 0) { loadMessagePort.findMessageIdsBySessionIds(any()) }
            verify(exactly = 0) { commandUtteranceAnalysisPort.deleteAllByMessageIds(any()) }
            verify(exactly = 0) { commandMessagePort.deleteAllBySessionIds(any()) }
            verify(exactly = 0) { commandReportPort.deleteAllBySessionIds(any()) }
            verify(exactly = 0) { commandPostActivityResultPort.deleteAllBySessionIds(any()) }
            verify(exactly = 0) { commandSpeakingSessionPort.deleteAllByChildIds(any()) }
        }

        test("메시지가 없으면 발화분석 삭제는 건너뛰고 나머지 세션 계열은 삭제한다") {
            every { loadUserPort.findByUserId(UserId(guardianIdStr)) } returns kakaoGuardian()
            every { loadChildPort.findAllByGuardianId(UserId(guardianIdStr)) } returns children
            every { loadSpeakingSessionPort.findSessionIdsByChildIds(childIds) } returns sessionIds
            every { loadPostActivityResultPort.findRetellingAudioUrlsBySessionIds(sessionIds) } returns emptyList()
            every { loadMessagePort.findMessageIdsBySessionIds(sessionIds) } returns emptyList()
            every { commandMessagePort.deleteAllBySessionIds(sessionIds) } returns Unit
            every { commandReportPort.deleteAllBySessionIds(sessionIds) } returns Unit
            every { commandPostActivityResultPort.deleteAllBySessionIds(sessionIds) } returns Unit
            every { commandSpeakingSessionPort.deleteAllByChildIds(childIds) } returns Unit
            every { commandChildConsentPort.deleteAllByChildIds(childIds) } returns Unit
            every { commandChildPort.deleteAllByGuardianId(UserId(guardianIdStr)) } returns Unit
            every { commandUserPort.delete(UserId(guardianIdStr)) } returns Unit
            every { refreshTokenPort.delete(guardianIdStr) } returns Unit
            every { accessTokenBlacklister.blacklist(null) } returns Unit

            service.execute(WithdrawGuardianCommand(userId = guardianIdStr, accessToken = null))

            verify(exactly = 0) { commandUtteranceAnalysisPort.deleteAllByMessageIds(any()) }
            verify(exactly = 1) { commandMessagePort.deleteAllBySessionIds(sessionIds) }
            verify(exactly = 1) { commandSpeakingSessionPort.deleteAllByChildIds(childIds) }
        }

        test("아이가 없으면 세션 조회조차 하지 않는다") {
            every { loadUserPort.findByUserId(UserId(guardianIdStr)) } returns kakaoGuardian()
            every { loadChildPort.findAllByGuardianId(UserId(guardianIdStr)) } returns emptyList()
            every { commandChildPort.deleteAllByGuardianId(UserId(guardianIdStr)) } returns Unit
            every { commandUserPort.delete(UserId(guardianIdStr)) } returns Unit
            every { refreshTokenPort.delete(guardianIdStr) } returns Unit
            every { accessTokenBlacklister.blacklist(null) } returns Unit

            service.execute(WithdrawGuardianCommand(userId = guardianIdStr, accessToken = null))

            verify(exactly = 0) { loadSpeakingSessionPort.findSessionIdsByChildIds(any()) }
            verify(exactly = 0) { commandSpeakingSessionPort.deleteAllByChildIds(any()) }
        }
    }

    context("커밋 이후 Redis 정리") {
        val userId = UserId(guardianIdStr)

        test("refresh 삭제 후 accessTokenBlacklister.blacklist에 위임한다") {
            every { refreshTokenPort.delete(guardianIdStr) } returns Unit
            every { accessTokenBlacklister.blacklist(providedAccess) } returns Unit

            service.cleanupSessionState(userId, providedAccess)

            verifyOrder {
                refreshTokenPort.delete(guardianIdStr)
                accessTokenBlacklister.blacklist(providedAccess)
            }
        }

        test("accessToken이 null이면 blacklist(null)에 위임한다") {
            every { refreshTokenPort.delete(guardianIdStr) } returns Unit
            every { accessTokenBlacklister.blacklist(null) } returns Unit

            service.cleanupSessionState(userId, null)

            verify(exactly = 1) { refreshTokenPort.delete(guardianIdStr) }
            verify(exactly = 1) { accessTokenBlacklister.blacklist(null) }
        }
    }

    context("커밋 이후 재구성 음성 파일 파기") {
        val children = listOf(child("child-1"), child("child-2"))
        val childIds = children.map { it.childId }
        val sessionIds = listOf("session-1", "session-2")
        val audioUrls = listOf("/files/retelling-1.m4a", "/files/retelling-2.webm")

        test("탈퇴 시 수집한 재구성 음성 URL마다 storeFilePort.deleteFile을 호출한다") {
            every { loadUserPort.findByUserId(UserId(guardianIdStr)) } returns kakaoGuardian()
            every { loadChildPort.findAllByGuardianId(UserId(guardianIdStr)) } returns children
            every { loadSpeakingSessionPort.findSessionIdsByChildIds(childIds) } returns sessionIds
            every { loadPostActivityResultPort.findRetellingAudioUrlsBySessionIds(sessionIds) } returns audioUrls
            every { loadMessagePort.findMessageIdsBySessionIds(sessionIds) } returns emptyList()
            every { commandMessagePort.deleteAllBySessionIds(sessionIds) } returns Unit
            every { commandReportPort.deleteAllBySessionIds(sessionIds) } returns Unit
            every { commandPostActivityResultPort.deleteAllBySessionIds(sessionIds) } returns Unit
            every { commandSpeakingSessionPort.deleteAllByChildIds(childIds) } returns Unit
            every { commandChildConsentPort.deleteAllByChildIds(childIds) } returns Unit
            every { commandChildPort.deleteAllByGuardianId(UserId(guardianIdStr)) } returns Unit
            every { commandUserPort.delete(UserId(guardianIdStr)) } returns Unit
            every { refreshTokenPort.delete(guardianIdStr) } returns Unit
            every { accessTokenBlacklister.blacklist(null) } returns Unit
            every { storeFilePort.deleteFile(any()) } returns Unit

            service.execute(WithdrawGuardianCommand(userId = guardianIdStr, accessToken = null))

            verify(exactly = 1) { storeFilePort.deleteFile("/files/retelling-1.m4a") }
            verify(exactly = 1) { storeFilePort.deleteFile("/files/retelling-2.webm") }
        }

        test("수집된 재구성 음성 URL이 없으면 deleteFile을 호출하지 않는다") {
            every { loadUserPort.findByUserId(UserId(guardianIdStr)) } returns kakaoGuardian()
            every { loadChildPort.findAllByGuardianId(UserId(guardianIdStr)) } returns children
            every { loadSpeakingSessionPort.findSessionIdsByChildIds(childIds) } returns sessionIds
            every { loadPostActivityResultPort.findRetellingAudioUrlsBySessionIds(sessionIds) } returns emptyList()
            every { loadMessagePort.findMessageIdsBySessionIds(sessionIds) } returns emptyList()
            every { commandMessagePort.deleteAllBySessionIds(sessionIds) } returns Unit
            every { commandReportPort.deleteAllBySessionIds(sessionIds) } returns Unit
            every { commandPostActivityResultPort.deleteAllBySessionIds(sessionIds) } returns Unit
            every { commandSpeakingSessionPort.deleteAllByChildIds(childIds) } returns Unit
            every { commandChildConsentPort.deleteAllByChildIds(childIds) } returns Unit
            every { commandChildPort.deleteAllByGuardianId(UserId(guardianIdStr)) } returns Unit
            every { commandUserPort.delete(UserId(guardianIdStr)) } returns Unit
            every { refreshTokenPort.delete(guardianIdStr) } returns Unit
            every { accessTokenBlacklister.blacklist(null) } returns Unit

            service.execute(WithdrawGuardianCommand(userId = guardianIdStr, accessToken = null))

            verify(exactly = 0) { storeFilePort.deleteFile(any()) }
        }

        test("deleteRetellingAudioFiles는 유효 URL마다 순서대로 deleteFile에 위임한다") {
            every { storeFilePort.deleteFile(any()) } returns Unit

            service.deleteRetellingAudioFiles(audioUrls)

            verifyOrder {
                storeFilePort.deleteFile("/files/retelling-1.m4a")
                storeFilePort.deleteFile("/files/retelling-2.webm")
            }
        }
    }
})
