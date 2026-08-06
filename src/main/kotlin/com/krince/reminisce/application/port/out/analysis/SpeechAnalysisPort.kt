package com.krince.reminisce.application.port.out.analysis

import com.krince.reminisce.domain.model.utteranceanalysis.RawUtteranceAnalysis

interface SpeechAnalysisPort {
    fun analyze(text: String): RawUtteranceAnalysis
}
