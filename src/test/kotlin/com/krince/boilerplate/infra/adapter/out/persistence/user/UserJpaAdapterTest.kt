package com.krince.boilerplate.infra.adapter.out.persistence.user

import com.krince.boilerplate.domain.model.user.vo.LoginId
import com.krince.boilerplate.domain.model.user.vo.UserId
import com.krince.boilerplate.infra.adapter.out.persistence.user.entity.UserOrmEntity
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.Optional

@Tags("test", "unitTest")
@DisplayName("UserJpaAdapter 단위테스트")
class UserJpaAdapterTest : FunSpec({

    val repository = mockk<UserRepository>()
    val adapter = UserJpaAdapter(repository)

    val userIdStr = "a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d"

    context("findByLoginId") {
        context("성공") {
            test("repository에 해당 loginId가 있으면 User 도메인을 반환한다") {
                clearMocks(repository)
                every { repository.findByLoginId("testUser") } returns UserOrmEntity(userIdStr, "testUser", "ROLE_USER")

                val result = adapter.findByLoginId(LoginId("testUser"))

                result!!.userId.value shouldBe userIdStr
                result.loginId.value shouldBe "testUser"
                result.role.value shouldBe "ROLE_USER"
                verify(exactly = 1) { repository.findByLoginId("testUser") }
            }
        }
        context("실패") {
            test("repository에 없으면 null을 반환한다") {
                clearMocks(repository)
                every { repository.findByLoginId("unknown") } returns null

                val result = adapter.findByLoginId(LoginId("unknown"))

                result shouldBe null
                verify(exactly = 1) { repository.findByLoginId("unknown") }
            }
        }
    }

    context("findByUserId") {
        context("성공") {
            test("repository에 해당 userId가 있으면 User 도메인을 반환한다") {
                clearMocks(repository)
                every { repository.findById(userIdStr) } returns Optional.of(UserOrmEntity(userIdStr, "testUser", "ROLE_USER"))

                val result = adapter.findByUserId(UserId(userIdStr))

                result!!.userId.value shouldBe userIdStr
                result.loginId.value shouldBe "testUser"
                result.role.value shouldBe "ROLE_USER"
                verify(exactly = 1) { repository.findById(userIdStr) }
            }
        }
        context("실패") {
            test("repository에 없으면 null을 반환한다") {
                clearMocks(repository)
                every { repository.findById(userIdStr) } returns Optional.empty()

                val result = adapter.findByUserId(UserId(userIdStr))

                result shouldBe null
                verify(exactly = 1) { repository.findById(userIdStr) }
            }
        }
    }
})
