plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.springframework)
}

group = "de.emaarco.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val eventRelaySidecarJar: Configuration by configurations.creating

dependencies {
    implementation(libs.bundles.defaultService)
    implementation(libs.bundles.graphql)
    implementation(libs.websocket)
    implementation(libs.restclient)
    implementation(libs.kotlinx.coroutines.reactor)
    testImplementation(libs.bundles.test)
    testImplementation(libs.spring.graphql.test)
    testImplementation(libs.spring.boot.graphql.test)
    testImplementation(libs.spring.kafka.test)
    testImplementation(libs.awaitility)
    testImplementation(project(":examples:architecture-combined"))
    eventRelaySidecarJar(
        project(
            mapOf(
                "path" to ":examples:spring-for-graphql-subscriptions-sidecar:event-relay-sidecar",
                "configuration" to "bootArchives",
            ),
        ),
    )
}

tasks.test {
    inputs.files(eventRelaySidecarJar)
    jvmArgumentProviders.add(
        CommandLineArgumentProvider {
            listOf("-Devent-relay-sidecar.jar=${eventRelaySidecarJar.singleFile.absolutePath}")
        },
    )
}
