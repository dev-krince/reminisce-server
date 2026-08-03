package com.krince.reminisce.application.service

import com.krince.reminisce.application.facade.user.UserFacade
import com.krince.reminisce.application.port.out.auth.PasswordEncoderPort
import com.krince.reminisce.application.port.out.email.EmailVerificationPort
import com.krince.reminisce.application.port.out.email.MailSenderPort
import com.krince.reminisce.application.port.out.user.LoadUserPort
import com.krince.reminisce.application.port.`in`.user.command.ConfirmEmailVerificationCommand
import com.krince.reminisce.application.port.`in`.user.command.GetUserCommand
import com.krince.reminisce.application.port.`in`.user.command.SendEmailVerificationCommand
import com.krince.reminisce.application.port.`in`.user.command.SignUpCommand
import com.krince.reminisce.domain.model.user.User
import com.krince.reminisce.domain.model.user.vo.AuthProvider
import com.krince.reminisce.domain.model.user.vo.Email
import com.krince.reminisce.domain.model.user.vo.Nickname
import com.krince.reminisce.domain.model.user.vo.Password
import com.krince.reminisce.domain.model.user.vo.Role
import com.krince.reminisce.domain.model.user.vo.UserId
import com.krince.reminisce.shared.exception.BadRequestException
import com.krince.reminisce.shared.exception.ConflictException
import com.krince.reminisce.shared.exception.NotFoundException
import com.krince.reminisce.shared.response.ExceptionResponseCode.DUPLICATE_EMAIL
import com.krince.reminisce.shared.response.ExceptionResponseCode.EMAIL_NOT_VERIFIED
import com.krince.reminisce.shared.response.ExceptionResponseCode.INVALID_PASSWORD_FORMAT
import com.krince.reminisce.shared.response.ExceptionResponseCode.NOT_FOUND_USER
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.mockk.verifyOrder
import java.time.Duration
import java.time.LocalDateTime

