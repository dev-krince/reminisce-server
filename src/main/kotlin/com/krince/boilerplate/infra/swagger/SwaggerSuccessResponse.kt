package com.krince.boilerplate.infra.swagger

import com.krince.boilerplate.shared.response.SuccessResponseCode


@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class SwaggerSuccessResponse(
    val responseCode: SuccessResponseCode,
    val description: String = ""
)