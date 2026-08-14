package com.krince.reminisce.application.port.out.analysis

import com.krince.reminisce.application.port.out.conversation.ConversationTurn
import com.krince.reminisce.domain.model.utteranceanalysis.RawUtteranceAnalysis

interface SpeechAnalysisPort {
    fun analyze(text: String, recentTurns: List<ConversationTurn> = emptyList()): RawUtteranceAnalysis
}
