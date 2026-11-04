package de.emaarco.sidecar.adapter.inbound.rest

import de.emaarco.sidecar.application.port.inbound.PublishEventUseCase
import de.emaarco.sidecar.domain.Event
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
class EventController(
    private val useCase: PublishEventUseCase,
) {

    private val log = KotlinLogging.logger {}

    @PostMapping("/events")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun publish(
        @RequestParam key: String?,
        @RequestBody payload: String,
    ) {
        log.info { "Local application handed over an event with key $key" }
        useCase.publish(Event(key, payload))
    }
}
