// Root-Projekt Build-Datei für ACCI EAF

plugins {
    id("org.jetbrains.kotlin.jvm") version "2.1.21" apply false
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