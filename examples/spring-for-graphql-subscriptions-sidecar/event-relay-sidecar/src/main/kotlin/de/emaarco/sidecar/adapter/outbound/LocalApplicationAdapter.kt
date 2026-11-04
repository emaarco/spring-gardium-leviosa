package de.emaarco.sidecar.adapter.outbound

import de.emaarco.sidecar.application.port.outbound.LocalApplicationPort
import de.emaarco.sidecar.domain.Event
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class LocalApplicationAdapter(
    restClientBuilder: RestClient.Builder,
    @Value("\${relay.delivery-url}") private val deliveryUrl: String,
) : LocalApplicationPort {

    private val application = restClientBuilder.build()

    override fun deliver(event: Event) {
        application
            .post()
            .uri(deliveryUrl)
            .contentType(MediaType.APPLICATION_JSON)
            .body(event.payload)
            .retrieve()
            .toBodilessEntity()
    }
}
