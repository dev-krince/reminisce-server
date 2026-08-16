package com.krince.reminisce.application.port.out.mission

interface MissionJudgePort {
    fun judge(text: String): MissionJudgement
}
