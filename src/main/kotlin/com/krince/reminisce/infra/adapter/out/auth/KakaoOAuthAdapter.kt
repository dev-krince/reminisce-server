package com.krince.reminisce.infra.adapter.out.auth

import com.krince.reminisce.application.port.out.auth.KakaoOAuthPort
import com.krince.reminisce.application.port.out.auth.KakaoUserInfo
import com.krince.reminisce.infra.config.properties.KakaoOAuthProperties
import com.krince.reminisce.shared.exception.SocialAuthException
import com.krince.reminisce.shared.response.ExceptionResponseCode.SOCIAL_AUTH_FAILED
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException

@Component
@EnableConfigurationProperties(KakaoOAuthProperties::class)
class KakaoOAuthAdapter(
    private val kakaoOAuthProperties: KakaoOAuthProperties,
    restClientBuilder: RestClient.Builder,
) : KakaoOAuthPort {

    private val restClient: RestClient = restClientBuilder.build()

    override fun exchangeCodeForUser(authorizationCode: String): KakaoUserInfo {
        val accessToken: String = requestAccessToken(authorizationCode)

        return requestUserInfo(accessToken)
    }

    private fun requestAccessToken(authorizationCode: String): String {
        val tokenResponse: KakaoTokenResponse = try {
            restClient.post()
                .uri(kakaoOAuthProperties.tokenUri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(buildTokenRequestBody(authorizationCode))
                .retrieve()
                .body(KakaoTokenResponse::class.java)
                ?: throw SocialAuthException(SOCIAL_AUTH_FAILED)
        } catch (cause: RestClientException) {
            throw SocialAuthException(exceptionResponseCode = SOCIAL_AUTH_FAILED, cause = cause)
        }

        return tokenResponse.accessToken?.takeIf { it.isNotBlank() }
            ?: throw SocialAuthException(SOCIAL_AUTH_FAILED)
    }

    private fun requestUserInfo(accessToken: String): KakaoUserInfo {
        val userResponse: KakaoUserResponse = try {
            restClient.get()
                .uri(kakaoOAuthProperties.userInfoUri)
                .header(AUTHORIZATION_HEADER, "$BEARER_PREFIX$accessToken")
                .retrieve()
                .body(KakaoUserResponse::class.java)
                ?: throw SocialAuthException(SOCIAL_AUTH_FAILED)
        } catch (cause: RestClientException) {
            throw SocialAuthException(exceptionResponseCode = SOCIAL_AUTH_FAILED, cause = cause)
        }

        return toUserInfo(userResponse)
    }

    private fun toUserInfo(response: KakaoUserResponse): KakaoUserInfo {
        val providerId: String = response.id?.toString()?.takeIf { it.isNotBlank() }
            ?: throw SocialAuthException(SOCIAL_AUTH_FAILED)
        val nickname: String = resolveNickname(response)

        return KakaoUserInfo(
            id = providerId,
            email = response.kakaoAccount?.email?.takeIf { it.isNotBlank() },
            nickname = nickname,
        )
    }

    private fun resolveNickname(response: KakaoUserResponse): String {
        val profileNickname: String? = response.kakaoAccount?.profile?.nickname?.takeIf { it.isNotBlank() }
        val propertiesNickname: String? = response.properties?.nickname?.takeIf { it.isNotBlank() }

        return profileNickname ?: propertiesNickname ?: DEFAULT_NICKNAME
    }

    private fun buildTokenRequestBody(authorizationCode: String): MultiValueMap<String, String> {
        val body: MultiValueMap<String, String> = LinkedMultiValueMap()
        body.add(GRANT_TYPE_KEY, AUTHORIZATION_CODE_GRANT_TYPE)
        body.add(CLIENT_ID_KEY, kakaoOAuthProperties.clientId)
        body.add(CLIENT_SECRET_KEY, kakaoOAuthProperties.clientSecret)
        body.add(REDIRECT_URI_KEY, kakaoOAuthProperties.redirectUri)
        body.add(CODE_KEY, authorizationCode)

        return body
    }

    companion object {
        private const val AUTHORIZATION_HEADER = "Authorization"
        private const val BEARER_PREFIX = "Bearer "
        private const val GRANT_TYPE_KEY = "grant_type"
        private const val AUTHORIZATION_CODE_GRANT_TYPE = "authorization_code"
        private const val CLIENT_ID_KEY = "client_id"
        private const val CLIENT_SECRET_KEY = "client_secret"
        private const val REDIRECT_URI_KEY = "redirect_uri"
        private const val CODE_KEY = "code"
        private const val DEFAULT_NICKNAME = "카카오회원"
    }
}
