package de.emaarco.example.application.port.out

import de.emaarco.example.domain.Task
import de.emaarco.example.domain.TaskId

interface TaskRepository {
    fun load(): List<Task>
    fun search(taskId: TaskId): Task?
    fun save(task: Task)
    fun delete(taskId: TaskId)
}