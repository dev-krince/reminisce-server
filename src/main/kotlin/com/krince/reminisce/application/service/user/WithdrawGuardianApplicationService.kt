package com.krince.reminisce.application.service.user

import com.krince.reminisce.application.port.`in`.user.command.WithdrawGuardianCommand
import com.krince.reminisce.application.port.`in`.user.usecase.WithdrawGuardianUseCase
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
import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.user.User
import com.krince.reminisce.domain.model.user.vo.Email
import com.krince.reminisce.domain.model.user.vo.UserId
import com.krince.reminisce.shared.exception.NotFoundException
import com.krince.reminisce.shared.response.ExceptionResponseCode.NOT_FOUND_USER
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager
import java.time.Duration

@Service
class WithdrawGuardianApplicationService(
    private val loadUserPort: LoadUserPort,
    private val loadChildPort: LoadChildPort,
    private val commandChildConsentPort: CommandChildConsentPort,
    private val commandChildPort: CommandChildPort,
    private val commandUserPort: CommandUserPort,
    private val refreshTokenPort: RefreshTokenPort,
    private val emailVerificationPort: EmailVerificationPort,
    private val accessTokenBlacklistPort: AccessTokenBlacklistPort,
    private val tokenProviderPort: TokenProviderPort,
) : WithdrawGuardianUseCase {

    @Transactional
    override fun execute(command: WithdrawGuardianCommand) {
        val userId = UserId(command.userId)
        val user: User = loadUserPort.findByUserId(userId)
            ?: throw NotFoundException(NOT_FOUND_USER, NOT_FOUND_USER.message)

        purgeChildData(userId)
        commandUserPort.delete(userId)

        registerRedisCleanup(userId, user.email, command.accessToken)
    }

    private fun purgeChildData(guardianId: UserId) {
        val children: List<Child> = loadChildPort.findAllByGuardianId(guardianId)
        val childIds: List<ChildId> = children.map { it.childId }
        if (childIds.isNotEmpty()) {
            commandChildConsentPort.deleteAllByChildIds(childIds)
        }
        commandChildPort.deleteAllByGuardianId(guardianId)
    }

    private fun registerRedisCleanup(userId: UserId, email: Email?, accessToken: String?) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            cleanupSessionState(userId, email, accessToken)
            return
        }
        TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronization {
            override fun afterCommit() {
                cleanupSessionState(userId, email, accessToken)
            }
        })
    }

    private fun cleanupSessionState(userId: UserId, email: Email?, accessToken: String?) {
        refreshTokenPort.delete(userId.value)
        email?.let { emailVerificationPort.deleteCode(it.value) }
        blacklistAccessToken(accessToken)
    }

    private fun blacklistAccessToken(rawAccessToken: String?) {
        val extractedAccessToken: String = extractAccessToken(rawAccessToken) ?: return

        registerBlacklist(extractedAccessToken)
    }

    private fun extractAccessToken(rawAccessToken: String?): String? {
        val provided: String = rawAccessToken?.takeIf { it.isNotBlank() } ?: return null

        return try {
            tokenProviderPort.extractToken(provided)
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    private fun registerBlacklist(extractedAccessToken: String) {
        val remaining: Duration
        val tokenId: String
        try {
            remaining = tokenProviderPort.getRemainingExpiration(extractedAccessToken)
            if (remaining <= Duration.ZERO) return
            tokenId = tokenProviderPort.getTokenId(extractedAccessToken) ?: return
        } catch (_: RuntimeException) {
            return
        }

        accessTokenBlacklistPort.register(tokenId, remaining)
    }
}
