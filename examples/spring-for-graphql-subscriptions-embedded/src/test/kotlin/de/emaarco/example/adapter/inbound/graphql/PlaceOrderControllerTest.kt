package de.emaarco.example.adapter.inbound.graphql

import com.ninjasquad.springmockk.MockkBean
import de.emaarco.example.application.port.inbound.PlaceOrderUseCase
import de.emaarco.example.domain.Order
import de.emaarco.example.domain.OrderId
import de.emaarco.example.domain.OrderStatus
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.graphql.test.autoconfigure.GraphQlTest
import org.springframework.graphql.test.tester.GraphQlTester

@GraphQlTest(controllers = [PlaceOrderController::class])
class PlaceOrderControllerTest {

    @Autowired
    private lateinit var graphQlTester: GraphQlTester

    @MockkBean
    private lateinit var placeOrderUseCase: PlaceOrderUseCase

    @Test
    fun `returns the placed order with the id assigned by the server`() {
        every { placeOrderUseCase.placeOrder("Espresso machine", 1) } returns
            Order(OrderId("order-42"), "Espresso machine", 1, OrderStatus.PLACED, "pod-2")

        graphQlTester
            .documentName("placeOrder")
            .variable("article", "Espresso machine")
            .variable("quantity", 1)
            .execute()
            .path("placeOrder.id")
            .entity(String::class.java)
            .isEqualTo("order-42")
            .path("placeOrder.status")
            .entity(String::class.java)
            .isEqualTo("PLACED")
            .path("placeOrder.placedOnPod")
            .entity(String::class.java)
            .isEqualTo("pod-2")
    }
}
