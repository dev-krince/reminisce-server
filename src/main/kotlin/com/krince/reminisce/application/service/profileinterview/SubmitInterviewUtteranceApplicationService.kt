package com.krince.reminisce.application.service.profileinterview

import com.krince.reminisce.application.port.access.child.ChildAccessPort
import com.krince.reminisce.application.port.`in`.profileinterview.command.SubmitInterviewUtteranceCommand
import com.krince.reminisce.application.port.`in`.profileinterview.result.ProfileInterviewResult
import com.krince.reminisce.application.port.`in`.profileinterview.usecase.SubmitInterviewUtteranceUseCase
import com.krince.reminisce.application.port.out.conversation.ConversationTurn
import com.krince.reminisce.application.port.out.profileinterview.CommandInterviewMessagePort
import com.krince.reminisce.application.port.out.profileinterview.CommandProfileInterviewPort
import com.krince.reminisce.application.port.out.profileinterview.InterviewReplyContext
import com.krince.reminisce.application.port.out.profileinterview.InterviewReplyPort
import com.krince.reminisce.application.port.out.profileinterview.LoadInterviewMessagePort
import com.krince.reminisce.application.port.out.profileinterview.LoadProfileInterviewPort
import com.krince.reminisce.application.port.out.tts.QUMI_VOICE_PROFILE
import com.krince.reminisce.application.port.out.tts.TtsPort
import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.profileinterview.InterviewMessage
import com.krince.reminisce.domain.model.profileinterview.ProfileInterview
import com.krince.reminisce.domain.model.profileinterview.vo.InterviewSpeaker
import com.krince.reminisce.domain.model.profileinterview.vo.InterviewStage
import com.krince.reminisce.domain.model.profileinterview.vo.ProfileInterviewId
import com.krince.reminisce.domain.model.profileinterview.vo.ProfileInterviewStatus
import com.krince.reminisce.domain.model.user.vo.UserId
import com.krince.reminisce.shared.exception.BusinessRuleViolationException
import com.krince.reminisce.shared.exception.NotFoundException
import com.krince.reminisce.shared.response.ExceptionResponseCode.BUSINESS_RULE_VIOLATION
import com.krince.reminisce.shared.response.ExceptionResponseCode.NOT_FOUND
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDateTime

@Service
class SubmitInterviewUtteranceApplicationService(
    private val childAccessPort: ChildAccessPort,
    private val loadProfileInterviewPort: LoadProfileInterviewPort,
    private val commandProfileInterviewPort: CommandProfileInterviewPort,
    private val loadInterviewMessagePort: LoadInterviewMessagePort,
    private val commandInterviewMessagePort: CommandInterviewMessagePort,
    private val interviewReplyPort: InterviewReplyPort,
    private val ttsPort: TtsPort,
    private val clock: Clock,
) : SubmitInterviewUtteranceUseCase {

    @Transactional
    override fun execute(command: SubmitInterviewUtteranceCommand): ProfileInterviewResult {
        val interview: ProfileInterview = loadOwnedInterview(command)
        verifyInProgress(interview)

        val now: LocalDateTime = LocalDateTime.now(clock)
        val recentTurns: List<ConversationTurn> = loadRecentTurns(interview.interviewId)
        val childTurnOrder: Long = loadInterviewMessagePort.countByInterview(interview.interviewId) + 1
        commandInterviewMessagePort.save(
            InterviewMessage.childUtterance(interview.interviewId, childTurnOrder, command.text, command.sttRawText, now),
        )

        val advanced: ProfileInterview = interview.advanceOnChildTurn(now)
        val qumiText: String = interviewReplyPort.generate(
            InterviewReplyContext(
                stage = advanced.currentStage,
                stageOpening = advanced.currentStage != interview.currentStage,
                childName = childAccessPort.findChildName(interview.childId),
                childUtterance = command.text,
                recentTurns = recentTurns,
            ),
        )
        val finalInterview: ProfileInterview = commandProfileInterviewPort.save(finishIfClosing(advanced, now))
        commandInterviewMessagePort.save(
            InterviewMessage.qumiLine(interview.interviewId, childTurnOrder + 1, qumiText, now),
        )

        return ProfileInterviewResult.from(
            interview = finalInterview,
            qumiText = qumiText,
            qumiAudio = ttsPort.synthesize(qumiText, QUMI_VOICE_PROFILE),
            created = false,
        )
    }

    private fun finishIfClosing(interview: ProfileInterview, at: LocalDateTime): ProfileInterview =
        if (interview.currentStage == InterviewStage.CLOSING) {
            interview.complete(at)
        } else {
            interview
        }

    private fun loadOwnedInterview(command: SubmitInterviewUtteranceCommand): ProfileInterview {
        val interview: ProfileInterview = loadProfileInterviewPort.findById(ProfileInterviewId(command.interviewId))
            ?: throw NotFoundException(NOT_FOUND, NOT_FOUND.message)
        verifyOwnership(interview.childId, UserId(command.guardianId))

        return interview
    }

    private fun verifyOwnership(childId: ChildId, guardianId: UserId) {
        val ownerId: UserId? = childAccessPort.findGuardianId(childId)
        if (ownerId == null || ownerId != guardianId) {
            throw NotFoundException(NOT_FOUND, NOT_FOUND.message)
        }
    }

    private fun verifyInProgress(interview: ProfileInterview) {
        if (interview.status != ProfileInterviewStatus.IN_PROGRESS) {
            throw BusinessRuleViolationException(BUSINESS_RULE_VIOLATION, BUSINESS_RULE_VIOLATION.message)
        }
    }

    private fun loadRecentTurns(interviewId: ProfileInterviewId): List<ConversationTurn> =
        loadInterviewMessagePort.findRecentByInterview(interviewId, RECENT_TURN_LIMIT)
            .map { ConversationTurn(isChild = it.speaker == InterviewSpeaker.CHILD, text = it.text) }

    companion object {
        const val RECENT_TURN_LIMIT = 6
    }
}
