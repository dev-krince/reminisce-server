package com.krince.reminisce.infra.swagger

import com.krince.reminisce.shared.response.ExceptionResponseCode


@Retention(AnnotationRetention.RUNTIME)
annotation class ExceptionExample(
    val code: ExceptionResponseCode,
    val name: String,
    val message: String,
    val description: String,
)