package com.krince.boilerplate.infra.adapter.out.persistence.user.mapper

import com.krince.boilerplate.domain.model.user.User
import com.krince.boilerplate.domain.model.user.vo.LoginId
import com.krince.boilerplate.domain.model.user.vo.Role
import com.krince.boilerplate.domain.model.user.vo.UserId
import com.krince.boilerplate.infra.adapter.out.persistence.user.dto.UserAggregateEntity
import com.krince.boilerplate.infra.adapter.out.persistence.user.entity.UserOrmEntity
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

    context("toDomain") {
        context("성공") {
            test("UserAggregateEntity를 User 도메인으로 변환한다") {
                val ormEntity = UserOrmEntity(userIdStr, "testUser", "ROLE_USER").apply {
                    createdDate = now
                    modifiedDate = now
                }
                val aggregate = UserAggregateEntity(userOrmEntity = ormEntity)

                val result = UserMapper.toDomain(aggregate)

                result.userId.value shouldBe userIdStr
                result.loginId.value shouldBe "testUser"
                result.role.value shouldBe "ROLE_USER"
                result.createdDate shouldBe now
                result.modifiedDate shouldBe now
            }
            test("createdDate와 modifiedDate가 null이어도 변환한다") {
                val ormEntity = UserOrmEntity(userIdStr, "admin", "ROLE_ADMIN")
                val aggregate = UserAggregateEntity(userOrmEntity = ormEntity)

                val result = UserMapper.toDomain(aggregate)

                result.userId.value shouldBe userIdStr
                result.loginId.value shouldBe "admin"
                result.role.value shouldBe "ROLE_ADMIN"
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
                    loginId = LoginId("testUser"),
                    role = Role("ROLE_USER"),
                    createdDate = now,
                    modifiedDate = now,
                )

                val result = UserMapper.toEntity(user)

                result.userOrmEntity.userId shouldBe userIdStr
                result.userOrmEntity.loginId shouldBe "testUser"
                result.userOrmEntity.role shouldBe "ROLE_USER"
                result.userOrmEntity.createdDate shouldBe now
                result.userOrmEntity.modifiedDate shouldBe now
            }
            test("createdDate와 modifiedDate가 null이어도 변환한다") {
                val user = User(
                    userId = UserId(userIdStr),
                    loginId = LoginId("admin"),
                    role = Role("ROLE_ADMIN"),
                )

                val result = UserMapper.toEntity(user)

                result.userOrmEntity.userId shouldBe userIdStr
                result.userOrmEntity.loginId shouldBe "admin"
                result.userOrmEntity.role shouldBe "ROLE_ADMIN"
                result.userOrmEntity.createdDate shouldBe null
                result.userOrmEntity.modifiedDate shouldBe null
            }
        }
    }
})
