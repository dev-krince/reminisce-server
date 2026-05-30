package com.krince.boilerplate.infra.swagger

import com.krince.boilerplate.shared.response.ExceptionResponseCode


@Retention(AnnotationRetention.RUNTIME)
annotation class ExceptionExample(
    val code: ExceptionResponseCode,
    val name: String,
    val message: String,
    val description: String,
)