package com.krince.reminisce.infra.adapter.`in`.dto.speakingsession.response

import com.krince.reminisce.application.port.`in`.speakingsession.result.SpeakingSessionViewResult
import com.krince.reminisce.infra.adapter.`in`.dto.story.response.SceneResponse
import com.krince.reminisce.infra.adapter.`in`.dto.story.response.sceneResponse
import io.swagger.v3.oas.annotations.media.Schema

@Schema(title = "SpeakingSessionViewResponse", description = "말하기 세션 현재 뷰 응답")
class SpeakingSessionViewResponse(
    @field:Schema(description = "현재 뷰 종류 (INTRO/SCENE)", example = "INTRO", required = true)
    val viewType: String,

    @field:Schema(description = "도입 뷰일 때 이야기 도입 텍스트", example = "옛날 어느 마을에 방귀를 크게 뀌는 며느리가 살았습니다.", required = false)
    val intro: String?,

    @field:Schema(description = "장면 뷰일 때 현재 장면 콘텐츠", required = false)
    val scene: SceneResponse?,
)

fun speakingSessionViewResponse(result: SpeakingSessionViewResult): SpeakingSessionViewResponse =
    SpeakingSessionViewResponse(
        viewType = result.viewType.name,
        intro = result.intro,
        scene = result.scene?.let { sceneResponse(it) },
    )
