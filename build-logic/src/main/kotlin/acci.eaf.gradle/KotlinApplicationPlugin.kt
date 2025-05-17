package acci.eaf.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ApplicationPlugin
import org.gradle.kotlin.dsl.apply

/**
 * Plugin for all Kotlin application projects in the ACCI EAF codebase.
 * Configures the project as a Kotlin application with proper runtime settings.
 */
class KotlinApplicationPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            plugins.apply("acci.eaf.kotlin.base")
            plugins.apply(ApplicationPlugin::class.java)

            // Default configuration for all applications
            tasks.named("run") {
                // Add standard JVM arguments for better development experience
                (this as org.gradle.api.tasks.JavaExec).apply {
                    jvmArgs = listOf(
                        "-XX:+HeapDumpOnOutOfMemoryError",  // Create heap dump on OOM
                        "-XX:HeapDumpPath=./build/heapdump.hprof",
                        "-Dfile.encoding=UTF-8",             // Ensure UTF-8 encoding
                        "-Djava.net.preferIPv4Stack=true"    // Prefer IPv4
                    )
                }
            }
        }
    }
}
