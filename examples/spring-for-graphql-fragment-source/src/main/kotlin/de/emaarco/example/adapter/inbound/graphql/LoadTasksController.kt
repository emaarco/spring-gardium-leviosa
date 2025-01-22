package de.emaarco.example.adapter.inbound.graphql

import de.emaarco.example.adapter.inbound.shared.TaskDto
import de.emaarco.example.application.port.inbound.LoadTasksQuery
import mu.KotlinLogging
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
class LoadTasksController(
    private val query: LoadTasksQuery,
) {

    private val log = KotlinLogging.logger {}

    @QueryMapping
    fun tasks(): List<TaskDto> {
        log.debug { "Received graphql-request to load tasks" }
        val tasks = query.loadTasks()
        return tasks.map { TaskDto.from(it) }
    }
}
