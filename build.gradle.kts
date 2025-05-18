// Root-Projekt Build-Datei für ACCI EAF

plugins {
    id("org.jetbrains.kotlin.jvm") version "2.0.0" apply false
    id("org.jetbrains.kotlin.plugin.spring") version "2.0.0" apply false
    id("org.jetbrains.kotlin.plugin.jpa") version "2.0.0" apply false
    id("com.diffplug.spotless") version "7.0.3" apply false
}

// Gemeinsame Konfiguration für alle Projekte
allprojects {
    group = "com.acci.eaf"
    version = "0.1.0"

    // Anwenden von Repositories für alle Projekte
    repositories {
        mavenCentral()
    }
}

subprojects {
    // Spotless Plugin anwenden und konfigurieren
    apply(plugin = "com.diffplug.spotless")
    configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        // Ratchet from an existing state (optional, but good for starting)
        // ratchetFrom("origin/main") // Example: only format files changed since origin/main

        kotlin {
            // Standardmäßig wird .editorconfig für ktlint verwendet, falls vorhanden.
            // target "src/**/*.kt" wird standardmäßig von der Kotlin-Extension abgedeckt, 
            // wenn das Kotlin-Plugin im jeweiligen Subprojekt angewendet wird.
            ktlint("1.5.0").editorConfigOverride(mapOf(
                "ktlint_function_signature_rule_force_multiline_when_parameter_count_greater_or_equal_than" to 3 
            ))
            // licenseHeaderFile(project.rootProject.file("config/spotless/copyright.kt"), "(package|import|@file:)")
        }
        kotlinGradle {
            // Standardmäßig wird .editorconfig für ktlint verwendet, falls vorhanden.
            // target "*.kts" wird standardmäßig von der KotlinGradle-Extension abgedeckt.
            ktlint("1.5.0").editorConfigOverride(mapOf(
                "ktlint_function_signature_rule_force_multiline_when_parameter_count_greater_or_equal_than" to 3
            ))
        }
        // Hier könnten weitere Formatter für andere Sprachen/Dateitypen hinzugefügt werden
        // z.B. json, yaml, markdown
        // json {
        //   target("src/**/*.json")
        //   jackson() // oder gson()
        // }
        // markdown {
        //   target("*.md", "docs/**/*.md")
        //   flexmark()
        // }
    }
}

tasks {
    // Definiere eine Aufgabe, um zu überprüfen, ob alle Abhängigkeiten aktuell sind
    register("checkDependencyUpdates") {
        // Diese Aufgabe dokumentiert den Zweck, muss aber keine Aktion ausführen
        description = "Überprüft, ob Abhängigkeiten aktualisiert werden können"
        doLast {
            println("Um Abhängigkeits-Updates zu überprüfen, verwende: './gradlew dependencyUpdates'")
        }
    }

    // Eine Hilfsaufgabe, um ein Gefühl dafür zu bekommen, was gebaut wird
    register("printProjectStructure") {
        description = "Zeigt die Struktur des Projekts an, um ein besseres Verständnis zu bekommen"
        doLast {
            println("\nProjekt-Struktur für ${rootProject.name}:")
            println("=======================================")
            rootProject.allprojects.forEach { project ->
                if (project == rootProject) {
                    println("${project.name} (Root)")
                } else {
                    println("└── ${project.name}")
                }
            }
            println("\n")
        }
    }
}

tasks.register("cleanAll") {
    group = "build"
    description = "Löscht alle Build-Verzeichnisse in allen Projekten"
    
    dependsOn(gradle.includedBuilds.map { it.task(":clean") })
    dependsOn(subprojects.map { it.tasks.named("clean") })
    
    doLast {
        println("Alle Build-Verzeichnisse wurden gelöscht.")
    }
}

tasks.register("buildAll") {
    group = "build"
    description = "Baut alle Projekte und führt Tests aus"
    
    dependsOn(gradle.includedBuilds.map { it.task(":build") })
    dependsOn(subprojects.map { it.tasks.named("build") })
    
    doLast {
        println("Alle Projekte wurden erfolgreich gebaut.")
    }
} 