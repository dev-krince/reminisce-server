package com.krince.reminisce.infra.adapter.out.persistence.user

import com.krince.reminisce.domain.model.user.User
import com.krince.reminisce.domain.model.user.vo.AuthProvider
import com.krince.reminisce.domain.model.user.vo.Email
import com.krince.reminisce.domain.model.user.vo.Nickname
import com.krince.reminisce.domain.model.user.vo.Role
import com.krince.reminisce.domain.model.user.vo.UserId
import com.krince.reminisce.infra.adapter.out.persistence.user.entity.UserOrmEntity
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
import java.util.Optional

@Tags("test", "unitTest")
@DisplayName("UserOrmAdapter 단위테스트")
class UserOrmAdapterTest : FunSpec({

    val repository = mockk<UserRepository>()
    val adapter = UserOrmAdapter(repository)

    val userIdStr = "a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d"

    fun ormEntity(): UserOrmEntity =
        UserOrmEntity(userIdStr, "user@example.com", "홍길동", "KAKAO", "ROLE_USER", "kakao-1")

    context("findByUserId") {
        context("성공") {
            test("repository에 해당 userId가 있으면 User 도메인을 반환한다") {
                clearMocks(repository)
                every { repository.findById(userIdStr) } returns Optional.of(ormEntity())

                val result = adapter.findByUserId(UserId(userIdStr))

                result.shouldNotBeNull()
                result.userId.value shouldBe userIdStr
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

    context("findByProviderAndProviderId") {
        context("성공") {
            test("repository에 provider·providerId가 있으면 User 도메인을 반환한다") {
                clearMocks(repository)
                every { repository.findByProviderAndProviderId("KAKAO", "kakao-1") } returns ormEntity()

                val result = adapter.findByProviderAndProviderId(AuthProvider.KAKAO, "kakao-1")

                result.shouldNotBeNull()
                result.userId.value shouldBe userIdStr
                verify(exactly = 1) { repository.findByProviderAndProviderId("KAKAO", "kakao-1") }
            }
        }
        context("실패") {
            test("repository에 없으면 null을 반환한다") {
                clearMocks(repository)
                every { repository.findByProviderAndProviderId("KAKAO", "none") } returns null

                val result = adapter.findByProviderAndProviderId(AuthProvider.KAKAO, "none")

                result shouldBe null
                verify(exactly = 1) { repository.findByProviderAndProviderId("KAKAO", "none") }
            }
        }
    }

    context("save") {
        context("성공") {
            test("User 도메인을 엔티티로 저장하고 저장된 값을 도메인으로 되돌린다") {
                clearMocks(repository)
                val entitySlot = slot<UserOrmEntity>()
                every { repository.saveAndFlush(capture(entitySlot)) } answers { entitySlot.captured }
                val user = User(
                    userId = UserId(userIdStr),
                    email = Email("user@example.com"),
                    nickname = Nickname("홍길동"),
                    provider = AuthProvider.KAKAO,
                    role = Role.user(),
                    providerId = "kakao-1",
                )

                val result = adapter.save(user)

                result.userId.value shouldBe userIdStr
                result.email!!.value shouldBe "user@example.com"
                entitySlot.captured.email shouldBe "user@example.com"
                verify(exactly = 1) { repository.saveAndFlush(any()) }
            }
        }
    }
})
