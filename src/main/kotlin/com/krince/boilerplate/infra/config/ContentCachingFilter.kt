package com.krince.boilerplate.infra.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.ContentCachingRequestWrapper
import org.springframework.web.util.ContentCachingResponseWrapper

@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
class ContentCachingFilter : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        if (isSseRequest(request)) {
            filterChain.doFilter(request, response)
            return
        }
        val wrappedRequest = ContentCachingRequestWrapper(request)
        wrappedRequest.setAttribute(ATTRIBUTE_CONTENT_CACHING_REQUEST, wrappedRequest)
        val wrappedResponse = ContentCachingResponseWrapper(response)
        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse)
        } finally {
            wrappedResponse.copyBodyToResponse()
        }
    }

    private fun isSseRequest(request: HttpServletRequest): Boolean =
        request.requestURI.contains("/sse")

    companion object {
        const val ATTRIBUTE_CONTENT_CACHING_REQUEST = "ContentCachingRequestWrapper"
    }
}