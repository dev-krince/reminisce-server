package com.krince.boilerplate.infra.interceptor

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.krince.boilerplate.infra.config.ContentCachingFilter
import com.krince.boilerplate.infra.security.CustomUserDetails
import com.krince.boilerplate.shared.context.RequestContext
import com.krince.boilerplate.shared.util.IpUtil
import com.krince.boilerplate.shared.util.LoggingUtil
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import net.logstash.logback.argument.StructuredArgument
import net.logstash.logback.argument.StructuredArguments.keyValue
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.HandlerMapping
import org.springframework.web.util.ContentCachingRequestWrapper
import java.lang.Exception
import java.nio.charset.StandardCharsets
import java.util.UUID
import java.util.regex.Pattern
import kotlin.collections.component1
import kotlin.collections.component2

@Component
class LoggingInterceptor(
    private val objectMapper: ObjectMapper,
) : HandlerInterceptor {
    private val logger = LoggerFactory.getLogger(LoggingInterceptor::class.java)

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val startTime: Long = System.currentTimeMillis()
        val clientIp: String = IpUtil.getClientIp(request)
        val userId: String? = resolveUserId()
        val queryString: String? = request.maskedQueryString()
        val requestId = resolveRequestId(request)
        val requestContext = RequestContext(
            requestId = requestId,
            userId = userId,
            clientIp = clientIp,
            userAgent = request.userAgent(),
            queryString = queryString,
            startTime = startTime,
        )

        request.setAttribute(RequestAttribute.START_TIME, startTime)
        request.setAttribute(RequestAttribute.REQUEST_CONTEXT, requestContext)
        response.setHeader(REQUEST_ID_HEADER, requestId)

        LoggingUtil.setRequestContext(requestContext)
        logger.info(
            LogMessage.HTTP_REQUEST_STARTED,
            logField(LogField.EVENT_ACTION, EventAction.HTTP_REQUEST_STARTED),
            logField(LogField.HTTP_REQUEST_METHOD, request.method),
            logField(LogField.URL_PATH, request.requestURI),
            logField(LogField.URL_QUERY, queryString.orEmpty()),
            logField(LogField.URL_FULL, request.fullUrl(queryString)),
            logField(LogField.CLIENT_IP, clientIp),
            logField(LogField.USER_ID, userId?.toString() ?: DefaultValue.GUEST_USER),
            logField(LogField.USER_AGENT_ORIGINAL, request.userAgent()),
        )

        return true
    }

    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        exception: Exception?,
    ) {
        val startTime = request.getAttribute(RequestAttribute.START_TIME) as? Long ?: System.currentTimeMillis()
        val duration = System.currentTimeMillis() - startTime
        val queryString = request.maskedQueryString()
        val bodyPart = getCachedRequestBody(request)
        val commonArguments = buildHttpLogArguments(
            request = request,
            response = response,
            duration = duration,
            queryString = queryString,
            body = bodyPart,
            action = if (exception == null) {
                EventAction.HTTP_REQUEST_COMPLETED
            } else {
                EventAction.HTTP_REQUEST_FAILED
            },
        )

        when (exception) {
            null -> logger.info(LogMessage.HTTP_REQUEST_COMPLETED, *commonArguments.toTypedArray())

            else -> {
                val errorArguments = (
                        commonArguments + listOf(
                            logField(LogField.ERROR_TYPE, exception.javaClass.simpleName),
                            logField(LogField.ERROR_MESSAGE, exception.message.orEmpty()),
                        )
                        ).toMutableList<Any>()
                errorArguments += exception
                logger.error(LogMessage.HTTP_REQUEST_FAILED, *errorArguments.toTypedArray())
            }
        }

        LoggingUtil.clearContext()
    }

    private fun buildHttpLogArguments(
        request: HttpServletRequest,
        response: HttpServletResponse,
        duration: Long,
        queryString: String?,
        body: String?,
        action: String,
    ): List<StructuredArgument> {
        val routePattern = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE) as? String
            ?: request.requestURI
        val arguments = mutableListOf(
            logField(LogField.EVENT_ACTION, action),
            logField(LogField.EVENT_OUTCOME, response.outcome()),
            logField(LogField.EVENT_DURATION_MS, duration),
            logField(LogField.HTTP_REQUEST_METHOD, request.method),
            logField(LogField.HTTP_RESPONSE_STATUS_CODE, response.status),
            logField(LogField.URL_PATH, request.requestURI),
            logField(LogField.URL_ROUTE, routePattern),
            logField(LogField.URL_QUERY, queryString.orEmpty()),
            logField(LogField.URL_FULL, request.fullUrl(queryString)),
            logField(LogField.CLIENT_IP, IpUtil.getClientIp(request)),
            logField(LogField.USER_AGENT_ORIGINAL, request.userAgent()),
        )

        if (!body.isNullOrBlank()) {
            arguments += logField(LogField.HTTP_REQUEST_BODY_CONTENT, body)
        }

        return arguments
    }

    private fun getCachedRequestBody(request: HttpServletRequest): String? {
        val wrapper =
            request.getAttribute(ContentCachingFilter.ATTRIBUTE_CONTENT_CACHING_REQUEST) as? ContentCachingRequestWrapper
                ?: (request as? ContentCachingRequestWrapper)
                ?: return null
        val contentType = wrapper.contentType ?: return null
        if (!contentType.startsWith(ContentType.APPLICATION_JSON) && !contentType.startsWith(ContentType.TEXT)) {
            return null
        }
        val content = wrapper.contentAsByteArray
        if (content.isEmpty()) return null
        val body = String(content, StandardCharsets.UTF_8)
        val safeBody = if (contentType.startsWith(ContentType.APPLICATION_JSON)) {
            maskSensitiveFieldsInJson(body)
        } else {
            body
        }
        val truncated = if (safeBody.length > REQUEST_BODY_MAX_LOG_LENGTH) {
            safeBody.take(REQUEST_BODY_MAX_LOG_LENGTH) + "..."
        } else {
            safeBody
        }
        return truncated
    }

    private fun resolveUserId(): String? {
        return when (val principal = SecurityContextHolder.getContext().authentication?.principal) {
            is CustomUserDetails -> principal.getId()
            else -> null
        }
    }

    private fun maskSensitiveFieldsInJson(raw: String): String {
        return try {
            val root = objectMapper.readTree(raw)
            val masked = maskJsonNode(root, objectMapper)
            objectMapper.writeValueAsString(masked)
        } catch (_: Exception) {
            SENSITIVE_JSON_VALUE_PATTERN.matcher(raw).replaceAll("\"\$1\":\"****\"")
        }
    }

    private fun maskJsonNode(node: JsonNode, mapper: ObjectMapper): JsonNode {
        return when {
            node.isObject -> {
                val result = mapper.createObjectNode()
                node.fields().forEach { (name, value) ->
                    if (name.lowercase() in SENSITIVE_JSON_KEYS) {
                        result.put(name, "****")
                    } else {
                        result.set(name, maskJsonNode(value, mapper))
                    }
                }
                result
            }

            node.isArray -> {
                mapper.createArrayNode().apply {
                    node.forEach { add(maskJsonNode(it, mapper)) }
                }
            }

            else -> node
        }
    }

    private fun resolveRequestId(request: HttpServletRequest): String =
        request.getHeader(REQUEST_ID_HEADER)
            ?.takeIf { it.isNotBlank() }
            ?.take(REQUEST_ID_MAX_LENGTH)
            ?: UUID.randomUUID().toString()

    private fun HttpServletRequest.maskedQueryString(): String? =
        queryString
            ?.takeIf { it.isNotBlank() }
            ?.let(::maskSensitiveQueryString)

    private fun HttpServletRequest.fullUrl(queryString: String?): String {
        val queryPart = if (queryString.isNullOrBlank()) "" else "?$queryString"
        return "$requestURI$queryPart"
    }

    private fun HttpServletRequest.userAgent(): String =
        getHeader(USER_AGENT_HEADER) ?: DefaultValue.UNKNOWN

    private fun HttpServletResponse.outcome(): String =
        if (status < HTTP_BAD_REQUEST_STATUS) EventOutcome.SUCCESS else EventOutcome.FAILURE

    private fun maskSensitiveQueryString(queryString: String): String =
        queryString
            .split("&")
            .joinToString("&") { parameter ->
                val name = parameter.substringBefore("=", parameter).lowercase()
                if (name in SENSITIVE_JSON_KEYS) {
                    "${parameter.substringBefore("=")}=****"
                } else {
                    parameter
                }
            }

    private fun logField(fieldName: String, value: Any): StructuredArgument =
        keyValue(fieldName, value)

    companion object {
        private const val REQUEST_ID_HEADER = "X-Request-Id"
        private const val USER_AGENT_HEADER = "User-Agent"
        private const val REQUEST_ID_MAX_LENGTH = 100
        private const val REQUEST_BODY_MAX_LOG_LENGTH = 2000
        private const val HTTP_BAD_REQUEST_STATUS = 400

        private object RequestAttribute {
            const val START_TIME = "startTime"
            const val REQUEST_CONTEXT = "requestContext"
        }

        private val SENSITIVE_JSON_KEYS = setOf(
            "password",
            "newpassword",
            "currentpassword",
            "accesstoken",
            "refreshtoken",
            "authorization",
        )

        private val SENSITIVE_JSON_VALUE_PATTERN: Pattern = Pattern.compile(
            "\"(password|newPassword|currentPassword|accessToken|refreshToken|authorization)\"\\s*:\\s*\"[^\"]*\"",
            Pattern.CASE_INSENSITIVE,
        )

        private object LogMessage {
            const val HTTP_REQUEST_STARTED = "HTTP request started"
            const val HTTP_REQUEST_COMPLETED = "HTTP request completed"
            const val HTTP_REQUEST_FAILED = "HTTP request failed"
        }

        private object EventAction {
            const val HTTP_REQUEST_STARTED = "http_request_started"
            const val HTTP_REQUEST_COMPLETED = "http_request_completed"
            const val HTTP_REQUEST_FAILED = "http_request_failed"
        }

        private object EventOutcome {
            const val SUCCESS = "success"
            const val FAILURE = "failure"
        }

        private object DefaultValue {
            const val GUEST_USER = "GUEST"
            const val UNKNOWN = "UNKNOWN"
        }

        private object ContentType {
            const val APPLICATION_JSON = "application/json"
            const val TEXT = "text/"
        }

        private object LogField {
            const val EVENT_ACTION = "event.action"
            const val EVENT_OUTCOME = "event.outcome"
            const val EVENT_DURATION_MS = "event.duration_ms"
            const val HTTP_REQUEST_METHOD = "http.request.method"
            const val HTTP_RESPONSE_STATUS_CODE = "http.response.status_code"
            const val HTTP_REQUEST_BODY_CONTENT = "http.request.body.content"
            const val URL_PATH = "url.path"
            const val URL_ROUTE = "url.route"
            const val URL_QUERY = "url.query"
            const val URL_FULL = "url.full"
            const val CLIENT_IP = "client.ip"
            const val USER_ID = "user.id"
            const val USER_AGENT_ORIGINAL = "user_agent.original"
            const val ERROR_TYPE = "error.type"
            const val ERROR_MESSAGE = "error.message"
        }
    }
}