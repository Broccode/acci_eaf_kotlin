# ACCI Enterprise Application Framework (EAF)

ACCI EAF ist ein modulares Framework für die Entwicklung von Enterprise-Anwendungen in Kotlin. Es bietet eine strukturierte, konsistente und effiziente Methode zur Erstellung skalierbarer, wartbarer und testbarer Anwendungen.

## Überblick

Das ACCI Enterprise Application Framework bietet:

- Einheitliches Build-System basierend auf Gradle
- Modulare Architektur mit klar definierten Abhängigkeiten
- Vorkonfigurierte Best Practices für Kotlin-Entwicklung
- Integrierte Qualitätssicherungswerkzeuge (ktlint, detekt)
- Standard-Konfigurationen für verschiedene Anwendungstypen (Bibliothek, Anwendung, Spring Boot)

## Voraussetzungen

- JDK 21 oder höher
- Gradle 8.14 (wird über Wrapper bereitgestellt)
- Git

## Projekt-Setup

### Repository klonen

```bash
git clone https://github.com/acci/eaf.git
cd eaf
```

### Projekt bauen

```bash
./gradlew build
```

## Projektstruktur

Das Projekt ist als Gradle-Multimodul-Projekt organisiert:

- `build-logic`: Enthält gemeinsame Build-Konfigurationen und Gradle-Plugins
- `eaf-core`: Kernbibliothek mit grundlegenden Funktionen und gemeinsamen Interfaces
- Weitere Module werden im Laufe der Entwicklung hinzugefügt

## Gradle-Plugins

Das Framework stellt folgende Gradle-Plugins zur Verfügung:

- `acci.eaf.kotlin.base`: Grundlegende Kotlin-Konfiguration für alle Projekte
- `acci.eaf.kotlin.library`: Konfiguration für Bibliotheksmodule
- `acci.eaf.kotlin.application`: Konfiguration für Anwendungsmodule
- `acci.eaf.kotlin.test`: Konfiguration für Testmodule
- `acci.eaf.kotlin.spring.boot`: Konfiguration für Spring Boot-Anwendungen

## Entwicklung

Weitere Informationen zur Entwicklung, Coding Standards und Projektrichtlinien finden Sie in der Dokumentation im `docs/`-Verzeichnis.

### Code-Formatierung

Dieses Projekt verwendet Spotless mit ktlint für die automatische Code-Formatierung. Weitere Informationen finden Sie in der [Code-Formatierungs-Dokumentation](docs/tooling/code-formatting.md).
