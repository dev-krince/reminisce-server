package com.krince.boilerplate.infra.config

import com.krince.boilerplate.infra.config.properties.FileStorageProperties
import com.krince.boilerplate.infra.interceptor.LoggingInterceptor
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.io.File

@Configuration
@EnableConfigurationProperties(FileStorageProperties::class)
class WebMvcConfig(
    private val loggingInterceptor: LoggingInterceptor,
    private val fileStorageProperties: FileStorageProperties,
) : WebMvcConfigurer {

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        val absolutePath = File(fileStorageProperties.path).absolutePath

        registry.addResourceHandler("/files/**")
            .addResourceLocations("file:$absolutePath/")
            .setCachePeriod(fileStorageProperties.cachePeriod)
    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(loggingInterceptor)
            .addPathPatterns("/**")
            .excludePathPatterns(
                "/swagger-ui/**",
                "/v3/api-docs/**",
                "/actuator/**",
                "/favicon.ico",
            )
    }
}