plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.springframework)
}

group = "de.emaarco.sidecar"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.bundles.defaultService)
    implementation(libs.kafka)
    implementation(libs.restclient)
    testImplementation(libs.bundles.test)
    testImplementation(libs.spring.kafka.test)
    testImplementation(libs.awaitility)
    testImplementation(project(":examples:architecture-combined"))
}
