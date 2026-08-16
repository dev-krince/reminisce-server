package com.krince.reminisce.infra.adapter.out.persistence.ttscache

import com.krince.reminisce.domain.model.ttscache.TtsCache
import com.krince.reminisce.infra.adapter.out.persistence.ttscache.entity.TtsCacheOrmEntity
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.springframework.dao.DataIntegrityViolationException

@Tags("test", "unitTest")
@DisplayName("TtsCacheOrmAdapter 단위테스트")
class TtsCacheOrmAdapterTest : FunSpec({

    val repository = mockk<TtsCacheRepository>()
    val adapter = TtsCacheOrmAdapter(repository)

    val cacheKey = "cache-key-1"
    val voiceProfile = "young_woman_gentle"
    val fileUrl = "audio://opening"

    context("findFileUrlByCacheKey") {
        test("캐시 키로 조회한 엔티티의 fileUrl을 반환한다") {
            clearMocks(repository)
            every { repository.findFirstByCacheKey(cacheKey) } returns
                TtsCacheOrmEntity(cacheKey = cacheKey, voiceProfile = voiceProfile, fileUrl = fileUrl)

            val result = adapter.findFileUrlByCacheKey(cacheKey)

            result shouldBe fileUrl
            verify(exactly = 1) { repository.findFirstByCacheKey(cacheKey) }
        }

        test("캐시 미스면 null을 반환한다") {
            clearMocks(repository)
            every { repository.findFirstByCacheKey(cacheKey) } returns null

            val result = adapter.findFileUrlByCacheKey(cacheKey)

            result shouldBe null
        }
    }

    context("save") {
        test("도메인을 엔티티로 매핑해 저장에 위임한다") {
            clearMocks(repository)
            val entitySlot = slot<TtsCacheOrmEntity>()
            every { repository.save(capture(entitySlot)) } answers { entitySlot.captured }

            adapter.save(TtsCache(cacheKey = cacheKey, voiceProfile = voiceProfile, fileUrl = fileUrl))

            entitySlot.captured.cacheKey shouldBe cacheKey
            entitySlot.captured.voiceProfile shouldBe voiceProfile
            entitySlot.captured.fileUrl shouldBe fileUrl
            verify(exactly = 1) { repository.save(any()) }
        }

        test("동시 삽입으로 유니크 제약 위반이 나면 예외를 삼켜 조회 안전을 유지한다") {
            clearMocks(repository)
            every { repository.save(any()) } throws DataIntegrityViolationException("duplicate key")

            adapter.save(TtsCache(cacheKey = cacheKey, voiceProfile = voiceProfile, fileUrl = fileUrl))

            verify(exactly = 1) { repository.save(any()) }
        }

        test("무결성 위반이 아닌 예외는 그대로 전파한다") {
            clearMocks(repository)
            every { repository.save(any()) } throws IllegalStateException("boom")

            shouldThrow<IllegalStateException> {
                adapter.save(TtsCache(cacheKey = cacheKey, voiceProfile = voiceProfile, fileUrl = fileUrl))
            }
        }
    }
})
