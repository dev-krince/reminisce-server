package com.krince.reminisce.infra.adapter.out.auth

import com.krince.reminisce.application.port.out.auth.NaverOAuthPort
import com.krince.reminisce.application.port.out.auth.NaverUserInfo
import com.krince.reminisce.infra.config.properties.NaverOAuthProperties
import com.krince.reminisce.shared.exception.SocialAuthException
import com.krince.reminisce.shared.response.ExceptionResponseCode.SOCIAL_AUTH_FAILED
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException

@Component
@ConditionalOnProperty(name = ["oauth.naver.mode"], havingValue = "real", matchIfMissing = true)
@EnableConfigurationProperties(NaverOAuthProperties::class)
class NaverOAuthAdapter(
    private val naverOAuthProperties: NaverOAuthProperties,
    restClientBuilder: RestClient.Builder,
) : NaverOAuthPort {

    private val restClient: RestClient = restClientBuilder.build()

    override fun exchangeCodeForUser(authorizationCode: String, state: String): NaverUserInfo {
        val accessToken: String = requestAccessToken(authorizationCode, state)

        return requestUserInfo(accessToken)
    }

    private fun requestAccessToken(authorizationCode: String, state: String): String {
        val tokenResponse: NaverTokenResponse = try {
            restClient.post()
                .uri(naverOAuthProperties.tokenUri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(buildTokenRequestBody(authorizationCode, state))
                .retrieve()
                .body(NaverTokenResponse::class.java)
                ?: throw SocialAuthException(SOCIAL_AUTH_FAILED)
        } catch (cause: RestClientException) {
            throw SocialAuthException(exceptionResponseCode = SOCIAL_AUTH_FAILED, cause = cause)
        }

        return tokenResponse.accessToken?.takeIf { it.isNotBlank() }
            ?: throw SocialAuthException(SOCIAL_AUTH_FAILED)
    }

    private fun requestUserInfo(accessToken: String): NaverUserInfo {
        val userResponse: NaverUserResponse = try {
            restClient.get()
                .uri(naverOAuthProperties.userInfoUri)
                .header(AUTHORIZATION_HEADER, "$BEARER_PREFIX$accessToken")
                .retrieve()
                .body(NaverUserResponse::class.java)
                ?: throw SocialAuthException(SOCIAL_AUTH_FAILED)
        } catch (cause: RestClientException) {
            throw SocialAuthException(exceptionResponseCode = SOCIAL_AUTH_FAILED, cause = cause)
        }

        return toUserInfo(userResponse)
    }

    private fun toUserInfo(userResponse: NaverUserResponse): NaverUserInfo {
        val detail: NaverUserDetail = userResponse.response
            ?: throw SocialAuthException(SOCIAL_AUTH_FAILED)
        val providerId: String = detail.id?.takeIf { it.isNotBlank() }
            ?: throw SocialAuthException(SOCIAL_AUTH_FAILED)
        val nickname: String = resolveNickname(detail)

        return NaverUserInfo(
            id = providerId,
            email = detail.email?.takeIf { it.isNotBlank() },
            nickname = nickname,
        )
    }

    private fun resolveNickname(detail: NaverUserDetail): String {
        detail.nickname?.takeIf { it.isNotBlank() }?.let { return it }
        detail.name?.takeIf { it.isNotBlank() }?.let { return it }

        return DEFAULT_NICKNAME
    }

    private fun buildTokenRequestBody(authorizationCode: String, state: String): MultiValueMap<String, String> {
        val body: MultiValueMap<String, String> = LinkedMultiValueMap()
        body.add(GRANT_TYPE_KEY, AUTHORIZATION_CODE_GRANT_TYPE)
        body.add(CLIENT_ID_KEY, naverOAuthProperties.clientId)
        body.add(CLIENT_SECRET_KEY, naverOAuthProperties.clientSecret)
        body.add(CODE_KEY, authorizationCode)
        body.add(STATE_KEY, state)

        return body
    }

    companion object {
        private const val AUTHORIZATION_HEADER = "Authorization"
        private const val BEARER_PREFIX = "Bearer "
        private const val GRANT_TYPE_KEY = "grant_type"
        private const val AUTHORIZATION_CODE_GRANT_TYPE = "authorization_code"
        private const val CLIENT_ID_KEY = "client_id"
        private const val CLIENT_SECRET_KEY = "client_secret"
        private const val CODE_KEY = "code"
        private const val STATE_KEY = "state"
        private const val DEFAULT_NICKNAME = "네이버회원"
    }
}
