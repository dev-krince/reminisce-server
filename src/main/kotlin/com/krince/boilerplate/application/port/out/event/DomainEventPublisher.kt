package com.krince.boilerplate.application.port.out.event

import com.krince.boilerplate.shared.event.DomainEvent

interface DomainEventPublisher {
    fun publish(events: List<DomainEvent>)
}