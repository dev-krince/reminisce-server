package com.krince.reminisce.application.port.access.user.context

import com.krince.reminisce.domain.model.user.User
import com.krince.reminisce.domain.model.user.vo.AuthProvider
import com.krince.reminisce.domain.model.user.vo.Email
import com.krince.reminisce.domain.model.user.vo.Nickname
import com.krince.reminisce.domain.model.user.vo.Role
import com.krince.reminisce.domain.model.user.vo.UserId
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDateTime

@Tags("test", "unitTest")
@DisplayName("UserResult 단위테스트")
class UserResultTest : FunSpec({

    val now = LocalDateTime.now()
    val nickname = Nickname("홍길동")
    val role = Role.user()

    context("from") {
        context("성공") {
            test("email이 null인 카카오 회원을 매핑하면 예외 없이 email이 null인 UserResult가 된다") {
                val user = User(
                    userId = UserId("kakao-1"),
                    email = null,
                    nickname = nickname,
                    provider = AuthProvider.KAKAO,
                    role = role,
                    providerId = "provider-1",
                    createdDate = now,
                    modifiedDate = now,
                )

                val result = UserResult.from(user)

                result.userId shouldBe "kakao-1"
                result.email shouldBe null
                result.nickname shouldBe "홍길동"
                result.role shouldBe role.value
            }

            test("email이 있는 소셜 회원을 매핑하면 email 값이 그대로 보존된다") {
                val user = User(
                    userId = UserId("google-1"),
                    email = Email("user@example.com"),
                    nickname = nickname,
                    provider = AuthProvider.GOOGLE,
                    role = role,
                    providerId = "google-sub-1",
                    createdDate = now,
                    modifiedDate = now,
                )

                val result = UserResult.from(user)

                result.userId shouldBe "google-1"
                result.email shouldBe "user@example.com"
                result.nickname shouldBe "홍길동"
            }
        }
    }
})
