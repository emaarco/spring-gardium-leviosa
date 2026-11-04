package de.emaarco.example.adapter.inbound.graphql

import de.emaarco.example.adapter.inbound.shared.OrderStatusChangeDto
import de.emaarco.example.application.port.inbound.TrackOrderStatusQuery
import de.emaarco.example.domain.OrderId
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.SubscriptionMapping
import org.springframework.stereotype.Controller

@Controller
class OrderStatusController(
    private val query: TrackOrderStatusQuery,
) {

    private val log = KotlinLogging.logger {}

    @SubscriptionMapping
    fun orderStatus(
        @Argument orderId: String,
    ): Flow<OrderStatusChangeDto> {
        log.debug { "Received graphql-subscription for the status of order $orderId" }
        val changes = query.trackOrderStatus(OrderId(orderId))
        return changes.map { OrderStatusChangeDto.from(it) }
    }
}
