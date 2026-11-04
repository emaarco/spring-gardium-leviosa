package de.emaarco.podbroadcast

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.atMost
import org.awaitility.kotlin.await
import org.awaitility.kotlin.until
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.health.contributor.Status
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.kafka.test.EmbeddedKafkaKraftBroker
import java.time.Duration
import kotlin.time.Duration.Companion.seconds

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PodBroadcasterTest {

    private val kafka = EmbeddedKafkaKraftBroker(1, 1).apply { afterPropertiesSet() }
    private val pod1 = startPod("pod-1")
    private val pod2 = startPod("pod-2")

    @BeforeAll
    fun awaitReadiness() {
        await atMost Duration.ofSeconds(30) until { isReady(pod1) && isReady(pod2) }
    }

    @AfterEach
    fun awaitStoppedReceivers() {
        awaitReceivers(pod1, 0)
        awaitReceivers(pod2, 0)
    }

    @AfterAll
    fun shutdown() {
        pod1.close()
        pod2.close()
        kafka.destroy()
    }

    @Test
    fun `every pod receives the event that one pod published, including the publishing pod`() {
        runBlocking {
            val onPod1 = async(Dispatchers.IO) { broadcaster(pod1).receive<Greeting>().first() }
            val onPod2 = async(Dispatchers.IO) { broadcaster(pod2).receive<Greeting>().first() }
            awaitReceivers(pod1, 1)
            awaitReceivers(pod2, 1)

            broadcaster(pod2).publishToAllPods("greeting-1", Greeting("hello from pod-2"))

            withTimeout(10.seconds) {
                assertThat(onPod1.await()).isEqualTo(Greeting("hello from pod-2"))
                assertThat(onPod2.await()).isEqualTo(Greeting("hello from pod-2"))
            }
        }
    }

    @Test
    fun `a receiver only gets the events of the type it asked for`() {
        runBlocking {
            val greetings = async(Dispatchers.IO) { broadcaster(pod1).receive<Greeting>().first() }
            awaitReceivers(pod1, 1)

            broadcaster(pod2).publishToAllPods("farewell-1", Farewell("bye", 3))
            broadcaster(pod2).publishToAllPods("greeting-2", Greeting("hello again"))

            withTimeout(10.seconds) {
                assertThat(greetings.await()).isEqualTo(Greeting("hello again"))
            }
        }
    }

    private fun startPod(name: String) =
        SpringApplicationBuilder(PodBroadcastTestApplication::class.java).run(
            "--spring.application.name=pod-broadcast-test",
            "--pod-broadcast.pod-name=$name",
            "--spring.kafka.bootstrap-servers=${kafka.brokersAsString}",
        )

    private fun broadcaster(pod: ConfigurableApplicationContext) = pod.getBean(PodBroadcaster::class.java)

    private fun isReady(pod: ConfigurableApplicationContext) =
        pod.getBean(PodBroadcastHealthIndicator::class.java).health().status == Status.UP

    private fun awaitReceivers(
        pod: ConfigurableApplicationContext,
        count: Int,
    ) {
        await atMost Duration.ofSeconds(10) until { broadcaster(pod).activeReceivers() == count }
    }

    data class Greeting(
        val text: String,
    )

    data class Farewell(
        val text: String,
        val waves: Int,
    )
}
