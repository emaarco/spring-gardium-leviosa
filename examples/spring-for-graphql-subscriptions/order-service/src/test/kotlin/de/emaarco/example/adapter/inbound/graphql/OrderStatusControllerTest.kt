package de.emaarco.example.adapter.inbound.graphql

import com.ninjasquad.springmockk.MockkBean
import de.emaarco.example.application.port.inbound.TrackOrderStatusQuery
import de.emaarco.example.domain.OrderId
import de.emaarco.example.domain.OrderStatus
import de.emaarco.example.domain.OrderStatusChange
import io.mockk.every
import kotlinx.coroutines.flow.flowOf
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.graphql.test.autoconfigure.GraphQlTest
import org.springframework.graphql.test.tester.GraphQlTester
import java.time.Duration
import java.time.Instant

@GraphQlTest(controllers = [OrderStatusController::class])
class OrderStatusControllerTest {

    @Autowired
    private lateinit var graphQlTester: GraphQlTester

    @MockkBean
    private lateinit var trackOrderStatusQuery: TrackOrderStatusQuery

    private val orderId = OrderId("order-42")

    @Test
    fun `streams every status change of the order as it happens`() {
        every { trackOrderStatusQuery.trackOrderStatus(orderId) } returns
            flowOf(change(OrderStatus.PAYMENT_CONFIRMED), change(OrderStatus.SHIPPED))

        val statuses = subscribe("orderStatus.status")

        assertThat(statuses).containsExactly("PAYMENT_CONFIRMED", "SHIPPED")
    }

    @Test
    fun `tells on which pod the status changed`() {
        every { trackOrderStatusQuery.trackOrderStatus(orderId) } returns flowOf(change(OrderStatus.SHIPPED))

        val pods = subscribe("orderStatus.changedOnPod")

        assertThat(pods).containsExactly("pod-2")
    }

    private fun subscribe(path: String) =
        graphQlTester
            .documentName("orderStatus")
            .variable("orderId", orderId.value)
            .executeSubscription()
            .toFlux(path, String::class.java)
            .collectList()
            .block(Duration.ofSeconds(5))

    private fun change(status: OrderStatus) =
        OrderStatusChange(orderId, status, Instant.parse("2026-12-11T08:15:00Z"), "pod-2")
}
