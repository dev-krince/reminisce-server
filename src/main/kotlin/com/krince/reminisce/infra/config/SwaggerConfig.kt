package com.krince.reminisce.infra.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.krince.reminisce.infra.swagger.ExampleHolder
import com.krince.reminisce.infra.swagger.ExceptionExample
import com.krince.reminisce.infra.swagger.SwaggerExceptionResponse
import com.krince.reminisce.infra.swagger.SwaggerSuccessResponse
import com.krince.reminisce.shared.response.ExceptionResponseCode
import com.krince.reminisce.shared.response.PageResponse
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.examples.Example
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.Encoding
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.media.Schema as MediaSchema
import io.swagger.v3.oas.models.responses.ApiResponse
import io.swagger.v3.oas.models.responses.ApiResponses
import io.swagger.v3.oas.models.parameters.RequestBody
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springdoc.core.customizers.OperationCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType as SpringMediaType
import org.springframework.web.method.HandlerMethod
import org.springframework.web.bind.annotation.RequestPart
import java.lang.reflect.ParameterizedType

@Configuration
class SwaggerConfig(
    private val swaggerExceptionResponseConfig: SwaggerExceptionResponseConfig
) {

    companion object {
        private const val VERSION = "0.0.1"
        private const val PROJECT_TITLE_NAME = "Reminisce API"
        private const val SWAGGER_DESCRIPTION = "Reminisce API 인터페이스 명세서"
        private const val AUTH_SCHEME_NAME = "Authorization"
    }

    @Bean
    fun openAPI(): OpenAPI {
        val components = swaggerExceptionResponseConfig.getExceptionResponseComponents()
        components.addSecuritySchemes(AUTH_SCHEME_NAME, createSecurityScheme())

        return OpenAPI()
            .components(components)
            .addSecurityItem(createSecurityRequirement())
            .info(
                Info()
                    .version(VERSION)
                    .title(PROJECT_TITLE_NAME)
                    .description(SWAGGER_DESCRIPTION)
            )
            .servers(createServers())
    }

    private fun createServers(): List<Server> {
        return listOf(
            Server()
                .url("http://localhost:8080")
                .description("로컬 서버"),
            Server()
                .url("https://test.pcntv.net:8070")
                .description("개발 서버"),
            Server()
                .url("https://test.pcntv.net:8060")
                .description("스테이징 서버"),
            Server()
                .url("https://test.pcntv.net:8050")
                .description("운영 서버"),
        )
    }

    private fun createSecurityScheme(): SecurityScheme {
        return SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")
            .description("JWT 액세스 토큰을 Authorization 헤더(Bearer)로 전달합니다. 로그인 API(/api/auth/tokens) 호출 후 발급받은 토큰을 우측 상단 'Authorize' 버튼에 입력하면 모든 API 요청에 자동으로 적용됩니다.")
    }

    private fun createSecurityRequirement(): SecurityRequirement {
        return SecurityRequirement().addList(AUTH_SCHEME_NAME)
    }

    @Bean
    fun exceptionResponseCustomizer(): OperationCustomizer {
        return OperationCustomizer { operation: Operation, handlerMethod: HandlerMethod ->
            // 예외 응답 처리
            val exceptionAnnotation = handlerMethod.getMethodAnnotation(
                SwaggerExceptionResponse::class.java
            )

            if (exceptionAnnotation != null) {
                // examples가 있으면 상세 방식, 없으면 간단한 방식
                if (exceptionAnnotation.examples.isNotEmpty()) {
                    generateDetailedErrorCodeResponseExample(operation, exceptionAnnotation.examples)
                } else if (exceptionAnnotation.value.isNotEmpty()) {
                    generateSimpleErrorCodeResponseExample(operation, exceptionAnnotation.value)
                }
            }

            // 성공 응답 처리
            val successAnnotation = handlerMethod.getMethodAnnotation(
                SwaggerSuccessResponse::class.java
            )

            if (successAnnotation != null) {
                generateSuccessResponseExample(operation, successAnnotation, handlerMethod)
            }

            operation
        }
    }

    @Bean
    fun multipartOperationCustomizer(): OperationCustomizer {
        return OperationCustomizer { operation: Operation, handlerMethod: HandlerMethod ->
            val hasRequestPart = handlerMethod.methodParameters.any { param ->
                param.getParameterAnnotation(RequestPart::class.java) != null
            }
            if (!hasRequestPart) return@OperationCustomizer operation

            val multipartContentType = SpringMediaType.MULTIPART_FORM_DATA_VALUE
            val existingContent = operation.requestBody?.content
            val multipartMediaType = existingContent?.get(multipartContentType) ?: MediaType()

            if (multipartMediaType.schema == null || multipartMediaType.schema?.type != "object") {
                multipartMediaType.schema = MediaSchema<Any>().type("object").description("multipart/form-data (파일 + JSON 파트)")
            }

            // request 파트는 application/json으로 전송되도록 encoding 지정
            multipartMediaType.addEncoding("request", Encoding().contentType("application/json"))

            val contentOnlyMultipart = Content().apply { addMediaType(multipartContentType, multipartMediaType) }
            operation.requestBody = operation.requestBody ?: RequestBody()
            operation.requestBody!!.content = contentOnlyMultipart

            operation
        }
    }

    /**
     * 간단한 방식: 에러 코드를 기반으로 Swagger 응답 예제를 생성 (기본 메시지 사용)
     */
    private fun generateSimpleErrorCodeResponseExample(
        operation: Operation,
        exceptionResponseCodes: Array<ExceptionResponseCode>
    ) {
        val responses = operation.responses

        // ExampleHolder(에러 응답값) 객체를 만들고 에러 코드(HTTP 상태 코드)별로 그룹화
        val statusWithExampleHolders = exceptionResponseCodes.map { errorCode ->
            // JSON 내부 message 는 ExceptionResponseCode의 기본 메시지 사용
            val example = getSwaggerExample(errorCode, null)
            // Swagger UI 의 Example Description 은 에러 코드 이름으로 표시
            example.description = errorCode.name

            ExampleHolder(
                holder = example,
                code = errorCode.code,
                name = errorCode.name,
                description = errorCode.message
            )
        }.groupBy { it.code }

        // ExampleHolders를 ApiResponses에 추가
        addExamplesToResponses(responses, statusWithExampleHolders)
    }

    /**
     * 상세한 방식: 커스텀 메시지를 포함한 예외 예제를 생성
     */
    private fun generateDetailedErrorCodeResponseExample(
        operation: Operation,
        exceptionExamples: Array<ExceptionExample>
    ) {
        val responses = operation.responses

        // ExampleHolder(에러 응답값) 객체를 만들고 에러 코드(HTTP 상태 코드)별로 그룹화
        val statusWithExampleHolders = exceptionExamples.map { example ->
            val code = example.code
            val customMessage = example.message
            // JSON 내부 message 는 customMessage 사용
            val swaggerExample = getSwaggerExample(code, customMessage)

            // Swagger UI 의 Example Description:
            // - 사용자가 description에 적은 디테일 텍스트를 사용
            // - 비어 있으면 코드의 기본 메시지(예: "요청 값이 올바르지 않습니다.") 사용
            val exampleDescription = if (example.description.isEmpty()) {
                code.message
            } else {
                example.description
            }
            swaggerExample.description = exampleDescription

            // 응답 설명은 HTTP 코드별 공통 메시지 사용 (예: "요청 값이 올바르지 않습니다.")
            val responseDescription = code.message

            ExampleHolder(
                holder = swaggerExample,
                code = code.code,
                name = example.name,
                description = responseDescription
            )
        }.groupBy { it.code }

        // ExampleHolders를 ApiResponses에 추가
        addExamplesToResponses(responses, statusWithExampleHolders)
    }

    /**
     * ExceptionResponseCode로부터 Swagger Example 객체를 생성
     */
    private fun getSwaggerExample(
        responseCode: ExceptionResponseCode,
        customMessage: String?
    ): Example {
        val example = Example()
        val message = customMessage ?: responseCode.message
        val jsonString = swaggerExceptionResponseConfig.generateExampleJson(responseCode, message)

        // JSON 문자열을 파싱하여 객체로 변환 (Swagger UI가 제대로 표시하도록)
        val exampleObject = try {
            ObjectMapper().readValue(jsonString, Any::class.java)
        } catch (e: Exception) {
            // 파싱 실패 시 문자열로 사용
            jsonString
        }

        example.value = exampleObject
        example.description = message
        return example
    }

    /**
     * ExampleHolder 리스트를 ApiResponses에 추가
     * HTTP 상태 코드별로 그룹화된 예제들을 Swagger 문서에 추가
     */
    private fun addExamplesToResponses(
        responses: ApiResponses,
        statusWithExampleHolders: Map<Int, List<ExampleHolder>>
    ) {
        statusWithExampleHolders.forEach { (status, exampleHolders) ->
            val content = Content()
            val mediaType = MediaType()
            val apiResponse = ApiResponse()

            // 같은 상태 코드를 가진 예제들을 모두 추가
            exampleHolders.forEach { exampleHolder ->
                mediaType.addExamples(exampleHolder.name, exampleHolder.holder)
            }

            content.addMediaType("application/json", mediaType)
            apiResponse.content = content
            apiResponse.description = generateResponseDescription(exampleHolders)

            responses.addApiResponse(status.toString(), apiResponse)
        }
    }

    /**
     * 응답 설명 생성 (같은 상태 코드를 가진 에러들의 설명을 조합)
     */
    private fun generateResponseDescription(exampleHolders: List<ExampleHolder>): String {
        // 같은 HTTP 상태 코드에 대해 응답 설명은 하나의 문장만 사용
        return exampleHolders[0].description
    }

    /**
     * 성공 응답 예제를 생성
     * 기존 스키마를 유지하면서 예제만 추가합니다.
     */
    private fun generateSuccessResponseExample(
        operation: Operation,
        annotation: SwaggerSuccessResponse,
        handlerMethod: HandlerMethod
    ) {
        val responses = operation.responses
        val responseCode = annotation.responseCode
        val description = if (annotation.description.isEmpty()) {
            responseCode.httpStatus
        } else {
            annotation.description
        }

        // 기존 200 응답 제거 (커스텀 응답 코드로 대체)
        if (responseCode.code != 200 && responses.containsKey("200")) {
            responses.remove("200")
        }

        // 204 No Content는 본문이 없어야 함
        if (responseCode.code == 204) {
            val newResponse = ApiResponse()
                .description(if (description.isEmpty()) "Success" else description)
            // content를 추가하지 않음 (204는 본문 없음)
            responses.addApiResponse(responseCode.code.toString(), newResponse)
            return
        }

        // HandlerMethod의 return type에서 data DTO 타입을 추출
        val dataExample = extractDataExampleFromReturnType(handlerMethod)
        val dataClassInfo = extractDataClassInfoFromReturnType(handlerMethod)

        // 전체 example JSON 생성
        val exampleJson = swaggerExceptionResponseConfig.generateSuccessExampleJson(
            responseCode,
            dataExample
        )

        // JSON 문자열을 파싱하여 객체로 변환 (Swagger UI가 제대로 표시하도록)
        val exampleObject = try {
            ObjectMapper().readValue(exampleJson, Any::class.java)
        } catch (e: Exception) {
            // 파싱 실패 시 문자열로 사용
            exampleJson
        }

        val statusCode = responseCode.code.toString()

        // 기존 200 응답 제거 (커스텀 응답 코드로 대체)
        if (responseCode.code != 200 && responses.containsKey("200")) {
            responses.remove("200")
        }

        // 기존 응답 제거하고 완전히 재생성 (스키마 포함)
        if (responses.containsKey(statusCode)) {
            responses.remove(statusCode)
        }

        // 스키마 생성
        val responseSchema = if (dataClassInfo != null) {
            createSuccessResponseSchema(dataClassInfo.first, dataClassInfo.second, dataClassInfo.third)
        } else {
            null
        }

        // 새로운 응답 생성
        val mediaType = MediaType().example(exampleObject)
        if (responseSchema != null) {
            mediaType.schema(responseSchema)
        }

        val newResponse = ApiResponse()
            .description(if (description.isEmpty()) "Success" else description)
            .content(
                Content().addMediaType("application/json", mediaType)
            )

        responses.addApiResponse(statusCode, newResponse)
    }

    /**
     * HandlerMethod의 return type에서 data DTO 클래스 정보 추출
     * @return Triple<데이터 클래스, List 여부, PageResponse 여부>
     */
    private fun extractDataClassInfoFromReturnType(handlerMethod: HandlerMethod): Triple<Class<*>, Boolean, Boolean>? {
        return try {
            // ResponseEntity<SuccessResponse<T>>의 제네릭 타입 추출
            val returnType = handlerMethod.returnType.genericParameterType

            if (returnType is ParameterizedType) {
                val typeArgs = returnType.actualTypeArguments

                // ResponseEntity<SuccessResponse<T>>의 경우
                if (typeArgs.isNotEmpty() && typeArgs[0] is ParameterizedType) {
                    val successResponseType = typeArgs[0] as ParameterizedType
                    val successResponseArgs = successResponseType.actualTypeArguments

                    if (successResponseArgs.isNotEmpty()) {
                        val dataType = successResponseArgs[0]

                        // PageResponse<T>인 경우
                        if (dataType is ParameterizedType) {
                            val paramType = dataType
                            val rawType = paramType.rawType

                            // PageResponse인지 확인
                            if (rawType is Class<*> && rawType.name == PageResponse::class.java.name) {
                                val pageResponseTypeArgs = paramType.actualTypeArguments

                                if (pageResponseTypeArgs.isNotEmpty() && pageResponseTypeArgs[0] is Class<*>) {
                                    return Triple(pageResponseTypeArgs[0] as Class<*>, true, true)
                                }
                            }
                            // List<T>인 경우
                            else if (rawType is Class<*> && List::class.java.isAssignableFrom(rawType)) {
                                val listTypeArgs = paramType.actualTypeArguments

                                if (listTypeArgs.isNotEmpty() && listTypeArgs[0] is Class<*>) {
                                    return Triple(listTypeArgs[0] as Class<*>, true, false)
                                }
                            }
                        }
                        // 단일 DTO인 경우
                        else if (dataType is Class<*>) {
                            return Triple(dataType, false, false)
                        }
                    }
                }
            }

            null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * SuccessResponse 래퍼를 포함한 전체 응답 스키마 생성
     */
    private fun createSuccessResponseSchema(dataClass: Class<*>, isList: Boolean, isPageResponse: Boolean): MediaSchema<*> {
        // SuccessResponse의 스키마 생성
        val successResponseSchema = MediaSchema<Any>()
            .type("object")
            .addProperty("success", MediaSchema<Boolean>().type("boolean").description("요청 성공 여부"))
            .addProperty("status", MediaSchema<String>().type("string").description("HTTP 상태 메시지"))
            .addProperty("code", MediaSchema<Int>().type("integer").description("HTTP 상태 코드"))
            .addProperty("detailCode", MediaSchema<Int>().type("integer").description("상세 응답 코드"))
            .addProperty("message", MediaSchema<String>().type("string").description("응답 메시지"))

        // data 필드의 스키마 생성
        val dataSchema = if (isPageResponse) {
            // PageResponse인 경우
            createPageResponseSchema(dataClass)
        } else if (isList) {
            // List인 경우 배열 스키마 생성 (인라인 스키마 사용)
            val itemsSchema = createInlineSchemaFromDtoClass(dataClass)

            MediaSchema<Any>()
                .type("array")
                .items(itemsSchema)
        } else {
            // 단일 객체인 경우 $ref 사용 (Springdoc이 자동 생성한 스키마 참조)
            val refSchema = MediaSchema<Any>()
            refSchema.`$ref`("#/components/schemas/${dataClass.simpleName}")
            refSchema
        }

        successResponseSchema.addProperty("data", dataSchema)

        return successResponseSchema
    }

    /**
     * PageResponse 스키마 생성
     */
    private fun createPageResponseSchema(dataClass: Class<*>): MediaSchema<*> {
        val pageResponseSchema = MediaSchema<Any>()
            .type("object")
            .description("페이징 응답")

        // content 필드 (List<T>)
        val itemsSchema = createInlineSchemaFromDtoClass(dataClass)
        val contentSchema = MediaSchema<Any>()
            .type("array")
            .description("데이터 리스트")
            .items(itemsSchema)
        pageResponseSchema.addProperty("content", contentSchema)

        // 페이징 메타데이터 필드들
        pageResponseSchema.addProperty("page", MediaSchema<Int>().type("integer").format("int32").description("현재 페이지 (1-based)").example(1))
        pageResponseSchema.addProperty("size", MediaSchema<Int>().type("integer").format("int32").description("페이지 크기").example(10))
        pageResponseSchema.addProperty("totalElements", MediaSchema<Long>().type("integer").format("int64").description("전체 데이터 개수").example(100L))
        pageResponseSchema.addProperty("totalPages", MediaSchema<Int>().type("integer").format("int32").description("전체 페이지 수").example(10))
        pageResponseSchema.addProperty("hasNext", MediaSchema<Boolean>().type("boolean").description("다음 페이지 존재 여부").example(true))
        pageResponseSchema.addProperty("hasPrevious", MediaSchema<Boolean>().type("boolean").description("이전 페이지 존재 여부").example(false))
        pageResponseSchema.addProperty("isFirst", MediaSchema<Boolean>().type("boolean").description("첫 페이지 여부").example(true))
        pageResponseSchema.addProperty("isLast", MediaSchema<Boolean>().type("boolean").description("마지막 페이지 여부").example(false))

        // required 필드 설정
        pageResponseSchema.required(
            listOf("content", "page", "size", "totalElements", "totalPages", "hasNext", "hasPrevious", "isFirst", "isLast")
        )

        return pageResponseSchema
    }

    /**
     * HandlerMethod의 return type에서 data DTO의 example 추출
     */
    private fun extractDataExampleFromReturnType(handlerMethod: HandlerMethod): String? {
        return try {
            // ResponseEntity<SuccessResponse<T>>의 제네릭 타입 추출
            val returnType = handlerMethod.returnType.genericParameterType

            if (returnType is ParameterizedType) {
                val typeArgs = returnType.actualTypeArguments

                // ResponseEntity<SuccessResponse<T>>의 경우
                if (typeArgs.isNotEmpty() && typeArgs[0] is ParameterizedType) {
                    val successResponseType = typeArgs[0] as ParameterizedType
                    val successResponseArgs = successResponseType.actualTypeArguments

                    if (successResponseArgs.isNotEmpty()) {
                        val dataType = successResponseArgs[0]

                        // PageResponse<T> 또는 List<T>인 경우
                        if (dataType is ParameterizedType) {
                            val paramType = dataType
                            val rawType = paramType.rawType

                            // PageResponse인지 확인
                            if (rawType is Class<*> && rawType.name == PageResponse::class.java.name) {
                                val pageResponseTypeArgs = paramType.actualTypeArguments

                                if (pageResponseTypeArgs.isNotEmpty() && pageResponseTypeArgs[0] is Class<*>) {
                                    val dtoClass = pageResponseTypeArgs[0] as Class<*>

                                    // PageResponse 형태로 example 생성
                                    val singleExample = generateExampleFromDtoClass(dtoClass)
                                    if (singleExample == "{}") return null

                                    return """
                                        {
                                          "content": [$singleExample],
                                          "page": 1,
                                          "size": 10,
                                          "totalElements": 100,
                                          "totalPages": 10,
                                          "hasNext": true,
                                          "hasPrevious": false,
                                          "isFirst": true,
                                          "isLast": false
                                        }
                                    """.trimIndent()
                                }
                            }
                            // List<T>인 경우
                            else if (rawType is Class<*> && List::class.java.isAssignableFrom(rawType)) {
                                val listTypeArgs = paramType.actualTypeArguments

                                if (listTypeArgs.isNotEmpty() && listTypeArgs[0] is Class<*>) {
                                    val dtoClass = listTypeArgs[0] as Class<*>

                                    // 배열 형태로 example 생성: [{...}]
                                    val singleExample = generateExampleFromDtoClass(dtoClass)
                                    return if (singleExample == "{}") null else "[$singleExample]"
                                }
                            }
                        }
                        // 단일 DTO인 경우
                        else if (dataType is Class<*>) {
                            val dtoClass = dataType

                            // DTO 클래스의 필드에서 example 생성
                            val example = generateExampleFromDtoClass(dtoClass)
                            return if (example == "{}") null else example
                        }
                    }
                }
            }

            null
        } catch (e: Exception) {
            // 예외 발생 시 null 반환 (빈 data 필드로 처리)
            null
        }
    }

    /**
     * DTO 클래스로부터 인라인 스키마 생성 (리스트 items용)
     */
    private fun createInlineSchemaFromDtoClass(dtoClass: Class<*>): MediaSchema<*> {
        val schema = MediaSchema<Any>()
            .type("object")

        try {
            val fields = dtoClass.declaredFields

            for (field in fields) {
                // Kotlin data class의 backing field 제외
                if (field.name.contains("$")) {
                    continue
                }

                val schemaAnnotation = field.getAnnotation(Schema::class.java)
                val fieldName = field.name
                val fieldType = field.type

                val propertySchema = when {
                    fieldType == String::class.java -> {
                        MediaSchema<String>()
                            .type("string")
                            .description(schemaAnnotation?.description ?: "")
                            .example(if (schemaAnnotation?.example?.isNotEmpty() == true) schemaAnnotation.example else "string")
                    }
                    fieldType == Long::class.javaPrimitiveType || fieldType == Long::class.java -> {
                        MediaSchema<Long>()
                            .type("integer")
                            .format("int64")
                            .description(schemaAnnotation?.description ?: "")
                            .example(if (schemaAnnotation?.example?.isNotEmpty() == true) schemaAnnotation.example.toLongOrNull() ?: 0L else 0L)
                    }
                    fieldType == Int::class.javaPrimitiveType || fieldType == Int::class.java -> {
                        MediaSchema<Int>()
                            .type("integer")
                            .format("int32")
                            .description(schemaAnnotation?.description ?: "")
                            .example(if (schemaAnnotation?.example?.isNotEmpty() == true) schemaAnnotation.example.toIntOrNull() ?: 0 else 0)
                    }
                    fieldType == Boolean::class.javaPrimitiveType || fieldType == Boolean::class.java -> {
                        MediaSchema<Boolean>()
                            .type("boolean")
                            .description(schemaAnnotation?.description ?: "")
                            .example(if (schemaAnnotation?.example?.isNotEmpty() == true) schemaAnnotation.example.toBoolean() else true)
                    }
                    fieldType == Double::class.javaPrimitiveType || fieldType == Double::class.java -> {
                        MediaSchema<Double>()
                            .type("number")
                            .format("double")
                            .description(schemaAnnotation?.description ?: "")
                            .example(if (schemaAnnotation?.example?.isNotEmpty() == true) schemaAnnotation.example.toDoubleOrNull() ?: 0.0 else 0.0)
                    }
                    fieldType == Float::class.javaPrimitiveType || fieldType == Float::class.java -> {
                        MediaSchema<Float>()
                            .type("number")
                            .format("float")
                            .description(schemaAnnotation?.description ?: "")
                            .example(if (schemaAnnotation?.example?.isNotEmpty() == true) schemaAnnotation.example.toFloatOrNull() ?: 0.0f else 0.0f)
                    }
                    // List 타입 처리
                    List::class.java.isAssignableFrom(fieldType) -> {
                        val genericType = field.genericType
                        val arraySchema = MediaSchema<Any>()
                            .type("array")
                            .description(schemaAnnotation?.description ?: "")

                        if (genericType is ParameterizedType) {
                            val typeArgs = genericType.actualTypeArguments
                            if (typeArgs.isNotEmpty() && typeArgs[0] is Class<*>) {
                                val itemClass = typeArgs[0] as Class<*>

                                // 제네릭 타입이 기본 타입인 경우
                                val itemSchema = when (itemClass) {
                                    String::class.java -> MediaSchema<String>().type("string").example("string")
                                    Long::class.java, Long::class.javaPrimitiveType -> MediaSchema<Long>().type("integer").format("int64").example(0L)
                                    Int::class.java, Int::class.javaPrimitiveType -> MediaSchema<Int>().type("integer").format("int32").example(0)
                                    Boolean::class.java, Boolean::class.javaPrimitiveType -> MediaSchema<Boolean>().type("boolean").example(true)
                                    Double::class.java, Double::class.javaPrimitiveType -> MediaSchema<Double>().type("number").format("double").example(0.0)
                                    Float::class.java, Float::class.javaPrimitiveType -> MediaSchema<Float>().type("number").format("float").example(0.0f)
                                    else -> {
                                        // 중첩된 DTO인 경우 재귀적으로 스키마 생성
                                        if (itemClass.name.startsWith("com.krince.reminisce")) { //TODO 변경
                                            createInlineSchemaFromDtoClass(itemClass)
                                        } else {
                                            MediaSchema<Any>().type("string").example("string")
                                        }
                                    }
                                }
                                arraySchema.items(itemSchema)
                            }
                        }

                        arraySchema
                    }
                    // LocalDateTime 타입 처리
                    fieldType.name == "java.time.LocalDateTime" -> {
                        MediaSchema<String>()
                            .type("string")
                            .description(schemaAnnotation?.description ?: "")
                            .example(if (schemaAnnotation?.example?.isNotEmpty() == true) schemaAnnotation.example else "2026-01-09 14:30:25")
                    }
                    // 중첩된 DTO 클래스인 경우
                    fieldType.name.startsWith("com.krince.reminisce") && !fieldType.isPrimitive && !fieldType.isInterface -> { //TODO 변경
                        createInlineSchemaFromDtoClass(fieldType)
                    }
                    else -> {
                        MediaSchema<Any>()
                            .type("string")
                            .description(schemaAnnotation?.description ?: "")
                            .example(if (schemaAnnotation?.example?.isNotEmpty() == true) schemaAnnotation.example else "string")
                    }
                }

                // required 속성 설정
                if (schemaAnnotation?.required == true) {
                    val requiredList = schema.required ?: mutableListOf()
                    requiredList.add(fieldName)
                    schema.required = requiredList
                }

                schema.addProperty(fieldName, propertySchema)
            }
        } catch (e: Exception) {
            // 예외 발생 시 빈 스키마 반환
        }

        return schema
    }

    /**
     * DTO 클래스로부터 example JSON 생성 (재귀적으로 중첩된 DTO와 List 처리)
     */
    private fun generateExampleFromDtoClass(dtoClass: Class<*>): String {
        return generateExampleFromDtoClass(dtoClass, 1)
    }

    /**
     * DTO 클래스로부터 example JSON 생성 (재귀적으로 중첩된 DTO와 List 처리)
     * @param dtoClass DTO 클래스
     * @param indentLevel 들여쓰기 레벨 (재귀 깊이)
     */
    private fun generateExampleFromDtoClass(dtoClass: Class<*>, indentLevel: Int): String {
        return try {
            val indent = "    ".repeat(indentLevel)
            val json = StringBuilder("{\n")
            val fields = dtoClass.declaredFields

            var index = 0
            for (field in fields) {
                // Kotlin data class의 backing field 제외 (field$delegate 등)
                if (field.name.contains("$")) {
                    continue
                }

                val fieldName = field.name
                val fieldType = field.type
                val schemaAnnotation = field.getAnnotation(Schema::class.java)

                if (index > 0) {
                    json.append(",\n")
                }

                json.append(indent).append("\"").append(fieldName).append("\": ")

                // 필드 타입에 따라 처리
                when {
                    // String 타입
                    fieldType == String::class.java -> {
                        val example = if (schemaAnnotation?.example?.isNotEmpty() == true) {
                            schemaAnnotation.example
                        } else {
                            "string"
                        }
                        json.append("\"").append(example).append("\"")
                    }
                    // Boolean 타입
                    fieldType == Boolean::class.javaPrimitiveType || fieldType == Boolean::class.java -> {
                        val example = if (schemaAnnotation?.example?.isNotEmpty() == true) {
                            schemaAnnotation.example.toBoolean()
                        } else {
                            true
                        }
                        json.append(example)
                    }
                    // Long 타입
                    fieldType == Long::class.javaPrimitiveType || fieldType == Long::class.java -> {
                        val example = if (schemaAnnotation?.example?.isNotEmpty() == true) {
                            schemaAnnotation.example.toLongOrNull() ?: 1L
                        } else {
                            1L
                        }
                        json.append(example)
                    }
                    // Int 타입
                    fieldType == Int::class.javaPrimitiveType || fieldType == Int::class.java -> {
                        val example = if (schemaAnnotation?.example?.isNotEmpty() == true) {
                            schemaAnnotation.example.toIntOrNull() ?: 1
                        } else {
                            1
                        }
                        json.append(example)
                    }
                    // Double 타입
                    fieldType == Double::class.javaPrimitiveType || fieldType == Double::class.java -> {
                        val example = if (schemaAnnotation?.example?.isNotEmpty() == true) {
                            schemaAnnotation.example.toDoubleOrNull() ?: 0.0
                        } else {
                            0.0
                        }
                        json.append(example)
                    }
                    // Float 타입
                    fieldType == Float::class.javaPrimitiveType || fieldType == Float::class.java -> {
                        val example = if (schemaAnnotation?.example?.isNotEmpty() == true) {
                            schemaAnnotation.example.toFloatOrNull() ?: 0.0f
                        } else {
                            0.0f
                        }
                        json.append(example)
                    }
                    // LocalDateTime 타입
                    fieldType.name == "java.time.LocalDateTime" -> {
                        val example = if (schemaAnnotation?.example?.isNotEmpty() == true) {
                            schemaAnnotation.example
                        } else {
                            "2026-01-09 14:30:25"
                        }
                        json.append("\"").append(example).append("\"")
                    }
                    // List 타입 처리
                    List::class.java.isAssignableFrom(fieldType) -> {
                        // 제네릭 타입 추출
                        val genericType = field.genericType
                        if (genericType is ParameterizedType) {
                            val typeArgs = genericType.actualTypeArguments
                            if (typeArgs.isNotEmpty() && typeArgs[0] is Class<*>) {
                                val itemClass = typeArgs[0] as Class<*>

                                // 제네릭 타입이 기본 타입인 경우
                                when (itemClass) {
                                    String::class.java -> {
                                        // Schema annotation의 example을 파싱하거나 기본값 사용
                                        val exampleValue = if (schemaAnnotation?.example?.isNotEmpty() == true) {
                                            schemaAnnotation.example
                                        } else {
                                            "[\"string\"]"
                                        }
                                        json.append(exampleValue)
                                    }
                                    Long::class.java, Long::class.javaPrimitiveType -> json.append("[1]")
                                    Int::class.java, Int::class.javaPrimitiveType -> json.append("[1]")
                                    Boolean::class.java, Boolean::class.javaPrimitiveType -> json.append("[true]")
                                    Double::class.java, Double::class.javaPrimitiveType -> json.append("[0.0]")
                                    Float::class.java, Float::class.javaPrimitiveType -> json.append("[0.0]")
                                    else -> {
                                        // 중첩된 DTO인 경우 재귀적으로 생성
                                        if (itemClass.name.startsWith("com.krince.reminisce")) { //TODO 변경
                                            val itemExample = generateExampleFromDtoClass(itemClass, indentLevel + 1)
                                            json.append("[\n").append(indent).append("  ").append(itemExample).append("\n").append(indent).append("]")
                                        } else {
                                            json.append("[]")
                                        }
                                    }
                                }
                            } else {
                                json.append("[]")
                            }
                        } else {
                            json.append("[]")
                        }
                    }
                    // 다른 DTO 클래스인 경우 재귀적으로 처리
                    fieldType.name.startsWith("com.krince.reminisce") && !fieldType.isPrimitive && !fieldType.isInterface -> { //TODO 변경
                        val nestedExample = generateExampleFromDtoClass(fieldType, indentLevel + 1)
                        json.append(nestedExample)
                    }
                    // 기타 타입
                    else -> {
                        val example = if (schemaAnnotation?.example?.isNotEmpty() == true) {
                            schemaAnnotation.example
                        } else {
                            "string"
                        }
                        json.append("\"").append(example).append("\"")
                    }
                }

                index++
            }

            if (index == 0) {
                // 필드가 하나도 없으면 빈 객체 반환
                return "{}"
            }

            json.append("\n").append("    ".repeat(indentLevel - 1)).append("}")
            json.toString()
        } catch (e: Exception) {
            "{}"
        }
    }
}