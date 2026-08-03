package com.krince.reminisce.application.facade.user

import com.krince.reminisce.application.port.access.user.UserAccessPort
import com.krince.reminisce.application.port.access.user.snapshot.UserSnapshot
import com.krince.reminisce.application.port.out.user.CommandUserPort
import com.krince.reminisce.application.port.out.user.LoadUserPort
import com.krince.reminisce.application.validator.user.SignUpValidator
import com.krince.reminisce.domain.model.user.User
import com.krince.reminisce.domain.model.user.vo.Email
import com.krince.reminisce.domain.model.user.vo.UserId
import com.krince.reminisce.shared.exception.ConflictException
import com.krince.reminisce.shared.exception.NotFoundException
import com.krince.reminisce.shared.response.ExceptionResponseCode.DUPLICATE_EMAIL
import com.krince.reminisce.shared.response.ExceptionResponseCode.NOT_FOUND_USER
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserFacade(
    private val loadPort: LoadUserPort,
    private val commandPort: CommandUserPort,
) : UserAccessPort {

    fun findById(userId: UserId): User = loadPort.findByUserId(userId)
        ?: throw NotFoundException(NOT_FOUND_USER, NOT_FOUND_USER.message)

    @Transactional
    fun persistNewUser(user: User): User {
        SignUpValidator.validateNotDuplicated(loadPort.existsByEmail(requireNotNull(user.email)))

        return runCatching { commandPort.save(user) }
            .getOrElse { throw resolvePersistFailure(it) }
    }

    private fun resolvePersistFailure(cause: Throwable): Throwable {
        if (cause is DataIntegrityViolationException) {
            return ConflictException(DUPLICATE_EMAIL, DUPLICATE_EMAIL.message)
        }

        return cause
    }

    override fun findByEmail(email: String): UserSnapshot {
        val user: User = loadPort.findByEmail(Email(email))
            ?: throw NotFoundException(NOT_FOUND_USER, NOT_FOUND_USER.message)

        return UserSnapshot.from(user)
    }

    override fun findByUserId(userId: UserId): UserSnapshot {
        val user: User = loadPort.findByUserId(userId)
            ?: throw NotFoundException(NOT_FOUND_USER, NOT_FOUND_USER.message)

        return UserSnapshot.from(user)
    }
}
