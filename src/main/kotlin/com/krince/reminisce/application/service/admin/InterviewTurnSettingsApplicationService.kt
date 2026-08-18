package com.krince.reminisce.application.service.admin

import com.krince.reminisce.application.port.`in`.admin.command.UpdateInterviewStageTurnsCommand
import com.krince.reminisce.application.port.`in`.admin.result.InterviewStageTurnsResult
import com.krince.reminisce.application.port.`in`.admin.usecase.GetInterviewStageTurnsUseCase
import com.krince.reminisce.application.port.`in`.admin.usecase.UpdateInterviewStageTurnsUseCase
import com.krince.reminisce.application.port.out.profileinterview.InterviewTurnSettingsPort
import com.krince.reminisce.domain.model.profileinterview.ProfileInterview
import com.krince.reminisce.domain.model.profileinterview.vo.InterviewStage
import com.krince.reminisce.shared.exception.BadRequestException
import com.krince.reminisce.shared.response.ExceptionResponseCode.INVALID_DTO_PARAMETER
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class InterviewTurnSettingsApplicationService(
    private val interviewTurnSettingsPort: InterviewTurnSettingsPort,
) : GetInterviewStageTurnsUseCase, UpdateInterviewStageTurnsUseCase {

    @Transactional(readOnly = true)
    override fun execute(): InterviewStageTurnsResult = resultFrom(interviewTurnSettingsPort.load())

    @Transactional
    override fun execute(command: UpdateInterviewStageTurnsCommand): InterviewStageTurnsResult {
        AdminKeyValidator.verify(command.adminKey)
        verifyAtLeastOneStageActive(command.stageTurns)

        interviewTurnSettingsPort.save(command.stageTurns)

        return resultFrom(command.stageTurns)
    }

    private fun verifyAtLeastOneStageActive(stageTurns: Map<InterviewStage, Int>) {
        if (ProfileInterview.totalTargetTurns(stageTurns) < MIN_TOTAL_TURNS) {
            throw BadRequestException(INVALID_DTO_PARAMETER, "모든 단계가 0이면 인터뷰를 진행할 수 없습니다.")
        }
    }

    private fun resultFrom(stageTurns: Map<InterviewStage, Int>): InterviewStageTurnsResult {
        val resolved: Map<InterviewStage, Int> = InterviewStage.entries
            .filter { it != InterviewStage.CLOSING }
            .associateWith { stage -> stageTurns[stage] ?: stage.targetChildTurns }

        return InterviewStageTurnsResult(
            stageTurns = resolved,
            totalChildTurns = resolved.values.sum(),
        )
    }

    private companion object {
        const val MIN_TOTAL_TURNS = 1
    }
}
