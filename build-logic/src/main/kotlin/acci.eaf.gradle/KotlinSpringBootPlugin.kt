package acci.eaf.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.springframework.boot.gradle.tasks.bundling.BootJar
import org.springframework.boot.gradle.tasks.run.BootRun
import org.springframework.boot.gradle.plugin.SpringBootPlugin
import io.spring.gradle.dependencymanagement.DependencyManagementPlugin

/**
 * Plugin for Spring Boot application modules in the ACCI EAF codebase.
 * Configures Spring Boot support with appropriate defaults and configurations.
 */
class KotlinSpringBootPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            // Apply base plugins
            plugins.apply("acci.eaf.kotlin.application")
            plugins.apply(SpringBootPlugin::class.java)
            plugins.apply(DependencyManagementPlugin::class.java)
            plugins.apply("org.jetbrains.kotlin.plugin.spring")
            
            // Configure Spring Boot specifics
            tasks.named("bootJar", BootJar::class.java) {
                archiveClassifier.set("boot")
                launchScript()
            }
            
            tasks.named("bootRun", BootRun::class.java) {
                // Set common Spring profiles for local development
                args("--spring.profiles.active=local,dev")
                
                // Add JVM arguments for better development experience
                jvmArgs = listOf(
                    "-XX:+HeapDumpOnOutOfMemoryError",
                    "-XX:HeapDumpPath=./build/heapdump.hprof",
                    "-Dspring.output.ansi.enabled=always",
                    "-Dfile.encoding=UTF-8"
                )
            }
            
            // Ensure jar task is still usable alongside bootJar
            tasks.named("jar") {
                enabled = true
            }
            
            // Configure Spring dependency management
            configure<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension> {
                imports {
                    mavenBom("org.springframework.boot:spring-boot-dependencies:${findProperty("springBootVersion") ?: "3.2.3"}")
                }
            }
        }
    }
} 