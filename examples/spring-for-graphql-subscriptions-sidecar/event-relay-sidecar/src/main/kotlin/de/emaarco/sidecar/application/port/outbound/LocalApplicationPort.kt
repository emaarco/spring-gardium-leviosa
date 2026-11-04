package de.emaarco.sidecar.application.port.outbound

import de.emaarco.sidecar.domain.Event

interface LocalApplicationPort {
    fun deliver(event: Event)
}
