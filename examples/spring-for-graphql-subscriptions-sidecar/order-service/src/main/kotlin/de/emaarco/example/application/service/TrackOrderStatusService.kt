package de.emaarco.example.application.service

import de.emaarco.example.application.port.inbound.TrackOrderStatusQuery
import de.emaarco.example.application.port.outbound.LocalOrderSubscribersPort
import de.emaarco.example.domain.OrderId
import org.springframework.stereotype.Service

@Service
class TrackOrderStatusService(
    private val subscribers: LocalOrderSubscribersPort,
) : TrackOrderStatusQuery {
    override fun trackOrderStatus(orderId: OrderId) = subscribers.changesOf(orderId)
}
