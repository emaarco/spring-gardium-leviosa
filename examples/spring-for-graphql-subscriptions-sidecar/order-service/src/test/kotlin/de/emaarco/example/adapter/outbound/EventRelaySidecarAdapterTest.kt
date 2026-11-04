package de.emaarco.example.adapter.outbound

import de.emaarco.example.domain.OrderId
import de.emaarco.example.domain.OrderStatus
import de.emaarco.example.domain.OrderStatusChange
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.content
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withAccepted
import org.springframework.test.web.client.response.MockRestResponseCreators.withServiceUnavailable
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestClient
import java.time.Instant

class EventRelaySidecarAdapterTest {

    private val restClientBuilder = RestClient.builder()
    private val sidecar = MockRestServiceServer.bindTo(restClientBuilder).build()
    private val adapter = EventRelaySidecarAdapter(restClientBuilder, "http://localhost:8096")

    private val change =
        OrderStatusChange(OrderId("order-42"), OrderStatus.SHIPPED, Instant.parse("2026-12-11T08:15:00Z"), "pod-2")

    @Test
    fun `hands the status change to the sidecar keyed by order id`() {
        sidecar
            .expect(requestTo("http://localhost:8096/events?key=order-42"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(
                content().json(
                    """
                    {
                      "orderId": "order-42",
                      "status": "SHIPPED",
                      "changedAt": "2026-12-11T08:15:00Z",
                      "changedOnPod": "pod-2"
                    }
                    """.trimIndent(),
                ),
            ).andRespond(withAccepted())

        adapter.broadcastToAllPods(change)

        sidecar.verify()
    }

    @Test
    fun `fails the status change when the sidecar cannot take it`() {
        sidecar.expect(requestTo("http://localhost:8096/events?key=order-42")).andRespond(withServiceUnavailable())

        assertThatThrownBy { adapter.broadcastToAllPods(change) }.isInstanceOf(HttpServerErrorException::class.java)
    }
}
