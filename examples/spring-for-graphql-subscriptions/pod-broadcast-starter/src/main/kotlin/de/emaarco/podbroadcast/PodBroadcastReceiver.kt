package de.emaarco.podbroadcast

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapNotNull
import tools.jackson.databind.json.JsonMapper

/**
 * Streams the events that any pod of this service published, filtered by type and optionally by key.
 * Keeps no history: a subscriber only gets events that arrive after it started collecting.
 * Skips events it cannot read, so one bad event never ends the streams of the other subscribers.
 */
class PodBroadcastReceiver(
    private val jsonMapper: JsonMapper,
) {

    private val log = KotlinLogging.logger {}

    private val eventsFromAllPods =
        MutableSharedFlow<ReceivedEvent>(
            extraBufferCapacity = 256,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )

    fun <T : Any> receive(
        type: Class<T>,
        key: String? = null,
    ): Flow<T> =
        eventsFromAllPods
            .filter { it.type == type.simpleName && (key == null || it.key == key) }
            .mapNotNull { readOrSkip(it, type) }

    inline fun <reified T : Any> receive(key: String? = null): Flow<T> = receive(T::class.java, key)

    fun activeSubscribers() = eventsFromAllPods.subscriptionCount.value

    internal fun deliver(
        type: String,
        key: String?,
        payload: String,
    ) {
        eventsFromAllPods.tryEmit(ReceivedEvent(type, key, payload))
    }

    private fun <T : Any> readOrSkip(
        event: ReceivedEvent,
        type: Class<T>,
    ): T? =
        runCatching { jsonMapper.readValue(event.payload, type) }
            .onFailure { log.warn(it) { "Skipping unreadable ${event.type} event with key ${event.key}" } }
            .getOrNull()

    private data class ReceivedEvent(
        val type: String,
        val key: String?,
        val payload: String,
    )
}
