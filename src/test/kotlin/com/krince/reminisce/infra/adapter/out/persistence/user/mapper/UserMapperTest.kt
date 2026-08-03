package com.krince.reminisce.infra.adapter.out.persistence.user.mapper

import com.krince.reminisce.domain.model.user.User
import com.krince.reminisce.domain.model.user.vo.AuthProvider
import com.krince.reminisce.domain.model.user.vo.Email
import com.krince.reminisce.domain.model.user.vo.Nickname
import com.krince.reminisce.domain.model.user.vo.Password
import com.krince.reminisce.domain.model.user.vo.Role
import com.krince.reminisce.domain.model.user.vo.UserId
import com.krince.reminisce.infra.adapter.out.persistence.user.dto.UserAggregateEntity
import com.krince.reminisce.infra.adapter.out.persistence.user.entity.UserOrmEntity
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDateTime

@Tags("test", "unitTest")
@DisplayName("UserMapper 단위테스트")
class UserMapperTest : FunSpec({

    val userIdStr = "a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d"
    val now = LocalDateTime.of(2026, 3, 9, 12, 0, 0)

    fun ormEntity(
        userId: String = userIdStr,
        email: String = "user@example.com",
        password: String = "\$2a\$10\$hashedvalue",
        nickname: String = "홍길동",
        provider: String = "LOCAL",
        role: String = "ROLE_USER",
    ): UserOrmEntity = UserOrmEntity(userId, email, password, nickname, provider, role)

    context("toDomain") {
        context("성공") {
            test("UserAggregateEntity를 User 도메인으로 변환한다") {
                val entity = ormEntity().apply {
                    createdDate = now
                    modifiedDate = now
                }
                val aggregate = UserAggregateEntity(userOrmEntity = entity)

                val result = UserMapper.toDomain(aggregate)

                result.userId.value shouldBe userIdStr
                result.email.value shouldBe "user@example.com"
                result.password.value shouldBe "\$2a\$10\$hashedvalue"
                result.nickname.value shouldBe "홍길동"
                result.provider shouldBe AuthProvider.LOCAL
                result.role.value shouldBe "ROLE_USER"
                result.createdDate shouldBe now
                result.modifiedDate shouldBe now
            }
            test("createdDate와 modifiedDate가 null이어도 변환한다") {
                val aggregate = UserAggregateEntity(userOrmEntity = ormEntity(role = "ROLE_ADMIN", provider = "KAKAO"))

                val result = UserMapper.toDomain(aggregate)

                result.role.value shouldBe "ROLE_ADMIN"
                result.provider shouldBe AuthProvider.KAKAO
                result.createdDate shouldBe null
                result.modifiedDate shouldBe null
            }
        }
    }

    context("toEntity") {
        context("성공") {
            test("User 도메인을 UserAggregateEntity로 변환한다") {
                val user = User(
                    userId = UserId(userIdStr),
                    email = Email("user@example.com"),
                    password = Password("\$2a\$10\$hashedvalue"),
                    nickname = Nickname("홍길동"),
                    provider = AuthProvider.LOCAL,
                    role = Role("ROLE_USER"),
                    createdDate = now,
                    modifiedDate = now,
                )

                val result = UserMapper.toEntity(user)

                result.userOrmEntity.userId shouldBe userIdStr
                result.userOrmEntity.email shouldBe "user@example.com"
                result.userOrmEntity.password shouldBe "\$2a\$10\$hashedvalue"
                result.userOrmEntity.nickname shouldBe "홍길동"
                result.userOrmEntity.provider shouldBe "LOCAL"
                result.userOrmEntity.role shouldBe "ROLE_USER"
                result.userOrmEntity.createdDate shouldBe now
                result.userOrmEntity.modifiedDate shouldBe now
            }
        }
    }
})
