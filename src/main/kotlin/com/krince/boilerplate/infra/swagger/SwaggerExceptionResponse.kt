package com.krince.boilerplate.infra.swagger

import com.krince.boilerplate.shared.response.ExceptionResponseCode


@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class SwaggerExceptionResponse(
    val value: Array<ExceptionResponseCode> = [],
    val examples: Array<ExceptionExample> = [],
)