package com.krince.reminisce.domain.model.postactivityresult

import com.krince.reminisce.domain.model.speakingsession.vo.SpeakingSessionId
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDateTime

class PostActivityResultRetellingSegmentsTest : FunSpec({

    val at = LocalDateTime.parse("2026-06-01T00:00:00")

    fun ordered(): PostActivityResult = PostActivityResult.firstSubmission(
        sessionId = SpeakingSessionId("session-1"),
        submittedOrder = listOf("s1", "s2"),
        isOrderCorrect = true,
    )

    test("completeWith에 장면별 세그먼트를 넘기면 함께 저장된다") {
        val completed = ordered().completeWith(
            retellingText = "전체 재구성 텍스트",
            retellingAudioUrl = "/files/a.mp3",
            at = at,
            retellingSegments = listOf("장면1 요약", "장면2 요약"),
        )

        completed.retellingText shouldBe "전체 재구성 텍스트"
        completed.retellingSegments shouldBe listOf("장면1 요약", "장면2 요약")
        completed.completedAt shouldBe at
    }

    test("세그먼트를 생략하면 null로 유지된다(기존 호출 호환)") {
        val completed = ordered().completeWith(
            retellingText = "전체 재구성 텍스트",
            retellingAudioUrl = null,
            at = at,
        )

        completed.retellingSegments shouldBe null
        completed.retellingText shouldBe "전체 재구성 텍스트"
    }
})
