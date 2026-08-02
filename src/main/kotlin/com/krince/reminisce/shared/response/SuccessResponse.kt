package com.krince.reminisce.shared.response

class SuccessResponse<T>(
    responseCode: SuccessResponseCode,
    message: String,
    val data: T,
) : Response(responseCode, message) {
    val success: Boolean = responseCode.isSuccess
    val status: String = responseCode.httpStatus
    val code: Int = responseCode.code
    val detailCode: String = responseCode.detailCode
}

fun <T> successResponse(responseCode: SuccessResponseCode, data: T): SuccessResponse<T> = SuccessResponse(
    responseCode = responseCode,
    message = responseCode.message,
    data = data,
)