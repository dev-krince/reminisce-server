package com.krince.reminisce.application.service.user

import com.krince.reminisce.application.port.`in`.user.command.WithdrawGuardianCommand
import com.krince.reminisce.application.port.out.auth.AccessTokenBlacklistPort
import com.krince.reminisce.application.port.out.auth.RefreshTokenPort
import com.krince.reminisce.application.port.out.auth.TokenProviderPort
import com.krince.reminisce.application.port.out.child.CommandChildPort
import com.krince.reminisce.application.port.out.child.LoadChildPort
import com.krince.reminisce.application.port.out.childconsent.CommandChildConsentPort
import com.krince.reminisce.application.port.out.email.EmailVerificationPort
import com.krince.reminisce.application.port.out.user.CommandUserPort
import com.krince.reminisce.application.port.out.user.LoadUserPort
import com.krince.reminisce.domain.model.child.Child
import com.krince.reminisce.domain.model.child.vo.BirthYear
import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.child.vo.ChildNickname
import com.krince.reminisce.domain.model.user.User
import com.krince.reminisce.domain.model.user.vo.AuthProvider
import com.krince.reminisce.domain.model.user.vo.Email
import com.krince.reminisce.domain.model.user.vo.Nickname
import com.krince.reminisce.domain.model.user.vo.Password
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
import java.time.Duration

