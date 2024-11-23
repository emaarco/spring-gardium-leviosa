package de.emaarco.example.domain

import java.util.*

data class Task(
    val id: TaskId = TaskId(UUID.randomUUID()),
    val createdBy: UserId,
    val title: TaskTitle,
    val description: TaskDescription?,
    val completed: Boolean = false,
) {
    fun complete() = this.copy(completed = true)
    fun updateContent(title: TaskTitle, description: TaskDescription?) = this.copy(
        title = title,
        description = description
    )
}
