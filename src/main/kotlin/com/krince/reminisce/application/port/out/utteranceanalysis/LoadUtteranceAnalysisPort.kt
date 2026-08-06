package com.krince.reminisce.application.port.out.utteranceanalysis

import com.krince.reminisce.domain.model.message.vo.MessageId
import com.krince.reminisce.domain.model.utteranceanalysis.UtteranceAnalysis

interface LoadUtteranceAnalysisPort {
    fun findByMessageIds(messageIds: List<MessageId>): List<UtteranceAnalysis>
}
