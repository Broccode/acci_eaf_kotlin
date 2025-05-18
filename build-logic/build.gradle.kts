plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.21")
    implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.23.3")
    implementation("org.jlleitschuh.gradle:ktlint-gradle:11.5.1")
    
    // Spring Boot Support
    implementation("org.springframework.boot:spring-boot-gradle-plugin:3.2.3")
    implementation("io.spring.gradle:dependency-management-plugin:1.1.4")
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(24))
    }
}

gradlePlugin {
    plugins {
        register("kotlinBasePlugin") {
            id = "acci.eaf.kotlin.base"
            implementationClass = "acci.eaf.gradle.KotlinBasePlugin"
        }
        register("kotlinLibraryPlugin") {
            id = "acci.eaf.kotlin.library"
            implementationClass = "acci.eaf.gradle.KotlinLibraryPlugin"
        }
        register("kotlinApplicationPlugin") {
            id = "acci.eaf.kotlin.application"
            implementationClass = "acci.eaf.gradle.KotlinApplicationPlugin"
        }
        register("kotlinTestPlugin") {
            id = "acci.eaf.kotlin.test"
            implementationClass = "acci.eaf.gradle.KotlinTestPlugin"
        }
        register("kotlinSpringBootPlugin") {
            id = "acci.eaf.kotlin.spring.boot"
            implementationClass = "acci.eaf.gradle.KotlinSpringBootPlugin"
        }
    }
}
