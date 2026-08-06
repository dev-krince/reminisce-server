package com.krince.reminisce.application.port.out.report

import com.krince.reminisce.domain.model.report.Report
import com.krince.reminisce.domain.model.speakingsession.vo.SpeakingSessionId

interface LoadReportPort {
    fun findBySession(sessionId: SpeakingSessionId): Report?
}
