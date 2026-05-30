package com.krince.boilerplate.application.facade.user

import com.krince.boilerplate.application.port.out.user.LoadUserPort
import com.krince.boilerplate.domain.model.user.User
import com.krince.boilerplate.domain.model.user.vo.LoginId
import com.krince.boilerplate.domain.model.user.vo.Role
import com.krince.boilerplate.domain.model.user.vo.UserId
import com.krince.boilerplate.shared.exception.NotFoundException
import com.krince.boilerplate.shared.response.ExceptionResponseCode.NOT_FOUND_USER
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
    val loginIdStr = "testUser"
    val now = LocalDateTime.now()
    val user = User(
        userId = userId,
        loginId = LoginId(loginIdStr),
        role = Role.user(),
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

                val ex = shouldThrow<NotFoundException> { facade.findById(userId) }

                ex.exceptionResponseCode shouldBe NOT_FOUND_USER
            }
        }
    }

    context("findByLoginId") {
        context("성공") {
            test("loadPort에서 User가 있으면 UserSnapshot을 반환한다") {
                clearMocks(loadPort)
                every { loadPort.findByLoginId(LoginId(loginIdStr)) } returns user

                val result = facade.findByLoginId(loginIdStr)

                result.userId shouldBe userId
                result.loginId shouldBe loginIdStr
                result.role shouldBe "ROLE_USER"
                verify(exactly = 1) { loadPort.findByLoginId(LoginId(loginIdStr)) }
            }
        }
        context("실패") {
            test("loadPort에서 null이면 NotFoundException을 던진다") {
                clearMocks(loadPort)
                every { loadPort.findByLoginId(LoginId(loginIdStr)) } returns null

                val ex = shouldThrow<NotFoundException> { facade.findByLoginId(loginIdStr) }

                ex.exceptionResponseCode shouldBe NOT_FOUND_USER
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
                result.loginId shouldBe loginIdStr
                verify(exactly = 1) { loadPort.findByUserId(userId) }
            }
        }
        context("실패") {
            test("loadPort에서 null이면 NotFoundException을 던진다") {
                clearMocks(loadPort)
                every { loadPort.findByUserId(userId) } returns null

                val ex = shouldThrow<NotFoundException> { facade.findByUserId(userId) }

                ex.exceptionResponseCode shouldBe NOT_FOUND_USER
            }
        }
    }
})
