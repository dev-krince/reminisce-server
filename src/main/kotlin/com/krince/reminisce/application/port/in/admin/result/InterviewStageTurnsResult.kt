package com.krince.reminisce.application.port.`in`.admin.result

import com.krince.reminisce.domain.model.profileinterview.vo.InterviewStage

class InterviewStageTurnsResult(
    val stageTurns: Map<InterviewStage, Int>,
    val totalChildTurns: Int,
)
