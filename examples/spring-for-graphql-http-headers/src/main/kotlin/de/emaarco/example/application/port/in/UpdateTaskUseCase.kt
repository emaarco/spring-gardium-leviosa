package de.emaarco.example.application.port.`in`

import de.emaarco.example.domain.Task
import de.emaarco.example.domain.TaskDescription
import de.emaarco.example.domain.TaskId
import de.emaarco.example.domain.TaskTitle

interface UpdateTaskUseCase {

    fun updateTask(command: Command): Task

    data class Command(
        val taskId: TaskId,
        val title: TaskTitle,
        val description: TaskDescription?,
    )
}