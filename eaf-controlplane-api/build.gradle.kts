plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot") version "3.2.3"
    id("io.spring.dependency-management") version "1.1.4"
    id("org.springdoc.openapi-gradle-plugin") version "1.8.0"
}

dependencyManagement {
    imports {
        mavenBom(libs.spring.boot.bom.get().toString())
    }
}

dependencies {
    // Dependencies from other EAF modules
    implementation(project(":eaf-core")) {
        // Exclude log4j dependencies that conflict with Spring Boot's Logback
        exclude(group = "org.apache.logging.log4j", module = "log4j-slf4j2-impl")
    }
    implementation(project(":eaf-multitenancy")) {
        // Exclude log4j dependencies that conflict with Spring Boot's Logback
        exclude(group = "org.apache.logging.log4j", module = "log4j-slf4j2-impl")
    }

    // Spring Boot starters
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.security)
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")

    // Datenbank und JPA
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.postgresql)

    // OpenAPI documentation
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")

    // Axon Framework für Tests
    testImplementation("org.axonframework:axon-spring-boot-starter:4.9.1") {
        // Exclude transitive dependencies that might conflict
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    }

    // Testing
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.testcontainers.postgresql)
    testImplementation(libs.testcontainers.junit)
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("com.h2database:h2")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "21"
        freeCompilerArgs += "-Xjvm-default=all"
        freeCompilerArgs += "-Xcontext-receivers"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()

    // Systemproperties für die Tests
    systemProperty("spring.profiles.active", "test")
}
