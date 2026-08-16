package com.krince.reminisce.application.port.out.missionresult

import com.krince.reminisce.domain.model.missionresult.MissionResult

interface CommandMissionResultPort {
    fun save(result: MissionResult): MissionResult
}
