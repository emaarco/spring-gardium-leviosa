package de.emaarco.example.adapter.inbound.graphql

import de.emaarco.example.adapter.inbound.shared.TaskDto
import de.emaarco.example.application.port.inbound.CompleteTaskUseCase
import de.emaarco.example.domain.TaskId
import mu.KotlinLogging
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.stereotype.Controller
import java.util.*

@Controller
class CompleteTaskController(
    private val useCase: CompleteTaskUseCase,
) {

    private val log = KotlinLogging.logger {}

    @MutationMapping
    fun completeTask(
        @Argument taskId: UUID,
    ): TaskDto {
        log.debug { "Received graphql-request to complete task with id '$taskId'" }
        val completedTask = useCase.completeTask(TaskId(taskId))
        return TaskDto.from(completedTask)
    }
}
