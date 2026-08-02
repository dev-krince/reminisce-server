package com.krince.reminisce.shared.response

enum class SuccessResponseCode(
    override val isSuccess: Boolean,
    override val code: Int,
    override val detailCode: String,
    override val httpStatus: String,
    override val message: String
) : ResponseCode {
    OK(true, 200, "OK-200", "OK", "요청이 성공적으로 처리되었습니다. 요청한 데이터를 반환합니다."),
    CREATED(true, 201, "CR-201", "Created", "요청이 성공적으로 처리되었습니다. 새로운 리소스가 생성되었습니다."),
    NO_CONTENT(true, 204, "NC-204", "No Content", "요청이 성공적으로 처리되었습니다. 응답 데이터는 없습니다."),
}