package de.emaarco.podbroadcast

import de.emaarco.podbroadcast.PodBroadcaster.Companion.EVENT_TYPE_HEADER
import de.emaarco.podbroadcast.PodBroadcaster.ReceivedEvent
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener

class PodBroadcastListener(
    private val broadcaster: PodBroadcaster,
    val topic: String,
    val consumerGroup: String,
) {

    @KafkaListener(
        id = LISTENER_ID,
        topics = ["#{__listener.topic}"],
        groupId = "#{__listener.consumerGroup}",
        properties = ["auto.offset.reset=latest"],
    )
    fun onEvent(record: ConsumerRecord<String, String>) {
        val type =
            record
                .headers()
                .lastHeader(EVENT_TYPE_HEADER)
                ?.value()
                ?.decodeToString() ?: return
        broadcaster.deliver(ReceivedEvent(type, record.value()))
    }

    internal companion object {
        const val LISTENER_ID = "pod-broadcast"
    }
}