@Tags("test", "unitTest")
@DisplayName("WithdrawGuardianApplicationService 단위테스트")
class WithdrawGuardianApplicationServiceTest : FunSpec({

    val loadUserPort = mockk<LoadUserPort>()
    val loadChildPort = mockk<LoadChildPort>()
    val commandChildConsentPort = mockk<CommandChildConsentPort>()
    val commandChildPort = mockk<CommandChildPort>()
    val commandUserPort = mockk<CommandUserPort>()
    val refreshTokenPort = mockk<RefreshTokenPort>()
    val emailVerificationPort = mockk<EmailVerificationPort>()
    val accessTokenBlacklistPort = mockk<AccessTokenBlacklistPort>()
    val tokenProviderPort = mockk<TokenProviderPort>()
    val service = WithdrawGuardianApplicationService(
        loadUserPort = loadUserPort,
        loadChildPort = loadChildPort,
        commandChildConsentPort = commandChildConsentPort,
        commandChildPort = commandChildPort,
        commandUserPort = commandUserPort,
        refreshTokenPort = refreshTokenPort,
        emailVerificationPort = emailVerificationPort,
        accessTokenBlacklistPort = accessTokenBlacklistPort,
        tokenProviderPort = tokenProviderPort,
    )

    beforeEach { clearAllMocks() }

    val guardianIdStr = "guardian-uuid-1"
    val emailValue = "guardian@example.com"
    val providedAccess = "Bearer access-token"
    val extractedAccess = "access-token"
    val noPrefixAccess = "no-prefix"
    val accessJti = "access-jti-1"
    val accessRemaining = Duration.ofHours(2)

    fun providerOf(email: Email?): AuthProvider {
        email ?: return AuthProvider.KAKAO

        return AuthProvider.LOCAL
    }

    fun localGuardian(email: Email? = Email(emailValue)): User = User(
        userId = UserId(guardianIdStr),
        email = email,
        password = email?.let { Password("\$2a\$10\$hashedvalue") },
        nickname = Nickname("보호자"),
        provider = providerOf(email),
        role = Role.user(),
    )

    fun child(childIdStr: String): Child = Child(
        childId = ChildId(childIdStr),
        guardianId = UserId(guardianIdStr),
        nickname = ChildNickname("토토"),
        birthYear = BirthYear(2019),
    )

    context("하드 삭제 순서·조건") {
        test("동의→아이→유저 순으로 각 1회 삭제하고 유저 조회로 이메일을 확보한다") {
            val children = listOf(child("child-1"), child("child-2"))
            every { loadUserPort.findByUserId(UserId(guardianIdStr)) } returns localGuardian()
            every { loadChildPort.findAllByGuardianId(UserId(guardianIdStr)) } returns children
            every { commandChildConsentPort.deleteAllByChildIds(any()) } returns Unit
            every { commandChildPort.deleteAllByGuardianId(UserId(guardianIdStr)) } returns Unit
            every { commandUserPort.delete(UserId(guardianIdStr)) } returns Unit
            every { refreshTokenPort.delete(guardianIdStr) } returns Unit
            every { emailVerificationPort.deleteCode(emailValue) } returns Unit

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
            every { loadUserPort.findByUserId(UserId(guardianIdStr)) } returns localGuardian()
            every { loadChildPort.findAllByGuardianId(UserId(guardianIdStr)) } returns emptyList()
            every { commandChildPort.deleteAllByGuardianId(UserId(guardianIdStr)) } returns Unit
            every { commandUserPort.delete(UserId(guardianIdStr)) } returns Unit
            every { refreshTokenPort.delete(guardianIdStr) } returns Unit
            every { emailVerificationPort.deleteCode(emailValue) } returns Unit

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

    fun stubHardDelete(user: User) {
        every { loadUserPort.findByUserId(UserId(guardianIdStr)) } returns user
        every { loadChildPort.findAllByGuardianId(UserId(guardianIdStr)) } returns emptyList()
        every { commandChildPort.deleteAllByGuardianId(UserId(guardianIdStr)) } returns Unit
        every { commandUserPort.delete(UserId(guardianIdStr)) } returns Unit
    }

    fun withdraw(accessToken: String?) {
        service.execute(WithdrawGuardianCommand(userId = guardianIdStr, accessToken = accessToken))
    }

    context("커밋 이후 Redis 정리") {
        test("액세스가 유효하면 refresh 삭제·이메일코드 삭제 후 jti·남은 수명으로 블랙리스트에 등록한다") {
            stubHardDelete(localGuardian())
            every { refreshTokenPort.delete(guardianIdStr) } returns Unit
            every { emailVerificationPort.deleteCode(emailValue) } returns Unit
            every { tokenProviderPort.extractToken(providedAccess) } returns extractedAccess
            every { tokenProviderPort.getRemainingExpiration(extractedAccess) } returns accessRemaining
            every { tokenProviderPort.getTokenId(extractedAccess) } returns accessJti
            every { accessTokenBlacklistPort.register(accessJti, accessRemaining) } returns Unit

            withdraw(providedAccess)

            verifyOrder {
                refreshTokenPort.delete(guardianIdStr)
                emailVerificationPort.deleteCode(emailValue)
                accessTokenBlacklistPort.register(accessJti, accessRemaining)
            }
        }

        test("email이 null인 카카오 보호자는 이메일코드 삭제를 호출하지 않는다") {
            stubHardDelete(localGuardian(email = null))
            every { refreshTokenPort.delete(guardianIdStr) } returns Unit
            every { tokenProviderPort.extractToken(providedAccess) } returns extractedAccess
            every { tokenProviderPort.getRemainingExpiration(extractedAccess) } returns accessRemaining
            every { tokenProviderPort.getTokenId(extractedAccess) } returns accessJti
            every { accessTokenBlacklistPort.register(accessJti, accessRemaining) } returns Unit

            withdraw(providedAccess)

            verify(exactly = 1) { refreshTokenPort.delete(guardianIdStr) }
            verify(exactly = 0) { emailVerificationPort.deleteCode(any()) }
            verify(exactly = 1) { accessTokenBlacklistPort.register(accessJti, accessRemaining) }
        }

        test("액세스 헤더가 없으면 refresh만 삭제하고 블랙리스트에 등록하지 않는다") {
            stubHardDelete(localGuardian())
            every { refreshTokenPort.delete(guardianIdStr) } returns Unit
            every { emailVerificationPort.deleteCode(emailValue) } returns Unit

            withdraw(null)

            verify(exactly = 1) { refreshTokenPort.delete(guardianIdStr) }
            verify(exactly = 0) { accessTokenBlacklistPort.register(any(), any()) }
        }

        test("액세스가 유효하지만 jti가 없으면 예외 없이 블랙리스트에 등록하지 않는다") {
            stubHardDelete(localGuardian())
            every { refreshTokenPort.delete(guardianIdStr) } returns Unit
            every { emailVerificationPort.deleteCode(emailValue) } returns Unit
            every { tokenProviderPort.extractToken(providedAccess) } returns extractedAccess
            every { tokenProviderPort.getRemainingExpiration(extractedAccess) } returns accessRemaining
            every { tokenProviderPort.getTokenId(extractedAccess) } returns null

            withdraw(providedAccess)

            verify(exactly = 0) { accessTokenBlacklistPort.register(any(), any()) }
        }

        test("액세스가 만료·무효면 예외 없이 블랙리스트에 등록하지 않는다") {
            stubHardDelete(localGuardian())
            every { refreshTokenPort.delete(guardianIdStr) } returns Unit
            every { emailVerificationPort.deleteCode(emailValue) } returns Unit
            every { tokenProviderPort.extractToken(providedAccess) } returns extractedAccess
            every { tokenProviderPort.getRemainingExpiration(extractedAccess) } throws
                io.jsonwebtoken.ExpiredJwtException(null, null, "expired")

            withdraw(providedAccess)

            verify(exactly = 0) { accessTokenBlacklistPort.register(any(), any()) }
        }

        test("액세스 접두어가 잘못되면 예외 없이 블랙리스트에 등록하지 않는다") {
            stubHardDelete(localGuardian())
            every { refreshTokenPort.delete(guardianIdStr) } returns Unit
            every { emailVerificationPort.deleteCode(emailValue) } returns Unit
            every { tokenProviderPort.extractToken(noPrefixAccess) } throws IllegalArgumentException("bad prefix")

            withdraw(noPrefixAccess)

            verify(exactly = 0) { accessTokenBlacklistPort.register(any(), any()) }
        }
    }
})
