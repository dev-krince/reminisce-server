package com.krince.reminisce.application.port.`in`.speakingsession.result

import com.krince.reminisce.application.port.`in`.story.result.SceneResult
import com.krince.reminisce.domain.model.story.Scene

class SpeakingSessionViewResult(
    val viewType: SpeakingSessionViewType,
    val intro: String?,
    val scene: SceneResult?,
) {
    companion object {
        fun intro(intro: String): SpeakingSessionViewResult = SpeakingSessionViewResult(
            viewType = SpeakingSessionViewType.INTRO,
            intro = intro,
            scene = null,
        )

        fun scene(
            scene: Scene,
            characterOpeningAudio: String?,
            characterClosingAudio: String?,
        ): SpeakingSessionViewResult = SpeakingSessionViewResult(
            viewType = SpeakingSessionViewType.SCENE,
            intro = null,
            scene = SceneResult.from(scene, characterOpeningAudio, characterClosingAudio),
        )

        fun postActivity(): SpeakingSessionViewResult = SpeakingSessionViewResult(
            viewType = SpeakingSessionViewType.POST_ACTIVITY,
            intro = null,
            scene = null,
        )
    }
}
