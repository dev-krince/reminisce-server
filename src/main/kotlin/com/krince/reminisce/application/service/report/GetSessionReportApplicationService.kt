package com.krince.reminisce.application.service.report

import com.krince.reminisce.application.port.access.child.ChildAccessPort
import com.krince.reminisce.application.port.`in`.report.command.GetSessionReportCommand
import com.krince.reminisce.application.port.`in`.report.result.SessionReportResult
import com.krince.reminisce.application.port.`in`.report.usecase.GetSessionReportUseCase
import com.krince.reminisce.application.port.out.message.LoadMessagePort
import com.krince.reminisce.application.port.out.report.CommandReportPort
import com.krince.reminisce.application.port.out.report.LoadReportPort
import com.krince.reminisce.application.port.out.report.ReportSummaryPort
import com.krince.reminisce.application.port.out.speakingsession.LoadSpeakingSessionPort
import com.krince.reminisce.application.port.out.utteranceanalysis.LoadUtteranceAnalysisPort
import com.krince.reminisce.domain.model.message.vo.MessageId
import com.krince.reminisce.domain.model.report.Report
import com.krince.reminisce.domain.model.speakingsession.SpeakingSession
import com.krince.reminisce.domain.model.speakingsession.vo.SessionStatus
import com.krince.reminisce.domain.model.speakingsession.vo.SpeakingSessionId
import com.krince.reminisce.domain.model.story.vo.ThinkingElement
import com.krince.reminisce.domain.model.user.vo.UserId
import com.krince.reminisce.domain.model.utteranceanalysis.UtteranceAnalysis
import com.krince.reminisce.shared.exception.BusinessRuleViolationException
import com.krince.reminisce.shared.exception.NotFoundException
import com.krince.reminisce.shared.response.ExceptionResponseCode.BUSINESS_RULE_VIOLATION
import com.krince.reminisce.shared.response.ExceptionResponseCode.NOT_FOUND
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDateTime

@Service
class GetSessionReportApplicationService(
    private val loadSpeakingSessionPort: LoadSpeakingSessionPort,
    private val childAccessPort: ChildAccessPort,
    private val loadReportPort: LoadReportPort,
    private val commandReportPort: CommandReportPort,
    private val loadMessagePort: LoadMessagePort,
    private val loadUtteranceAnalysisPort: LoadUtteranceAnalysisPort,
    private val reportSummaryPort: ReportSummaryPort,
    private val clock: Clock,
) : GetSessionReportUseCase {

    @Transactional
    override fun execute(command: GetSessionReportCommand): SessionReportResult {
        val session: SpeakingSession = loadOwnedSession(command.sessionId, command.guardianId)
        if (session.status != SessionStatus.COMPLETED) {
            throw BusinessRuleViolationException(BUSINESS_RULE_VIOLATION, BUSINESS_RULE_VIOLATION.message)
        }

        val existing: Report? = loadReportPort.findBySession(session.sessionId)
        if (existing != null) {
            return sessionReportResult(existing)
        }

        return sessionReportResult(generateReport(session.sessionId))
    }

    private fun generateReport(sessionId: SpeakingSessionId): Report {
        val strengths: List<ThinkingElement> = aggregateStrengths(sessionId)
        val nextFocus: List<ThinkingElement> = ThinkingElement.entries.filterNot { it in strengths }
        val summary: String = reportSummaryPort.generate(strengths, nextFocus)
        val report: Report = Report.generate(
            sessionId = sessionId,
            strengths = strengths,
            nextFocus = nextFocus,
            summary = summary,
            at = LocalDateTime.now(clock),
        )

        return commandReportPort.save(report)
    }

    private fun aggregateStrengths(sessionId: SpeakingSessionId): List<ThinkingElement> {
        val childMessageIds: List<MessageId> = loadMessagePort.findChildMessageIdsBySession(sessionId)
        val analyses: List<UtteranceAnalysis> = loadUtteranceAnalysisPort.findByMessageIds(childMessageIds)

        return analyses.flatMap { it.detectedElements }.map { it.type }.distinct()
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

    private fun sessionReportResult(report: Report): SessionReportResult = SessionReportResult(
        summary = report.summary,
        strengths = report.strengths,
        nextFocus = report.nextFocus,
        createdAt = report.createdAt,
    )
}
