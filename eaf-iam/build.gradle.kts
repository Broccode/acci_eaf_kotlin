// EAF IAM Module - Identity & Access Management für EAF

plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    kotlin("plugin.jpa")
    id("org.springframework.boot") version "3.2.3"
    id("io.spring.dependency-management") version "1.1.4"
}

version = "0.1.0"

dependencies {
    // Modulabhängigkeiten
    implementation(project(":eaf-core"))
    implementation(project(":eaf-multitenancy"))

    // Kotlin Core
    implementation(libs.bundles.kotlin.core)
    implementation(libs.bundles.kotlin.extensions)

    // Spring Boot
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.security)

    // Datenbanken
    implementation(libs.postgresql)
    implementation(libs.liquibase.core)
    implementation(libs.hikari) // HikariCP für Connection Pooling

    // Validation
    implementation(libs.spring.boot.starter.validation)

    // JWT für Token-basierte Authentifizierung
    implementation("io.jsonwebtoken:jjwt-api:0.12.3")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.3")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.3")

    // OpenAPI/Swagger Dokumentation
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")

    // Jackson
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.16.1")

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
    testImplementation("org.testcontainers:junit-jupiter:1.19.1")
    testImplementation("org.testcontainers:postgresql:1.19.1")
    testImplementation("org.jetbrains.kotlin:kotlin-test:2.0.0")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:2.0.0")

    // H2 In-Memory Datenbank für Tests
    testRuntimeOnly("com.h2database:h2:2.2.224")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xjvm-default=all",
            "-Xcontext-receivers"
        )
    }
}

tasks.withType<Test> {
    enabled = true
    useJUnitPlatform()
}
