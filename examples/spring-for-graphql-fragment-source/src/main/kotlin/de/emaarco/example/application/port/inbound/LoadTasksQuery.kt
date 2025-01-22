package de.emaarco.example.application.port.inbound

import de.emaarco.example.domain.Task

interface LoadTasksQuery {
    fun loadTasks(): List<Task>
}
