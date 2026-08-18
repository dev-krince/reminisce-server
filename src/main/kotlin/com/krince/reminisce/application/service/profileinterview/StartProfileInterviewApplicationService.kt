package com.krince.reminisce.application.service.profileinterview

import com.krince.reminisce.application.port.access.child.ChildAccessPort
import com.krince.reminisce.application.port.access.childconsent.ChildConsentAccessPort
import com.krince.reminisce.application.port.`in`.profileinterview.command.StartProfileInterviewCommand
import com.krince.reminisce.application.port.`in`.profileinterview.result.ProfileInterviewResult
import com.krince.reminisce.application.port.`in`.profileinterview.usecase.StartProfileInterviewUseCase
import com.krince.reminisce.application.port.out.profileinterview.CommandInterviewMessagePort
import com.krince.reminisce.application.port.out.profileinterview.CommandProfileInterviewPort
import com.krince.reminisce.application.port.out.profileinterview.LoadInterviewMessagePort
import com.krince.reminisce.application.port.out.profileinterview.InterviewReplyContext
import com.krince.reminisce.application.port.out.profileinterview.InterviewReplyPort
import com.krince.reminisce.application.port.out.profileinterview.InterviewTurnSettingsPort
import com.krince.reminisce.application.port.out.profileinterview.LoadProfileInterviewPort
import com.krince.reminisce.application.port.out.tts.QUMI_VOICE_PROFILE
import com.krince.reminisce.application.port.out.tts.TtsPort
import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.profileinterview.InterviewMessage
import com.krince.reminisce.domain.model.profileinterview.ProfileInterview
import com.krince.reminisce.domain.model.profileinterview.vo.InterviewStage
import com.krince.reminisce.domain.model.story.ChildNamePersonalizer
import com.krince.reminisce.domain.model.user.vo.UserId
import com.krince.reminisce.shared.exception.BusinessRuleViolationException
import com.krince.reminisce.shared.exception.NotFoundException
import com.krince.reminisce.shared.response.ExceptionResponseCode.CONSENT_REQUIRED
import com.krince.reminisce.shared.response.ExceptionResponseCode.NOT_FOUND_CHILD
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDateTime

@Service
class StartProfileInterviewApplicationService(
    private val childAccessPort: ChildAccessPort,
    private val childConsentAccessPort: ChildConsentAccessPort,
    private val loadProfileInterviewPort: LoadProfileInterviewPort,
    private val commandProfileInterviewPort: CommandProfileInterviewPort,
    private val loadInterviewMessagePort: LoadInterviewMessagePort,
    private val commandInterviewMessagePort: CommandInterviewMessagePort,
    private val interviewReplyPort: InterviewReplyPort,
    private val interviewTurnSettingsPort: InterviewTurnSettingsPort,
    private val ttsPort: TtsPort,
    private val clock: Clock,
) : StartProfileInterviewUseCase {

    @Transactional
    override fun execute(command: StartProfileInterviewCommand): ProfileInterviewResult {
        val childId = ChildId(command.childId)
        verifyOwnership(childId, UserId(command.guardianId))
        verifyActiveConsent(childId)

        val existing: ProfileInterview? = loadProfileInterviewPort.findInProgressByChild(childId)
        if (existing != null) {
            return resumeResult(existing)
        }

        return startNewInterview(childId)
    }

    private fun startNewInterview(childId: ChildId): ProfileInterviewResult {
        val now: LocalDateTime = LocalDateTime.now(clock)
        val stageTurns: Map<InterviewStage, Int> = interviewTurnSettingsPort.load()
        val interview: ProfileInterview = commandProfileInterviewPort.save(ProfileInterview.start(childId, now, stageTurns))
        val childName: String? = childAccessPort.findChildName(childId)
        val firstQuestion: String = firstQumiQuestion(interview.currentStage, childName)
        commandInterviewMessagePort.save(
            InterviewMessage.qumiLine(interview.interviewId, FIRST_TURN_ORDER, firstQuestion, now),
        )

        return ProfileInterviewResult.from(
            interview = interview,
            qumiText = firstQuestion,
            qumiAudio = ttsPort.synthesize(firstQuestion, QUMI_VOICE_PROFILE),
            created = true,
        )
    }

    private fun resumeResult(interview: ProfileInterview): ProfileInterviewResult {
        val latestQumiMessage: InterviewMessage = checkNotNull(
            loadInterviewMessagePort.findLatestQumiMessage(interview.interviewId),
        ) { "진행 중 인터뷰에 큐미 메시지가 없습니다: ${interview.interviewId.value}" }

        return ProfileInterviewResult.from(
            interview = interview,
            qumiText = latestQumiMessage.text,
            qumiAudio = ttsPort.synthesize(latestQumiMessage.text, QUMI_VOICE_PROFILE),
            created = false,
        )
    }

    private fun verifyOwnership(childId: ChildId, guardianId: UserId) {
        val ownerId: UserId? = childAccessPort.findGuardianId(childId)
        if (ownerId == null || ownerId != guardianId) {
            throw NotFoundException(NOT_FOUND_CHILD, NOT_FOUND_CHILD.message)
        }
    }

    private fun verifyActiveConsent(childId: ChildId) {
        if (!childConsentAccessPort.hasActiveConsent(childId)) {
            throw BusinessRuleViolationException(CONSENT_REQUIRED, CONSENT_REQUIRED.message)
        }
    }

    private fun firstQumiQuestion(startStage: InterviewStage, childName: String?): String {
        if (startStage == InterviewStage.FREE_TALK) {
            return ChildNamePersonalizer.personalize(FIRST_QUESTION_TEMPLATE, childName)
        }

        return interviewReplyPort.generate(
            InterviewReplyContext(
                stage = startStage,
                stageOpening = true,
                childName = childName,
                childUtterance = "",
                recentTurns = emptyList(),
            ),
        )
    }

    companion object {
        const val FIRST_TURN_ORDER = 1L
        const val FIRST_QUESTION_TEMPLATE =
            "안녕 ㅇㅇ아! 나는 큐미야! 오늘은 ㅇㅇ이랑 재미있는 이야기를 만들어 볼 거야. ㅇㅇ이는 어떤 이야기를 좋아해?"
    }
}
