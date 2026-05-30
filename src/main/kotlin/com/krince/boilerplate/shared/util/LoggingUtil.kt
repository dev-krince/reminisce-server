package com.krince.boilerplate.shared.util

import com.krince.boilerplate.shared.context.RequestContext
import org.slf4j.MDC

object LoggingUtil {

    private const val QUERY_STRING_MAX_LENGTH = 500

    fun setRequestContext(context: RequestContext) {
        MDC.put("requestId", context.requestId)
        MDC.put("userId", context.userId ?: "GUEST")
        MDC.put("clientIp", context.clientIp ?: "UNKNOWN")
        MDC.put("userAgent", context.userAgent ?: "UNKNOWN")
        MDC.put("queryString", truncate(context.queryString, QUERY_STRING_MAX_LENGTH))
    }

    private fun truncate(value: String?, maxLength: Int): String {
        if (value.isNullOrBlank()) return ""
        return if (value.length <= maxLength) value else value.take(maxLength) + "..."
    }

    fun clearContext() {
        MDC.clear()
    }
}