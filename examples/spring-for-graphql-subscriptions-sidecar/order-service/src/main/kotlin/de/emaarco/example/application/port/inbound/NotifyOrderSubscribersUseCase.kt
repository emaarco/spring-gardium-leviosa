package de.emaarco.example.application.port.inbound

import de.emaarco.example.domain.OrderStatusChange

interface NotifyOrderSubscribersUseCase {
    fun notifySubscribers(change: OrderStatusChange)
}
