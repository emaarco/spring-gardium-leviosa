import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.springframework)
}

group = "de.emaarco.architecture"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Self-contained: carries both tools directly and imports nothing from the sibling rule modules.
    // `api` so a consuming service gets ArchUnit, Konsist and JUnit on its test classpath transitively.
    api(libs.bundles.test)
    api(libs.bundles.archunit)
    api(libs.bundles.konsist)
}

tasks.getByName<BootJar>("bootJar") {
    enabled = false
}
