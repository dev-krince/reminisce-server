package com.krince.boilerplate.infra.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.krince.boilerplate.shared.response.ExceptionResponseCode.FORBIDDEN
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
import org.springframework.security.access.AccessDeniedException
import java.io.PrintWriter
import java.io.StringWriter

@Tags("test", "unitTest")
@DisplayName("CustomAccessDeniedHandler 단위테스트")
class CustomAccessDeniedHandlerTest : FunSpec({

    val objectMapper = ObjectMapper()
    val handler = CustomAccessDeniedHandler(objectMapper)

    context("handle") {
        context("성공") {
            test("request에 exceptionMessage가 없으면 FORBIDDEN으로 403 JSON 응답을 쓴다") {
                val request = mockk<HttpServletRequest>()
                every { request.getAttribute("exceptionMessage") } returns null
                val out = StringWriter()
                val response = mockk<HttpServletResponse>(relaxed = true)
                every { response.writer } returns PrintWriter(out)
                val accessDeniedException = mockk<AccessDeniedException>()

                handler.handle(request, response, accessDeniedException)

                verify { response.contentType = MediaType.APPLICATION_JSON_VALUE }
                verify { response.status = FORBIDDEN.code }
                verify { response.characterEncoding = "UTF-8" }
                val body = out.toString()
                body.shouldContain(FORBIDDEN.message)
                body.shouldContain("\"code\":403")
                body.shouldContain("\"detailCode\":\"FBD-000\"")
            }
            test("request에 exceptionMessage가 있으면 해당 코드로 JSON 응답을 쓴다") {
                val request = mockk<HttpServletRequest>()
                every { request.getAttribute("exceptionMessage") } returns FORBIDDEN.message
                val out = StringWriter()
                val response = mockk<HttpServletResponse>(relaxed = true)
                every { response.writer } returns PrintWriter(out)
                val accessDeniedException = mockk<AccessDeniedException>()

                handler.handle(request, response, accessDeniedException)

                verify { response.status = FORBIDDEN.code }
                out.toString().shouldContain(FORBIDDEN.message)
            }
        }
    }
})
