package de.emaarco.sidecar.adapter.outbound

import de.emaarco.sidecar.domain.Event
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.content
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent
import org.springframework.web.client.RestClient

class LocalApplicationAdapterTest {

    private val restClientBuilder = RestClient.builder()
    private val application = MockRestServiceServer.bindTo(restClientBuilder).build()
    private val adapter =
        LocalApplicationAdapter(restClientBuilder, "http://localhost:8086/internal/order-status-changes")

    @Test
    fun `posts the payload unchanged as json to the local application`() {
        application
            .expect(requestTo("http://localhost:8086/internal/order-status-changes"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().string("""{"orderId":"order-42"}"""))
            .andRespond(withNoContent())

        adapter.deliver(Event("order-42", """{"orderId":"order-42"}"""))

        application.verify()
    }
}
