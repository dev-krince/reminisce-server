package com.krince.reminisce.application.service.childconsent

import com.krince.reminisce.application.port.access.child.ChildAccessPort
import com.krince.reminisce.application.port.`in`.childconsent.command.WithdrawChildConsentCommand
import com.krince.reminisce.application.port.`in`.childconsent.usecase.WithdrawChildConsentUseCase
import com.krince.reminisce.application.port.out.childconsent.CommandChildConsentPort
import com.krince.reminisce.application.port.out.childconsent.LoadChildConsentPort
import com.krince.reminisce.application.port.out.file.StoreFilePort
import com.krince.reminisce.application.service.child.ChildLearningDataPurger
import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.childconsent.ChildConsent
import com.krince.reminisce.domain.model.user.vo.UserId
import com.krince.reminisce.shared.exception.BusinessRuleViolationException
import com.krince.reminisce.shared.exception.NotFoundException
import com.krince.reminisce.shared.response.ExceptionResponseCode.BUSINESS_RULE_VIOLATION
import com.krince.reminisce.shared.response.ExceptionResponseCode.NOT_FOUND_CHILD
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager
import java.time.Clock
import java.time.LocalDateTime

@Service
class WithdrawChildConsentApplicationService(
    private val childAccessPort: ChildAccessPort,
    private val loadChildConsentPort: LoadChildConsentPort,
    private val commandChildConsentPort: CommandChildConsentPort,
    private val childLearningDataPurger: ChildLearningDataPurger,
    private val storeFilePort: StoreFilePort,
    private val clock: Clock,
) : WithdrawChildConsentUseCase {

    @Transactional
    override fun execute(command: WithdrawChildConsentCommand) {
        val childId = ChildId(command.childId)
        verifyOwnership(childId, UserId(command.guardianId))

        val activeConsent: ChildConsent = loadChildConsentPort.findActiveByChildId(childId)
            ?: throw BusinessRuleViolationException(BUSINESS_RULE_VIOLATION, BUSINESS_RULE_VIOLATION.message)

        commandChildConsentPort.save(activeConsent.withdraw(LocalDateTime.now(clock)))

        val retellingAudioUrls: List<String> = childLearningDataPurger.purge(listOf(childId))
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
