package com.krince.reminisce.application.service.user

import com.krince.reminisce.application.facade.user.UserFacade
import com.krince.reminisce.application.port.`in`.user.command.GetUserCommand
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
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalDateTime

@Tags("test", "unitTest")
@DisplayName("UserApplicationService 단위테스트")
class UserApplicationServiceTest : FunSpec({

    val facade = mockk<UserFacade>()
    val service = UserApplicationService(facade = facade)

    beforeEach { clearAllMocks() }

    val userIdStr = "user-uuid-1"
    val userId = UserId(userIdStr)
    val now = LocalDateTime.now()
    val user = User(
        userId = userId,
        email = Email("user@example.com"),
        nickname = Nickname("홍길동"),
        provider = AuthProvider.KAKAO,
        role = Role.user(),
        providerId = "kakao-1",
        createdDate = now,
        modifiedDate = now,
    )

    context("GetUserUseCase") {
        context("성공") {
            test("facade.findById로 User를 조회해 UserResult로 반환한다") {
                every { facade.findById(userId) } returns user

                val result = service.execute(GetUserCommand(userIdStr))

                result.userId shouldBe userIdStr
                result.email shouldBe "user@example.com"
                result.nickname shouldBe "홍길동"
                result.role shouldBe "ROLE_USER"
                verify(exactly = 1) { facade.findById(userId) }
            }
        }
        context("실패") {
            test("facade에서 NotFoundException이 나면 그대로 전파된다") {
                every { facade.findById(userId) } throws NotFoundException(NOT_FOUND_USER, NOT_FOUND_USER.message)

                shouldThrow<NotFoundException> { service.execute(GetUserCommand(userIdStr)) }
            }
        }
    }
})
