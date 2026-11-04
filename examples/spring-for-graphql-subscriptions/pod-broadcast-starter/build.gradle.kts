import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.springframework)
}

group = "de.emaarco.podbroadcast"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    api(libs.kafka)
    api(libs.kotlinx.coroutines.core)
    implementation(libs.actuator)
    implementation(libs.jackson)
    implementation(libs.jackson.kotlin)
    implementation(libs.kotlin.logging)
    testImplementation(libs.bundles.test)
    testImplementation(libs.spring.kafka.test)
    testImplementation(libs.awaitility)
}

tasks.getByName<BootJar>("bootJar") {
    enabled = false
}
