package com.krince.reminisce.application.port.out.event

import com.krince.reminisce.shared.event.DomainEvent

interface DomainEventPublisher {
    fun publish(events: List<DomainEvent>)
}