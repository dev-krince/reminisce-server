package com.krince.reminisce.infra.adapter.`in`.advice

import com.krince.reminisce.shared.exception.BadRequestException
import com.krince.reminisce.shared.exception.BusinessRuleViolationException
import com.krince.reminisce.shared.exception.ConflictException
import com.krince.reminisce.shared.exception.ForbiddenException
import com.krince.reminisce.shared.exception.MailSendException
import com.krince.reminisce.shared.exception.NotFoundException
import com.krince.reminisce.shared.exception.UnauthorizedRefreshTokenException
import com.krince.reminisce.shared.response.ExceptionResponse
import com.krince.reminisce.shared.response.ExceptionResponseCode.*
import jakarta.validation.ConstraintViolationException
import net.logstash.logback.argument.StructuredArguments.keyValue
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.dao.InvalidDataAccessApiUsageException
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authorization.AuthorizationDeniedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

@RestControllerAdvice
class GlobalExceptionHandler {

    companion object {
        private const val PROJECT_PACKAGE_PREFIX = "com.krince" //TODO 변경, logback.xml도 변경
        private const val REQUEST_ID_VALUE = "requestId"
        private const val HANDLED_EXCEPTION_MESSAGE = "Handled exception"
        private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
    }

    @ExceptionHandler(BadRequestException::class)
    fun handleBadRequestException(exception: BadRequestException): ResponseEntity<ExceptionResponse> {
        printExceptionInfo(exception = exception)

        val exceptionResponseCode = exception.exceptionResponseCode
        val message = exception.message
        val exceptionResponse = ExceptionResponse(
            responseCode = exceptionResponseCode,
            message = message,
            requestId = MDC.get(REQUEST_ID_VALUE)
        )

        return ResponseEntity.status(exceptionResponse.code).body(exceptionResponse)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(exception: IllegalArgumentException): ResponseEntity<ExceptionResponse> {
        printExceptionInfo(exception)

        val exceptionResponseCode = BAD_REQUEST
        val message = exception.message ?: exceptionResponseCode.message
        val exceptionResponse = ExceptionResponse(
            responseCode = exceptionResponseCode,
            message = message,
            requestId = MDC.get(REQUEST_ID_VALUE)
        )

        return ResponseEntity.status(exceptionResponseCode.code).body(exceptionResponse)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(exception: HttpMessageNotReadableException): ResponseEntity<ExceptionResponse> {
        printExceptionInfo(exception)

        val exceptionResponseCode = INVALID_DTO_PARAMETER
        val message = "${exceptionResponseCode.message}: ${exception.message}"
        val exceptionResponse = ExceptionResponse(
            responseCode = exceptionResponseCode,
            message = message,
            requestId = MDC.get(REQUEST_ID_VALUE)
        )

        return ResponseEntity.status(exceptionResponse.code).body(exceptionResponse)
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleMethodArgumentTypeMismatchException(exception: MethodArgumentTypeMismatchException): ResponseEntity<ExceptionResponse> {
        printExceptionInfo(exception)

        val exceptionResponseCode = INVALID_DTO_PARAMETER
        val message = "${exceptionResponseCode.message}: ${exception.message}"
        val exceptionResponse = ExceptionResponse(
            responseCode = exceptionResponseCode,
            message = message,
            requestId = MDC.get(REQUEST_ID_VALUE)
        )

        return ResponseEntity.status(exceptionResponse.code).body(exceptionResponse)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(exception: MethodArgumentNotValidException): ResponseEntity<ExceptionResponse> {
        printExceptionInfo(exception = exception)

        val message: String = exception.bindingResult
            .fieldErrors
            .joinToString(", ") {
                it.defaultMessage ?: INVALID_DTO_PARAMETER.message
            }
        val exceptionResponse = ExceptionResponse(
            responseCode = INVALID_DTO_PARAMETER,
            message = message,
            requestId = MDC.get(REQUEST_ID_VALUE)
        )

        return ResponseEntity.status(exceptionResponse.code).body(exceptionResponse)
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolationException(exception: ConstraintViolationException): ResponseEntity<ExceptionResponse> {
        printExceptionInfo(exception)

        val message: String = exception.constraintViolations
            .joinToString(", ") { it.message }
        val exceptionResponse = ExceptionResponse(
            responseCode = INVALID_DTO_PARAMETER,
            message = message,
            requestId = MDC.get(REQUEST_ID_VALUE)
        )

        return ResponseEntity.status(exceptionResponse.code).body(exceptionResponse)
    }

    @ExceptionHandler(InvalidDataAccessApiUsageException::class)
    fun handleInvalidDataAccessApiUsageException(exception: InvalidDataAccessApiUsageException): ResponseEntity<ExceptionResponse> {
        printExceptionInfo(exception)

        val exceptionResponseCode = INVALID_DTO_PARAMETER
        val exceptionResponse = ExceptionResponse(
            responseCode = exceptionResponseCode,
            message = exception.message!!,
            requestId = MDC.get(REQUEST_ID_VALUE)
        )

        return ResponseEntity.status(exceptionResponse.code).body(exceptionResponse)
    }

    @ExceptionHandler(BadCredentialsException::class)
    fun handleBadCredentialsException(exception: BadCredentialsException): ResponseEntity<ExceptionResponse> {
        printExceptionInfo(exception)

        val exceptionResponseCode = INVALID_PASSWORD
        val exceptionResponse = ExceptionResponse(
            responseCode = exceptionResponseCode,
            message = exceptionResponseCode.message,
            requestId = MDC.get(REQUEST_ID_VALUE)
        )

        return ResponseEntity.status(exceptionResponse.code).body(exceptionResponse)
    }

    @ExceptionHandler(UnauthorizedRefreshTokenException::class)
    fun handleUnauthorizedRefreshToken(exception: UnauthorizedRefreshTokenException): ResponseEntity<ExceptionResponse> {
        printExceptionInfo(exception)

        val exceptionResponseCode = exception.exceptionResponseCode
        val message = exception.message
        val exceptionResponse = ExceptionResponse(
            responseCode = exceptionResponseCode,
            message = message,
            requestId = MDC.get(REQUEST_ID_VALUE)
        )

        return ResponseEntity.status(exceptionResponse.code).body(exceptionResponse)
    }

    @ExceptionHandler(AuthorizationDeniedException::class)
    fun handleAuthorizationDeniedException(exception: AuthorizationDeniedException): ResponseEntity<ExceptionResponse> {
        printExceptionInfo(exception)

        val exceptionResponseCode = FORBIDDEN
        val message = exceptionResponseCode.message
        val exceptionResponse = ExceptionResponse(
            responseCode = exceptionResponseCode,
            message = message,
            requestId = MDC.get(REQUEST_ID_VALUE)
        )

        return ResponseEntity.status(exceptionResponse.code).body(exceptionResponse)
    }

    @ExceptionHandler(ForbiddenException::class)
    fun handleForbiddenException(exception: ForbiddenException): ResponseEntity<ExceptionResponse> {
        printExceptionInfo(exception)

        val exceptionResponseCode = exception.exceptionResponseCode
        val message = exception.message
        val exceptionResponse = ExceptionResponse(
            responseCode = exceptionResponseCode,
            message = message,
            requestId = MDC.get(REQUEST_ID_VALUE)
        )

        return ResponseEntity.status(exceptionResponse.code).body(exceptionResponse)
    }

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFoundException(exception: NotFoundException): ResponseEntity<ExceptionResponse> {
        printExceptionInfo(exception = exception)

        val exceptionResponseCode = exception.exceptionResponseCode
        val message = exception.message
        val exceptionResponse = ExceptionResponse(
            responseCode = exceptionResponseCode,
            message = message,
            requestId = MDC.get(REQUEST_ID_VALUE)
        )

        return ResponseEntity.status(exceptionResponse.code).body(exceptionResponse)
    }

    @ExceptionHandler(ConflictException::class)
    fun handleConflictException(exception: ConflictException): ResponseEntity<ExceptionResponse> {
        printExceptionInfo(exception)

        val exceptionResponseCode = exception.exceptionResponseCode
        val message = exception.message
        val exceptionResponse = ExceptionResponse(
            responseCode = exceptionResponseCode,
            message = message,
            requestId = MDC.get(REQUEST_ID_VALUE)
        )

        return ResponseEntity.status(exceptionResponse.code).body(exceptionResponse)
    }

    @ExceptionHandler(BusinessRuleViolationException::class)
    fun handleBusinessRuleViolationException(exception: BusinessRuleViolationException): ResponseEntity<ExceptionResponse> {
        printExceptionInfo(exception)

        val exceptionResponseCode = exception.exceptionResponseCode
        val message = exception.message
        val exceptionResponse = ExceptionResponse(
            responseCode = exceptionResponseCode,
            message = message,
            requestId = MDC.get(REQUEST_ID_VALUE)
        )

        return ResponseEntity.status(exceptionResponse.code).body(exceptionResponse)
    }

    @ExceptionHandler(MailSendException::class)
    fun handleMailSendException(exception: MailSendException): ResponseEntity<ExceptionResponse> {
        printExceptionInfo(exception)

        val exceptionResponseCode = exception.exceptionResponseCode
        val message = exception.message
        val exceptionResponse = ExceptionResponse(
            responseCode = exceptionResponseCode,
            message = message,
            requestId = MDC.get(REQUEST_ID_VALUE)
        )

        return ResponseEntity.status(exceptionResponse.code).body(exceptionResponse)
    }

    @ExceptionHandler(Exception::class)
    fun handleException(exception: Exception): ResponseEntity<ExceptionResponse> {
        printExceptionInfo(exception = exception)
        val exceptionResponseCode = INTERNAL_SERVER_ERROR
        val exceptionResponse = ExceptionResponse(
            responseCode = exceptionResponseCode,
            message = exceptionResponseCode.message,
            requestId = MDC.get(REQUEST_ID_VALUE)
        )

        return ResponseEntity.status(exceptionResponse.code).body(exceptionResponse)
    }

    private fun printExceptionInfo(exception: Exception, expected: Boolean = true) {
        val origin = findApplicationOrigin(exception)
        val arguments = listOf(
            keyValue("event.action", "exception_handled"),
            keyValue("event.outcome", "failure"),
            keyValue("error.type", exception.javaClass.simpleName),
            keyValue("error.message", exception.message ?: ""),
            keyValue("error.origin.class", origin?.className ?: ""),
            keyValue("error.origin.method", origin?.methodName ?: ""),
            keyValue("error.origin.file", origin?.fileName ?: ""),
            keyValue("error.origin.line", origin?.lineNumber ?: -1),
        )

        if (expected) {
            log.warn(HANDLED_EXCEPTION_MESSAGE, *arguments.toTypedArray())
        } else {
            log.error(HANDLED_EXCEPTION_MESSAGE, *(arguments + exception).toTypedArray())
        }
    }

    private fun findApplicationOrigin(exception: Exception): StackTraceElement? {
        return exception.stackTrace.firstOrNull { element ->
            element.className.startsWith(PROJECT_PACKAGE_PREFIX) &&
                    !element.className.contains("LoggingAspect") &&
                    !element.className.contains("$$") &&
                    element.lineNumber > 0
        }
    }
}