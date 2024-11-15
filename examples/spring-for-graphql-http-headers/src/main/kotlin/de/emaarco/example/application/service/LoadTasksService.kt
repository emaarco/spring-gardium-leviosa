package de.emaarco.example.application.service

import de.emaarco.example.application.port.`in`.LoadTasksQuery
import de.emaarco.example.application.port.out.TaskRepository
import org.springframework.stereotype.Service

@Service
class LoadTasksService(
    private val repository: TaskRepository
) : LoadTasksQuery {
    override fun loadTasks() = repository.load()
}