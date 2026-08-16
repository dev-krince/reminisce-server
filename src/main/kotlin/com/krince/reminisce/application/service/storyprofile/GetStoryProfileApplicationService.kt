package com.krince.reminisce.application.service.storyprofile

import com.krince.reminisce.application.port.access.child.ChildAccessPort
import com.krince.reminisce.application.port.`in`.storyprofile.command.GetStoryProfileCommand
import com.krince.reminisce.application.port.`in`.storyprofile.result.StoryProfileResult
import com.krince.reminisce.application.port.`in`.storyprofile.usecase.GetStoryProfileUseCase
import com.krince.reminisce.application.port.out.conversation.ConversationTurn
import com.krince.reminisce.application.port.out.profileinterview.LoadInterviewMessagePort
import com.krince.reminisce.application.port.out.profileinterview.LoadProfileInterviewPort
import com.krince.reminisce.application.port.out.storyprofile.CommandStoryProfilePort
import com.krince.reminisce.application.port.out.storyprofile.LoadStoryProfilePort
import com.krince.reminisce.application.port.out.storyprofile.ProfileAnalysisContext
import com.krince.reminisce.application.port.out.storyprofile.ProfileAnalysisPort
import com.krince.reminisce.application.port.out.storyprofile.ProfileAnalysisReport
import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.profileinterview.ProfileInterview
import com.krince.reminisce.domain.model.profileinterview.vo.InterviewSpeaker
import com.krince.reminisce.domain.model.storyprofile.StoryProfile
import com.krince.reminisce.domain.model.user.vo.UserId
import com.krince.reminisce.shared.exception.NotFoundException
import com.krince.reminisce.shared.response.ExceptionResponseCode.NOT_FOUND
import com.krince.reminisce.shared.response.ExceptionResponseCode.NOT_FOUND_CHILD
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDateTime

@Service
class GetStoryProfileApplicationService(
    private val childAccessPort: ChildAccessPort,
    private val loadStoryProfilePort: LoadStoryProfilePort,
    private val commandStoryProfilePort: CommandStoryProfilePort,
    private val loadProfileInterviewPort: LoadProfileInterviewPort,
    private val loadInterviewMessagePort: LoadInterviewMessagePort,
    private val profileAnalysisPort: ProfileAnalysisPort,
    private val clock: Clock,
) : GetStoryProfileUseCase {

    @Transactional
    override fun execute(command: GetStoryProfileCommand): StoryProfileResult {
        val childId = ChildId(command.childId)
        verifyOwnership(childId, UserId(command.guardianId))

        val existing: StoryProfile? = loadStoryProfilePort.findByChild(childId)
        if (existing != null) {
            return StoryProfileResult.from(existing)
        }

        return StoryProfileResult.from(materializeFromCompletedInterview(childId))
    }

    private fun materializeFromCompletedInterview(childId: ChildId): StoryProfile {
        val interview: ProfileInterview = loadProfileInterviewPort.findLatestCompletedByChild(childId)
            ?: throw NotFoundException(NOT_FOUND, NOT_FOUND.message)
        val turns: List<ConversationTurn> = loadInterviewMessagePort.findAllByInterview(interview.interviewId)
            .map { ConversationTurn(isChild = it.speaker == InterviewSpeaker.CHILD, text = it.text) }
        val report: ProfileAnalysisReport = profileAnalysisPort.analyze(
            ProfileAnalysisContext(childName = childAccessPort.findChildName(childId), turns = turns),
        )

        return commandStoryProfilePort.save(
            StoryProfile.create(
                childId = childId,
                interviewId = interview.interviewId,
                interestTopics = report.interestTopics,
                strengths = report.strengths,
                practicePoints = report.practicePoints,
                speechAnalyses = report.speechAnalyses,
                at = LocalDateTime.now(clock),
            ),
        )
    }

    private fun verifyOwnership(childId: ChildId, guardianId: UserId) {
        val ownerId: UserId? = childAccessPort.findGuardianId(childId)
        if (ownerId == null || ownerId != guardianId) {
            throw NotFoundException(NOT_FOUND_CHILD, NOT_FOUND_CHILD.message)
        }
    }
}
