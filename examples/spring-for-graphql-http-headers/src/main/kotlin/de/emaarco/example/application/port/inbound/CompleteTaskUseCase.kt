package de.emaarco.example.application.port.inbound

import de.emaarco.example.domain.Task
import de.emaarco.example.domain.TaskId

interface CompleteTaskUseCase {
    fun completeTask(taskId: TaskId): Task
}
