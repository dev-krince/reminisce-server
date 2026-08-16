package com.krince.reminisce.application.service.report

import com.krince.reminisce.application.port.access.child.ChildAccessPort
import com.krince.reminisce.application.port.`in`.report.command.GetLatestSessionReportCommand
import com.krince.reminisce.application.port.`in`.report.command.GetSessionReportCommand
import com.krince.reminisce.application.port.`in`.report.result.LatestSessionReportResult
import com.krince.reminisce.application.port.`in`.report.usecase.GetLatestSessionReportUseCase
import com.krince.reminisce.application.port.`in`.report.usecase.GetSessionReportUseCase
import com.krince.reminisce.application.port.out.speakingsession.LoadSpeakingSessionPort
import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.speakingsession.SpeakingSession
import com.krince.reminisce.domain.model.user.vo.UserId
import com.krince.reminisce.shared.exception.NotFoundException
import com.krince.reminisce.shared.response.ExceptionResponseCode.NOT_FOUND
import com.krince.reminisce.shared.response.ExceptionResponseCode.NOT_FOUND_CHILD
import org.springframework.stereotype.Service

@Service
class GetLatestSessionReportApplicationService(
    private val childAccessPort: ChildAccessPort,
    private val loadSpeakingSessionPort: LoadSpeakingSessionPort,
    private val getSessionReportUseCase: GetSessionReportUseCase,
) : GetLatestSessionReportUseCase {

    override fun execute(command: GetLatestSessionReportCommand): LatestSessionReportResult {
        val childId = ChildId(command.childId)
        verifyOwnership(childId, UserId(command.guardianId))

        val latestCompleted: SpeakingSession = loadSpeakingSessionPort.findLatestCompletedByChild(childId)
            ?: throw NotFoundException(NOT_FOUND, NOT_FOUND.message)
        val report = getSessionReportUseCase.execute(
            GetSessionReportCommand(sessionId = latestCompleted.sessionId.value, guardianId = command.guardianId),
        )

        return LatestSessionReportResult(sessionId = latestCompleted.sessionId.value, report = report)
    }

    private fun verifyOwnership(childId: ChildId, guardianId: UserId) {
        val ownerId: UserId? = childAccessPort.findGuardianId(childId)
        if (ownerId == null || ownerId != guardianId) {
            throw NotFoundException(NOT_FOUND_CHILD, NOT_FOUND_CHILD.message)
        }
    }
}
