package com.krince.reminisce.infra.security

import com.krince.reminisce.application.port.access.user.UserAccessPort
import com.krince.reminisce.application.port.access.user.snapshot.UserSnapshot
import com.krince.reminisce.domain.model.user.vo.UserId
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalDateTime

@Tags("test", "unitTest")
@DisplayName("CustomUserDetailsService 단위테스트")
class CustomUserDetailsServiceTest : FunSpec({

    val userAccessPort = mockk<UserAccessPort>()
    val service = CustomUserDetailsService(userAccessPort)

    val now = LocalDateTime.of(2026, 3, 9, 12, 0, 0)
    val userIdStr = "a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d"
    val userId = UserId(userIdStr)
    val snapshot = UserSnapshot(
        userId = userId,
        loginId = "testUser",
        role = "ROLE_USER",
        createdDate = now,
        modifiedDate = now,
    )

    context("loadUserByUsername") {
        context("성공") {
            test("loginId로 조회한 UserSnapshot으로 CustomUserDetails를 반환한다") {
                clearMocks(userAccessPort)
                every { userAccessPort.findByLoginId("testUser") } returns snapshot

                val result = service.loadUserByUsername("testUser")

                result.getId() shouldBe userIdStr
                result.getRole() shouldBe "ROLE_USER"
                verify(exactly = 1) { userAccessPort.findByLoginId("testUser") }
            }
        }
    }

    context("loadUserById") {
        context("성공") {
            test("id로 조회한 UserSnapshot으로 CustomUserDetails를 반환한다") {
                clearMocks(userAccessPort)
                every { userAccessPort.findByUserId(userId) } returns snapshot

                val result = service.loadUserById(userIdStr)

                result.getId() shouldBe userIdStr
                result.getRole() shouldBe "ROLE_USER"
                verify(exactly = 1) { userAccessPort.findByUserId(userId) }
            }
        }
    }
})
