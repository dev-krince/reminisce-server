package com.krince.reminisce.shared.context

import java.util.UUID

data class RequestContext(
    val requestId: String = UUID.randomUUID().toString(),
    val userId: String? = null,
    val clientIp: String? = null,
    val userAgent: String? = null,
    val queryString: String? = null,
    val startTime: Long = System.currentTimeMillis(),
)