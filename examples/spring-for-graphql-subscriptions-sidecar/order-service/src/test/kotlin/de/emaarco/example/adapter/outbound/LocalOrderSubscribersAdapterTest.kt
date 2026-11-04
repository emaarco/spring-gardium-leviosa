package de.emaarco.example.adapter.outbound

import de.emaarco.example.domain.OrderId
import de.emaarco.example.domain.OrderStatus
import de.emaarco.example.domain.OrderStatusChange
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant

class LocalOrderSubscribersAdapterTest {

    private val adapter = LocalOrderSubscribersAdapter()

    private val order = OrderId("order-42")
    private val otherOrder = OrderId("order-7")

    @Test
    fun `delivers a status change to every subscription of the order`() {
        runBlocking {
            val first = async(start = CoroutineStart.UNDISPATCHED) { adapter.changesOf(order).first() }
            val second = async(start = CoroutineStart.UNDISPATCHED) { adapter.changesOf(order).first() }

            adapter.deliver(change(order, OrderStatus.PAYMENT_CONFIRMED))

            assertThat(first.await().status).isEqualTo(OrderStatus.PAYMENT_CONFIRMED)
            assertThat(second.await().status).isEqualTo(OrderStatus.PAYMENT_CONFIRMED)
        }
    }

    @Test
    fun `only passes on the status changes of the tracked order`() {
        runBlocking {
            val tracking = async(start = CoroutineStart.UNDISPATCHED) { adapter.changesOf(order).take(2).toList() }

            adapter.deliver(change(order, OrderStatus.PAYMENT_CONFIRMED))
            adapter.deliver(change(otherOrder, OrderStatus.PAYMENT_CONFIRMED))
            adapter.deliver(change(order, OrderStatus.SHIPPED))

            assertThat(tracking.await().map { it.status }).containsExactly(
                OrderStatus.PAYMENT_CONFIRMED,
                OrderStatus.SHIPPED,
            )
        }
    }

    @Test
    fun `does not replay earlier status changes to a new subscription`() {
        runBlocking {
            adapter.deliver(change(order, OrderStatus.PAYMENT_CONFIRMED))
            val later = async(start = CoroutineStart.UNDISPATCHED) { adapter.changesOf(order).first() }

            adapter.deliver(change(order, OrderStatus.READY_TO_SHIP))

            assertThat(later.await().status).isEqualTo(OrderStatus.READY_TO_SHIP)
        }
    }

    @Test
    fun `counts the active subscriptions`() {
        runBlocking {
            assertThat(adapter.activeSubscriptions()).isZero()

            val subscription = async(start = CoroutineStart.UNDISPATCHED) { adapter.changesOf(order).first() }

            assertThat(adapter.activeSubscriptions()).isEqualTo(1)
            subscription.cancel()
        }
    }

    private fun change(
        orderId: OrderId,
        status: OrderStatus,
    ) = OrderStatusChange(orderId, status, Instant.parse("2026-12-11T08:15:00Z"), "pod-1")
}
