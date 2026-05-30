package com.krince.boilerplate.infra.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.krince.boilerplate.shared.response.ExceptionResponseCode.EMPTY_TOKEN
import com.krince.boilerplate.shared.response.ExceptionResponseCode.EXPIRED_TOKEN
import com.krince.boilerplate.shared.response.ExceptionResponseCode.UNAUTHORIZED
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import java.io.PrintWriter
import java.io.StringWriter

@Tags("test", "unitTest")
@DisplayName("CustomAuthenticationEntryPoint 단위테스트")
class CustomAuthenticationEntryPointTest : FunSpec({

    val objectMapper = ObjectMapper()
    val entryPoint = CustomAuthenticationEntryPoint(objectMapper)

    context("commence") {
        context("성공") {
            test("request에 exceptionMessage가 없으면 UNAUTHORIZED로 401 JSON 응답을 쓴다") {
                val request = mockk<HttpServletRequest>()
                every { request.getAttribute("exceptionMessage") } returns null
                val out = StringWriter()
                val response = mockk<HttpServletResponse>(relaxed = true)
                every { response.writer } returns PrintWriter(out)
                val authException = mockk<AuthenticationException>()

                entryPoint.commence(request, response, authException)

                verify { response.contentType = MediaType.APPLICATION_JSON_VALUE }
                verify { response.status = UNAUTHORIZED.code }
                verify { response.characterEncoding = "UTF-8" }
                val body = out.toString()
                body.shouldContain(UNAUTHORIZED.message)
                body.shouldContain("\"code\":401")
                body.shouldContain("\"detailCode\":\"UA-000\"")
            }
            test("request에 exceptionMessage가 있으면 해당 코드로 JSON 응답을 쓴다") {
                val request = mockk<HttpServletRequest>()
                every { request.getAttribute("exceptionMessage") } returns EMPTY_TOKEN.message
                val out = StringWriter()
                val response = mockk<HttpServletResponse>(relaxed = true)
                every { response.writer } returns PrintWriter(out)
                val authException = mockk<AuthenticationException>()

                entryPoint.commence(request, response, authException)

                verify { response.status = EMPTY_TOKEN.code }
                val body = out.toString()
                body.shouldContain(EMPTY_TOKEN.message)
                body.shouldContain("\"detailCode\":\"UA-002\"")
            }
            test("만료된 토큰 메시지면 EXPIRED_TOKEN 코드로 401 응답을 쓴다") {
                val request = mockk<HttpServletRequest>()
                every { request.getAttribute("exceptionMessage") } returns EXPIRED_TOKEN.message
                val out = StringWriter()
                val response = mockk<HttpServletResponse>(relaxed = true)
                every { response.writer } returns PrintWriter(out)
                val authException = mockk<AuthenticationException>()

                entryPoint.commence(request, response, authException)

                verify { response.status = EXPIRED_TOKEN.code }
                out.toString().shouldContain(EXPIRED_TOKEN.message)
            }
        }
    }
})
