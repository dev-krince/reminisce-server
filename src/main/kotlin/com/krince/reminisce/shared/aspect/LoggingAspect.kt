package com.krince.reminisce.shared.aspect

import com.krince.reminisce.shared.annotation.Loggable
import com.krince.reminisce.shared.annotation.Loggable.LogLevel
import io.github.oshai.kotlinlogging.KotlinLogging
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.stereotype.Component

@Aspect
@Component
class LoggingAspect {

    private val logger = KotlinLogging.logger {}

    // 1. @Loggable 어노테이션이 있는 메서드/클래스
    @Around("@annotation(loggable) || @within(loggable)")
    fun logAnnotatedMethod(joinPoint: ProceedingJoinPoint, loggable: Loggable): Any? {
        return executeWithLogging(joinPoint, loggable)
    }

    // 2. 비즈니스 로직 자동 로깅
    @Around(//TODO 프로젝트에 맞게 변경
        """
         (execution(* com.krince.reminisce.application.service..*.*(..)) ||
         execution(* com.krince.reminisce.application.port..*.*(..)) ||
         execution(* com.krince.reminisce.infra.adapter.out.persistence..*.*(..)) ||
         execution(* com.krince.reminisce.infra.adapter.in.controller..*.*(..)))
        && !@annotation(com.krince.reminisce.shared.annotation.Loggable) 
        && !@within(com.krince.reminisce.shared.annotation.Loggable)
        && !execution(* org.springframework..*.*(..))
        && !execution(* org.hibernate..*.*(..))
        && !execution(* com.querydsl..*.*(..))
    """
    )
    fun logBusinessMethods(joinPoint: ProceedingJoinPoint): Any? {
        val defaultLoggable = Loggable(
            level = LogLevel.DEBUG,
            includeArgs = true,
            includeResult = false,
            includeExecutionTime = true
        )

        return executeWithLogging(joinPoint, defaultLoggable)
    }

    private fun executeWithLogging(joinPoint: ProceedingJoinPoint, loggable: Loggable): Any? {
        val methodInfo = extractMethodInfo(joinPoint)
        val startTime = System.currentTimeMillis()

        logMethodStart(methodInfo, joinPoint.args, loggable)

        return try {
            val result = joinPoint.proceed()
            val duration = System.currentTimeMillis() - startTime
            logMethodSuccess(methodInfo, result, duration, loggable)
            result
        } catch (exception: Exception) {
            val duration = System.currentTimeMillis() - startTime
            logMethodError(methodInfo, exception, duration)
            throw exception
        }
    }

    private fun extractMethodInfo(joinPoint: ProceedingJoinPoint): MethodInfo {
        val methodSignature = joinPoint.signature as MethodSignature
        val method = methodSignature.method
        val className = joinPoint.target.javaClass.simpleName
        val methodName = method.name

        return MethodInfo(
            className = className,
            methodName = methodName,
            fullName = "$className.$methodName"
        )
    }

    private fun logMethodStart(methodInfo: MethodInfo, args: Array<Any?>, loggable: Loggable) {
        if (!loggable.includeArgs) return

        val message = buildString {
            append("Method started: ${methodInfo.fullName}")
            if (args.isNotEmpty()) {
                append(" - Args: ${formatArgs(args)}")
            }
        }
        logMessage(message, loggable.level)
    }

    private fun logMethodSuccess(methodInfo: MethodInfo, result: Any?, duration: Long, loggable: Loggable) {
        val message = buildString {
            append("Method completed: ${methodInfo.fullName}")
            if (loggable.includeExecutionTime) {
                append(" - Duration: ${duration}ms")
            }
            if (loggable.includeResult && result != null) {
                append(" - Result: ${formatValue(result)}")
            }
        }
        logMessage(message, loggable.level)
    }

    private fun logMethodError(methodInfo: MethodInfo, ex: Exception, duration: Long) {
        logger.debug { "Method failed: ${methodInfo.fullName} - Duration: ${duration}ms - Exception: ${ex.message}" }
    }

    private fun logMessage(message: String, level: LogLevel) {
        when (level) {
            LogLevel.TRACE -> logger.trace { message }
            LogLevel.DEBUG -> logger.debug { message }
            LogLevel.INFO -> logger.info { message }
            LogLevel.WARN -> logger.warn { message }
            LogLevel.ERROR -> logger.error { message }
        }
    }

    private fun formatArgs(args: Array<Any?>): String {
        return args.joinToString(", ") { formatValue(it) }
    }

    private fun formatValue(value: Any?): String {
        return when (value) {
            null -> "null"
            is String -> "\"$value\""
            is Collection<*> -> "[${value.size} items]"
            is Map<*, *> -> "{${value.size} entries}"
            is Array<*> -> "[${value.size} items]"
            else -> value.toString()
        }
    }

    // 데이터 클래스로 메서드 정보 캡슐화
    private data class MethodInfo(
        val className: String,
        val methodName: String,
        val fullName: String
    )
}