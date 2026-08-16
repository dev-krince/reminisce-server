package com.krince.reminisce.application.service.report

import com.krince.reminisce.application.port.access.child.ChildAccessPort
import com.krince.reminisce.application.port.access.story.StoryAccessPort
import com.krince.reminisce.application.port.access.story.StoryReportSnapshot
import com.krince.reminisce.application.port.`in`.report.command.GetSessionReportCommand
import com.krince.reminisce.application.port.`in`.report.result.SessionReportChildUtterance
import com.krince.reminisce.application.port.`in`.report.result.SessionReportRepresentative
import com.krince.reminisce.application.port.`in`.report.result.SessionReportResult
import com.krince.reminisce.application.port.`in`.report.result.SessionReportSceneCard
import com.krince.reminisce.application.port.`in`.report.result.SessionReportSummary
import com.krince.reminisce.application.port.`in`.report.usecase.GetSessionReportUseCase
import com.krince.reminisce.application.port.access.story.StoryReportScene
import com.krince.reminisce.application.port.out.message.LoadMessagePort
import com.krince.reminisce.application.port.out.postactivityresult.LoadPostActivityResultPort
import com.krince.reminisce.application.port.out.report.CommandReportPort
import com.krince.reminisce.application.port.out.report.LoadReportPort
import com.krince.reminisce.application.port.out.report.ReportAnalysisContext
import com.krince.reminisce.application.port.out.report.ReportAnalysisPort
import com.krince.reminisce.application.port.out.report.ReportAnalysisResult
import com.krince.reminisce.application.port.out.report.ReportSceneContext
import com.krince.reminisce.application.port.out.report.ReportTurnContext
import com.krince.reminisce.application.port.out.report.ReportUtteranceContext
import com.krince.reminisce.application.port.out.report.RepresentativeSelection
import com.krince.reminisce.application.port.out.speakingsession.LoadSpeakingSessionPort
import com.krince.reminisce.application.port.out.utteranceanalysis.LoadUtteranceAnalysisPort
import com.krince.reminisce.domain.model.message.Message
import com.krince.reminisce.domain.model.message.vo.SpeakerType
import com.krince.reminisce.domain.model.postactivityresult.PostActivityResult
import com.krince.reminisce.domain.model.report.Report
import com.krince.reminisce.domain.model.report.RepresentativeUtterance
import com.krince.reminisce.domain.model.report.SceneHighlight
import com.krince.reminisce.domain.model.speakingsession.SpeakingSession
import com.krince.reminisce.domain.model.story.vo.SceneType
import com.krince.reminisce.domain.model.speakingsession.vo.SessionStatus
import com.krince.reminisce.domain.model.speakingsession.vo.SpeakingSessionId
import com.krince.reminisce.domain.model.user.vo.UserId
import com.krince.reminisce.domain.model.utteranceanalysis.UtteranceAnalysis
import com.krince.reminisce.shared.exception.BusinessRuleViolationException
import com.krince.reminisce.shared.exception.NotFoundException
import com.krince.reminisce.shared.response.ExceptionResponseCode.BUSINESS_RULE_VIOLATION
import com.krince.reminisce.shared.response.ExceptionResponseCode.NOT_FOUND
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.Duration
import java.time.LocalDateTime

