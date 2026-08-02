package com.krince.reminisce.infra.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.krince.reminisce.shared.response.ExceptionResponse
import com.krince.reminisce.shared.response.ExceptionResponseCode
import com.krince.reminisce.shared.response.ExceptionResponseCode.*
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component

@Component
class CustomAuthenticationEntryPoint(private val objectMapper: ObjectMapper) : AuthenticationEntryPoint {

    companion object {
        private const val ATTRIBUTE_KEY = "exceptionMessage"
        private const val ENCODING_TYPE = "UTF-8"
    }

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException,
    ) {
        val exceptionMessage: String = request.getAttribute(ATTRIBUTE_KEY) as? String ?: UNAUTHORIZED.message
        val status: ExceptionResponseCode = ExceptionResponseCode.entries
            .find { it.message == exceptionMessage }
            ?: UNAUTHORIZED
        val exceptionResponse = ExceptionResponse(responseCode = status, message = exceptionMessage)
        val responseBody = objectMapper.writeValueAsString(exceptionResponse)

        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.status = status.code
        response.characterEncoding = ENCODING_TYPE
        response.writer.write(responseBody)
    }
}