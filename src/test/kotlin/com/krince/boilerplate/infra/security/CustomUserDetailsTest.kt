package com.krince.boilerplate.infra.security

import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe

@Tags("test", "unitTest")
@DisplayName("CustomUserDetails 단위테스트")
class CustomUserDetailsTest : FunSpec({

    val id = "a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d"
    val role = "ROLE_USER"

    context("getId") {
        context("성공") {
            test("생성 시 전달한 id를 반환한다") {
                val details = CustomUserDetails(id = id, role = role)
                details.getId() shouldBe id
            }
        }
    }

    context("getRole") {
        context("성공") {
            test("생성 시 전달한 role을 반환한다") {
                val details = CustomUserDetails(id = id, role = role)
                details.getRole() shouldBe role
            }
        }
    }

    context("getAuthorities") {
        context("성공") {
            test("role로 SimpleGrantedAuthority 하나를 반환한다") {
                val details = CustomUserDetails(id = id, role = "ROLE_ADMIN")
                val authorities = details.authorities
                authorities.shouldHaveSize(1)
                authorities.first().authority shouldBe "ROLE_ADMIN"
            }
        }
    }

    context("getPassword") {
        context("성공") {
            test("고정 메시지를 반환한다") {
                val details = CustomUserDetails(id = id, role = role)
                details.password shouldBe "The Password is not being entered"
            }
        }
    }

    context("getUsername") {
        context("성공") {
            test("고정 메시지를 반환한다") {
                val details = CustomUserDetails(id = id, role = role)
                details.username shouldBe "The username is not being entered."
            }
        }
    }
})
