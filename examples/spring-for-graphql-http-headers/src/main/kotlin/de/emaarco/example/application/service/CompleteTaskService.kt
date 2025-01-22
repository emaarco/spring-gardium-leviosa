package de.emaarco.example.application.service

import de.emaarco.example.application.port.inbound.CompleteTaskUseCase
import de.emaarco.example.application.port.outbound.TaskRepository
import de.emaarco.example.domain.Task
import de.emaarco.example.domain.TaskId
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class CompleteTaskService(
    private val repository: TaskRepository,
) : CompleteTaskUseCase {

    private val log = KotlinLogging.logger {}

    override fun completeTask(taskId: TaskId): Task {
        val task = repository.search(taskId) ?: throw NoSuchElementException("Task not found")
        val completedTask = task.complete()
        repository.save(completedTask)
        log.info { "Task successfully completed: $completedTask" }
        return completedTask
    }
}
