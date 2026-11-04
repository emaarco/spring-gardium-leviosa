package de.emaarco.sidecar.application.port.inbound

import de.emaarco.sidecar.domain.Event

interface DeliverEventUseCase {
    fun deliver(event: Event)
}
