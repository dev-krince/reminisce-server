package com.krince.reminisce.application.service.user

import com.krince.reminisce.application.facade.user.UserFacade
import com.krince.reminisce.application.port.access.user.context.UserResult
import com.krince.reminisce.application.port.`in`.user.command.ConfirmEmailVerificationCommand
import com.krince.reminisce.application.port.`in`.user.command.GetUserCommand
import com.krince.reminisce.application.port.`in`.user.command.SendEmailVerificationCommand
import com.krince.reminisce.application.port.`in`.user.command.SignUpCommand
import com.krince.reminisce.application.port.`in`.user.usecase.ConfirmEmailVerificationUseCase
import com.krince.reminisce.application.port.`in`.user.usecase.GetUserUseCase
import com.krince.reminisce.application.port.`in`.user.usecase.SendEmailVerificationUseCase
import com.krince.reminisce.application.port.`in`.user.usecase.SignUpUseCase
import com.krince.reminisce.application.port.out.auth.PasswordEncoderPort
import com.krince.reminisce.application.port.out.email.EmailVerificationPort
import com.krince.reminisce.application.port.out.email.MailSenderPort
import com.krince.reminisce.application.port.out.user.LoadUserPort
import com.krince.reminisce.application.validator.user.ConfirmEmailVerificationValidator
import com.krince.reminisce.application.validator.user.SendEmailVerificationValidator
import com.krince.reminisce.application.validator.user.SignUpValidator
import com.krince.reminisce.domain.model.user.User
import com.krince.reminisce.domain.model.user.vo.Email
import com.krince.reminisce.domain.model.user.vo.Nickname
import com.krince.reminisce.domain.model.user.vo.Password
import com.krince.reminisce.domain.model.user.vo.UserId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.SecureRandom
import java.time.Duration

@Service
class UserApplicationService(
    private val facade: UserFacade,
    private val loadUserPort: LoadUserPort,
    private val emailVerificationPort: EmailVerificationPort,
    private val mailSenderPort: MailSenderPort,
    private val passwordEncoderPort: PasswordEncoderPort,
) : GetUserUseCase, SendEmailVerificationUseCase, ConfirmEmailVerificationUseCase, SignUpUseCase {

    @Transactional(readOnly = true)
    override fun execute(command: GetUserCommand): UserResult {
        val user: User = facade.findById(UserId(command.userId))

        return UserResult.from(user)
    }

    override fun execute(command: SendEmailVerificationCommand) {
        val email = Email(command.email)
        SendEmailVerificationValidator.validateNotDuplicated(loadUserPort.existsByEmail(email))

        val code: String = generateVerificationCode()
        emailVerificationPort.saveCode(command.email, code, VERIFICATION_CODE_TTL)
        mailSenderPort.sendVerificationCode(command.email, code)
    }

    override fun execute(command: ConfirmEmailVerificationCommand) {
        val storedCode: String? = emailVerificationPort.findCode(command.email)
        ConfirmEmailVerificationValidator.validateCodeMatches(storedCode, command.code)

        emailVerificationPort.deleteCode(command.email)
        emailVerificationPort.markVerified(command.email)
    }

    override fun execute(command: SignUpCommand): UserResult {
        val email = Email(command.email)
        SignUpValidator.validateVerified(emailVerificationPort.isVerified(command.email))
        SignUpValidator.validatePasswordFormat(command.password)

        val encodedPassword: String = passwordEncoderPort.encode(command.password)
        val user: User = User.signUp(
            email = email,
            password = Password(encodedPassword),
            nickname = Nickname(command.nickname),
        )
        val savedUser: User = facade.persistNewUser(user)

        return UserResult.from(savedUser)
    }

    private fun generateVerificationCode(): String {
        val bound: Int = VERIFICATION_CODE_BOUND - VERIFICATION_CODE_MIN
        val code: Int = VERIFICATION_CODE_MIN + SECURE_RANDOM.nextInt(bound)

        return code.toString()
    }

    companion object {
        private val SECURE_RANDOM: SecureRandom = SecureRandom()
        private val VERIFICATION_CODE_TTL: Duration = Duration.ofMinutes(5)
        private const val VERIFICATION_CODE_MIN = 100_000
        private const val VERIFICATION_CODE_BOUND = 1_000_000
    }
}
