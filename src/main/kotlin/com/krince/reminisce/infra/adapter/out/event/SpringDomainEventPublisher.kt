package com.krince.reminisce.infra.adapter.out.event

import com.krince.reminisce.application.port.out.event.DomainEventPublisher
import com.krince.reminisce.shared.event.DomainEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class SpringDomainEventPublisher(private val eventPublisher: ApplicationEventPublisher) : DomainEventPublisher {
    override fun publish(events: List<DomainEvent>) = events.forEach { eventPublisher.publishEvent(it) }
}