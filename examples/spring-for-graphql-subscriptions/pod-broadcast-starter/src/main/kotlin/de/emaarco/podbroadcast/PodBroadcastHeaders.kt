package de.emaarco.podbroadcast

/**
 * Kafka headers shared by [PodBroadcastPublisher] and [PodBroadcastKafkaListener].
 * [EVENT_TYPE] carries the class name of the event, so that [PodBroadcastReceiver] can filter by type.
 */
internal object PodBroadcastHeaders {
    const val EVENT_TYPE = "pod-broadcast-event-type"
}
