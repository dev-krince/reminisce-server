package com.krince.reminisce.infra.adapter.out.mission

import com.krince.reminisce.application.port.out.mission.MissionJudgePort
import com.krince.reminisce.application.port.out.mission.MissionJudgement
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(name = ["analysis.engine"], havingValue = "stub", matchIfMissing = true)
class MissionJudgeStubAdapter : MissionJudgePort {

    override fun judge(text: String): MissionJudgement = MissionJudgement(passed = true, hint = DEFAULT_HINT)

    private companion object {
        const val DEFAULT_HINT = "좋아요. 지금처럼 자유롭게 이야기해 보세요."
    }
}
