package de.emaarco.example.application.port.inbound

import de.emaarco.example.domain.TaskId

interface DeleteTaskUseCase {
    fun deleteTask(taskId: TaskId)
}
