package de.emaarco.sidecar

import com.ninjasquad.springmockk.MockkBean
import de.emaarco.sidecar.application.port.outbound.LocalApplicationPort
import de.emaarco.sidecar.domain.Event
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.atMost
import org.awaitility.kotlin.await
import org.awaitility.kotlin.until
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.web.client.RestClient
import java.time.Duration

@SpringBootTest(webEnvironment = RANDOM_PORT)
@EmbeddedKafka(bootstrapServersProperty = "spring.kafka.bootstrap-servers")
class EventRelayIntegrationTest {

    @LocalServerPort
    private var port = 0

    @MockkBean(relaxUnitFun = true)
    private lateinit var localApplication: LocalApplicationPort

    private val sidecar by lazy { RestClient.create("http://localhost:$port") }

    private val payload = """{"orderId":"order-42","status":"SHIPPED"}"""

    @BeforeEach
    fun awaitReadiness() {
        await atMost Duration.ofSeconds(30) until { isReady() }
    }

    @Test
    fun `accepts an event once the broker has stored it`() {
        val response = handOver(payload, key = "order-42")

        assertThat(response.statusCode).isEqualTo(HttpStatus.ACCEPTED)
    }

    @Test
    fun `relays an event of the local application through the broker back to the local application`() {
        handOver(payload, key = "order-42")

        verify(timeout = 10_000) { localApplication.deliver(Event("order-42", payload)) }
    }

    private fun handOver(
        payload: String,
        key: String,
    ) = sidecar
        .post()
        .uri { it.path("/events").queryParam("key", key).build() }
        .contentType(MediaType.APPLICATION_JSON)
        .body(payload)
        .retrieve()
        .toBodilessEntity()

    private fun isReady() =
        runCatching {
            sidecar
                .get()
                .uri("/actuator/health/readiness")
                .retrieve()
                .toBodilessEntity()
        }.isSuccess
}
