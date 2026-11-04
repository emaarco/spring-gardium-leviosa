package de.emaarco.podbroadcast

import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.kafka.core.KafkaTemplate
import tools.jackson.databind.json.JsonMapper

class PodBroadcastPublisher(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val jsonMapper: JsonMapper,
    private val topic: String,
) {

    fun publishToAllPods(
        key: String,
        event: Any,
    ) {
        val record = ProducerRecord(topic, key, jsonMapper.writeValueAsString(event))
        record.headers().add(PodBroadcastHeaders.EVENT_TYPE, event.javaClass.name.toByteArray())
        kafkaTemplate.send(record).join()
    }
}
