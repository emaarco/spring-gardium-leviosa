package de.emaarco.example.application.port.outbound

import de.emaarco.example.domain.OrderId
import de.emaarco.example.domain.OrderStatusChange
import kotlinx.coroutines.flow.Flow

interface OrderStatusBroadcastPort {
    fun broadcastToAllPods(change: OrderStatusChange)

    fun changesOf(orderId: OrderId): Flow<OrderStatusChange>
}
