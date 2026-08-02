package com.krince.reminisce.shared.event

import java.time.LocalDateTime

interface DomainEvent {
    val eventId: String
    val occurredOn: LocalDateTime
}