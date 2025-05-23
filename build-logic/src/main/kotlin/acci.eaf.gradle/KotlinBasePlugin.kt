package acci.eaf.gradle

import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import io.gitlab.arturbosch.detekt.DetektPlugin
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.jlleitschuh.gradle.ktlint.KtlintPlugin
import org.jlleitschuh.gradle.ktlint.KtlintExtension

/**
 * Base plugin for all Kotlin projects in the ACCI EAF codebase.
 * Configures common settings like Kotlin compiler options, JVM target version,
 * code style checking via detekt and ktlint.
 */
class KotlinBasePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            setupKotlinConfig()
            setupCodeQuality()
        }
    }

    private fun Project.setupKotlinConfig() {
        plugins.apply("org.jetbrains.kotlin.jvm")

        configure<KotlinProjectExtension> {
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_21)
                apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_1_9)
                languageVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_1_9)
                allWarningsAsErrors.set(true)
                freeCompilerArgs.addAll(
                    "-Xjsr305=strict",          // Strict null safety for JSR-305 annotations
                    "-opt-in=kotlin.RequiresOptIn", // Allow usage of opt-in APIs
                    "-Xcontext-receivers"       // Enable context receivers feature
                )
            }
        }

        configure<JavaPluginExtension> {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(24))
            }
            withSourcesJar()
            withJavadocJar()
        }
    }

    private fun Project.setupCodeQuality() {
        plugins.apply(DetektPlugin::class.java)
        plugins.apply(KtlintPlugin::class.java)

        configure<DetektExtension> {
            buildUponDefaultConfig = true
            allRules = false
            config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
            baseline = file("$rootDir/config/detekt/baseline.xml")
            ignoreFailures = true  // Ignore failures for this initial setup
        }

        configure<KtlintExtension> {
            version.set("0.50.0")
            android.set(false)
            ignoreFailures.set(true)  // Ignore failures for this initial setup
            enableExperimentalRules.set(true)
            filter {
                exclude { it.file.path.contains("build/") }
            }
        }
    }
}
