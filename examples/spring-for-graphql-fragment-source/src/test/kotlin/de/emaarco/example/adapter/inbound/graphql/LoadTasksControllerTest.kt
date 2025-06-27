package de.emaarco.example.adapter.inbound.graphql

import com.ninjasquad.springmockk.MockkBean
import de.emaarco.example.adapter.inbound.shared.TaskDto
import de.emaarco.example.application.port.inbound.LoadTasksQuery
import de.emaarco.example.domain.Task
import de.emaarco.example.domain.TaskDescription
import de.emaarco.example.domain.TaskId
import de.emaarco.example.domain.TaskTitle
import de.emaarco.example.domain.UserId
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest
import org.springframework.context.annotation.Import
import org.springframework.graphql.test.tester.GraphQlTester
import java.util.*

@GraphQlTest(controllers = [LoadTasksController::class])
@Import(GraphQlTestConfiguration::class)
class LoadTasksControllerTest {

    @Autowired
    private lateinit var graphQlTester: GraphQlTester

    @MockkBean
    private lateinit var query: LoadTasksQuery

    @Test
    fun `load all tasks`() {
        val firstTaskID = UUID.randomUUID()
        val secondTaskID = UUID.randomUUID()

        every { query.loadTasks() } returns
            listOf(
                buildTask(firstTaskID, "Task 1", "Description 1", false),
                buildTask(secondTaskID, "Task 2", "Description 2", true),
            )

        graphQlTester
            .documentName("tasks")
            .fragmentName("task.fragments")
            .execute()
            .path("tasks")
            .entityList(TaskDto::class.java)
            .containsExactly(
                buildTaskDto(firstTaskID.toString(), "Task 1", "Description 1", false),
                buildTaskDto(secondTaskID.toString(), "Task 2", "Description 2", true),
            )

        verify { query.loadTasks() }
        confirmVerified(query)
    }

    private fun buildTask(
        id: UUID,
        title: String,
        description: String,
        completed: Boolean,
    ) = Task(
        id = TaskId(id),
        createdBy = UserId("test-user"),
        title = TaskTitle(title),
        description = TaskDescription(description),
        completed = completed,
    )

    private fun buildTaskDto(
        id: String,
        title: String,
        description: String,
        completed: Boolean,
    ) = TaskDto(
        id = id,
        title = title,
        description = description,
        completed = completed,
    )
}
