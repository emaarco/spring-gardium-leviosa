package de.emaarco.example.application.service

import de.emaarco.example.application.port.inbound.NotifyOrderSubscribersUseCase
import de.emaarco.example.application.port.outbound.LocalOrderSubscribersPort
import de.emaarco.example.domain.OrderStatusChange
import org.springframework.stereotype.Service

@Service
class NotifyOrderSubscribersService(
    private val subscribers: LocalOrderSubscribersPort,
) : NotifyOrderSubscribersUseCase {
    override fun notifySubscribers(change: OrderStatusChange) = subscribers.deliver(change)
}
