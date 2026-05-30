package com.krince.boilerplate.infra.adapter.`in`.advice

import com.krince.boilerplate.shared.exception.BadRequestException
import com.krince.boilerplate.shared.exception.BusinessRuleViolationException
import com.krince.boilerplate.shared.exception.ConflictException
import com.krince.boilerplate.shared.exception.ForbiddenException
import com.krince.boilerplate.shared.exception.NotFoundException
import com.krince.boilerplate.shared.exception.UnauthorizedRefreshTokenException
import com.krince.boilerplate.shared.response.ExceptionResponseCode.BAD_REQUEST
import com.krince.boilerplate.shared.response.ExceptionResponseCode.BUSINESS_RULE_VIOLATION
import com.krince.boilerplate.shared.response.ExceptionResponseCode.CONFLICT
import com.krince.boilerplate.shared.response.ExceptionResponseCode.FORBIDDEN
import com.krince.boilerplate.shared.response.ExceptionResponseCode.INTERNAL_SERVER_ERROR
import com.krince.boilerplate.shared.response.ExceptionResponseCode.INVALID_PASSWORD
import com.krince.boilerplate.shared.response.ExceptionResponseCode.NOT_FOUND_USER
import com.krince.boilerplate.shared.response.ExceptionResponseCode.INVALID_DTO_PARAMETER
import com.krince.boilerplate.shared.response.ExceptionResponseCode.UNAUTHORIZED_REFRESH_TOKEN
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import jakarta.validation.ConstraintViolation
import jakarta.validation.ConstraintViolationException
import jakarta.validation.Path
import org.slf4j.MDC
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authorization.AuthorizationDeniedException
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.dao.InvalidDataAccessApiUsageException
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

