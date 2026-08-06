package com.krince.reminisce.infra.config

import com.krince.reminisce.infra.security.CustomAccessDeniedHandler
import com.krince.reminisce.infra.security.CustomAuthenticationEntryPoint
import com.krince.reminisce.infra.security.JwtAuthFilter
import jakarta.servlet.http.HttpServletRequest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod.*
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, prePostEnabled = true)
class SecurityConfig(
    private val authenticationEntryPoint: CustomAuthenticationEntryPoint,
    private val accessDeniedHandler: CustomAccessDeniedHandler,
    private val jwtAuthFilter: JwtAuthFilter,
) {
    private val permitAllUrls = listOf(
        "/swagger-ui/**",
        "/v3/api-docs/**",
        "/favicon.ico",
        "/actuator/**",
        "/files/**",
    )
    private val permitAllGetUrls = listOf<String>()
    private val permitAllPostUrls = listOf(
        "/api/auth/tokens/kakao",
        "/api/auth/tokens/google",
        "/api/auth/tokens/refresh",
    )
    private val permitAllPutUrls = listOf<String>()
    private val permitAllPatchUrls = listOf<String>()
    private val permitAllDeleteUrls = listOf(
        "/api/auth/tokens",
    )

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain = http
        .authorizeHttpRequests {
            it.requestMatchers(*permitAllUrls.toTypedArray()).permitAll()
            it.requestMatchers(GET, *permitAllGetUrls.toTypedArray()).permitAll()
            it.requestMatchers(POST, *permitAllPostUrls.toTypedArray()).permitAll()
            it.requestMatchers(PUT, *permitAllPutUrls.toTypedArray()).permitAll()
            it.requestMatchers(PATCH, *permitAllPatchUrls.toTypedArray()).permitAll()
            it.requestMatchers(DELETE, *permitAllDeleteUrls.toTypedArray()).permitAll()
            it.anyRequest().authenticated()
        }
        .httpBasic { obj: HttpBasicConfigurer<HttpSecurity> -> obj.disable() }
        .formLogin { obj: FormLoginConfigurer<HttpSecurity> -> obj.disable() }
        .csrf { obj: CsrfConfigurer<HttpSecurity> -> obj.disable() }
        .cors { cors: CorsConfigurer<HttpSecurity> -> this.corsConfigSetting(cors) }
        .sessionManagement { sessionManagement: SessionManagementConfigurer<HttpSecurity> ->
            this.stateless(
                sessionManagement
            )
        }
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)
        .exceptionHandling { exceptionHandling: ExceptionHandlingConfigurer<HttpSecurity> ->
            this.authExceptionHandler(
                exceptionHandling
            )
        }
        .orBuild

    private fun corsConfigSetting(cors: CorsConfigurer<HttpSecurity>) {
        val allowedOriginEnvironmentName = "CORS_ALLOWED_ORIGINS"
        val delimiter = ","
        val allowedOriginList: List<String> = System.getenv(allowedOriginEnvironmentName)
            ?.takeIf { it.isNotBlank() }
            ?.split(delimiter)
            ?.map { it.trim() }
            ?: listOf("*")

        val allowedMethodList = listOf("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")

        val corsConfigSource = CorsConfigurationSource { _: HttpServletRequest ->
            CorsConfiguration().apply {
                allowedOriginPatterns = allowedOriginList
                allowedMethods = allowedMethodList
                allowedHeaders = listOf("*")
                exposedHeaders = listOf("Authorization", "refreshToken")
                allowCredentials = true
                maxAge = 3600L
            }
        }

        cors.configurationSource(corsConfigSource)
    }

    private fun stateless(sessionManagementConfigurer: SessionManagementConfigurer<HttpSecurity>) =
        sessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS)

    private fun authExceptionHandler(exceptionHandling: ExceptionHandlingConfigurer<HttpSecurity>) =
        exceptionHandling.authenticationEntryPoint(authenticationEntryPoint)
            .accessDeniedHandler(accessDeniedHandler)
}