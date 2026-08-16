package com.krince.reminisce.application.service.child

import com.krince.reminisce.application.port.access.child.ChildAccessPort
import com.krince.reminisce.application.port.`in`.child.command.DeleteChildCommand
import com.krince.reminisce.application.port.`in`.child.usecase.DeleteChildUseCase
import com.krince.reminisce.application.port.out.child.CommandChildPort
import com.krince.reminisce.application.port.out.childconsent.CommandChildConsentPort
import com.krince.reminisce.application.port.out.file.StoreFilePort
import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.user.vo.UserId
import com.krince.reminisce.shared.exception.NotFoundException
import com.krince.reminisce.shared.response.ExceptionResponseCode.NOT_FOUND_CHILD
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager

@Service
class DeleteChildApplicationService(
    private val childAccessPort: ChildAccessPort,
    private val childLearningDataPurger: ChildLearningDataPurger,
    private val commandChildConsentPort: CommandChildConsentPort,
    private val commandChildPort: CommandChildPort,
    private val storeFilePort: StoreFilePort,
) : DeleteChildUseCase {

    @Transactional
    override fun execute(command: DeleteChildCommand) {
        val childId = ChildId(command.childId)
        verifyOwnership(childId, UserId(command.guardianId))

        val retellingAudioUrls: List<String> = childLearningDataPurger.purge(listOf(childId))
        commandChildConsentPort.deleteAllByChildIds(listOf(childId))
        commandChildPort.deleteById(childId)
        registerAfterCommitFileCleanup(retellingAudioUrls)
    }

    private fun verifyOwnership(childId: ChildId, guardianId: UserId) {
        val ownerId: UserId? = childAccessPort.findGuardianId(childId)
        if (ownerId == null || ownerId != guardianId) {
            throw NotFoundException(NOT_FOUND_CHILD, NOT_FOUND_CHILD.message)
        }
    }

    private fun registerAfterCommitFileCleanup(retellingAudioUrls: List<String>) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            deleteRetellingAudioFiles(retellingAudioUrls)
            return
        }
        TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronization {
            override fun afterCommit() {
                deleteRetellingAudioFiles(retellingAudioUrls)
            }
        })
    }

    internal fun deleteRetellingAudioFiles(retellingAudioUrls: List<String>) {
        retellingAudioUrls.forEach { storeFilePort.deleteFile(it) }
    }
}
