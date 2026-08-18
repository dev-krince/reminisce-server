package com.krince.reminisce.application.service.admin

import com.krince.reminisce.application.port.`in`.admin.command.UpdateSceneTurnsCommand
import com.krince.reminisce.application.port.`in`.admin.result.SceneTurnsResult
import com.krince.reminisce.application.port.`in`.admin.usecase.UpdateSceneTurnsUseCase
import com.krince.reminisce.application.port.out.story.SceneTurnPort
import com.krince.reminisce.application.port.out.story.SceneTurns
import com.krince.reminisce.shared.exception.BadRequestException
import com.krince.reminisce.shared.exception.NotFoundException
import com.krince.reminisce.shared.response.ExceptionResponseCode.INVALID_DTO_PARAMETER
import com.krince.reminisce.shared.response.ExceptionResponseCode.NOT_FOUND
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SceneTurnsApplicationService(
    private val sceneTurnPort: SceneTurnPort,
) : UpdateSceneTurnsUseCase {

    @Transactional
    override fun execute(command: UpdateSceneTurnsCommand): SceneTurnsResult {
        AdminKeyValidator.verify(command.adminKey)

        val current: SceneTurns = sceneTurnPort.findTurns(command.sceneId)
            ?: throw NotFoundException(NOT_FOUND, NOT_FOUND.message)
        val mergedPreferred: Int? = command.preferredTurns ?: current.preferredTurns
        val mergedMax: Int? = command.maxTurns ?: current.maxTurns
        verifyTurnRange(mergedPreferred, mergedMax)

        sceneTurnPort.updateTurns(command.sceneId, mergedPreferred, mergedMax)

        return SceneTurnsResult(
            sceneId = command.sceneId,
            preferredTurns = mergedPreferred,
            maxTurns = mergedMax,
        )
    }

    private fun verifyTurnRange(preferredTurns: Int?, maxTurns: Int?) {
        if (preferredTurns != null && maxTurns != null && preferredTurns > maxTurns) {
            throw BadRequestException(INVALID_DTO_PARAMETER, "최소 발화(preferredTurns)는 최대 발화(maxTurns)보다 클 수 없습니다.")
        }
    }
}
