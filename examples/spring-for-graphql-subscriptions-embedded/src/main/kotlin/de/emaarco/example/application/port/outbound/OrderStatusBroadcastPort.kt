package de.emaarco.example.application.port.outbound

import de.emaarco.example.domain.OrderStatusChange

interface OrderStatusBroadcastPort {
    fun broadcastToAllPods(change: OrderStatusChange)
}
