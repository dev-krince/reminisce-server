package com.krince.reminisce.application.port.`in`.postactivity.result

import com.krince.reminisce.domain.model.speakingsession.vo.SessionStatus
import java.time.LocalDateTime

data class RetellingResult(
    val retellingText: String,
    val retellingAudioUrl: String?,
    val completedAt: LocalDateTime,
    val status: SessionStatus,
    val retellingSegments: List<String>? = null,
)
