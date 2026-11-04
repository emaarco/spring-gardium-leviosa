package de.emaarco.sidecar.application.service

import de.emaarco.sidecar.application.port.inbound.PublishEventUseCase
import de.emaarco.sidecar.application.port.outbound.MessageBrokerPort
import de.emaarco.sidecar.domain.Event
import org.springframework.stereotype.Service

@Service
class PublishEventService(
    private val broker: MessageBrokerPort,
) : PublishEventUseCase {
    override fun publish(event: Event) = broker.publishToAllPods(event)
}
