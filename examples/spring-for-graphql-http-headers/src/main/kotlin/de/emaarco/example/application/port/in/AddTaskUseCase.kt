package de.emaarco.example.application.port.`in`

import de.emaarco.example.domain.Task
import de.emaarco.example.domain.TaskDescription
import de.emaarco.example.domain.TaskTitle
import de.emaarco.example.domain.UserId

interface AddTaskUseCase {

    fun addTask(command: Command): Task

    data class Command(
        val title: TaskTitle,
        val description: TaskDescription?,
        val createdBy: UserId,
    )
}