package de.emaarco.example.adapter.inbound.graphql

import de.emaarco.example.adapter.inbound.shared.OrderDto
import de.emaarco.example.application.port.inbound.PlaceOrderUseCase
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.stereotype.Controller

@Controller
class PlaceOrderController(
    private val useCase: PlaceOrderUseCase,
) {

    private val log = KotlinLogging.logger {}

    @MutationMapping
    fun placeOrder(
        @Argument article: String,
        @Argument quantity: Int,
    ): OrderDto {
        log.debug { "Received graphql-request to order $quantity x $article" }
        val order = useCase.placeOrder(article, quantity)
        return OrderDto.from(order)
    }
}
