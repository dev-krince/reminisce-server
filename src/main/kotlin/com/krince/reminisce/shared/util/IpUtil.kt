package com.krince.reminisce.shared.util

import jakarta.servlet.http.HttpServletRequest

object IpUtil {
    private val IP_HEADERS = listOf(
        "X-Forwarded-For",
        "X-Real-IP",
        "X-Forwarded",
        "X-Cluster-Client-IP",
        "Forwarded-For",
        "Forwarded"
    )

    fun getClientIp(request: HttpServletRequest): String = IP_HEADERS
        .mapNotNull { request.getHeader(it) }
        .firstOrNull { it.isNotBlank() }
        ?.split(",")
        ?.firstOrNull()
        ?.trim()
        ?: request.remoteAddr
        ?: "UNKNOWN"
}