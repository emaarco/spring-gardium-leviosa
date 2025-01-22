package de.emaarco.example.adapter.inbound.graphql

import de.emaarco.example.adapter.inbound.shared.CustomRequestHeaders.X_USER_ID
import de.emaarco.example.adapter.inbound.shared.TaskDto
import de.emaarco.example.adapter.inbound.shared.TaskInput
import de.emaarco.example.application.port.inbound.AddTaskUseCase
import mu.KotlinLogging
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.ContextValue
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.stereotype.Controller

@Controller
class AddTaskController(
    private val useCase: AddTaskUseCase,
) {

    private val log = KotlinLogging.logger {}

    @MutationMapping
    fun addTask(
        @Argument payload: TaskInput,
        @ContextValue(X_USER_ID) userId: String,
    ): TaskDto {
        log.debug { "Received graphql-request to add task: $payload" }
        val command = payload.toAddTaskCommand(userId)
        val task = useCase.addTask(command)
        return TaskDto.from(task)
    }
}
