package de.emaarco.example.application.service

import de.emaarco.example.application.port.outbound.TaskRepository
import de.emaarco.example.domain.Task
import de.emaarco.example.domain.TaskDescription
import de.emaarco.example.domain.TaskId
import de.emaarco.example.domain.TaskTitle
import de.emaarco.example.domain.UserId
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID

class LoadTasksServiceTest {

    private val repository = mockk<TaskRepository>()
    private val service = LoadTasksService(repository)

    @Test
    fun `load all tasks`() {
        val tasks = listOf(buildTask("Task 1"), buildTask("Task 2"))
        every { repository.load() } returns tasks

        val result = service.loadTasks()

        // Asserting the actual payload (not just non-null / size) is what kills the
        // `replaced return value with Collections.emptyList` mutant on `loadTasks()`.
        assertThat(result).containsExactlyElementsOf(tasks)
        verify { repository.load() }
    }

    private fun buildTask(title: String) =
        Task(
            id = TaskId(UUID.randomUUID()),
            createdBy = UserId("test-user"),
            title = TaskTitle(title),
            description = TaskDescription("some description"),
        )
}
