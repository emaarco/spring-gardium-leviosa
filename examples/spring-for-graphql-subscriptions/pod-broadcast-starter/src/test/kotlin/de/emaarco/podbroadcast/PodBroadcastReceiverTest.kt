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
import org.junit.jupiter.api.Test
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.kotlinModule
import java.time.Duration
import kotlin.time.Duration.Companion.seconds

class PodBroadcastReceiverTest {

    private val receiver = PodBroadcastReceiver(JsonMapper.builder().addModule(kotlinModule()).build())

    @Test
    fun `skips an unreadable event and keeps streaming the following ones`() {
        runBlocking {
            val greeting = async(Dispatchers.IO) { receiver.receive<Greeting>().first() }
            awaitSubscribers(1)

            receiver.deliver("Greeting", "greeting-1", """{"unexpected":true}""")
            receiver.deliver("Greeting", "greeting-2", """{"text":"hello"}""")

            withTimeout(5.seconds) { assertThat(greeting.await()).isEqualTo(Greeting("hello")) }
        }
    }

    @Test
    fun `only streams the events with the requested key`() {
        runBlocking {
            val greeting = async(Dispatchers.IO) { receiver.receive<Greeting>(key = "order-2").first() }
            awaitSubscribers(1)

            receiver.deliver("Greeting", "order-1", """{"text":"for order 1"}""")
            receiver.deliver("Greeting", "order-2", """{"text":"for order 2"}""")

            withTimeout(5.seconds) { assertThat(greeting.await()).isEqualTo(Greeting("for order 2")) }
        }
    }

    private fun awaitSubscribers(count: Int) {
        await atMost Duration.ofSeconds(5) until { receiver.activeSubscribers() == count }
    }

    data class Greeting(
        val text: String,
    )
}
