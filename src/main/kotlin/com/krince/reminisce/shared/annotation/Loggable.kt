package com.krince.reminisce.shared.annotation

import org.springframework.stereotype.Component

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Component
annotation class Loggable(
    val level: LogLevel = LogLevel.INFO,
    val includeArgs: Boolean = true,
    val includeResult: Boolean = true,
    val includeExecutionTime: Boolean = true
) {
    enum class LogLevel {
        TRACE, DEBUG, INFO, WARN, ERROR
    }
}
