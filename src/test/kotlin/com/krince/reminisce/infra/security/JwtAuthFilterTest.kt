package com.krince.reminisce.infra.security

import com.krince.reminisce.application.port.out.auth.AccessTokenBlacklistPort
import com.krince.reminisce.shared.response.ExceptionResponseCode.LOGGED_OUT_TOKEN
import io.kotest.core.annotation.DisplayName
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import java.util.Base64
import java.util.Date

@Tags("test", "unitTest")
@DisplayName("JwtAuthFilter 단위테스트")
class JwtAuthFilterTest : FunSpec({

    val secretKeyBase64 = Base64.getEncoder().encodeToString("01234567890123456789012345678901".toByteArray())
    val accessExpired = 86400000L
    val refreshExpired = 1209600000L
    val jwtProvider = JwtProvider(secretKeyBase64, accessExpired, refreshExpired)

    val userDetailsService = mockk<CustomUserDetailsService>()
    val accessTokenBlacklistPort = mockk<AccessTokenBlacklistPort>()
    val filter = JwtAuthFilter(jwtProvider, userDetailsService, accessTokenBlacklistPort)

    val userId = "a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d"
    val role = "ROLE_USER"
    val attributeKey = "exceptionMessage"

    val doFilterInternal = JwtAuthFilter::class.java.getDeclaredMethod(
        "doFilterInternal",
        HttpServletRequest::class.java,
        HttpServletResponse::class.java,
        FilterChain::class.java,
    ).apply { isAccessible = true }

    fun invokeFilter(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        doFilterInternal.invoke(filter, request, response, filterChain)
    }

    fun String.rawToken(): String = removePrefix("Bearer ").trim()

    beforeTest { clearMocks(userDetailsService, accessTokenBlacklistPort) }

    afterTest { SecurityContextHolder.clearContext() }

    context("블랙리스트에 없는 유효 토큰") {
        test("SecurityContext에 인증을 설정하고 doFilter로 통과시킨다") {
            SecurityContextHolder.clearContext()
            val bearer = jwtProvider.createAccessToken(userId, role)
            val tokenId = requireNotNull(jwtProvider.getTokenId(bearer.rawToken()))
            val request = mockk<HttpServletRequest>(relaxed = true)
            val response = mockk<HttpServletResponse>(relaxed = true)
            val filterChain = mockk<FilterChain>(relaxed = true)
            every { request.getHeader("Authorization") } returns bearer
            every { accessTokenBlacklistPort.isBlacklisted(tokenId) } returns false
            every { userDetailsService.loadUserById(userId) } returns CustomUserDetails(id = userId, role = role)

            invokeFilter(request, response, filterChain)

            SecurityContextHolder.getContext().authentication.shouldNotBeNull()
            verify(exactly = 1) { filterChain.doFilter(request, response) }
        }
    }

    context("jti가 없는 유효 토큰") {
        test("블랙리스트를 조회하지 않고 SecurityContext에 인증을 설정한다") {
            SecurityContextHolder.clearContext()
            val secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKeyBase64))
            val now = Date()
            val rawWithoutJti = Jwts.builder()
                .subject(userId)
                .issuedAt(now)
                .expiration(Date(now.time + accessExpired))
                .claim("role", role)
                .claim("tokenType", "accessToken")
                .signWith(secretKey)
                .compact()
            val request = mockk<HttpServletRequest>(relaxed = true)
            val response = mockk<HttpServletResponse>(relaxed = true)
            val filterChain = mockk<FilterChain>(relaxed = true)
            every { request.getHeader("Authorization") } returns "Bearer $rawWithoutJti"
            every { userDetailsService.loadUserById(userId) } returns CustomUserDetails(id = userId, role = role)

            invokeFilter(request, response, filterChain)

            SecurityContextHolder.getContext().authentication.shouldNotBeNull()
            verify(exactly = 1) { filterChain.doFilter(request, response) }
            verify(exactly = 0) { accessTokenBlacklistPort.isBlacklisted(any()) }
        }
    }

    context("블랙리스트에 있는 유효 토큰") {
        test("SecurityContext를 설정하지 않고 LOGGED_OUT_TOKEN 메시지를 요청 속성에 넣어 통과시킨다") {
            SecurityContextHolder.clearContext()
            val bearer = jwtProvider.createAccessToken(userId, role)
            val tokenId = requireNotNull(jwtProvider.getTokenId(bearer.rawToken()))
            val request = mockk<HttpServletRequest>(relaxed = true)
            val response = mockk<HttpServletResponse>(relaxed = true)
            val filterChain = mockk<FilterChain>(relaxed = true)
            val attributeValue = slot<Any>()
            every { request.getHeader("Authorization") } returns bearer
            every { accessTokenBlacklistPort.isBlacklisted(tokenId) } returns true
            every { request.setAttribute(attributeKey, capture(attributeValue)) } returns Unit

            invokeFilter(request, response, filterChain)

            SecurityContextHolder.getContext().authentication shouldBe null
            attributeValue.captured shouldBe LOGGED_OUT_TOKEN.message
            verify(exactly = 1) { filterChain.doFilter(request, response) }
            verify(exactly = 0) { userDetailsService.loadUserById(any()) }
        }
    }
})
