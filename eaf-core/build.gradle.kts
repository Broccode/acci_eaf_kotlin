// EAF Core Module - Grundlegende Funktionen und Schnittstellen für das EAF

plugins {
    kotlin("jvm") version "2.1.21"
    kotlin("plugin.spring") version "2.1.21"
    id("org.springframework.boot") version "3.2.3"
    id("io.spring.dependency-management") version "1.1.4"
}

version = "0.1.0"

dependencies {
    // Kotlin Core
    implementation(libs.bundles.kotlin.core)
    implementation(libs.bundles.kotlin.extensions)

    // Spring Boot und Axon
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.bundles.axon)

    // Logging
    implementation(libs.bundles.logging)
    implementation("org.apache.logging.log4j:log4j-api:2.22.1")
    implementation("org.apache.logging.log4j:log4j-core:2.22.1")
    
    // Exclude conflicting Spring Boot default logger
    configurations.all {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    }

    // Utility Libraries
    implementation(libs.bundles.commons)

    // Testing
    testImplementation(libs.bundles.testing.core)
    testImplementation(libs.bundles.testing.kotest)
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.axon.test)
}

// Konfiguriere die Java- und Kotlin-Kompilierungsoptionen entsprechend dem Projekt-Standard
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-Xjvm-default=all", // Aktiviere JVM Default-Methoden für alle
            // "-Xexplicit-api=strict", // Erfordere explizite Sichtbarkeitsmodifikatoren (temporär deaktiviert)
            "-Xcontext-receivers" // Aktiviere Context Receivers für fortgeschrittene Funktionen
        )
    }
}

// Tests are now enabled
tasks.withType<Test> {
    enabled = true
    useJUnitPlatform()
}
