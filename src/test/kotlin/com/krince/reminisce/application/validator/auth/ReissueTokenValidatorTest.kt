package com.krince.reminisce.application.validator.auth

import com.krince.reminisce.shared.exception.UnauthorizedRefreshTokenException
import com.krince.reminisce.shared.response.ExceptionResponseCode.INVALID_REFRESH_TOKEN
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

@Tags("test", "unitTest")
@DisplayName("ReissueTokenValidator 단위테스트")
class ReissueTokenValidatorTest : FunSpec({

    context("validateMatches") {
        context("성공") {
            test("제공한 토큰과 저장분이 같으면 예외를 던지지 않는다") {
                shouldNotThrowAny {
                    ReissueTokenValidator.validateMatches(
                        providedToken = "Bearer refresh",
                        storedToken = "Bearer refresh",
                    )
                }
            }
        }
        context("실패") {
            test("저장분이 없으면 INVALID_REFRESH_TOKEN을 던진다") {
                val exception = shouldThrow<UnauthorizedRefreshTokenException> {
                    ReissueTokenValidator.validateMatches(
                        providedToken = "Bearer refresh",
                        storedToken = null,
                    )
                }

                exception.exceptionResponseCode shouldBe INVALID_REFRESH_TOKEN
            }
            test("회전 후 기존 토큰처럼 저장분과 다르면 INVALID_REFRESH_TOKEN을 던진다") {
                val exception = shouldThrow<UnauthorizedRefreshTokenException> {
                    ReissueTokenValidator.validateMatches(
                        providedToken = "Bearer old-refresh",
                        storedToken = "Bearer new-refresh",
                    )
                }

                exception.exceptionResponseCode shouldBe INVALID_REFRESH_TOKEN
            }
        }
    }
})
