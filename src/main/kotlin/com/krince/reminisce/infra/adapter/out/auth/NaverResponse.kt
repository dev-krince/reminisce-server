package com.krince.reminisce.infra.adapter.out.auth

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class NaverTokenResponse(
    @param:JsonProperty("access_token")
    val accessToken: String?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class NaverUserResponse(
    @param:JsonProperty("resultcode")
    val resultcode: String?,

    @param:JsonProperty("message")
    val message: String?,

    @param:JsonProperty("response")
    val response: NaverUserDetail?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class NaverUserDetail(
    @param:JsonProperty("id")
    val id: String?,

    @param:JsonProperty("email")
    val email: String?,

    @param:JsonProperty("nickname")
    val nickname: String?,

    @param:JsonProperty("name")
    val name: String?,
)
