import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.springframework)
    alias(libs.plugins.spring.dependency)
    alias(libs.plugins.ktlint)
}

allprojects {
    group = "de.emaarco"
    version = "1.0-SNAPSHOT"
    repositories {
        mavenCentral()
    }

    /**
     * Disables task that due to the current library versions are not working (but also not required)
     * Please check them regularly to see if they can be enabled again
     */
    val disabledTasks = arrayOf("runKtlintCheckOverKotlinScripts", "runKtlintFormatOverKotlinScripts")
    tasks.matching { it.name in disabledTasks }.configureEach {
        enabled = false
    }
}

tasks.getByName<Jar>("jar") {
    enabled = true
}

tasks.getByName<BootJar>("bootJar") {
    enabled = false
}

subprojects {

    println("Enabling JVM plugin in project ${project.name}...")
    apply(plugin = "org.jetbrains.kotlin.jvm")

    println("Enabling Kotlin Spring plugin in project ${project.name}...")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")

    println("Enabling Spring Boot Dependency Management in project ${project.name}...")
    apply(plugin = "io.spring.dependency-management")

    println("Enabling ktLint plugin in project ${project.name}...")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    ktlint {
        debug.set(true)
        outputToConsole.set(true)
        version.set("1.5.0")
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    tasks.withType<KotlinCompile>().configureEach {
        println("Configuring $name in project ${project.name}...")
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
            freeCompilerArgs.add("-Xjsr305=strict")
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    tasks.named("build") {
        dependsOn("ktlintCheck")
    }
}
