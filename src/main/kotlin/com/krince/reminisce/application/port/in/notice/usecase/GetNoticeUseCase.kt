package com.krince.reminisce.application.port.`in`.notice.usecase

import com.krince.reminisce.application.port.`in`.notice.command.GetNoticeCommand
import com.krince.reminisce.application.port.`in`.notice.result.NoticeDetailResult

interface GetNoticeUseCase {
    fun execute(command: GetNoticeCommand): NoticeDetailResult
}
