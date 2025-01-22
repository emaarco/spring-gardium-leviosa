package de.emaarco.example.adapter.inbound.shared

import de.emaarco.example.domain.Task

data class TaskDto(
    val id: String,
    val title: String,
    val description: String?,
    val completed: Boolean,
) {
    companion object {
        fun from(task: Task) =
            TaskDto(
                id = task.id.value.toString(),
                title = task.title.value,
                description = task.description?.value,
                completed = task.completed,
            )
    }
}
