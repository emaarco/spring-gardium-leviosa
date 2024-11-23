package de.emaarco.example.application.port.`in`

import de.emaarco.example.domain.Task

interface LoadTasksQuery {
    fun loadTasks(): List<Task>
}