@Service
class GetSessionReportApplicationService(
    private val loadSpeakingSessionPort: LoadSpeakingSessionPort,
    private val childAccessPort: ChildAccessPort,
    private val storyAccessPort: StoryAccessPort,
    private val loadReportPort: LoadReportPort,
    private val commandReportPort: CommandReportPort,
    private val loadMessagePort: LoadMessagePort,
    private val loadPostActivityResultPort: LoadPostActivityResultPort,
    private val loadUtteranceAnalysisPort: LoadUtteranceAnalysisPort,
    private val reportAnalysisPort: ReportAnalysisPort,
    private val clock: Clock,
) : GetSessionReportUseCase {

    override fun execute(command: GetSessionReportCommand): SessionReportResult {
        val session: SpeakingSession = loadOwnedSession(command.sessionId, command.guardianId)
        if (session.status != SessionStatus.COMPLETED) {
            throw BusinessRuleViolationException(BUSINESS_RULE_VIOLATION, BUSINESS_RULE_VIOLATION.message)
        }

        val story: StoryReportSnapshot = storyAccessPort.findReportSnapshot(session.storyId)
            ?: throw NotFoundException(NOT_FOUND, NOT_FOUND.message)
        val childName: String? = childAccessPort.findChildName(session.childId)
        val messages: List<Message> = loadMessagePort.findAllBySession(session.sessionId)
        val report: Report = resolveReport(session, story, childName, messages)

        return sessionReportResult(session, story, childName, messages, report)
    }

    private fun resolveReport(
        session: SpeakingSession,
        story: StoryReportSnapshot,
        childName: String?,
        messages: List<Message>,
    ): Report {
        val existing: Report? = loadReportPort.findBySession(session.sessionId)
        if (existing != null) {
            return existing
        }

        return generateReport(session, story, childName, messages)
    }

    private fun generateReport(
        session: SpeakingSession,
        story: StoryReportSnapshot,
        childName: String?,
        messages: List<Message>,
    ): Report {
        val childMessages: List<Message> = messages.filter { it.speakerType == SpeakerType.CHILD }
        val analyses: List<UtteranceAnalysis> =
            loadUtteranceAnalysisPort.findByMessageIds(childMessages.map { it.messageId })
        val analysisResult: ReportAnalysisResult =
            reportAnalysisPort.analyze(buildContext(childName, story, messages, analyses))
        val report: Report = Report.generate(
            sessionId = session.sessionId,
            overall = analysisResult.overall,
            participation = analysisResult.participation,
            speechAnalyses = analysisResult.speechAnalyses,
            sceneHighlights = resolveSceneHighlights(analysisResult.sceneHighlights, childMessages),
            representative = resolveRepresentative(analysisResult.representative, childMessages, analyses),
            homeGuide = analysisResult.homeGuide,
            at = LocalDateTime.now(clock),
        )

        return commandReportPort.save(report)
    }

    private fun buildContext(
        childName: String?,
        story: StoryReportSnapshot,
        messages: List<Message>,
        analyses: List<UtteranceAnalysis>,
    ): ReportAnalysisContext = ReportAnalysisContext(
        childName = childName,
        storyTitle = story.title,
        scenes = story.scenes.map { scene ->
            ReportSceneContext(sceneId = scene.sceneId, description = scene.description, goal = scene.goal)
        },
        turns = messages.sortedBy { it.turnOrder }.map { message ->
            ReportTurnContext(
                sceneId = message.sceneId.value,
                turnOrder = message.turnOrder,
                isChild = message.speakerType == SpeakerType.CHILD,
                text = message.text,
                messageId = message.messageId.value.takeIf { message.speakerType == SpeakerType.CHILD },
            )
        },
        analyses = analyses.map { analysis ->
            ReportUtteranceContext(
                messageId = analysis.messageId.value,
                detectedElements = analysis.detectedElements,
            )
        },
    )

    private fun resolveSceneHighlights(
        analyzedHighlights: List<SceneHighlight>,
        childMessages: List<Message>,
    ): List<SceneHighlight> {
        val messagesByScene: Map<String, List<Message>> = childMessages.groupBy { it.sceneId.value }
        val orderedSceneIds: List<String> = childMessages.sortedBy { it.turnOrder }.map { it.sceneId.value }.distinct()

        return orderedSceneIds.map { sceneId ->
            val lastChildMessage: Message = messagesByScene.getValue(sceneId).maxBy { it.turnOrder }
            val analyzedHighlight: SceneHighlight? = analyzedHighlights.firstOrNull { it.sceneId == sceneId }

            SceneHighlight(
                sceneId = sceneId,
                messageId = lastChildMessage.messageId.value,
                featureSentence = analyzedHighlight?.featureSentence.orEmpty(),
                featureChips = analyzedHighlight?.featureChips.orEmpty(),
            )
        }
    }

    private fun resolveRepresentative(
        selection: RepresentativeSelection,
        childMessages: List<Message>,
        analyses: List<UtteranceAnalysis>,
    ): RepresentativeUtterance {
        val childMessagesById: Map<String, Message> = childMessages.associateBy { it.messageId.value }
        val anchor: Message? = selection.messageId?.let { childMessagesById[it] }
            ?: fallbackAnchor(childMessagesById, analyses)

        return RepresentativeUtterance(
            messageId = anchor?.messageId?.value,
            text = anchor?.text,
            situation = selection.situation,
            reason = selection.reason,
            strengths = selection.strengths,
            practiceTip = selection.practiceTip,
            commentary = selection.commentary,
            chips = selection.chips,
        )
    }

    private fun fallbackAnchor(
        childMessagesById: Map<String, Message>,
        analyses: List<UtteranceAnalysis>,
    ): Message? = analyses
        .mapNotNull { analysis ->
            childMessagesById[analysis.messageId.value]?.let { it to analysis.detectedElements.size }
        }
        .sortedWith(compareByDescending<Pair<Message, Int>> { it.second }.thenBy { it.first.turnOrder })
        .firstOrNull()
        ?.first

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

    private fun sessionReportResult(
        session: SpeakingSession,
        story: StoryReportSnapshot,
        childName: String?,
        messages: List<Message>,
        report: Report,
    ): SessionReportResult {
        val messagesById: Map<String, Message> = messages.associateBy { it.messageId.value }
        val postActivity: PostActivityResult? = loadPostActivityResultPort.findBySession(session.sessionId)

        return SessionReportResult(
            summary = summary(session, story, childName, postActivity),
            overall = report.overall,
            participation = report.participation,
            speechAnalyses = report.speechAnalyses,
            sceneCards = sceneCards(story, messages, report.sceneHighlights),
            representative = representative(report.representative, messagesById),
            homeGuide = report.homeGuide,
            createdAt = report.createdAt,
        )
    }

    private fun summary(
        session: SpeakingSession,
        story: StoryReportSnapshot,
        childName: String?,
        postActivity: PostActivityResult?,
    ): SessionReportSummary = SessionReportSummary(
        childName = childName,
        storyTitle = story.title,
        activityDate = session.startedAt,
        durationMinutes = durationMinutes(session),
        cardOrderCompleted = postActivity?.isOrderCorrect == true,
        retellingCompleted = postActivity?.completedAt != null,
    )

    private fun durationMinutes(session: SpeakingSession): Long {
        val elapsed: Long = Duration.between(session.startedAt, session.lastActivityAt).toMinutes()
        if (elapsed < NO_ELAPSED_MINUTES) {
            return NO_ELAPSED_MINUTES
        }

        return elapsed
    }

    private fun sceneCards(
        story: StoryReportSnapshot,
        messages: List<Message>,
        sceneHighlights: List<SceneHighlight>,
    ): List<SessionReportSceneCard> {
        val childMessagesByScene: Map<String, List<Message>> = messages
            .filter { it.speakerType == SpeakerType.CHILD }
            .groupBy { it.sceneId.value }
        val highlightBySceneId: Map<String, SceneHighlight> = sceneHighlights.associateBy { it.sceneId }

        return story.scenes
            .filter { it.sceneType == SceneType.DIALOGUE }
            .sortedBy { it.sceneOrder }
            .mapNotNull { scene ->
                sceneCard(scene, childMessagesByScene[scene.sceneId], messages, highlightBySceneId[scene.sceneId])
            }
    }

    private fun sceneCard(
        scene: StoryReportScene,
        sceneChildMessages: List<Message>?,
        messages: List<Message>,
        highlight: SceneHighlight?,
    ): SessionReportSceneCard? {
        val lastChildMessage: Message = sceneChildMessages?.maxByOrNull { it.turnOrder } ?: return null

        return SessionReportSceneCard(
            sceneNumber = scene.sceneOrder,
            sceneId = scene.sceneId,
            title = scene.sceneTitle,
            imageUrl = scene.imageUrl,
            situation = scene.description,
            characterQuestion = precedingCharacterText(lastChildMessage, messages),
            childUtterance = SessionReportChildUtterance(
                text = lastChildMessage.text,
                audioUrl = lastChildMessage.audioUrl,
                sttRawText = lastChildMessage.sttRawText,
            ),
            featureSentence = highlight?.featureSentence.orEmpty(),
            featureChips = highlight?.featureChips.orEmpty(),
        )
    }

    private fun precedingCharacterText(childMessage: Message, messages: List<Message>): String? = messages
        .filter { it.speakerType == SpeakerType.CHARACTER && it.turnOrder < childMessage.turnOrder }
        .maxByOrNull { it.turnOrder }
        ?.text

    private fun representative(
        representative: RepresentativeUtterance,
        messagesById: Map<String, Message>,
    ): SessionReportRepresentative = SessionReportRepresentative(
        text = representative.text,
        audioUrl = representative.messageId?.let { messagesById[it]?.audioUrl },
        commentary = representative.commentary,
        chips = representative.chips,
        situation = representative.situation,
        reason = representative.reason,
        strengths = representative.strengths,
        practiceTip = representative.practiceTip,
    )

    companion object {
        private const val NO_ELAPSED_MINUTES: Long = 0L
    }
}
