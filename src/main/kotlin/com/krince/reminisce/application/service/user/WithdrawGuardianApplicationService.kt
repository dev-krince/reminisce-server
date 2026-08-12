package com.krince.reminisce.application.service.user

import com.krince.reminisce.application.port.`in`.user.command.WithdrawGuardianCommand
import com.krince.reminisce.application.port.`in`.user.usecase.WithdrawGuardianUseCase
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
import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.user.User
import com.krince.reminisce.domain.model.user.vo.UserId
import com.krince.reminisce.shared.exception.NotFoundException
import com.krince.reminisce.shared.response.ExceptionResponseCode.NOT_FOUND_USER
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager

@Service
class WithdrawGuardianApplicationService(
    private val loadUserPort: LoadUserPort,
    private val loadChildPort: LoadChildPort,
    private val loadSpeakingSessionPort: LoadSpeakingSessionPort,
    private val loadMessagePort: LoadMessagePort,
    private val loadPostActivityResultPort: LoadPostActivityResultPort,
    private val commandChildConsentPort: CommandChildConsentPort,
    private val commandChildPort: CommandChildPort,
    private val commandUserPort: CommandUserPort,
    private val commandSpeakingSessionPort: CommandSpeakingSessionPort,
    private val commandMessagePort: CommandMessagePort,
    private val commandReportPort: CommandReportPort,
    private val commandPostActivityResultPort: CommandPostActivityResultPort,
    private val commandUtteranceAnalysisPort: CommandUtteranceAnalysisPort,
    private val storeFilePort: StoreFilePort,
    private val refreshTokenPort: RefreshTokenPort,
    private val accessTokenBlacklister: AccessTokenBlacklister,
) : WithdrawGuardianUseCase {

    @Transactional
    override fun execute(command: WithdrawGuardianCommand) {
        val userId = UserId(command.userId)
        val user: User = loadUserPort.findByUserId(userId)
            ?: throw NotFoundException(NOT_FOUND_USER, NOT_FOUND_USER.message)

        val retellingAudioUrls: List<String> = purgeChildData(userId)
        commandUserPort.delete(userId)

        registerAfterCommitCleanup(user.userId, command.accessToken, retellingAudioUrls)
    }

    private fun purgeChildData(guardianId: UserId): List<String> {
        val children: List<Child> = loadChildPort.findAllByGuardianId(guardianId)
        val childIds: List<ChildId> = children.map { it.childId }
        if (childIds.isEmpty()) {
            commandChildPort.deleteAllByGuardianId(guardianId)
            return emptyList()
        }
        val retellingAudioUrls: List<String> = purgeSessionData(childIds)
        commandChildConsentPort.deleteAllByChildIds(childIds)
        commandChildPort.deleteAllByGuardianId(guardianId)

        return retellingAudioUrls
    }

    private fun purgeSessionData(childIds: List<ChildId>): List<String> {
        val sessionIds: List<String> = loadSpeakingSessionPort.findSessionIdsByChildIds(childIds)
        if (sessionIds.isEmpty()) {
            return emptyList()
        }
        val retellingAudioUrls: List<String> = loadPostActivityResultPort.findRetellingAudioUrlsBySessionIds(sessionIds)
        val messageIds: List<String> = loadMessagePort.findMessageIdsBySessionIds(sessionIds)
        if (messageIds.isNotEmpty()) {
            commandUtteranceAnalysisPort.deleteAllByMessageIds(messageIds)
        }
        commandMessagePort.deleteAllBySessionIds(sessionIds)
        commandReportPort.deleteAllBySessionIds(sessionIds)
        commandPostActivityResultPort.deleteAllBySessionIds(sessionIds)
        commandSpeakingSessionPort.deleteAllByChildIds(childIds)

        return retellingAudioUrls
    }

    private fun registerAfterCommitCleanup(
        userId: UserId,
        accessToken: String?,
        retellingAudioUrls: List<String>,
    ) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            cleanupSessionState(userId, accessToken)
            deleteRetellingAudioFiles(retellingAudioUrls)
            return
        }
        TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronization {
            override fun afterCommit() {
                cleanupSessionState(userId, accessToken)
                deleteRetellingAudioFiles(retellingAudioUrls)
            }
        })
    }

    internal fun cleanupSessionState(userId: UserId, accessToken: String?) {
        refreshTokenPort.delete(userId.value)
        accessTokenBlacklister.blacklist(accessToken)
    }

    internal fun deleteRetellingAudioFiles(retellingAudioUrls: List<String>) {
        retellingAudioUrls.forEach { storeFilePort.deleteFile(it) }
    }
}
