package com.krince.boilerplate.shared.response

open class Response(
    private val responseCode: ResponseCode,
    val message: String,
) {
    private val success: Boolean = responseCode.isSuccess
    private val status: String = responseCode.httpStatus
    private val code: Int = responseCode.code
    private val detailCode: String = responseCode.detailCode
}