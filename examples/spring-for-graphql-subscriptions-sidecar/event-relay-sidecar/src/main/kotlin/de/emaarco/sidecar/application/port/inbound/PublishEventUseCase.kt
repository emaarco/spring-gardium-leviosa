package de.emaarco.sidecar.application.port.inbound

import de.emaarco.sidecar.domain.Event

interface PublishEventUseCase {
    fun publish(event: Event)
}
