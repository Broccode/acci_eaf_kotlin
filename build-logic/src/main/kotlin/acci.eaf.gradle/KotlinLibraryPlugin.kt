package acci.eaf.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.register

/**
 * Plugin for all Kotlin library projects in the ACCI EAF codebase.
 * Configures the project as a Kotlin library with proper publishing configuration.
 */
class KotlinLibraryPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            plugins.apply("acci.eaf.kotlin.base")
            plugins.apply(JavaLibraryPlugin::class.java)
            plugins.apply(MavenPublishPlugin::class.java)
            
            setupLibraryPublishing()
        }
    }
    
    private fun Project.setupLibraryPublishing() {
        configure<PublishingExtension> {
            publications {
                register<MavenPublication>("maven") {
                    groupId = "com.acci.eaf"
                    artifactId = project.name
                    version = project.version.toString()
                    
                    from(components["java"])
                    
                    pom {
                        name.set("ACCI EAF ${project.name}")
                        description.set("ACCI Enterprise Application Framework - ${project.name} module")
                        url.set("https://github.com/acci/eaf")
                        licenses {
                            license {
                                name.set("Proprietary")
                                url.set("https://acci.com/licenses/eaf")
                            }
                        }
                    }
                }
            }
            
            repositories {
                maven {
                    name = "ProjectRepository"
                    url = uri("${project.rootProject.buildDir}/repo")
                }
            }
        }
    }
} 