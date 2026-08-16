package com.krince.reminisce.infra.adapter.out.persistence.missionresult

import com.krince.reminisce.domain.model.missionresult.MissionResult
import com.krince.reminisce.domain.model.missionresult.vo.MissionResultId
import com.krince.reminisce.domain.model.speakingsession.vo.SpeakingSessionId
import com.krince.reminisce.infra.adapter.out.persistence.missionresult.entity.MissionResultOrmEntity
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.time.LocalDateTime

@Tags("test", "unitTest")
@DisplayName("MissionResultOrmAdapter 단위테스트")
class MissionResultOrmAdapterTest : FunSpec({

    val repository = mockk<MissionResultRepository>()
    val adapter = MissionResultOrmAdapter(repository)

    val idStr = "result-uuid-1"
    val sessionIdStr = "session-uuid-1"
    val sceneIdStr = "sc_banggui_16"
    val completedAt = LocalDateTime.of(2026, 8, 16, 10, 0)

    fun ormEntity(completed: Boolean, attemptCount: Int): MissionResultOrmEntity = MissionResultOrmEntity(
        id = idStr,
        sessionId = sessionIdStr,
        sceneId = sceneIdStr,
        completed = completed,
        attemptCount = attemptCount,
        completedAt = if (completed) completedAt else null,
    )

    context("findBySessionAndScene") {
        test("세션+신 결과가 완료 상태로 저장돼 있으면 completed=true 도메인으로 되돌린다") {
            clearMocks(repository)
            every {
                repository.findBySessionIdAndSceneId(sessionIdStr, sceneIdStr)
            } returns ormEntity(completed = true, attemptCount = 2)

            val result = adapter.findBySessionAndScene(SpeakingSessionId(sessionIdStr), sceneIdStr).shouldNotBeNull()

            result.completed shouldBe true
            result.attemptCount shouldBe 2
            result.sceneId shouldBe sceneIdStr
            result.completedAt shouldBe completedAt
        }

        test("세션+신 결과가 없으면 null을 반환한다") {
            clearMocks(repository)
            every { repository.findBySessionIdAndSceneId(sessionIdStr, sceneIdStr) } returns null

            adapter.findBySessionAndScene(SpeakingSessionId(sessionIdStr), sceneIdStr) shouldBe null
        }
    }

    context("save") {
        test("도메인을 엔티티로 저장하고 저장된 값을 도메인으로 되돌린다") {
            clearMocks(repository)
            val entitySlot = slot<MissionResultOrmEntity>()
            every { repository.saveAndFlush(capture(entitySlot)) } answers { entitySlot.captured }
            val domain = MissionResult(
                id = MissionResultId(idStr),
                sessionId = SpeakingSessionId(sessionIdStr),
                sceneId = sceneIdStr,
                completed = true,
                attemptCount = 1,
                completedAt = completedAt,
            )

            val result = adapter.save(domain)

            result.completed shouldBe true
            entitySlot.captured.sessionId shouldBe sessionIdStr
            entitySlot.captured.sceneId shouldBe sceneIdStr
            entitySlot.captured.completed shouldBe true
            verify(exactly = 1) { repository.saveAndFlush(any()) }
        }
    }
})
