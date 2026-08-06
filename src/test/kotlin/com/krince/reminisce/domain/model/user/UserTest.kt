package com.krince.reminisce.domain.model.user

import com.krince.reminisce.domain.model.user.vo.AuthProvider
import com.krince.reminisce.domain.model.user.vo.Email
import com.krince.reminisce.domain.model.user.vo.Nickname
import com.krince.reminisce.domain.model.user.vo.Role
import com.krince.reminisce.domain.model.user.vo.UserId
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeBlank
import java.time.LocalDateTime

@Tags("test", "unitTest")
@DisplayName("User 단위테스트")
class UserTest : FunSpec({

    val userId = UserId("user-uuid-1")
    val email = Email("user@example.com")
    val nickname = Nickname("홍길동")
    val role = Role.user()
    val now = LocalDateTime.now()

    context("생성") {
        context("성공") {
            test("필수 필드만으로 생성하면 createdDate와 modifiedDate는 null이다") {
                val user = User(userId, email, nickname, AuthProvider.KAKAO, role)

                user.userId shouldBe userId
                user.email shouldBe email
                user.nickname shouldBe nickname
                user.provider shouldBe AuthProvider.KAKAO
                user.role shouldBe role
                user.createdDate shouldBe null
                user.modifiedDate shouldBe null
            }
            test("createdDate와 modifiedDate를 넣으면 그대로 보존된다") {
                val user = User(userId, email, nickname, AuthProvider.KAKAO, role, createdDate = now, modifiedDate = now)

                user.createdDate.shouldNotBeNull() shouldBe now
                user.modifiedDate.shouldNotBeNull() shouldBe now
            }
        }
    }

    context("kakao 팩토리") {
        context("성공") {
            test("provider는 KAKAO, providerId가 채워지고 role은 ROLE_USER로 만든다") {
                val user = User.kakao(providerId = "1234567890", email = email, nickname = nickname)

                user.userId.value.shouldNotBeBlank()
                user.email shouldBe email
                user.nickname shouldBe nickname
                user.provider shouldBe AuthProvider.KAKAO
                user.providerId shouldBe "1234567890"
                user.role shouldBe Role.user()
            }
            test("이메일을 주지 않으면 email이 null인 채로 생성한다") {
                val user = User.kakao(providerId = "9999", email = null, nickname = nickname)

                user.email shouldBe null
                user.providerId shouldBe "9999"
                user.provider shouldBe AuthProvider.KAKAO
            }
            test("매 호출마다 서로 다른 userId를 생성한다") {
                val first = User.kakao(providerId = "1", email = null, nickname = nickname)
                val second = User.kakao(providerId = "2", email = null, nickname = nickname)

                (first.userId == second.userId) shouldBe false
            }
        }
    }

    context("google 팩토리") {
        context("성공") {
            test("provider는 GOOGLE, providerId가 채워지고 role은 ROLE_USER로 만든다") {
                val user = User.google(providerId = "google-sub-1", email = email, nickname = nickname)

                user.userId.value.shouldNotBeBlank()
                user.email shouldBe email
                user.nickname shouldBe nickname
                user.provider shouldBe AuthProvider.GOOGLE
                user.providerId shouldBe "google-sub-1"
                user.role shouldBe Role.user()
            }
        }
    }
})
