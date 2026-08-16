package com.krince.reminisce.application.service.speakingsession

import com.krince.reminisce.application.port.access.child.ChildAccessPort
import com.krince.reminisce.application.port.`in`.speakingsession.command.DeleteSpeakingSessionCommand
import com.krince.reminisce.application.port.`in`.speakingsession.usecase.DeleteSpeakingSessionUseCase
import com.krince.reminisce.application.port.out.file.StoreFilePort
import com.krince.reminisce.application.port.out.speakingsession.CommandSpeakingSessionPort
import com.krince.reminisce.application.port.out.speakingsession.LoadSpeakingSessionPort
import com.krince.reminisce.domain.model.speakingsession.SpeakingSession
import com.krince.reminisce.domain.model.speakingsession.vo.SpeakingSessionId
import com.krince.reminisce.domain.model.user.vo.UserId
import com.krince.reminisce.shared.exception.NotFoundException
import com.krince.reminisce.shared.response.ExceptionResponseCode.NOT_FOUND
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager

@Service
class DeleteSpeakingSessionApplicationService(
    private val loadSpeakingSessionPort: LoadSpeakingSessionPort,
    private val commandSpeakingSessionPort: CommandSpeakingSessionPort,
    private val childAccessPort: ChildAccessPort,
    private val sessionCascadePurger: SpeakingSessionCascadePurger,
    private val storeFilePort: StoreFilePort,
) : DeleteSpeakingSessionUseCase {

    @Transactional
    override fun execute(command: DeleteSpeakingSessionCommand) {
        val session: SpeakingSession = loadOwnedSession(command.sessionId, command.guardianId)

        val audioUrls: List<String> = sessionCascadePurger.purgeBySessionIds(listOf(session.sessionId.value))
        commandSpeakingSessionPort.deleteById(session.sessionId)
        registerAfterCommitFileCleanup(audioUrls)
    }

    private fun loadOwnedSession(sessionId: String, guardianId: String): SpeakingSession {
        val session: SpeakingSession = loadSpeakingSessionPort.findById(SpeakingSessionId(sessionId))
            ?: throw NotFoundException(NOT_FOUND, NOT_FOUND.message)
        verifyOwnership(session, UserId(guardianId))

        return session
    }

    private fun verifyOwnership(session: SpeakingSession, guardianId: UserId) {
        val ownerId: UserId? = childAccessPort.findGuardianId(session.childId)
        if (ownerId == null || ownerId != guardianId) {
            throw NotFoundException(NOT_FOUND, NOT_FOUND.message)
        }
    }

    private fun registerAfterCommitFileCleanup(audioUrls: List<String>) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            deleteAudioFiles(audioUrls)
            return
        }
        TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronization {
            override fun afterCommit() {
                deleteAudioFiles(audioUrls)
            }
        })
    }

    internal fun deleteAudioFiles(audioUrls: List<String>) {
        audioUrls.forEach { storeFilePort.deleteFile(it) }
    }
}
