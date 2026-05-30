package com.krince.boilerplate.shared.response

class ExceptionResponse(
    responseCode: ResponseCode,
    message: String,
    val requestId: String? = null,
) : Response(responseCode, message) {
    val success: Boolean = responseCode.isSuccess
    val status: String = responseCode.httpStatus
    val code: Int = responseCode.code
    val detailCode: String = responseCode.detailCode
}