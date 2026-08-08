# 🚀 Using Custom Fragment Locations in Spring-for-GraphQL 🌐

This repository demonstrates how to configure Spring-for-GraphQL to access fragments stored in custom locations. By
default, Spring-for-GraphQL only uses fragments in the `graphql-test` folder. With a custom configuration, you can
specify any folder to store your fragments.

## ✨ Use-Case

The example showcases how to:

- Use a custom `DocumentSource` to include fragments from multiple directories.
- Configure a custom `GraphQlTester` to integrate the `DocumentSource`.

By modifying the configuration, you can easily change the locations from which fragments are loaded.

## 🔧 Configuration Overview

The custom configuration is defined in the `GraphQlTestConfiguration` file:

```kotlin
@Configuration
class GraphQlTestConfiguration {

    @Bean
    @Primary
    fun customGraphQlTester(
        executionGraphQlTester: ExecutionGraphQlService,
    ): GraphQlTester {
        val customDocumentSource = documentSource()
        return ExecutionGraphQlServiceTester.builder(executionGraphQlTester)
            .documentSource(customDocumentSource)
            .encoder(Jackson2JsonEncoder())
            .decoder(Jackson2JsonDecoder())
            .build()
    }

    private fun documentSource(): DocumentSource {
        val resources = listOf(
            ClassPathResource("graphql-test/"),
            ClassPathResource("graphql-fragments/") // Add custom directories here
        )
        return ResourceDocumentSource(resources, ResourceDocumentSource.FILE_EXTENSIONS)
    }
}
```

### Key Modification Point:

To change the fragment locations, update the `resources` list in the `documentSource()` method with the desired folder
paths.

## ✅ Test Example

The test case demonstrates how the custom configuration resolves fragments from the specified locations:

```kotlin
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

        every { query.loadTasks() } returns listOf(
            buildTask(firstTaskID, "Task 1", "Description 1", false),
            buildTask(secondTaskID, "Task 2", "Description 2", true),
        )

        graphQlTester.documentName("tasks")
            .fragmentName("task.fragments")
            .execute()
            .path("tasks")
            .entityList(TaskDto::class.java)
            .containsExactly(
                buildTaskDto(firstTaskID.toString(), "Task 1", "Description 1", false),
                buildTaskDto(secondTaskID.toString(), "Task 2", "Description 2", true)
            )

        verify { query.loadTasks() }
        confirmVerified(query)
    }
}
```

### Key Points:

- The test specifies the file the fragment is stored in using `.fragmentName("task.fragments")`.
- Fragments are resolved from the custom directories configured in `GraphQlTestConfiguration`.

---

With this setup, you can easily adapt the fragment locations by modifying the `documentSource` configuration in
`GraphQlTestConfiguration`. Happy coding! 🎉

## 🧬 Bonus: Mutation testing (pitest)

Because this is the one module with a real **behavioural** test, it also hosts a hands-on
**mutation testing** spike with [pitest](https://pitest.org). Mutation testing measures *test
quality* — would a test actually *catch* a bug — instead of mere line coverage.

```bash
# JDK 25 default toolchain is lowered to 21 for the spike (see the doc for why)
./gradlew :examples:spring-for-graphql-fragment-source:pitest -PjavaToolchainVersion=21
# report → build/reports/pitest/index.html
```

The full write-up — what it is, what value it adds, how it's wired up, a concrete
survived-mutant we deliberately killed (58 % → 67 % mutation score), and the Kotlin-synthetics /
JVM-25 pitfalls — lives in **[docs/mutation-testing.md](docs/mutation-testing.md)**.
