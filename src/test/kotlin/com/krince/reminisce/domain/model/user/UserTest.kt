package com.krince.reminisce.domain.model.user

import com.krince.reminisce.domain.model.user.vo.LoginId
import com.krince.reminisce.domain.model.user.vo.Role
import com.krince.reminisce.domain.model.user.vo.UserId
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import java.time.LocalDateTime

@Tags("test", "unitTest")
@DisplayName("User 단위테스트")
class UserTest : FunSpec({

    val userId = UserId("user-uuid-1")
    val loginId = LoginId("testUser")
    val role = Role.user()
    val now = LocalDateTime.now()

    context("생성") {
        context("성공") {
            test("필수 필드만으로 생성하면 createdDate와 modifiedDate는 null이다") {
                val user = User(userId = userId, loginId = loginId, role = role)

                user.userId shouldBe userId
                user.loginId shouldBe loginId
                user.role shouldBe role
                user.createdDate shouldBe null
                user.modifiedDate shouldBe null
            }
            test("createdDate와 modifiedDate를 넣으면 그대로 보존된다") {
                val user = User(
                    userId = userId,
                    loginId = loginId,
                    role = role,
                    createdDate = now,
                    modifiedDate = now,
                )

                user.userId shouldBe userId
                user.loginId shouldBe loginId
                user.role shouldBe role
                user.createdDate.shouldNotBeNull() shouldBe now
                user.modifiedDate.shouldNotBeNull() shouldBe now
            }
            test("동일 필드로 생성한 두 User는 필드 값이 같다") {
                val a = User(userId, loginId, role, now, now)
                val b = User(userId, loginId, role, now, now)

                a.userId shouldBe b.userId
                a.loginId shouldBe b.loginId
                a.role shouldBe b.role
                a.createdDate shouldBe b.createdDate
                a.modifiedDate shouldBe b.modifiedDate
            }
            test("서로 다른 userId면 다른 User다") {
                val otherId = UserId("user-uuid-2")
                val user1 = User(userId, loginId, role)
                val user2 = User(otherId, loginId, role)

                user1.userId shouldBe userId
                user2.userId shouldBe otherId
                (user1.userId == user2.userId) shouldBe false
            }
            test("Role.admin으로도 생성할 수 있다") {
                val user = User(userId, loginId, Role.admin(), now, now)

                user.role shouldBe Role.admin()
                user.role.value shouldBe "ROLE_ADMIN"
            }
        }
    }
})
