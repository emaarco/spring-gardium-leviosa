package de.emaarco.example.application.port.inbound

import de.emaarco.example.domain.OrderId
import de.emaarco.example.domain.OrderStatusChange
import kotlinx.coroutines.flow.Flow

interface TrackOrderStatusQuery {
    fun trackOrderStatus(orderId: OrderId): Flow<OrderStatusChange>
}
