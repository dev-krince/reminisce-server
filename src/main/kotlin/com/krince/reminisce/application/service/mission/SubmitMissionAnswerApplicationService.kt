package com.krince.reminisce.application.service.mission

import com.krince.reminisce.application.port.access.child.ChildAccessPort
import com.krince.reminisce.application.port.access.story.StoryAccessPort
import com.krince.reminisce.application.port.`in`.mission.command.SubmitMissionAnswerCommand
import com.krince.reminisce.application.port.`in`.mission.result.MissionAnswerResult
import com.krince.reminisce.application.port.`in`.mission.usecase.SubmitMissionAnswerUseCase
import com.krince.reminisce.application.port.out.mission.MissionJudgePort
import com.krince.reminisce.application.port.out.mission.MissionJudgement
import com.krince.reminisce.application.port.out.missionresult.CommandMissionResultPort
import com.krince.reminisce.application.port.out.missionresult.LoadMissionResultPort
import com.krince.reminisce.application.port.out.speakingsession.LoadSpeakingSessionPort
import com.krince.reminisce.domain.model.missionresult.MissionResult
import com.krince.reminisce.domain.model.speakingsession.SpeakingSession
import com.krince.reminisce.domain.model.speakingsession.vo.SpeakingSessionId
import com.krince.reminisce.domain.model.story.Mission
import com.krince.reminisce.domain.model.story.Scene
import com.krince.reminisce.domain.model.story.vo.MissionType
import com.krince.reminisce.domain.model.story.vo.SceneType
import com.krince.reminisce.domain.model.user.vo.UserId
import com.krince.reminisce.shared.exception.BusinessRuleViolationException
import com.krince.reminisce.shared.exception.NotFoundException
import com.krince.reminisce.shared.response.ExceptionResponseCode.BUSINESS_RULE_VIOLATION
import com.krince.reminisce.shared.response.ExceptionResponseCode.NOT_FOUND
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class SubmitMissionAnswerApplicationService(
    private val loadSpeakingSessionPort: LoadSpeakingSessionPort,
    private val childAccessPort: ChildAccessPort,
    private val storyAccessPort: StoryAccessPort,
    private val missionJudgePort: MissionJudgePort,
    private val loadMissionResultPort: LoadMissionResultPort,
    private val commandMissionResultPort: CommandMissionResultPort,
) : SubmitMissionAnswerUseCase {

    @Transactional
    override fun execute(command: SubmitMissionAnswerCommand): MissionAnswerResult {
        val session: SpeakingSession = loadOwnedSession(command.sessionId, command.guardianId)
        val mission: Mission = missionOf(session, command.sceneId)

        val judgement: MissionJudgement = judge(mission, command.submittedOrder, command.text)
        val saved: MissionResult = upsert(session.sessionId, command.sceneId, judgement.passed)

        return MissionAnswerResult(
            completed = saved.completed,
            attemptCount = saved.attemptCount,
            hints = resolveHints(judgement, mission),
        )
    }

    private fun missionOf(session: SpeakingSession, sceneId: String): Mission {
        val scene: Scene = storyAccessPort.findScene(session.storyId, sceneId)
            ?: throw BusinessRuleViolationException(BUSINESS_RULE_VIOLATION, BUSINESS_RULE_VIOLATION.message)
        if (scene.sceneType != SceneType.DIALOGUE) {
            throw BusinessRuleViolationException(BUSINESS_RULE_VIOLATION, BUSINESS_RULE_VIOLATION.message)
        }

        return scene.mission
            ?: throw BusinessRuleViolationException(BUSINESS_RULE_VIOLATION, BUSINESS_RULE_VIOLATION.message)
    }

    private fun judge(mission: Mission, submittedOrder: List<String>?, text: String?): MissionJudgement {
        if (mission.type == MissionType.WORD_ORDER) {
            return judgeWordOrder(mission, submittedOrder)
        }

        return missionJudgePort.judge(text.orEmpty())
    }

    private fun judgeWordOrder(mission: Mission, submittedOrder: List<String>?): MissionJudgement {
        val correctSequence: List<String> =
            mission.wordCards.orEmpty().sortedBy { it.correctOrder }.map { it.text }
        val passed: Boolean = submittedOrder != null && submittedOrder == correctSequence

        return MissionJudgement(passed = passed, hint = null)
    }

    private fun upsert(sessionId: SpeakingSessionId, sceneId: String, passed: Boolean): MissionResult {
        val now: LocalDateTime = LocalDateTime.now()
        val existing: MissionResult? = loadMissionResultPort.findBySessionAndScene(sessionId, sceneId)
        val toSave: MissionResult = existing?.resubmit(passed, now)
            ?: MissionResult.firstSubmission(sessionId, sceneId, passed, now)

        return commandMissionResultPort.save(toSave)
    }

    private fun resolveHints(judgement: MissionJudgement, mission: Mission): List<String> {
        if (judgement.passed) {
            return emptyList()
        }
        if (mission.examples.isNotEmpty()) {
            return mission.examples
        }

        return listOfNotNull(judgement.hint).ifEmpty { listOf(DEFAULT_HINT) }
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

    private companion object {
        const val DEFAULT_HINT = "조금 더 자유롭게 이야기해 보세요."
    }
}
