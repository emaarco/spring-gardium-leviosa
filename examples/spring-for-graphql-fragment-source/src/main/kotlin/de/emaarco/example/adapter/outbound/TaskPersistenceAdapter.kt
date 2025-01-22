package de.emaarco.example.adapter.outbound

import de.emaarco.example.application.port.outbound.TaskRepository
import de.emaarco.example.domain.Task
import de.emaarco.example.domain.TaskId
import org.springframework.stereotype.Component

@Component
class TaskPersistenceAdapter : TaskRepository {

    private val tasks: MutableMap<TaskId, Task> = mutableMapOf()

    override fun load(): List<Task> {
        return tasks.values.toList()
    }

    override fun search(taskId: TaskId): Task? {
        return tasks[taskId]
    }

    override fun save(task: Task) {
        tasks[task.id] = task
    }

    override fun delete(taskId: TaskId) {
        tasks.remove(taskId)
    }
}
