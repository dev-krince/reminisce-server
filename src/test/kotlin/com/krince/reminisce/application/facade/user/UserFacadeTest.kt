package com.krince.reminisce.application.facade.user

import com.krince.reminisce.application.port.out.user.LoadUserPort
import com.krince.reminisce.domain.model.user.User
import com.krince.reminisce.domain.model.user.vo.AuthProvider
import com.krince.reminisce.domain.model.user.vo.Email
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
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalDateTime

@Tags("test", "unitTest")
@DisplayName("UserFacade 단위테스트")
class UserFacadeTest : FunSpec({

    val loadPort = mockk<LoadUserPort>()
    val facade = UserFacade(loadPort)

    val userId = UserId("user-uuid-1")
    val emailStr = "user@example.com"
    val now = LocalDateTime.now()
    val user = User(
        userId = userId,
        email = Email(emailStr),
        nickname = Nickname("홍길동"),
        provider = AuthProvider.KAKAO,
        role = Role.user(),
        providerId = "kakao-1",
        createdDate = now,
        modifiedDate = now,
    )

    context("findById") {
        context("성공") {
            test("loadPort에서 User가 있으면 그대로 반환한다") {
                clearMocks(loadPort)
                every { loadPort.findByUserId(userId) } returns user

                val result = facade.findById(userId)

                result shouldBe user
                verify(exactly = 1) { loadPort.findByUserId(userId) }
            }
        }
        context("실패") {
            test("loadPort에서 null이면 NotFoundException을 던진다") {
                clearMocks(loadPort)
                every { loadPort.findByUserId(userId) } returns null

                val exception = shouldThrow<NotFoundException> { facade.findById(userId) }

                exception.exceptionResponseCode shouldBe NOT_FOUND_USER
            }
        }
    }

    context("findByUserId") {
        context("성공") {
            test("loadPort에서 User가 있으면 UserSnapshot을 반환한다") {
                clearMocks(loadPort)
                every { loadPort.findByUserId(userId) } returns user

                val result = facade.findByUserId(userId)

                result.userId shouldBe userId
                result.email shouldBe emailStr
                verify(exactly = 1) { loadPort.findByUserId(userId) }
            }
        }
        context("실패") {
            test("loadPort에서 null이면 NotFoundException을 던진다") {
                clearMocks(loadPort)
                every { loadPort.findByUserId(userId) } returns null

                val exception = shouldThrow<NotFoundException> { facade.findByUserId(userId) }

                exception.exceptionResponseCode shouldBe NOT_FOUND_USER
            }
        }
    }
})
