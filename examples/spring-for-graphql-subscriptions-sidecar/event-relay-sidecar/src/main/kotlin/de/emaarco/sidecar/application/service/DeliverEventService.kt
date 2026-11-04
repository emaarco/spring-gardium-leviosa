package de.emaarco.sidecar.application.service

import de.emaarco.sidecar.application.port.inbound.DeliverEventUseCase
import de.emaarco.sidecar.application.port.outbound.LocalApplicationPort
import de.emaarco.sidecar.domain.Event
import org.springframework.stereotype.Service

@Service
class DeliverEventService(
    private val application: LocalApplicationPort,
) : DeliverEventUseCase {
    override fun deliver(event: Event) = application.deliver(event)
}
