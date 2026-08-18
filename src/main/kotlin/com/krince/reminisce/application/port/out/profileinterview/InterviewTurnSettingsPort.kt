package com.krince.reminisce.application.port.out.profileinterview

import com.krince.reminisce.domain.model.profileinterview.vo.InterviewStage

interface InterviewTurnSettingsPort {
    fun load(): Map<InterviewStage, Int>

    fun save(stageTurns: Map<InterviewStage, Int>)
}
