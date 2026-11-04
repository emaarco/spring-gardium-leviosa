package de.emaarco.example.adapter.inbound.fulfillment

import de.emaarco.example.application.port.inbound.ChangeOrderStatusUseCase
import de.emaarco.example.domain.FulfillmentUpdate
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class FulfillmentUpdateListener(
    private val useCase: ChangeOrderStatusUseCase,
) {
    @EventListener
    fun onFulfillmentUpdate(update: FulfillmentUpdate) = useCase.changeOrderStatus(update)
}
