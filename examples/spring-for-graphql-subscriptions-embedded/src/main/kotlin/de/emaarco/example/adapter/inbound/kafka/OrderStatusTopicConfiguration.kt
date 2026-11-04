package de.emaarco.example.adapter.inbound.kafka

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder

@Configuration
class OrderStatusTopicConfiguration {

    @Bean
    fun orderStatusTopic(
        @Value("\${order-status-changes.topic}") topic: String,
    ) = TopicBuilder.name(topic).partitions(3).build()
}
