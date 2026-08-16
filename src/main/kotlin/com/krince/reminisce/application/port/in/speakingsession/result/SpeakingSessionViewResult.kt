package com.krince.reminisce.application.port.`in`.speakingsession.result

import com.krince.reminisce.application.port.`in`.story.result.SceneResult
import com.krince.reminisce.domain.model.story.Scene

class SpeakingSessionViewResult(
    val viewType: SpeakingSessionViewType,
    val intro: String?,
    val introAudio: String?,
    val scene: SceneResult?,
) {
    companion object {
        fun intro(intro: String, introAudio: String?): SpeakingSessionViewResult = SpeakingSessionViewResult(
            viewType = SpeakingSessionViewType.INTRO,
            intro = intro,
            introAudio = introAudio,
            scene = null,
        )

        fun scene(
            scene: Scene,
            characterOpeningAudio: String?,
            characterClosingAudio: String?,
            narrationAudio: String?,
            missionExplanationAudio: String?,
        ): SpeakingSessionViewResult = SpeakingSessionViewResult(
            viewType = SpeakingSessionViewType.SCENE,
            intro = null,
            introAudio = null,
            scene = SceneResult.from(
                scene,
                characterOpeningAudio,
                characterClosingAudio,
                narrationAudio,
                missionExplanationAudio,
            ),
        )

        fun postActivity(): SpeakingSessionViewResult = SpeakingSessionViewResult(
            viewType = SpeakingSessionViewType.POST_ACTIVITY,
            intro = null,
            introAudio = null,
            scene = null,
        )
    }
}
