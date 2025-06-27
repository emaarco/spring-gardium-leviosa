package de.emaarco.example.adapter.inbound.rest

import de.emaarco.example.adapter.inbound.shared.CustomRequestHeaders.X_USER_ID
import de.emaarco.example.adapter.inbound.shared.TaskDto
import de.emaarco.example.adapter.inbound.shared.TaskInput
import de.emaarco.example.application.port.inbound.AddTaskUseCase
import mu.KotlinLogging
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/tasks")
class AddTaskRestController(
    private val useCase: AddTaskUseCase,
) {

    private val log = KotlinLogging.logger {}

    @PostMapping
    fun addTask(
        @RequestBody input: TaskInput,
        @RequestHeader(X_USER_ID) userId: String,
    ): TaskDto {
        log.debug { "Received REST-request to add task: $input" }
        val command = input.toAddTaskCommand(userId)
        val task = useCase.addTask(command)
        return TaskDto.from(task)
    }
}
