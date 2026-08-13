package com.krince.reminisce.application.service.speakingsession

import com.krince.reminisce.application.port.access.child.ChildAccessPort
import com.krince.reminisce.application.port.access.story.StoryAccessPort
import com.krince.reminisce.application.port.`in`.speakingsession.command.GetSpeakingHintCommand
import com.krince.reminisce.application.port.`in`.speakingsession.result.SpeakingHintResult
import com.krince.reminisce.application.port.`in`.speakingsession.usecase.GetSpeakingHintUseCase
import com.krince.reminisce.application.port.out.speakingsession.LoadSpeakingSessionPort
import com.krince.reminisce.domain.model.speakingsession.SpeakingSession
import com.krince.reminisce.domain.model.speakingsession.vo.SpeakingSessionId
import com.krince.reminisce.domain.model.story.Mission
import com.krince.reminisce.domain.model.user.vo.UserId
import com.krince.reminisce.shared.exception.NotFoundException
import com.krince.reminisce.shared.response.ExceptionResponseCode.NOT_FOUND
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetSpeakingHintApplicationService(
    private val loadSpeakingSessionPort: LoadSpeakingSessionPort,
    private val childAccessPort: ChildAccessPort,
    private val storyAccessPort: StoryAccessPort,
) : GetSpeakingHintUseCase {

    @Transactional(readOnly = true)
    override fun execute(command: GetSpeakingHintCommand): SpeakingHintResult {
        val session: SpeakingSession = loadOwnedSession(command.sessionId, command.guardianId)
        val mission: Mission? = currentSceneMission(session)

        return SpeakingHintResult(
            goal = mission?.goal,
            hints = mission?.examples ?: emptyList(),
        )
    }

    private fun currentSceneMission(session: SpeakingSession): Mission? {
        val currentSceneId: String = session.currentSceneId ?: return null

        return storyAccessPort.findScene(session.storyId, currentSceneId)?.mission
    }

    private fun loadOwnedSession(sessionId: String, guardianId: String): SpeakingSession {
        val session: SpeakingSession = loadSpeakingSessionPort.findById(SpeakingSessionId(sessionId))
            ?: throw NotFoundException(NOT_FOUND, NOT_FOUND.message)
        val ownerId: UserId? = childAccessPort.findGuardianId(session.childId)
        if (ownerId == null || ownerId != UserId(guardianId)) {
            throw NotFoundException(NOT_FOUND, NOT_FOUND.message)
        }

        return session
    }
}
