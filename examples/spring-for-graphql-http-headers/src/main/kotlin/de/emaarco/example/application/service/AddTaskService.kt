package de.emaarco.example.application.service

import de.emaarco.example.application.port.`in`.AddTaskUseCase
import de.emaarco.example.application.port.out.TaskRepository
import de.emaarco.example.domain.Task
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class AddTaskService(
    private val repository: TaskRepository
) : AddTaskUseCase {

    private val log = KotlinLogging.logger {}

    override fun addTask(command: AddTaskUseCase.Command): Task {
        val task = buildTask(command)
        repository.save(task)
        log.info { "Task successfully added: $task" }
        return task
    }

    private fun buildTask(command: AddTaskUseCase.Command) = Task(
        title = command.title,
        description = command.description,
        createdBy = command.createdBy,
    )

}