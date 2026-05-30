package com.krince.boilerplate.infra.adapter.out

import com.krince.boilerplate.shared.response.ExceptionResponseCode.INVALID_PASSWORD
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.crypto.password.PasswordEncoder

@Tags("test", "unitTest")
@DisplayName("PasswordEncoderAdapter 단위테스트")
class PasswordEncoderAdapterTest : FunSpec({

    val encoder = mockk<PasswordEncoder>()
    val adapter = PasswordEncoderAdapter(encoder)

    context("encode") {
        context("성공") {
            test("encoder.encode 결과를 그대로 반환한다") {
                clearMocks(encoder)
                val raw = "Password1!"
                val encoded = "\$2a\$10\$encodedHash"
                every { encoder.encode(raw) } returns encoded

                val result = adapter.encode(raw)

                result shouldBe encoded
                verify(exactly = 1) { encoder.encode(raw) }
            }
        }
        context("실패") {
            test("encoder.encode가 null을 반환하면 IllegalStateException을 던진다") {
                clearMocks(encoder)
                every { encoder.encode(any()) } returns null

                val ex = shouldThrow<IllegalStateException> { adapter.encode("any") }

                ex.message shouldBe "비밀번호 암호화에 실패했습니다."
            }
        }
    }

    context("matchPassword") {
        context("성공") {
            test("일치하면 예외를 던지지 않는다") {
                clearMocks(encoder)
                every { encoder.matches("Password1!", "\$2a\$10\$hash") } returns true

                adapter.matchPassword("Password1!", "\$2a\$10\$hash")

                verify(exactly = 1) { encoder.matches("Password1!", "\$2a\$10\$hash") }
            }
        }
        context("실패") {
            test("일치하지 않으면 BadCredentialsException을 던진다") {
                clearMocks(encoder)
                every { encoder.matches("wrong", "encoded") } returns false

                val ex = shouldThrow<BadCredentialsException> {
                    adapter.matchPassword("wrong", "encoded")
                }

                ex.message shouldBe INVALID_PASSWORD.message
            }
        }
    }
})
