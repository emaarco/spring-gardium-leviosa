package de.emaarco.example.application.port.inbound

import de.emaarco.example.domain.FulfillmentUpdate

interface ChangeOrderStatusUseCase {
    fun changeOrderStatus(update: FulfillmentUpdate)
}
