package com.krince.reminisce.infra.security

import com.krince.reminisce.shared.exception.UnauthorizedRefreshTokenException
import com.krince.reminisce.shared.response.ExceptionResponseCode.*
import com.krince.reminisce.shared.util.UuidGenerator
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jws
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtProvider(
    @Value("\${jwt.secret.key}") secretKey: String,
    @Value("\${jwt.access-token-expired}") accessTokenExpired: Long,
    @Value("\${jwt.refresh-token-expired}") refreshTokenExpired: Long,
) {
    private final val keyBytes = Decoders.BASE64.decode(secretKey)
    private final val secretKey: SecretKey = Keys.hmacShaKeyFor(keyBytes)
    private final val ACCESS_TOKEN_EXPIRED: Long = accessTokenExpired
    private final val REFRESH_TOKEN_EXPIRED: Long = refreshTokenExpired

    companion object {
        private const val TOKEN_PREFIX = "Bearer "
        private const val ROLE = "role"
        private const val TOKEN_TYPE = "tokenType"
        private const val ACCESS_TOKEN_HEADER_NAME = "Authorization"
        private const val REFRESH_TOKEN_HEADER_NAME = "refreshToken"
        private const val REFRESH_TOKEN_TYPE = "refreshToken"
    }

    fun createAccessToken(id: String, role: String): String {
        val now = Date()
        val validity = Date(now.time + ACCESS_TOKEN_EXPIRED)
        val tokenTypeValue = "accessToken"

        return TOKEN_PREFIX + Jwts.builder()
            .subject(id)
            .id(UuidGenerator.generate())
            .issuedAt(now)
            .expiration(validity)
            .claim(ROLE, role)
            .claim(TOKEN_TYPE, tokenTypeValue)
            .signWith(secretKey)
            .compact()
    }

    fun createRefreshToken(id: String, role: String): String {
        val now = Date()
        val validity = Date(now.time + REFRESH_TOKEN_EXPIRED)
        val tokenTypeValue = "refreshToken"

        return TOKEN_PREFIX + Jwts.builder()
            .subject(id)
            .id(UuidGenerator.generate())
            .issuedAt(now)
            .expiration(validity)
            .claim(ROLE, role)
            .claim(TOKEN_TYPE, tokenTypeValue)
            .signWith(secretKey)
            .compact()
    }

    fun isValidToken(token: String): Boolean {
        return try {
            getClaimsJws(token).payload
                .expiration
                .after(Date())
        } catch (exception: ExpiredJwtException) {
            throw exception
        } catch (_: JwtException) {
            false
        } catch (_: IllegalArgumentException) {
            false
        }
    }

    fun validateRefreshToken(token: String) {
        try {
            val claims: Claims = getClaimsJws(token).payload
            val tokenType = claims.get(TOKEN_TYPE, String::class.java)

            require(tokenType == REFRESH_TOKEN_TYPE) { throw UnauthorizedRefreshTokenException(
                UNAUTHORIZED_REFRESH_TOKEN, UNAUTHORIZED_REFRESH_TOKEN.message) }

            claims.expiration.after(Date())
        } catch (_: ExpiredJwtException) {
            throw UnauthorizedRefreshTokenException(EXPIRED_REFRESH_TOKEN, EXPIRED_REFRESH_TOKEN.message)
        } catch (_: JwtException) {
            throw UnauthorizedRefreshTokenException(INVALID_REFRESH_TOKEN, INVALID_REFRESH_TOKEN.message)
        } catch (_: IllegalArgumentException) {
            throw UnauthorizedRefreshTokenException(INVALID_REFRESH_TOKEN, INVALID_REFRESH_TOKEN.message)
        }
    }

    fun getRefreshTokenExpiration(): Duration = Duration.ofMillis(REFRESH_TOKEN_EXPIRED)

    fun getTokenId(token: String): String? = getClaimsJws(token)
        .payload
        .id

    fun getRemainingExpiration(token: String): Duration {
        val expiration: Instant = getClaimsJws(token).payload.expiration.toInstant()
        val remaining: Duration = Duration.between(Instant.now(), expiration)

        if (remaining.isNegative) return Duration.ZERO

        return remaining
    }

    fun getId(token: String): String = getClaimsJws(token)
        .payload
        .subject

    fun getRole(token: String): String = getClaimsJws(token)
        .payload
        .get(ROLE, String::class.java)

    fun getAccessTokenFromRequest(request: HttpServletRequest): String? =
        request.getHeader(ACCESS_TOKEN_HEADER_NAME)
            ?.takeIf { it.isNotBlank() }

    fun getUserIdFromRequest(request: HttpServletRequest): String? =
        getAccessTokenFromRequest(request)
            ?.let { getId(it) }

    private fun getClaimsJws(token: String): Jws<Claims> = Jwts.parser()
        .verifyWith(secretKey)
        .build()
        .parseSignedClaims(token)

    fun extractToken(token: String): String {
        require(!isInvalidTokenType(token)) { INVALID_TOKEN.message }

        return token.substring(TOKEN_PREFIX.length)
    }

    fun getTokenType(token: String): String {
        require(isValidToken(token)) { INVALID_TOKEN.message }

        return getClaimsJws(token).payload
            .get(TOKEN_TYPE, String::class.java)
    }

    private fun isInvalidTokenType(token: String): Boolean = !token.startsWith(TOKEN_PREFIX)
}