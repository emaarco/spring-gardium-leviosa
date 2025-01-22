package de.emaarco.example.adapter.inbound.graphql

import de.emaarco.example.application.port.inbound.DeleteTaskUseCase
import de.emaarco.example.domain.TaskId
import mu.KotlinLogging
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.stereotype.Controller
import java.util.*

@Controller
class DeleteTaskController(
    private val query: DeleteTaskUseCase,
) {

    private val log = KotlinLogging.logger {}

    @MutationMapping
    fun deleteTask(
        @Argument taskId: UUID,
    ): Boolean {
        log.debug { "Received graphql-request to delete task with id '$taskId'" }
        query.deleteTask(TaskId(taskId))
        return true
    }
}
