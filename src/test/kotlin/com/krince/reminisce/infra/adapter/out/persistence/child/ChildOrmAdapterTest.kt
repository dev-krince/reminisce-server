package com.krince.reminisce.infra.adapter.out.persistence.child

import com.krince.reminisce.domain.model.child.Child
import com.krince.reminisce.domain.model.child.vo.ChildId
import com.krince.reminisce.domain.model.child.vo.ChildNickname
import com.krince.reminisce.domain.model.user.vo.UserId
import com.krince.reminisce.infra.adapter.out.persistence.child.entity.ChildOrmEntity
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.util.Optional

@Tags("test", "unitTest")
@DisplayName("ChildOrmAdapter 단위테스트")
class ChildOrmAdapterTest : FunSpec({

    val repository = mockk<ChildRepository>()
    val adapter = ChildOrmAdapter(repository)

    val childIdStr = "child-uuid-1"
    val guardianIdStr = "guardian-uuid-1"

    fun ormEntity(): ChildOrmEntity = ChildOrmEntity(childIdStr, guardianIdStr, "토토")

    context("findById") {
        context("성공") {
            test("repository에 해당 childId가 있으면 Child 도메인을 반환한다") {
                clearMocks(repository)
                every { repository.findById(childIdStr) } returns Optional.of(ormEntity())

                val result = adapter.findById(ChildId(childIdStr))

                result.shouldNotBeNull()
                result.childId.value shouldBe childIdStr
                result.guardianId.value shouldBe guardianIdStr
                result.nickname.value shouldBe "토토"
                verify(exactly = 1) { repository.findById(childIdStr) }
            }
        }
        context("실패") {
            test("repository에 없으면 null을 반환한다") {
                clearMocks(repository)
                every { repository.findById(childIdStr) } returns Optional.empty()

                val result = adapter.findById(ChildId(childIdStr))

                result shouldBe null
                verify(exactly = 1) { repository.findById(childIdStr) }
            }
        }
    }

    context("findAllByGuardianId") {
        context("성공") {
            test("보호자 아이 목록을 도메인 리스트로 반환한다") {
                clearMocks(repository)
                every { repository.findAllByGuardianId(guardianIdStr) } returns listOf(ormEntity())

                val result = adapter.findAllByGuardianId(UserId(guardianIdStr))

                result shouldHaveSize 1
                result.first().guardianId.value shouldBe guardianIdStr
                verify(exactly = 1) { repository.findAllByGuardianId(guardianIdStr) }
            }
        }
    }

    context("countByGuardianId") {
        context("성공") {
            test("repository의 count 결과를 그대로 반환한다") {
                clearMocks(repository)
                every { repository.countByGuardianId(guardianIdStr) } returns 2

                val result = adapter.countByGuardianId(UserId(guardianIdStr))

                result shouldBe 2L
                verify(exactly = 1) { repository.countByGuardianId(guardianIdStr) }
            }
        }
    }

    context("save") {
        context("성공") {
            test("Child 도메인을 엔티티로 저장하고 저장된 값을 도메인으로 되돌린다") {
                clearMocks(repository)
                val entitySlot = slot<ChildOrmEntity>()
                every { repository.saveAndFlush(capture(entitySlot)) } answers { entitySlot.captured }
                val child = Child(
                    childId = ChildId(childIdStr),
                    guardianId = UserId(guardianIdStr),
                    nickname = ChildNickname("토토"),
                )

                val result = adapter.save(child)

                result.childId.value shouldBe childIdStr
                result.guardianId.value shouldBe guardianIdStr
                entitySlot.captured.guardianId shouldBe guardianIdStr
                verify(exactly = 1) { repository.saveAndFlush(any()) }
            }
        }
    }
})
