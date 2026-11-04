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

dependencies {
    implementation(libs.bundles.defaultService)
    implementation(libs.bundles.graphql)
    implementation(libs.websocket)
    implementation(libs.kafka)
    implementation(libs.jackson.kotlin)
    implementation(libs.kotlinx.coroutines.reactor)
    testImplementation(libs.bundles.test)
    testImplementation(libs.spring.graphql.test)
    testImplementation(libs.spring.boot.graphql.test)
    testImplementation(libs.spring.kafka.test)
    testImplementation(libs.awaitility)
    testImplementation(project(":examples:architecture-combined"))
}
