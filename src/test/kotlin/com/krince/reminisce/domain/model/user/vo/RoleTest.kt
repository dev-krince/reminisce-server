package com.krince.reminisce.domain.model.user.vo

import com.krince.reminisce.shared.exception.BadRequestException
import com.krince.reminisce.shared.response.ExceptionResponseCode.REQUIRE_NOT_BLANK
import com.krince.reminisce.shared.response.ExceptionResponseCode.REQUIRE_START_WITH
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

@Tags("test", "unitTest")
@DisplayName("Role 단위테스트")
class RoleTest : FunSpec({

    context("생성") {
        context("성공") {
            test("ROLE_ 로 시작하는 비공백 문자열이면 생성되고 value가 보존된다") {
                val vo = Role("ROLE_USER")

                vo.value shouldBe "ROLE_USER"
            }
            test("ROLE_ 접두어 뒤에 임의 문자열이어도 생성된다") {
                val vo = Role("ROLE_CUSTOM")

                vo.value shouldBe "ROLE_CUSTOM"
            }
            test("admin()은 ROLE_ADMIN을 반환한다") {
                val vo = Role.admin()

                vo.value shouldBe "ROLE_ADMIN"
            }
            test("user()는 ROLE_USER를 반환한다") {
                val vo = Role.user()

                vo.value shouldBe "ROLE_USER"
            }
            test("동일 value로 생성한 두 인스턴스는 같다") {
                val a = Role("ROLE_USER")
                val b = Role.user()

                a shouldBe b
                a.value shouldBe b.value
            }
            test("서로 다른 value면 다른 인스턴스다") {
                val a = Role.admin()
                val b = Role.user()

                (a == b) shouldBe false
                a.value shouldBe "ROLE_ADMIN"
                b.value shouldBe "ROLE_USER"
            }
        }
        context("실패") {
            test("빈 문자열이면 REQUIRE_NOT_BLANK BadRequestException을 던진다") {
                val ex = shouldThrow<BadRequestException> { Role("") }

                ex.exceptionResponseCode shouldBe REQUIRE_NOT_BLANK
                ex.message shouldBe REQUIRE_NOT_BLANK.message
            }
            test("공백만 있으면 REQUIRE_NOT_BLANK BadRequestException을 던진다") {
                val ex = shouldThrow<BadRequestException> { Role("   ") }

                ex.exceptionResponseCode shouldBe REQUIRE_NOT_BLANK
            }
            test("ROLE_ 접두어 없이 값만 있으면 REQUIRE_START_WITH BadRequestException을 던진다") {
                val ex = shouldThrow<BadRequestException> { Role("ADMIN") }

                ex.exceptionResponseCode shouldBe REQUIRE_START_WITH
                ex.message shouldBe REQUIRE_START_WITH.message
            }
            test("ROLE_ 접두어 없이 USER만 있으면 REQUIRE_START_WITH를 던진다") {
                val ex = shouldThrow<BadRequestException> { Role("USER") }

                ex.exceptionResponseCode shouldBe REQUIRE_START_WITH
            }
            test("소문자 role_ 로 시작하면 REQUIRE_START_WITH를 던진다") {
                val ex = shouldThrow<BadRequestException> { Role("role_admin") }

                ex.exceptionResponseCode shouldBe REQUIRE_START_WITH
            }
            test("앞에 공백이 있으면 REQUIRE_START_WITH를 던진다") {
                val ex = shouldThrow<BadRequestException> { Role(" ROLE_USER") }

                ex.exceptionResponseCode shouldBe REQUIRE_START_WITH
            }
        }
    }
})
