package de.emaarco.example.adapter.inbound.shared

import de.emaarco.example.application.port.inbound.AddTaskUseCase
import de.emaarco.example.application.port.inbound.UpdateTaskUseCase
import de.emaarco.example.domain.TaskDescription
import de.emaarco.example.domain.TaskId
import de.emaarco.example.domain.TaskTitle
import de.emaarco.example.domain.UserId

data class TaskInput(
    val title: String,
    val description: String?,
) {

    fun toAddTaskCommand(userId: String) =
        AddTaskUseCase.Command(
            title = TaskTitle(title),
            description = description?.let { TaskDescription(it) },
            createdBy = UserId(userId),
        )

    fun toUpdateTaskCommand(taskId: TaskId) =
        UpdateTaskUseCase.Command(
            taskId = taskId,
            title = TaskTitle(title),
            description = description?.let { TaskDescription(it) },
        )
}
