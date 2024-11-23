package de.emaarco.example.application.service

import de.emaarco.example.application.port.`in`.DeleteTaskUseCase
import de.emaarco.example.application.port.out.TaskRepository
import de.emaarco.example.domain.TaskId
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class DeleteTaskService(
    private val repository: TaskRepository
) : DeleteTaskUseCase {

    private val log = KotlinLogging.logger {}

    override fun deleteTask(taskId: TaskId) {
        val task = repository.search(taskId) ?: throw IllegalArgumentException("Task not found")
        repository.delete(taskId)
        log.info { "Task successfully deleted: $task" }
    }

}