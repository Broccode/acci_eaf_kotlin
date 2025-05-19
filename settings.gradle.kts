rootProject.name = "ACCI-EAF"

// Include build-logic as a build of the root project
includeBuild("build-logic")

// Include core EAF modules
include("eaf-core")
include("eaf-multitenancy")
include("eaf-controlplane-api")
include("eaf-iam")

// Settings for repositories
dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

// Customize module names and locations if needed
// This is a simpler approach without the problematic forEach
for (project in rootProject.children) {
    if (project.name.startsWith(":")) {
        val moduleName = project.name.substring(1)
        val moduleDir = rootProject.projectDir.resolve(moduleName)
        if (moduleDir.exists()) {
            project.projectDir = moduleDir
            project.name = moduleName
        }
    }
}