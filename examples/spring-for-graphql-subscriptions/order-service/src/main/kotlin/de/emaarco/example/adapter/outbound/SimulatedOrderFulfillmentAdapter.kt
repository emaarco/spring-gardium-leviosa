package de.emaarco.example.adapter.outbound

import de.emaarco.example.application.port.outbound.OrderFulfillmentPort
import de.emaarco.example.domain.FulfillmentUpdate
import de.emaarco.example.domain.Order
import de.emaarco.example.domain.OrderStatus
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit.MILLISECONDS

@Component
class SimulatedOrderFulfillmentAdapter(
    private val events: ApplicationEventPublisher,
    @Value("\${order-fulfillment.step-delay}") private val stepDelay: Duration,
) : OrderFulfillmentPort,
    DisposableBean {

    private val log = KotlinLogging.logger {}

    private val scheduler = Executors.newSingleThreadScheduledExecutor()

    override fun startFulfillment(order: Order) {
        FULFILLMENT_STEPS.forEachIndexed { index, status ->
            val delay = stepDelay.multipliedBy(index + 1L).toMillis()
            scheduler.schedule({ report(FulfillmentUpdate(order.id, status)) }, delay, MILLISECONDS)
        }
    }

    override fun destroy() {
        scheduler.shutdownNow()
    }

    private fun report(update: FulfillmentUpdate) {
        log.info { "Fulfillment reports ${update.status} for order ${update.orderId.value}" }
        runCatching { events.publishEvent(update) }
            .onFailure { log.error(it) { "Could not process ${update.status} of order ${update.orderId.value}" } }
    }

    private companion object {
        val FULFILLMENT_STEPS = listOf(OrderStatus.PAYMENT_CONFIRMED, OrderStatus.READY_TO_SHIP, OrderStatus.SHIPPED)
    }
}
