package de.emaarco.example.application.port.outbound

import de.emaarco.example.domain.OrderId
import de.emaarco.example.domain.OrderStatusChange
import kotlinx.coroutines.flow.Flow

interface LocalOrderSubscribersPort {
    fun deliver(change: OrderStatusChange)

    fun changesOf(orderId: OrderId): Flow<OrderStatusChange>
}