@Tags("test", "unitTest")
@DisplayName("GlobalExceptionHandler 단위테스트")
class GlobalExceptionHandlerTest : FunSpec({

    val handler = GlobalExceptionHandler()

    afterEach {
        MDC.clear()
    }

    context("handleBadRequestException") {
        context("성공") {
            test("BadRequestException에 맞는 status와 ExceptionResponse를 반환한다") {
                val ex = BadRequestException(BAD_REQUEST, "잘못된 요청 메시지")

                val result = handler.handleBadRequestException(ex)

                result.statusCode.value() shouldBe BAD_REQUEST.code
                result.body!!.code shouldBe BAD_REQUEST.code
                result.body!!.detailCode shouldBe BAD_REQUEST.detailCode
                result.body!!.message shouldBe "잘못된 요청 메시지"
                result.body!!.success shouldBe false
            }
        }
    }

    context("handleIllegalArgumentException") {
        context("성공") {
            test("400과 BAD_REQUEST로 ResponseEntity를 반환한다") {
                val ex = IllegalArgumentException("invalid argument")

                val result = handler.handleIllegalArgumentException(ex)

                result.statusCode.value() shouldBe BAD_REQUEST.code
                result.body!!.code shouldBe BAD_REQUEST.code
                result.body!!.message shouldBe "invalid argument"
            }
        }
    }

    context("handleBadCredentialsException") {
        context("성공") {
            test("401과 INVALID_PASSWORD 메시지로 ResponseEntity를 반환한다") {
                val ex = BadCredentialsException("Bad credentials")

                val result = handler.handleBadCredentialsException(ex)

                result.statusCode.value() shouldBe INVALID_PASSWORD.code
                result.body!!.detailCode shouldBe INVALID_PASSWORD.detailCode
                result.body!!.message shouldBe INVALID_PASSWORD.message
            }
        }
    }

    context("handleAuthorizationDeniedException") {
        context("성공") {
            test("403과 FORBIDDEN으로 ResponseEntity를 반환한다") {
                val ex = AuthorizationDeniedException("Denied")

                val result = handler.handleAuthorizationDeniedException(ex)

                result.statusCode.value() shouldBe FORBIDDEN.code
                result.body!!.detailCode shouldBe FORBIDDEN.detailCode
            }
        }
    }

    context("handleForbiddenException") {
        context("성공") {
            test("exception의 코드와 메시지로 ResponseEntity를 반환한다") {
                val ex = ForbiddenException(FORBIDDEN, "접근 불가")

                val result = handler.handleForbiddenException(ex)

                result.statusCode.value() shouldBe FORBIDDEN.code
                result.body!!.message shouldBe "접근 불가"
            }
        }
    }

    context("handleNotFoundException") {
        context("성공") {
            test("exception의 코드와 메시지로 ResponseEntity를 반환한다") {
                val ex = NotFoundException(NOT_FOUND_USER, "회원이 없습니다")

                val result = handler.handleNotFoundException(ex)

                result.statusCode.value() shouldBe NOT_FOUND_USER.code
                result.body!!.message shouldBe "회원이 없습니다"
                result.body!!.detailCode shouldBe NOT_FOUND_USER.detailCode
            }
        }
    }

    context("handleConflictException") {
        context("성공") {
            test("409와 exception 메시지로 ResponseEntity를 반환한다") {
                val ex = ConflictException(CONFLICT, "이미 존재합니다")

                val result = handler.handleConflictException(ex)

                result.statusCode.value() shouldBe CONFLICT.code
                result.body!!.message shouldBe "이미 존재합니다"
            }
        }
    }

    context("handleBusinessRuleViolationException") {
        context("성공") {
            test("422와 exception 메시지로 ResponseEntity를 반환한다") {
                val ex = BusinessRuleViolationException(BUSINESS_RULE_VIOLATION, "정책 위반")

                val result = handler.handleBusinessRuleViolationException(ex)

                result.statusCode.value() shouldBe BUSINESS_RULE_VIOLATION.code
                result.body!!.message shouldBe "정책 위반"
            }
        }
    }

    context("handleUnauthorizedRefreshToken") {
        context("성공") {
            test("exception의 코드와 메시지로 ResponseEntity를 반환한다") {
                val ex = UnauthorizedRefreshTokenException(UNAUTHORIZED_REFRESH_TOKEN, "리프레시 토큰 오류")

                val result = handler.handleUnauthorizedRefreshToken(ex)

                result.statusCode.value() shouldBe UNAUTHORIZED_REFRESH_TOKEN.code
                result.body!!.message shouldBe "리프레시 토큰 오류"
            }
        }
    }

    context("handleException") {
        context("성공") {
            test("500과 INTERNAL_SERVER_ERROR로 ResponseEntity를 반환한다") {
                val ex = RuntimeException("unexpected")

                val result = handler.handleException(ex)

                result.statusCode.value() shouldBe INTERNAL_SERVER_ERROR.code
                result.body!!.detailCode shouldBe INTERNAL_SERVER_ERROR.detailCode
                result.body!!.message shouldBe INTERNAL_SERVER_ERROR.message
            }
        }
    }

    context("handleHttpMessageNotReadableException") {
        context("성공") {
            test("400과 INVALID_DTO_PARAMETER 메시지로 ResponseEntity를 반환한다") {
                val ex = mockk<HttpMessageNotReadableException>()
                every { ex.message } returns "JSON parse error"
                every { ex.stackTrace } returns arrayOf<StackTraceElement>()

                val result = handler.handleHttpMessageNotReadableException(ex)

                result.statusCode.value() shouldBe INVALID_DTO_PARAMETER.code
                result.body!!.message shouldContain INVALID_DTO_PARAMETER.message
                result.body!!.message shouldContain "JSON parse error"
            }
        }
    }

    context("handleMethodArgumentTypeMismatchException") {
        context("성공") {
            test("400과 INVALID_DTO_PARAMETER 메시지로 ResponseEntity를 반환한다") {
                val ex = mockk<MethodArgumentTypeMismatchException>()
                every { ex.message } returns "Failed to convert value of type 'String'"
                every { ex.stackTrace } returns arrayOf<StackTraceElement>()

                val result = handler.handleMethodArgumentTypeMismatchException(ex)

                result.statusCode.value() shouldBe INVALID_DTO_PARAMETER.code
                result.body!!.message shouldContain INVALID_DTO_PARAMETER.message
            }
        }
    }

    context("handleMethodArgumentNotValidException") {
        context("성공") {
            test("fieldErrors의 defaultMessage를 합쳐 400 INVALID_DTO_PARAMETER로 반환한다") {
                val bindingResult = mockk<BindingResult>()
                val fieldError1 = FieldError("dto", "loginId", "로그인 ID는 비어있을 수 없습니다.")
                val fieldError2 = FieldError("dto", "password", "비밀번호를 입력하세요.")
                every { bindingResult.fieldErrors } returns listOf(fieldError1, fieldError2)
                val ex = mockk<MethodArgumentNotValidException>()
                every { ex.bindingResult } returns bindingResult
                every { ex.stackTrace } returns arrayOf<StackTraceElement>()

                val result = handler.handleMethodArgumentNotValidException(ex)

                result.statusCode.value() shouldBe INVALID_DTO_PARAMETER.code
                result.body!!.message shouldContain "로그인 ID는 비어있을 수 없습니다."
                result.body!!.message shouldContain "비밀번호를 입력하세요."
            }
        }
    }

    context("handleConstraintViolationException") {
        context("성공") {
            test("constraintViolations 메시지를 합쳐 400 INVALID_DTO_PARAMETER로 반환한다") {
                val pathMock = mockk<Path>(relaxed = true)
                val violation1 = mockk<ConstraintViolation<*>>()
                val violation2 = mockk<ConstraintViolation<*>>()
                every { violation1.message } returns "size must be between 1 and 50"
                every { violation1.propertyPath } returns pathMock
                every { violation2.message } returns "must not be blank"
                every { violation2.propertyPath } returns pathMock
                val ex = ConstraintViolationException(mutableSetOf(violation1, violation2))

                val result = handler.handleConstraintViolationException(ex)

                result.statusCode.value() shouldBe INVALID_DTO_PARAMETER.code
                result.body!!.message shouldContain "size must be between 1 and 50"
                result.body!!.message shouldContain "must not be blank"
            }
        }
    }

    context("handleInvalidDataAccessApiUsageException") {
        context("성공") {
            test("400과 INVALID_DTO_PARAMETER로 ResponseEntity를 반환한다") {
                val ex = InvalidDataAccessApiUsageException("Invalid usage")

                val result = handler.handleInvalidDataAccessApiUsageException(ex)

                result.statusCode.value() shouldBe INVALID_DTO_PARAMETER.code
                result.body!!.message shouldBe "Invalid usage"
            }
        }
    }

    context("requestId") {
        context("성공") {
            test("MDC에 requestId가 있으면 응답 body에 포함된다") {
                MDC.put("requestId", "req-123")
                val ex = BadRequestException(BAD_REQUEST, "msg")

                val result = handler.handleBadRequestException(ex)

                result.body!!.requestId shouldBe "req-123"
            }
        }
    }
})
