# ACCI EAF (Axians Competence Center Infrastructure Enterprise Application Framework) Architektur-Dokument

## 1\\. Einleitung / Präambel

Dieses Dokument beschreibt die Gesamtprojektarchitektur für das ACCI EAF (Axians Competence Center Infrastructure Enterprise Application Framework). Dies umfasst Backend-Systeme, gemeinsam genutzte Dienste und nicht UI-spezifische Belange. Sein Hauptziel ist es, als leitender Architektur-Bauplan für KI-gesteuerte Entwicklung und menschliche Entwickler zu dienen, um Konsistenz und die Einhaltung gewählter Muster und Technologien sicherzustellen. Das ACCI EAF wurde entwickelt, um die Entwicklung zu beschleunigen und Enterprise-Softwareprodukte zu standardisieren, die externen Kunden bereitgestellt werden, insbesondere auf der IBM Power Architektur (ppc64le).

**Beziehung zur Frontend-Architektur:**
Das ACCI EAF beinhaltet eine "Control Plane UI" für administrative Aufgaben. Während dieses Dokument die Backend-APIs für diese UI definiert (innerhalb des `eaf-controlplane-api`-Moduls) und eine übergeordnete Technologieentscheidung für das Frontend trifft (React, inspiriert von React-Admin gemäß PRD), würde die detaillierte Frontend-Architektur (Komponentenstruktur, Zustandsverwaltung, spezifische Bibliotheken über React hinaus) typischerweise in einem separaten Frontend-Architektur-Dokument detailliert. Für den Umfang dieses Dokuments konzentrieren wir uns auf die Backend- und Kern-EAF-Architektur. Hier dokumentierte Kerntechnologie-Stack-Entscheidungen (siehe "Definitive Technologie-Stack-Auswahl") sind für das gesamte Projekt endgültig, einschließlich aller Frontend-Komponenten, die als Teil des EAF entwickelt werden.

## 2\\. Inhaltsverzeichnis

1. Einleitung / Präambel
2. Inhaltsverzeichnis
3. Technische Zusammenfassung
4. Übergeordnete Übersicht
5. Komponentenansicht
      * Übernommene Architektur- / Designmuster
6. Projektstruktur
      * Beschreibungen der Schlüsselverzeichnisse
      * Anmerkungen
7. API-Referenz
      * Konsumierte externe APIs
          * 1. LDAP / Active Directory
          * 2. SMTP-Server
          * 3. OpenID Connect (OIDC) Provider
          * 4. SAML 2.0 Identity Provider (IdP)
      * Bereitgestellte interne APIs
          * 1. ACCI EAF Control Plane API (`eaf-controlplane-api`)
          * 2. ACCI EAF Lizenzserver API (`eaf-license-server`)
8. Datenmodelle
      * Kernanwendungsentitäten / Domänenobjekte
          * 1. Mandant (Tenant)
          * 2. Benutzer (IAM-Benutzer)
          * 3. ServiceAccount (IAM Service Account)
          * 4. AktivierteLizenz (ActivatedLicense)
      * API-Payload-Schemata (falls abweichend)
      * Datenbankschemata (falls zutreffend)
          * 1. Event Store Schema
          * 2. Read Model Schemata (Beispiele)
          * 3. Konfigurations- / Zustandsdaten-Schemata (Beispiele)
9. Kernworkflows / Sequenzdiagramme
      * 1. Benutzerauthentifizierung über externen OIDC-Provider
      * 2. Befehlsverarbeitungs- und Event-Sourcing-Fluss
      * 3. Mandantenerstellung im Detail
      * 4. Online-Lizenzaktivierung
10. Definitive Technologie-Stack-Auswahl
11. Infrastruktur- und Bereitstellungsübersicht
12. Fehlerbehandlungsstrategie
13. Kodierungsstandards (ACCI Kotlin Kodierungsstandards v1.0)
      * Primäre Sprache(n) & Laufzeit(en)
      * Style Guide & Linter
      * Namenskonventionen
      * Dateistruktur
      * Asynchrone Operationen
      * Typsicherheit
      * Kommentare & Dokumentation
      * Abhängigkeitsmanagement
      * Detaillierte Sprach- & Framework-Konventionen
          * Kotlin-Spezifika
          * Spring Boot-Spezifika
          * Axon Framework-Spezifika
          * Wichtige Bibliotheksverwendungskonventionen (Allgemein Kotlin/Java)
          * Zu vermeidende Anti-Muster bei der Codegenerierung
14. Gesamt-Teststrategie
15. Sicherheits-Best-Practices
16. Wichtige Referenzdokumente
17. Änderungsprotokoll
18. Aufforderung für Design-Architekt: ACCI EAF Control Plane UI Frontend-Architektur

*(Das Inhaltsverzeichnis wurde aktualisiert, um alle erstellten Abschnitte widerzuspiegeln.)*

## 3\\. Technische Zusammenfassung

Das ACCI EAF (Axians Competence Center Infrastructure Enterprise Application Framework) ist als **modularer Monolith** konzipiert, der für den internen Gebrauch bestimmt ist, um die Entwicklung zu beschleunigen und Enterprise-Softwareprodukte zu standardisieren, die für externe Kunden bereitgestellt werden, speziell ausgerichtet auf die IBM Power Architektur (ppc64le). Es zielt darauf ab, das alte "DCA"-Framework zu ersetzen und signifikante Verbesserungen in Funktionen, Leistung und Entwicklererfahrung zu bieten.

Die Architektur nutzt die Programmiersprache **Kotlin** auf der **JVM**, mit **Spring Boot** als Kernanwendungsframework und **Axon Framework** zur Implementierung von **Domain-Driven Design (DDD)**, **Command Query Responsibility Segregation (CQRS)** und **Event Sourcing (ES)** Mustern. **PostgreSQL** dient als primäre Datenbank für Read Models, Anwendungszustände und als Event Store. Das gesamte Projekt wird in einer **Monorepo**-Struktur mit **Gradle** als Build-Tool verwaltet.

Wichtige Architekturziele umfassen robuste Mandantenfähigkeit, umfassendes Identitäts- und Zugriffsmanagement (IAM), flexible Lizenzierung, Internationalisierung, ein Plugin-System für Erweiterbarkeit sowie hohe Standards für Beobachtbarkeit und Sicherheit. Das EAF ist in Kernmodule wie `eaf-core`, `eaf-iam`, `eaf-multitenancy`, `eaf-licensing`, `eaf-observability`, `eaf-internationalization` und `eaf-plugin-system` strukturiert, ergänzt durch optionale Module wie eine CLI für Entwickler und eine Control Plane mit einer React-basierten UI für die Administration.

## 4\\. Übergeordnete Übersicht

Das ACCI EAF ist als **modularer Monolith** konzipiert. Dieser Architekturstil wurde gewählt, um eine starke Kohäsion der Kernfunktionalitäten des Frameworks mit einer klaren Trennung der Belange zwischen verschiedenen EAF-Modulen (z. B. IAM, Lizenzierung, Mandantenfähigkeit) auszubalancieren. Er vereinfacht auch die Entwicklung und Bereitstellung in den Anfangsphasen und für die Ziel-On-Premise-VM-Umgebungen, während er weiterhin eine gut definierte interne Struktur und zukünftige Skalierbarkeit einzelner Module bei Bedarf ermöglicht. Das EAF bietet eine grundlegende Plattform, auf der verschiedene Unternehmensanwendungen aufgebaut werden.

Die gesamte Codebasis, einschließlich aller Kern-EAF-Module, optionalen Module und Build-Logik, wird in einem **Monorepo** mit Git verwaltet. Dieser Ansatz erleichtert ein zentralisiertes Abhängigkeitsmanagement (über Gradle Version Catalogs), atomare Commits über mehrere Module hinweg, einfacheres Refactoring und konsistente Build- und Testprozesse im gesamten Framework.

Konzeptionell interagiert das ACCI EAF auf einige Schlüsselweisen mit seiner Umgebung und seinen Benutzern:

1. **EAF-basierte Anwendungen:** Softwareprodukte, die mit dem ACCI EAF entwickelt werden, nutzen dessen Kernmodule (`eaf-core` für CQRS/ES-Muster, `eaf-iam` für Sicherheit, `eaf-multitenancy` für Mandantentrennung, `eaf-licensing` für Funktionsberechtigungen usw.) als Bibliotheken oder grundlegende Komponenten. Diese Anwendungen implementieren ihre spezifische Geschäftslogik, während sie sich für übergreifende Belange und standardisierte Unternehmensfunktionalitäten auf das EAF verlassen.
2. **Control Plane UI & API:** Administratoren interagieren mit der `eaf-controlplane-api` (typischerweise über die React-basierte Control Plane UI), um Mandanten, Benutzer innerhalb von Mandanten, Lizenzen und Internationalisierungseinstellungen für EAF-basierte Anwendungen zu verwalten. Diese API fungiert als administrative Schnittstelle zu den Verwaltungsfunktionen des EAF.
3. **Entwickler:** Entwickler interagieren mit dem EAF, indem sie dessen definierte APIs, Erweiterungspunkte (z. B. das Plugin-System) und potenziell CLI-Tools (`eaf-cli`) verwenden, um Anwendungen zu erstellen, zu bauen und zu warten. Das `app-example-module` dient als praktische Anleitung.

Der primäre Datenfluss für EAF-basierte Anwendungen wird oft CQRS/ES-Mustern folgen, die durch `eaf-core` und Axon Framework ermöglicht werden: Befehle ändern den Zustand, indem sie Ereignisse erstellen, die im Event Store (PostgreSQL) persistiert werden, und Abfragen lesen aus dedizierten Read Models (ebenfalls in PostgreSQL), die asynchron aus diesen Ereignissen aktualisiert werden. Administrative Operationen über die Control Plane API interagieren ebenfalls mit den Kernmodulen des EAF, um deren jeweilige Konfigurationen und Daten zu verwalten, wobei oft auch CQRS/ES-Prinzipien für prüfbare Änderungen genutzt werden.

```mermaid
graph TD
    AdminUser[Administrator] -->|Verwaltet über UI| CP_UI[Control Plane UI (React)]
    CP_UI -->|Interagiert mit API| EAF_CP_API[eaf-controlplane-api]

    Developer[Entwickler] -->|Verwendet/Erweitert| ACCI_EAF[ACCI EAF Module]
    ACCI_EAF -->|Baut auf| JVM[Kotlin/JVM]
    JVM -->|Verwendet| SpringBoot[Spring Boot]
    SpringBoot -->|Integriert| Axon[Axon Framework]
    Axon -->|Persistiert in/Liest aus| PostgreSQL[PostgreSQL (Event Store / Read Models)]

    subgraph ACCI EAF Module
        direction LR
        EAF_Core[eaf-core]
        EAF_IAM[eaf-iam]
        EAF_MultiTenancy[eaf-multitenancy]
        EAF_Licensing[eaf-licensing]
        EAF_PluginSystem[eaf-plugin-system]
        EAF_Observability[eaf-observability]
        EAF_i18n[eaf-internationalization]
        EAF_CP_API
    end

    EAF_App[EAF-basierte Anwendung] -->|Nutzt| ACCI_EAF
    EAF_App -->|Dient| EndUser[Endbenutzer]

    classDef AdminUser fill:#c9f,stroke:#333,stroke-width:2px
    classDef Developer fill:#c9f,stroke:#333,stroke-width:2px
    classDef EndUser fill:#c9f,stroke:#333,stroke-width:2px
    classDef CP_UI fill:#9cf,stroke:#333,stroke-width:2px
    classDef EAF_App fill:#9cf,stroke:#333,stroke-width:2px
```

*{Hinweis: Das obige Mermaid-Diagramm ist eine erste konzeptionelle Darstellung. Es kann im weiteren Verlauf verfeinert und durch spezifischere Diagramme (z.B. C4 Layer 1 & 2) ergänzt oder ersetzt werden.}*

## 5\\. Komponentenansicht

Das ACCI EAF ist als modularer Monolith strukturiert, mit distinkten logischen Komponenten (Gradle-Teilprojekte innerhalb des Monorepos), die spezifische Framework-Funktionalitäten kapseln. Diese Komponenten sind so konzipiert, dass sie kohäsiv und so lose wie möglich gekoppelt sind, was die Wartbarkeit und, wo möglich, die unabhängige Entwicklung fördert.

Im Folgenden sind die wichtigsten logischen Komponenten des ACCI EAF und ihre Verantwortlichkeiten aufgeführt:

* **`build-logic`**:
  * *Verantwortlichkeit:* Enthält die zentrale Build-Logik, Abhängigkeitsversionen (verwaltet über Gradle Version Catalogs) und Build-/Entwicklungskonventionen für alle Module innerhalb des Monorepos. Es gewährleistet Konsistenz im Build-Prozess über das gesamte EAF hinweg.
* **Framework-Kernmodule:**
  * **`eaf-core`**:
    * *Verantwortlichkeit:* Stellt die fundamentalen Bausteine und Kernabstraktionen für EAF-basierte Anwendungen bereit. Dies umfasst Basisklassen/-schnittstellen für Aggregate, Befehle, Ereignisse, allgemeine Hilfsprogramme und die grundlegende Konfiguration für CQRS (Command Query Responsibility Segregation) und ES (Event Sourcing) Muster, unter Nutzung des Axon Frameworks. Es bildet das Herzstück von Anwendungen, die mit dem EAF erstellt wurden.
  * **`eaf-iam` (Identitäts- & Zugriffsmanagement)**:
    * *Verantwortlichkeit:* Implementiert umfassende Funktionalitäten für Benutzerverwaltung, Authentifizierung (unterstützt lokales Anmeldeinformationsmanagement und Integration mit externen Anbietern wie LDAP/AD, OIDC, SAML2) und Autorisierung (Rollenbasierte Zugriffskontrolle - RBAC, mit grundlegenden Elementen für zukünftige Attributbasierte Zugriffskontrolle - ABAC). Es beinhaltet auch Unterstützung für Service-Konten für System-zu-System-Integrationen. Dieses Modul ist als wiederverwendbare Framework-Komponente konzipiert.
  * **`eaf-multitenancy`**:
    * *Verantwortlichkeit:* Stellt die Logik und Mechanismen zur Unterstützung der Mandantenfähigkeit in EAF-basierten Anwendungen bereit. Dies umfasst Strategien zur Mandantenisolierung (z. B. durch Row-Level Security in PostgreSQL für Datentrennung) und die Verwaltung des Mandantenkontexts während Anwendungsanfragen und der Ausführung von Geschäftslogik.
  * **`eaf-licensing`**:
    * *Verantwortlichkeit:* Bietet Funktionen für das Lizenzmanagement, die für mit dem ACCI EAF erstellte Anwendungen gelten. Dies beinhaltet Funktionen zur Definition verschiedener Lizenztypen (z. B. zeitlich begrenzt, hardwaregebunden) und Mechanismen für die Offline- und Online-Lizenzaktivierung und -validierung.
  * **`eaf-observability`**:
    * *Verantwortlichkeit:* Liefert standardisierte Konfigurationen, Integrationen und Werkzeuge für die Anwendungsbeobachtbarkeit. Dies umfasst strukturiertes Logging, Metrikexposition kompatibel mit Prometheus (über Micrometer), standardisierte Health-Check-Endpunkte (unter Nutzung von Spring Boot Actuator) und dedizierte Audit-Logging-Funktionen für kritische Operationen.
  * **`eaf-internationalization` (i18n)**:
    * *Verantwortlichkeit:* Stellt Werkzeuge, Konventionen und eine Basisinfrastruktur für die Internationalisierung und Lokalisierung von EAF-basierten Anwendungen bereit. Dies beinhaltet Unterstützung für die Verwaltung von Sprachressourcen und mandantenspezifischen Übersetzungen.
  * **`eaf-plugin-system`**:
    * *Verantwortlichkeit:* Implementiert die Plugin-Infrastruktur, basierend auf der Java ServiceLoader API. Dieses System ermöglicht es dem EAF selbst und darauf aufbauenden Anwendungen, modular durch wohldefinierte Dienstanbieterschnittstellen erweitert zu werden, wodurch Funktionserweiterungen ohne Änderung des Kerncodes ermöglicht werden.
* **Optionale / Unterstützende Module:**
  * **`eaf-cli`**:
    * *Verantwortlichkeit:* Erleichtert die Entwicklung von Command Line Interface (CLI) Tools, die sich an Entwickler richten, die das EAF verwenden. Diese Tools können bei Aufgaben wie Projekt-Scaffolding, Codegenerierung für EAF-Muster und Diagnoseprogrammen unterstützen.
  * **`app-example-module`**:
    * *Verantwortlichkeit:* Dient als Referenzimplementierung und Schnellstartanleitung für Entwickler. Es demonstriert, wie eine typische Geschäftsanwendung oder ein spezifisches Domänenmodul unter Verwendung von ACCI EAF-Komponenten entwickelt werden kann, unter Einhaltung seiner Architekturprinzipien und Best Practices.
  * **`eaf-controlplane-api`**: (Backend für die Control Plane UI)
    * *Verantwortlichkeit:* Stellt die RESTful APIs bereit, die von der Control Plane UI benötigt werden. Diese APIs ermöglichen es Administratoren, Mandanten, Benutzer, Lizenzen und Internationalisierungseinstellungen über EAF-basierte Anwendungen hinweg zu verwalten.
  * **`eaf-license-server`**: (Eine EAF-basierte Anwendung selbst)
    * *Verantwortlichkeit:* Stellt die serverseitige Logik für die Online-Lizenzaktivierung und -validierung bereit und fungiert als zentraler Dienst für Produkte, die diese Funktionalität benötigen.

**Zusammenarbeit und Modulabhängigkeiten:**
Diese Module arbeiten zusammen, um ein umfassendes Anwendungsframework bereitzustellen. Beispielsweise würde bei einer eingehenden Anfrage an eine EAF-basierte Anwendung zuerst deren Mandantenkontext durch `eaf-multitenancy` hergestellt. Sicherheitsprüfungen (Authentifizierung und Autorisierung) würden von `eaf-iam` gehandhabt. Geschäftslogik, potenziell unter Verwendung von CQRS/ES-Mustern aus `eaf-core`, würde dann ausgeführt. Alle Operationen unterlägen Beobachtbarkeitsmaßnahmen von `eaf-observability`. Das `eaf-plugin-system` ermöglicht benutzerdefinierte Erweiterungen dieser Abläufe oder das Hinzufügen neuer Geschäftsfähigkeiten. Administrative Funktionen werden über `eaf-controlplane-api` bereitgestellt, das wiederum die anderen EAF-Module verwendet, um Änderungen zu bewirken.

Das folgende Diagramm veranschaulicht die Schlüsselmodule des ACCI EAF und ihre primären Abhängigkeiten. Es unterscheidet zwischen "Kern-EAF-Modulen" und "Optionalen/Unterstützenden EAF-Anwendungen & Werkzeugen" und zeigt auch, wie eine typische "EAF-basierte Anwendung" diese Module nutzen würde.

```mermaid
graph TD
    subgraph "Kern-EAF-Module"
        direction LR
        Core["eaf-core"]
        IAM["eaf-iam"]
        MultiTenancy["eaf-multitenancy"]
        Licensing["eaf-licensing"]
        Observability["eaf-observability"]
        I18N["eaf-internationalization"]
        PluginSystem["eaf-plugin-system"]
    end

    subgraph "Optionale/Unterstützende EAF-Anwendungen & Werkzeuge"
        direction LR
        CLI["eaf-cli"]
        ExampleApp["app-example-module"]
        CP_API["eaf-controlplane-api"]
        LicenseServer["eaf-license-server"]
    end

    %% Kernabhängigkeiten: Die meisten Kernmodule hängen von eaf-core ab
    IAM --> Core
    MultiTenancy --> Core
    Licensing --> Core
    Observability --> Core
    I18N --> Core
    PluginSystem --> Core

    %% Anwendungs-/Werkzeugabhängigkeiten von Kernmodulen
    CP_API --> Core
    CP_API --> IAM
    CP_API --> MultiTenancy
    CP_API --> Licensing
    CP_API --> I18N
    CP_API --> Observability

    LicenseServer --> Core
    LicenseServer --> Licensing
    LicenseServer --> IAM
    LicenseServer --> Observability
    LicenseServer --> MultiTenancy %% Annahme, dass der Lizenzserver selbst mandantenfähig sein muss oder seinen eigenen Admin-Zugang sichert

    ExampleApp --> Core
    ExampleApp --> IAM
    ExampleApp --> MultiTenancy
    ExampleApp --> Licensing
    ExampleApp --> Observability
    ExampleApp --> I18N
    ExampleApp --> PluginSystem %% Zur Demonstration der Verwendung/Bereitstellung von Plugins

    CLI --> Core %% Für gemeinsame Hilfsprogramme oder zum Verständnis von EAF-Projektstrukturen

    %% EAF-basierte Anwendungen würden diese Module verwenden
    ExternalApp["EAF-basierte Anwendung (Allgemeines Beispiel)"]
    ExternalApp --> Core
    ExternalApp -.-> IAM
    ExternalApp -.-> MultiTenancy
    ExternalApp -.-> Licensing
    ExternalApp -.-> Observability
    ExternalApp -.-> I18N
    ExternalApp -.-> PluginSystem

    classDef coreModule fill:#D6EAF8,stroke:#2874A6,stroke-width:2px;
    classDef suppModule fill:#D1F2EB,stroke:#117A65,stroke-width:2px;
    classDef externalApp fill:#FEF9E7,stroke:#B7950B,stroke-width:2px;

    class Core,IAM,MultiTenancy,Licensing,Observability,I18N,PluginSystem coreModule;
    class CLI,ExampleApp,CP_API,LicenseServer suppModule;
    class ExternalApp externalApp
```

### 5.1 Übernommene Architektur- / Designmuster

Das ACCI EAF übernimmt explizit mehrere Schlüsselarchitektur- und Designmuster, um seine Ziele der Modularität, Wartbarkeit, Skalierbarkeit und Ausrichtung an modernen Praktiken der Unternehmensanwendungsentwicklung zu erreichen. Diese grundlegenden Muster leiten das Design von Komponenten, deren Interaktionen und Technologieentscheidungen:

* **Modularer Monolith:**
  * *Begründung:* Gewählt, um eine einzelne bereitstellbare Einheit für On-Premise-VM-Umgebungen bereitzustellen, was die anfängliche Betriebskomplexität vereinfacht und gleichzeitig eine starke logische Trennung und Kohäsion durch wohldefinierte Module (Gradle-Teilprojekte) ermöglicht. Es erlaubt eine fokussierte Entwicklung innerhalb einer einzigen Codebasis und konsistente Werkzeuge.
* **Hexagonale Architektur (Ports und Adapter):**
  * *Begründung:* Um die Kernanwendungslogik (Domänen- und Anwendungsdienste) von externen Belangen wie UI, Datenbanken, Nachrichtensystemen oder anderen Drittanbieterintegrationen zu entkoppeln. Dies wird durch die Definition klarer "Ports" (Schnittstellen im Anwendungskern) und "Adapter" (Implementierungen dieser Schnittstellen für spezifische Technologien) erreicht. Dieses Muster verbessert die Testbarkeit, Wartbarkeit und die Fähigkeit, Technologien auszutauschen oder mit neuen Systemen zu integrieren, mit minimalen Auswirkungen auf die Kernlogik. Jedes EAF-Modul und darauf aufbauende Anwendungen sollten bestrebt sein, dieses Muster intern zu befolgen.
