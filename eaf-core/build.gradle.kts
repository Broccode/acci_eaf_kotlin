// EAF Core Module - Grundlegende Funktionen und Schnittstellen für das EAF

plugins {
    id("acci.eaf.kotlin.library")
}

version = "0.1.0"

dependencies {
    // Kotlin Core
    implementation(libs.bundles.kotlin.core)
    implementation(libs.bundles.kotlin.extensions)

    // Logging
    implementation(libs.bundles.logging)

    // Utility Libraries
    implementation(libs.bundles.commons)

    // Testing
    testImplementation(libs.bundles.testing.core)
    testImplementation(libs.bundles.testing.kotest)
}

// Konfiguriere die Java- und Kotlin-Kompilierungsoptionen entsprechend dem Projekt-Standard
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        freeCompilerArgs += listOf(
            "-Xjvm-default=all", // Aktiviere JVM Default-Methoden für alle
            "-Xexplicit-api=strict", // Erfordere explizite Sichtbarkeitsmodifikatoren
            "-Xcontext-receivers" // Aktiviere Context Receivers für fortgeschrittene Funktionen
        )
    }
}

// Konfiguriere die Test-Tasks
tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}
