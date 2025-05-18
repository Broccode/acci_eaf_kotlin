plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    kotlin("plugin.jpa")
    id("org.springframework.boot") version "3.2.3" apply false
    id("io.spring.dependency-management") version "1.1.4"
    id("org.liquibase.gradle") version "2.2.0"
}

dependencyManagement {
    imports {
        mavenBom(libs.spring.boot.bom.get().toString())
    }
}

dependencies {
    // Dependencies from eaf-core for common utilities
    implementation(project(":eaf-core"))

    // Database related
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.postgresql)
    implementation(libs.liquibase.core)

    // Validation
    implementation(libs.spring.boot.starter.validation)

    // Testing
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.testcontainers.postgresql)
    testImplementation(libs.testcontainers.junit)
    testImplementation(libs.bundles.testing.core)
    testImplementation(kotlin("test"))
}

// Liquibase-Konfiguration
liquibase {
    activities.register("main") {
        this.arguments = mapOf(
            "logLevel" to "info",
            "changeLogFile" to "src/main/resources/db/changelog/db.changelog-master.xml",
            "url" to "jdbc:postgresql://localhost:5432/eaf_multitenancy_db",
            "username" to "eaf_user",
            "password" to "eaf_password"
        )
    }
    runList = "main"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        freeCompilerArgs.addAll(
            "-Xjvm-default=all",
            "-Xcontext-receivers"
        )
    }
}
