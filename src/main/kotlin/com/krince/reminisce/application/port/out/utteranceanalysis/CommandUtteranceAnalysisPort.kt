package com.krince.reminisce.application.port.out.utteranceanalysis

import com.krince.reminisce.domain.model.utteranceanalysis.UtteranceAnalysis

interface CommandUtteranceAnalysisPort {
    fun save(analysis: UtteranceAnalysis): UtteranceAnalysis

    fun deleteAllByMessageIds(messageIds: List<String>)
}
