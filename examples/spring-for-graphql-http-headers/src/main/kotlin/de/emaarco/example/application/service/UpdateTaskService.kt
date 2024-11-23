package de.emaarco.example.application.service

import de.emaarco.example.application.port.`in`.UpdateTaskUseCase
import de.emaarco.example.application.port.out.TaskRepository
import de.emaarco.example.domain.Task
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class UpdateTaskService(
    private val repository: TaskRepository
) : UpdateTaskUseCase {

    private val log = KotlinLogging.logger {}

    override fun updateTask(command: UpdateTaskUseCase.Command): Task {
        val task = repository.search(command.taskId) ?: throw IllegalArgumentException("Task not found")
        val updatedTask = task.updateContent(command.title, command.description)
        repository.save(updatedTask)
        log.info { "Task successfully updated: $updatedTask" }
        return updatedTask
    }

}