package com.krince.reminisce.application.port.`in`.admin.command

import com.krince.reminisce.domain.model.profileinterview.vo.InterviewStage

class UpdateInterviewStageTurnsCommand(
    val adminKey: String,
    val stageTurns: Map<InterviewStage, Int>,
)
