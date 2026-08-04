package com.krince.reminisce.domain.model.child

import com.krince.reminisce.domain.model.child.vo.ChildNickname
import com.krince.reminisce.domain.model.user.vo.UserId
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeBlank

@Tags("test", "unitTest")
@DisplayName("Child 도메인 단위테스트")
class ChildTest : FunSpec({

    val guardianId = UserId("guardian-uuid-1")
    val nickname = ChildNickname("토토")

    context("register") {
        context("성공") {
            test("보호자 식별자와 애칭으로 아이를 생성하면 childId가 채워지고 값이 보존된다") {
                val child = Child.register(guardianId, nickname)

                child.childId.value.shouldNotBeBlank()
                child.guardianId shouldBe guardianId
                child.nickname shouldBe nickname
            }

            test("생성 직후에는 감사 필드가 비어있다") {
                val child = Child.register(guardianId, nickname)

                child.createdDate.shouldBeNull()
                child.modifiedDate.shouldBeNull()
            }

            test("연속 생성 시 childId는 서로 다르다") {
                val first = Child.register(guardianId, nickname)
                val second = Child.register(guardianId, nickname)

                (first.childId.value == second.childId.value) shouldBe false
            }
        }
    }
})
