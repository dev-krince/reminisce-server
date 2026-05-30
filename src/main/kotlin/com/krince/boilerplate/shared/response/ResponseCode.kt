package com.krince.boilerplate.shared.response

interface ResponseCode {
    val isSuccess: Boolean
    val code: Int
    val detailCode: String
    val httpStatus: String
    val message: String
}