package de.emaarco.example.adapter.inbound.spring

import de.emaarco.example.adapter.inbound.shared.TaskInput
import de.emaarco.example.application.port.inbound.AddTaskUseCase
import jakarta.annotation.PostConstruct
import mu.KotlinLogging
import org.springframework.stereotype.Component

/**
 * Inserts test-tasks into the in-memory repository post startup.
 */
@Component
class TaskDataSink(private val useCase: AddTaskUseCase) {

    private val log = KotlinLogging.logger {}

    @PostConstruct
    fun addTasks() {
        log.debug { "Inserting test-tasks..." }
        tasks.forEach {
            val command = it.toAddTaskCommand("test-user")
            useCase.addTask(command)
        }
    }

    private val tasks =
        listOf(
            TaskInput("Task 1", "Description 1"),
            TaskInput("Task 2", "Description 2"),
            TaskInput("Task 3", "Description 3"),
            TaskInput("Task 4", "Description 4"),
            TaskInput("Task 5", "Description 5"),
        )
}
