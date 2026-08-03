package com.krince.reminisce.infra.adapter.out.auth

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class KakaoTokenResponse(
    @param:JsonProperty("access_token")
    val accessToken: String?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class KakaoUserResponse(
    @param:JsonProperty("id")
    val id: Long?,

    @param:JsonProperty("kakao_account")
    val kakaoAccount: KakaoAccount?,

    @param:JsonProperty("properties")
    val properties: KakaoProperties?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class KakaoAccount(
    @param:JsonProperty("email")
    val email: String?,

    @param:JsonProperty("profile")
    val profile: KakaoProfile?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class KakaoProfile(
    @param:JsonProperty("nickname")
    val nickname: String?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class KakaoProperties(
    @param:JsonProperty("nickname")
    val nickname: String?,
)
