package com.krince.reminisce.application.service.speakingsession

import com.krince.reminisce.application.port.access.child.ChildAccessPort
import com.krince.reminisce.application.port.`in`.speakingsession.command.GetResumableSessionsCommand
import com.krince.reminisce.application.port.`in`.speakingsession.result.SpeakingSessionSummaryResult
import com.krince.reminisce.application.port.`in`.speakingsession.usecase.GetResumableSessionsUseCase
import com.krince.reminisce.application.port.out.speakingsession.LoadSpeakingSessionPort
import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.user.vo.UserId
import com.krince.reminisce.shared.exception.NotFoundException
import com.krince.reminisce.shared.response.ExceptionResponseCode.NOT_FOUND
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetResumableSessionsApplicationService(
    private val loadSpeakingSessionPort: LoadSpeakingSessionPort,
    private val childAccessPort: ChildAccessPort,
) : GetResumableSessionsUseCase {

    @Transactional(readOnly = true)
    override fun execute(command: GetResumableSessionsCommand): List<SpeakingSessionSummaryResult> {
        val childId = ChildId(command.childId)
        verifyOwnership(childId, UserId(command.guardianId))

        return loadSpeakingSessionPort.findInProgressByChild(childId)
            .map { SpeakingSessionSummaryResult.from(it) }
    }

    private fun verifyOwnership(childId: ChildId, guardianId: UserId) {
        val ownerId: UserId? = childAccessPort.findGuardianId(childId)
        if (ownerId == null || ownerId != guardianId) {
            throw NotFoundException(NOT_FOUND, NOT_FOUND.message)
        }
    }
}
