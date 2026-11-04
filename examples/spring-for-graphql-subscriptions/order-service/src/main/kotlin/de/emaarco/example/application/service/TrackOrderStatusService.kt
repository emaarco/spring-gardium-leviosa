package de.emaarco.example.application.service

import de.emaarco.example.application.port.inbound.TrackOrderStatusQuery
import de.emaarco.example.application.port.outbound.OrderStatusBroadcastPort
import de.emaarco.example.domain.OrderId
import org.springframework.stereotype.Service

@Service
class TrackOrderStatusService(
    private val broadcast: OrderStatusBroadcastPort,
) : TrackOrderStatusQuery {
    override fun trackOrderStatus(orderId: OrderId) = broadcast.changesOf(orderId)
}