* **Domain-Driven Design (DDD):**
  * *Begründung:* Um die Komplexität von Unternehmensanwendungen durch Fokussierung auf die Kerndomäne und Domänenlogik zu bewältigen. DDD-Prinzipien wie ubiquitäre Sprache, Aggregate, Entitäten, Wertobjekte, Repositories und Domänendienste werden angewendet, um die Geschäftsprobleme innerhalb der EAF-Module (z. B. `eaf-iam`, `eaf-licensing`) genau zu modellieren und Entwickler beim Erstellen von Anwendungen auf dem EAF anzuleiten.
* **Command Query Responsibility Segregation (CQRS):**
  * *Begründung:* Um das Modell zur Aktualisierung von Informationen (Befehle) vom Modell zum Lesen von Informationen (Abfragen) zu trennen. Dies ermöglicht eine unabhängige Optimierung jeder Seite. Beispielsweise kann sich die Befehlsseite auf Konsistenz und Validierung konzentrieren (unter Nutzung von DDD-Aggregaten), während die Abfrageseite denormalisierte Lesemodelle verwenden kann, die für spezifische Abfrageanforderungen optimiert sind, wodurch Leistung und Skalierbarkeit verbessert werden. Das Axon Framework bietet starke Unterstützung für die Implementierung dieses Musters.
* **Event Sourcing (ES):**
  * *Begründung:* Um alle Änderungen am Anwendungszustand als eine Sequenz unveränderlicher Ereignisse zu erfassen. Anstatt nur den aktuellen Zustand zu speichern, speichert das EAF die vollständige Historie dessen, was passiert ist. Dies bietet starke Audit-Funktionen, erleichtert das Debugging und temporale Abfragen und ermöglicht den Wiederaufbau des Zustands oder die Erstellung neuer Lesemodellprojektionen aus dem Ereignisprotokoll. ES passt natürlich zu CQRS und DDD und ist auch eine Kernfunktion, die vom Axon Framework unter Verwendung von PostgreSQL als Event Store unterstützt wird.

## 6\\. Projektstruktur

Das ACCI EAF wird als Monorepo entwickelt, das mit Git verwaltet wird. Gradle dient als Build-System und orchestriert den Build aller Module (Gradle-Teilprojekte). Das folgende Diagramm und die Beschreibungen skizzieren die vorgeschlagene Projektordnerstruktur. Diese Struktur ist darauf ausgelegt, die modulare Monolith-Architektur zu unterstützen und Belange effektiv zu trennen.

```plaintext
ACCI-EAF/
├── .github/                    # CI/CD-Workflows (z.B. GitHub Actions)
│   └── workflows/
│       └── ci.yml
├── .vscode/                    # VSCode-Einstellungen (optional)
│   └── settings.json
├── build-logic/                # Zentrale Gradle-Build-Logik (Konventions-Plugins)
│   └── src/main/kotlin/        # Benutzerdefinierte Tasks, Plugins für Build-Konsistenz
├── docs/                       # Projektdokumentation
│   ├── ACCI-EAF-PRD.md
│   ├── ACCI-EAF-Architecture.md # Dieses Dokument (Englische Version)
│   └── adr/                    # Architekturentscheidungen (ADRs)
│   └── de/
│       └── ACCI-EAF-Architecture.md # Dieses Dokument (Deutsche Version)
├── gradle/                     # Gradle-Wrapper-Dateien
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── eaf-core/                   # Kern-EAF-Modul (grundlegende APIs, CQRS/ES-Basis)
│   ├── src/main/kotlin/        # Kotlin-Quellcode
│   ├── src/main/resources/     # Ressourcendateien
│   ├── src/test/kotlin/        # Unit- und Integrationstests für dieses Modul
│   └── build.gradle.kts        # Modulspezifisches Gradle-Build-Skript
├── eaf-iam/                    # Identitäts- & Zugriffsmanagement-Modul
│   └── ... (ähnliche Struktur wie eaf-core)
├── eaf-multitenancy/           # Mandantenfähigkeitsmodul
│   └── ...
├── eaf-licensing/              # Lizenzierungsmodul
│   └── ...
├── eaf-observability/          # Beobachtbarkeitsmodul
│   └── ...
├── eaf-internationalization/   # Internationalisierungs (i18n)-Modul
│   └── ...
├── eaf-plugin-system/          # Plugin-System-Infrastrukturmodul
│   └── ...
├── eaf-cli/                    # Optional: CLI-Tools für EAF-Entwickler
│   └── ...
├── app-example-module/         # Optional: Beispielanwendung/-modul unter Verwendung des EAF
│   └── ...
├── eaf-controlplane-api/       # Optional: Backend-API für die Control Plane (Spring Boot-Anwendung)
│   ├── src/main/kotlin/        # Kotlin-Quellcode für die API-Anwendung
│   ├── src/main/resources/
│   ├── src/test/kotlin/
│   └── build.gradle.kts
├── eaf-license-server/         # Optional: Lizenzserver-Anwendung (auf EAF aufgebaut)
│   ├── src/main/kotlin/        # Kotlin-Quellcode für die Lizenzserver-Anwendung
│   ├── src/main/resources/
│   ├── src/test/kotlin/
│   └── build.gradle.kts
├── controlplane-ui/            # Optional: Frontend React-Anwendung für die Control Plane
│   ├── public/
│   ├── src/                    # React-Komponentenquellen etc.
│   ├── package.json
│   ├── tsconfig.json
│   └── build.gradle.kts        # Optional: Gradle-Build für Frontend (z.B. über Node-Plugin für Integration)
├── test/                       # Top-Level-Tests (z.B. E2E-Tests über mehrere Module) - (Optional)
│   └── e2e/
├── .env.example                # Beispiel für Umgebungsvariablen
├── .gitattributes              # Git-Attribute für Zeilenenden etc.
├── .gitignore                  # Spezifiziert absichtlich nicht getrackte Dateien, die Git ignorieren soll
├── build.gradle.kts            # Root Gradle-Build-Datei (gemeinsame Konfigurationen, Plugin-Versionen)
├── settings.gradle.kts         # Gradle-Einstellungsdatei (deklariert alle Module/Teilprojekte)
├── gradlew                     # Gradle-Wrapper-Skript für Unix-ähnliche Systeme
├── gradlew.bat                 # Gradle-Wrapper-Skript für Windows
└── README.md                   # Projektübersicht, Setup- und Nutzungsanweisungen
```

### 6.1 Beschreibungen der Schlüsselverzeichnisse

* **`ACCI-EAF/`**: Das Stammverzeichnis des Monorepos.
* **`.github/workflows/`**: Enthält CI/CD-Pipeline-Definitionen (z.B. für GitHub Actions), einschließlich Build-, Test- und potenziell Release-Workflows.
* **`build-logic/`**: Beherbergt benutzerdefinierte Gradle-Konventions-Plugins und gemeinsame Build-Konfigurationen, um Konsistenz über alle Module hinweg sicherzustellen (z.B. Kotlin-Versionen, gemeinsame Abhängigkeiten, Compiler-Optionen). Dies ist ein Standardansatz von Gradle zur Verwaltung komplexer Builds.
* **`docs/`**: Enthält die gesamte projektbezogene Dokumentation, einschließlich dieses Architektur-Dokuments, des PRDs und potenziell ADRs (Architectural Decision Records) im Unterverzeichnis `docs/adr/`, Diagramme usw. Das Verzeichnis `docs/de/` enthält die deutsche Version dieses Dokuments.
* **`gradle/wrapper/`**: Enthält den Gradle Wrapper, der es Entwicklern ermöglicht, das Projekt mit einer konsistenten Gradle-Version zu bauen, ohne eine systemweite Installation zu benötigen.
* **`eaf-*` (z.B. `eaf-core/`, `eaf-iam/`)**: Diese Verzeichnisse repräsentieren die einzelnen Module (Gradle-Teilprojekte) des ACCI EAF. Jedes Modul folgt typischerweise einer Standard-Kotlin/Gradle-Projektstruktur:
  * **`src/main/kotlin/`**: Enthält den Haupt-Kotlin-Quellcode für das Modul.
    * Innerhalb dessen sollten Prinzipien der Hexagonalen Architektur angewendet werden, indem Code in Pakete wie `domain` (Kerngeschäftslogik, Entitäten, Wertobjekte, Domänenereignisse), `application` (Anwendungsfälle, Anwendungsdienste, die Domänenlogik orchestrieren) und `adapters` (oder `infrastructure`) für Implementierungen von Ports (z.B. REST-Controller, Datenbank-Repositories, Event-Listener) organisiert wird.
  * **`src/main/resources/`**: Enthält Ressourcendateien für das Modul (z.B. modulspezifische Spring Boot `application.yml`, falls es sich um eine ausführbare App handelt, Datenbankmigrationsskripte, i18n-Bundles).
  * **`src/test/kotlin/`**: Enthält Unit- und Integrationstests für den Code des Moduls. Die Organisation der Testdateien sollte die Struktur von `src/main/kotlin/` widerspiegeln.
  * **`build.gradle.kts`**: Das Gradle-Build-Skript, das spezifisch für dieses Modul ist und dessen Abhängigkeiten, Plugins und Build-Konfigurationen deklariert.
* **`eaf-controlplane-api/`**: Ein spezifisches Spring Boot-Anwendungsmodul, das das Backend für die administrative UI bereitstellt.
* **`eaf-license-server/`**: Ein weiteres spezifisches Spring Boot-Anwendungsmodul, das auf EAF-Prinzipien aufgebaut ist, um Online-Lizenzoperationen zu handhaben.
* **`controlplane-ui/`**: Enthält den Quellcode für das React-basierte Frontend der Control Plane. Dies ist eine Standard-Node.js-Projektstruktur (z.B. erstellt mit Create React App oder ähnlich). Seine detaillierte interne Struktur würde in einem separaten Frontend-Architektur-Dokument definiert, falls eines erstellt würde. Es könnte ein eigenes `build.gradle.kts` haben, wenn sein Build in den Haupt-Gradle-Build integriert ist.
* **`test/e2e/`**: (Optional) Für End-to-End-Tests, die Workflows über mehrere Module oder das gesamte System validieren.
* **`build.gradle.kts` (root)**: Die Haupt-Gradle-Build-Datei im Stammverzeichnis des Projekts. Sie definiert typischerweise gemeinsame Konfigurationen, Plugin-Versionen (unter Verwendung des Plugin-Management-Blocks) und wendet Plugins auf Teilprojekte an.
* **`settings.gradle.kts`**: Die Gradle-Einstellungsdatei. Sie ist entscheidend, da sie alle Teilprojekte (Module wie `eaf-core`, `eaf-iam` usw.) deklariert, die Teil des Multi-Projekt-Builds sind.

### 6.2 Anmerkungen

* **Build-Ausgabe:** Kompilierte Artefakte (JARs, WARs falls vorhanden) für jedes Modul befinden sich typischerweise in ihren jeweiligen `build/libs/`-Verzeichnissen. Ausführbare Anwendungen (wie `eaf-controlplane-api` oder `eaf-license-server`) erzeugen ausführbare JARs.
* **IDE-Integration:** Diese Struktur ist Standard für Gradle-Multi-Projekt-Builds und sollte von IDEs wie IntelliJ IDEA (mit exzellenter Kotlin- und Gradle-Unterstützung) gut unterstützt werden.
* **Hexagonale Struktur innerhalb von Modulen:** Wie in "Beschreibungen der Schlüsselverzeichnisse" erwähnt, wird erwartet, dass einzelne EAF-Module (insbesondere solche mit signifikanter Geschäftslogik wie `eaf-iam` oder `eaf-licensing`) intern eine Paketstruktur annehmen, die die Hexagonale Architektur widerspiegelt (z.B. `com.axians.accieaf.[modul].domain`, `com.axians.accieaf.[modul].application`, `com.axians.accieaf.[modul].adapter.rest`, `com.axians.accieaf.[modul].adapter.persistence`). Dies wird im Abschnitt "Kodierungsstandards" weiter detailliert.

## 7\\. API-Referenz

Dieser Abschnitt beschreibt die Anwendungsprogrammierschnittstellen (APIs), mit denen das ACCI EAF-System interagieren wird, sowohl jene, die von externen Quellen konsumiert werden, als auch jene, die von seinen eigenen Komponenten bereitgestellt werden.

### 7.1 Konsumierte externe APIs

Das ACCI EAF, insbesondere durch seine `eaf-iam`- und Benachrichtigungsfunktionen, wird die folgenden externen Dienste/Protokolle konsumieren:

#### 7.1.1 LDAP / Active Directory

* **Zweck:** Wird von `eaf-iam` für die externe Benutzerauthentifizierung und zum Abrufen von Benutzerattributen (z.B. Gruppenmitgliedschaften, E-Mail-Adressen) aus einem Unternehmensverzeichnisdienst verwendet.
* **Protokoll:** Lightweight Directory Access Protocol (LDAP v3)
* **Verbindungsparameter (pro Bereitstellung/Mandant zu konfigurieren):**
  * LDAP Server Hostname(s): `{ldap_host}`
  * LDAP Server Port: `{ldap_port}` (z.B. 389 für LDAP, 636 für LDAPS)
  * SSL/TLS verwenden (LDAPS): `true/false`
  * StartTLS verwenden: `true/false` (falls Port 389 und SSL/TLS gewünscht)
  * Bind DN (Dienstkonto für Suchen, optional): `{bind_dn_user}`
  * Bind-Passwort (Passwort des Dienstkontos, optional): `{bind_dn_password}` (Sicher gespeichert, z.B. über Umgebungsvariablen oder gemountete Secrets)
  * Benutzersuchbasis-DN: `{user_search_base}` (z.B. `ou=users,dc=example,dc=com`)
  * Benutzersuchfilter: `{user_search_filter}` (z.B. `(&(objectClass=person)(sAMAccountName={0}))` oder `(&(objectClass=inetOrgPerson)(uid={0}))`)
  * Gruppensuchbasis-DN: `{group_search_base}` (z.B. `ou=groups,dc=example,dc=com`)
  * Gruppensuchfilter: `{group_search_filter}` (z.B. `(&(objectClass=group)(member={0}))`)
  * Attributzuordnungen: (Konfigurierbare Karte von EAF-Benutzerattributen zu LDAP-Attributen, z.B. `username -> sAMAccountName`, `email -> mail`, `displayName -> displayName`, `groups -> memberOf`)
* **Authentifizierungsmethode für EAF-Dienstkonto:** Simple BIND mit dem konfigurierten Bind DN und Passwort (falls anonymes Binden für Suchen nicht ausreicht).
* **Authentifizierungsmethode für Endbenutzer:** Simple BIND-Operation gegen den LDAP-Server unter Verwendung des vom Benutzer bereitgestellten Benutzernamens (transformiert über Suchfilter) und Passworts.
* **Schlüsseloperationen, die von `eaf-iam` verwendet werden:**
  * **BIND-Operation:**
    * Beschreibung: Zur Authentifizierung eines Benutzers durch Versuch, sich mit dessen bereitgestellten Anmeldeinformationen am LDAP-Server anzumelden.
    * Wird auch vom EAF-Dienstkonto (falls konfiguriert) verwendet, um eine Verbindung für Suchen herzustellen.
  * **SEARCH-Operation:**
    * Beschreibung: Um den DN eines Benutzers basierend auf seinem Anmeldenamen zu finden und Benutzerattribute abzurufen (z.B. zum Erstellen eines Benutzerprofils im EAF oder zum Überprüfen von Gruppenmitgliedschaften für die Autorisierung).
    * Um Gruppen zu finden, deren Mitglied ein Benutzer ist.
