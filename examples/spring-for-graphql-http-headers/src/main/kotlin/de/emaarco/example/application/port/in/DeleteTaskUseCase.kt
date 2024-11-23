package de.emaarco.example.application.port.`in`

import de.emaarco.example.domain.TaskId

interface DeleteTaskUseCase {
    fun deleteTask(taskId: TaskId)
}