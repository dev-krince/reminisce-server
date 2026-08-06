package com.krince.reminisce.infra.adapter.out.auth

import com.krince.reminisce.application.port.out.auth.GoogleOAuthPort
import com.krince.reminisce.application.port.out.auth.GoogleUserInfo
import com.krince.reminisce.infra.config.properties.GoogleOAuthProperties
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
@EnableConfigurationProperties(GoogleOAuthProperties::class)
class GoogleOAuthAdapter(
    private val googleOAuthProperties: GoogleOAuthProperties,
    restClientBuilder: RestClient.Builder,
) : GoogleOAuthPort {

    private val restClient: RestClient = restClientBuilder.build()

    override fun exchangeCodeForUser(authorizationCode: String): GoogleUserInfo {
        val accessToken: String = requestAccessToken(authorizationCode)

        return requestUserInfo(accessToken)
    }

    private fun requestAccessToken(authorizationCode: String): String {
        val tokenResponse: GoogleTokenResponse = try {
            restClient.post()
                .uri(googleOAuthProperties.tokenUri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(buildTokenRequestBody(authorizationCode))
                .retrieve()
                .body(GoogleTokenResponse::class.java)
                ?: throw SocialAuthException(SOCIAL_AUTH_FAILED)
        } catch (cause: RestClientException) {
            throw SocialAuthException(exceptionResponseCode = SOCIAL_AUTH_FAILED, cause = cause)
        }

        return tokenResponse.accessToken?.takeIf { it.isNotBlank() }
            ?: throw SocialAuthException(SOCIAL_AUTH_FAILED)
    }

    private fun requestUserInfo(accessToken: String): GoogleUserInfo {
        val userResponse: GoogleUserResponse = try {
            restClient.get()
                .uri(googleOAuthProperties.userInfoUri)
                .header(AUTHORIZATION_HEADER, "$BEARER_PREFIX$accessToken")
                .retrieve()
                .body(GoogleUserResponse::class.java)
                ?: throw SocialAuthException(SOCIAL_AUTH_FAILED)
        } catch (cause: RestClientException) {
            throw SocialAuthException(exceptionResponseCode = SOCIAL_AUTH_FAILED, cause = cause)
        }

        return toUserInfo(userResponse)
    }

    private fun toUserInfo(response: GoogleUserResponse): GoogleUserInfo {
        val providerId: String = response.sub?.takeIf { it.isNotBlank() }
            ?: throw SocialAuthException(SOCIAL_AUTH_FAILED)
        val nickname: String = response.name?.takeIf { it.isNotBlank() } ?: DEFAULT_NICKNAME

        return GoogleUserInfo(
            id = providerId,
            email = response.email?.takeIf { it.isNotBlank() },
            nickname = nickname,
        )
    }

    private fun buildTokenRequestBody(authorizationCode: String): MultiValueMap<String, String> {
        val body: MultiValueMap<String, String> = LinkedMultiValueMap()
        body.add(GRANT_TYPE_KEY, AUTHORIZATION_CODE_GRANT_TYPE)
        body.add(CLIENT_ID_KEY, googleOAuthProperties.clientId)
        body.add(CLIENT_SECRET_KEY, googleOAuthProperties.clientSecret)
        body.add(REDIRECT_URI_KEY, googleOAuthProperties.redirectUri)
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
        private const val DEFAULT_NICKNAME = "구글회원"
    }
}
