package com.krince.reminisce.infra.adapter.out.auth

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class GoogleTokenResponse(
    @param:JsonProperty("access_token")
    val accessToken: String?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GoogleUserResponse(
    @param:JsonProperty("sub")
    val sub: String?,

    @param:JsonProperty("email")
    val email: String?,

    @param:JsonProperty("name")
    val name: String?,
)
