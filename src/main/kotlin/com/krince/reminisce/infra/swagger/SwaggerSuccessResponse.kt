package com.krince.reminisce.infra.swagger

import com.krince.reminisce.shared.response.SuccessResponseCode


@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class SwaggerSuccessResponse(
    val responseCode: SuccessResponseCode,
    val description: String = ""
)