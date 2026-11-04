package de.emaarco.podbroadcast

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.kafka.core.KafkaTemplate
import tools.jackson.databind.json.JsonMapper

class PodBroadcaster(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val jsonMapper: JsonMapper,
    private val topic: String,
) {

    private val receivedEvents =
        MutableSharedFlow<ReceivedEvent>(
            extraBufferCapacity = 256,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )

    fun publishToAllPods(
        key: String,
        event: Any,
    ) {
        val record = ProducerRecord(topic, key, jsonMapper.writeValueAsString(event))
        record.headers().add(EVENT_TYPE_HEADER, event.javaClass.name.toByteArray())
        kafkaTemplate.send(record).join()
    }

    fun <T : Any> receive(type: Class<T>): Flow<T> =
        receivedEvents
            .filter { it.type == type.name }
            .map { jsonMapper.readValue(it.payload, type) }

    inline fun <reified T : Any> receive(): Flow<T> = receive(T::class.java)

    fun activeReceivers() = receivedEvents.subscriptionCount.value

    internal fun deliver(event: ReceivedEvent) {
        receivedEvents.tryEmit(event)
    }

    internal data class ReceivedEvent(
        val type: String,
        val payload: String,
    )

    internal companion object {
        const val EVENT_TYPE_HEADER = "pod-broadcast-event-type"
    }
}
