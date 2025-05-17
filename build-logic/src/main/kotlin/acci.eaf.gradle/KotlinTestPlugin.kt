package acci.eaf.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType

/**
 * Plugin for test modules in the ACCI EAF codebase.
 * Configures the project as a Kotlin test module with necessary testing dependencies
 * and configurations.
 */
class KotlinTestPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            plugins.apply("acci.eaf.kotlin.base")
            
            // Configure test task
            tasks.withType<Test> {
                useJUnitPlatform()
                
                testLogging {
                    events("passed", "skipped", "failed")
                    showExceptions = true
                    showStackTraces = true
                    showCauses = true
                    showStandardStreams = false
                    
                    // Show more verbose test output in CI environments
                    val ciMode = System.getenv("CI")?.toBoolean() ?: false
                    if (ciMode) {
                        showStandardStreams = true
                    }
                }
                
                // Fail on any test failure
                ignoreFailures = false
                
                // Set system properties for tests
                systemProperty("java.util.logging.manager", "org.apache.logging.log4j.LogManager")
                systemProperty("file.encoding", "UTF-8")
                
                // Ensure tests are always re-run
                outputs.upToDateWhen { false }
            }
            
            // Add common test dependencies
            dependencies {
                "implementation"(project.dependencies.platform("org.jetbrains.kotlin:kotlin-bom"))
                
                "testImplementation"("org.junit.jupiter:junit-jupiter-api")
                "testImplementation"("org.junit.jupiter:junit-jupiter-params")
                "testRuntimeOnly"("org.junit.jupiter:junit-jupiter-engine")
                
                "testImplementation"("io.mockk:mockk")
                
                "testImplementation"("io.kotest:kotest-runner-junit5")
                "testImplementation"("io.kotest:kotest-assertions-core")
                "testImplementation"("io.kotest:kotest-property")
                
                "testImplementation"("org.slf4j:slf4j-api")
                "testImplementation"("org.apache.logging.log4j:log4j-slf4j2-impl")
            }
        }
    }
} 