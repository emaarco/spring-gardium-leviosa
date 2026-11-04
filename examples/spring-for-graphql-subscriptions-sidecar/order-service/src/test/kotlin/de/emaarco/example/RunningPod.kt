package de.emaarco.example

import de.emaarco.example.adapter.outbound.LocalOrderSubscribersAdapter
import org.awaitility.kotlin.atMost
import org.awaitility.kotlin.await
import org.awaitility.kotlin.until
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.graphql.test.tester.HttpGraphQlTester
import org.springframework.graphql.test.tester.WebSocketGraphQlTester
import org.springframework.test.util.TestSocketUtils
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient
import java.time.Duration
import java.util.concurrent.CompletableFuture

class RunningPod(
    val name: String,
    kafkaBrokers: String,
) : AutoCloseable {

    private val port = TestSocketUtils.findAvailableTcpPort()

    private val sidecar = EventRelaySidecarProcess(name, port, kafkaBrokers)

    private val context =
        SpringApplicationBuilder(ExampleApplication::class.java).run(
            "--server.port=$port",
            "--pod.name=$name",
            "--event-relay-sidecar.url=${sidecar.url}",
            "--order-fulfillment.step-delay=2s",
        )

    private val localSubscribers = context.getBean(LocalOrderSubscribersAdapter::class.java)

    private val httpTester =
        HttpGraphQlTester.create(WebTestClient.bindToServer().baseUrl("http://localhost:$port/api/graphql").build())

    private val webSocketTester =
        WebSocketGraphQlTester.builder("ws://localhost:$port/api/graphql-ws", ReactorNettyWebSocketClient()).build()

    fun awaitReadiness() = sidecar.awaitReadiness()

    fun awaitActiveSubscriptions(count: Int) {
        await atMost Duration.ofSeconds(10) until { localSubscribers.activeSubscriptions() == count }
    }

    fun placeOrder(
        article: String,
        quantity: Int,
    ): String {
        val response =
            httpTester
                .documentName("placeOrder")
                .variable("article", article)
                .variable("quantity", quantity)
                .execute()
        response.path("placeOrder.placedOnPod").entity(String::class.java).isEqualTo(name)
        return response.path("placeOrder.id").entity(String::class.java).get()
    }

    fun trackOrder(
        orderId: String,
        expectedChanges: Long,
    ): CompletableFuture<List<OrderStatusChangeView>> =
        webSocketTester
            .documentName("orderStatus")
            .variable("orderId", orderId)
            .executeSubscription()
            .toFlux("orderStatus", OrderStatusChangeView::class.java)
            .take(expectedChanges)
            .collectList()
            .toFuture()
            .thenApply { requireNotNull(it) }

    override fun close() {
        webSocketTester.stop().block()
        context.close()
        sidecar.close()
    }

    data class OrderStatusChangeView(
        val orderId: String,
        val status: String,
        val changedOnPod: String,
    )
}
