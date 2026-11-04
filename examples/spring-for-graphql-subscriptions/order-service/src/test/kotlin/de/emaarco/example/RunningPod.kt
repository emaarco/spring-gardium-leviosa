package de.emaarco.example

import de.emaarco.podbroadcast.PodBroadcastReceiver
import org.awaitility.kotlin.atMost
import org.awaitility.kotlin.await
import org.awaitility.kotlin.until
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.graphql.test.tester.HttpGraphQlTester
import org.springframework.graphql.test.tester.WebSocketGraphQlTester
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.client.RestClient
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient
import java.time.Duration
import java.util.concurrent.CompletableFuture

class RunningPod(
    val name: String,
    kafkaBrokers: String,
) : AutoCloseable {

    private val context =
        SpringApplicationBuilder(ExampleApplication::class.java).run(
            "--server.port=0",
            "--pod.name=$name",
            "--spring.kafka.bootstrap-servers=$kafkaBrokers",
            "--order-fulfillment.step-delay=2s",
        )

    private val port = context.environment.getRequiredProperty("local.server.port")

    private val broadcastReceiver = context.getBean(PodBroadcastReceiver::class.java)

    private val httpTester =
        HttpGraphQlTester.create(WebTestClient.bindToServer().baseUrl("http://localhost:$port/api/graphql").build())

    private val webSocketTester =
        WebSocketGraphQlTester.builder("ws://localhost:$port/api/graphql-ws", ReactorNettyWebSocketClient()).build()

    fun awaitReadiness() {
        await atMost Duration.ofSeconds(30) until { isReady() }
    }

    fun awaitActiveSubscriptions(count: Int) {
        await atMost Duration.ofSeconds(10) until { broadcastReceiver.activeSubscribers() == count }
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

    fun trackOrderUntilItCompletes(orderId: String): CompletableFuture<List<OrderStatusChangeView>> =
        webSocketTester
            .documentName("orderStatus")
            .variable("orderId", orderId)
            .executeSubscription()
            .toFlux("orderStatus", OrderStatusChangeView::class.java)
            .collectList()
            .toFuture()
            .thenApply { requireNotNull(it) }

    override fun close() {
        webSocketTester.stop().block()
        context.close()
    }

    private fun isReady() =
        runCatching {
            RestClient
                .create()
                .get()
                .uri("http://localhost:$port/actuator/health/readiness")
                .retrieve()
                .toBodilessEntity()
        }.isSuccess

    data class OrderStatusChangeView(
        val orderId: String,
        val status: String,
        val changedOnPod: String,
    )
}
