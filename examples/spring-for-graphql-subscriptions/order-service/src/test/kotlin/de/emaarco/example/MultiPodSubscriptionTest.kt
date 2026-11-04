package de.emaarco.example

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.kafka.test.EmbeddedKafkaKraftBroker
import java.util.concurrent.TimeUnit.SECONDS

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MultiPodSubscriptionTest {

    private val kafka = EmbeddedKafkaKraftBroker(1, 1).apply { afterPropertiesSet() }
    private val pod1 = RunningPod("pod-1", kafka.brokersAsString)
    private val pod2 = RunningPod("pod-2", kafka.brokersAsString)

    @BeforeAll
    fun awaitReadiness() {
        pod1.awaitReadiness()
        pod2.awaitReadiness()
    }

    @AfterEach
    fun awaitClosedSubscriptions() {
        pod1.awaitActiveSubscriptions(0)
        pod2.awaitActiveSubscriptions(0)
    }

    @AfterAll
    fun shutdown() {
        pod1.close()
        pod2.close()
        kafka.destroy()
    }

    @Test
    fun `the client gets every change until shipping although its order is on pod-2 and its subscription on pod-1`() {
        val orderId = pod2.placeOrder("Espresso machine", 1)

        val statusChanges = pod1.trackOrderUntilItCompletes(orderId)
        pod1.awaitActiveSubscriptions(1)

        val changes = statusChanges.get(20, SECONDS)
        assertThat(changes.map { it.status }).containsExactly("PAYMENT_CONFIRMED", "READY_TO_SHIP", "SHIPPED")
        assertThat(changes.map { it.changedOnPod }).containsOnly("pod-2")
    }

    @Test
    fun `the status changes take the same path when order and subscription are on the same pod`() {
        val orderId = pod1.placeOrder("Coffee beans", 3)

        val statusChanges = pod1.trackOrderUntilItCompletes(orderId)
        pod1.awaitActiveSubscriptions(1)

        val changes = statusChanges.get(20, SECONDS)
        assertThat(changes.map { it.status }).containsExactly("PAYMENT_CONFIRMED", "READY_TO_SHIP", "SHIPPED")
        assertThat(changes.map { it.changedOnPod }).containsOnly("pod-1")
    }
}