@Tags("test", "unitTest")
@DisplayName("UserApplicationService 단위테스트")
class UserApplicationServiceTest : FunSpec({

    val facade = mockk<UserFacade>()
    val loadUserPort = mockk<LoadUserPort>()
    val emailVerificationPort = mockk<EmailVerificationPort>()
    val mailSenderPort = mockk<MailSenderPort>()
    val passwordEncoderPort = mockk<PasswordEncoderPort>()
    val service = UserApplicationService(
        facade = facade,
        loadUserPort = loadUserPort,
        emailVerificationPort = emailVerificationPort,
        mailSenderPort = mailSenderPort,
        passwordEncoderPort = passwordEncoderPort,
    )

    beforeEach { clearAllMocks() }

    val userIdStr = "user-uuid-1"
    val userId = UserId(userIdStr)
    val now = LocalDateTime.now()
    val user = User(
        userId = userId,
        email = Email("user@example.com"),
        password = Password("\$2a\$10\$hashedvalue"),
        nickname = Nickname("홍길동"),
        provider = AuthProvider.LOCAL,
        role = Role.user(),
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

    context("SendEmailVerificationUseCase") {
        context("성공") {
            test("중복이 아니면 코드를 저장하고 메일을 발송한다") {
                every { loadUserPort.existsByEmail(Email("user@example.com")) } returns false
                every { emailVerificationPort.saveCode("user@example.com", any(), any()) } returns Unit
                every { mailSenderPort.sendVerificationCode("user@example.com", any()) } returns Unit

                service.execute(SendEmailVerificationCommand("user@example.com"))

                val codeSlot = slot<String>()
                val ttlSlot = slot<Duration>()
                verify(exactly = 1) {
                    emailVerificationPort.saveCode("user@example.com", capture(codeSlot), capture(ttlSlot))
                }
                verify(exactly = 1) { mailSenderPort.sendVerificationCode("user@example.com", codeSlot.captured) }
                ttlSlot.captured shouldBe Duration.ofMinutes(5)
            }
        }
        context("실패") {
            test("이미 가입된 이메일이면 DUPLICATE_EMAIL을 던지고 발송하지 않는다") {
                every { loadUserPort.existsByEmail(Email("user@example.com")) } returns true

                val exception = shouldThrow<ConflictException> {
                    service.execute(SendEmailVerificationCommand("user@example.com"))
                }

                exception.exceptionResponseCode shouldBe DUPLICATE_EMAIL
                verify(exactly = 0) { mailSenderPort.sendVerificationCode(any(), any()) }
            }
        }
    }

    context("ConfirmEmailVerificationUseCase") {
        context("성공") {
            test("코드가 일치하면 코드를 삭제하고 인증 상태로 표시한다 (삭제 후 표시 순서)") {
                every { emailVerificationPort.findCode("user@example.com") } returns "123456"
                every { emailVerificationPort.deleteCode("user@example.com") } returns Unit
                every { emailVerificationPort.markVerified("user@example.com") } returns Unit

                service.execute(ConfirmEmailVerificationCommand("user@example.com", "123456"))

                verifyOrder {
                    emailVerificationPort.deleteCode("user@example.com")
                    emailVerificationPort.markVerified("user@example.com")
                }
            }
        }
    }

    context("SignUpUseCase") {
        context("성공") {
            test("인증 완료 후 비밀번호를 암호화해 저장하고 원문은 저장하지 않는다") {
                every { emailVerificationPort.isVerified("user@example.com") } returns true
                every { passwordEncoderPort.encode("Password1!") } returns "\$2a\$10\$hashedvalue"
                val savedSlot = slot<User>()
                every { facade.persistNewUser(capture(savedSlot)) } returns user

                val result = service.execute(SignUpCommand("user@example.com", "Password1!", "홍길동"))

                result.userId shouldBe userIdStr
                result.email shouldBe "user@example.com"
                result.nickname shouldBe "홍길동"
                result.role shouldBe "ROLE_USER"
                savedSlot.captured.password.value shouldBe "\$2a\$10\$hashedvalue"
                (savedSlot.captured.password.value == "Password1!") shouldBe false
                verifyOrder {
                    emailVerificationPort.isVerified("user@example.com")
                    passwordEncoderPort.encode("Password1!")
                    facade.persistNewUser(any())
                }
            }
        }
        context("실패") {
            test("이메일 미인증이면 EMAIL_NOT_VERIFIED를 던지고 저장하지 않는다") {
                every { emailVerificationPort.isVerified("user@example.com") } returns false

                val exception = shouldThrow<BadRequestException> {
                    service.execute(SignUpCommand("user@example.com", "Password1!", "홍길동"))
                }

                exception.exceptionResponseCode shouldBe EMAIL_NOT_VERIFIED
                verify(exactly = 0) { facade.persistNewUser(any()) }
            }
            test("비밀번호 정책 위반이면 INVALID_PASSWORD_FORMAT을 던지고 저장하지 않는다") {
                every { emailVerificationPort.isVerified("user@example.com") } returns true

                val exception = shouldThrow<BadRequestException> {
                    service.execute(SignUpCommand("user@example.com", "weak", "홍길동"))
                }

                exception.exceptionResponseCode shouldBe INVALID_PASSWORD_FORMAT
                verify(exactly = 0) { facade.persistNewUser(any()) }
            }
            test("facade가 DUPLICATE_EMAIL을 던지면 그대로 전파된다") {
                every { emailVerificationPort.isVerified("user@example.com") } returns true
                every { passwordEncoderPort.encode("Password1!") } returns "\$2a\$10\$hashedvalue"
                every { facade.persistNewUser(any()) } throws
                    ConflictException(DUPLICATE_EMAIL, DUPLICATE_EMAIL.message)

                val exception = shouldThrow<ConflictException> {
                    service.execute(SignUpCommand("user@example.com", "Password1!", "홍길동"))
                }

                exception.exceptionResponseCode shouldBe DUPLICATE_EMAIL
            }
        }
    }
})
