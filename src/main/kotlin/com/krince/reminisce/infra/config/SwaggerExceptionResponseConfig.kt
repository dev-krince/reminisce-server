package com.krince.reminisce.infra.config

import com.krince.reminisce.shared.response.ExceptionResponseCode
import com.krince.reminisce.shared.response.SuccessResponseCode
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.responses.ApiResponse
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerExceptionResponseConfig {
    fun getExceptionResponseComponents(): Components {
        val components = Components()
        generateExceptionResponseComponents(components)
        return components
    }

    private fun generateExceptionResponseComponents(components: Components) {
        ExceptionResponseCode.entries.forEach { responseCode ->
            components.addResponses(
                responseCode.detailCode,
                createApiResponse(responseCode.message, generateExampleJson(responseCode))
            )
        }
    }

    private fun createApiResponse(description: String, example: String): ApiResponse {
        return ApiResponse()
            .description(description)
            .content(
                Content().addMediaType(
                    "application/json",
                    MediaType().schema(Schema<Any>()).example(example)
                )
            )
    }

    fun generateExampleJson(responseCode: ExceptionResponseCode): String {
        return generateExampleJson(responseCode, responseCode.message)
    }

    fun generateExampleJson(responseCode: ExceptionResponseCode, customMessage: String): String {
        return """
            {
                "success": ${responseCode.isSuccess},
                "status": "${responseCode.httpStatus}",
                "code": ${responseCode.code},
                "detailCode": "${responseCode.detailCode}",
                "message": "${customMessage.replace("\"", "\\\"")}"
            }
        """.trimIndent()
    }

    fun generateSuccessExampleJson(responseCode: SuccessResponseCode, dataExample: String?): String {
        return if (!dataExample.isNullOrEmpty()) {
            """
            {
                "success": ${responseCode.isSuccess},
                "status": "${responseCode.httpStatus}",
                "code": ${responseCode.code},
                "detailCode": "${responseCode.detailCode}",
                "message": "${responseCode.message.replace("\"", "\\\"")}",
                "data": $dataExample
            }
            """.trimIndent()
        } else {
            """
            {
                "success": ${responseCode.isSuccess},
                "status": "${responseCode.httpStatus}",
                "code": ${responseCode.code},
                "detailCode": "${responseCode.detailCode}",
                "message": "${responseCode.message.replace("\"", "\\\"")}"
            }
            """.trimIndent()
        }
    }
}