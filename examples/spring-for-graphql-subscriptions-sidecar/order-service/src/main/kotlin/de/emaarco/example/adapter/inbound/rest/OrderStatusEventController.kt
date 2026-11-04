package de.emaarco.example.adapter.inbound.rest

import de.emaarco.example.application.port.inbound.NotifyOrderSubscribersUseCase
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
class OrderStatusEventController(
    private val useCase: NotifyOrderSubscribersUseCase,
) {

    private val log = KotlinLogging.logger {}

    @PostMapping("/internal/order-status-changes")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun onOrderStatusChange(
        @RequestBody change: OrderStatusChangeInput,
    ) {
        log.info {
            "Sidecar delivered status ${change.status} of order ${change.orderId} changed on ${change.changedOnPod}"
        }
        useCase.notifySubscribers(change.toDomain())
    }
}
