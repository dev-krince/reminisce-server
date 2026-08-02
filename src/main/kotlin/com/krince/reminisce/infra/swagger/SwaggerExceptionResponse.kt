package com.krince.reminisce.infra.swagger

import com.krince.reminisce.shared.response.ExceptionResponseCode


@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class SwaggerExceptionResponse(
    val value: Array<ExceptionResponseCode> = [],
    val examples: Array<ExceptionExample> = [],
)