package de.emaarco.example.adapter.outbound

import de.emaarco.example.application.port.outbound.OrderStatusBroadcastPort
import de.emaarco.example.domain.OrderStatusChange
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class EventRelaySidecarAdapter(
    restClientBuilder: RestClient.Builder,
    @Value("\${event-relay-sidecar.url}") sidecarUrl: String,
) : OrderStatusBroadcastPort {

    private val log = KotlinLogging.logger {}

    private val sidecar = restClientBuilder.baseUrl(sidecarUrl).build()

    override fun broadcastToAllPods(change: OrderStatusChange) {
        sidecar
            .post()
            .uri { it.path("/events").queryParam("key", change.orderId.value).build() }
            .contentType(MediaType.APPLICATION_JSON)
            .body(OrderStatusChangeMessage.from(change))
            .retrieve()
            .toBodilessEntity()
        log.info { "Handed status ${change.status} of order ${change.orderId.value} to the event relay sidecar" }
    }

    data class OrderStatusChangeMessage(
        val orderId: String,
        val status: String,
        val changedAt: String,
        val changedOnPod: String,
    ) {
        companion object {
            fun from(change: OrderStatusChange) =
                OrderStatusChangeMessage(
                    orderId = change.orderId.value,
                    status = change.status.name,
                    changedAt = change.changedAt.toString(),
                    changedOnPod = change.changedOnPod,
                )
        }
    }
}
