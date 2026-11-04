package de.emaarco.example.adapter.outbound

import de.emaarco.example.application.port.outbound.LocalOrderSubscribersPort
import de.emaarco.example.domain.OrderId
import de.emaarco.example.domain.OrderStatusChange
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import org.springframework.stereotype.Component

@Component
class LocalOrderSubscribersAdapter : LocalOrderSubscribersPort {

    private val log = KotlinLogging.logger {}

    private val changes =
        MutableSharedFlow<OrderStatusChange>(
            extraBufferCapacity = 256,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )

    override fun deliver(change: OrderStatusChange) {
        log.info {
            "Delivering status of order ${change.orderId.value} to ${activeSubscriptions()} local subscription(s)"
        }
        changes.tryEmit(change)
    }

    override fun changesOf(orderId: OrderId) = changes.filter { it.orderId == orderId }

    fun activeSubscriptions() = changes.subscriptionCount.value
}
