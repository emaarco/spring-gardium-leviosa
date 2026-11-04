package de.emaarco.example.application.port.outbound

import de.emaarco.example.domain.Order

interface OrderFulfillmentPort {
    fun startFulfillment(order: Order)
}
