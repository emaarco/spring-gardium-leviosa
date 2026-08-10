plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.springframework)
    alias(libs.plugins.pitest)
}

group = "de.emaarco.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.bundles.defaultService)
    implementation(libs.bundles.graphql)
    testImplementation(libs.bundles.test)
    testImplementation(libs.spring.graphql.test)
    testImplementation(libs.spring.boot.graphql.test)
    testImplementation(project(":examples:archunit"))
    testImplementation(project(":examples:konsist"))
    testImplementation(project(":examples:architecture-combined"))

    // Teaches pitest how to discover & run JUnit 5 (Jupiter) tests.
    pitest(libs.pitest.junit5)
}

/**
 * Mutation-testing spike (see README → "Mutation testing").
 *
 * Scoped deliberately to the behavioural surface the tests actually assert on — the GraphQL
 * controller, the `TaskDto` mapping and `LoadTasksService` — so the mutation score is a clean,
 * meaningful signal rather than a sea of NO_COVERAGE from the architecture-only tests.
 *
 * Run it with a pitest/ASM-compatible toolchain (see the JVM-25 pitfall in the README):
 *   ./gradlew :examples:spring-for-graphql-fragment-source:pitest -PjavaToolchainVersion=21
 *
 * The HTML report lands in build/reports/pitest/index.html.
 */
pitest {
    pitestVersion.set("1.22.1")
    targetClasses.set(
        setOf(
            "de.emaarco.example.adapter.inbound.graphql.*",
            "de.emaarco.example.adapter.inbound.shared.TaskDto*",
            "de.emaarco.example.application.service.LoadTasksService*",
        ),
    )
    targetTests.set(
        setOf(
            "de.emaarco.example.adapter.inbound.graphql.LoadTasksControllerTest",
            "de.emaarco.example.application.service.LoadTasksServiceTest",
        ),
    )
    threads.set(4)
    outputFormats.set(setOf("HTML", "XML"))
    timestampedReports.set(false)
}
