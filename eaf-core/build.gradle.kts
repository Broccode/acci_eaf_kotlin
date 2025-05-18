// EAF Core Module - Grundlegende Funktionen und Schnittstellen für das EAF

plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
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

    // Datenbanken und JDBC - Für TenantAwareDataSourceConfig
    implementation(libs.spring.boot.starter.data.jpa) // Enthält JDBC-Unterstützung
    implementation(libs.hikari) // HikariCP für Connection Pooling

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

    // H2 für Tests
    testRuntimeOnly("com.h2database:h2:2.2.224")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

// Konfiguriere die Java- und Kotlin-Kompilierungsoptionen entsprechend dem Projekt-Standard
kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    compilerOptions {
        freeCompilerArgs.addAll(
            // Aktiviere JVM Default-Methoden für alle
            "-Xjvm-default=all",
            // Erfordere explizite Sichtbarkeitsmodifikatoren (temporär deaktiviert)
            // "-Xexplicit-api=strict",
            // Aktiviere Context Receivers für fortgeschrittene Funktionen
            "-Xcontext-receivers"
        )
    }
}

// Tests are now enabled
tasks.withType<Test> {
    enabled = true
    useJUnitPlatform()
}
