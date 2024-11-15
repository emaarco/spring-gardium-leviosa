package de.emaarco.example.adapter.`in`.rest

import de.emaarco.example.adapter.`in`.shared.CustomRequestHeaders.X_USER_ID
import de.emaarco.example.adapter.`in`.shared.TaskDto
import de.emaarco.example.adapter.`in`.shared.TaskInput
import de.emaarco.example.application.port.`in`.AddTaskUseCase
import mu.KotlinLogging
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/tasks")
class AddTaskRestController(private val useCase: AddTaskUseCase) {

    private val log = KotlinLogging.logger {}

    @PostMapping
    fun addTask(
        @RequestBody input: TaskInput,
        @RequestHeader(X_USER_ID) userId: String
    ): TaskDto {
        log.debug { "Received REST-request to add task: $input" }
        val command = input.toAddTaskCommand(userId)
        val task = useCase.addTask(command)
        return TaskDto.from(task)
    }
}
