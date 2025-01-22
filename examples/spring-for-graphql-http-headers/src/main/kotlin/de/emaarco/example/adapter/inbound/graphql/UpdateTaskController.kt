package de.emaarco.example.adapter.inbound.graphql

import de.emaarco.example.adapter.inbound.shared.TaskDto
import de.emaarco.example.adapter.inbound.shared.TaskInput
import de.emaarco.example.application.port.inbound.UpdateTaskUseCase
import de.emaarco.example.domain.TaskId
import mu.KotlinLogging
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.stereotype.Controller
import java.util.*

@Controller
class UpdateTaskController(private val useCase: UpdateTaskUseCase) {

    private val log = KotlinLogging.logger {}

    @MutationMapping
    fun updateTask(
        @Argument taskId: UUID,
        @Argument payload: TaskInput,
    ): TaskDto {
        log.debug { "Received graphql-request to update task with id '$taskId': $payload" }
        val command = payload.toUpdateTaskCommand(TaskId(taskId))
        val updatedTask = useCase.updateTask(command)
        return TaskDto.from(updatedTask)
    }
}