* **Datenformat:** LDAP Data Interchange Format (LDIF) für Einträge und Attribute.
* **Fehlerbehandlung:** LDAP-Ergebniscodes (z.B. "Ungültige Anmeldeinformationen", "Kein solches Objekt", "Server nicht erreichbar") werden abgefangen und auf entsprechende interne EAF-Ausnahmen oder Fehlerantworten abgebildet. Verbindungs- und Suchzeitüberschreitungen müssen konfiguriert werden.
* **Link zu offiziellen Dokumenten:**
  * LDAP v3: [RFC 4510-4519](https://datatracker.ietf.org/doc/html/rfc4510) (und verwandte RFCs)
  * Active Directory: Microsoft-Dokumentation für AD LDAP.

#### 7.1.2 SMTP-Server

* **Zweck:** Wird von verschiedenen EAF-Modulen oder EAF-basierten Anwendungen zum Senden von E-Mail-Benachrichtigungen verwendet (z.B. Links zum Zurücksetzen von Passwörtern, Systemwarnungen, Lizenzbenachrichtigungen).
* **Protokoll:** Simple Mail Transfer Protocol (SMTP)
* **Verbindungsparameter (pro Bereitstellung zu konfigurieren):**
  * SMTP Server Hostname: `{smtp_host}`
  * SMTP Server Port: `{smtp_port}` (z.B. 25, 465 für SMTPS, 587 für SMTP mit STARTTLS)
  * Authentifizierung erforderlich: `true/false`
  * Benutzername (falls Authentifizierung erforderlich): `{smtp_username}`
  * Passwort (falls Authentifizierung erforderlich): `{smtp_password}` (Sicher gespeichert)
  * Transportsicherheit: Keine / SSL/TLS (SMTPS) / STARTTLS
  * Standard-Absenderadresse ("From"): `{default_from_address}`
* **Authentifizierungsmethode:** Typischerweise SMTP AUTH (z.B. PLAIN, LOGIN, CRAM-MD5), falls vom Server erforderlich.
* **Schlüsseloperationen, die verwendet werden:**
  * **Senden einer E-Mail:**
    * Beschreibung: Das EAF konstruiert eine E-Mail-Nachricht (Header, Body) und überträgt sie zur Zustellung an den konfigurierten SMTP-Server.
    * Beteiligte SMTP-Schlüsselbefehle: `EHLO/HELO`, `MAIL FROM`, `RCPT TO`, `DATA`.
* **Datenformat:** E-Mail-Nachrichten formatiert gemäß RFC 5322 (Internet Message Format) und verwandten MIME-Standards (RFC 2045-2049).
* **Fehlerbehandlung:** SMTP-Antwortcodes (z.B. "550 No such user here", "421 Service not available") werden behandelt. Verbindungsfehler, Zeitüberschreitungen und Authentifizierungsfehler werden protokolliert und können Wiederholungsmechanismen auslösen oder Administratoren alarmieren.
* **Link zu offiziellen Dokumenten:**
  * SMTP: [RFC 5321](https://datatracker.ietf.org/doc/html/rfc5321)
  * MIME: [RFC 2045-2049](https://datatracker.ietf.org/doc/html/rfc2045)

#### 7.1.3 OpenID Connect (OIDC) Provider

* **Zweck:** Wird von `eaf-iam` verwendet, um die externe Benutzerauthentifizierung über OpenID Connect 1.0 zu ermöglichen, sodass sich Benutzer mit ihren vorhandenen Konten von einem konfigurierten OIDC Identity Provider (IdP) anmelden können.
* **Protokoll:** OpenID Connect 1.0 (aufbauend auf OAuth 2.0).
* **Interaktionstyp:** Das EAF fungiert als OIDC Relying Party (RP). Der typische Fluss ist der Authorization Code Flow.
* **Schlüsselendpunkte & Metadaten (bereitgestellt vom OIDC Provider):**
  * **Discovery Endpoint (`/.well-known/openid-configuration`):**
    * Beschreibung: Eine bekannte URI, unter der der OIDC Provider seine Metadaten veröffentlicht. Diese Metadaten enthalten URLs für andere notwendige Endpunkte (Authorization, Token, UserInfo, JWKS), unterstützte Scopes, Antworttypen, Claims und kryptografische Algorithmen.
    * `eaf-iam` ruft diese Metadaten ab und verwendet sie, um seine Interaktion mit dem OIDC-Provider dynamisch oder bei der Einrichtung zu konfigurieren.
  * **Authorization Endpoint:**
    * Beschreibung: Der Endpunkt beim OIDC Provider, zu dem der Benutzer vom EAF zur Authentifizierung und Zustimmung weitergeleitet wird.
    * Interaktion: EAF leitet den Browser des Benutzers zu dieser URL mit Parametern wie `client_id`, `response_type=code`, `scope`, `redirect_uri`, `state`, `nonce` weiter.
  * **Token Endpoint:**
    * Beschreibung: Der Endpunkt beim OIDC Provider, an dem das EAF (RP) einen Autorisierungscode (erhalten über Redirect vom Authorization Endpoint) gegen ein ID Token, Access Token und optional ein Refresh Token austauscht.
    * Interaktion: EAF sendet eine direkte (Server-zu-Server) POST-Anfrage an diese URL mit Parametern wie `grant_type=authorization_code`, `code`, `redirect_uri`, `client_id`, `client_secret`.
  * **UserInfo Endpoint (Optional):**
    * Beschreibung: Eine OAuth 2.0 geschützte Ressource beim OIDC Provider, wo das EAF zusätzliche Claims über den authentifizierten Benutzer unter Verwendung des vom Token Endpoint erhaltenen Access Tokens abrufen kann.
    * Interaktion: EAF sendet eine GET- oder POST-Anfrage an diese URL, einschließlich des Access Tokens im `Authorization`-Header.
  * **JWKS (JSON Web Key Set) URI:**
    * Beschreibung: Ein Endpunkt, an dem der OIDC Provider seine öffentlichen Schlüssel (im JWK-Format) veröffentlicht, die zum Signieren von ID Tokens verwendet werden.
    * Interaktion: `eaf-iam` ruft diese Schlüssel ab, um die Signatur empfangener ID Tokens zu validieren.
* **Authentifizierung (EAF RP gegenüber OIDC Provider):**
  * Das EAF wird als Client beim OIDC Provider registriert und erhält eine `client_id`.
  * Für den Token Endpoint authentifiziert sich das EAF unter Verwendung seiner `client_id` und eines `client_secret` (oder anderer Client-Authentifizierungsmethoden wie Private Key JWT).
* **Anfrage-/Antwortdatenformate:**
  * **ID Token:** Ein JSON Web Token (JWT), das Claims über das Authentifizierungsereignis und den Benutzer enthält (z.B. `iss` (Aussteller), `sub` (Subjekt/Benutzer-ID), `aud` (Zielgruppe/Client-ID), `exp` (Ablauf), `iat` (ausgestellt am), `nonce`, `email`, `name`, `preferred_username`). Das ID Token ist das primäre Artefakt für die Authentifizierung.
  * **Access Token:** Für das EAF normalerweise eine opake Zeichenfolge, die verwendet wird, um den Zugriff auf den UserInfo Endpoint zu autorisieren. Das Format ist spezifisch für den OIDC Provider.
  * **UserInfo Response:** JSON-Objekt, das Benutzer-Claims enthält.
* **Schlüsselkonfigurationsparameter (pro OIDC Provider / Mandant):**
  * Issuer URL (z.B. `https://idp.example.com/oidc`)
  * Client ID (erhalten von der OIDC Provider-Registrierung)
  * Client Secret (erhalten von der OIDC Provider-Registrierung, sicher gespeichert)
  * Redirection URI(s) (EAF-Endpunkt(e), zu dem Benutzer nach der Authentifizierung zurückgeleitet werden, z.B. `https://eaf-app.example.com/login/oauth2/code/{registrationId}`)
  * Angeforderte Scopes (z.B. `openid`, `profile`, `email`, benutzerdefinierte Scopes)
  * Attributzuordnungen (falls erforderlich, um OIDC-Claims EAF-Benutzerprofilattributen zuzuordnen)
  * Bevorzugter JWS (JSON Web Signature)-Algorithmus für die ID Token-Validierung.
* **Fehlerbehandlung:** OAuth 2.0 und OIDC spezifische Fehlercodes (z.B. `invalid_request`, `unauthorized_client`, `access_denied`, `invalid_grant`), die vom OIDC Provider zurückgegeben werden, werden behandelt. Fehler bei der ID Token-Validierung (Signatur, Aussteller, Zielgruppe, Ablauf, Nonce) müssen zu einem Authentifizierungsfehler führen.
* **Link zu offiziellen Dokumenten:**
  * OpenID Connect Core 1.0: [https://openid.net/specs/openid-connect-core-1_0.html](https://openid.net/specs/openid-connect-core-1_0.html)
  * OpenID Connect Discovery 1.0: [https://openid.net/specs/openid-connect-discovery-1_0.html](https://openid.net/specs/openid-connect-discovery-1_0.html)

#### 7.1.4 SAML 2.0 Identity Provider (IdP)

* **Zweck:** Wird von `eaf-iam` verwendet, um die externe Benutzerauthentifizierung über das SAML 2.0 Web Browser SSO Profile zu ermöglichen, sodass sich Benutzer mit ihren vorhandenen Unternehmensidentitäten anmelden können.
* **Protokoll:** SAML (Security Assertion Markup Language) 2.0.
* **Interaktionstyp:** Das EAF fungiert als SAML Service Provider (SP). Der typische Fluss ist das Web Browser SSO Profile (oft SP-initiiert).
* **Schlüsselendpunkte & Metadaten:**
  * **IdP-Metadaten:**
    * Beschreibung: Ein XML-Dokument, das vom SAML IdP bereitgestellt wird und dessen Dienste, Endpunkte (z.B. SingleSignOnService, SingleLogoutService), unterstützte Bindings (z.B. HTTP-Redirect, HTTP-POST) und X.509-Zertifikate für Signierung und Verschlüsselung beschreibt.
    * Interaktion: `eaf-iam` (SP) konsumiert diese Metadaten, um Vertrauen und Interaktionsparameter mit dem IdP zu konfigurieren. Dies kann über eine URL oder durch Hochladen der Metadatendatei erfolgen.
  * **SP-Metadaten:**
    * Beschreibung: Ein XML-Dokument, das von `eaf-iam` (SP) generiert oder konfiguriert wird und dessen eigene Dienste, ACS URL, SLO URL, Entitäts-ID und X.509-Zertifikate beschreibt.
    * Interaktion: Diese Metadaten werden dem SAML IdP zur Verfügung gestellt, um die Vertrauensstellung von Seiten des IdP zu konfigurieren.
  * **SingleSignOnService (SSO) Endpoint (beim IdP):**
    * Beschreibung: Der Endpunkt des IdP, an den das EAF (SP) SAML AuthnRequests (Authentifizierungsanfragen) sendet oder den Browser des Benutzers zur Authentifizierung weiterleitet.
    * Bindings: Typischerweise HTTP-Redirect oder HTTP-POST.
  * **Assertion Consumer Service (ACS) Endpoint (beim EAF/SP):**
    * Beschreibung: Der Endpunkt des EAF, der SAML Assertions (die Authentifizierungsaussagen und Attribute enthalten) vom IdP über den Browser des Benutzers empfängt (typischerweise über HTTP-POST).
  * **SingleLogoutService (SLO) Endpoint (beim IdP und SP, Optional):**
    * Beschreibung: Endpunkte, die verwendet werden, um Single Logout zu ermöglichen, sodass sich ein Benutzer von allen föderierten Sitzungen abmelden kann.
* **Authentifizierung & Vertrauen:**
  * Vertrauen wird durch den Austausch von SAML-Metadaten zwischen dem EAF (SP) und dem IdP hergestellt.
  * Nachrichten (AuthnRequests vom SP, Assertions vom IdP) werden typischerweise digital unter Verwendung von XML Signature mit X.509-Zertifikaten signiert, deren öffentliche Schlüssel über Metadaten ausgetauscht werden. Assertions können auch mit XML Encryption verschlüsselt werden.
* **Anfrage-/Antwortdatenformate:**
  * **SAML AuthnRequest:** Ein XML-Dokument, das vom EAF (SP) an den IdP gesendet wird, um eine Benutzerauthentifizierung anzufordern.
  * **SAML Assertion:** Ein XML-Dokument, das vom IdP nach erfolgreicher Benutzerauthentifizierung ausgestellt wird. Es enthält Aussagen über das Authentifizierungsereignis, das authentifizierte Subjekt (Benutzer), Attribute und Bedingungen, unter denen die Assertion gültig ist.
* **Schlüsselkonfigurationsparameter (pro SAML IdP / Mandant):**
  * IdP-Metadaten-URL oder XML-Inhalt.
  * SP Entity ID (EAFs eindeutiger Bezeichner für diese SAML-Integration, z.B. `https://eaf-app.example.com/saml/metadata`).
  * SP ACS URL (z.B. `https://eaf-app.example.com/login/saml2/sso/{registrationId}`).
  * Privater Schlüssel und Zertifikat des SP zum Signieren von AuthnRequests und Entschlüsseln von Assertions (falls verschlüsselt).
  * NameID Policy (Format des vom IdP erwarteten Benutzeridentifikators).
  * Attributzuordnungen (um SAML Assertion-Attribute EAF-Benutzerprofilattributen zuzuordnen).
  * Zu verwendende Binding-Typen für Anfragen und Antworten (z.B. HTTP-POST, HTTP-Redirect).
* **Fehlerbehandlung:** SAML-Statuscodes in Antworten zeigen Erfolg oder Misserfolg an. Fehler bei der Validierung von SAML Assertions (Signatur, Aussteller, Zielgruppe, Bedingungen, Subjektbestätigung, Replay-Angriffe) müssen zu einem Authentifizierungsfehler führen. Fehler während des Protokollaustauschs (z.B. ungültige AuthnRequest) sind ebenfalls möglich.
* **Link zu offiziellen Dokumenten:**
  * OASIS SAML 2.0 Standard: [https://www.oasis-open.org/standards#samlv2.0](https://www.google.com/search?q=https://www.oasis-open.org/standards%23samlv2.0) (umfasst Core-, Bindings-, Profiles-Spezifikationen).

### 7.2 Bereitgestellte interne APIs

#### 7.2.1 ACCI EAF Control Plane API (`eaf-controlplane-api`)

* **Zweck:** Diese API stellt RESTful-Endpunkte für Administratoren zur Verwaltung zentraler Aspekte des ACCI EAF-Ökosystems bereit. Sie dient als Backend für die React-basierte Control Plane UI und ermöglicht Operationen im Zusammenhang mit Mandantenmanagement, Benutzeradministration innerhalb von Mandanten, Lizenzkonfiguration, Internationalisierungseinstellungen und Identity-Provider-Konfigurationen.
* **Basis-URL(s):**
  * Vorgeschlagen: `/controlplane/api/v1` (Die tatsächliche Bereitstellungs-URL hängt von der Umgebung ab.)
* **Authentifizierung/Autorisierung:**
  * **Authentifizierung:** Alle Endpunkte sind geschützt. Clients (d.h. die Control Plane UI, die von Administratoren verwendet wird) müssen sich authentifizieren. Dies wird von `eaf-iam` gehandhabt, wahrscheinlich unter Verwendung dedizierter administrativer Benutzerkonten mit Anmeldeinformationen (z.B. Benutzername/Passwort). Der Authentifizierungsmechanismus ist tokenbasiert (z.B. JWTs, die nach erfolgreicher Anmeldung ausgestellt werden).
  * **Autorisierung:** Granulare Berechtigungen werden basierend auf administrativen Rollen (z.B. SuperAdmin, TenantAdmin) durchgesetzt, die innerhalb von `eaf-iam` verwaltet werden. Beispielsweise sind einige Operationen möglicherweise nur für SuperAdmins verfügbar, während andere für deren spezifischen Mandanten an TenantAdmins delegiert werden können.
* **Allgemeine API-Konventionen:**
  * Datenformat: JSON für Anfrage- und Antwort-Bodies.
  * Fehlerbehandlung: Verwendet Standard-HTTP-Statuscodes (z.B. `400 Bad Request`, `401 Unauthorized`, `403 Forbidden`, `404 Not Found`, `500 Internal Server Error`). Fehlerantworten enthalten einen JSON-Body mit Details (z.B. `errorCode`, `message`, `details`).
  * Idempotenz: `PUT`- und `DELETE`-Operationen sollten idempotent sein. `POST`-Operationen zur Erstellung sind dies möglicherweise nicht.
  * Paginierung: Listenendpunkte verwenden Abfrageparameter für die Paginierung (z.B. `page`, `size`).
  * Sortierung & Filterung: Listenendpunkte können Sortierung (z.B. `sort=fieldName,asc`) und Filterung basierend auf spezifischen Feldwerten unterstützen.
* **Schlüsselendpunkte (Illustrative Beispiele, vollständig durch OpenAPI-Definitionen zu spezifizieren):**
  * **Mandantenmanagement:**
    * **`POST /tenants`**: Erstellt einen neuen Mandanten.
      * Request Body Schema: `{ "name": "string", "description"?: "string", "status": "ACTIVE"|"INACTIVE", ... }`
      * Success Response Schema (201 Created): `{ "id": "string", "name": "string", ... }` (Vollständige Mandantendetails)
    * **`GET /tenants`**: Listet alle Mandanten auf (paginiert, filterbar).
      * Success Response Schema (200 OK): `[{ "id": "string", "name": "string", "status": "string", ... }]`
    * **`GET /tenants/{tenantId}`**: Ruft Details eines bestimmten Mandanten ab.
      * Success Response Schema (200 OK): `{ "id": "string", "name": "string", ... }`
    * **`PUT /tenants/{tenantId}`**: Aktualisiert einen Mandanten.
      * Request Body Schema: `{ "name"?: "string", "description"?: "string", "status"?: "ACTIVE"|"INACTIVE", ... }`
      * Success Response Schema (200 OK): `{ "id": "string", "name": "string", ... }`
    * **`DELETE /tenants/{tenantId}`**: Deaktiviert/löscht einen Mandanten (logisches oder hartes Löschen TBD).
      * Success Response Schema (204 No Content oder 200 OK mit Status)
  * **Benutzermanagement (innerhalb eines Mandanten):**
    * **`POST /tenants/{tenantId}/users`**: Erstellt einen neuen lokalen Benutzer innerhalb eines Mandanten.
      * Request Body Schema: `{ "username": "string", "email"?: "string", "firstName"?: "string", "lastName"?: "string", "initialPassword"?: "string", "roles": ["string"], ... }`
      * Success Response Schema (201 Created): `{ "id": "string", "username": "string", "email"?: "string", ... }`
    * **`GET /tenants/{tenantId}/users`**: Listet Benutzer in einem Mandanten auf.
      * Success Response Schema (200 OK): `[{ "id": "string", "username": "string", "email"?: "string", ... }]`
    * **`GET /tenants/{tenantId}/users/{userId}`**: Ruft Details eines bestimmten Benutzers ab.
    * **`PUT /tenants/{tenantId}/users/{userId}`**: Aktualisiert Benutzerdetails (z.B. Status, Rollen, Profilinformationen).
    * **`POST /tenants/{tenantId}/users/{userId}/reset-password`**: Löst ein Passwort-Reset für einen Benutzer aus.
  * **Service Account Management (innerhalb eines Mandanten):**
    * **`POST /tenants/{tenantId}/service-accounts`**: Erstellt ein Service-Konto für einen Mandanten.
      * Request Body Schema: `{ "name": "string", "description"?: "string", "roles": ["string"] }`
      * Success Response Schema (201 Created): `{ "id": "string", "name": "string", "clientId": "string", "clientSecret"?: "string (nur bei Erstellung zurückgegeben)", ... }`
    * **`GET /tenants/{tenantId}/service-accounts`**: Listet Service-Konten für einen Mandanten auf.
    * **`DELETE /tenants/{tenantId}/service-accounts/{accountId}`**: Löscht ein Service-Konto.
  * **Identity Provider Konfiguration (pro Mandant):**
    * **`POST /tenants/{tenantId}/identity-providers`**: Konfiguriert einen externen IdP (LDAP, OIDC, SAML) für einen Mandanten.
      * Request Body Schema: `{ "type": "LDAP"|"OIDC"|"SAML", "name": "string", "configuration": "object (Schema variiert je nach Typ)", "enabled": "boolean" }` (z.B. für LDAP: Host, Port, BaseDN; für OIDC: issuerUrl, clientId, clientSecret)
      * Success Response Schema (201 Created): `{ "id": "string", "type": "string", "name": "string", ... }`
    * **`GET /tenants/{tenantId}/identity-providers`**: Listet konfigurierte IdPs für einen Mandanten auf.
    * **`PUT /tenants/{tenantId}/identity-providers/{idpId}`**: Aktualisiert eine IdP-Konfiguration.
  * **Lizenzmanagement (Potenziell global oder Mandanten zuweisbar):**
    * **`POST /licenses`**: Erstellt eine neue Lizenzdefinition (für ACCI Team).
      * Request Body Schema: `{ "productName": "string", "type": "TIME_LIMITED"|"FEATURE_BASED"|"HARDWARE_BOUND", "validityPeriodDays"?: "number", "features": ["string"], "maxActivations"?: "number", ... }`
    * **`GET /licenses`**: Listet alle Lizenzdefinitionen auf.
    * **`POST /tenants/{tenantId}/assigned-licenses`**: Weist einem Mandanten eine Lizenz zu/verknüpft sie oder aktiviert eine Lizenz für einen Mandanten.
      * Request Body Schema: `{ "licenseId": "string", "activationDetails"?: "object" }`
  * **Internationalisierungs (i18n)-Management (pro Mandant):**
    * **`GET /tenants/{tenantId}/i18n/languages`**: Listet unterstützte/konfigurierte Sprachen für einen Mandanten auf.
    * **`PUT /tenants/{tenantId}/i18n/languages`**: Legt unterstützte Sprachen für einen Mandanten fest.
    * **`GET /tenants/{tenantId}/i18n/translations/{langCode}`**: Ruft alle Übersetzungen für eine bestimmte Sprache für einen Mandanten ab.
      * Success Response Schema (200 OK): `{ "key1": "translation1", "key2": "translation2", ... }`
    * **`PUT /tenants/{tenantId}/i18n/translations/{langCode}`**: Aktualisiert/setzt Übersetzungen für eine Sprache.
      * Request Body Schema: `{ "key1": "new_translation1", ... }`
* **Rate Limits:** Zu definieren, aber eine angemessene Ratenbegrenzung sollte implementiert werden, um die API vor Missbrauch zu schützen.
* **Link zur detaillierten API-Spezifikation:** *(Platzhalter: Eine OpenAPI (Swagger)-Spezifikation wird im Rahmen des Entwicklungsprozesses für diese API generiert und gepflegt. Sie befindet sich in `docs/api/controlplane-v1.yml` oder ist über einen Swagger UI-Endpunkt verfügbar.)*

#### 7.2.2 ACCI EAF Lizenzserver API (`eaf-license-server`)

* **Zweck:** Diese API bietet zentralisierte Online-Dienste zur Aktivierung, Validierung und potenziell Deaktivierung von Lizenzen für Anwendungen, die mit dem ACCI EAF erstellt wurden. Sie ermöglicht Szenarien, in denen eine bereitgestellte Anwendungsinstanz ihren Lizenzstatus bei einem Remote-Server bestätigen muss. Lizenzen werden im Allgemeinen auf "Kunden"-Ebene ausgestellt.
* **Basis-URL(s):**
  * Vorgeschlagen: `/licenseserver/api/v1` (Die tatsächliche Bereitstellungs-URL hängt von der Umgebung ab.)
* **Authentifizierung/Autorisierung:**
  * **Authentifizierung:** Alle Endpunkte sind streng geschützt. Client-Anwendungen (EAF-basierte Anwendungen, die Lizenzprüfungen erfordern) müssen sich bei diesem Server authentifizieren.
    * **Methode 1 (Bevorzugt):** Verwendung von Client-Anmeldeinformationen (z.B. eine eindeutige `clientId` und `clientSecret` oder ein signiertes JWT), die pro EAF-basierter Anwendung ausgestellt werden und effektiv den "Kunden" oder einen spezifischen Bereitstellungskontext identifizieren. Diese Anmeldeinformationen könnten über die `eaf-controlplane-api` verwaltet und sicher an die Client-Anwendungen verteilt werden. Diese Interaktion würde `eaf-iam`-Konzepte nutzen.
    * **Methode 2 (Alternative):** Ein vorab geteiltes Geheimnis oder ein API-Schlüssel, der spezifisch für die bereitgestellte Anwendungsinstanz ist, kombiniert mit anderen identifizierenden Informationen (z.B. Produktcode, Instanz-ID).
  * **Autorisierung:** Operationen können basierend auf der Identität der authentifizierten Client-Anwendung (die den Kunden repräsentiert) und dem spezifischen Lizenzschlüssel oder der Aktivierungs-ID, auf die Bezug genommen wird, autorisiert werden. Der Server überprüft, ob die anfragende Anwendung/der Kunde berechtigt ist, die Operation für die angegebene Lizenz durchzuführen.
* **Allgemeine API-Konventionen:**
  * Datenformat: JSON für Anfrage- und Antwort-Bodies.
  * Fehlerbehandlung: Verwendet Standard-HTTP-Statuscodes. Fehlerantworten enthalten einen JSON-Body mit Details (z.B. `errorCode`, `message`, `validationErrors`).
  * Idempotenz: Schlüsseloperationen sollten gegebenenfalls unter Berücksichtigung der Idempotenz entworfen werden (z.B. sollte die erneute Validierung einer bereits aktiven Lizenz ihren aktuellen Status ohne Nebeneffekte zurückgeben).
* **Schlüsselendpunkte (Illustrative Beispiele, vollständig durch OpenAPI-Definitionen zu spezifizieren):**
  * **Lizenzaktivierung:**
    * **`POST /activations`**: Versucht, eine Lizenz für eine Produktinstanz (Kundenebene) zu aktivieren.
      * Request Body Schema:
                ```json
                {
                  "productCode": "string",
                  "licenseKey": "string",
                  "hardwareIds": ["string"],
                  "instanceId": "string"
                }
                ```
      * Success Response Schema (200 OK oder 201 Created):
                ```json
                {
                  "activationId": "string",
                  "status": "ACTIVE",
                  "productName": "string",
                  "licenseType": "TIME_LIMITED" | "FEATURE_BASED" | "PERPETUAL",
                  "expiresAt": "iso-datetime | null",
                  "activatedAt": "iso-datetime",
                  "features": ["string"],
                  "validationIntervalSeconds": "number | null"
                }
                ```
      * Error Response Schema (z.B. 400, 403, 404):
                ```json
                {
                  "errorCode": "string",
                  "message": "string"
                }
                ```
  * **Lizenzvalidierung:**
    * **`POST /validations`**: Validiert den aktuellen Status einer aktivierten Lizenz. Dies wird periodisch von der Client-Anwendung aufgerufen.
      * Request Body Schema:
                ```json
                {
                  "activationId": "string",
                  "productCode": "string",
                  "hardwareIds": ["string"],
                  "instanceId": "string"
                }
                ```
      * Success Response Schema (200 OK):
                ```json
                {
                  "activationId": "string",
                  "status": "ACTIVE" | "EXPIRED" | "INVALID" | "REVOKED",
                  "productName": "string",
                  "licenseType": "string",
                  "expiresAt": "iso-datetime | null",
                  "features": ["string"],
                  "validationIntervalSeconds": "number | null",
                  "message": "string | null"
                }
                ```
      * Error Response Schema (z.B. 400, 403, 404):
                ```json
                {
                  "errorCode": "string",
                  "message": "string"
                }
                ```
  * **Lizenzdeaktivierung (Optional):**
    * **`DELETE /activations/{activationId}`**: Deaktiviert eine zuvor aktivierte Lizenz für eine bestimmte Produktinstanz.
      * Request Parameters: `activationId` (Pfadparameter).
      * Request Body Schema (Optional, kann zusätzlichen Nachweis/Kontext erfordern):
                ```json
                {
                  "productCode": "string",
                  "instanceId": "string",
                  "reason": "string | null"
                }
                ```
      * Success Response Schema (200 OK oder 204 No Content):
                ```json
                {
                  "status": "DEACTIVATED",
                  "deactivatedAt": "iso-datetime"
                }
                ```
      * Error Response Schema (z.B. 400, 403, 404):
                ```json
                {
                  "errorCode": "string",
                  "message": "string"
                }
                ```
* **Rate Limits:** Zu definieren. Aufrufe an `/validations` können von vielen bereitgestellten Instanzen häufig erfolgen, daher sind geeignete Strategien (z.B. pro `activationId` oder pro Client-IP/Anwendungs-ID, die den Kunden repräsentiert) erforderlich.
* **Link zur detaillierten API-Spezifikation:** *(Platzhalter: Eine OpenAPI (Swagger)-Spezifikation wird im Rahmen des Entwicklungsprozesses für diese API generiert und gepflegt. Sie befindet sich in `docs/api/licenseserver-v1.yml` oder ist über einen Swagger UI-Endpunkt verfügbar.)*

## 8\\. Datenmodelle

Dieser Abschnitt definiert die Hauptdatenstrukturen, die innerhalb des ACCI EAF verwendet werden. Dies umfasst Kerndomänenobjekte, Überlegungen zu API-Payloads und Datenbankschemastrukturen sowohl für den Event Store als auch für Read Models. Angesichts der Verwendung von Kotlin werden Datenstrukturen beispielhaft anhand von Kotlin Data Classes oder Interfaces dargestellt.

### 8.1 Kernanwendungsentitäten / Domänenobjekte

Dies sind die zentralen Konzepte, die das ACCI EAF und darauf aufbauende Anwendungen verwalten werden. Im Kontext von Event Sourcing (unter Verwendung des Axon Frameworks) werden viele
davon als Aggregate repräsentiert, deren Zustand aus einer Sequenz von Ereignissen abgeleitet wird. Die folgenden Definitionen repräsentieren den typischen Zustand dieser Aggregate oder Schlüsselentitäten.

#### 8.1.1 Mandant (Tenant)

* **Beschreibung:** Repräsentiert einen Kunden oder eine distinkte Organisationseinheit, die EAF-basierte Anwendungen nutzt. Mandanten bieten einen Geltungsbereich für Benutzerverwaltung, Lizenzierung und andere Konfigurationen. Verwaltet von `eaf-multitenancy` und `eaf-controlplane-api`.
* **Schema / Data Class Definition (Kotlin):**

    ```kotlin
    data class Tenant(
        val id: String, // Aggregat-Identifikator (UUID)
        val name: String, // Name des Mandanten
        val description: String? = null,
        val status: TenantStatus = TenantStatus.ACTIVE,
        val createdAt: java.time.Instant,
        val updatedAt: java.time.Instant,
        // Potenziell weitere mandantenspezifische Konfigurationsdetails
        val identityProviderConfigurations: List<IdentityProviderConfigSummary> = emptyList(),
        val assignedLicenseInfo: AssignedLicenseSummary? = null
    )

    enum class TenantStatus {
        ACTIVE,
        INACTIVE,
        SUSPENDED
    }

    data class IdentityProviderConfigSummary(
        val idpId: String,
        val name: String,
        val type: String // z.B. "OIDC", "SAML", "LDAP"
    )

    data class AssignedLicenseSummary(
        val licenseId: String,
        val productName: String,
        val expiresAt: java.time.Instant?
    )
    ```

* **Validierungsregeln:** `id` ist obligatorisch und eindeutig. `name` ist obligatorisch. `status` muss einer der definierten Enum-Werte sein.

#### 8.1.2 Benutzer (IAM-Benutzer)

* **Beschreibung:** Repräsentiert einen einzelnen Endbenutzer oder Administrator im Kontext eines Mandanten. Benutzer haben Anmeldeinformationen für die Authentifizierung und ihnen sind Rollen für die Autorisierung zugewiesen. Verwaltet von `eaf-iam` und `eaf-controlplane-api`.
* **Schema / Data Class Definition (Kotlin):**

    ```kotlin
    data class User(
        val id: String, // Aggregat-Identifikator (UUID)
        val tenantId: String, // Identifikator des Mandanten, zu dem dieser Benutzer gehört
        val username: String, // Eindeutiger Benutzername innerhalb des Mandanten
        val email: String? = null, // Optionale E-Mail-Adresse des Benutzers (nicht notwendigerweise eindeutig)
        var firstName: String? = null,
        var lastName: String? = null,
        var displayName: String? = null,
        val status: UserStatus = UserStatus.ACTIVE,
        var passwordHash: String? = null, // Für lokale Benutzer; gesalzen und gehasht
        val externalIdpSubject: String? = null, // Subjekt vom externen IdP, falls föderiert
        val identityProviderAlias: String? = null, // Alias des für die Föderation verwendeten IdP
        val roles: Set<String> = emptySet(), // Satz von Rollenidentifikatoren, die dem Benutzer zugewiesen sind
        val createdAt: java.time.Instant,
        var updatedAt: java.time.Instant,
        var lastLoginAt: java.time.Instant? = null
    )

    enum class UserStatus {
        PENDING_VERIFICATION, // z.B. E-Mail-Verifizierung erforderlich, falls E-Mail angegeben
        ACTIVE,
        INACTIVE, // Vom Administrator deaktiviert
        LOCKED // Aufgrund fehlgeschlagener Anmeldeversuche gesperrt etc.
    }
    ```

* **Validierungsregeln:** `id`, `tenantId`, `username` sind obligatorisch. `username` muss innerhalb des Mandanten eindeutig sein. `passwordHash` ist für lokale Benutzer erforderlich, die keinen externen IdP verwenden (es sei denn, andere primäre Authentifizierungsmethoden sind konfiguriert). `roles` sollten auf gültige Rollenentitäten verweisen.

#### 8.1.3 ServiceAccount (IAM Service Account)

* **Beschreibung:** Repräsentiert einen nicht-menschlichen Akteur (z.B. eine Anwendung, ein Dienst), der sich bei EAF-geschützten Ressourcen oder APIs authentifizieren und autorisieren muss, typischerweise im Kontext eines bestimmten Mandanten. Verwaltet von `eaf-iam` und `eaf-controlplane-api`.
* **Schema / Data Class Definition (Kotlin):**

    ```kotlin
    data class ServiceAccount(
        val id: String, // Aggregat-Identifikator (UUID)
        val tenantId: String,
        val name: String, // Ein beschreibender Name für das Service-Konto
        val description: String? = null,
        val clientId: String, // Eindeutiger Client-Identifikator
        var clientSecretHash: String? = null, // Gesalzenes und gehashtes Client-Secret (nur bei Verwendung von Client-Secret-Authentifizierung)
                                            // Alternativ öffentliche Schlüssel für JWT-Client-Assertion
        val status: ServiceAccountStatus = ServiceAccountStatus.ACTIVE,
        val roles: Set<String> = emptySet(), // Diesem Service-Konto zugewiesene Rollen
        val createdAt: java.time.Instant,
        var updatedAt: java.time.Instant,
        var secretExpiresAt: java.time.Instant? = null
    )

    enum class ServiceAccountStatus {
        ACTIVE,
        INACTIVE
    }
    ```

* **Validierungsregeln:** `id`, `tenantId`, `name`, `clientId` sind obligatorisch und eindeutig. `clientSecretHash` wird bei Erstellung/Rotation gesetzt.

#### 8.1.4 AktivierteLizenz (ActivatedLicense)

* **Beschreibung:** Repräsentiert eine Instanz einer Lizenz, die für die Produktbereitstellung eines Kunden aktiviert wurde. Dies ist die primäre Entität, die vom `eaf-license-server` verwaltet und mit Lizenzinformationen in `eaf-licensing` verknüpft ist.
* **Schema / Data Class Definition (Kotlin):**

    ```kotlin
    data class ActivatedLicense(
        val activationId: String, // Aggregat-Identifikator (UUID), eindeutig für diese Aktivierung
        val licenseKey: String, // Der Hauptlizenzschlüssel, der aktiviert wurde
        val customerId: String, // Identifikator für den Kunden, dem diese Lizenz gehört (abgeleitet vom Lizenzschlüssel oder Authentifizierungskontext)
        val productCode: String,
        val instanceId: String, // Identifikator für die spezifische Produktinstanz
        var status: LicenseActivationStatus = LicenseActivationStatus.PENDING,
        val hardwareIds: List<String> = emptyList(),
        val activatedAt: java.time.Instant,
        var lastValidatedAt: java.time.Instant? = null,
        var expiresAt: java.time.Instant? = null, // Falls die Lizenz zeitlich begrenzt ist
        val features: List<String> = emptyList(), // Durch diese Lizenzaktivierung aktivierte Funktionen
        val deactivationReason: String? = null,
        var updatedAt: java.time.Instant
    )

    enum class LicenseActivationStatus {
        PENDING, // Anfangszustand, z.B. wartet auf erste Validierung
        ACTIVE,
        EXPIRED,
        REVOKED, // Manuell von einem Administrator widerrufen
        DEACTIVATED, // Ordnungsgemäß von der Client-Anwendung deaktiviert
        INVALID_HARDWARE // Aktivierung ist aufgrund von Hardware-Nichtübereinstimmung ungültig
    }
    ```

* **Validierungsregeln:** `activationId`, `licenseKey`, `customerId`, `productCode`, `instanceId` sind obligatorisch. `status` spiegelt den Lebenszyklus wider.

*{Weitere Kernentitäten wie `Role` (Rolle), `Permission` (Berechtigung), `LicenseDefinition` (Lizenzdefinition), `IdentityProviderConfig` (Identitätsanbieterkonfiguration), `I18NTranslationBundle` (I18N-Übersetzungsbündel), `AuditEvent` (Audit-Ereignis), `PluginDescriptor` (Plugin-Beschreiber) usw. würden hier auf ähnliche Weise definiert.}*

### 8.2 API-Payload-Schemata (falls abweichend)

Als allgemeines Prinzip leiten sich die Anfrage- und Antwort-Payload-Schemata für die HTTP-APIs (detailliert im Abschnitt "API-Referenz" für `eaf-controlplane-api` und `eaf-license-server`) direkt von den oben definierten "Kernanwendungsentitäten / Domänenobjekten" ab oder sind spezifische Teilmengen/DTOs (Data Transfer Objects), die auf eine bestimmte API-Operation zugeschnitten sind.

Beispielsweise würde eine `POST`-Anfrage zum Erstellen eines `Tenant` wahrscheinlich eine Payload annehmen, die der `Tenant`-Datenklasse ähnelt, jedoch ohne systemgenerierte Felder wie `id`, `createdAt` oder `updatedAt`. Die Antwort würde dann typischerweise das vollständig ausgefüllte `Tenant`-Objekt enthalten.

Spezifische Anfrage- und Antwortschemata, einschließlich genauer Feldnamen, Datentypen und Validierungsregeln (z.B. Pflichtfelder, Formatbeschränkungen), wurden im Abschnitt "API-Referenz" illustrativ dargestellt. Die endgültige und detaillierteste Spezifikation für alle API-Payloads wird in den OpenAPI (Swagger)-Dokumenten gepflegt, die parallel zur Entwicklung der jeweiligen API-Module generiert werden (z.B. `docs/api/controlplane-v1.yml`, `docs/api/licenseserver-v1.yml`).

Wiederverwendbare, komplexe Payload-Strukturen, die sich von Kernentitäten unterscheiden und über mehrere API-Endpunkte hinweg verwendet werden (z.B. standardisierte Fehlerantwortformate, Paginierungs-Wrapper), werden ebenfalls in diesen OpenAPI-Spezifikationen definiert. Beispielsweise könnte eine übliche Fehlerantwort-Payload wie folgt aussehen:

```json
{
  "timestamp": "iso-datetime",
  "status": "integer (HTTP status code)",
  "error": "string (HTTP error phrase)",
  "message": "string (developer-friendly error message)",
  "path": "string (request path)",
  "details": [
    {
      "field": "string (field causing the error, if applicable)",
      "issue": "string (description of the issue)"
    }
  ]
}
```

### 8.3 Datenbankschemata (falls zutreffend)

Das ACCI EAF verwendet PostgreSQL als primäres Datenbanksystem. Angesichts der Übernahme von CQRS und Event Sourcing mit dem Axon Framework dient die Datenbank mehreren Zwecken:

1. **Event Store:** Persistierung aller von den Aggregaten generierten Domänenereignisse.
2. **Read Models (Query Models):** Speicherung denormalisierter Datenprojektionen, die für Abfragen und UI-Anzeige optimiert sind.
3. **Zustandsdaten:** Speicherung von Konfigurationsdaten oder anderen zustandsbehafteten Informationen für EAF-Module, die möglicherweise nicht ereignisgesteuert sind.

#### 8.3.1 Event Store Schema

Das ACCI EAF verwendet die JDBC-Implementierung des Axon Frameworks für seinen Event Store, mit PostgreSQL als zugrundeliegender Datenbank. Das Axon Framework bietet ein standardmäßiges, vordefiniertes Schema zum Speichern von Domänenereignissen und Snapshots. Zu den Schlüsseltabellen gehören:

* **`DOMAINEVENTS` (oder `domain_event_entry` in neueren Axon-Versionen):** Speichert die serialisierten Domänenereignisse, einschließlich des Aggregat-Identifikators, der Sequenznummer, des Ereignistyps, der Payload und der Metadaten.
* **`SNAPSHOTEVENTS` (oder `snapshot_event_entry`):** Speichert Snapshots von Aggregaten, um die Ladezeiten für Aggregate mit langen Ereignishistorien zu optimieren.
* Andere Axon-spezifische Tabellen zur Verfolgung von Tokens für Ereignisprozessoren (`token_entry`), Saga-Zustand (`saga_entry`) usw. können ebenfalls Teil dieses Schemas sein, abhängig von den verwendeten Axon-Funktionen.

Die genaue DDL für diese Tabellen wird vom Axon Framework bereitgestellt und während der Ersteinrichtung oder über Datenbankmigrationstools (z.B. Flyway, Liquibase), die für das EAF konfiguriert sind, angewendet. Details finden Sie in der offiziellen Axon Framework-Dokumentation bezüglich JDBC Event Storage.

#### 8.3.2 Read Model Schemata (Beispiele)

Read Models sind speziell entworfene relationale Tabellen in PostgreSQL, die optimierte Abfragefunktionen für die `eaf-controlplane-api`, EAF-basierte Anwendungen und alle anderen Abfragekonsumenten bereitstellen. Diese Tabellen werden von Ereignis-Listenern/Prozessoren gefüllt, die die Domänenereignisse aus dem Event Store abonnieren.

Im Folgenden finden Sie einige illustrative Beispiele für DDL für Read Model-Tabellen:

* **`read_tenants` Tabelle:** Zum Abfragen von Mandanteninformationen.

    ```sql
    CREATE TABLE read_tenants (
        tenant_id VARCHAR(36) PRIMARY KEY,
        name VARCHAR(255) NOT NULL,
        description TEXT,
        status VARCHAR(50) NOT NULL, -- z.B. 'ACTIVE', 'INACTIVE'
        created_at TIMESTAMP WITH TIME ZONE NOT NULL,
        updated_at TIMESTAMP WITH TIME ZONE NOT NULL
        -- Zusätzliche denormalisierte Felder für Abfragen können hier hinzugefügt werden
    );
    CREATE INDEX idx_read_tenants_name ON read_tenants(name);
    CREATE INDEX idx_read_tenants_status ON read_tenants(status);
    ```

* **`read_users` Tabelle:** Zum Abfragen von Benutzerinformationen.

    ```sql
    CREATE TABLE read_users (
        user_id VARCHAR(36) PRIMARY KEY,
        tenant_id VARCHAR(36) NOT NULL REFERENCES read_tenants(tenant_id),
        username VARCHAR(255) NOT NULL,
        email VARCHAR(255), -- Optional, gemäß Benutzerentität
        first_name VARCHAR(255),
        last_name VARCHAR(255),
        display_name VARCHAR(255),
        status VARCHAR(50) NOT NULL, -- z.B. 'ACTIVE', 'LOCKED'
        is_external_auth BOOLEAN DEFAULT FALSE, -- True, falls über externen IdP föderiert
        created_at TIMESTAMP WITH TIME ZONE NOT NULL,
        updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
        last_login_at TIMESTAMP WITH TIME ZONE
        -- Rollen könnten in einer separaten Join-Tabelle (read_user_roles) oder als Array/JSON gespeichert werden, falls einfach
    );
    CREATE UNIQUE INDEX idx_read_users_tenant_username ON read_users(tenant_id, username);
    CREATE INDEX idx_read_users_email ON read_users(email);
    CREATE INDEX idx_read_users_status ON read_users(status);
    ```

* **`read_activated_licenses` Tabelle:** Zum Abfragen von Informationen zu aktivierten Lizenzen.

    ```sql
    CREATE TABLE read_activated_licenses (
        activation_id VARCHAR(36) PRIMARY KEY,
        license_key VARCHAR(255) NOT NULL,
        customer_id VARCHAR(255) NOT NULL, -- Identifikator des Kunden
        product_code VARCHAR(100) NOT NULL,
        instance_id VARCHAR(255) NOT NULL,
        status VARCHAR(50) NOT NULL, -- z.B. 'ACTIVE', 'EXPIRED', 'REVOKED'
        hardware_ids TEXT, -- Komma-separierte oder JSON-Array von Hardware-IDs
        activated_at TIMESTAMP WITH TIME ZONE NOT NULL,
        last_validated_at TIMESTAMP WITH TIME ZONE,
        expires_at TIMESTAMP WITH TIME ZONE,
        features TEXT, -- Komma-separierte oder JSON-Array von aktivierten Funktionen
        updated_at TIMESTAMP WITH TIME ZONE NOT NULL
    );
    CREATE INDEX idx_read_activated_licenses_license_key ON read_activated_licenses(license_key);
    CREATE INDEX idx_read_activated_licenses_customer_id ON read_activated_licenses(customer_id);
    CREATE INDEX idx_read_activated_licenses_product_code ON read_activated_licenses(product_code);
    ```

*(Weitere Read Model-Tabellen für Service-Konten, IdP-Konfigurationen, Rollen, Lizenzdefinitionen usw. werden bei Bedarf analog definiert.)*

#### 8.3.3 Konfigurations- / Zustandsdaten-Schemata (Beispiele)

Einige EAF-Module erfordern möglicherweise die Speicherung von Konfigurations- oder Zustandsdaten, die nicht ereignisgesteuert sind, sondern eher wie traditionelle relationale Daten verwaltet werden.

* **`iam_roles` Tabelle:** Zur Definition der im System verfügbaren Rollen (verwendet von `eaf-iam`).

    ```sql
    CREATE TABLE iam_roles (
        role_id VARCHAR(100) PRIMARY KEY, -- z.B. 'SUPER_ADMIN', 'TENANT_ADMIN', 'USER'
        description TEXT,
        is_system_role BOOLEAN DEFAULT FALSE -- Gibt an, ob es sich um eine Kern-EAF-Rolle handelt
    );
    -- Mit Rollen verbundene Berechtigungen könnten in einer separaten Tabelle iam_role_permissions
    -- oder im Code definiert sein, falls statisch für Systemrollen.
    ```

* **`licensing_definitions` Tabelle:** Zum Speichern von Hauptlizenzdefinitionen (verwendet von `eaf-licensing`).

    ```sql
    CREATE TABLE licensing_definitions (
        license_def_id VARCHAR(36) PRIMARY KEY,
        product_name VARCHAR(255) NOT NULL,
        license_type VARCHAR(50) NOT NULL, -- z.B. 'TIME_LIMITED', 'PERPETUAL', 'FEATURE_BASED'
        default_duration_days INTEGER, -- Falls zeitlich begrenzt
        default_features TEXT, -- Komma-separiert oder JSON-Array
        max_activations INTEGER,
        notes TEXT,
        created_at TIMESTAMP WITH TIME ZONE NOT NULL,
        updated_at TIMESTAMP WITH TIME ZONE NOT NULL
    );
    ```

Datenbankschemamigrationen für Read Models und Konfigurations-/Zustandstabellen werden mit **Liquibase** (Version `4.31.1`) verwaltet, das in den Build- und Bereitstellungsprozess integriert ist.

## 9\\. Kernworkflows / Sequenzdiagramme

Dieser Abschnitt veranschaulicht wichtige oder komplexe Arbeitsabläufe mithilfe von Mermaid-Sequenzdiagrammen. Diese Diagramme helfen, die Interaktionen zwischen verschiedenen Komponenten des ACCI EAF und externen Systemen zu verstehen.

### 9.1 Benutzerauthentifizierung über externen OIDC-Provider

Dieses Sequenzdiagramm zeigt den typischen "Authorization Code Flow", wenn sich ein Benutzer bei einer EAF-basierten Anwendung über einen externen OpenID Connect (OIDC) Provider authentifiziert. Das `eaf-iam`-Modul innerhalb der EAF-basierten Anwendung handhabt die Logik des OIDC Relying Party (RP).

```mermaid
sequenceDiagram
    actor UserBrowser as Benutzer (Browser)
    participant EAFApp as EAF-basierte Anwendung
    participant EAFiam as eaf-iam Modul
    participant OIDCProvider as Externer OIDC Provider

    UserBrowser->>+EAFApp: 1. Zugriff auf geschützte Ressource
    EAFApp->>EAFiam: 2. Authentifizierung prüfen
    alt Benutzer nicht authentifiziert
        EAFiam->>EAFApp: 3. OIDC-Authentifizierung initiieren
        EAFApp->>EAFiam: 4. OIDC AuthN Request vorbereiten (AuthN URL erstellen)
        EAFiam-->>EAFApp: AuthN URL mit client_id, redirect_uri, scope, state, nonce
        EAFApp-->>-UserBrowser: 5. Weiterleitung zum OIDC Provider (Authorization Endpoint)
    end

    UserBrowser->>+OIDCProvider: 6. Authentifiziert sich beim OIDC Provider (z.B. Eingabe von Anmeldedaten)
    OIDCProvider-->>-UserBrowser: 7. Zurückleitung zur EAF App (Redirect URI mit Authorization Code & state)

    UserBrowser->>+EAFApp: 8. Anfrage an EAF App Redirect URI (mit Authorization Code)
    EAFApp->>EAFiam: 9. OIDC-Callback verarbeiten (Authorization Code, state übergeben)
    EAFiam->>+OIDCProvider: 10. Authorization Code gegen Tokens tauschen (Token Endpoint)<br/>(sendet code, client_id, client_secret, redirect_uri)
    OIDCProvider-->>-EAFiam: 11. ID Token, Access Token, (Refresh Token)

    EAFiam->>EAFiam: 12. ID Token validieren (Signatur, Aussteller, Zielgruppe, Ablauf, Nonce)
    alt ID Token gültig
        opt UserInfo abrufen
            EAFiam->>+OIDCProvider: 13. UserInfo anfordern (UserInfo Endpoint mit Access Token)
            OIDCProvider-->>-EAFiam: 14. UserInfo Antwort (Claims)
        end
        EAFiam->>EAFApp: 15. Authentifizierung erfolgreich, Sitzung herstellen<br/>(Benutzerkontext mit Claims erstellt/aktualisiert)
        EAFApp-->>-UserBrowser: 16. Geschützte Ressource bereitstellen
    else ID Token ungültig
        EAFiam->>EAFApp: Authentifizierung fehlgeschlagen
        EAFApp-->>-UserBrowser: Fehlerseite anzeigen / Zurück zum Login
    end
```

**Beschreibung des Ablaufs:**

1. **Zugriff auf geschützte Ressource:** Der Benutzer versucht, auf eine geschützte Ressource in einer auf ACCI EAF basierenden Anwendung zuzugreifen.
2. **Authentifizierung prüfen:** Die EAF-basierte Anwendung prüft unter Verwendung des `eaf-iam`-Moduls, ob der Benutzer bereits authentifiziert ist.
3. **OIDC-Authentifizierung initiieren:** Falls der Benutzer nicht authentifiziert ist, stellt `eaf-iam` fest, dass eine OIDC-Authentifizierung initiiert werden soll (basierend auf Mandanten- oder Anwendungskonfiguration).
4. **OIDC AuthN Request vorbereiten:** `eaf-iam` konstruiert die URL für den Authorization Endpoint des OIDC Providers, einschließlich Parametern wie `client_id`, `redirect_uri`, angeforderten `scope`s (z.B. `openid profile email`), einem `state`-Parameter (für CSRF-Schutz) und einer `nonce` (für Replay-Schutz).
5. **Weiterleitung zum OIDC Provider:** Die EAF-basierte Anwendung leitet den Browser des Benutzers zum OIDC Provider weiter.
6. **Benutzer authentifiziert sich beim OIDC Provider:** Der Benutzer interagiert mit dem OIDC Provider zur Authentifizierung (z.B. Eingabe von Benutzername/Passwort, Durchführung von MFA).
7. **Zurückleitung zur EAF App:** Nach erfolgreicher Authentifizierung leitet der OIDC Provider den Browser des Benutzers zurück zur bei der EAF-basierten Anwendung registrierten `redirect_uri`. Diese Weiterleitung enthält einen `authorization_code` und den ursprünglichen `state`-Parameter.
8. **Anfrage an Redirect URI:** Der Browser des Benutzers sendet eine Anfrage an die Redirect URI der EAF-basierten Anwendung und übermittelt den `authorization_code`.
9. **OIDC-Callback verarbeiten:** Die EAF-basierte Anwendung übergibt den `authorization_code` und `state` an `eaf-iam`. `eaf-iam` validiert zuerst den `state`-Parameter.
10. **Authorization Code gegen Tokens tauschen:** `eaf-iam` sendet eine direkte (Server-zu-Server) Anfrage an den Token Endpoint des OIDC Providers und tauscht den `authorization_code` gegen ein ID Token, ein Access Token und optional ein Refresh Token. Diese Anfrage wird unter Verwendung der `client_id` und des `client_secret` der EAF-Anwendung authentifiziert.
11. **ID Token, Access Token zurückgegeben:** Der OIDC Provider gibt die angeforderten Tokens zurück.
12. **ID Token validieren:** `eaf-iam` validiert das ID Token sorgfältig:
    * Signaturprüfung unter Verwendung der öffentlichen Schlüssel des OIDC Providers (erhalten über JWKS URI).
    * Validierung von Claims wie `iss` (Aussteller), `aud` (Zielgruppe, muss mit `client_id` übereinstimmen), `exp` (Ablaufzeit), `iat` (Ausstellungszeit) und `nonce` (muss mit dem in Schritt 4 gesendeten übereinstimmen).
13. **(Optional) UserInfo abrufen:** Falls erforderlich und ein Access Token empfangen wurde, kann `eaf-iam` das Access Token verwenden, um zusätzliche Benutzer-Claims vom UserInfo Endpoint des OIDC Providers anzufordern.
14. **UserInfo Antwort:** Der OIDC Provider gibt die zusätzlichen Benutzer-Claims zurück.
15. **Authentifizierung erfolgreich, Sitzung herstellen:** Wenn alle Validierungen erfolgreich sind, betrachtet `eaf-iam` den Benutzer als authentifiziert. Es erstellt einen lokalen Sicherheitskontext/eine Sitzung für den Benutzer innerhalb der EAF-basierten Anwendung. Benutzerinformationen (aus ID Token und UserInfo Endpoint Claims) können verwendet werden, um eine lokale Repräsentation des Benutzers im `eaf-iam` Benutzerspeicher für den Mandanten anzulegen oder zu aktualisieren.
16. **Geschützte Ressource bereitstellen:** Die EAF-basierte Anwendung stellt nun die ursprünglich angeforderte geschützte Ressource dem authentifizierten Benutzer bereit.

### 9.2 Befehlsverarbeitungs- und Event-Sourcing-Fluss

Dieses Sequenzdiagramm veranschaulicht den typischen Fluss der Verarbeitung eines Befehls, der Generierung von Domänenereignissen, der Persistierung dieser Ereignisse (Event Sourcing) und der Aktualisierung von Read Models (CQRS) innerhalb einer Anwendung, die auf dem ACCI EAF basiert und das Axon Framework verwendet.

```mermaid
sequenceDiagram
    participant Client as Client (z.B. UI, API-Konsument)
    participant AppService as EAF App Service/API Endpunkt
    participant CmdGateway as Axon Command Gateway
    participant Aggregate as Ziel-Aggregat (z.B. TenantAggregat)
    participant EvtStore as Axon Event Store (PostgreSQL)
    participant EvtBus as Axon Event Bus
    participant Projector as Event Handler / Projektor
    participant ReadDB as Read Model Datenbank (PostgreSQL)

    Client->>+AppService: 1. Befehl senden (z.B. CreateTenantCommand mit Daten)
    AppService->>CmdGateway: 2. Befehlsobjekt erstellen & absenden
    CmdGateway->>+Aggregate: 3. Befehl an @CommandHandler Methode weiterleiten
    Aggregate->>Aggregate: 4. Befehl validieren (gegen aktuellen Aggregatzustand)
    alt Befehl gültig
        Aggregate->>Aggregate: 5. Domänenereignis(se) anwenden (z.B. TenantCreatedEvent)<br/>(mittels AggregateLifecycle.apply())
        Note over Aggregate: Interner @EventSourcingHandler aktualisiert Aggregatzustand
        Aggregate-->>CmdGateway: (Befehlsverarbeitung erfolgreich)
        CmdGateway-->>AppService: 6. (Optional) Befehlsergebnis (z.B. aggregateId)
        AppService-->>-Client: 7. (Optional) HTTP Antwort (z.B. 201 Created mit ID)

        Aggregate->>EvtBus: 8. Ereignis(se) im Event Bus veröffentlicht (durch Axon)
        EvtBus->>EvtStore: 9. Ereignis(se) persistieren (durch Axon)
        EvtStore-->>EvtBus: (Persistierung erfolgreich)

        EvtBus->>+Projector: 10. Ereignis(se) an @EventHandler Methode zugestellt
        Projector->>+ReadDB: 11. Read Model(s) aktualisieren (z.B. INSERT in read_tenants)
        ReadDB-->>-Projector: (Aktualisierung erfolgreich)
        Projector-->>-EvtBus: (Ereignisverarbeitung abgeschlossen)
    else Befehl ungültig
        Aggregate-->>CmdGateway: Ausnahme (z.B. Validierung fehlgeschlagen)
        CmdGateway-->>AppService: Ausnahme weiterleiten
        AppService-->>-Client: HTTP Fehlerantwort (z.B. 400 Bad Request)
    end
```

**Beschreibung des Ablaufs:**

1. **Befehl senden:** Ein Client (z.B. ein Benutzer, der mit der Control Plane UI interagiert, ein externes System, das eine API aufruft, oder ein anderer Dienst innerhalb des EAF) initiiert eine Aktion, indem er einen Befehl sendet. Ein Befehl ist eine Absicht, den Zustand eines Aggregats zu ändern (z.B. `CreateTenantCommand`, `UpdateUserEmailCommand`). Er enthält typischerweise die für die Operation notwendigen Daten.
2. **Befehlsobjekt erstellen & absenden:** Der Anwendungsdienst oder API-Endpunkt in der EAF-basierten Anwendung empfängt die Befehlsdaten, konstruiert ein formales Befehlsobjekt (ein DTO, das den Befehl repräsentiert) und sendet es über Axons `CommandGateway` ab.
3. **Befehl an Command Handler weiterleiten:** Der `CommandGateway` leitet das Befehlsobjekt an die entsprechende `@CommandHandler`-Methode innerhalb des designierten DDD-Aggregats (z.B. `TenantAggregate`) weiter. Das Axon Framework stellt sicher, dass die Zielaggregatinstanz aus dem Event Store geladen (aus ihren vergangenen Ereignissen rehydriert) oder neu erstellt wird, falls sie noch nicht existiert (z.B. für Erstellungsbefehle).
4. **Befehl validieren:** Die `@CommandHandler`-Methode innerhalb des Aggregats enthält die Geschäftslogik zur Validierung des Befehls gegen den aktuellen Zustand des Aggregats und etwaige Geschäftsregeln.
5. **Domänenereignis(se) anwenden:** Wenn der Befehl gültig ist, ändert der `@CommandHandler` den Zustand des Aggregats nicht direkt. Stattdessen trifft er eine Entscheidung und *wendet* ein oder mehrere Domänenereignisse an, die das Ergebnis des Befehls repräsentieren (z.B. `TenantCreatedEvent`, `UserEmailUpdatedEvent`). Dies geschieht typischerweise mit `AggregateLifecycle.apply(eventObject)` in Axon.
    * Intern wird beim Anwenden eines Ereignisses eine entsprechende `@EventSourcingHandler`-Methode innerhalb desselben Aggregats mit dem Ereignis aufgerufen. Dieser Handler ist dafür verantwortlich, den In-Memory-Zustand des Aggregats basierend auf dem Inhalt des Ereignisses zu aktualisieren.
6. **(Optional) Befehlsergebnis:** Nachdem der Command Handler den Befehl erfolgreich verarbeitet hat (d.h. Ereignisse angewendet wurden), kann er ein Ergebnis zurückgeben (z.B. die ID des neu erstellten Aggregats oder void, wenn kein direktes Ergebnis benötigt wird). Dieses Ergebnis wird über den `CommandGateway` zurückgegeben.
7. **(Optional) HTTP Antwort:** Der Anwendungsdienst/API-Endpunkt kann dann eine entsprechende HTTP-Antwort an den Client zurückgeben (z.B. HTTP 201 Created mit der neuen Ressourcen-ID oder HTTP 200 OK).
8. **Ereignis(se) im Event Bus veröffentlicht:** Nachdem die Command Handler-Methode erfolgreich abgeschlossen wurde und die Arbeitseinheit committet ist, veröffentlicht das Axon Framework das/die angewendeten Domänenereignis(se) im `EventBus`.
9. **Ereignis(se) persistieren:** Das Axon Framework stellt außerdem sicher, dass diese Domänenereignisse dauerhaft im konfigurierten Event Store persistiert werden (in diesem Fall in der `DOMAINEVENTS`-Tabelle in PostgreSQL über Axons JDBC Event Storage Mechanismus). Dies ist der "Event Sourcing"-Teil.
10. **Ereignis(se) an Event Handler zugestellt:** Andere Komponenten im System, bekannt als Event Handler oder Projektoren (oft annotiert mit `@EventHandler`), abonnieren spezifische Ereignistypen im `EventBus`. Wenn relevante Ereignisse veröffentlicht werden, liefert Axon sie an diese Handler.
11. **Read Model(s) aktualisieren:** Der Event Handler (Projektor) verarbeitet das Ereignis und aktualisiert ein oder mehrere Read Models (denormalisierte Ansichten der Daten, die in separaten Tabellen in der Read Model Datenbank gespeichert sind, z.B. `read_tenants` in PostgreSQL). Diese Read Models sind für Abfragen und die Bereitstellung von Daten an UIs oder andere Abfrageclients optimiert.

Dieser CQRS/ES-Fluss gewährleistet eine klare Trennung der Belange, bietet einen vollständigen Audit Trail durch den Event Store und ermöglicht flexible und skalierbare Read Model-Projektionen.

### 9.3 Mandantenerstellung im Detail

Dieses Sequenzdiagramm veranschaulicht den Prozess, bei dem ein Administrator über die Control Plane UI einen neuen Mandanten erstellt. Dies beinhaltet Interaktionen zwischen der UI, der `eaf-controlplane-api`, dem `eaf-multitenancy`-Modul (das ein `TenantAggregate` unter Verwendung des Axon Frameworks verwalten würde) und potenziell `eaf-iam` zum Einrichten anfänglicher mandantenspezifischer Konfigurationen oder Benutzer (letzteres ist in diesem Diagramm zur Fokussierung vereinfacht dargestellt).

```mermaid
sequenceDiagram
    actor Admin as Administrator
    participant CP_UI as Control Plane UI (React)
    participant CP_API as eaf-controlplane-api
    participant CmdGateway as Axon Command Gateway (in CP_API)
    participant TenantAgg as TenantAggregat (z.B. in eaf-multitenancy)
    participant EvtStore as Axon Event Store (PostgreSQL)
    participant EvtBus as Axon Event Bus
    participant TenantProjector as TenantReadModelProjektor
    participant ReadDB as Read Model DB (PostgreSQL)

    Admin->>+CP_UI: 1. Füllt Formular zur Mandantenerstellung (Name, Beschreibung etc.)
    CP_UI->>+CP_API: 2. POST /controlplane/api/v1/tenants (mit Mandantendaten)
    CP_API->>CmdGateway: 3. CreateTenantCommand erstellen & absenden
    CmdGateway->>+TenantAgg: 4. Befehl an @CommandHandler weiterleiten (neue Aggregatinstanz)
    TenantAgg->>TenantAgg: 5. Befehl validieren (z.B. eindeutiger Name falls erforderlich)
    alt Befehl gültig
        TenantAgg->>TenantAgg: 6. TenantCreatedEvent anwenden (mit tenantId, Name etc.)
        Note over TenantAgg: @EventSourcingHandler aktualisiert Aggregatzustand
        TenantAgg-->>CmdGateway: (Befehlsverarbeitung erfolgreich, gibt tenantId zurück)
        CmdGateway-->>CP_API: 7. tenantId zurückgegeben
        CP_API-->>-CP_UI: 8. HTTP 201 Created (mit tenantId und Repräsentation)

        TenantAgg->>EvtBus: 9. TenantCreatedEvent veröffentlicht (durch Axon)
        EvtBus->>EvtStore: 10. TenantCreatedEvent persistieren (durch Axon)
        EvtStore-->>EvtBus: (Persistierung erfolgreich)

        EvtBus->>+TenantProjector: 11. TenantCreatedEvent zugestellt
        TenantProjector->>+ReadDB: 12. Neuen Mandantendatensatz in 'read_tenants' Tabelle einfügen
        ReadDB-->>-TenantProjector: (Aktualisierung erfolgreich)
        TenantProjector-->>-EvtBus: (Ereignisverarbeitung abgeschlossen)
        
        CP_UI-->>-Admin: 13. Erfolg anzeigen (Mandant erstellt)
        
        Note right of CP_API: Optional könnte CP_API jetzt<br/>einen nachfolgenden Befehl an eaf-iam senden,<br/>um einen Standard-Admin-Benutzer für diesen neuen Mandanten zu erstellen.
    else Befehl ungültig
        TenantAgg-->>CmdGateway: Ausnahme (z.B. Validierung fehlgeschlagen)
        CmdGateway-->>CP_API: Ausnahme weiterleiten
        CP_API-->>-CP_UI: HTTP Fehler (z.B. 400 Bad Request mit Fehlerdetails)
        CP_UI-->>-Admin: Fehler anzeigen
    end
```

**Beschreibung des Ablaufs:**

1. **Formular ausfüllen:** Ein Administrator verwendet die Control Plane UI, um die Details zur Erstellung eines neuen Mandanten einzugeben (z.B. Name, Beschreibung).
2. **Anfrage senden:** Die UI sendet eine `POST`-Anfrage mit den Mandantendaten an die `eaf-controlplane-api`.
3. **Befehl absenden:** Der API-Controller in `eaf-controlplane-api` empfängt die Anfrage, validiert sie, konstruiert ein `CreateTenantCommand`-Objekt und sendet es über Axons `CommandGateway` ab.
4. **An Aggregat weiterleiten:** Der `CommandGateway` leitet den Befehl an die `@CommandHandler`-Methode im `TenantAggregate` weiter. Da dies ein neuer Mandant ist, instanziiert das Axon Framework ein neues `TenantAggregate`.
5. **Befehl validieren:** Das `TenantAggregate` validiert den Befehl (z.B. stellt sicher, dass der Mandantenname Kriterien erfüllt, prüft auf Eindeutigkeit, falls von Geschäftsregeln gefordert).
6. **Ereignis anwenden:** Falls gültig, wendet das `TenantAggregate` ein `TenantCreatedEvent` an, das alle notwendigen Daten für den neuen Mandanten erfasst. Der `@EventSourcingHandler` innerhalb des Aggregats aktualisiert seinen Zustand basierend auf diesem Ereignis.
7. **Befehlsergebnis:** Der Command Handler schließt erfolgreich ab und gibt möglicherweise die neue `tenantId` zurück.
8. **HTTP Antwort an UI:** Die `eaf-controlplane-api` gibt eine Erfolgsantwort (z.B. HTTP 201 Created) an die Control Plane UI zurück, einschließlich der ID und Repräsentation des neuen Mandanten.
9. **Ereignis veröffentlicht:** Das Axon Framework veröffentlicht das `TenantCreatedEvent` im `EventBus`.
10. **Ereignis persistiert:** Das Axon Framework persistiert das `TenantCreatedEvent` im Event Store (PostgreSQL).
11. **Ereignis an Projektor zugestellt:** Der `TenantReadModelProjector`, ein Event Handler, der `TenantCreatedEvent` abonniert hat, empfängt das Ereignis.
12. **Read Model aktualisieren:** Der Projektor erstellt einen neuen Datensatz für den Mandanten in der `read_tenants`-Tabelle (oder anderen relevanten Read Models) in der Read Model Datenbank.
13. **Erfolg anzeigen:** Die Control Plane UI informiert den Administrator, dass der Mandant erfolgreich erstellt wurde.
    *Hinweis: Wie im Diagramm angedeutet, könnte die `eaf-controlplane-api` nach erfolgreicher Mandantenerstellung nachfolgende Befehle initiieren, beispielsweise an das `eaf-iam`-Modul, um einen initialen Administratorbenutzer für den neu erstellten Mandanten anzulegen. Diese Folgeaktion ist Teil des übergeordneten Geschäftsprozesses, wurde aber zur Klarheit in diesem spezifischen Aggregat-Befehlsfluss getrennt dargestellt.*

### 9.4 Online-Lizenzaktivierung

Dieses Sequenzdiagramm skizziert den Prozess, bei dem eine EAF-basierte Anwendungsinstanz eine Online-Aktivierung ihrer Lizenz durch Kommunikation mit dem `eaf-license-server` durchführt. Der `eaf-license-server` selbst ist eine EAF-basierte Anwendung, die CQRS/ES-Prinzipien zur Verwaltung von `ActivatedLicenseAggregate`s verwendet.

```mermaid
sequenceDiagram
    participant EAFApp as EAF-basierte Anwendung (Client)
    participant LicenseServerAPI as eaf-license-server (REST API)
    participant LSCmdGateway as Axon Command Gateway (im Lizenzserver)
    participant ActivatedLicAgg as ActivatedLicenseAggregat (im Lizenzserver)
    participant LSEvtStore as Axon Event Store (PostgreSQL, für Lizenzserver)
    participant LSEvtBus as Axon Event Bus (im Lizenzserver)
    participant LicProjector as LizenzReadModelProjektor (im Lizenzserver)
    participant LSReadDB as Read Model DB (PostgreSQL, für Lizenzserver)

    EAFApp->>+LicenseServerAPI: 1. POST /licenseserver/api/v1/activations<br/>(productCode, licenseKey, hardwareIds, instanceId)
    Note over EAFApp, LicenseServerAPI: Anwendung authentifiziert sich beim Lizenzserver
    LicenseServerAPI->>LSCmdGateway: 2. ActivateLicenseCommand erstellen & absenden
    LSCmdGateway->>+ActivatedLicAgg: 3. Befehl an @CommandHandler weiterleiten
    ActivatedLicAgg->>ActivatedLicAgg: 4. Befehl validieren (Gültigkeit des Lizenzschlüssels prüfen,<br/>Aktivierungslimits, hardwareIds etc.)
    alt Befehl gültig
        ActivatedLicAgg->>ActivatedLicAgg: 5. LicenseActivatedEvent anwenden (mit activationId, Funktionen, Ablauf etc.)
        Note over ActivatedLicAgg: @EventSourcingHandler aktualisiert Aggregatzustand
        ActivatedLicAgg-->>LSCmdGateway: (Befehlsverarbeitung erfolgreich, gibt Aktivierungsdetails zurück)
        LSCmdGateway-->>LicenseServerAPI: 6. Aktivierungsdetails (activationId, Status, Funktionen)
        LicenseServerAPI-->>-EAFApp: 7. HTTP 200 OK / 201 Created (mit Aktivierungsdetails)

        ActivatedLicAgg->>LSEvtBus: 8. LicenseActivatedEvent veröffentlicht (durch Axon)
        LSEvtBus->>LSEvtStore: 9. LicenseActivatedEvent persistieren (durch Axon)
        LSEvtStore-->>LSEvtBus: (Persistierung erfolgreich)

        LSEvtBus->>+LicProjector: 10. LicenseActivatedEvent zugestellt
        LicProjector->>+LSReadDB: 11. 'read_activated_licenses' Datensatz einfügen/aktualisieren
        LSReadDB-->>-LicProjector: (Aktualisierung erfolgreich)
        LicProjector-->>-LSEvtBus: (Ereignisverarbeitung abgeschlossen)
        
        EAFApp->>EAFApp: 12. Aktivierungsdetails lokal speichern
    else Befehl ungültig
        ActivatedLicAgg-->>LSCmdGateway: Ausnahme (z.B. Lizenz ungültig, Limit erreicht)
        LSCmdGateway-->>LicenseServerAPI: Ausnahme weiterleiten
        LicenseServerAPI-->>-EAFApp: HTTP Fehler (z.B. 400 Bad Request mit Fehlercode)
    end
```

**Beschreibung des Ablaufs:**

1. **Aktivierung anfordern:** Eine EAF-basierte Anwendung sendet bei der Initialisierung oder bei Bedarf eine `POST`-Anfrage an den `/activations`-Endpunkt des `eaf-license-server`. Die Anfrage enthält den `productCode`, den `licenseKey` des Kunden, aktuelle `hardwareIds` (falls für Node-Locking relevant) und eine eindeutige `instanceId` für die Anwendungsinstanz. Die Anwendung authentifiziert sich beim `eaf-license-server`.
2. **Befehl absenden:** Der API-Controller im `eaf-license-server` empfängt die Anfrage, validiert sie, konstruiert ein `ActivateLicenseCommand` und sendet es über seinen internen Axon `CommandGateway` ab.
3. **An Aggregat weiterleiten:** Der `CommandGateway` leitet den Befehl an den `@CommandHandler` im `ActivatedLicenseAggregate` weiter. Axon lädt oder erstellt eine Aggregatinstanz, die potenziell durch den `licenseKey` oder einen zusammengesetzten Schlüssel identifiziert wird.
4. **Befehl validieren:** Das `ActivatedLicenseAggregate` führt Validierungen durch:
    * Prüft die Gültigkeit und Berechtigungen des bereitgestellten `licenseKey` (dies kann das Nachschlagen einer `LicenseDefinition` aus eigenen Read Models oder einer gemeinsamen Konfiguration beinhalten).
    * Überprüft, ob die Lizenz weitere Aktivierungen zulässt (z.B. Prüfung gegen `maxActivations`).
    * Vergleicht `hardwareIds` mit etwaigen bestehenden Aktivierungen für die Lizenz, falls Hardware-Bindung erzwungen wird.
5. **Ereignis anwenden:** Wenn die Validierung erfolgreich ist, wendet das Aggregat ein `LicenseActivatedEvent` an. Dieses Ereignis enthält alle relevanten Details wie eine eindeutige `activationId`, die durch diese Lizenz aktivierten Funktionen, das Ablaufdatum usw. Der `@EventSourcingHandler` innerhalb des Aggregats aktualisiert seinen Zustand.
6. **Befehlsergebnis:** Der Command Handler gibt die erfolgreichen Aktivierungsdetails zurück.
7. **HTTP Antwort an Anwendung:** Die `eaf-license-server` API sendet eine Erfolgsantwort (z.B. HTTP 200 OK oder 201 Created) zurück an die EAF-basierte Anwendung, einschließlich der `activationId`, des aktuellen `status`, der Liste der aktivierten `features` und des `expiresAt`-Datums.
8. **Ereignis veröffentlicht:** Das Axon Framework veröffentlicht das `LicenseActivatedEvent` im internen `EventBus` des `eaf-license-server`.
9. **Ereignis persistiert:** Das Axon Framework persistiert das `LicenseActivatedEvent` in seinem Event Store (PostgreSQL).
10. **Ereignis an Projektor zugestellt:** Der `LicenseReadModelProjector` (ein Event Handler innerhalb des `eaf-license-server`) empfängt das Ereignis.
11. **Read Model aktualisieren:** Der Projektor erstellt oder aktualisiert den Datensatz für diese Aktivierung in der `read_activated_licenses`-Tabelle in seiner Read Model Datenbank.
12. **Aktivierung lokal speichern:** Die EAF-basierte Anwendung empfängt die erfolgreichen Aktivierungsdetails und sollte sie lokal speichern (z.B. in einer Konfigurationsdatei, gesichertem Speicher) für zukünftige Offline-Validierungen und um wiederholte Online-Aktivierungen zu vermeiden.

## 10\\. Definitive Technologie-Stack-Auswahl

Dieser Abschnitt beschreibt die endgültigen Technologieentscheidungen für das ACCI EAF-Projekt. Diese Auswahl ist die alleinige Wahrheitsquelle für alle Technologieentscheidungen. Andere Architekturdokumente (z.B. für das Frontend) müssen sich auf diese Auswahl beziehen und ihre spezifische Anwendung erläutern, anstatt sie neu zu definieren.

**Hinweise zur Versionierung:**

* **Exakte Versionen:** Spezifische, exakte Versionen (z.B. `1.2.3`) sind erforderlich. Bereiche sind nicht zulässig.
* **"Neueste Stabile":** Wenn "Neueste Stabile" angegeben ist, bezieht es sich auf die neueste stabile Version, die zum Datum der "Letzten Dokumentaktualisierung" verfügbar war. Die tatsächlich verwendete Versionsnummer (z.B. `library@2.3.4`) muss hier festgehalten werden. Das Festschreiben von Versionen wird dringend bevorzugt.
* **Letzte Dokumentaktualisierung:** 16. Mai 2025

**Bevorzugte Starter-Vorlagen:**

* Bevorzugte Starter-Vorlage Frontend (Control Plane UI): React-Admin. Für andere potenzielle Frontend-Anwendungen werden Vite-basierte Starter-Vorlagen bevorzugt.
* Bevorzugte Starter-Vorlage Backend: Spring Initializr ([start.spring.io](https://start.spring.io/)) für die Struktur von Spring Boot-Modulen. Für die spezifische Struktur des Axon Frameworks werden offizielle Beispiele und empfohlene Projektlayouts befolgt.

| Kategorie                   | Technologie                                                       | Version / Details                      | Beschreibung / Zweck                                                                 | Begründung (Optional)                                                                                        |
| :-------------------------- | :---------------------------------------------------------------- | :------------------------------------- | :----------------------------------------------------------------------------------- | :----------------------------------------------------------------------------------------------------------- |
| **Sprachen**                | Kotlin                                                            | 2.1.21                                 | Primäre Sprache für Backend-EAF-Module und -Anwendungen                              | Modern, prägnant, null-sicher, exzellente Java-Interoperabilität, gute IDE-Unterstützung. Vom PRD vorgegeben. |
| **Laufzeitumgebung**        | JVM (IBM Semeru Runtimes for Power oder OpenJDK)                  | Java 21 (LTS)                          | Ausführungsumgebung für Kotlin/Spring Boot-Anwendungen auf ppc64le                   | Standard für Kotlin/Java. IBM Semeru bietet optimierte Builds für Power-Architektur. JDK 21 als LTS.        |
| **Frameworks**              | Spring Boot                                                       | 3.4.5                                  | Kern-Anwendungsframework für Backend-Module und -Dienste                               | Umfassend, etabliert, unterstützt schnelle Entwicklung, gute Integrationen. Vom PRD vorgegeben.            |
|                             | Axon Framework                                                    | 4.11.2 \<br/>\(Upgrade auf v5 geplant)     | Framework für DDD, CQRS, Event Sourcing                                              | Spezialisiert auf die gewählten Architekturmuster, gute Integration mit Spring. Vom PRD vorgegeben.         |
|                             | React                                                             | 19.1                                   | JavaScript-Bibliothek zum Erstellen der Control Plane UI                               | Populär, komponentenbasiert, großes Ökosystem. Vom PRD vorgegeben.                                     |
| **Datenbanken**             | PostgreSQL                                                        | 17.5                                   | Primäres RDBMS für Event Store, Read Models und Zustandsdaten                          | Leistungsstark, Open Source, ACID-konform, gute Unterstützung für JSON. Vom PRD vorgegeben.               |
| **Build-Tool**              | Gradle                                                            | 8.14                                   | Build-Automatisierung für das Monorepo                                               | Flexibel, gut für Kotlin & Multi-Projekt-Builds, Dependency Management. Vom PRD vorgegeben.              |
| **Infrastruktur**           | Docker                                                            | Neueste stabile Engine-Version         | Containerisierung für Deployment- und Entwicklungskonsistenz auf ppc64le             | Ermöglicht portable Umgebungen, vereinfacht Deployment. Erwähnt in PRD NFRs.                              |
|                             | Docker Compose                                                    | Neueste stabile Version                | Orchestrierung von lokalen Entwicklungs-/Test-Containern (App, DB, etc.) und Produktions-Stack auf einzelner VM | Vereinfacht das Setup von Multi-Container-Umgebungen lokal und in Produktion (gemäß Anforderung).          |
| **UI-Bibliotheken (Control Plane)** | React-Admin                                                       | 5.8.1                                  | Admin-Framework zum Erstellen der Control Plane UI (Datentabellen, Formulare etc.)       | Bietet viele Out-of-the-Box-Komponenten für Admin-Oberflächen, inspiriert Design laut PRD.               |
| **Zustandsverwaltung (Control Plane)** | In React-Admin integriert / empfohlen (z.B. React Context, Ra-Store) | Gekoppelt an React-Admin Version        | Frontend-Zustandsverwaltung für Control Plane UI                                       | Von React-Admin bereitgestellt oder direkt integrierbar.                                                    |
| **Testen (Backend)**        | JUnit Jupiter                                                     | 5.12.2                                 | Test-Framework für Java/Kotlin                                                       | Standard für JVM-Tests.                                                                                     |
|                             | MockK                                                             | 1.14.2                                 | Mocking-Bibliothek für Kotlin                                                        | Kotlin-idiomatische Mocks.                                                                                  |
|                             | Kotest                                                            | 5.9.1                                  | Assertion-Bibliothek für flüssige Tests                                              | Verbessert Lesbarkeit von Tests, bietet verschiedene Teststile.                                            |
|                             | Testcontainers                                                    | 1.21.0 (Java)                          | Für Integrationstests mit Docker-abhängigen Abhängigkeiten (z.B. DB)                   | Ermöglicht zuverlässige Integrationstests.                                                                 |
|                             | Axon Test Fixture                                                 | Gekoppelt an Axon Framework 4.11.2     | Zum Testen von Axon-Aggregaten und Sagas                                             | Spezifisch für Axon-Komponenten.                                                                            |
| **Testen (Frontend - CP UI)** | Jest                                                              | 29.7.0                                 | JavaScript-Test-Framework                                                            | Weit verbreitet für React-Tests.                                                                            |
|                             | React Testing Library                                             | 16.3.x                                 | Zum Testen von React-Komponenten                                                     | Fördert Best Practices beim Testen von UI-Komponenten.                                                       |
|                             | Playwright                                                        | 1.52.x                                 | Für End-to-End-Tests der Control Plane UI                                            | Mächtiges Werkzeug für zuverlässige E2E-Tests.                                                            |
| **CI/CD**                   | GitHub Actions                                                    | N/A (Dienst)                           | Automatisierung für Build-, Test- und Deployment-Pipelines                             | Integriert in GitHub, flexibel konfigurierbar. Gemäß Projektstruktur.                                     |
| **Andere Werkzeuge**        | Logback                                                           | (Version über Spring Boot)              | Logging-Framework für Backend                                                        | Standard in Spring Boot.                                                                                    |
|                             | Micrometer                                                        | (Version über Spring Boot)              | Fassade für Anwendungsmetriken                                                       | Ermöglicht Metrik-Export (z.B. an Prometheus). Erwähnt in PRD.                                           |
|                             | springdoc-openapi                                                 | `Version passend zu Spring Boot 3.4.x` | Generiert OpenAPI 3-Dokumentation aus Spring Boot-Controllern                        | Automatisiert API-Dokumentationserstellung.                                                                 |
|                             | Liquibase                                                         | 4.31.1                                 | Werkzeug zur Verwaltung von Datenbankschemaänderungen (Read Models etc.)               | Notwendig für versionierte DB-Migrationen.                                                                  |

## 11\\. Infrastruktur- und Bereitstellungsübersicht

Dieser Abschnitt beschreibt die Infrastrukturumgebung, für die das ACCI EAF und seine abgeleiteten Anwendungen konzipiert sind, sowie die Strategie für deren Bereitstellung.

* **Ziel-Infrastrukturanbieter:**
  * On-Premise-Rechenzentren / Private-Cloud-Umgebungen.
  * **Virtuelle Maschinen (VMs) werden vom Kunden bereitgestellt und verwaltet**, typischerweise auf IBM Power Architektur (ppc64le). Die ACCI EAF-Bereitstellung beinhaltet keine VM-Provisionierung oder Konfiguration auf Betriebssystemebene, die über das für die Docker-Laufzeit Notwendige hinausgeht.
  * Keine Abhängigkeit von Public-Cloud-Anbieterdiensten (z.B. AWS, Azure, GCP) oder Kubernetes für das Kern-EAF und seine MVP-Anwendungen.

* **Genutzte Kerndienste (bereitgestellt über Docker Compose auf einer einzelnen Kunden-VM):**
  * Der gesamte Anwendungsstack, einschließlich aller notwendigen Dienste, ist so konzipiert, dass er von Docker Compose auf einer einzelnen vom Kunden bereitgestellten VM orchestriert wird. Dies beinhaltet:
    * **PostgreSQL Server:** Läuft als Docker-Container, verwaltet innerhalb des Docker Compose-Setups. Seine Daten werden über Docker-Volumes persistiert, die auf die Host-VM gemappt sind.
    * **ACCI EAF-Anwendungen:** Ausführbare Anwendungen wie `eaf-controlplane-api` und `eaf-license-server` sowie Anwendungen, die von Endbenutzern basierend auf dem ACCI EAF erstellt wurden, werden als Docker-Container bereitgestellt und von Docker Compose verwaltet.
    * **Webserver / Reverse Proxy (Optional):** Falls erforderlich (z.B. für SSL-Terminierung, Auslieferung statischer Inhalte für die Control Plane UI oder als API-Gateway), würde ein Webserver wie Nginx oder Traefik ebenfalls als Docker-Container innerhalb desselben Docker Compose-Setups laufen.

* **Infrastrukturdefinition & Anwendungspaketierung:**
  * **Anwendungspaketierung:** **Docker** wird verwendet, um Container-Images für alle ausführbaren EAF-Komponenten und PostgreSQL zu erstellen. Alle Docker-Images werden speziell für die **ppc64le**-Architektur erstellt. Dockerfiles werden im Quellcode jedes Anwendungsmoduls verwaltet.
  * **Laufzeitorchestrierung auf VM:** **Docker Compose** ist das primäre Werkzeug zur Definition, Orchestrierung und Verwaltung des Lebenszyklus des gesamten Anwendungsstacks (Datenbank, Backend-Anwendungen, Webserver) auf der Kunden-VM. Eine Master-`docker-compose.yml`-Datei definiert alle Dienste, Netzwerke, Volumes und Konfigurationen.
  * **VM-Provisionierung & -Konfiguration:** Dies liegt in der **Verantwortung des Kunden**. Das ACCI EAF-Bereitstellungspaket setzt eine VM mit einem kompatiblen Linux-Betriebssystem, installiertem und laufendem Docker (und Docker Compose) sowie ausreichenden Ressourcen voraus. Es werden keine Werkzeuge wie Ansible für die VM-Konfiguration von ACCI für die EAF-Bereitstellung selbst bereitgestellt oder benötigt.
  * **Auslieferungspaket (für Air-Gapped-Umgebungen):**
    * Für Kundenbereitstellungen, die immer als Air-Gapped und ohne Zugriff auf öffentliche Docker-Register betrachtet werden, stellt ACCI ein **TAR-Archiv** bereit.
    * Dieses TAR-Archiv enthält:
      * Alle erforderlichen Docker-Images (exportiert mit `docker save`).
      * Die `docker-compose.yml`-Datei, die den gesamten Stack definiert.
      * Notwendige Hilfsskripte (z.B. Bash oder Python) für Installation, Updates und grundlegende Verwaltung (Start, Stopp, Status) des Stacks.
      * Liquibase-Migrationsskripte für die Datenbank.
      * Konfigurationsvorlagendateien.

* **Bereitstellungsstrategie:**
  * **Artefakte:** Die CI/CD-Pipeline (GitHub Actions) erstellt, testet und paketiert die ppc64le Docker-Images. Das endgültige "Release-Artefakt" für Kunden ist das oben genannte TAR-Archiv.
  * **CI/CD-Tool:** **GitHub Actions** für Continuous Integration, automatisiertes Testen und Zusammenstellen des Bereitstellungs-TAR-Archivs.
  * **Bereitstellung auf Kunden-VM (manueller Air-Gapped-Prozess):**
        1. Sichere Übertragung des versionierten TAR-Archivs in die Kundenumgebung.
        2. Der Kunde (oder ein ACCI-Techniker vor Ort) entpackt das TAR-Archiv auf der Ziel-VM.
        3. Das bereitgestellte Installationsskript wird ausgeführt. Dieses Skript wird typischerweise:
            *Docker-Images in den lokalen Docker-Daemon auf der VM laden (mit `docker load < image.tar`).
            * Umgebungsspezifische Parameter konfigurieren (z.B. Netzwerkeinstellungen, externe Dienst-URLs falls vorhanden, Geheimnisse – potenziell über eine `.env`-Datei, die von Docker Compose verwendet wird).
            *Datenbankschemamigrationen mit Liquibase ausführen (dies kann in das Startskript einer Anwendung integriert oder als separater Schritt vom Installationsskript vor dem Start des Hauptanwendungsstacks ausgeführt werden).
            * Den gesamten Anwendungsstack mit `docker-compose up -d` und der bereitgestellten `docker-compose.yml` starten.
  * **Updates:** Updates folgen einem ähnlichen Prozess: Bereitstellung eines neuen TAR-Archivs, Stoppen des aktuellen Stacks, Laden neuer Images, potenzielles Ausführen von Daten-/Schemamigrationen und Neustart des Stacks mit der aktualisierten Konfiguration/den Images.

* **Umgebungen:**
  * **Entwicklung:** Lokale Entwicklerrechner, die Docker Compose verwenden, um das Single-VM-Produktionssetup genau zu replizieren.
  * **Staging/QA:** Eine dedizierte VM-Umgebung für Integrationstests, UAT und Leistungstests, bereitgestellt mit derselben TAR-Archiv- und Docker Compose-Methodik wie die Produktion.
  * **Produktion:** Die Live-VM des Kunden, bereitgestellt und verwaltet wie oben beschrieben.

* **Strategie zur Umgebungsförderung (Environment Promotion):**
  * Code wird entwickelt und getestet. Nach erfolgreicher Validierung wird ein Release-Kandidat-TAR-Archiv erstellt.
  * Dieses TAR-Archiv wird zuerst in der **Staging/QA**-Umgebung für gründliche Tests bereitgestellt.
  * Nach erfolgreicher Staging-Validierung und Freigabe wird das *identische* TAR-Archiv für die **Produktions**bereitstellung durch den Kunden genehmigt.

* **Rollback-Strategie:**
  * **Rollback des Anwendungsstacks:** Im Falle einer fehlerhaften Bereitstellung besteht die primäre Rollback-Strategie darin:
        1. Den aktuellen Docker Compose-Stack zu stoppen und zu entfernen (`docker-compose down`).
        2. Wenn das TAR-Archiv und die geladenen Images einer früheren Version noch auf der VM verfügbar sind (oder erneut übertragen werden können), verwenden Sie die Skripte und `docker-compose.yml` dieser früheren Version, um den älteren, stabilen Stack neu zu starten.
        3. Eine sorgfältige Verwaltung von Docker-Image-Tags innerhalb des TAR-Archivs (z.B. `image:tag_version_X`) und entsprechenden `docker-compose.yml`-Dateien ist entscheidend.
  * **Datenbank-Rollback:** Liquibase unterstützt Rollback-Befehle für Schemaänderungen. Datenzustands-Rollbacks erfordern typischerweise die Wiederherstellung aus einem Datenbank-Backup. Verfahren zur Datenbanksicherung liegen in der Verantwortung des Kunden, können aber von ACCI beraten werden.

## 12\\. Fehlerbehandlungsstrategie

Eine robuste Fehlerbehandlungsstrategie ist entscheidend für die Stabilität, Wartbarkeit und Diagnosefähigkeit des ACCI EAF und der darauf basierenden Anwendungen. Dieser Abschnitt beschreibt den allgemeinen Ansatz, Protokollierungspraktiken und spezifische Fehlerbehandlungsmuster.

* **Allgemeiner Ansatz:**
  * **Exceptions als primärer Mechanismus:** Exceptions sind der primäre Mechanismus zur Signalisierung und Weitergabe von Fehlern im Anwendungscode. Es werden die Standard-Exceptions von Kotlin und die Exception-Hierarchie von Java verwendet.
  * **Benutzerdefinierte Exception-Hierarchie:** Es wird eine benutzerdefinierte Exception-Hierarchie definiert, die Standard-Exceptions (z.B. `RuntimeException`, `IllegalArgumentException`) erweitert. Diese Hierarchie umfasst:
    * Eine Basis-Anwendungsexception (z.B. `AcciEafException`).
    * Spezifische Business-Exceptions für verschiedene Domänen (z.B. `TenantNotFoundException` von `eaf-multitenancy`, `UserAuthenticationException` von `eaf-iam`, `LicenseValidationException` von `eaf-licensing`).
    * Technische/Integrations-Exceptions (z.B. `ExternalServiceUnavailableException`, `ConfigurationException`).
  * **Klare Fehlermeldungen:** Exceptions sollten klare, prägnante Meldungen enthalten, die für Entwickler/Logs gedacht sind, und möglicherweise eindeutige Fehlercodes zur einfacheren Nachverfolgung und Referenz.
  * **Fail Fast:** Bei nicht behebbaren Fehlern oder ungültigen Zuständen sollte das System schnell fehlschlagen, um eine weitere inkonsistente Verarbeitung zu verhindern.

* **Protokollierung:**
  * **Bibliothek/Methode:** **Logback** (standardmäßig mit Spring Boot bereitgestellt) ist das primäre Protokollierungsframework. Es wird für **strukturiertes Logging im JSON-Format** konfiguriert, um die Analyse durch Log-Management-Systeme zu erleichtern. Die Bibliothek `logstash-logback-encoder` kann verwendet werden, um die JSON-Formatierung zu verbessern und benutzerdefinierte Felder einzuschließen.
  * **Log-Level:** Standard-Log-Level werden konsistent verwendet:
    * `ERROR`: Kritische Fehler, die den normalen Betrieb verhindern oder zu Dateninkonsistenz führen. Signifikante Fehler, die sofortige Aufmerksamkeit erfordern. Enthält Stacktraces.
    * `WARN`: Mögliche Probleme oder ungewöhnliche Situationen, die die Verarbeitung (noch) nicht stoppen, aber auf zukünftige Probleme hinweisen oder eine Untersuchung erfordern könnten (z.B. Wiederholung einer Operation, Konfigurationsprobleme, Deprecation-Warnungen).
    * `INFO`: Allgemeine Meldungen, die den Lebenszyklus der Anwendung und wichtige Geschäftsoperationen verfolgen (z.B. Anwendungsstart, wichtige Serviceaufrufe, Mandantenerstellung, erfolgreiche Lizenzaktivierung).
    * `DEBUG`: Detaillierte Informationen, die für Entwickler beim Debuggen nützlich sind (z.B. Methodenein-/-ausstieg, Variablenwerte, detaillierte Ablaufverfolgung). Sollte in der Produktion standardmäßig deaktiviert, aber konfigurierbar sein.
    * `TRACE`: Extrem detaillierte Diagnoseinformationen, typischerweise nur für spezifische Fehlerbehebungsszenarien aktiviert.
  * **Kontextinformationen in Logs:** Alle Log-Einträge (insbesondere `INFO`, `WARN`, `ERROR`) sollten wichtige kontextbezogene Informationen enthalten:
    * **Zeitstempel** (ISO 8601-Format).
    * **Log-Level**.
    * **Thread-Name**.
    * **Logger-Name** (typischerweise der Klassenname).
    * **Nachricht**.
    * **Stacktrace** (für Exceptions auf `ERROR`- und optional `WARN`-Ebene).
    * **Korrelations-ID (Trace ID):** Eine eindeutige ID, die zu Beginn einer Anfrage (z.B. eingehender API-Aufruf) generiert und durch alle nachfolgenden Serviceaufrufe und Log-Meldungen im Zusammenhang mit dieser Anfrage weitergegeben wird. Dies ist entscheidend für die Verfolgung verteilter Operationen. Spring Cloud Sleuth (auch ohne Zipkin für Tracing, falls nicht verwendet) oder ein ähnlicher Mechanismus (z.B. MDC) wird verwendet.
    * **Mandanten-ID:** (Falls auf den Kontext anwendbar und nicht sensibel in der Log-Nachricht selbst).
    * **Benutzer-ID / Principal-Name:** (Falls anwendbar, wobei sichergestellt wird, dass PII gemäß Sicherheitsrichtlinien behandelt wird).
    * **Operationsname / Servicename:** Identifiziert die spezifische Operation oder Komponente.
    * **Schlüsselparameter (bereinigt):** Relevante Eingabeparameter oder Identifikatoren, wobei sichergestellt wird, dass sensible Daten (Passwörter, Geheimnisse, PII) maskiert oder weggelassen werden.

* **Spezifische Behandlungsmuster:**
  * **Externe API-Aufrufe / Integrationen (HTTP, LDAP, SMTP, etc.):**
    * **Timeouts:** Konfigurieren Sie angemessene Verbindungs- und Lese-Timeouts für alle externen Aufrufe, um unbegrenztes Blockieren zu verhindern. Bibliotheken wie `OkHttp`, `RestTemplate` (mit Konfiguration) oder spezifische Protokollbibliotheken (z.B. JavaMail, UnboundID LDAP SDK) bieten hierfür Mechanismen.
    * **Wiederholungsversuche:** Implementieren Sie für vorübergehende Netzwerkprobleme oder temporäre Nichtverfügbarkeit externer Dienste automatische Wiederholungsmechanismen mit exponentiellem Backoff und Jitter. **Spring Retry** (`@Retryable`) ist hierfür die bevorzugte Bibliothek.
    * **Circuit Breaker:** Für Integrationen, die anfällig für Fehler oder hohe Latenzzeiten sind, wird ein Circuit Breaker-Muster mit **Resilience4j** implementiert. Dies verhindert kaskadierende Fehler, indem Anfragen an einen fehlerhaften Dienst für einen bestimmten Zeitraum gestoppt werden. Fallback-Mechanismen (z.B. Rückgabe von zwischengespeicherten Daten, Standardwerten oder einer spezifischen Fehlermeldung) sollten in Betracht gezogen werden.
    * **Fehlerzuordnung:** Fehler von externen Diensten (z.B. HTTP 4xx/5xx-Statuscodes, LDAP-Fehlercodes, SMTP-Exceptions) werden abgefangen und auf spezifische interne `AcciEafException`-Subtypen abgebildet, um einen konsistenten Fehlerbehandlungsansatz innerhalb des EAF zu gewährleisten. Sensible Details von externen Fehlern sollten nicht direkt an Endbenutzer weitergegeben werden.
  * **Interne Geschäftslogik-Exceptions:**
    * Benutzerdefinierte domänenspezifische Exceptions (z.B. `InvalidTenantStatusException`, `DuplicateUsernameException`, `LicenseExpiredException`) werden von der Geschäftslogik in Aggregaten oder Domänendiensten ausgelöst.
    * **Fehlerbehandlung auf API-Ebene (z.B. in `eaf-controlplane-api`, `eaf-license-server`):** Die Mechanismen `@ControllerAdvice` und `@ExceptionHandler` von Spring Boot werden verwendet, um diese benutzerdefinierten Exceptions (und Standard-Spring-Exceptions) global zu behandeln. Diese Handler werden:
      * Den vollständigen Fehler mit Stacktrace auf `ERROR`-Ebene protokollieren.
      * Die Exception in eine standardisierte JSON-Fehlerantwort für den API-Client umwandeln, einschließlich einer benutzerfreundlichen Nachricht, eines eindeutigen Fehlercodes/-ID (für den Support) und eines geeigneten HTTP-Statuscodes (z.B. 400 für Validierungsfehler, 404 für nicht gefunden, 403 für verboten, 409 für Konflikte, 500 für unerwartete Serverfehler).
  * **Axon Framework Command-Behandlung:**
    * Exceptions, die von `@CommandHandler`-Methoden in Aggregaten ausgelöst werden, werden an den Aufrufer des `CommandGateway` weitergegeben.
    * Diese Exceptions sollten spezifische Business-Exceptions sein. Die API-Schicht (oder die Serviceschicht, die den Befehl sendet) behandelt diese dann wie oben beschrieben.
    * Axon ermöglicht auch `CommandDispatchInterceptor` und `CommandHandlerInterceptor`, um bei Bedarf eine übergreifende Fehlerbehandlung hinzuzufügen.
  * **Axon Framework Event-Behandlung / Projektionen:**
    * Fehler, die innerhalb von `@EventHandler`-Methoden auftreten (z.B. beim Aktualisieren von Read Models), erfordern sorgfältige Überlegung. Axon Framework bietet konfigurierbare Fehlerbehandler für Event-Prozessoren (z.B. `ListenerInvocationErrorHandler`, `ErrorHandler`).
    * **Strategie:**
      * Für vorübergehende Fehler (z.B. Datenbankverbindungsproblem während der Read Model-Aktualisierung) kann ein Wiederholungsmechanismus für den Event-Prozessor konfiguriert werden.
      * Für nicht vorübergehende Fehler (z.B. ein Event, das aufgrund eines Fehlers im Handler oder unerwarteter Daten konsistent nicht verarbeitet werden kann) sollte das Event typischerweise nach einigen fehlgeschlagenen Versuchen in eine Dead-Letter Queue (DLQ) verschoben werden, oder der Event-Prozessor könnte gestoppt werden, um eine weitere Blockierung der Event-Verarbeitung zu verhindern. Dies erfordert die Überwachung des DLQ- oder Prozessorstatus.
      * Die Protokollierung solcher Fehler bei der Event-Verarbeitung ist entscheidend.
  * **Transaktionsmanagement:**
    * **Lokale Transaktionen (z.B. Read Model-Aktualisierungen):** Standard-Spring-`@Transactional`-Annotationen werden verwendet, um ACID-Transaktionen für Datenbankoperationen innerhalb von Event-Projektoren oder Diensten zu verwalten, die direkt mit PostgreSQL für zustandsbehaftete Daten interagieren. Wenn ein Event-Handler mehrere Aktualisierungen verarbeitet, sollten diese idealerweise innerhalb einer einzigen Transaktion erfolgen.
    * **Verteilte Transaktionen / Sagas (Konsistenz über Aggregate hinweg):** Für Geschäftsprozesse, die sich über mehrere Aggregate erstrecken und letztendliche Konsistenz erfordern, werden **Axon Sagas** verwendet. Sagas lauschen auf Events und senden neue Befehle, um den Prozess zu orchestrieren. Sagas müssen Kompensationslogik (kompensierende Aktionen/Befehle) implementieren, um Fehler in jedem Schritt der verteilten Transaktion zu behandeln und sicherzustellen, dass das System in einen konsistenten Zustand zurückgeführt werden kann.

## 13\\. Coding-Standards (ACCI Kotlin Coding Standards v1.0)

Diese Standards, zusammenfassend als **"ACCI Kotlin Coding Standards v1.0"** bezeichnet, sind für den gesamten Code verbindlich, der von KI-Agenten und menschlichen Entwicklern für das ACCI EAF-Projekt erstellt wird. Abweichungen sind nicht zulässig, es sei denn, sie werden ausdrücklich genehmigt und als Ausnahme in diesem Abschnitt oder einem verlinkten Nachtrag dokumentiert. Die Einhaltung dieser Standards wird durch Code-Reviews und automatisierte Prüfungen in der CI/CD-Pipeline, wo immer möglich unter Verwendung von Werkzeugen wie Ktlint und Detekt, durchgesetzt.

Die Hauptziele dieser Standards sind die Sicherstellung von Codequalität, Konsistenz, Wartbarkeit, Lesbarkeit und die Bereitstellung klarer Richtlinien für eine effiziente Entwicklung.

* **Primäre Sprache & Laufzeit(en):**
  * **Sprache:** Kotlin (Version `2.1.21` wie in "Definitive Technologie-Stack-Auswahl" spezifiziert).
  * **Laufzeit:** JVM (Java `21` LTS - z.B. IBM Semeru Runtimes for Power oder OpenJDK, wie in "Definitive Technologie-Stack-Auswahl" spezifiziert).

* **Style Guide & Linter:**
  * **Offizielle Kotlin Coding Conventions:** Jeglicher Kotlin-Code muss den offiziellen Kotlin Coding Conventions entsprechen, die von JetBrains dokumentiert sind: [https://kotlinlang.org/docs/coding-conventions.html](https://kotlinlang.org/docs/coding-conventions.html)
  * **Linter/Formatter:**
    * **Ktlint:** Ktlint wird als primärer Linter und Formatierer verwendet, um diese Konventionen durchzusetzen. Es sollte in den Build-Prozess (Gradle-Task) und idealerweise in die IDE integriert werden.
    * **IDE-Formatierung:** Der integrierte Formatierer von IntelliJ IDEA sollte so konfiguriert werden, dass er den offiziellen Kotlin Coding Conventions und Ktlint-Regeln entspricht (oft erreicht durch Importieren von Einstellungen aus `.editorconfig`, wenn Ktlint für dessen Verwendung konfiguriert ist).
  * **Konfiguration:** Eine gemeinsame `.editorconfig`-Datei wird im Stammverzeichnis des Monorepos verwaltet und enthält Einstellungen für Ktlint und allgemeine Editor-Konfigurationen (Einrückung, Zeilenenden usw.). Spezifische Ktlint-Regelsatzkonfigurationen, falls von den Standardeinstellungen abweichend oder um benutzerdefinierte Regeln erweitert, werden zentral dokumentiert und verwaltet.
  * **CI-Durchsetzung:** Die CI/CD-Pipeline muss einen Schritt zur Ausführung von Ktlint-Prüfungen enthalten; Builds sollten fehlschlagen, wenn Verstöße festgestellt werden.
  * **Statische Analyse (Detekt):** Detekt wird zusätzlich zu Ktlint für eine tiefere statische Analyse verwendet, um Code Smells, Komplexitätsprobleme und potenzielle Fehler zu prüfen. Detekt wird ebenfalls in den Gradle-Build und die CI-Pipeline integriert, mit generierten Berichten. Ein Build-Fehler kann für kritische Probleme konfiguriert werden.

* **Namenskonventionen:**
    (Gemäß den Standardkonventionen von Kotlin)
  * **Pakete:** Kleinschreibung mit Punkt-Trennung (z.B. `com.axians.accieaf.iam.domain`).
  * **Klassen, Interfaces, Objekte, Enums, Annotationen, Typ-Aliase:** `PascalCase` (z.B. `TenantService`, `UserRepository`).
  * **Funktionen, Methoden, Eigenschaften (nicht konstant), lokale Variablen, Parameter:** `camelCase` (z.B. `createTenant`, `userName`, `isActive`).
  * **Testfunktionen/-methoden:** `camelCase` oder beschreibende Sätze in Backticks (z.B. `` `sollte Mandant erstellen wenn valide Daten bereitgestellt werden` ``).
  * **Konstanten (`const val`, Top-Level-`val` mit `@JvmField` in Objekten, `enum`-Einträge):** `UPPER_SNAKE_CASE` (z.B. `MAX_RETRIES`, `DEFAULT_TIMEOUT_MS`).
  * **Generische Typparameter:** Einzelner Großbuchstabe (z.B. `T`, `R`) oder ein beschreibender `PascalCase`-Name, wenn mehr Klarheit erforderlich ist (z.B. `RequestType`).
  * **Kotlin-Quelldateien (`.kt`):**
    * Wenn eine Datei eine einzelne Top-Level-Klasse/Objekt/Interface enthält, muss der Dateiname mit deren Namen und der `.kt`-Erweiterung übereinstimmen (z.B. `Tenant.kt`).
    * Wenn eine Datei mehrere Top-Level-Deklarationen oder nur Erweiterungsfunktionen/-eigenschaften enthält, sollte der Dateiname deren Inhalt in `PascalCase` beschreiben (z.B. `CollectionUtils.kt`).
  * **Gradle-Module:** `kebab-case` wie in der "Projektstruktur" definiert (z.B. `eaf-core`, `eaf-iam`).

* **Dateistruktur:**
  * **Allgemeine Projektstruktur:** Halten Sie sich strikt an das Layout, das im Abschnitt "Projektstruktur" dieses Dokuments definiert ist.
  * **Paketstruktur innerhalb von Modulen:** Innerhalb der Verzeichnisse `src/main/kotlin/` und `src/test/kotlin/` jedes Moduls sollten Pakete nach Feature oder Schicht organisiert sein, konsistent mit den Prinzipien der Hexagonalen Architektur (z.B. `com.axians.accieaf.[moduleName].domain`, `com.axians.accieaf.[moduleName].application`, `com.axians.accieaf.[moduleName].adapter.api`, `com.axians.accieaf.[moduleName].adapter.persistence`).
  * **Organisation von Unit-Test-Dateien:**
    * **Speicherort:** Unit-Test-Dateien müssen sich im Verzeichnis `src/test/kotlin/` ihres jeweiligen Moduls befinden. Die Paketstruktur innerhalb von `src/test/kotlin/` muss die Paketstruktur des zu testenden Codes in `src/main/kotlin/` widerspiegeln.
    * **Namenskonvention:** Testklassendateien müssen nach der Klasse benannt werden, die sie testen, mit dem Suffix `Test`. Zum Beispiel wird eine Klasse `com.axians.accieaf.iam.application.TenantService` in `eaf-iam/src/main/kotlin/com/axians/accieaf/iam/application/TenantService.kt` ihre entsprechende Testklasse `com.axians.accieaf.iam.application.TenantServiceTest` in `eaf-iam/src/test/kotlin/com/axians/accieaf/iam/application/TenantServiceTest.kt` haben.

* **Asynchrone Operationen:**
  * **Kotlin Coroutines:** Kotlin Coroutines (`suspend`-Funktionen, `kotlinx.coroutines.flow.Flow`, `kotlinx.coroutines.channels.Channel`) sind der bevorzugte Mechanismus zur Verwaltung asynchroner Operationen, nicht-blockierender I/O und Nebenläufigkeit.
  * **Strukturierte Nebenläufigkeit:** Code muss den Prinzipien der strukturierten Nebenläufigkeit entsprechen. Coroutines sollten innerhalb eines `CoroutineScope` gestartet werden, der an den Lebenszyklus der sie verwaltenden Komponente gebunden ist (z.B. ein Spring-Service, ein Axon-Event-Handler). Vermeiden Sie das Starten von Coroutines auf `GlobalScope`, es sei denn, dies ist ausdrücklich gerechtfertigt und verwaltet.
  * **Dispatcher:** Verwenden Sie geeignete Dispatcher von `kotlinx.coroutines.Dispatchers` (`Dispatchers.IO` für blockierende I/O-Operationen, die noch nicht für Coroutines angepasst sind, `Dispatchers.Default` für CPU-intensive Arbeit, `Dispatchers.Unconfined` mit Vorsicht). Spring MVC-Controller mit `suspend`-Funktionen laufen typischerweise auf einem geeigneten, von Spring verwalteten Dispatcher.
  * **Axon Framework Integration:** Nutzen Sie die Unterstützung des Axon Frameworks für Kotlin Coroutines (z.B. suspendierende Command Handler, Query Handler und Event Handler, wo durch `axon-kotlin`-Erweiterungen oder entsprechende Integration bereitgestellt).
  * **Spring Integration:** Das Spring Framework (einschließlich Spring Boot MVC) bietet eine hervorragende Unterstützung für Kotlin Coroutines. `suspend`-Funktionen können direkt in `@RestController`-Methoden verwendet werden.
  * **Fehlerbehandlung:** Stellen Sie eine ordnungsgemäße Fehlerbehandlung innerhalb von Coroutines mithilfe von `try-catch`-Blöcken und einem Verständnis der Coroutine-Cancellation sicher.
  * **Blockieren vermeiden:** Rufen Sie keinen blockierenden Code innerhalb eines Coroutine-Kontexts auf, ohne zu einem geeigneten Dispatcher zu wechseln (z.B. `Dispatchers.IO`).

* **Typsicherheit:**
  * **Kotlin-Typsystem nutzen:** Das starke Typsystem von Kotlin, einschließlich seiner robusten Null-Safety-Funktionen, muss vollständig genutzt werden.
    * **Null-Safety:**
      * Deklarieren Sie Typen nur dann als nullable (`?`), wenn `null` ein gültiger und aussagekräftiger Wert für diese Variable oder Eigenschaft ist.
      * Vermeiden Sie den Non-Null-Assertion-Operator (`!!`) wo immer möglich. Seine Verwendung wird dringend abgeraten und erfordert eine ausdrückliche Begründung in einem Kommentar, wenn sie als absolut notwendig erachtet wird.
      * Bevorzugen Sie Safe Calls (`?.`), den Elvis-Operator (`?:`), `let` mit Safe Calls oder andere idiomatische Kotlin-Konstrukte zur Behandlung von nullable Typen.
    * **Explizite Typen für öffentliche APIs:** Öffentlich zugängliche Funktionen, Eigenschaften und Klassenmember (d.h. solche, die nicht `private` oder `internal` sind) müssen explizite Typdeklarationen für Parameter und Rückgabewerte haben. Dies verbessert die Klarheit und Wartbarkeit des Codes, auch wenn der Typ vom Compiler abgeleitet werden könnte. Für `private`- oder `internal`-Member ist die Typinferenz akzeptabel, wenn sie die Lesbarkeit verbessert.
  * **Richtlinie zu `Any`:** Die Verwendung von `kotlin.Any` als Typ für Parameter, Eigenschaften oder Rückgabewerte sollte vermieden werden. Bevorzugen Sie spezifische Typen oder Generics (`<T>`), um Typsicherheit und Klarheit zu wahren.
  * **Typdefinitionen (Ort und Stil):**
    * **Domänenobjekte:** Data-Klassen, Sealed Classes/Interfaces und Enums, die Kerndomänenkonzepte repräsentieren (Aggregate, Entitäten, Value Objects, Domänenevents, Befehle, Abfragen), sollten typischerweise innerhalb des `domain`-Pakets (oder eines Unterpakets davon) ihres jeweiligen Moduls definiert werden (z.B. `eaf-iam/src/main/kotlin/com/axians/accieaf/iam/domain/model/User.kt`).
    * **DTOs (Data Transfer Objects):** DTOs, die für API-Anfrage-/Antwort-Payloads oder zur Datenübertragung zwischen Schichten verwendet werden, sollten klar als Data-Klassen definiert sein. Sie befinden sich typischerweise in `dto`- oder `model`-Unterpaketen innerhalb der relevanten Adapterschicht (z.B. `eaf-controlplane-api/src/main/kotlin/com/axians/accieaf/controlplane/adapter/api/dto/TenantDto.kt`) oder der Anwendungsserviceschicht.
    * **Unveränderlichkeit:** Bevorzugen Sie unveränderliche Typen (Data-Klassen mit `val`-Eigenschaften, `List`, `Set`, `Map` gegenüber ihren veränderlichen Gegenstücken), wo immer dies praktikabel ist.

* **Kommentare & Dokumentation:**
  * **KDoc (Kotlin-Dokumentation):**
    * **Verbindlich für öffentliche APIs:** Alle `public`- und `internal`-Top-Level-Deklarationen (Klassen, Interfaces, Objekte, Funktionen, Eigenschaften) und ihre Member (Konstruktoren, Funktionen, Eigenschaften) müssen eine KDoc-Dokumentation haben.
    * **Inhalt:** KDoc sollte den Zweck des Elements, seine Parameter (`@param`), Rückgabewerte (`@return`) und alle Exceptions, die es auslösen könnte (`@throws`), klar erläutern. Bei Klassen beschreiben Sie deren Verantwortlichkeit und Hauptmerkmale.
    * **Klarheit vor Ausführlichkeit:** Erklären Sie das *Warum* hinter komplexer Logik oder nicht offensichtlichen Designentscheidungen, nicht nur *was* der Code tut (was aus gut geschriebenem, selbstdokumentierendem Code ersichtlich sein sollte).
    * **Redundante Kommentare vermeiden:** Kommentieren Sie keinen offensichtlichen Code (z.B. einfache Getter/Setter, die nur ein Feld holen/setzen).
  * **Inline-Kommentare:** Verwenden Sie Inline-Kommentare (`//`) sparsam, um komplexe oder knifflige Codeabschnitte zu klären, die nicht durch bessere Benennung oder Struktur selbsterklärend gemacht werden können.
  * **`TODO` / `FIXME` Kommentare:** Verwenden Sie Standard-`// TODO:`- oder `// FIXME:`-Kommentare, um Bereiche zu markieren, die zukünftige Aufmerksamkeit erfordern, bekannte Probleme oder temporäre Workarounds. Fügen Sie nach Möglichkeit einen Verweis auf ein Tracking-Element (z.B. JIRA-Ticket-ID), Ihre Initialen und das Datum hinzu. Beispiel: `// TODO (MAX-123, 2025-05-16): Refactor this to use the new FoobarService.`
  * **Modul-`README.md`-Dateien:** Jedes Gradle-Modul (z.B. `eaf-core`, `eaf-iam`) muss eine `README.md`-Datei in seinem Stammverzeichnis haben. Dieses README sollte kurz erklären:
    * Den Zweck und die Hauptverantwortlichkeiten des Moduls.
    * Wichtige architekturelle Entscheidungen oder Muster, die spezifisch für das Modul sind (falls vorhanden).
    * Wie das Modul gebaut und getestet wird (falls es spezifische Anweisungen gibt, die über die Standard-Gradle-Befehle im Stammverzeichnis hinausgehen).
    * Alle wichtigen Abhängigkeiten oder Einrichtungsanweisungen für Entwickler, die an diesem Modul arbeiten.
  * **Architectural Decision Records (ADRs):** Wesentliche architekturelle Entscheidungen, insbesondere solche mit nicht offensichtlichen Kompromissen oder langfristigen Auswirkungen, sollten mithilfe von Architectural Decision Records (ADRs) dokumentiert werden. ADRs sollten in einem dedizierten `docs/adr/`-Verzeichnis im Monorepo gespeichert werden, unter Verwendung eines einfachen Formats (z.B. Markdown mit Feldern wie Titel, Status, Kontext, Entscheidung, Konsequenzen).

* **Abhängigkeitsmanagement:**
  * **Werkzeug:** Gradle mit Versionskatalogen (typischerweise definiert in `gradle/libs.versions.toml` im Projektstammverzeichnis und potenziell referenziert/verwaltet über `build-logic`) ist die alleinige Wahrheitsquelle für alle externen Bibliotheksversionen.
  * **Richtlinie zum Hinzufügen neuer Abhängigkeiten:** Das Hinzufügen neuer externer Abhängigkeiten (insbesondere zu Kern-EAF-Modulen) erfordert sorgfältige Überlegung:
    * **Begründung:** Die Notwendigkeit der Abhängigkeit muss klar formuliert werden. Könnte die Funktionalität mit vorhandenen Abhängigkeiten oder Standardbibliotheksfunktionen von Kotlin/Java erreicht werden?
    * **Alternativenrecherche:** Dokumentieren Sie kurz erwogene Alternativen und warum die gewählte Abhängigkeit bevorzugt wird.
    * **Lizenzkompatibilität:** Überprüfen Sie, ob die Lizenz der Abhängigkeit mit der Gesamtstrategie für die Lizenzierung und dem Vertriebsmodell des ACCI EAF kompatibel ist. Permissive Lizenzen wie Apache 2.0, MIT oder EPL werden im Allgemeinen bevorzugt. GPL/LGPL-Abhängigkeiten erfordern aufgrund ihrer reziproken Natur eine gründliche Überprüfung und Genehmigung.
    * **Sicherheitslücken:** Überprüfen Sie die Abhängigkeit (und ihre transitiven Abhängigkeiten) auf bekannte Sicherheitslücken mit Werkzeugen wie dem OWASP Dependency-Check Gradle-Plugin, Snyk oder GitHub Dependabot-Warnungen.
    * **Reife & Wartung:** Bevorzugen Sie gut gewartete, stabile Bibliotheken aus seriösen Quellen mit einer aktiven Community und guter Dokumentation. Vermeiden Sie veraltete oder nicht gewartete Bibliotheken.
    * **Transitive Abhängigkeiten:** Analysieren Sie die Auswirkungen transitiver Abhängigkeiten, die durch die neue Bibliothek eingeführt werden. Minimieren Sie unnötige transitive Abhängigkeiten.
    * **ppc64le-Kompatibilität:** Stellen Sie bei Bibliotheken, die nativen Code enthalten könnten, die Kompatibilität mit der ppc64le-Zielarchitektur sicher. Reine Java/Kotlin-Bibliotheken sind im Allgemeinen sicher.
    * **Genehmigung:** Für Kern-EAF-Module (`eaf-*`) kann das Hinzufügen einer neuen externen Abhängigkeit eine Überprüfung und Genehmigung durch einen leitenden Architekten oder technischen Leiter erfordern.
  * **Versionierungsstrategie (im Versionskatalog):**
    * **Gepinnte Versionen:** Verwenden Sie spezifische, gepinnte Versionen für alle Abhängigkeiten. Vermeiden Sie dynamische Versionen (z.B. `+`, `latest.release`, Versionsbereiche wie `[1.0, 2.0)`), um reproduzierbare und stabile Builds sicherzustellen.
    * **Regelmäßige Updates:** Planen Sie regelmäßige, kontrollierte Updates von Abhängigkeiten, um Sicherheitspatches und Verbesserungen zu integrieren. Solche Updates müssen gründlich getestet werden.
  * **Abhängigkeitsbereiche (Scopes):** Verwenden Sie geeignete Gradle-Abhängigkeitskonfigurationen (`implementation`, `api`, `compileOnly`, `runtimeOnly`, `testImplementation` usw.), um Compile-Zeit- und Laufzeit-Klassenpfade korrekt zu verwalten und um zu vermeiden, dass transitive Abhängigkeiten unnötig aus den APIs von Modulen offengelegt werden. Verwenden Sie `api` sparsam und nur, wenn ein Modul absichtlich Typen aus einer Abhängigkeit als Teil seiner eigenen öffentlichen API verfügbar macht.

## 14. Gesamt-Teststrategie

Dieser Abschnitt beschreibt die umfassende Teststrategie des Projekts, an die sich der gesamte von KI-generierte und von Menschen geschriebene Code halten muss. Sie ergänzt die im Abschnitt "Definitive Technologie-Stack-Auswahl" ausgewählten Testwerkzeuge und die NFRs bezüglich der Testabdeckung. Ein mehrschichtiger Testansatz wird verfolgt, um die Softwarequalität sicherzustellen.

* **Primäre Testwerkzeuge & Frameworks:**
  * **Backend (Kotlin/JVM):**
    * **Unit- & Integrationstests:** JUnit Jupiter (`5.12.2`), MockK (`1.14.2`) für Mocking, Kotest (`5.9.1`) oder AssertJ für Assertions.
    * **Axon-spezifische Tests:** Axon Test Fixture (Version abgestimmt auf Axon Framework `4.11.2`).
    * **Abhängigkeiten für Integrationstests:** Testcontainers (`1.21.0` für Java) zur Verwaltung von Docker-basierten Abhängigkeiten wie PostgreSQL.
    * **Codeabdeckung:** JaCoCo Gradle-Plugin.
  * **Frontend (Control Plane UI - React):**
    * **Unit- & Komponententests:** Jest (`29.7.0`), React Testing Library (`16.3.x`).
    * **End-to-End (E2E) Tests:** Playwright (`1.52.x`).
  * **CI-Integration:** Alle automatisierten Tests (Unit, Integration, relevante E2E) werden als Teil der CI/CD-Pipeline (GitHub Actions) für jeden Pull-Request und jeden Commit im Hauptbranch ausgeführt. Builds schlagen fehl, wenn Tests fehlschlagen oder die Abdeckung unter definierte Schwellenwerte fällt.

* **Unit-Tests:**
  * **Umfang:** Testen einzelner Funktionen, Methoden, Klassen oder kleiner, isolierter Module (z.B. ein einzelner Spring-Service, die Logik einer Domänenentität, eine Hilfsfunktion, die Befehls-/Ereignishandler eines Axon-Aggregats in Isolation). Der Fokus liegt auf der Überprüfung von Geschäftslogik, Algorithmen, Transformationsregeln und Randbedingungen isoliert von externen Abhängigkeiten.
  * **Speicherort & Benennung (Kotlin - Backend):**
    * Wie in den "Kodierungsstandards" definiert: Unit-Testdateien müssen sich im Verzeichnis `src/test/kotlin/` ihres jeweiligen Moduls befinden. Die Paketstruktur innerhalb von `src/test/kotlin/` muss die Paketstruktur des zu testenden Codes widerspiegeln.
    * Testklassendateien müssen nach der Klasse benannt werden, die sie testen, mit dem Suffix `Test` (z.B. `TenantServiceTest.kt`).
  * **Mocking/Stubbing (Backend):**
    * **MockK** ist die bevorzugte Bibliothek zum Erstellen von Mocks, Stubs und Spies in Kotlin-Unit-Tests.
    * Alle externen Abhängigkeiten (z.B. andere Dienste, Datenbank-Repositories, Netzwerk-Clients, Dateisysteminteraktionen, Systemzeit falls relevant) müssen gemockt oder gestubbt werden, um sicherzustellen, dass Tests isoliert sind und schnell laufen.
    * **Axon Test Fixture:** Wird zum Testen von Axon-Aggregaten verwendet, indem ein Test im Given-When-Then-Stil für Befehlshandler und Event-Sourcing-Logik bereitgestellt wird.
  * **Verantwortung des KI-Agenten:** KI-Agenten, die mit der Codegenerierung oder -änderung beauftragt sind, müssen umfassende Unit-Tests generieren, die alle öffentlichen Methoden neuer/geänderter Klassen, signifikante Logikpfade (einschließlich Happy Paths und Edge Cases) sowie Fehlerbedingungen abdecken.

* **Integrationstests (Backend):**
  * **Umfang:** Testen der Interaktion und Zusammenarbeit zwischen mehreren Komponenten oder Diensten innerhalb der Anwendungsgrenze oder zwischen der Anwendung und externer Infrastruktur, die sie direkt kontrolliert (wie eine Datenbank). Beispiele:
    * API-Endpunkt bis zur Serviceschicht und (Test-)Datenbank.
    * Interaktion zwischen einem Axon Command Handler, Event Store und einem Event Handler, der ein Read Model aktualisiert.
    * Kommunikation zwischen verschiedenen internen EAF-Modulen, wenn sie direkte synchrone oder asynchrone Schnittstellen haben (über die Kerninteraktionen des Event Bus für CQRS hinaus).
  * **Speicherort & Benennung (Backend):**
    * Befinden sich typischerweise in `src/test/kotlin/` neben den Unit-Tests, können aber unterschieden werden durch:
      * Eine spezifische Namenskonvention (z.B. `*IntegrationTest.kt`).
      * Platzierung in einem dedizierten Paket (z.B. `com.axians.accieaf.[module].integration`).
    * Alternativ kann ein separates Gradle-Source-Set (z.B. `src/integrationTest/kotlin`) konfiguriert werden, wenn eine stärkere Trennung gewünscht ist.
  * **Umgebung & Abhängigkeiten:**
    * **Testcontainers:** Verwendung von Testcontainers zur Verwaltung der Lebenszyklen externer Abhängigkeiten wie PostgreSQL-Instanzen für Integrationstests. Dies stellt sicher, dass Tests gegen eine reale Datenbank-Engine in einer sauberen, isolierten Umgebung laufen.
    * Die Testunterstützung von Spring Boot (`@SpringBootTest`) wird verwendet, um Anwendungskontexte für das Testen von Dienstinteraktionen zu laden.
  * **Verantwortung des KI-Agenten:** KI-Agenten können mit der Generierung von Integrationstests für wichtige Dienstinteraktionen oder API-Endpunkte basierend auf definierten Spezifikationen beauftragt werden, insbesondere wenn die Zusammenarbeit von Komponenten kritisch ist.

* **End-to-End (E2E) Tests (Hauptsächlich für Control Plane UI):**
  * **Umfang:** Validieren vollständiger Benutzerabläufe oder kritischer Pfade durch das System aus der Perspektive des Endbenutzers. Für das ACCI EAF gilt dies hauptsächlich für die Control Plane UI und ihre Interaktion mit der `eaf-controlplane-api`.
  * **Werkzeuge:** **Playwright (`1.52.x`)** wird für E2E-Tests der Control Plane UI verwendet.
  * **Testszenarien:** Basierend auf User Stories und Akzeptanzkriterien für die Funktionen der Control Plane UI (z.B. Anmelden, Erstellen eines Mandanten, Zuweisen einer Lizenz, Konfigurieren eines IdP).
  * **Ausführung:** E2E-Tests sind ressourcenintensiver und laufen typischerweise seltener als Unit-/Integrationstests (z.B. nächtliche Builds, Pre-Release-Pipelines), aber kritische Smoke-Tests könnten bei jedem PR ausgeführt werden.
  * **Verantwortung des KI-Agenten:** KI-Agenten können mit der Generierung von E2E-Test-Stubs oder -Skripten (z.B. Playwright Page Object Models, grundlegende Testszenarien) basierend auf User Stories oder UI-Spezifikationen beauftragt werden.

* **Testabdeckung:**
  * **Ziel (gemäß PRD NFR 4a):**
    * Kern-EAF-Module (`eaf-*`) streben eine **100%ige Unit-Testabdeckung für kritische Geschäftslogik** an.
    * Für neue Geschäftslogik, die innerhalb des EAF oder darauf basierender Anwendungen entwickelt wird, wird eine hohe Unit-Testabdeckung von **>80% (Zeilen- und Zweigabdeckung)** angestrebt.
  * **Messung:** Das **JaCoCo** Gradle-Plugin wird verwendet, um die Codeabdeckung für Kotlin/JVM-Code zu messen und zu berichten. Abdeckungsberichte werden als Teil des CI-Builds generiert.
  * **Qualität vor Quantität:** Obwohl Abdeckungsziele wichtig sind, stehen die Qualität, Relevanz und Effektivität der Tests an erster Stelle. Tests müssen aussagekräftig sein und das tatsächliche Verhalten und die Anforderungen überprüfen.

## 15. Sicherheits-Best-Practices

Sicherheit ist ein vorrangiges Anliegen für das ACCI EAF und die darauf aufbauenden Anwendungen. Die folgenden Best Practices sind verbindlich und müssen von allen Entwicklern (menschliche und KI-Agenten) während des gesamten Entwicklungszyklus aktiv berücksichtigt werden. Diese Praktiken zielen darauf ab, häufige Schwachstellen (einschließlich der in den OWASP Top 10 genannten) zu mindern und die Einhaltung der relevanten NFRs sicherzustellen.

* **Eingabebereinigung und -validierung (OWASP A03:2021-Injection):**
  * **API-Eingabevalidierung:** Alle Daten, die von externen Quellen (API-Anforderungs-Payloads, Abfrageparameter, Header) von einer beliebigen EAF-API (z.B. `eaf-controlplane-api`, `eaf-license-server`) empfangen werden, müssen an der Grenze vor der Verarbeitung streng validiert werden.
    * Nutzen Sie die Spring Validation API (Bean Validation mit JSR 303/380-Annotationen wie `@Valid`, `@NotNull`, `@NotEmpty`, `@Size`, `@Pattern`, benutzerdefinierte Validatoren) für DTOs in `@RestController`-Methoden.
    * Die Validierung sollte Datentypen, Formate, Längen, Bereiche und zulässige Zeichensätze abdecken.
  * **Kontextbezogenes Escaping/Encoding für andere Eingaben:** Daten aus weniger direkten Quellen (z.B. Konfigurationsdateien, Datenbank, Nachrichten von anderen internen Systemen), die in potenziell riskanten Operationen verwendet werden könnten (wie das Erstellen von Protokollnachrichten, Abfragen oder Dateipfaden), sollten sorgfältig behandelt werden, unter Verwendung geeigneter Escaping- oder Encoding-Verfahren, wenn sie jemals in Ausgaben reflektiert oder in sensiblen Senken verwendet werden.
  * **Verhinderung von Log-Injection:** Stellen Sie beim Protokollieren von benutzerdefinierten oder externen Daten sicher, dass diese vom strukturierten Logging-Framework ordnungsgemäß behandelt werden, um Log-Fälschung oder die Injektion bösartiger Zeichen (z.B. CRLF-Injection) zu verhindern. Parametrisierte Protokollierung hilft dabei erheblich.

* **Ausgabe-Encoding (OWASP A03:2021-Injection, speziell XSS):**
  * **JSON-APIs:** Für REST-APIs wie `eaf-controlplane-api` und `eaf-license-server`, die JSON zurückgeben, handhabt Spring Boot mit Jackson automatisch das korrekte JSON-Encoding, was XSS-Schwachstellen innerhalb eines JSON-Kontexts mindert. Stellen Sie sicher, dass die Content-Typen korrekt auf `application/json` gesetzt sind.
  * **Control Plane UI (React):** Die React-basierte Control Plane UI ist für ihre eigene XSS-Prävention verantwortlich. Das beinhaltet:
    * Nutzung der standardmäßigen JSX-String-Kodierung von React.
    * Vermeidung der direkten Verwendung von `dangerouslySetInnerHTML` mit nicht bereinigtem, vom Benutzer bereitgestelltem Inhalt.
    * Verwendung geeigneter, geprüfter Bibliotheken zum Rendern komplexer Inhalte wie Markdown, wenn dieser von Benutzern stammen kann.
  * **Daten an die UI:** Obwohl die Backend-API JSON bereitstellt, sollten alle Daten, die aus Benutzereingaben stammen und an eine UI zurückgesendet werden, von der UI weiterhin als potenziell nicht vertrauenswürdig behandelt und mit geeigneten Ausgabe-Encoding- oder Bereinigungsmechanismen innerhalb des UI-Frameworks gehandhabt werden.

* **Geheimnisverwaltung:**
  * **Keine hartcodierten Geheimnisse:** Geheimnisse (Passwörter, API-Schlüssel, Client-Secrets, Verschlüsselungsschlüssel, Datenbankanmeldeinformationen) dürfen **niemals** im Quellcode hartcodiert, in die Versionskontrolle eingecheckt oder in Build-Artefakte aufgenommen werden.
  * **Externalisierte Konfiguration:** Geheimnisse müssen extern verwaltet und der Anwendung zur Laufzeit über die externalisierten Konfigurationsmechanismen von Spring Boot bereitgestellt werden. Für Docker Compose-Deployments auf Kunden-VMs bedeutet dies typischerweise:
    * Verwendung einer `.env`-Datei (außerhalb der Versionskontrolle und auf der VM gesichert), die Docker Compose als Umgebungsvariablen in die Container injiziert.
    * Direktes Mounten von Geheimnisdateien in Container (z.B. über Docker Compose `secrets` oder Volume Mounts) und Konfiguration von Spring Boot, um diese zu lesen.
  * **Protokollierung:** Stellen Sie sicher, dass Geheimnisse niemals protokolliert werden. Konfigurieren Sie bei Bedarf Log-Maskierung für bekannte Geheimnismuster, obwohl die Vermeidung der Protokollierung an erster Stelle steht.
  * **`eaf-iam` Client Secrets:** Client Secrets für von `eaf-iam` generierte Dienstkonten müssen sicher gehandhabt, nur bei der Erstellung zurückgegeben und innerhalb des EAF gehasht gespeichert werden.

* **Abhängigkeitssicherheit & Software Bill of Materials (SBOM):**
  * **Schwachstellen-Scanning:** Implementieren Sie automatisches Scannen von Abhängigkeitsschwachstellen mit Werkzeugen wie dem **OWASP Dependency-Check Gradle-Plugin**. Dieser Scan muss in die CI/CD-Pipeline integriert und so konfiguriert werden, dass der Build fehlschlägt, wenn kritische oder schwerwiegende Schwachstellen in Projektabhängigkeiten oder deren transitiven Abhängigkeiten erkannt werden.
  * **Regelmäßige Updates:** Überprüfen und aktualisieren Sie Abhängigkeiten regelmäßig auf ihre neuesten sicheren Versionen, gemäß der in "Abhängigkeitsmanagement" definierten Richtlinie.
  * **SBOM-Generierung & -Überprüfung (PRD NFR 9):**
    * Generieren Sie automatisch eine SBOM (Software Bill of Materials) in einem Standardformat (z.B. CycloneDX, SPDX) für jede EAF-Version und für darauf aufbauende Anwendungen. Dies wird Teil der CI/CD-Pipeline sein.
    * Etablieren Sie einen Prozess zur kontinuierlichen Überprüfung dieser SBOMs auf Lizenzkonformität und neu entdeckte Schwachstellen in Drittanbieterkomponenten (z.B. mit OWASP Dependency Track oder ähnlichen Werkzeugen).

* **Authentifizierungs- und Autorisierungsprüfungen (über `eaf-iam`):**
  * **Authentifizierung erzwingen:** Alle API-Endpunkte (außer möglicherweise einige wenige explizit öffentliche wie Health Checks, falls vorhanden) müssen eine robuste Authentifizierung unter Verwendung von Mechanismen erzwingen, die von `eaf-iam` bereitgestellt oder integriert werden (z.B. tokenbasiert für API-Clients, OIDC/SAML für Benutzer über die Control Plane UI). Dies wird über Spring Security integriert.
  * **Autorisierung erzwingen:** Die Autorisierung (Berechtigungs-/Rollenbasierte Zugriffskontrolle - RBAC) basierend auf Definitionen innerhalb von `eaf-iam` muss für alle geschützten Ressourcen und Operationen erzwungen werden. Dies sollte auf der Serviceschicht oder den API-Eingangspunkten erfolgen, unter Nutzung der Methodensicherheit von Spring Security (`@PreAuthorize`, `@PostAuthorize`) oder feingranularer Prüfungen innerhalb der Geschäftslogik, wo dies angemessen ist.

* **Prinzip der geringsten Rechte (Principle of Least Privilege):**
  * **Datenbankbenutzer:** Dem/den von den ACCI EAF-Anwendungen genutzten PostgreSQL-Benutzerkonto/-konten (z.B. `eaf-controlplane-api`, `eaf-license-server`, Axon Event Store-Benutzer) dürfen nur die minimal notwendigen DML/DDL-Berechtigungen (SELECT, INSERT, UPDATE, DELETE auf bestimmte Tabellen/Schemata) gewährt werden, die für ihren Betrieb erforderlich sind. Vermeiden Sie die Verwendung von PostgreSQL-Superuser-Konten für die Anwendungs-Laufzeit.
  * **Betriebssystem-Dienstkonten (Docker-Kontext):** Docker-Container sollten so konfiguriert werden, dass sie nach Möglichkeit mit Nicht-Root-Benutzern ausgeführt werden. Definieren Sie spezifische Benutzer in Dockerfiles.
  * **Anwendungsberechtigungen:** Innerhalb der Anwendungslogik sollten Komponenten nur Zugriff auf die Daten und Operationen haben, die für ihre spezifische Funktion notwendig sind.

* **API-Sicherheit (Allgemein):**
  * **Ausschließlich HTTPS:** Jegliche externe API-Kommunikation (zu und von `eaf-controlplane-api`, `eaf-license-server`) muss über HTTPS erfolgen. Die SSL/TLS-Terminierung kann von einem Reverse-Proxy (z.B. Nginx/Traefik-Container innerhalb des Docker Compose-Stacks) gehandhabt oder direkt in Spring Boot konfiguriert werden (seltener für Edge-Traffic, aber möglich).
  * **Ratenbegrenzung & Drosselung:** Implementieren Sie Ratenbegrenzung für alle öffentlich zugänglichen API-Endpunkte, um sich vor Denial-of-Service (DoS)-Angriffen und API-Missbrauch zu schützen. Dies kann mit Bibliotheken wie Resilience4j (`RateLimiter`) innerhalb von Spring Boot-Anwendungen oder über einen Reverse-Proxy implementiert werden.
  * **HTTP-Sicherheitsheader:** Konfigurieren Sie geeignete HTTP-Sicherheitsheader, die von API-Antworten zurückgegeben werden, um die Browsersicherheit für die Control Plane UI zu verbessern. Dazu gehören:
    * `Strict-Transport-Security (HSTS)`
    * `X-Content-Type-Options: nosniff`
    * `X-Frame-Options: DENY` (oder `SAMEORIGIN`)
    * `Content-Security-Policy (CSP)` (kann komplex sein, ist aber sehr effektiv)
    * `X-XSS-Protection` (obwohl weitgehend durch CSP abgelöst)
        Diese können in Spring Security oder dem Reverse-Proxy konfiguriert werden.
  * **Eingabevalidierung:** (Wiederholt) Entscheidend zur Verhinderung von Injection-Angriffen und zur Gewährleistung der Datenintegrität.

* **Fehlerbehandlung & Informationsweitergabe:**
  * Wie in der "Fehlerbehandlungsstrategie" definiert: Stellen Sie sicher, dass Fehlermeldungen, die an API-Clients zurückgegeben oder in UIs angezeigt werden, **keine** sensiblen internen Systeminformationen preisgeben (z.B. Stacktraces, detaillierte SQL-Fehlermeldungen, interne Dateipfade, Bibliotheksversionen).
  * Protokollieren Sie detaillierte technische Fehler serverseitig für Diagnosezwecke. Stellen Sie dem Client generische, benutzerfreundliche Fehlermeldungen oder Korrelations-IDs zur Verfügung.

* **Regelmäßige Sicherheitsaudits & -tests (PRD NFR 2f):**
  * **Interne Code-Reviews:** Sicherheitsaspekte müssen Teil regelmäßiger Code-Reviews sein.
  * **Penetrationstests:** Planen und führen Sie externe Penetrationstests für das EAF und kritische EAF-basierte Anwendungen vor wichtigen Produktions-Deployments oder größeren Releases durch.
  * **SAST/DAST:** Erwägen Sie die Integration von Static Application Security Testing (SAST)-Werkzeugen in die CI/CD-Pipeline. Dynamic Application Security Testing (DAST) kann auf laufenden Anwendungen in Testumgebungen durchgeführt werden.

* **Andere relevante Sicherheitspraktiken:**
  * **Datenverschlüsselung:**
    * **In Transit:** HTTPS ist für die externe Kommunikation obligatorisch. Die Kommunikation zwischen Containern innerhalb des Docker Compose-Stacks auf demselben Host (z.B. API zu Datenbank) kann das Docker-Netzwerk verwenden; wenn dieses Netzwerk nicht als vollständig vertrauenswürdig gilt oder wenn die Compliance dies erfordert, sollte Inter-Service-TLS in Betracht gezogen werden.
    * **At Rest:**
      * Sensible Konfigurationsdaten (z.B. Geheimnisse in `.env`-Dateien auf der VM) müssen durch geeignete VM-Host-Sicherheit und Dateiberechtigungen geschützt werden (Verantwortung des Kunden).
      * Für in PostgreSQL gespeicherte Daten (z.B. gehashte Passwörter in `eaf-iam`, sensible Audit-Log-Einträge) sollten Optionen wie Transparent Data Encryption (TDE) von PostgreSQL über Erweiterungen (z.B. `pgcrypto` für Spaltenverschlüsselung bei Bedarf, obwohl dies die Komplexität erhöht) oder die Verschlüsselung auf Dateisystemebene auf dem VM-Host (Verantwortung des Kunden) in Betracht gezogen werden.
    * **FIPS 140-2/3-Konformität (PRD NFR 2c):** Alle kryptografischen Operationen (Passwort-Hashing, JWT-Signierung/-Validierung, TLS-Konfiguration usw.) müssen unter Verwendung von Bibliotheken und JVM-Konfigurationen durchgeführt werden, die FIPS 140-2/3-validierte kryptografische Module unterstützen oder verwenden. Dies erfordert eine sorgfältige Auswahl und Konfiguration der JVM (z.B. IBM Semeru Runtimes im FIPS-Modus oder OpenJDK mit einem FIPS-zertifizierten Anbieter wie BouncyCastle-FIPS) und kryptografischer Bibliotheken.
  * **Sitzungsverwaltung (Control Plane UI):**
    * Wenn traditionelle Sitzungs-Cookies vom Authentifizierungsmechanismus der UI verwendet werden (obwohl tokenbasiert über API mit React wahrscheinlicher ist), stellen Sie sicher, dass sie sicher konfiguriert sind: `HttpOnly`, `Secure` (wenn UI HTTPS ist), `SameSite`-Attribute.
    * Verwenden Sie für die tokenbasierte Authentifizierung (z.B. von `eaf-iam` ausgestellte und von der UI konsumierte JWTs) kurzlebige Zugriffstoken und sichere Mechanismen zum Speichern und Aktualisieren von Token, wenn Aktualisierungstoken verwendet werden.
  * **Audit-Protokollierung (`eaf-observability`):**
    * Stellen Sie sicher, dass umfassende und unveränderliche Audit-Protokolle alle sicherheitsrelevanten Ereignisse erfassen: erfolgreiche/fehlgeschlagene Anmeldungen, Abmeldungen, wichtige administrative Aktionen (Mandantenerstellung, Benutzeränderung, Rollenänderungen, Lizenzzuweisungen), Zugriff auf sensible Daten, Änderungen an Sicherheitskonfigurationen.
    * Schützen Sie Audit-Protokolle vor unbefugtem Zugriff und Manipulation. Stellen Sie sicher, dass sie ausreichende Details enthalten (Zeitstempel, Benutzer, Aktion, Ergebnis, relevante Ressourcen-IDs).

## 16. Wichtige Referenzdokumente

Dieser Abschnitt listet wichtige Dokumente auf, die entweder Eingaben für diese Architektur sind, weitere Details zu spezifischen Aspekten liefern oder als ergänzende Artefakte erstellt werden.

1. **ACCI EAF Product Requirements Document (PRD):**
    * *Speicherort:* `docs/ACCI-EAF-PRD.md` (oder der vom Produktmanagement bereitgestellte Speicherort)
    * *Beschreibung:* Das primäre Dokument, das die funktionalen und nicht-funktionalen Anforderungen, Ziele, den Kontext, User Stories (Epics) und zentrale technische Entscheidungen für das ACCI EAF detailliert beschreibt. Dieses Architekturdokument basiert grundlegend auf dem PRD.

2. **Offizielle Kotlin Coding Conventions:**
    * *Speicherort:* [https://kotlinlang.org/docs/coding-conventions.html](https://kotlinlang.org/docs/coding-conventions.html)
    * *Beschreibung:* Der offizielle Styleguide für die Kotlin-Entwicklung, der die Grundlage der in diesem Dokument detaillierten "ACCI Kotlin Coding Standards v1.0" bildet.

3. **ACCI EAF API-Spezifikationen (OpenAPI):**
    * *Speicherort (Geplant):*
        * `docs/api/controlplane-v1.yml` (oder `.json`)
        * `docs/api/licenseserver-v1.yml` (oder `.json`)
    * *Beschreibung:* Detaillierte OpenAPI 3.x-Spezifikationen für die von `eaf-controlplane-api` und `eaf-license-server` bereitgestellten RESTful APIs. Diese werden während der Entwicklung generiert/gepflegt.

4. **Architectural Decision Records (ADRs):**
    * *Speicherort (Geplant):* `docs/adr/`
    * *Beschreibung:* Eine Sammlung von Aufzeichnungen, die wichtige architekturelle Entscheidungen, ihren Kontext, Kompromisse und Konsequenzen dokumentieren. *(Die Projektstruktur wurde um dieses Verzeichnis erweitert.)*

5. **Frontend-Architekturdokument (Control Plane UI - falls separat erstellt):**
    * *Speicherort (Hypothetisch):* `docs/ACCI-EAF-Frontend-Architecture.md`
    * *Beschreibung:* Falls ein separates detailliertes Architekturdokument für die React-basierte Control Plane UI vom Design-Architekten erstellt wird, würde es hier referenziert werden. Dieses Dokument würde die Komponentenstruktur, das detaillierte Zustandsmanagement, spezifische UI-Bibliotheksverwendungsmuster usw. basierend auf den Richtlinien dieses Hauptarchitekturdokuments und dem "Prompt für Design-Architekt" (siehe unten) ausarbeiten.

## 17. Änderungsprotokoll

| Änderung                                   | Datum            | Version | Beschreibung                                 | Autor           |
| :----------------------------------------- | :--------------- | :------ | :------------------------------------------- | :-------------- |
| Erster Entwurf des Architekturdokuments    | 16. Mai 2025     | 0.1.0   | Erster vollständiger Entwurf basierend auf PRD und Inputs | Architect Agent |
|                                            |                  |         |                                              |                 |

*(Dieses Protokoll wird aktualisiert, während sich die Architektur weiterentwickelt.)*
