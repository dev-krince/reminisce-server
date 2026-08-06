package com.krince.reminisce.application.port.`in`.notice.usecase

import com.krince.reminisce.application.port.`in`.notice.result.NoticeSummaryResult

interface GetNoticesUseCase {
    fun execute(): List<NoticeSummaryResult>
}
