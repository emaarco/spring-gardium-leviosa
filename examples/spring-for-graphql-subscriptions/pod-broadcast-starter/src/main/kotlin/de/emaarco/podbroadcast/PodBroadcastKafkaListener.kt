package de.emaarco.podbroadcast

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener

/**
 * Reads the broadcast topic with a consumer group of its own per pod, so that every pod gets every event,
 * and hands each event to the [PodBroadcastReceiver] of this pod. Starts at the latest offset: only live events count.
 */
class PodBroadcastKafkaListener(
    private val receiver: PodBroadcastReceiver,
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
                .lastHeader(PodBroadcastHeaders.EVENT_TYPE)
                ?.value()
                ?.decodeToString() ?: return
        receiver.deliver(type, record.value())
    }

    internal companion object {
        const val LISTENER_ID = "pod-broadcast"
    }
}
