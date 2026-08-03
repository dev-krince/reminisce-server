package com.krince.reminisce.application.facade.user

import com.krince.reminisce.application.port.out.user.CommandUserPort
import com.krince.reminisce.application.port.out.user.LoadUserPort
import com.krince.reminisce.domain.model.user.User
import com.krince.reminisce.domain.model.user.vo.AuthProvider
import com.krince.reminisce.domain.model.user.vo.Email
import com.krince.reminisce.domain.model.user.vo.Nickname
import com.krince.reminisce.domain.model.user.vo.Password
import com.krince.reminisce.domain.model.user.vo.Role
import com.krince.reminisce.domain.model.user.vo.UserId
import com.krince.reminisce.shared.exception.ConflictException
import com.krince.reminisce.shared.exception.NotFoundException
import com.krince.reminisce.shared.response.ExceptionResponseCode.DUPLICATE_EMAIL
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
import org.springframework.dao.DataIntegrityViolationException
import java.time.LocalDateTime

@Tags("test", "unitTest")
@DisplayName("UserFacade 단위테스트")
class UserFacadeTest : FunSpec({

    val loadPort = mockk<LoadUserPort>()
    val commandPort = mockk<CommandUserPort>()
    val facade = UserFacade(loadPort, commandPort)

    val userId = UserId("user-uuid-1")
    val emailStr = "user@example.com"
    val now = LocalDateTime.now()
    val user = User(
        userId = userId,
        email = Email(emailStr),
        password = Password("\$2a\$10\$hashedvalue"),
        nickname = Nickname("홍길동"),
        provider = AuthProvider.LOCAL,
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

                val exception = shouldThrow<NotFoundException> { facade.findById(userId) }

                exception.exceptionResponseCode shouldBe NOT_FOUND_USER
            }
        }
    }

    context("persistNewUser") {
        context("성공") {
            test("중복이 아니면 commandPort로 저장하고 저장된 User를 반환한다") {
                clearMocks(loadPort, commandPort)
                every { loadPort.existsByEmail(Email(emailStr)) } returns false
                every { commandPort.save(user) } returns user

                val result = facade.persistNewUser(user)

                result shouldBe user
                verify(exactly = 1) { loadPort.existsByEmail(Email(emailStr)) }
                verify(exactly = 1) { commandPort.save(user) }
            }
        }
        context("실패") {
            test("이미 존재하는 이메일이면 DUPLICATE_EMAIL을 던지고 저장하지 않는다") {
                clearMocks(loadPort, commandPort)
                every { loadPort.existsByEmail(Email(emailStr)) } returns true

                val exception = shouldThrow<ConflictException> { facade.persistNewUser(user) }

                exception.exceptionResponseCode shouldBe DUPLICATE_EMAIL
                verify(exactly = 0) { commandPort.save(any()) }
            }
            test("동시 요청 경합으로 save가 유니크 제약을 위반하면 DUPLICATE_EMAIL로 매핑된다") {
                clearMocks(loadPort, commandPort)
                every { loadPort.existsByEmail(Email(emailStr)) } returns false
                every { commandPort.save(user) } throws DataIntegrityViolationException("unique violation")

                val exception = shouldThrow<ConflictException> { facade.persistNewUser(user) }

                exception.exceptionResponseCode shouldBe DUPLICATE_EMAIL
                verify(exactly = 1) { commandPort.save(user) }
            }
        }
    }

    context("findByEmail") {
        context("성공") {
            test("loadPort에서 User가 있으면 UserSnapshot을 반환한다") {
                clearMocks(loadPort)
                every { loadPort.findByEmail(Email(emailStr)) } returns user

                val result = facade.findByEmail(emailStr)

                result.userId shouldBe userId
                result.email shouldBe emailStr
                result.nickname shouldBe "홍길동"
                result.role shouldBe "ROLE_USER"
                verify(exactly = 1) { loadPort.findByEmail(Email(emailStr)) }
            }
        }
        context("실패") {
            test("loadPort에서 null이면 NotFoundException을 던진다") {
                clearMocks(loadPort)
                every { loadPort.findByEmail(Email(emailStr)) } returns null

                val exception = shouldThrow<NotFoundException> { facade.findByEmail(emailStr) }

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
