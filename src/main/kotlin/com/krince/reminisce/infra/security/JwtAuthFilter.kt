package com.krince.reminisce.infra.security

import com.krince.reminisce.application.port.out.auth.AccessTokenBlacklistPort
import com.krince.reminisce.shared.exception.NotFoundException
import com.krince.reminisce.shared.response.ExceptionResponseCode.*
import io.jsonwebtoken.ExpiredJwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthFilter(
    private val jwtProvider: JwtProvider,
    private val userDetailsService: CustomUserDetailsService,
    private val accessTokenBlacklistPort: AccessTokenBlacklistPort,
) : OncePerRequestFilter() {

    companion object {
        private const val ATTRIBUTE_KEY = "exceptionMessage"
        private const val AUTHORIZATION_HEADER = "Authorization"
        private const val BEARER_PREFIX = "Bearer "
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val header: String? = request.getHeader(AUTHORIZATION_HEADER)
        val token: String? = header
            ?.takeIf { it.startsWith(BEARER_PREFIX) }
            ?.removePrefix(BEARER_PREFIX)
            ?.trim()
            ?.takeIf { it.isNotEmpty() }

        if (token.isNullOrBlank()) {
            request.setAttribute(ATTRIBUTE_KEY, EMPTY_TOKEN.message)
            filterChain.doFilter(request, response)

            return
        }

        try {
            val isInvalidToken = !jwtProvider.isValidToken(token)

            if (isInvalidToken) {
                request.setAttribute(ATTRIBUTE_KEY, INVALID_TOKEN.message)
                filterChain.doFilter(request, response)
                return
            }

        } catch (_: ExpiredJwtException) {
            request.setAttribute(ATTRIBUTE_KEY, EXPIRED_TOKEN.message)
            filterChain.doFilter(request, response)
            return
        }

        try {
            val tokenId: String? = jwtProvider.getTokenId(token)

            if (tokenId != null && accessTokenBlacklistPort.isBlacklisted(tokenId)) {
                request.setAttribute(ATTRIBUTE_KEY, LOGGED_OUT_TOKEN.message)
                filterChain.doFilter(request, response)
                return
            }

            val id: String = jwtProvider.getId(token)
            val userDetails: UserDetails = userDetailsService.loadUserById(id)
            val usernamePasswordAuthenticationToken =
                UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)
            SecurityContextHolder.getContext().authentication = usernamePasswordAuthenticationToken
        } catch (_: ExpiredJwtException) {
            request.setAttribute(ATTRIBUTE_KEY, EXPIRED_TOKEN.message)
            filterChain.doFilter(request, response)
            return
        } catch (_: NotFoundException) {
            request.setAttribute(ATTRIBUTE_KEY, INVALID_TOKEN.message)
            filterChain.doFilter(request, response)
            return
        }

        filterChain.doFilter(request, response)
    }
}
