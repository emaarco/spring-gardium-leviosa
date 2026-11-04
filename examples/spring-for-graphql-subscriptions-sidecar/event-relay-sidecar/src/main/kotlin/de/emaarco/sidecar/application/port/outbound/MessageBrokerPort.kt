package de.emaarco.sidecar.application.port.outbound

import de.emaarco.sidecar.domain.Event

interface MessageBrokerPort {
    fun publishToAllPods(event: Event)
}
