package de.emaarco.podbroadcast

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import tools.jackson.databind.json.JsonMapper

class PodBroadcastReceiver(
    private val jsonMapper: JsonMapper,
) {

    private val eventsFromAllPods =
        MutableSharedFlow<ReceivedEvent>(
            extraBufferCapacity = 256,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )

    fun <T : Any> receive(type: Class<T>): Flow<T> =
        eventsFromAllPods
            .filter { it.type == type.name }
            .map { jsonMapper.readValue(it.payload, type) }

    inline fun <reified T : Any> receive(): Flow<T> = receive(T::class.java)

    fun activeSubscribers() = eventsFromAllPods.subscriptionCount.value

    internal fun deliver(
        type: String,
        payload: String,
    ) {
        eventsFromAllPods.tryEmit(ReceivedEvent(type, payload))
    }

    private data class ReceivedEvent(
        val type: String,
        val payload: String,
    )
}
