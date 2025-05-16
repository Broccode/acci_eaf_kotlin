Okay, verstanden. Sie möchten das vollständige PRD-Dokument, inklusive aller detaillierten Epics und User Stories, nun wieder auf Deutsch.

Ich werde die englische Version, die ich Ihnen gerade bereitgestellt habe, sorgfältig ins Deutsche zurückübersetzen, wobei ich sicherstelle, dass alle Details, einschließlich der erweiterten Akzeptanzkriterien für die User Stories, korrekt übertragen werden.

Bitte haben Sie erneut etwas Geduld, da dies eine umfangreiche Übersetzung ist.

---

# ACCI EAF (Axians Competence Center Infrastructure Enterprise Application Framework) Product Requirements Document (PRD)

## Ziel, Zweck und Kontext (Goal, Objective and Context)

* **Ziel des EAF:** Interne Nutzung zur Beschleunigung der Entwicklung und Standardisierung von Enterprise-Softwareprodukten, die externen Kunden bereitgestellt werden. Die Entwicklung mit dem ACCI EAF zielt darauf ab, das veraltete, nicht mehr erweiterbare und performancemäßig limitierte interne "DCA"-Framework abzulösen.
* **Fokus & Wertversprechen:**
  * **Für das ACCI-Entwicklungsteam:** Signifikante Zeit- und Kostenersparnis bei der Entwicklung neuer Produkte und Features, verbesserte Wartbarkeit und Testbarkeit durch moderne Technologien und Architekturen sowie eine verbesserte Developer Experience. "Befreiung" von den Limitierungen des Altsystems.
  * **Für Endkunden von ACCI-Produkten:** Erheblicher Zugewinn an modernen Features (z.B. Mandantenfähigkeit, erweiterte Sicherheit, flexibles Lizenzmanagement, Internationalisierung), verbesserte Performance und eine wesentlich modernere User Experience der auf dem EAF basierenden Produkte.
* **Kontext:** Das ACCI EAF wird vom Axians Competence Center Infrastructure Team entwickelt und primär für Softwareprodukte im Enterprise-Segment eingesetzt, die auf IBM Power Architecture (ppc64le) bei Kunden betrieben werden, oft in Umgebungen ohne direkten Internetzugriff.

## [OPTIONAL: For Simplified PM-to-Development Workflow Only] Kern-Technologieentscheidungen & Anwendungsstruktur (Core Technical Decisions & Application Structure)

Dieser Abschnitt dokumentiert die grundlegenden technischen Entscheidungen und die geplante Anwendungsstruktur für das ACCI EAF.

**1. Kern-Technologie-Stack (Core Technology Stack):**

* **Programmiersprache/Plattform:** Kotlin (laufend auf der Java Virtual Machine - JVM)
* **Kern-Framework (Anwendungsschicht):** Spring Boot
* **Architektur-Framework (für DDD, CQRS, ES):** Axon Framework
* **Datenbank (primär für Read Models, Zustandsdaten und als Event Store):** PostgreSQL
* **Build-Werkzeug (Build Tool):** Gradle

**2. Repository-Struktur (Repository Structure):**

* **Ansatz:** Monorepo
  * Alle Module und die zugehörige Build-Logik des ACCI EAF werden in einem einzigen Git-Repository verwaltet.

**3. Anwendungsstruktur (Module & Verantwortlichkeiten) (Application Structure (Modules & Responsibilities)):**

Das ACCI EAF wird als modularer Monolith mit den folgenden Hauptmodulen (Gradle-Unterprojekten) strukturiert:

* **`build-logic`**:
  * *Verantwortlichkeit:* Enthält die zentrale Build-Logik, Abhängigkeitsversionen (Dependency Management über Gradle Version Catalogs) und Konventionen für alle Module im Monorepo.
* **Framework-Module (Kern des ACCI EAF):**
  * **`eaf-core`**:
    * *Verantwortlichkeit:* Stellt die grundlegenden Bausteine, Kernabstraktionen (z.B. für Aggregate, Commands, Events), gemeinsame Utilities und die Basiskonfiguration für CQRS/ES unter Verwendung des Axon Frameworks bereit. Bildet die Grundlage für Anwendungen, die das EAF nutzen.
  * **`eaf-iam` (Identity & Access Management)**:
    * *Verantwortlichkeit:* Implementiert die Funktionalitäten für Benutzerverwaltung, Authentifizierung (lokal und extern via LDAP/AD, OIDC, SAML2) und Autorisierung (RBAC, mit Vorbereitung für ABAC) als wiederverwendbares Framework-Modul, inklusive Unterstützung für Service-Accounts.
  * **`eaf-multitenancy`**:
    * *Verantwortlichkeit:* Stellt die Logik für die Mandantenfähigkeit bereit, inklusive Mechanismen zur Tenant-Isolation (z.B. über Row-Level Security in PostgreSQL) und zur Verwaltung des Tenant-Kontextes innerhalb der Anwendung.
  * **`eaf-licensing`**:
    * *Verantwortlichkeit:* Bietet Funktionen für das Lizenzmanagement von Anwendungen, die mit dem ACCI EAF erstellt werden (z.B. zeitlich begrenzt, hardwaregebunden, Offline-/Online-Aktivierung).
  * **`eaf-observability`**:
    * *Verantwortlichkeit:* Stellt standardisierte Konfigurationen und Werkzeuge für Logging (strukturiert), Metriken (Prometheus-Export via Micrometer), Health Checks (Spring Boot Actuator) und dediziertes Audit-Logging bereit.
  * **`eaf-internationalization` (i18n)**:
    * *Verantwortlichkeit:* Stellt Werkzeuge, Konventionen und eine Basisinfrastruktur für die Internationalisierung und Lokalisierung von Anwendungen bereit, inklusive mandantenspezifischer Sprach- und Übersetzungsverwaltung.
  * **`eaf-plugin-system`**:
    * *Verantwortlichkeit:* Enthält die Implementierung der Plugin-Infrastruktur (basierend auf der Java ServiceLoader API), die es ermöglicht, das EAF und darauf basierende Anwendungen modular durch definierte Schnittstellen zu erweitern.
* **Optionale/Ergänzende Module:**
  * **`eaf-cli`**:
    * *Verantwortlichkeit:* Entwicklung von Kommandozeilenwerkzeugen (CLIs) für Entwickler, die das EAF nutzen (z.B. für Projekt-Scaffolding, Codegenerierung, Diagnose).
  * **`app-example-module`**:
    * *Verantwortlichkeit:* Dient als Referenzimplementierung und Schnellstartanleitung. Zeigt, wie eine typische Geschäftsanwendung oder ein spezifisches Domänenmodul unter Verwendung der ACCI EAF-Komponenten und Best Practices entwickelt wird.
  * **`eaf-controlplane-api`**: (Backend für die Control Plane UI)
    * *Verantwortlichkeit:* Stellt die RESTful APIs für die Control Plane UI bereit (Mandanten-, Benutzer-, Lizenz-, i18n-Verwaltung).
  * **`eaf-license-server`**: (Als EAF-basierte Anwendung)
    * *Verantwortlichkeit:* Stellt die serverseitige Logik für die Online-Lizenzaktivierung und -validierung bereit.

## Funktionale Anforderungen (MVP) (Functional Requirements (MVP))

Die funktionalen Anforderungen für das MVP des ACCI EAF sind in 10 Epics strukturiert. Jedes Epic ist mit User Stories und deren Akzeptanzkriterien detailliert, unter Berücksichtigung von Randfällen, Fehlerbehandlung und Best Practices.

**Epic 1: EAF Grundgerüst & Kerninfrastruktur (EAF Foundational Setup & Core Infrastructure)**
*Beschreibung:* Erstellt die initiale Projektstruktur des ACCI EAF im Gradle-Monorepo, inklusive `build-logic`, Kernkonfigurationen, CI/CD-Pipeline-Setup (initial für eine einfache Zielumgebung) und das Basis-`eaf-core`-Modul mit initialer Axon Framework Konfiguration. Dieses Epic liefert ein lauffähiges, wenn auch funktional noch leeres, Framework-Grundgerüst.
*Wert:* Fundament für alle weitere Entwicklung, ermöglicht frühes CI/CD und Validierung der Zielumgebung.

**Story 1.1: Monorepo Projekt-Setup mit Gradle (Monorepo Project Setup with Gradle)**

* **Als** Entwickler **möchte ich** ein neues Gradle Monorepo für das ACCI EAF Projekt initialisiert haben, **damit** alle EAF-Module zentral und konsistent verwaltet werden können.
* **Akzeptanzkriterien (ACs):**
    1. Ein Git-Repository für das ACCI EAF Projekt ist lokal initialisiert und eine umfassende `.gitignore`-Datei (die typische IDE-, OS-, Build-Artefakte und sensible Dateien ignoriert) ist vorhanden.
    2. Ein Gradle-Root-Projekt ist mit einer `settings.gradle.kts`-Datei eingerichtet, die `build-logic` und das initiale `eaf-core`-Modul als Unterprojekte deklariert. Die Gradle-Version ist auf eine aktuelle, stabile Version festgelegt.
    3. Ein `build-logic`-Modul ist mit einer grundlegenden Struktur für Gradle Convention-Plugins oder Version Catalogs erstellt, um geteilte Build-Konfigurationen (z.B. Java/Kotlin Version, Compiler-Optionen, Standard-Plugins wie Checkstyle/Klint) und Abhängigkeitsversionen zentral und konsistent zu verwalten.
    4. Das Modul `eaf-core` ist als valides Gradle-Unterprojekt (Kotlin-basiert) erstellt und so konfiguriert, dass es Konfigurationen und Abhängigkeiten aus `build-logic` nutzt.
    5. Der Befehl `./gradlew build` (unter Verwendung des Gradle Wrappers) wird erfolgreich ausgeführt, ohne Kompilierungsfehler oder signifikante Warnungen, die auf Fehlkonfigurationen hindeuten. Alle definierten Linting- und Style-Checks (falls initial konfiguriert) werden bestanden. Es werden erwartete Build-Artefakte erzeugt (z.B. leere JAR-Dateien, falls zutreffend).
    6. Die Gradle Wrapper-Dateien (`gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar`, `gradle-wrapper.properties`) sind korrekt konfiguriert, im Repository vorhanden und lauffähig, um konsistente Builds über verschiedene Entwicklungsumgebungen hinweg sicherzustellen. Der Wrapper ist so konfiguriert, dass er eine spezifische Gradle-Version verwendet, die für das Projekt geeignet ist.
    7. Eine grundlegende `README.md`-Datei ist im Root-Verzeichnis vorhanden und enthält den Projektnamen, eine kurze Beschreibung des EAF, Voraussetzungen für die Entwicklungsumgebung (JDK-Version, etc.) und grundlegende Anweisungen zum Klonen, Bauen und Ausführen des Projekts.
    8. Das Setup ist auf einer sauberen Entwicklungsumgebung (mit korrekt installiertem JDK gemäß Projektanforderungen und ohne globale Gradle-Installationen, die interferieren könnten) nachvollziehbar und führt zu einem erfolgreichen Build.

**Story 1.2: Basis `eaf-core`-Modul mit Spring Boot & Axon Konfiguration (Basic `eaf-core` Module with Spring Boot & Axon Configuration)**

* **Als** Entwickler **möchte ich**, dass das `eaf-core`-Modul eine lauffähige Spring Boot Anwendung mit initialer Axon Framework Konfiguration ist, **damit** die grundlegenden CQRS/ES-Fähigkeiten etabliert und getestet werden können.
* **Akzeptanzkriterien (ACs):**
    1. Das `eaf-core`-Modul enthält die notwendigen Abhängigkeiten für Spring Boot (z.B. `spring-boot-starter-web` für Web-Fähigkeiten, `spring-boot-starter-actuator` für Monitoring-Endpunkte), verwaltet über `build-logic`.
    2. Das `eaf-core`-Modul enthält die Kern-Abhängigkeiten des Axon Frameworks (z.B. `axon-spring-boot-starter`), verwaltet über `build-logic`. Die Axon-Version ist auf eine aktuelle, stabile Version festgelegt.
    3. Eine grundlegende Axon-Konfiguration ist vorhanden: Command Bus, Query Bus und Event Bus/Event Store (initial konfiguriert für In-Memory-Betrieb für einfache Tests oder eine Basis-PostgreSQL-Anbindung, falls direkt umsetzbar) sind als Spring Beans korrekt initialisiert. Fehler bei der Initialisierung dieser Axon-Komponenten (z.B. fehlende Konfiguration, Verbindungsprobleme zum Event Store) führen zu aussagekräftigen Fehlermeldungen beim Anwendungsstart und einem fehlschlagenden Health-Check.
    4. Die `eaf-core`-Anwendung kann mittels `./gradlew :eaf-core:bootRun` erfolgreich und ohne Laufzeitfehler gestartet werden.
    5. Der Spring Boot Actuator Health-Endpunkt (`/actuator/health`) ist verfügbar, meldet den Status `UP` und beinhaltet Basiszustandsinformationen für Axon-Komponenten (z.B. Konnektivität zum Event Store, Status der Event-Prozessoren falls bereits welche definiert sind).
    6. Ein einfacher Test-Command (z.B. `PingCommand`), ein zugehöriger Command Handler (der z.B. ein `PongEvent` erzeugt) und ein Event Handler (der das `PongEvent` z.B. im Log ausgibt) sind im `eaf-core`-Modul implementiert. Das Senden des Commands (z.B. über einen Test-REST-Endpunkt oder einen Integrationstest) führt zur erwarteten Event-Publikation und -Verarbeitung. Fehler bei der Command-Verarbeitung (z.B. kein Handler gefunden, Validierungsfehler im Handler) führen zu einer definierten, nachvollziehbaren Fehlerantwort (z.B. standardisierte JSON-Fehlerstruktur mit Fehlercode und Meldung) oder einer protokollierten Exception.
    7. Die `application.properties` oder `application.yml` Datei im `eaf-core`-Modul ist strukturiert (z.B. Nutzung von Spring Profilen für `dev`, `test`, `prod`), enthält Platzhalter oder umgebungsspezifische Konfigurationen für Axon, Datenbankverbindung etc. und ist gut kommentiert.
    8. Eine grundlegende Logging-Konfiguration (z.B. via Logback, konfiguriert in `eaf-observability` und genutzt von `eaf-core`) ist vorhanden und gibt beim Start und bei der Ausführung des Test-Commands/Events aussagekräftige, strukturierte (z.B. JSON) Log-Informationen aus, inklusive Zeitstempel, Log-Level, Thread-Name und Logger-Name.

**Story 1.3: Initiale CI/CD-Pipeline Einrichtung (Initial CI/CD Pipeline Setup)**

* **Als** Entwicklungsteam **möchte ich** eine grundlegende CI/CD-Pipeline für das ACCI EAF Monorepo konfiguriert haben, **damit** Code-Änderungen automatisch gebaut, getestet (initial Unit-Tests und Code-Qualitätschecks) und ein deploybares Artefakt (z.B. Docker-Image für `eaf-core`) erzeugt wird.
* **Akzeptanzkriterien (ACs):**
    1. Die CI-Pipeline (z.B. GitHub Actions Workflow, Jenkinsfile, GitLab CI YAML) ist im Monorepo als Code versioniert und wird bei jedem Push auf den Haupt-Branch (z.B. `main` oder `develop`) sowie bei jedem Pull/Merge Request auf diesen Branch automatisch ausgelöst.
    2. Die Pipeline führt den Befehl `./gradlew build cleanCheck` (oder äquivalente Gradle-Tasks) aus, um alle Module zu kompilieren, statische Code-Analyse (Linting, Style-Checks via Checkstyle/Klint) durchzuführen und Unit-Tests auszuführen. Die Pipeline schlägt fehl, wenn Tests fehlschlagen oder konfigurierte Qualitäts-Gates (z.B. Mindest-Testabdeckung, keine kritischen Code-Smells) nicht erfüllt sind. Detaillierte Testergebnisse und Analyseberichte sind als Artefakte der Pipeline verfügbar.
    3. Die Pipeline generiert versionierte Build-Artefakte (z.B. JAR-Dateien für die Module), die eine Manifest-Datei mit Build-Informationen (z.B. Git-Commit-ID, Build-Zeitstempel, semantische Version basierend auf Git-Tags oder Build-Parametern) enthalten.
    4. (Optional für initiales Setup, aber empfohlen) Die Pipeline baut ein Docker-Image für das `eaf-core`-Modul unter Verwendung eines Multi-Stage Dockerfiles (optimiert für minimale Größe, Nutzung eines Non-Root-Users, korrekte Layer-Struktur). Das Image wird mit der Build-Version getaggt und in eine interne Docker-Registry veröffentlicht (Authentifizierung zur Registry erfolgt sicher über Secrets). Ein Basis-Vulnerability-Scan des Docker-Images (z.B. mit Trivy) wird durchgeführt und kritische Funde führen zum Fehlschlag der Pipeline.
    5. Der Status der Pipeline (Erfolg/Fehlschlag jedes Schrittes) wird klar im entsprechenden CI/CD-System und idealerweise auch im Git-Repository (z.B. als Commit-Status oder Pull-Request-Check) signalisiert. Benachrichtigungen bei Fehlschlägen werden an das Entwicklungsteam gesendet.
    6. Detaillierte und strukturierte Logs der Pipeline-Ausführung sind für mindestens 7 Tage zugänglich, um eine effiziente Fehlersuche zu ermöglichen.
    7. Die Pipeline ist so konfiguriert, dass sie Caching-Mechanismen (z.B. für Gradle-Abhängigkeiten, Docker-Layer) nutzt, um die Build-Zeiten zu optimieren, ohne die Korrektheit zu beeinträchtigen.
    8. Die Pipeline behandelt Fehler bei einzelnen Schritten (z.B. temporärer Ausfall der Docker-Registry) robust (z.B. durch Retry-Mechanismen für bestimmte Operationen) und meldet diese klar.

---

**Epic 2: Kernimplementierung Mandantenfähigkeit (Core Multitenancy Implementation)**
*Beschreibung:* Implementiert die grundlegenden Fähigkeiten zur Mandantenfähigkeit in den Modulen `eaf-multitenancy` und `eaf-core`. Dies umfasst die Konfiguration von Row-Level Security (RLS) in PostgreSQL, Mechanismen zur Weitergabe des Mandantenkontexts und die grundlegenden API-Endpunkte im Backend der Control Plane für CRUD-Operationen von Mandanten.
*Wert:* Ermöglicht die Datenisolation zwischen Mandanten, eine Kernanforderung des EAF, und die grundlegende administrative Verwaltung von Mandanten.

**Story 2.1: Mandanten-Entität und Basis-Persistenz (Tenant Entity & Basic Persistence)**

* **Als** Systemadministrator (des EAF) **möchte ich** Mandanten-Entitäten definieren und persistieren können (z.B. Mandanten-ID, Name, Status), **damit** Mandanten im System eindeutig identifiziert und verwaltet werden können.
* **Akzeptanzkriterien (ACs):**
    1. Eine `Tenant`-Entität ist innerhalb des `eaf-multitenancy`-Moduls (oder `eaf-core`) mit mindestens den Attributen definiert: `tenantId` (UUID-Typ, Primärschlüssel, bei Erstellung systemgeneriert und nicht änderbar), `name` (String, muss innerhalb des Systems eindeutig sein, unterliegt Validierungsregeln für Länge und erlaubte Zeichen, z.B. min 3 / max 100 Zeichen, alphanumerisch mit Bindestrichen), `status` (Enum, z.B. `ACTIVE`, `INACTIVE`, `SUSPENDED`, `PENDING_VERIFICATION`), `createdAt` (Timestamp, bei Erstellung gesetzt), `updatedAt` (Timestamp, bei jeder Änderung aktualisiert).
    2. Eine PostgreSQL-Tabelle (`tenants`) wird mittels eines idempotenten Schema-Migrationsskripts (z.B. Flyway, Liquibase), inklusive eines getesteten Rollback-Skripts, erstellt. Notwendige Indizes (mindestens für `tenantId` (unique) und `name` (unique)) sind vorhanden.
    3. Im Backend sind Services für CRUD-Operationen (Create, Read, Update, "Delete" als Soft-Delete durch Statusänderung auf `INACTIVE` oder `ARCHIVED`) für `Tenant`-Entitäten implementiert. Eine tatsächliche physische Löschung von Mandanten ist für das MVP nicht vorgesehen oder ist eine stark eingeschränkte, protokollierte administrative Operation.
    4. Die Services validieren Eingabedaten für Create- und Update-Operationen (z.B. Namensformat, Statusübergänge) und behandeln Datenbankfehler (z.B. Unique-Constraint-Verletzung bei `name`, Verbindungsprobleme) robust, indem sie aussagekräftige, fachliche Exceptions werfen oder standardisierte Fehlercodes zurückgeben (die von der API-Schicht verarbeitet werden können).
    5. Umfassende Unit-Tests (z.B. mit Mocking der Datenbankschicht) und Integrationstests (z.B. mit einer Test-Datenbank) decken die CRUD-Operationen für die `Tenant`-Entität ab, inklusive aller Validierungsregeln, Erfolgsfälle und erwarteten Fehlerfälle (z.B. Erstellung eines Mandanten mit einem bereits existierenden Namen).
    6. Die Validierungsregeln für Mandantenattribute (insbesondere `name` und erlaubte `status`-Übergänge) sind klar dokumentiert.

**Story 2.2: Mechanismus zur Weitergabe des Mandantenkontexts (Tenant Context Propagation Mechanism)**

* **Als** EAF-Entwickler **möchte ich** einen zuverlässigen Mechanismus zur Weitergabe des Kontexts des aktuellen Mandanten (z.B. Mandanten-ID) über den gesamten Anwendungs-Request hinweg haben, **damit** Geschäftslogik und Datenzugriff mandantensensitiv gestaltet werden können.
* **Akzeptanzkriterien (ACs):**
    1. Ein Mechanismus ist implementiert, um die `tenantId` zu Beginn eines Requests (z.B. in einem API-Gateway, einem vorgeschalteten Authentifizierungsfilter oder direkt in den Controllern) sicher zu erfassen und zu validieren. Die erwartete Quelle der `tenantId` (z.B. aus einem validierten JWT-Claim, einem speziellen HTTP-Header) ist klar definiert und dokumentiert. Fehlt die `tenantId` in einem Kontext, wo sie erwartet wird, oder ist sie ungültig (z.B. Format stimmt nicht, Mandant existiert nicht oder ist nicht aktiv), wird ein definierter Fehler (z.B. HTTP 400 Bad Request oder HTTP 401 Unauthorized / HTTP 403 Forbidden) zurückgegeben, bevor weitere Geschäftslogik ausgeführt wird.
    2. Die validierte `tenantId` wird sicher und unveränderlich in einem Request-bezogenen Kontext gespeichert (z.B. mittels `ThreadLocal` für synchrone Verarbeitungen, `kotlinx.coroutines.ThreadContextElement` für Coroutinen, oder als Metadaten in Axon-Messages). Der Kontext darf nicht von einer Komponente zur anderen "durchsickern" oder fälschlicherweise überschrieben werden.
    3. Services innerhalb von `eaf-core` und anderen EAF-Modulen können über eine klar definierte Schnittstelle (z.B. `TenantContextHolder.getCurrentTenantId()`) zuverlässig und einfach auf die aktuelle `tenantId` zugreifen. Der Zugriff auf den Kontext, wenn dieser nicht oder nicht korrekt gesetzt ist (z.B. in einem Hintergrundprozess, der nicht mandantenspezifisch sein soll oder fälschlicherweise keinen Kontext hat), führt zu einer klar definierten Exception (z.B. `TenantContextNotSetException`), es sei denn, es handelt sich um explizit mandantenunabhängige Systemoperationen.
    4. Der Mechanismus zur Kontextweitergabe funktioniert korrekt und nachweislich in asynchronen Operationen, insbesondere bei der Nutzung von Kotlin Coroutines (`withContext`, `async`, `launch`), Spring's `@Async`-Methoden und innerhalb von Axon Framework Nachrichtenflüssen (Commands, Events, Queries müssen den Mandantenkontext transportieren, typischerweise in Metadaten).
    5. Der Mechanismus ist für Entwickler von EAF-Anwendungen klar dokumentiert, inklusive Best Practices zur Nutzung und zur Implementierung mandantensensitiver Komponenten. Explizite Warnungen vor typischen Fallstricken (z.B. Verlust des Kontexts in neuen Threads) sind Teil der Doku.
    6. Unit- und Integrationstests verifizieren die korrekte Erfassung, Speicherung, Weitergabe und den Abruf des Mandantenkontexts in verschiedenen Szenarien: synchrone Aufrufe, asynchrone Aufrufe (Coroutinen, Axon-Handler), gültiger Kontext, fehlender Kontext, ungültiger Kontext (z.B. Mandant nicht aktiv).

**Story 2.3: Einrichtung von Row-Level Security (RLS) in PostgreSQL für Mandantendaten (Row-Level Security (RLS) Setup in PostgreSQL for Tenant Data)**

* **Als** EAF-Entwickler **möchte ich** RLS-Richtlinien in PostgreSQL konfiguriert haben, **damit** Datenbankabfragen Daten automatisch basierend auf dem aktuellen Mandantenkontext filtern und so eine strikte Datentrennung gewährleisten.
* **Akzeptanzkriterien (ACs):**
    1. Eine klare und dokumentierte Strategie zur Anwendung von RLS auf alle Tabellen, die mandantenspezifische Daten enthalten, ist definiert. Diese Strategie beinhaltet:
        * Jede mandantenspezifische Tabelle *muss* eine Spalte `tenant_id` (UUID Typ) enthalten, die nicht null sein darf und einen Foreign Key zur `tenants.tenantId`-Spalte besitzt.
        * Für welche Benutzerrollen der Datenbank RLS standardmäßig aktiviert (`FORCE ROW LEVEL SECURITY`) und für welche ggf. umgangen (`BYPASSRLS` Attribut für hochprivilegierte Wartungsrollen) wird.
    2. Generische, aktivierbare RLS-Richtlinien (`CREATE POLICY`) sind in PostgreSQL mittels Schema-Migrationsskripten erstellt. Diese Policies verwenden eine Datenbanksitzungsvariable (z.B. `current_setting('app.current_tenant_id', true)`) zur Datenfilterung. Es gibt mindestens eine Policy für `SELECT` und restriktivere Policies für `INSERT`, `UPDATE`, `DELETE`, die sicherstellen, dass Daten nur im korrekten Mandantenkontext modifiziert werden können.
    3. Der Datenbankverbindungsmechanismus des EAF (z.B. der DataSource-Wrapper in Spring/JPA oder die Konfiguration von Axon JDBC Event Store) ist so konfiguriert, dass die Datenbanksitzungsvariable `app.current_tenant_id` zu Beginn jeder Transaktion oder jedes Requests korrekt und sicher mit der `tenantId` aus dem Mandantenkontext (aus Story 2.2) gesetzt wird. Ist kein Mandantenkontext gesetzt, wird die Variable nicht gesetzt oder auf einen Wert gesetzt (z.B. `'-1'`), der garantiert keinen Datenzugriff erlaubt (außer für explizit mandantenübergreifende Systemoperationen, die eine Rolle mit `BYPASSRLS` nutzen). Das Setzen der Variable muss auch bei Verbindungs-Pooling korrekt gehandhabt werden (z.B. bei Rückgabe der Verbindung in den Pool zurücksetzen).
    4. Mindestens zwei verschiedene Beispieldomänentabellen mit mandantenspezifischen Daten (mit `tenant_id`-Spalte und Foreign Key) und darauf angewandten RLS-Richtlinien werden im Rahmen der Schema-Migration erstellt.
    5. Umfassende Integrationstests (die mit unterschiedlichen Datenbankbenutzern und gesetzten/nicht gesetzten Mandantenkontexten agieren) demonstrieren rigoros:
        * `SELECT`-Abfragen auf die Beispieldatensätze liefern *ausschließlich* Daten des in der Sitzungsvariable gesetzten Mandanten.
        * `INSERT`-Operationen setzen automatisch die korrekte `tenant_id` aus der Sitzungsvariable oder schlagen fehl, wenn versucht wird, eine andere `tenant_id` einzufügen.
        * `UPDATE`/`DELETE`-Operationen betreffen nur Datensätze des aktuellen Mandanten.
        * Versuche, Daten eines anderen Mandanten abzurufen oder zu modifizieren (auch durch "trickreiche" Queries), schlagen fehl oder liefern leere Ergebnisse.
        * Operationen ohne gesetzten Mandantenkontext (oder mit einem ungültigen) führen zu keinem Datenzugriff auf mandantenspezifische Tabellen (außer für definierte System-Rollen/Ausnahmen).
    6. Die Auswirkungen von RLS auf die Performance gängiger Abfragetypen (inkl. Joins) werden initial bewertet (z.B. durch `EXPLAIN ANALYZE`) und als akzeptabel für die erwarteten Workloads eingeschätzt. Notwendige Indizes zur Unterstützung von RLS-Performance (insbesondere auf `tenant_id`-Spalten) sind vorhanden.
    7. Die Konfiguration und das Verhalten von RLS sind detailliert für Entwickler dokumentiert, inklusive der Implikationen für Datenbankabfragen und Schema-Design.

**Story 2.4: Basis Control Plane API für Mandantenverwaltung (CRUD) (Basic Control Plane API for Tenant Management (CRUD))**

* **Als** Control Plane Administrator **möchte ich** eine sichere Backend-API zur Verwaltung von Mandanten haben (Erstellen, Lesen, Aktualisieren, Deaktivieren), **damit** ich den Lebenszyklus von Mandanten administrieren kann.
* **Akzeptanzkriterien (ACs):**
    1. RESTful API-Endpunkte werden vom EAF bereitgestellt (z.B. unter `/api/controlplane/tenants`) und sind im `eaf-core` oder einem dedizierten Control-Plane-Backend-Modul (z.B. `eaf-controlplane-api`) angesiedelt. Die API folgt den Prinzipien eines gut designten RESTful Webservices (korrekte Nutzung von HTTP-Methoden, Statuscodes, Headern).
    2. Die Endpunkte unterstützen HTTP-Methoden für Standard-CRUD-Operationen mit klar definierten Request- und Response-Payloads (JSON):
        * `POST /tenants`: Erstellt einen neuen Mandanten. Erfordert einen validen Mandantennamen und ggf. weitere initiale Konfigurationsparameter (z.B. initiale Admin-E-Mail). Validiert Eingaben serverseitig (z.B. Namensformat, -länge, Eindeutigkeit des Namens, gültige E-Mail). Gibt bei Erfolg HTTP 201 Created mit dem vollständigen Mandantenobjekt (inklusive systemgenerierter `tenantId`) im Body und einem `Location`-Header zurück.
        * `GET /tenants`: Listet alle Mandanten auf (oder eine paginierte Teilmenge). Unterstützt Paginierung (z.B. `page`, `size` Query-Parameter mit Default-Werten und Obergrenzen) und Filterung (z.B. nach `status`, Freitextsuche im `name`). Gibt HTTP 200 OK mit einer Liste von Mandantenobjekten und Paginierungsinformationen im Body oder in Headern zurück. Gibt eine leere Liste zurück, wenn keine Mandanten den Kriterien entsprechen.
        * `GET /tenants/{tenantId}`: Ruft Details eines spezifischen Mandanten anhand seiner `tenantId` (UUID-Format) ab. Gibt HTTP 200 OK bei Erfolg mit dem Mandantenobjekt oder HTTP 404 Not Found zurück, wenn der Mandant mit der gegebenen ID nicht existiert.
        * `PUT /tenants/{tenantId}`: Aktualisiert einen bestehenden Mandanten (z.B. Name, Status). Ist idempotent. Validiert Eingaben. Gibt HTTP 200 OK (mit dem aktualisierten Objekt) oder HTTP 204 No Content bei Erfolg, HTTP 404 Not Found bei nicht existentem Mandanten, HTTP 400 Bad Request bei Validierungsfehlern.
        * `DELETE /tenants/{tenantId}`: Deaktiviert (Soft Delete durch Statusänderung auf `INACTIVE` oder `ARCHIVED`) einen Mandanten. Ist idempotent. Gibt HTTP 204 No Content bei Erfolg, HTTP 404 Not Found bei nicht existentem Mandanten.
    3. Alle API-Endpunkte sind durch geeignete, robuste Authentifizierungs- und Autorisierungsmechanismen (z.B. OAuth2 Client Credentials Flow für M2M-Zugriff, spezifische Admin-Rollen) gesichert. Unautorisierte Zugriffsversuche führen zu HTTP 401 Unauthorized, Zugriffsversuche mit unzureichenden Rechten zu HTTP 403 Forbidden.
    4. API-Anfragen und -Antworten verwenden durchgängig JSON. Fehlerantworten folgen einem standardisierten Format (z.B. RFC 7807 Problem Details for HTTP APIs) und enthalten eine maschinenlesbare Fehlermeldung, einen menschenlesbaren Beschreibungstext und ggf. einen Trace-Identifier für die Korrelation mit Server-Logs. Datumsformate in JSON sind standardisiert (ISO 8601).
    5. Alle API-Operationen, die den Zustand von Mandanten ändern (Create, Update, Delete/Deactivate), sind transaktional und atomar implementiert. Im Fehlerfall erfolgt ein Rollback, um inkonsistente Zustände zu vermeiden.
    6. Eine aktuelle und detaillierte API-Dokumentation (z.B. generiert aus Code mit OpenAPI 3.x / Swagger) ist für diese Endpunkte verfügbar. Sie beschreibt alle Endpunkte, Parameter, Request/Response-Schemata, Validierungsregeln, mögliche Fehlercodes und Sicherheitsanforderungen.
    7. Umfassende Integrationstests (z.B. mit Spring Boot Test, Testcontainers für die Datenbank) decken alle API-Endpunkte ab. Getestet werden Happy Paths, alle definierten Validierungsfehler, Autorisierungs- und Authentifizierungsfehler, Randfälle (z.B. Operation auf nicht existierenden Entitäten, leere Listen, Paginierungsgrenzen) und Idempotenz von PUT/DELETE.
    8. Alle administrativen Änderungen an Mandanten (Erstellung, Update, Statusänderung), die über diese API erfolgen, werden detailliert im zentralen Audit-Log (siehe Epic 10) erfasst, inklusive Zeitstempel, ausführendem Akteur und den geänderten Daten.

---

**Epic 3: Core Identity & Access Management (IAM) - Lokale Benutzer & RBAC (Core Identity & Access Management (IAM) - Local Users & RBAC)**
*Beschreibung:* Implementiert die Benutzerverwaltung innerhalb eines Mandanten (API im Modul `eaf-iam`), die Authentifizierung lokaler Benutzer und ein grundlegendes rollenbasiertes Zugriffskontrollsystem (RBAC). Beinhaltet auch die initiale Einrichtung für Service-Accounts.
*Wert:* Ermöglicht einen sicheren Benutzerzugriff und eine grundlegende Rechteverwaltung innerhalb der Mandanten.

**Story 3.1: Lokale Benutzer-Entität & Sichere Speicherung von Anmeldeinformationen (pro Mandant) (Local User Entity & Secure Credential Storage (per Tenant))**

* **Als** EAF-Entwickler **möchte ich** lokale Benutzer-Entitäten (z.B. Benutzername, gehashtes Passwort, Status, zugehörige Mandanten-ID) definieren und sicher persistieren können, **damit** mandantenspezifische Benutzer lokal authentifiziert werden können.
* **Akzeptanzkriterien (ACs):**
    1. Eine `LocalUser`-Entität ist innerhalb des `eaf-iam`-Moduls definiert mit mindestens den Attributen: `userId` (UUID, Primärschlüssel, systemgeneriert), `tenantId` (UUID, Foreign Key zu `tenants.tenantId`, nicht null), `username` (String, muss pro `tenantId` eindeutig sein, unterliegt Validierungsregeln für Länge und erlaubte Zeichen, z.B. E-Mail-Format oder definierter Namensraum), `hashedPassword` (String, speichert den sicher gehashten Passwort-Wert), `salt` (String, falls vom Hashing-Algorithmus benötigt und nicht im Hashwert integriert), `email` (String, optional, Formatvalidierung), `status` (Enum: z.B. `ACTIVE`, `EMAIL_VERIFICATION_PENDING`, `LOCKED_BY_ADMIN`, `DISABLED_BY_ADMIN`, `PASSWORD_EXPIRED`), `createdAt` (Timestamp), `updatedAt` (Timestamp).
    2. Eine PostgreSQL-Tabelle (`local_users`) wird mittels eines idempotenten Schema-Migrationsskripts (inkl. Rollback) erstellt. Notwendige Indizes (mindestens für `userId` (unique), (`tenantId`, `username`) (unique)) sind vorhanden. Die Spalte `hashedPassword` und `salt` sind so dimensioniert, dass sie moderne Hashing-Algorithmen unterstützen.
    3. Für die Speicherung von Passwörtern wird ein starker, adaptiver Hashing-Algorithmus (z.B. Argon2id, scrypt oder bcrypt über Spring Security `DelegatingPasswordEncoder`) mit angemessenen Konfigurationsparametern (z.B. Iterationszahl, Speicherbedarf, Parallelität) verwendet. Klartextpasswörter werden zu keinem Zeitpunkt gespeichert oder geloggt.
    4. Grundlegende Backend-Services (intern im `eaf-iam`-Modul) zum Erstellen (inkl. Passwort-Hashing), Abrufen (ohne Passwort-Hash), Aktualisieren (exkl. Passwort) und Suchen von lokalen Benutzern sind implementiert. Diese Services validieren Eingabedaten und behandeln Datenbankfehler robust.
    5. Umfassende Unit-Tests decken die Benutzererstellung mit korrektem Passwort-Hashing, das Abrufen von Benutzern (ohne Passwortdaten) und die Validierung von Benutzereingaben (z.B. E-Mail-Format, Eindeutigkeit von `username` pro Mandant) ab. Fehlerfälle (z.B. ungültige Eingaben, Datenbankfehler) werden ebenfalls getestet.
    6. Richtlinien für Passwortkomplexität (minimale Länge, Zeichentypen) sind definierbar (Systemkonfiguration) und werden bei der Passworterstellung/-änderung serverseitig validiert.

**Story 3.2: API für die Verwaltung lokaler Benutzer (innerhalb eines Mandanten) (API for Local User Management (within a Tenant))**

* **Als** Mandanten-Administrator (über die Control Plane API) **möchte ich** eine sichere Backend-API zur Verwaltung lokaler Benutzer innerhalb meines Mandanten haben (Erstellen, Lesen, Aktualisieren, Status setzen), **damit** ich den Benutzerzugriff steuern kann.
* **Akzeptanzkriterien (ACs):**
    1. RESTful API-Endpunkte werden vom `eaf-iam`-Modul bereitgestellt (z.B. unter `/api/controlplane/tenants/{tenantId}/users`) und folgen den Prinzipien guten API-Designs.
    2. Die Endpunkte unterstützen folgende Operationen mit klar definierten JSON Request/Response Payloads:
        * `POST /users`: Erstellt einen neuen lokalen Benutzer für den im Pfad angegebenen Mandanten. Benötigt `username`, `password`, `email`. Validiert Eingaben (Passwortkomplexität, E-Mail-Format, Eindeutigkeit des `username`). Gibt HTTP 201 Created mit dem Benutzerobjekt (ohne Passwortdetails) zurück.
        * `GET /users`: Listet lokale Benutzer des Mandanten auf. Unterstützt Paginierung, Filterung (z.B. nach `status`, `username`) und Sortierung. Gibt HTTP 200 OK zurück.
        * `GET /users/{userId}`: Ruft Details eines spezifischen lokalen Benutzers ab. Gibt HTTP 200 OK oder HTTP 404 Not Found zurück.
        * `PUT /users/{userId}`: Aktualisiert Details eines lokalen Benutzers (z.B. `email`, `status`). `username` und `tenantId` sind nicht änderbar. Validiert Eingaben. Gibt HTTP 200 OK oder HTTP 404 zurück.
        * `POST /users/{userId}/set-password`: Ermöglicht einem Administrator, das Passwort eines Benutzers zurückzusetzen/zu ändern (erfordert ggf. Bestätigung des Admins). Validiert neues Passwort gegen Komplexitätsregeln. Gibt HTTP 204 No Content zurück.
        * `PUT /users/{userId}/status`: Aktualisiert den Benutzerstatus (z.B. `ACTIVE`, `LOCKED_BY_ADMIN`, `DISABLED_BY_ADMIN`). Validiert erlaubte Statusübergänge. Gibt HTTP 200 OK oder HTTP 404 zurück.
    3. Alle API-Endpunkte sind durch den Mandantenkontext (aus dem Pfad `{tenantId}`) und geeignete Berechtigungen (z.B. "TenantUserAdmin"-Rolle) gesichert. Ein Administrator eines Mandanten darf nur Benutzer seines eigenen Mandanten verwalten.
    4. Fehlerantworten folgen dem standardisierten Format (RFC 7807 Problem Details). Validierungsfehler listen betroffene Felder und spezifische Probleme auf.
    5. Alle Operationen, die Benutzerdaten ändern, sind transaktional und werden im Audit-Log erfasst.
    6. Die API-Dokumentation (OpenAPI 3.x) ist detailliert und aktuell.
    7. Integrationstests decken alle Endpunkte, Erfolgsfälle, Validierungsfehler, Autorisierungsfehler und Randfälle ab (z.B. Versuch, einen Benutzer eines anderen Mandanten zu bearbeiten).

**Story 3.3: Authentifizierungsmechanismus für lokale Benutzer (Local User Authentication Mechanism)**

* **Als** Benutzer **möchte ich** mich mit meinen lokalen EAF-Benutzeranmeldeinformationen (Benutzername/Passwort für einen spezifischen Mandanten) authentifizieren können, **damit** ich auf mandantenspezifische Anwendungen/APIs zugreifen kann.
* **Akzeptanzkriterien (ACs):**
    1. Ein Authentifizierungs-Endpunkt (z.B. `/api/auth/login` oder `/oauth/token` bei Verwendung von OAuth2 Password Grant) wird vom `eaf-iam`-Modul bereitgestellt oder über Spring Security (ggf. mit Anpassungen für Mandantenkontext) konfiguriert.
    2. Der Endpunkt akzeptiert eine Mandantenkennung (z.B. als Teil des `username` im Format `user@tenantidentifier`, oder als separater Parameter/Header, der vor der Authentifizierung validiert wird), den Benutzernamen (innerhalb des Mandanten) und das Passwort.
    3. Die Authentifizierungslogik validiert die Anmeldeinformationen sicher gegen die gespeicherten gehashten Passwörter und Salts der lokalen Benutzer (aus Story 3.1). Der Benutzerstatus (`ACTIVE`) wird ebenfalls geprüft.
    4. Bei erfolgreicher Authentifizierung wird ein sicheres, kurzlebiges Zugriffstoken (z.B. JWT gemäß RFC 7519) ausgestellt. Das Token enthält mindestens `userId`, `tenantId`, `username` und die zugewiesenen Rollen des Benutzers sowie eine `exp`-Claim (Ablaufzeit). Ein Refresh-Token kann optional zur Session-Verlängerung ausgestellt werden.
    5. Fehlgeschlagene Authentifizierungsversuche (ungültiger Benutzername/Passwort, gesperrter/inaktiver Account, ungültiger Mandant) führen zu generischen Fehlermeldungen (HTTP 400/401) ohne Preisgabe spezifischer Details über den Grund des Fehlschlags (um User Enumeration zu verhindern). Wiederholte fehlgeschlagene Versuche für einen Benutzer führen zu einer temporären Sperrung des Accounts (Account Lockout Policy, konfigurierbar). Alle Authentifizierungsversuche (erfolgreich und fehlgeschlagen) werden sicher protokolliert (Audit Log und ggf. Security Log).
    6. Integrationstests verifizieren erfolgreiche und diverse fehlgeschlagene Authentifizierungsszenarien, Token-Ausstellung und -Inhalt sowie das Account Lockout Verhalten.
    7. Der ausgestellte Token-Mechanismus (insbesondere das JWT) enthält die `tenantId` in einer Weise, dass sie für die nachfolgende Weitergabe des Mandantenkontexts (Story 2.2) zuverlässig extrahiert werden kann.
    8. Die Sicherheit des Endpunkts gegen Brute-Force-Angriffe und andere gängige Authentifizierungs-Schwachstellen ist berücksichtigt (z.B. durch Rate Limiting, sichere Token-Handhabung).

**Story 3.4: RBAC - Definition und Zuweisung von Rollen & Berechtigungen (RBAC - Role & Permission Definition and Assignment)**

* **Als** EAF-Entwickler **möchte ich** Rollen und Berechtigungen definieren können, und **als** Mandanten-Administrator (über die Control Plane API) **möchte ich** lokalen Benutzern innerhalb meines Mandanten Rollen zuweisen können, **damit** der Zugriff auf Ressourcen rollenbasiert gesteuert werden kann.
* **Akzeptanzkriterien (ACs):**
    1. Entitäten für `Role` (z.B. `roleId`, `tenantId` (null für systemweite EAF-Admin-Rollen, `tenantId` für mandantenspezifische Rollen), `name` (eindeutig pro `tenantId` oder global), `description`) und `Permission` (z.B. `permissionId`, `name` (eindeutig, z.B. `user:create`, `tenant:edit`), `description`) sind im `eaf-iam`-Modul definiert und persistiert. Permissions sind initial systemdefiniert und nicht durch Mandanten-Admins erstellbar.
    2. Eine Many-to-Many-Beziehung zwischen `Role` und `Permission` ist etabliert und wird über eine Zwischentabelle persistiert (Rollen können mehrere Berechtigungen haben, eine Berechtigung kann in mehreren Rollen vorkommen).
    3. Eine Many-to-Many-Beziehung zwischen `LocalUser` und `Role` (innerhalb eines Mandanten) ist etabliert und wird über eine Zwischentabelle persistiert.
    4. Backend-API-Endpunkte (z.B. unter `/api/controlplane/permissions` für globale Permissions, `/api/controlplane/roles` für globale Rollen, `/api/controlplane/tenants/{tenantId}/roles` für mandantenspezifische Rollen und `/api/controlplane/tenants/{tenantId}/users/{userId}/roles` für Zuweisungen) werden bereitgestellt für:
        * Auflisten aller definierten (systemweiten) Permissions.
        * (Für EAF-Super-Admin) CRUD-Operationen für systemweite Rollen und Zuweisung von Permissions zu diesen Rollen.
        * (Für Mandanten-Admin) Auflisten verfügbarer Rollen (systemweit anwendbare und eigene mandantenspezifische). Erstellen, Aktualisieren, Löschen von mandantenspezifischen Rollen. Zuweisen/Entziehen von (aus einem Pool erlaubter) Permissions zu/von mandantenspezifischen Rollen.
        * Zuweisen/Entziehen von Rollen zu/von Benutzern des Mandanten.
    5. Das Authentifizierungs-Token (z.B. JWT aus Story 3.3) enthält die effektiven Berechtigungsnamen (nicht nur Rollennamen) des Benutzers oder die Rollennamen, wenn die Berechtigungsprüfung serverseitig gegen die Rollen erfolgt.
    6. Das EAF stellt einen robusten Mechanismus (z.B. Integration mit Spring Security Method Security unter Verwendung von `@PreAuthorize` mit benutzerdefinierten Expressions oder über einen zentralen `AccessDecisionManager`) bereit, um den Zugriff auf Services und API-Endpunkte basierend auf den zugewiesenen Berechtigungen (oder Rollen) des authentifizierten Benutzers (oder Service-Accounts) zu schützen. Fehlender Zugriff führt zu HTTP 403 Forbidden.
    7. Das grundlegende RBAC-Setup, die Definition von Permissions und die Erstellung von Rollen sind für Entwickler und Administratoren detailliert dokumentiert.
    8. Das Design der RBAC-Strukturen und -Mechanismen berücksichtigt die spätere Erweiterbarkeit für ABAC-Konzepte (z.B. indem Berechtigungen nicht nur Namen, sondern auch Kontextinformationen oder Bedingungen aufnehmen könnten, auch wenn dies im MVP noch nicht voll genutzt wird).
    9. Alle administrativen Änderungen an Rollen, Berechtigungen und Zuweisungen werden im Audit-Log erfasst.

**Story 3.5: Verwaltung und Authentifizierung von Service-Accounts mit Standard-Expiration (Service Account Management & Authentication with Default Expiration)**

* **Als** Mandanten-Administrator (über die Control Plane API) **möchte ich** Service-Accounts für meinen Mandanten erstellen und verwalten können, welche eine Standard-Ablauffrist haben, und **als** externes System **möchte ich** mich mit Service-Account-Anmeldeinformationen authentifizieren können, **damit** der Machine-to-Machine API-Zugriff gesichert und standardmäßig zeitlich begrenzt ist.
* **Akzeptanzkriterien (ACs):**
    1. Eine `ServiceAccount`-Entität ist im `eaf-iam`-Modul definiert mit mindestens den Attributen: `serviceAccountId` (UUID, PK), `tenantId` (UUID, FK), `clientId` (String, eindeutig pro Mandant, systemgeneriert), `clientSecretHash` (String, speichert Hash des Secrets), `salt` (String), `description` (String), `status` (Enum: `ACTIVE`, `INACTIVE`), zugewiesene `Role`-IDs, `createdAt` (Timestamp), `expiresAt` (Timestamp, nullable).
    2. Wenn ein Service-Account über die API erstellt wird, wird `expiresAt` standardmäßig auf einen konfigurierbaren Wert gesetzt (z.B. 1 Jahr ab `createdAt`), es sei denn, eine andere Ablauffrist (innerhalb einer systemseitig maximal erlaubten Frist) wird explizit bei der Erstellung angegeben. Ein Service-Account kann auch ohne Ablauffrist erstellt werden, wenn dies explizit angegeben wird (und administrativ erlaubt ist).
    3. Die sichere Generierung, Speicherung (Hashing des Secrets) und Verwaltung (Rotation des Secrets, Widerruf) von Client-Anmeldeinformationen ist implementiert. Das Client Secret wird dem Administrator nur einmalig direkt nach der Erstellung oder einer Rotation angezeigt und danach nicht mehr abrufbar gespeichert.
    4. Backend-API-Endpunkte (z.B. unter `/api/controlplane/tenants/{tenantId}/service-accounts`) werden für Mandanten-Administratoren zur CRUD-Verwaltung von Service-Accounts bereitgestellt. Dies beinhaltet das Erstellen, Auflisten, Anzeigen von Details (exkl. Secret-Hash), Aktualisieren (Beschreibung, Status, `expiresAt`) und Löschen (Soft Delete) von Service-Accounts sowie das Auslösen einer Secret-Rotation.
    5. Ein sicherer Authentifizierungsmechanismus für Service-Accounts ist implementiert, vorzugsweise der OAuth 2.0 Client Credentials Grant Flow (`POST /oauth/token` mit `grant_type=client_credentials`, `client_id`, `client_secret`). Dieser Mechanismus prüft strikt den `status` (`ACTIVE`) und `expiresAt` (darf nicht in der Vergangenheit liegen) des Service-Accounts.
    6. Bei erfolgreicher Authentifizierung eines Service-Accounts wird ein kurzlebiges Zugriffstoken (JWT) ausgestellt, das mindestens die `serviceAccountId`, `clientId`, `tenantId` und die effektiven Berechtigungen/Rollen des Service-Accounts enthält. Die Gültigkeit des Tokens darf die `expiresAt`-Zeit des Service-Accounts nicht überschreiten.
    7. Service-Accounts können (analog zu Benutzern) Rollen für RBAC zugewiesen bekommen, um ihre Zugriffsberechtigungen zu definieren.
    8. Die Standard-Ablauffrist (z.B. 1 Jahr) und die maximal erlaubte Ablauffrist für Service-Accounts sind auf EAF-Systemebene konfigurierbar.
    9. Fehlgeschlagene Authentifizierungsversuche von Service-Accounts (ungültige Credentials, abgelaufener Account, inaktiver Account) werden sicher protokolliert und führen zu einer standardisierten Fehlermeldung (HTTP 400/401) ohne Preisgabe interner Details.
    10. Die API zur Verwaltung von Service-Accounts validiert alle Eingaben (z.B. auf Gültigkeit von `expiresAt`) und gibt klare Fehlermeldungen bei ungültigen Daten zurück. Alle administrativen Änderungen an Service-Accounts werden im Audit-Log erfasst.

---

**Epic 4: Control Plane UI - Phase 1 (Mandanten- & Basis-Benutzerverwaltung) (Control Plane UI - Phase 1 (Tenant & Basic User Management))**
*Beschreibung:* Entwickelt die erste Version der Control Plane UI (React-Admin basiert), die administrative Fähigkeiten für die Verwaltung von Mandanten (CRUD) und die Verwaltung lokaler Benutzer & deren Rollen innerhalb dieser Mandanten bereitstellt (unter Nutzung der APIs aus Epic 2 & 3).
*Wert:* Stellt eine benutzbare Oberfläche für zentrale administrative Aufgaben bereit.

**Story 4.1: UI-Grundgerüst der Control Plane & Login (Control Plane UI Shell & Login)**

* **Als** Control Plane Administrator **möchte ich** ein Basis-UI-Grundgerüst (Navigation, Layout) für die Control Plane und eine Login-Maske haben, **damit** ich sicher auf die administrativen Funktionalitäten zugreifen kann.
* **Akzeptanzkriterien (ACs):**
    1. Eine neue React-Anwendung ist für die Control Plane UI initialisiert und konfiguriert (z.B. mittels Create React App, Vite oder einem ähnlichen etablierten Toolchain, mit TypeScript als Standardsprache). Das Projekt enthält grundlegende Linting- und Formatting-Regeln.
    2. Die UI nutzt ein etabliertes Komponenten-Framework, das dem Stil von React-Admin nahekommt (z.B. Material-UI, Ant Design oder direkt React-Admin-Komponenten), um ein professionelles, funktionales und konsistentes Erscheinungsbild zu gewährleisten.
    3. Eine Login-Seite ist implementiert, die Eingabefelder für Benutzername/E-Mail und Passwort sowie einen Login-Button enthält. Sie ruft sicher die Backend-Authentifizierungs-API auf (aus Story 3.3, ggf. angepasst für Control Plane Admins). CSRF-Schutz ist implementiert, falls anwendbar.
    4. Bei erfolgreichem Login wird ein Zugriffstoken (z.B. JWT) sicher im Client gespeichert (z.B. im `localStorage` oder `sessionStorage` mit Überlegungen zur XSS-Prävention, oder als `HttpOnly`-Cookie, falls vom Backend unterstützt und für die Architektur passend). Der Benutzer wird zu einem Haupt-Dashboard oder einer Startseite weitergeleitet. Der Zustand "eingeloggt" ist persistent über Seiten-Reloads (innerhalb der Token-Gültigkeit).
    5. Eine grundlegende Navigation (z.B. persistente Seitenleiste, Kopfzeile mit Benutzermenü und Logout-Button) ist vorhanden. Die Navigation zeigt nur die Bereiche an, für die der eingeloggte Administrator die entsprechenden Berechtigungen hat (RBAC-gesteuert).
    6. Die UI ist primär für Desktop-Browser optimiert (aktuelle Versionen von Chrome, Firefox, Edge). Eine grundlegende responsive Darstellung stellt sicher, dass Kerninformationen auf Tablets ohne schwerwiegende Darstellungsfehler oder Funktionsverluste einsehbar sind.
    7. Fehlgeschlagene Login-Versuche (z.B. ungültige Anmeldeinformationen, Serverfehler, gesperrter Account) werden dem Benutzer mit einer klaren, aber nicht zu detaillierten (um Enumeration zu vermeiden) Fehlermeldung angezeigt. Wiederholte fehlgeschlagene Versuche können clientseitig zu einer kurzen Verzögerung führen, bevor ein erneuter Versuch möglich ist.
    8. Ein "Passwort vergessen"-Flow ist für diese Phase **nicht** Teil des MVP; stattdessen wird auf einen manuellen administrativen Prozess zur Passwortzurücksetzung verwiesen (z.B. über einen anderen Admin).
    9. Die UI fängt globale JavaScript-Fehler sowie unaufgefangene Fehler in API-Antworten (z.B. 5xx Serverfehler) ab und zeigt eine generische, benutzerfreundliche Fehlermeldung an, um ein "Einfrieren" oder eine nichtssagende weiße Seite zu verhindern. Ein Mechanismus zum Loggen von Client-Side-Fehlern (z.B. Sentry.io oder ein einfacher `console.error` mit Versand an ein Backend, falls möglich) wird in Betracht gezogen.
    10. Ein Logout-Button ist vorhanden und invalidiert die lokale Session/das Token und leitet den Benutzer zur Login-Seite zurück.

**Story 4.2: UI für Mandantenverwaltung (CRUD) (UI for Tenant Management (CRUD))**

* **Als** Control Plane Administrator **möchte ich** einen UI-Bereich zur Verwaltung von Mandanten haben (Auflisten, Erstellen, Anzeigen, Bearbeiten, Deaktivieren/Aktivieren), **damit** ich Mandanten-Administrationsaufgaben visuell durchführen kann.
* **Akzeptanzkriterien (ACs):**
    1. Ein Bereich "Mandantenverwaltung" ist über die UI-Navigation sicher zugänglich (nur für Administratoren mit entsprechender Berechtigung).
    2. Ein Datenraster/eine Tabelle zeigt eine Liste von Mandanten an. Angezeigte Spalten umfassen mindestens Mandanten-ID (ggf. gekürzt/verlinkt), Name, Status (z.B. Aktiv, Inaktiv) und Erstellungsdatum. Die Tabelle unterstützt client- oder serverseitige Paginierung für große Mandantenlisten, Sortierung nach den meisten Spalten und eine Freitextsuche/Filterung (z.B. nach Name, Status).
    3. Ein Formular (z.B. in einem Modal oder einer separaten Seite) ermöglicht das Erstellen neuer Mandanten (Aufruf der API aus Story 2.4). Das Formular beinhaltet clientseitige Validierung für Pflichtfelder (z.B. Name) und Datenformate gemäß den API-Vorgaben, bevor die Anfrage an die API gesendet wird. Fehlermeldungen der API (z.B. "Name bereits vergeben", Validierungsfehler) werden dem Nutzer verständlich direkt bei den betreffenden Feldern oder als globale Formularmeldung angezeigt.
    4. Eine schreibgeschützte Detailansicht (z.B. erreichbar durch Klick auf einen Mandanten in der Liste) ermöglicht die Anzeige aller relevanten Informationen eines ausgewählten Mandanten.
    5. Ein Formular ermöglicht das Bearbeiten bestehender Mandanten (z.B. Name, Status) (Aufruf der API aus Story 2.4). Auch hier mit clientseitiger Validierung und kontextbezogener Fehlerbehandlung von API-Fehlern. Felder, die nicht geändert werden können (z.B. `tenantId`), sind nicht editierbar.
    6. Aktionen (z.B. Buttons in der Tabellenzeile, Kontextmenü-Einträge oder Buttons in der Detailansicht) ermöglichen das Deaktivieren/Aktivieren von Mandanten mit einer vorgeschalteten Bestätigungsabfrage (um unbeabsichtigte Aktionen zu verhindern). Der Status des Mandanten wird in der Liste nach der Aktion korrekt und ohne manuellen Refresh aktualisiert.
    7. Die Benutzerinteraktionen sind intuitiv und folgen dem "React-Admin"-Stil (z.B. klare Buttons für Primär- und Sekundäraktionen, konsistente Formularlayouts, informative Tooltips).
    8. Ladezustände (z.B. beim Abrufen von Daten für die Tabelle, beim Speichern von Änderungen in Formularen) werden dem Benutzer visuell angezeigt (z.B. durch Ladeindikatoren, Deaktivieren von Buttons während der Aktion). Erfolgs- und Fehlermeldungen nach Aktionen werden klar (z.B. als "Toast"-Benachrichtigungen) angezeigt.

**Story 4.3: UI für die Verwaltung lokaler Benutzer innerhalb eines Mandanten (UI for Local User Management within a Tenant)**

* **Als** Control Plane Administrator (oder Mandanten-Administrator, falls die UI dies später unterstützt und der eingeloggte Benutzer die entsprechenden Rechte für den ausgewählten Mandanten hat) **möchte ich** einen UI-Bereich zur Verwaltung lokaler Benutzer innerhalb eines ausgewählten Mandanten haben (Auflisten, Erstellen, Anzeigen, Bearbeiten, Status verwalten, Passwort zurücksetzen), **damit** die Benutzeradministration für Mandanten visuell erfolgen kann.
* **Akzeptanzkriterien (ACs):**
    1. Innerhalb der Detailansicht eines Mandanten oder eines dedizierten Bereichs "Benutzerverwaltung" (eindeutig auf einen Mandanten bezogen und nur zugänglich, wenn ein Mandant ausgewählt/im Kontext ist) zeigt ein Datenraster/eine Tabelle die lokalen Benutzer dieses Mandanten an (Spalten: z.B. Benutzername, E-Mail, Status, Erstellungsdatum). Paginierung, Filterung und Sortierung werden unterstützt.
    2. Formulare und Aktionen ermöglichen das Erstellen neuer lokaler Benutzer (Benutzername, E-Mail, initiales Passwort – Passwort wird maskiert eingegeben und nur zum Senden an die API verwendet), die Anzeige von Benutzerdetails, die Bearbeitung von Benutzerinformationen (z.B. E-Mail, Status) und das Auslösen von Passwort-Resets (Aufruf der APIs aus Story 3.2). Validierung, Fehlerbehandlung und Ladezustände wie in Story 4.2 beschrieben.
    3. Der Benutzerstatus (Aktiv, Gesperrt, Deaktiviert) kann über die UI verwaltet werden, inklusive Bestätigungsdialogen für kritische Statusänderungen.
    4. Bei der Erstellung eines Benutzers oder beim Passwort-Reset werden serverseitig definierte Passwortstärkerichtlinien (falls vorhanden) clientseitig angedeutet (z.B. als Tooltip) und serverseitig validiert; entsprechende Fehlermeldungen der API werden angezeigt.
    5. Die Benutzeroberfläche verhindert zu jedem Zeitpunkt die direkte Anzeige von Klartext- oder gehashten Passwörtern.
    6. Der Versuch, einen Benutzer zu erstellen, dessen Benutzername im Mandantenkontext bereits existiert, führt zu einer aussagekräftigen Fehlermeldung.
    7. Die Benutzerinteraktionen sind intuitiv und konsistent mit dem Rest der Control Plane.

**Story 4.4: UI für RBAC-Verwaltung (Rollenzuweisung innerhalb eines Mandanten) (UI for RBAC Management (Role Assignment within a Tenant))**

* **Als** Control Plane Administrator (oder Mandanten-Administrator mit entsprechenden Rechten) **möchte ich** einen UI-Bereich zur Verwaltung von Rollenzuweisungen zu Benutzern innerhalb eines ausgewählten Mandanten haben, **damit** die Zugriffskontrolle visuell konfiguriert werden kann.
* **Akzeptanzkriterien (ACs):**
    1. Ein UI-Bereich (z.B. in der Benutzerdetailansicht eines lokalen Benutzers oder eines Service-Accounts, oder als separater Tab/Bereich "Rollen") ermöglicht die Anzeige der für den aktuellen Mandanten verfügbaren Rollen (sowohl systemweit anwendbare als auch mandantenspezifische, falls diese später unterstützt werden).
    2. Für einen ausgewählten Benutzer/Service-Account können dessen aktuell zugewiesene Rollen klar und deutlich angezeigt werden.
    3. Für einen ausgewählten Benutzer/Service-Account können Rollen aus einer Liste der verfügbaren Rollen zugewiesen oder entfernt werden (z.B. über eine Multi-Select-Box, eine Liste mit Checkboxen, Drag-and-Drop-Interface). Änderungen rufen die entsprechenden APIs aus Story 3.4 auf.
    4. Eine explizite Speicheraktion mit Bestätigungsdialog ist erforderlich, bevor Änderungen an Rollenzuweisungen wirksam werden.
    5. *(MVP-Fokus für Phase 1):* Das Erstellen/Bearbeiten von Rollen selbst und das Zuweisen von Berechtigungen (Permissions) zu Rollen erfolgt **nicht** über die UI in dieser Phase. Die UI fokussiert sich ausschließlich auf die Zuweisung *bestehender, vordefinierter* Rollen zu Benutzern/Service-Accounts.
    6. Die Benutzerinteraktionen sind intuitiv und fehlertolerant (z.B. was passiert, wenn versucht wird, eine nicht existierende Rolle zuzuweisen – sollte durch UI-Auswahl verhindert werden).
    7. Fehler beim Speichern von Rollenzuweisungen (z.B. API-Fehler, konkurrierende Änderung) werden dem Benutzer klar kommuniziert. Ladezustände werden angezeigt.

**Story 4.5: UI für Service-Account-Verwaltung innerhalb eines Mandanten (UI for Service Account Management within a Tenant)**

* **Als** Control Plane Administrator (oder Mandanten-Administrator mit entsprechenden Rechten) **möchte ich** einen UI-Bereich zur Verwaltung von Service-Accounts innerhalb eines ausgewählten Mandanten haben (Auflisten, Erstellen, Anzeigen, Bearbeiten, Status verwalten, Anmeldeinformationen verwalten, Ablaufdatum anzeigen/verwalten), **damit** der M2M-Zugriff visuell administriert werden kann.
* **Akzeptanzkriterien (ACs):**
    1. Innerhalb der Detailansicht eines Mandanten oder eines dedizierten Bereichs "Service-Account-Verwaltung" (auf einen Mandanten bezogen) zeigt ein Datenraster/eine Tabelle die Service-Accounts dieses Mandanten an (Spalten z.B. Client-ID, Beschreibung, Status, Erstellungsdatum, Ablaufdatum). Paginierung, Filterung, Sortierung werden unterstützt.
    2. Formulare und Aktionen ermöglichen das Erstellen neuer Service-Accounts (mit Anzeige des Standard-Ablaufdatums und ggf. Option zur Anpassung), die Anzeige von Details (einschließlich Client-ID und Ablaufdatum, aber *ohne* das Client Secret), die Bearbeitung von Informationen (z.B. Beschreibung, Status, Ablaufdatum) und die Verwaltung von Anmeldeinformationen (z.B. Option zum Neugenerieren des Client Secrets – das neue Secret wird dann *einmalig* angezeigt und muss vom Admin sicher kopiert werden; Client-ID ist sichtbar) (Aufruf der APIs aus der aktualisierten Story 3.5).
    3. Der Status (Aktiv, Deaktiviert) und das Ablaufdatum von Service-Accounts können über die UI verwaltet werden.
    4. Visuelle Indikatoren (z.B. Farbcodierung, Icons) weisen in der Liste und Detailansicht auf bald ablaufende oder bereits abgelaufene Service-Accounts hin.
    5. Die Benutzerinteraktionen sind intuitiv. Das Kopieren der Client-ID und des einmalig angezeigten Client Secrets wird durch UI-Elemente (z.B. "Kopieren"-Button) erleichtert.
    6. Klare Warnungen und Bestätigungsdialoge werden angezeigt, bevor ein Service-Account gelöscht/deaktiviert oder ein Client Secret rotiert/neu generiert wird.
    7. Validierung, Fehlerbehandlung und Ladezustände wie in Story 4.2 beschrieben.

---

**Epic 5: Kernmechanismus Lizenzmanagement (Core Licensing Mechanism)**
*Beschreibung:* Implementiert das Modul `eaf-licensing` mit Unterstützung für die Erstellung und Validierung von zeitlich begrenzten Lizenzen sowie für die Offline-Lizenzaktivierung und -validierung. Beinhaltet grundlegende API-Endpunkte im Backend der Control Plane für die interne Lizenzgenerierung.
*Wert:* Ermöglicht grundlegende Lizenzierungsfähigkeiten für Produkte, die auf dem EAF basieren.

**Story 5.1: Lizenz-Entität Definition & Sichere Speicherung (License Entity Definition & Secure Storage)**

* **Als** EAF-Entwickler **möchte ich** eine `License`-Entität definieren, die Zeitlimitierung unterstützt, sowie Mechanismen für deren sichere Speicherung, **damit** Lizenzen für EAF-basierte Produkte repräsentiert und verwaltet werden können.
* **Akzeptanzkriterien (ACs):**
    1. Eine `License`-Entität ist im Modul `eaf-licensing` definiert mit mindestens den Attributen: `licenseId` (UUID, PK, systemgeneriert), `productId` (String, identifiziert das lizenzierte Produkt/Modul eindeutig), `productVersion` (String, optional, für welche Produktversion die Lizenz gilt), `tenantId` (UUID, FK zu `tenants.tenantId`, wem die Lizenz ausgestellt wurde), `issueDate` (Timestamp), `validFrom` (Timestamp), `validUntil` (Timestamp, für Zeitlimitierung), `licenseKey` (String, eindeutig, sicher generiert und schwer zu erraten/fälschen), `status` (Enum: z.B. `PENDING_GENERATION`, `ISSUED`, `ACTIVE`, `EXPIRED`, `REVOKED`, `INVALID`), `activationType` (Enum: z.B. `OFFLINE`, `ONLINE`), `maxAllowedCpuCores` (Integer, nullable, für spätere Hardwarebindung aus Epic 9), `features` (z.B. JSONB oder Textfeld, das eine Liste aktivierter Feature-Flags oder -Module speichert), `signedLicenseData` (String/BLOB, speichert die kryptographisch signierten Lizenzinformationen für die Offline-Validierung).
    2. Eine PostgreSQL-Tabelle (`licenses`) wird mittels eines idempotenten Schema-Migrationsskripts (inkl. Rollback) erstellt. Notwendige Indizes (mindestens für `licenseId` (unique), `licenseKey` (unique), (`tenantId`, `productId`)) sind vorhanden.
    3. Ein robuster Mechanismus zur Generierung kryptographisch gesicherter, manipulationsgeschützter Lizenzschlüssel und/oder signierter Lizenzdateien (z.B. unter Verwendung von asymmetrischer Kryptographie wie RSA oder ECDSA) ist implementiert. Der private Schlüssel für die Signierung wird sicher verwaltet und ist nicht im EAF-Code oder der ausgelieferten Anwendung enthalten. Das Verfahren zur Schlüsselverwaltung (Erzeugung, Speicherung, Rotation des privaten Schlüssels) ist dokumentiert.
    4. Grundlegende Backend-Services im `eaf-licensing`-Modul zum Erstellen (inkl. Signierung), Abrufen und Aktualisieren (z.B. Statusänderung) von Lizenz-Entitäten sind implementiert. Diese Services validieren Eingabedaten und behandeln Datenbankfehler robust.
    5. Unit-Tests decken die Erstellung von Lizenzen (inkl. korrekter Signierung), das Abrufen und die Validierung von Lizenzattributen (z.B. `validFrom` muss vor `validUntil` liegen) ab. Fehlerfälle (z.B. ungültige Eingaben, Fehler bei Signierung) werden ebenfalls getestet.
    6. Das Format des `licenseKey` und der `signedLicenseData` ist klar definiert und versioniert, um zukünftige Änderungen zu ermöglichen.

**Story 5.2: Backend-API für interne Lizenzgenerierung (für ACCI-Team) (Backend API for Internal License Generation (for ACCI Team))**

* **Als** ACCI Lizenzmanager (über eine Backend-API der Control Plane) **möchte ich** neue, zeitlich begrenzte Lizenzen für spezifische Produkte und Mandanten generieren können, **damit** ich Lizenzen an Kunden ausstellen kann.
* **Akzeptanzkriterien (ACs):**
    1. Sichere Backend-API-Endpunkte werden bereitgestellt (z.B. unter `/api/controlplane/licenses`, nur für stark authentifiziertes und autorisiertes ACCI-Personal zugänglich, z.B. über eine dedizierte Admin-Rolle).
    2. Die Endpunkte unterstützen folgende Operationen mit klar definierten JSON Request/Response Payloads:
        * `POST /licenses`: Erstellt eine neue Lizenz. Erfordert mindestens `productId`, `tenantId`, `validFrom`, `validUntil` und ggf. eine Liste von `features`. Die API generiert intern den `licenseKey` und die `signedLicenseData` (für Offline-Aktivierung). Gibt HTTP 201 Created mit dem vollständigen Lizenzobjekt (inkl. `licenseKey` und `signedLicenseData` zum einmaligen Kopieren/Herunterladen) zurück. Validiert alle Eingaben.
        * `GET /licenses`: Listet generierte Lizenzen auf. Unterstützt Paginierung und umfassende Filterung (z.B. nach `productId`, `tenantId`, `status`, Gültigkeitszeitraum).
        * `GET /licenses/{licenseId}`: Ruft Details einer spezifischen Lizenz ab (inkl. aller Parameter außer dem privaten Signierschlüssel).
        * `PUT /licenses/{licenseId}/status`: Ermöglicht das Aktualisieren des Lizenzstatus (z.B. auf `REVOKED`). Validiert erlaubte Statusübergänge.
    3. Alle Eingaben an die API werden serverseitig validiert (z.B. gültige Daten, korrekte Produkt-IDs, `validFrom` vor `validUntil`). Fehler führen zu HTTP 400 mit detaillierten Problembeschreibungen.
    4. Die API-Dokumentation (OpenAPI 3.x) ist detailliert und aktuell, inklusive der Beschreibung, wie `signedLicenseData` für den Kunden bereitgestellt wird.
    5. Integrationstests decken alle API-Funktionalitäten, Validierungsregeln, Autorisierungsprüfungen und Fehlerfälle ab.
    6. Jede Lizenzgenerierung und jede Statusänderung wird im zentralen Audit-Log (Epic 10) detailliert erfasst.

**Story 5.3: Mechanismus zur Offline-Lizenzaktivierung & -validierung für EAF-Anwendungen (Offline License Activation & Validation Mechanism for EAF Applications)**

* **Als** Entwickler einer EAF-basierten Anwendung **möchte ich**, dass das EAF einen Mechanismus zur Offline-Aktivierung und -Validierung einer Lizenz bereitstellt (z.B. durch Import einer Lizenzdatei/-zeichenkette), **damit** meine Anwendung in Air-Gapped-Umgebungen ohne Internetverbindung laufen kann.
* **Akzeptanzkriterien (ACs):**
    1. Das Modul `eaf-licensing` stellt eine klare API oder einen Service (z.B. `LicenseActivationService.activateOffline(signedLicenseData)`) für eine EAF-basierte Anwendung bereit, um die `signedLicenseData` (aus Story 5.2) einzureichen.
    2. Die eingereichte `signedLicenseData` wird clientseitig (innerhalb der EAF-Anwendung) kryptographisch validiert (Signaturprüfung gegen den öffentlichen Schlüssel, Integritätsprüfung der Lizenzdaten). Der öffentliche Schlüssel muss sicher in der EAF-Anwendung hinterlegt sein.
    3. Bei erfolgreicher Validierung wird der Lizenzstatus (inkl. `validFrom`, `validUntil`, `features`, `productId`, `tenantId`, ggf. Hardware-Parameter aus Epic 9) sicher lokal auf dem System der Anwendung gespeichert (z.B. in einer geschützten Datei im Dateisystem der Anwendung oder einer lokalen Konfigurationsdatenbank). Der Speicherort muss vor einfachem User-Tampering geschützt sein, soweit unter den gegebenen Betriebssystem-Constraints möglich.
    4. Die EAF-Anwendung kann diesen lokal aktivierten Lizenzstatus zur Laufzeit über eine definierte Schnittstelle im `eaf-licensing`-Modul abfragen.
    5. Der Prozess zur Generierung der `signedLicenseData` (aus Story 5.2), deren sichere Übergabe an den Kunden und der Import/Aktivierung in einer EAF-Anwendung (inkl. Fehlerbehandlung bei fehlerhaftem Import) ist detailliert dokumentiert.
    6. Umfassende Testfälle demonstrieren die erfolgreiche Offline-Aktivierung und -Validierung mit gültigen Lizenzen sowie die korrekte Abweisung von manipulierten, abgelaufenen oder für ein anderes Produkt/Mandanten ausgestellten Lizenzen. Fehlermeldungen bei fehlgeschlagener Aktivierung/Validierung sind klar und für den Anwendungsentwickler diagnosefähig.
    7. Der Mechanismus ist robust gegen einfache Versuche, die lokale Lizenzdatei oder den Aktivierungsstatus zu manipulieren (z.B. durch Prüfsummen, interne Konsistenzchecks).

**Story 5.4: EAF-Werkzeuge für anwendungsseitige Lizenzprüfung (EAF Tooling for Application-Side License Checking)**

* **Als** Entwickler einer EAF-basierten Anwendung **möchte ich** einfache, vom EAF bereitgestellte Werkzeuge oder eine API haben, um den aktuellen Lizenzstatus zu prüfen (z.B. ist aktiv, Ablaufdatum, berechtigte Features), **damit** ich einfach lizenzgesteuertes Verhalten in meiner Anwendung implementieren kann.
* **Akzeptanzkriterien (ACs):**
    1. Das Modul `eaf-licensing` stellt eine klare, einfach zu nutzende API bereit (z.B. `LicenseService.isActive()`, `LicenseService.getLicenseDetails()`, `LicenseService.isFeatureEnabled("X")`, `LicenseService.getLicenseViolationReason()`).
    2. Dieser Service prüft gegen den lokal aktivierten Lizenzstatus (aus Story 5.3 für Offline-Lizenzen oder später aus Story 9.5 für Online-Lizenzen).
    3. Die API ist performant genug, um ggf. auch häufiger (z.B. bei Zugriff auf bestimmte Module/Features) aufgerufen zu werden, ohne die Anwendungsperformance signifikant zu beeinträchtigen (ggf. durch Caching des validierten Lizenzstatus im Speicher).
    4. Die API ist Thread-sicher.
    5. Die API ist gut dokumentiert mit Code-Beispielen, die zeigen, wie Entwickler darauf basierend Features freischalten oder sperren oder Warnmeldungen anzeigen können.
    6. Das EAF stellt grundlegende Informationen oder enum-basierte Rückgabewerte bereit, die eine Anwendung nutzen kann, um auf spezifische Lizenzverstöße (z.B. Lizenz abgelaufen, Feature nicht lizenziert, Hardware-Bindung verletzt) zu reagieren. Die konkrete Implementierung der Reaktion (z.B. Feature deaktivieren, Anwendung beenden, Warnung anzeigen) verbleibt in der Verantwortung der EAF-Anwendung.
    7. Unit-Tests decken die Lizenzprüfungs-API für verschiedene Lizenzzustände (aktiv, abgelaufen, Feature vorhanden/nicht vorhanden, etc.) und Fehlerfälle (z.B. keine Lizenz aktiviert) ab.
    8. Die API gibt klare und ggf. lokalisierbare Meldungen zurück, die eine Anwendung dem Endbenutzer im Falle von Lizenzproblemen anzeigen kann (oder zumindest Codes, die die Anwendung in lokalisierte Meldungen umwandeln kann).

---

**Epic 6: Internationalisierung (i18n) - Kernfunktionalität & Control Plane Integration (Internationalization (i18n) - Core Functionality & Control Plane Integration)**
*Beschreibung:* Implementiert das Modul `eaf-internationalization` mit Funktionen zum Laden von Übersetzungsdateien, sprachabhängiger Datenformatierung und Sprachumschaltung. Umfasst auch Funktionen in der Control Plane UI, die es Mandanten ermöglichen, ihre eigenen Sprachen und Übersetzungen zu verwalten.
*Wert:* Ermöglicht mehrsprachige Anwendungen und mandantenspezifische Sprachanpassungen.

**Story 6.1: EAF Kernmechanismus für i18n - Laden von Übersetzungsdateien & Nachrichtenauflösung (EAF Core i18n Mechanism - Translation File Loading & Message Resolution)**

* **Als** EAF-Entwickler **möchte ich**, dass das EAF einen robusten Mechanismus zum Laden von Übersetzungsdateien (z.B. Java ResourceBundles) und zur Auflösung internationalisierter Nachrichten basierend auf der Locale eines Benutzers bereitstellt, **damit** Anwendungen, die auf dem EAF basieren, einfach mehrere Sprachen unterstützen können.
* **Akzeptanzkriterien (ACs):**
    1. Das Modul `eaf-internationalization` definiert eine klare Strategie zur Organisation und zum Laden von Übersetzungsdateien (z.B. `.properties`-Dateien im UTF-8 Format, pro Sprache/Locale, unter Verwendung der Standard Java `ResourceBundle`-Konventionen). Die Strategie unterstützt das Laden von Bundles aus dem Classpath der Anwendung und potenziell aus externen Verzeichnissen für spätere Erweiterungen.
    2. Das EAF stellt einen zentralen, einfach zu nutzenden Service bereit (z.B. eine Fassade um Spring's `MessageSource`), den Anwendungen verwenden können, um lokalisierte Nachrichten anhand eines Schlüssels und optionaler Parameter abzurufen.
    3. Der Mechanismus unterstützt parametrisierte Nachrichten (z.B. "Hallo {0}, Sie haben {1} neue Nachrichten.") unter Verwendung des `java.text.MessageFormat`-Standards oder einer äquivalenten, sicheren Methode.
    4. Ein klar definierter Fallback-Mechanismus ist implementiert: Wenn eine Übersetzung für die angeforderte Locale und den Schlüssel nicht existiert, wird versucht, auf eine allgemeinere Sprache (z.B. von `de_CH` auf `de`) und schließlich auf eine konfigurierbare Primärsprache des EAF (z.B. Englisch oder Deutsch) zurückzufallen. Ist auch dort kein Schlüssel vorhanden, wird ein definierter Platzhalter (z.B. `???key_name???`) oder der Schlüssel selbst zurückgegeben, und ein Warning wird geloggt.
    5. Das Setup und die Nutzung des i18n-Mechanismus (inkl. Dateiorganisation, Schlüsselkonventionen, Nutzung des Message-Services) sind für Entwickler von EAF-Anwendungen detailliert dokumentiert.
    6. Umfassende Unit-Tests verifizieren die Nachrichtenauflösung für verschiedene Locales (inkl. Varianten wie `de_DE`, `de_CH`), das korrekte Fallback-Verhalten bei fehlenden Schlüsseln oder Sprachen und die korrekte Verarbeitung von parametrisierten Nachrichten. Das Verhalten bei malformatierten Resource-Bundle-Dateien (z.B. falsche Zeichenkodierung, Syntaxfehler) ist definiert (z.B. Fehler beim Start, Log-Warnung).
    7. Die Performance der Nachrichtenauflösung ist optimiert (z.B. durch Caching der geladenen ResourceBundles), um keinen signifikanten Overhead bei häufigen Aufrufen zu erzeugen.

**Story 6.2: EAF-Unterstützung für Locale-spezifische Datenformatierung (EAF Support for Locale-Specific Data Formatting)**

* **Als** EAF-Entwickler **möchte ich**, dass das EAF die Locale-spezifische Formatierung von Zahlen, Datums-, Zeit- und Währungsangaben erleichtert, **damit** Daten korrekt entsprechend der Sprache und den kulturellen Präferenzen des Benutzers dargestellt werden.
* **Akzeptanzkriterien (ACs):**
    1. Das Modul `eaf-internationalization` bietet Hilfsklassen oder klare Anleitungen und Beispiele zur Integration und Nutzung der Standard Java/Kotlin Bibliotheken (z.B. `java.text.NumberFormat`, `java.text.DateFormat`, `java.time.format.DateTimeFormatter`, `java.util.Currency`) für die Locale-abhängige Formatierung von Zahlen (Dezimalzahlen, Prozentwerte).
    2. Entsprechende Hilfsklassen/Anleitungen werden für die Locale-abhängige Formatierung von Datums- und Zeitangaben (kurzes, mittleres, langes Format) bereitgestellt. Die Nutzung von `java.time` wird empfohlen.
    3. Entsprechende Hilfsklassen/Anleitungen werden für die Locale-abhängige Formatierung von Währungsbeträgen (inkl. Währungssymbol und korrekter Positionierung) bereitgestellt.
    4. Beispiele und Dokumentation zeigen Entwicklern von EAF-Anwendungen, wie diese Formatierungsfähigkeiten sicher in Verbindung mit der aktuellen Locale des Benutzers (ermittelt über Story 6.3) genutzt werden können, sowohl in Backend-Logik (z.B. für das Generieren von Berichten) als auch in Frontend-Komponenten (ggf. durch Bereitstellung der Locale-Info für clientseitige Formatierung).
    5. Die Fehlerbehandlung bei ungültigen Locale-Angaben für Formatierungsfunktionen ist definiert (z.B. Fallback auf Default-Locale, Exception).
    6. Die Dokumentation weist auf mögliche Fallstricke bei der internationalen Formatierung hin (z.B. unterschiedliche Kalendersysteme, Zeitzonenproblematik – obwohl die tiefergehende Zeitzonenbehandlung über den reinen Formatierungsaspekt hinausgehen kann).

**Story 6.3: Verwaltung der Benutzersprachpräferenz & Sprachumschaltung in EAF-Anwendungen (User Language Preference Management & Switching in EAF Applications)**

* **Als** EAF-Entwickler **möchte ich**, dass das EAF eine einfache Möglichkeit für Anwendungen bereitstellt, die Sprachpräferenz eines Benutzers zu verwalten und Benutzern das Umschalten der Sprache zu ermöglichen, **damit** die Benutzeroberfläche der Anwendung in der vom Benutzer gewählten Sprache angezeigt werden kann.
* **Akzeptanzkriterien (ACs):**
    1. Das EAF stellt einen Mechanismus bereit, um die bevorzugte Locale des aktuellen Benutzers zu bestimmen. Die Prioritätenordnung ist: 1. Explizite Auswahl des Benutzers (persistent gespeichert), 2. `Accept-Language`-Header des Browsers, 3. Konfigurierte Default-Locale der Anwendung/des EAF.
    2. Das EAF unterstützt die Persistierung der expliziten Sprachpräferenz des Benutzers (z.B. als Teil des Benutzerprofils in der Datenbank (siehe `LocalUser`-Entität) oder als langlebiges Cookie/`localStorage`-Eintrag). Die gewählte Methode ist sicher und respektiert die Privatsphäre.
    3. Das EAF bietet wiederverwendbare Komponenten, serverseitige Hilfsmethoden oder klare Anleitungen für die Implementierung eines UI-Elements zur Sprachumschaltung (z.B. Dropdown-Menü mit verfügbaren Sprachen) in EAF-basierten Webanwendungen (insbesondere für die Control Plane UI).
    4. Die Änderung der Sprachpräferenz durch den Benutzer führt dazu, dass die Anwendungsoberfläche (bei der nächsten Navigation oder durch dynamisches Neuladen betroffener Komponenten) Texte und ggf. formatierte Daten in der neu ausgewählten Sprache anzeigt, unter Verwendung des i18n-Mechanismus aus Story 6.1 und 6.2.
    5. Die aktuell aktive Locale ist für die gesamte Dauer eines Requests serverseitig leicht zugänglich (z.B. über einen `LocaleContextHolder` oder ähnliches).
    6. Wenn eine persistierte Sprachpräferenz eines Benutzers auf eine nicht mehr unterstützte oder ungültige Sprache verweist, erfolgt ein definierter Fallback (z.B. auf die Default-Sprache der Anwendung) und der Benutzer wird ggf. informiert.

**Story 6.4: Control Plane API für mandantenspezifische Sprachverwaltung (Control Plane API for Tenant-Specific Language Management)**

* **Als** Mandanten-Administrator (über die Control Plane API) **möchte ich** neue benutzerdefinierte Sprachen für meinen Mandanten hinzufügen und deren Verfügbarkeit verwalten können, **damit** meine Instanz einer EAF-basierten Anwendung Sprachen über das Standardset hinaus unterstützen kann.
* **Akzeptanzkriterien (ACs):**
    1. Backend-API-Endpunkte (z.B. unter `/api/controlplane/tenants/{tenantId}/languages`) werden im EAF bereitgestellt und sind durch geeignete Berechtigungen für Mandanten-Administratoren gesichert.
    2. Die Endpunkte unterstützen folgende Operationen mit JSON Payloads:
        * `POST /languages`: Fügt einen neuen benutzerdefinierten Sprachcode (z.B. "fr-CA-custom", unter Einhaltung des BCP 47 Formats oder einer definierten Konvention) für den im Pfad genannten Mandanten hinzu. Validiert den Sprachcode auf Format und Eindeutigkeit pro Mandant.
        * `GET /languages`: Listet alle für den Mandanten verfügbaren Sprachen auf (Standard-Applikationssprachen und vom Mandanten hinzugefügte benutzerdefinierte Sprachen), inklusive ihres Aktivierungsstatus.
        * `PUT /languages/{langCode}`: Aktualisiert Eigenschaften einer benutzerdefinierten Sprache (z.B. Anzeigename, Aktivierungsstatus `enabled/disabled`). Standard-Applikationssprachen können nicht über diese API modifiziert werden (außer ggf. Aktivierung/Deaktivierung für den Mandanten).
        * `DELETE /languages/{langCode}`: Entfernt eine *benutzerdefinierte* Sprache für den Mandanten. Standard-Applikationssprachen können nicht gelöscht werden. Eine Bestätigung wird empfohlen. Das Löschen einer Sprache mit existierenden Übersetzungen führt zu definiertem Verhalten (z.B. Archivierung der Übersetzungen oder Fehler, falls noch aktiv genutzt).
    3. Das EAF stellt einen persistenten Speicher (z.B. eigene DB-Tabelle `tenant_languages`) für diese mandantenspezifischen Sprachkonfigurationen bereit, verknüpft mit der `tenantId`.
    4. Der Kern-i18n-Mechanismus des EAF (Story 6.1) ist in der Lage, diese vom Mandanten definierten Sprachen zu erkennen und (falls Übersetzungen vorhanden sind, siehe Story 6.5) für die Nachrichtenauflösung für Benutzer dieses Mandanten zu berücksichtigen.
    5. Alle Änderungen an den Spracheinstellungen eines Mandanten werden im Audit-Log erfasst.
    6. Validierungsfehler (z.B. ungültiger Sprachcode, Versuch, Standardsprache zu löschen) führen zu klaren HTTP 4xx Fehlermeldungen.

**Story 6.5: Control Plane API & UI für mandantenspezifische i18n-Textübersetzung (Control Plane API & UI for Tenant-Specific i18n Text Translation)**

* **Als** Mandanten-Administrator (über die Control Plane) **möchte ich** meine eigenen Übersetzungen für die i18n-Textschlüssel der Anwendung für meine benutzerdefinierten Sprachen bereitstellen und verwalten können (und potenziell Standardübersetzungen überschreiben können), **damit** ich die Anwendung vollständig für meine Benutzer lokalisieren kann.
* **Akzeptanzkriterien (ACs):**
    1. Backend-API-Endpunkte (z.B. unter `/api/controlplane/tenants/{tenantId}/languages/{langCode}/translations`) ermöglichen es Mandanten-Administratoren, Übersetzungen für alle relevanten i18n-Schlüssel für ihre (vom Mandanten aktivierten) Sprachen einzureichen und zu verwalten.
    2. Die API unterstützt mindestens:
        * `GET /`: Listet alle i18n-Schlüssel der Basisanwendung auf, idealerweise mit den Standardübersetzungen der Basissprache und den aktuellen mandantenspezifischen Übersetzungen für die angegebene `{langCode}`. Paginierung und Filterung nach Schlüssel oder Übersetzungsstatus (übersetzt/nicht übersetzt/überschrieben) wird unterstützt.
        * `PUT /{messageKey}`: Erstellt oder aktualisiert die mandantenspezifische Übersetzung für einen gegebenen `{messageKey}` und die angegebene `{langCode}`. Der Request-Body enthält den Übersetzungstext. Validiert Eingaben (z.B. auf maximale Länge, Verhinderung von XSS durch serverseitige Sanitisierung, falls die Texte direkt als HTML interpretiert werden könnten – besser ist jedoch, die Interpretation dem Frontend zu überlassen und hier nur Plain Text zu speichern).
        * `DELETE /{messageKey}`: Entfernt eine mandantenspezifische Übersetzung für einen Schlüssel (führt dazu, dass der Fallback auf die Standardübersetzung der Anwendung greift).
    3. Das EAF stellt einen persistenten Speicher (z.B. eigene DB-Tabelle `tenant_translations` mit `tenantId`, `langCode`, `messageKey`, `translationText`) für diese mandantenspezifischen Übersetzungen bereit.
    4. Der Kern-i18n-Nachrichtenauflösungsmechanismus des EAF (Story 6.1) priorisiert bei der Suche nach einer Übersetzung für einen gegebenen Mandanten und eine Locale die mandantenspezifischen Übersetzungen (aus diesem Speicher), bevor er auf die Standard-Anwendungsübersetzungen zurückfällt.
    5. Ein Bereich in der Control Plane UI (React-Admin basiert, zugänglich für Mandanten-Administratoren mit entsprechender Berechtigung) wird entwickelt, um:
        * Die für den Mandanten konfigurierten und aktivierten Sprachen aufzulisten (aus Story 6.4).
        * Die Auswahl einer Sprache zur Bearbeitung der Übersetzungen zu ermöglichen.
        * Eine paginierte und filterbare Liste von i18n-Schlüsseln anzuzeigen, daneben die Standardübersetzung (aus der Basisanwendung) und das Eingabefeld/Anzeige der mandantenspezifischen Übersetzung.
        * Eine intuitive Schnittstelle (z.B. Inline-Editierfelder, Speicher-Buttons pro Eintrag oder für eine Gruppe) bereitzustellen, über die Mandanten Übersetzungen für jeden Schlüssel in der ausgewählten Sprache eingeben, bearbeiten und löschen können. Änderungen werden über die API (siehe ACs 1-2) gespeichert.
        * Visuelles Feedback über den Speicherstatus (gespeichert, Fehler, ausstehend) zu geben.
    6. Die UI für die Übersetzungsverwaltung ist robust und benutzerfreundlich, auch bei einer großen Anzahl von Textschlüsseln (z.B. durch effiziente Paginierung, Such-/Filterfunktionen nach Schlüssel oder Inhalt).
    7. Änderungen an mandantenspezifischen Übersetzungen werden im Audit-Log erfasst.
    8. Die UI zeigt klar an, ob eine Übersetzung mandantenspezifisch ist oder vom Anwendungsstandard stammt.

---

**Epic 7: Fundament des Plugin-Systems (Plugin System Foundation)**
*Beschreibung:* Implementiert das Modul `eaf-plugin-system` (z.B. basierend auf der Java ServiceLoader API), das eine grundlegende Erweiterbarkeit des EAF durch andere Module ermöglicht. Beinhaltet ein einfaches Beispiel-Plugin zur Demonstration.
*Wert:* Stellt die Kernfunktionalität zur Erweiterung des EAF bereit und demonstriert diese.

**Story 7.1: Definition von EAF-Erweiterungspunkten mittels ServiceLoader-Interfaces (Define EAF Extension Points using ServiceLoader Interfaces)**

* **Als** EAF-Entwickler **möchte ich** klare Java/Kotlin-Interfaces innerhalb von `eaf-core` (oder einem dedizierten `eaf-plugin-api`-Modul, das minimale Abhängigkeiten hat) definieren, die als standardisierte Erweiterungspunkte (Service Provider Interfaces - SPIs) für Plugins dienen, **damit** verschiedene Teile des EAF und darauf basierende Anwendungen konsistent und typsicher erweitert werden können.
* **Akzeptanzkriterien (ACs):**
    1. Mindestens 2-3 verschiedene, fachlich sinnvolle Erweiterungspunkte sind für das MVP identifiziert und im `eaf-plugin-api`-Modul (oder `eaf-core`) definiert (z.B. `TenantLifecycleListener` für Reaktionen auf Mandanten-Events, `CustomCommandValidator` für zusätzliche Validierungslogik von Commands, `UIMenuItemProvider` zur dynamischen Erweiterung von Navigationsmenüs in EAF-basierten UIs).
    2. Für jeden Erweiterungspunkt ist ein klares, gut dokumentiertes Java/Kotlin-Interface definiert. Die Methoden der Interfaces sind präzise benannt, ihre Parameter und Rückgabewerte sind klar typisiert, und das erwartete Verhalten sowie mögliche Exceptions sind spezifiziert (KDoc/JavaDoc).
    3. Diese Interfaces sind als stabile, öffentliche API für Plugin-Entwickler konzipiert. Überlegungen zur Versionierung der SPIs und zur Vermeidung von Breaking Changes sind in der Design-Dokumentation festgehalten.
    4. Eine initiale Dokumentation für diese Erweiterungspunkt-Interfaces ist erstellt, die deren Zweck, typische Anwendungsfälle und grundlegende Implementierungshinweise erläutert.
    5. Die Kernlogik des EAF (in relevanten Modulen wie `eaf-core`, `eaf-iam` etc.) ist so refaktoriert oder entworfen, dass sie Implementierungen dieser Interfaces mittels des Java `ServiceLoader`-Mechanismus erkennt, lädt und an den entsprechenden Stellen im Programmablauf sicher aufruft. Fehler beim Aufruf einer Plugin-Methode (z.B. Exception im Plugin-Code) dürfen die Kernfunktionalität des EAF nicht gefährden (z.B. durch `try-catch`-Blöcke und entsprechendes Logging).

**Story 7.2: Implementierung des Plugin-Erkennungs- und Lademechanismus (Implement Plugin Discovery and Loading Mechanism)**

* **Als** EAF-Entwickler **möchte ich**, dass das Modul `eaf-plugin-system` einen robusten Mechanismus implementiert, um Plugins zur Laufzeit (oder beim Anwendungsstart) mittels Java `ServiceLoader` zu erkennen, zu laden und deren Instanzen zu verwalten, **damit** EAF-basierte Anwendungen einfach und standardisiert erweitert werden können.
* **Akzeptanzkriterien (ACs):**
    1. Das Modul `eaf-plugin-system` (oder eine Kernkomponente in `eaf-core`) enthält Logik, die `ServiceLoader.load(MyExtensionPointInterface.class)` verwendet, um alle im Classpath registrierten Plugin-Implementierungen für die in Story 7.1 definierten Erweiterungspunkte zu finden.
    2. Erkannte Plugin-Instanzen werden verwaltet. Dies beinhaltet:
        * Sichere Initialisierung der Plugin-Instanzen (ggf. unter Berücksichtigung von Dependency Injection, falls Plugins Spring Beans sind, was bei `ServiceLoader` zusätzliche Konfiguration erfordert oder indem Plugins einfache, parameterlose Konstruktoren haben).
        * Bereitstellung der Plugin-Instanzen für die Kernlogik des EAF (z.B. über eine Registry oder direkte Übergabe an die konsumierenden Komponenten).
    3. Das System behandelt Szenarien adäquat, in denen keine Plugins für einen Erweiterungspunkt gefunden werden (führt nicht zu Fehlern, sondern operiert mit Standardverhalten). Werden mehrere Plugins für denselben Punkt gefunden, werden entweder alle aufgerufen (z.B. bei Listenern) oder es gibt eine definierte Strategie zur Auswahl oder Priorisierung (falls nur eine Implementierung zulässig ist – für MVP werden alle aufgerufen, falls sinnvoll).
    4. Aussagekräftiges Logging (Level DEBUG oder INFO) ist implementiert, um anzuzeigen, welche Plugins für welche Erweiterungspunkte erkannt, geladen und ggf. initialisiert wurden. Fehler beim Laden oder Initialisieren eines Plugins werden klar geloggt (WARN oder ERROR) und führen nicht zum Absturz der Hauptanwendung, sondern das fehlerhafte Plugin wird übergangen.
    5. Der Prozess, wie ein Plugin (als separates JAR/Gradle-Modul) seine Service-Implementierungen für die Erkennung durch den `ServiceLoader` registriert (d.h. durch Erstellung einer Datei im Verzeichnis `META-INF/services/` mit dem vollqualifizierten Namen des Interface und darin dem vollqualifizierten Namen der Implementierungsklasse), ist klar und detailliert für Plugin-Entwickler dokumentiert.
    6. Die Performance des Plugin-Ladevorgangs beim Anwendungsstart wird überwacht und darf den Start nicht unverhältnismäßig verlangsamen.

**Story 7.3: Entwicklung eines einfachen Beispiel-Plugin-Moduls (Develop a Simple Example Plugin Module)**

* **Als** EAF-Entwickler (und als zukünftiger Plugin-Entwickler) **möchte ich** ein einfaches Beispiel-Plugin als separates Gradle-Modul innerhalb des Monorepos entwickelt haben, **damit** ich verstehen kann, wie man ein Plugin für das EAF erstellt, baut und integriert und wie es mit dem EAF interagiert.
* **Akzeptanzkriterien (ACs):**
    1. Ein neues Gradle-Modul (z.B. `eaf-example-plugin-auditor`) ist im Monorepo als eigenständiges Projekt erstellt. Es hat nur Abhängigkeiten zum `eaf-plugin-api`-Modul (oder `eaf-core` wo die SPIs liegen) und nicht umgekehrt.
    2. Dieses Modul implementiert mindestens eines der in Story 7.1 definierten EAF-Erweiterungspunkt-Interfaces (z.B. einen `TenantLifecycleListener`, der auf Mandantenerstellung lauscht).
    3. Das Beispiel-Plugin stellt eine einfache, aber klar überprüfbare Funktionalität bereit (z.B. loggt eine Nachricht mit Mandanten-ID, wenn ein neuer Mandant über die API aus Epic 2 erstellt wird, oder steuert einen Dummy-Eintrag zu einer Liste bei, die von einem anderen EAF-Service verwaltet wird).
    4. Das Beispiel-Plugin deklariert seine Service-Implementierungen korrekt in seiner `META-INF/services/`-Verzeichnisstruktur.
    5. Wenn die EAF-Kernanwendung (z.B. `eaf-core` oder eine dedizierte Testanwendung, die Plugins lädt) startet und das `eaf-example-plugin-auditor`-Modul (als JAR) in ihrem Classpath enthalten ist, wird das Plugin vom `eaf-plugin-system` (Story 7.2) erkannt und geladen.
    6. Die Funktionalität des Beispiel-Plugins wird bei entsprechenden Aktionen im EAF (z.B. Erstellen eines Mandanten) nachweislich aufgerufen und ist beobachtbar (z.B. durch die geloggte Nachricht oder den beigesteuerten Dummy-Eintrag).
    7. Das Beispiel-Plugin enthält eigene Unit-Tests für seine interne Logik.
    8. Die Struktur, der Build-Prozess (`build.gradle.kts`) und die Konfiguration des Beispiel-Plugins sind minimal gehalten und dienen als klare Vorlage für die zukünftige Entwicklung weiterer Plugins.

**Story 7.4: Dokumentation des EAF-Plugin-Entwicklungsprozesses (Document EAF Plugin Development Process)**

* **Als** Entwickler, der beabsichtigt, ein Plugin für eine EAF-basierte Anwendung zu erstellen, **möchte ich** eine klare und umfassende Dokumentation darüber haben, wie man ein Plugin entwickelt, konfiguriert, baut, paketiert und in einer EAF-Anwendung bereitstellt, **damit** ich die Funktionalität der Anwendung effektiv, sicher und standardkonform erweitern kann.
* **Akzeptanzkriterien (ACs):**
    1. Die Entwicklerdokumentation (siehe Epic 10) enthält einen dedizierten Abschnitt zur Plugin-Entwicklung.
    2. Dieser Abschnitt beschreibt detailliert alle offiziell unterstützten EAF-Erweiterungspunkt-Interfaces (SPIs aus Story 7.1), deren Methoden, erwartetes Verhalten und Anwendungsbeispiele.
    3. Die Dokumentation erläutert den Java `ServiceLoader`-Mechanismus, die Erstellung der `META-INF/services/`-Dateien und wie ein Plugin-Service korrekt registriert wird.
    4. Eine Schritt-für-Schritt-Anleitung, die das `eaf-example-plugin-auditor` (aus Story 7.3) als Referenz verwendet, führt den Entwickler durch den gesamten Prozess der Plugin-Erstellung, von der Moduldefinition im Gradle-Build bis zur Verifizierung der Plugin-Funktionalität.
    5. Wichtige Überlegungen für Plugin-Entwickler werden behandelt:
        * Best Practices für das Design von Plugin-Implementierungen (z.B. Zustandslosigkeit, Performance-Aspekte, Fehlerbehandlung innerhalb des Plugins, um das Host-System nicht zu beeinträchtigen).
        * Abhängigkeitsmanagement für Plugins (wie man Konflikte mit EAF-internen oder anderen Plugin-Abhängigkeiten minimiert).
        * Grundlegende Hinweise zur Versionierung von Plugins im Verhältnis zur EAF-Version und zu den SPI-Versionen.
        * Sicherheitsaspekte (z.B. dass Plugins im selben Security Context wie das EAF laufen und welche Verantwortung daraus für den Plugin-Entwickler erwächst).
    6. Ein Troubleshooting-Guide für häufige Probleme bei der Plugin-Entwicklung oder -Integration ist enthalten.
    7. Die Dokumentation ist aktuell zur implementierten Funktionalität und wird bei Änderungen an den SPIs oder dem Lademechanismus angepasst.

---

**Epic 8: Erweitertes IAM - Externe Authentifizierungs-Provider (Advanced IAM - External Authentication Providers)**
*Beschreibung:* Erweitert das Modul `eaf-iam` um die Unterstützung für die Konfiguration von externen Authentifizierungs-Providern (LDAP/AD, OAuth2/OIDC, SAML2) auf mandantenspezifischer Basis.
*Wert:* Bietet flexible Authentifizierungsoptionen für Enterprise-Kunden.

**Story 8.1: Definition & Persistenz von Konfigurationen für externe Authentifizierungs-Provider (pro Mandant) (Define & Persist External Authentication Provider Configuration (per Tenant))**

* **Als** EAF-Entwickler **möchte ich** ein Datenmodell und Persistenzmechanismen für mandantenspezifische Konfigurationen externer Authentifizierungs-Provider (LDAP/AD, OAuth2/OIDC, SAML2) definieren, **damit** Mandanten ihre bevorzugten Identity Provider sicher und flexibel einrichten können.
* **Akzeptanzkriterien (ACs):**
    1. Generische und spezifische Datenmodelle für `ExternalAuthProviderConfig` sind innerhalb des `eaf-iam`-Moduls definiert. Eine Basis-Entität enthält gemeinsame Attribute (`id`, `tenantId`, `providerType` (Enum: `LDAP`, `OIDC`, `SAML`), `name` (vom Mandanten vergeben, eindeutig pro Mandant), `isEnabled` (boolean)). Spezifische Entitäten erben davon und fügen provider-spezifische Einstellungen hinzu:
        * **LDAP/AD:** `serverUrl` (validierte URL), `baseDnUsers`, `baseDnGroups`, `bindUserDn` (optional), `bindUserPassword` (verschlüsselt gespeichert), `userSearchFilter`, `groupSearchFilter`, `userAttributeForUsername`, `userAttributeForEmail`, `groupAttributeForRole`, `connectionTimeoutMillis`, `readTimeoutMillis`, `useSsl/StartTls` (boolean).
        * **OAuth2/OIDC:** `clientId`, `clientSecret` (verschlüsselt gespeichert), `authorizationEndpointUrl`, `tokenEndpointUrl`, `userInfoEndpointUrl` (optional), `jwkSetUri` (optional), `issuerUrl` (für OIDC Discovery), `defaultScopes` (kommaseparierte Liste), `userNameAttribute` (aus UserInfo/ID Token), `emailAttribute`, `groupsClaimName` (für Rollenmapping).
        * **SAML2:** `idpMetadataUrl` (URL zum IdP-Metadaten-XML) ODER `idpEntityId`, `idpSsoUrl`, `idpX509Certificate` (PEM-Format), `spEntityId` (vom EAF generiert/konfigurierbar), `spAcsUrl` (Assertion Consumer Service URL, vom EAF bereitgestellt), `nameIdPolicyFormat`, `attributeConsumingServiceIndex` (optional), `attributesForUsername`, `attributesForEmail`, `attributesForGroups`.
    2. Jede Konfiguration ist eindeutig einer `tenantId` zugeordnet. Ein Mandant kann mehrere Konfigurationen desselben oder unterschiedlicher Typen haben (z.B. zwei LDAP-Server, ein OIDC-Provider).
    3. Eine PostgreSQL-Tabelle (oder mehrere normalisierte Tabellen) wird mittels idempotenter Schema-Migrationsskripts (inkl. Rollback) zur Speicherung dieser Konfigurationen erstellt. Sensible Informationen (Client Secrets, Bind-Passwörter) werden vor der Persistierung stark verschlüsselt (z.B. mit AES-GCM unter Verwendung eines Master-Schlüssels, der sicher verwaltet wird – nicht im Code!).
    4. Backend-Services im `eaf-iam`-Modul für CRUD-Operationen dieser Konfigurationen sind implementiert, inklusive Validierung aller spezifischen Parameter (z.B. gültige URLs, korrekte Formate).
    5. Unit-Tests decken die Erstellung, Validierung und das sichere Speichern/Abrufen (inkl. Ver-/Entschlüsselung) der Konfigurationen für jeden Provider-Typ ab. Fehlerfälle bei ungültigen Konfigurationen werden getestet.

**Story 8.2: Control Plane API für die Verwaltung von Konfigurationen externer Auth-Provider (Control Plane API for Managing External Auth Provider Configurations)**

* **Als** Mandanten-Administrator (über die Control Plane API) **möchte ich** externe Authentifizierungs-Provider für meinen Mandanten konfigurieren und verwalten können, **damit** meine Benutzer sich mit ihren bestehenden Enterprise-Anmeldeinformationen anmelden können.
* **Akzeptanzkriterien (ACs):**
    1. RESTful API-Endpunkte werden vom `eaf-iam`-Modul bereitgestellt (z.B. unter `/api/controlplane/tenants/{tenantId}/auth-providers`) und sind durch geeignete Berechtigungen für Mandanten-Administratoren gesichert.
    2. Die Endpunkte unterstützen vollständige CRUD-Operationen (POST, GET-Liste, GET-Details, PUT, DELETE) für LDAP/AD-, OAuth2/OIDC- und SAML2-Provider-Konfigurationen für den im Pfad angegebenen Mandanten. Die PUT-Methode aktualisiert die gesamte Konfiguration, PATCH kann für Teilaktualisierungen (z.B. nur `isEnabled`-Status) angeboten werden.
    3. Die API erlaubt das Aktivieren (`isEnabled=true`) und Deaktivieren (`isEnabled=false`) spezifischer Provider-Konfigurationen für einen Mandanten. Nur aktivierte Provider werden im Login-Prozess berücksichtigt.
    4. Sensible Informationen (z.B. Client Secrets, Bind-Passwörter) werden in API-Requests sicher gehandhabt (z.B. nur beim Erstellen oder expliziten Aktualisieren eines Secrets übergeben, niemals in GET-Antworten zurückgegeben – stattdessen Platzhalter wie "*******" oder Status "gesetzt/nicht gesetzt").
    5. Die API validiert alle eingehenden Konfigurationsdaten serverseitig gegen die in Story 8.1 definierten Modelle und gibt bei Fehlern detaillierte HTTP 400-Antworten (RFC 7807 Problem Details) zurück.
    6. Eine aktuelle API-Dokumentation (OpenAPI 3.x) ist für diese Endpunkte verfügbar und beschreibt alle Parameter, Schemata und Sicherheitsanforderungen.
    7. Integrationstests decken alle API-Endpunkte ab, inklusive verschiedener Konfigurationsszenarien, Validierungsfehler und Autorisierungsprüfungen.
    8. Alle Änderungen an den Konfigurationen externer Authentifizierungs-Provider werden im Audit-Log erfasst.

**Story 8.3: EAF-Integration mit LDAP/Active Directory Authentifizierung (EAF Integration with LDAP/Active Directory Authentication)**

* **Als** Benutzer eines Mandanten, der mit LDAP/AD konfiguriert ist, **möchte ich** mich bei EAF-basierten Anwendungen mit meinen LDAP/AD-Anmeldeinformationen authentifizieren können, **damit** ich kein separates EAF-Passwort benötige und Single Sign-On innerhalb meiner Organisation nutzen kann.
* **Akzeptanzkriterien (ACs):**
    1. Der Authentifizierungsfluss des EAF (z.B. über einen angepassten Spring Security `AuthenticationProvider`) kann die Authentifizierung an einen oder mehrere für den Mandanten konfigurierte und aktivierte LDAP/AD-Provider delegieren. Die Auswahl des zu verwendenden Providers (falls mehrere konfiguriert sind) erfolgt anhand von Kriterien (z.B. E-Mail-Domäne des Benutzers, explizite Auswahl im Login-Formular).
    2. Das EAF verbindet sich sicher (unterstützt LDAPS/StartTLS) mit dem konfigurierten LDAP/AD-Server, sucht den Benutzer anhand des konfigurierten Suchfilters und validiert die vom Benutzer eingegebenen Anmeldeinformationen durch einen Bind-Versuch.
    3. Bei erfolgreicher LDAP/AD-Authentifizierung wird eine EAF-Session/ein Zugriffstoken (JWT) für den Benutzer erstellt (analog zu Story 3.3).
    4. Benutzerattribute (z.B. E-Mail, Vorname, Nachname, Telefonnummer) werden gemäß einer konfigurierbaren Mapping-Definition aus den LDAP/AD-Attributen in die EAF-Benutzerrepräsentation übernommen. Ein Schatten-`LocalUser`-Konto wird beim ersten erfolgreichen Login JIT (Just-In-Time) erstellt oder ein bestehendes aktualisiert (Status, Attribute).
    5. Ein grundlegendes Rollenmapping von LDAP/AD-Gruppenmitgliedschaften zu EAF-Rollen (aus Story 3.4) wird unterstützt. Die Konfiguration des Mappings (LDAP-Gruppe zu EAF-Rolle) ist Teil der LDAP-Provider-Konfiguration.
    6. Die Konfiguration und Nutzung mehrerer LDAP/AD-Server pro Mandant ist möglich und wird im Login-Prozess korrekt berücksichtigt.
    7. Eine robuste Fehlerbehandlung für LDAP-Konnektivitätsprobleme (z.B. Server nicht erreichbar, Timeout, Zertifikatsfehler), Authentifizierungsfehler (falsches Passwort, Benutzer nicht gefunden, Account gesperrt in AD) und Konfigurationsfehler ist implementiert und gibt dem Benutzer ggf. verständliche (aber sichere) Fehlermeldungen. Alle Fehler werden serverseitig detailliert geloggt.
    8. Die LDAP-Integration ist durch Integrationstests (mit einem Test-LDAP-Server, z.B. Docker-basiert) abgesichert.

**Story 8.4: EAF-Integration mit OAuth 2.0 / OpenID Connect (OIDC) Authentifizierung (EAF Integration with OAuth 2.0 / OpenID Connect (OIDC) Authentication)**

* **Als** Benutzer eines Mandanten, der mit einem OIDC-Provider konfiguriert ist, **möchte ich** mich bei EAF-basierten Anwendungen über diesen OIDC-Provider (z.B. Firmen-SSO, Google, Microsoft) authentifizieren können, **damit** ich bestehende Login-Sessions nutzen und von den Sicherheitsfeatures des IdP profitieren kann.
* **Akzeptanzkriterien (ACs):**
    1. Der Authentifizierungsfluss des EAF (unter Verwendung der Spring Security OAuth2/OIDC Client-Unterstützung) kann Benutzer zur Authentifizierung an den für den Mandanten konfigurierten und aktivierten OIDC-Provider weiterleiten (Authorization Code Flow mit PKCE wird bevorzugt).
    2. Das EAF handelt den OIDC-Callback sicher ab, validiert das ID-Token (Signatur, Issuer, Audience, Nonce, Expiration), tauscht den Authorization Code gegen ein Access Token und ruft optional den UserInfo-Endpunkt auf.
    3. Bei erfolgreicher OIDC-Authentifizierung wird eine EAF-Session/ein Zugriffstoken (JWT) für den Benutzer erstellt.
    4. Benutzerattribute (gemäß den konfigurierten Attribut-Mappings aus `userNameAttribute`, `emailAttribute` etc. der OIDC-Provider-Konfiguration) aus den Claims des ID-Tokens oder der UserInfo-Antwort werden in die EAF-Benutzerrepräsentation übernommen (JIT-Provisionierung/Aktualisierung eines Schatten-`LocalUser`-Kontos).
    5. Ein grundlegendes Rollenmapping von OIDC-Claims (z.B. `groups`, `roles` oder benutzerdefinierte Claims) zu EAF-Rollen wird unterstützt. Das Mapping ist Teil der OIDC-Provider-Konfiguration.
    6. Die Konfiguration mehrerer OIDC-Provider pro Mandant ist möglich (z.B. Anzeige mehrerer "Login mit..." Buttons).
    7. Eine robuste Fehlerbehandlung für alle Schritte des OIDC-Flows (z.B. Fehler vom IdP, Token-Validierungsfehler, Netzwerkprobleme) ist implementiert. Fehler werden geloggt und dem Benutzer ggf. angezeigt.
    8. Die OIDC-Integration ist durch Integrationstests (ggf. mit einem mock IdP oder einem konfigurierbaren Test-IdP) abgesichert. Sicherheitsaspekte wie State-Parameter-Validierung gegen CSRF sind implementiert.

**Story 8.5: EAF-Integration mit SAML 2.0 Authentifizierung (EAF Integration with SAML 2.0 Authentication)**

* **Als** Benutzer eines Mandanten, der mit einem SAML IdP konfiguriert ist, **möchte ich** mich bei EAF-basierten Anwendungen über diesen SAML IdP authentifizieren können, **damit** ich die föderierte Enterprise-Authentifizierung und Single Sign-On nutzen kann.
* **Akzeptanzkriterien (ACs):**
    1. Der Authentifizierungsfluss des EAF (unter Verwendung der Spring Security SAML-Unterstützung) kann als SAML Service Provider (SP) agieren und sich mit mandantenkonfigurierten SAML Identity Providern (IdPs) integrieren (SP-initiated SSO Flow). Das EAF stellt eigene SP-Metadaten bereit.
    2. Das EAF kann SAML-Authentifizierungsanfragen (AuthnRequests) generieren, Benutzer zum IdP weiterleiten und eingehende SAML-Antworten (Assertions) sicher empfangen und validieren (Signatur, Bedingungen, Audience Restriction, Subject Confirmation).
    3. Bei erfolgreicher SAML-Authentifizierung wird eine EAF-Session/ein Zugriffstoken (JWT) für den Benutzer erstellt.
    4. Benutzerattribute aus der SAML-Assertion (gemäß der konfigurierten Attribut-Mappings) werden in die EAF-Benutzerrepräsentation übernommen (JIT-Provisionierung/Aktualisierung eines Schatten-`LocalUser`-Kontos).
    5. Ein grundlegendes Rollenmapping von SAML-Attributen (z.B. `memberOf`, `eduPersonAffiliation`) zu EAF-Rollen wird unterstützt. Das Mapping ist Teil der SAML-Provider-Konfiguration.
    6. Die Konfiguration mehrerer SAML IdPs pro Mandant ist möglich.
    7. Eine robuste Fehlerbehandlung für alle Schritte des SAML-Flows (z.B. ungültige Assertion, Fehler vom IdP, Konfigurationsfehler) ist implementiert. Fehler werden geloggt und dem Benutzer ggf. angezeigt.
    8. Die SAML-Integration ist durch Integrationstests (ggf. mit einem mock IdP oder einem konfigurierbaren Test-IdP wie simplesamlphp) abgesichert. Aspekte wie sicherer Austausch von Zertifikaten und Metadaten sind berücksichtigt.

**Story 8.6: Control Plane UI für die Verwaltung von Konfigurationen externer Auth-Provider (Control Plane UI for Managing External Auth Provider Configurations)**

* **Als** Mandanten-Administrator **möchte ich** einen UI-Bereich in der Control Plane haben, um LDAP/AD-, OAuth2/OIDC- und SAML2-Authentifizierungs-Provider für meinen Mandanten zu konfigurieren und zu verwalten, **damit** ich die Integration externer Identitäten visuell einrichten und den Login-Prozess für meine Benutzer steuern kann.
* **Akzeptanzkriterien (ACs):**
    1. Ein Bereich "Authentifizierungs-Provider" ist in der Control Plane UI verfügbar (innerhalb des Mandantenkontexts) und über die Navigation erreichbar (nur für berechtigte Mandanten-Administratoren).
    2. Die UI ermöglicht das Auflisten der für den Mandanten bereits konfigurierten externen Authentifizierungs-Provider, inklusive ihres Typs (LDAP, OIDC, SAML) und ihres Aktivierungsstatus (`isEnabled`).
    3. Spezifische Formulare für jeden Provider-Typ (LDAP, OIDC, SAML2) werden bereitgestellt, um neue Provider-Konfigurationen hinzuzufügen. Diese Formulare erfassen alle in Story 8.1 definierten Parameter und bieten Hilfetexte/Tooltips für komplexe Felder. Clientseitige Validierung für Pflichtfelder und Formate wird durchgeführt.
    4. Die UI erlaubt das Bearbeiten und Löschen/Deaktivieren bestehender Provider-Konfigurationen. Sensible Felder wie Client Secrets oder Bind-Passwörter werden beim Bearbeiten nicht angezeigt, können aber neu gesetzt werden ("Ändern"-Option).
    5. Die UI zeigt klar den Aktivierungsstatus jedes Providers an und erlaubt dessen Änderung. Es wird sichergestellt, dass nicht versehentlich alle Authentifizierungsmethoden deaktiviert werden, sodass sich niemand mehr anmelden kann (z.B. mindestens ein lokaler Admin-Zugang oder ein Provider muss aktiv bleiben, falls dies die einzige Login-Möglichkeit ist).
    6. Die Benutzerinteraktionen sind intuitiv und folgen dem "React-Admin"-Stil. Ladezustände und Fehlermeldungen (sowohl clientseitige Validierungsfehler als auch serverseitige API-Fehler) werden dem Benutzer klar und kontextbezogen angezeigt.
    7. Für jeden Provider-Typ könnte eine "Testverbindung" (falls technisch sinnvoll und sicher implementierbar, z.B. für LDAP) angeboten werden, um die Konfiguration vor der Aktivierung zu überprüfen.
    8. Die Reihenfolge, in der aktivierte Provider dem Benutzer ggf. auf einer Login-Seite angeboten werden, ist konfigurierbar.

---

**Epic 9: Erweitertes Lizenzmanagement - Hardware-Bindung & Online-Aktivierung (Advanced Licensing - Hardware Binding & Online Activation)**
*Beschreibung:* Erweitert `eaf-licensing` um hardwaregebundene Lizenzierungsoptionen (z.B. CPU-Kerne für ppc64le). Implementiert oder bereitet die Struktur für den "License Activation Server" als interne EAF-Anwendung für die Online-Lizenzaktivierung vor.
*Wert:* Unterstützt komplexere Lizenzmodelle und die Online-Aktivierung.

**Story 9.1: Mechanismus zur Erfassung von Hardware-Parametern (ppc64le CPU-Kerne) (Mechanism for Hardware Parameter Collection (ppc64le CPU Cores))**

* **Als** EAF-Entwickler **möchte ich**, dass das Modul `eaf-licensing` einen Mechanismus bereitstellt, mit dem eine EAF-basierte Anwendung relevante Hardware-Parameter, insbesondere die Anzahl der verwaltbaren CPU-Kerne auf einem IBM POWER (ppc64le)-System, erfassen kann, **damit** diese Information für hardwaregebundene Lizenzen verwendet werden kann.
* **Akzeptanzkriterien (ACs):**
    1. Eine Methode/ein Service ist innerhalb von `eaf-licensing` (oder einem System-Utility-Modul, auf das `eaf-licensing` zugreifen kann) implementiert, um die Anzahl der aktiven/lizenzierten CPU-Kerne auf dem ppc64le-System zu bestimmen, auf dem die EAF-Anwendung läuft. Die Methode muss spezifisch für Linux auf ppc64le zuverlässig funktionieren (z.B. durch Auswertung von `/proc/cpuinfo` oder systemnahen Befehlen).
    2. Der Mechanismus ist so gestaltet, dass er gegen einfache Manipulationen (z.B. Ändern von Umgebungsvariablen) gehärtet ist, soweit dies softwareseitig möglich ist. Die Grenzen der Manipulationssicherheit sind dokumentiert.
    3. Die erfassten Hardware-Informationen (Anzahl CPU-Kerne) können von der Lizenzvalidierungslogik des EAF zuverlässig und in einem standardisierten Format abgerufen werden.
    4. Die Methode zur Hardware-Erfassung ist für Entwickler von EAF-Anwendungen dokumentiert, einschließlich etwaiger notwendiger Betriebssystem-Berechtigungen oder Konfigurationen für die Anwendung.
    5. Eine robuste Fehlerbehandlung ist implementiert für Fälle, in denen Hardware-Informationen nicht abgerufen werden können oder das Ergebnis nicht eindeutig ist (z.B. das System ist keine ppc64le-Architektur, `/proc/cpuinfo` ist nicht lesbar). In solchen Fällen wird ein definierter Fehlerwert oder eine Exception zurückgegeben.
    6. Das Verhalten bei dynamischen Änderungen der CPU-Kernanzahl während der Laufzeit der Anwendung (z.B. bei VMs, die im laufenden Betrieb skaliert werden) ist definiert (z.B. Lizenzprüfung erfolgt beim Start und/oder periodisch; eine Änderung kann einen Lizenzverstoß auslösen).
    7. Unit-Tests (ggf. mit gemockten Systemaufrufen) und Integrationstests auf einer ppc64le-Testumgebung validieren die korrekte Erfassung der CPU-Kernanzahl und das Fehlerverhalten.

**Story 9.2: Erweiterung der Lizenz-Entität & -Generierung für Hardware-Bindung (Extend License Entity & Generation for Hardware Binding)**

* **Als** ACCI Lizenzmanager (über die Backend-API der Control Plane) **möchte ich** Lizenzen generieren können, die Hardware-Bindungsparameter (z.B. maximale CPU-Kerne) enthalten, **damit** die Produktnutzung basierend auf der Systemhardware limitiert werden kann.
* **Akzeptanzkriterien (ACs):**
    1. Die `License`-Entität (aus Story 5.1) ist im `eaf-licensing`-Modul und der Datenbanktabelle `licenses` um Felder für Hardware-Bindungsparameter erweitert, spezifisch `maxAllowedCpuCores` (Integer, nullable).
    2. Die Backend-API zur Lizenzgenerierung (aus Story 5.2, `/api/controlplane/licenses`) ist aktualisiert, um die Angabe von `maxAllowedCpuCores` beim Erstellen oder Aktualisieren einer neuen Lizenz zu ermöglichen. Die Eingabe wird validiert (z.B. positive Ganzzahl, Plausibilitätsgrenzen).
    3. Die generierte `signedLicenseData` (für Offline-Aktivierung) oder die Datenstruktur für die Online-Aktivierung enthält diese Hardware-Bindungsparameter sicher und manipulationsgeschützt.
    4. Die Control Plane UI (falls bereits für Lizenzmanagement erweitert, ansonsten ist dies eine Anforderung an eine spätere UI-Story) ermöglicht die Eingabe von `maxAllowedCpuCores` bei der Lizenzdefinition.
    5. Die API-Dokumentation und die interne Dokumentation für Lizenzmanager spiegeln diese Erweiterung wider.
    6. Tests stellen sicher, dass Lizenzen korrekt mit und ohne Hardware-Bindungsparameter erstellt und gespeichert werden können.

**Story 9.3: Implementierung der hardwaregebundenen Lizenzvalidierung in EAF-Anwendungen (Implement Hardware-Bound License Validation in EAF Applications)**

* **Als** Entwickler einer EAF-basierten Anwendung **möchte ich**, dass der Lizenzvalidierungsmechanismus des EAF Hardware-Bindungsparameter (z.B. CPU-Kerne) gegen die tatsächliche Systemhardware prüft, **damit** die Lizenzkonformität durchgesetzt werden kann.
* **Akzeptanzkriterien (ACs):**
    1. Die Lizenzvalidierungslogik des EAF (aus Story 5.3 und 5.4) ist erweitert, um:
        * Die in der aktivierten Lizenz gespeicherten Hardware-Bindungsparameter (z.B. `maxAllowedCpuCores`) auszulesen.
        * Die tatsächlichen Hardware-Parameter vom System zu erfassen (unter Verwendung des Mechanismus aus Story 9.1).
        * Die lizenzierten Parameter mit den tatsächlichen Systemparametern zu vergleichen (z.B. `actualCpuCores <= licensedMaxCpuCores`).
    2. Das Ergebnis der Validierung der Hardware-Bindung wird klar signalisiert (z.B. als Teil des Gesamt-Lizenzstatus oder als spezifischer Verstoßgrund).
    3. EAF-basierte Anwendungen können dieses Validierungsergebnis nutzen, um ihr Verhalten anzupassen (z.B. Start verweigern, Funktionalität einschränken, Warnungen ausgeben). Die EAF-Lizenz-API (Story 5.4) liefert entsprechende Informationen.
    4. Umfassende Testfälle (sowohl Unit- als auch Integrationstests auf einer ppc64le-Umgebung) demonstrieren die korrekte Validierung gegen übereinstimmende und nicht übereinstimmende Hardware-Parameter (z.B. mehr CPU-Kerne als lizenziert, weniger CPU-Kerne als lizenziert). Das Verhalten bei nicht abrufbaren Hardware-Informationen ist ebenfalls definiert und getestet (z.B. Lizenz gilt als ungültig oder es erfolgt ein Fallback-Verhalten).
    5. Die Dokumentation für Entwickler beschreibt, wie die Hardware-Bindung funktioniert und wie Anwendungen darauf reagieren können.

**Story 9.4: Design & Grundgerüst für den Online License Activation Server (Design & Scaffolding for Online License Activation Server)**

* **Als** EAF-Entwicklungsteam **möchte ich** das grundlegende Design und das Projektgerüst für einen "Online License Activation Server" (der selbst als EAF-basierte Anwendung gebaut wird) haben, **damit** ein zentraler Dienst für die Online-Lizenzaktivierung und -validierung entwickelt werden kann.
* **Akzeptanzkriterien (ACs):**
    1. Ein High-Level-Design-Dokument für den Online License Activation Server ist erstellt. Es skizziert die Kernverantwortlichkeiten:
        * Empfang von Aktivierungsanfragen von EAF-Anwendungen (mit Lizenzschlüssel und ggf. Hardware-Identifikatoren).
        * Validierung des Lizenzschlüssels gegen die zentrale Datenbank der von ACCI ausgestellten Lizenzen (aus Story 5.1/5.2).
        * Speicherung von Aktivierungsdatensätzen (welche Lizenz auf welchem System/Hardware-Fingerprint wann aktiviert wurde).
        * Ausgabe von Aktivierungsbestätigungen oder -tokens an die anfragende Anwendung.
        * Handhabung periodischer Re-Validierungsanfragen ("Pings") von aktivierten Anwendungen.
        * Mechanismen zur Deaktivierung/Invalidierung von Lizenzen serverseitig.
    2. Ein neues Gradle-Modul (z.B. `eaf-license-server`) ist im Monorepo erstellt und als EAF-basierte Spring Boot-Anwendung konfiguriert (nutzt `eaf-core`, `eaf-observability`, etc.).
    3. Grundlegende RESTful API-Endpunkte für die Lizenzaktivierung (z.B. `POST /api/license/activate`) und -validierung (z.B. `POST /api/license/validate`) sind als Stubs (ohne vollständige Implementierung, aber mit definierten Request/Response-Strukturen) im `eaf-license-server`-Modul vorhanden.
    4. Das Design sieht vor, wie der Lizenzserver auf die Datenbank der von ACCI generierten Lizenzen (aus Story 5.1) zugreift (z.B. direkter DB-Zugriff, interne API). Für MVP könnte dies dieselbe DB sein, aber das Design sollte auch eine getrennte DB ermöglichen.
    5. Grundlegende Sicherheitsüberlegungen für den Server sind dokumentiert (Schutz der Lizenzdaten, sichere Kommunikation mit den Client-Anwendungen via HTTPS, Schutz der Server-APIs vor Missbrauch).
    6. Es ist dokumentiert, dass dieser Server eine intern von ACCI gehostete und verwaltete Anwendung sein wird. Die Anforderungen an seine eigene Betriebsumgebung (VM, Ressourcenbedarf) sind initial abgeschätzt.
    7. Das Projektgerüst für den `eaf-license-server` enthält eine grundlegende Struktur für Services, Controller und eine Readme-Datei mit dem Design-Überblick.

**Story 9.5: EAF-Anwendungsunterstützung für Online-Lizenzaktivierung & -Validierung (EAF Application Support for Online License Activation & Validation)**

* **Als** Entwickler einer EAF-basierten Anwendung **möchte ich**, dass das EAF einen Mechanismus bereitstellt, um die Lizenz meiner Anwendung online gegen einen zentralen License Activation Server zu aktivieren und periodisch zu validieren, **damit** Lizenzen dynamisch verwaltet und Missbrauch besser erkannt werden kann.
* **Akzeptanzkriterien (ACs):**
    1. Das Modul `eaf-licensing` stellt eine API/einen Service für eine EAF-basierte Anwendung bereit, um sicher (HTTPS) mit dem Online License Activation Server (aus Story 9.4) zu kommunizieren. Die URL des Servers ist konfigurierbar.
    2. Die Anwendung kann über diesen Service eine Aktivierungsanfrage an den Server senden. Diese Anfrage enthält mindestens den Lizenzschlüssel und eindeutige, aber anonymisierte Hardware-Identifikatoren (basierend auf Story 9.1, ggf. als Hash, um Privatsphäre zu wahren).
    3. Die Anwendung empfängt die Aktivierungsantwort vom Server (z.B. ein Aktivierungs-Token, eine signierte Bestätigung oder einen aktualisierten lokalen Lizenzstatus) und speichert diese sicher lokal (analog zu Story 5.3). Fehler vom Server (z.B. Lizenzschlüssel ungültig, Aktivierungslimit erreicht) werden behandelt und können der Anwendung signalisiert werden.
    4. Die Anwendung kann (konfigurierbar) periodisch den Server kontaktieren, um ihre Lizenz erneut zu validieren (Re-Validierungs-Ping). Das Intervall ist konfigurierbar.
    5. Die Lizenzprüfwerkzeuge des EAF (aus Story 5.4) nutzen primär den Status, der durch die Online-Aktivierung/-Validierung erhalten wurde, wenn diese Methode konfiguriert ist.
    6. Die Kommunikation mit dem Server beinhaltet Retry-Mechanismen für temporäre Netzwerkfehler.
    7. Das Verhalten der EAF-Anwendung für den Fall, dass der Aktivierungsserver temporär nicht erreichbar ist, ist klar definiert und konfigurierbar (z.B. eine Toleranzperiode/"Grace Period" basierend auf der letzten erfolgreichen Validierung, danach ggf. Fallback auf eingeschränkte Funktionalität oder Warnung).
    8. Der Prozess der Online-Aktivierung und -Validierung ist für Entwickler von EAF-Anwendungen dokumentiert, inklusive Fehlerbehandlung und Konfigurationsoptionen.
    9. Testfälle (Integrationstests, die einen Mock-Lizenzserver verwenden) demonstrieren den erfolgreichen Online-Aktivierungs- und Validierungsfluss sowie das Verhalten bei Serverfehlern oder Nichterreichbarkeit.

---

**Epic 10: EAF Observability & Optimierung der Developer Experience (DX) (EAF Observability & Developer Experience (DX) Enhancements)**
*Beschreibung:* Implementiert das Modul `eaf-observability` (Logging, Metriken, Health Checks). Verbessert die Entwicklerdokumentation, stellt umfassende Anwendungsbeispiele bereit (z.B. im `app-example-module`) und entwickelt ggf. erste CLI-Werkzeuge (`eaf-cli`).
*Wert:* Verbessert die Betriebsbereitschaft des EAF und die Akzeptanz und Produktivität der Entwickler, die das EAF nutzen.

**Story 10.1: Standardisiertes Logging Framework für EAF-Anwendungen (Standardized Logging Framework for EAF Applications)**

* **Als** EAF-Entwickler **möchte ich**, dass das Modul `eaf-observability` ein standardisiertes, konfigurierbares Logging-Framework (z.B. SLF4J mit Logback, strukturiertes Logging) bereitstellt, **damit** EAF-basierte Anwendungen über ein konsistentes, effektives und für Monitoring-Systeme optimiertes Application-Logging verfügen.
* **Akzeptanzkriterien (ACs):**
    1. Das Modul `eaf-observability` integriert SLF4J als Logging-Fassade und Logback als Standard-Implementierung. Die Abhängigkeiten werden zentral verwaltet.
    2. Standard-Logback-Konfigurationen (z.B. `logback-spring.xml`) werden bereitgestellt, die strukturiertes Logging im JSON-Format nach `stdout` ermöglichen. Das JSON-Format enthält mindestens Zeitstempel (ISO 8601 mit Millisekunden und Zeitzone), Log-Level, Thread-Name, Logger-Name, Nachricht und ggf. Marker sowie MDC-Parameter (Mapped Diagnostic Context).
    3. Konfigurationsoptionen (z.B. über Spring Properties) ermöglichen es EAF-Anwendungen, Log-Level für verschiedene Logger-Hierarchien zur Laufzeit anzupassen, eigene Appender hinzuzufügen und das Log-Format bei Bedarf zu modifizieren, ohne die Kernfunktionalität des EAF-Loggings zu beeinträchtigen.
    4. Die Weitergabe und Einbindung von Korrelations-IDs (z.B. Trace ID, Span ID aus einem Distributed Tracing System, oder eine pro Request generierte ID) in jede Log-Nachricht über MDC wird unterstützt und ist standardmäßig aktiviert, um die Nachverfolgung von Abläufen zu erleichtern.
    5. Die Dokumentation und Code-Beispiele zeigen detailliert, wie Entwickler von EAF-Anwendungen den Logging-Service nutzen, eigene Logger instanziieren, strukturierte Informationen loggen und MDC für zusätzlichen Kontext verwenden können. Best Practices für performantes Logging werden erläutert.
    6. Die Konfiguration für Log-Rotation und -Archivierung wird nicht vom EAF selbst übernommen, aber die Dokumentation gibt klare Empfehlungen, wie dies in typischen Deployment-Szenarien (z.B. VM mit Log-Management-Agenten) gehandhabt werden kann.
    7. Richtlinien und Beispiele für das Maskieren oder Auslassen sensibler Daten (z.B. Passwörter, personenbezogene Daten) in Log-Nachrichten werden bereitgestellt.

**Story 10.2: Metrik-Sammlung & -Export für EAF-Anwendungen (Metrics Collection & Export for EAF Applications)**

* **Als** EAF-Entwickler **möchte ich**, dass das Modul `eaf-observability` eine Metrik-Bibliothek (z.B. Micrometer) integriert und eine Möglichkeit zum Export von Metriken in einem gängigen Format (z.B. Prometheus) bereitstellt, **damit** die Performance, Ressourcennutzung und der Zustand von EAF-basierten Anwendungen zentral überwacht und analysiert werden können.
* **Akzeptanzkriterien (ACs):**
    1. Das Modul `eaf-observability` integriert Micrometer als Standard-Metrikfassade.
    2. Standardmetriken (System-Metriken) für gängige Aspekte sind standardmäßig aktiviert und konfiguriert (z.B. JVM-Metriken wie Heap/Non-Heap-Nutzung, GC-Aktivität, Thread-Anzahl; CPU-Nutzung; Spring Boot Actuator Metriken wie HTTP-Request-Statistiken, DataSource-Nutzung; Axon-spezifische Metriken falls von Axon bereitgestellt).
    3. Ein HTTP-Endpunkt (z.B. `/actuator/prometheus` via Spring Boot Actuator) wird standardmäßig bereitgestellt, um Metriken im Prometheus-Format für das Scraping durch ein Prometheus-System zu exponieren. Der Endpunkt ist optional durch Sicherheitseinstellungen schützbar.
    4. Entwickler von EAF-Anwendungen können einfach und standardisiert (z.B. durch Annotationen oder programmatische Registrierung bei Micrometer) benutzerdefinierte, anwendungsspezifische Metriken (Counter, Timer, Gauges) definieren und registrieren.
    5. Die Dokumentation und Code-Beispiele für die Nutzung von Metriken, die Konfiguration des Prometheus-Endpunkts und die Erstellung benutzerdefinierter Metriken sind umfassend und verständlich. Empfehlungen für wichtige, von Anwendungen zu exposeende Metriken werden gegeben.
    6. Die Performance-Auswirkungen der Metrik-Sammlung sind minimal und konfigurierbar (z.B. Sampling-Raten für bestimmte Metriken).
    7. Die Möglichkeit, Standard-Metriken bei Bedarf zu deaktivieren, um den Overhead zu reduzieren, ist gegeben.

**Story 10.3: Standardisierte Health-Check-Endpunkte für EAF-Anwendungen (Standardized Health Check Endpoints for EAF Applications)**

* **Als** EAF-Entwickler **möchte ich**, dass das Modul `eaf-observability` standardisierte Health-Check-Endpunkte (z.B. über Spring Boot Actuator) bereitstellt, **damit** der Betriebsstatus und die Funktionsfähigkeit von EAF-basierten Anwendungen einfach von externen Monitoring-Systemen, Load Balancern oder Orchestrierungsplattformen (auch wenn nicht primärer Fokus) überprüft werden kann.
* **Akzeptanzkriterien (ACs):**
    1. Das Modul `eaf-observability` konfiguriert standardmäßig den Spring Boot Actuator Health-Endpunkt (z.B. `/actuator/health`) sowie Liveness- (`/actuator/health/liveness`) und Readiness-Probes (`/actuator/health/readiness`).
    2. Der Health-Endpunkt beinhaltet standardmäßig Prüfungen für den grundlegenden Anwendungsstatus (z.B. Applikationskontext ist `UP`), den Zustand kritischer interner Komponenten (z.B. Datenbankverbindung über den DataSource Health Indicator, Verbindung zum Axon Event Store) und den Festplattenspeicher.
    3. Entwickler von EAF-Anwendungen können einfach und standardisiert eigene, anwendungsspezifische `HealthIndicator`-Komponenten implementieren und registrieren, die den Gesamtstatus des Health-Endpunkts beeinflussen.
    4. Die Konfiguration des Health-Endpunkts (z.B. welche Details angezeigt werden, Caching-Dauer) ist über Anwendungseigenschaften möglich. Der Endpunkt ist optional durch Sicherheitseinstellungen schützbar.
    5. Die Dokumentation und Code-Beispiele erläutern detailliert die Nutzung der Standard-Health-Checks, deren Konfiguration und die Implementierung und Integration benutzerdefinierter Health Indicators.
    6. Das Verhalten bei fehlschlagenden Health Checks (z.B. Rückgabe von HTTP 503 Service Unavailable) ist klar definiert.

**Story 10.4: Umfassendes EAF Entwickler-Dokumentationsportal (Comprehensive EAF Developer Documentation Portal)**

* **Als** Entwickler, der das ACCI EAF nutzt, **möchte ich** ein umfassendes, gut strukturiertes, versioniertes und einfach navigierbares Dokumentationsportal haben, **damit** ich EAF-Konzepte, APIs, Nutzungsmuster, Architekturentscheidungen und Best Practices schnell verstehen und effizient anwenden kann.
* **Akzeptanzkriterien (ACs):**
    1. Ein Dokumentationsportal oder eine Website wird eingerichtet (z.B. unter Verwendung eines Static Site Generators wie MkDocs, Docusaurus, Antora oder eines gut strukturierten Wiki-Systems wie Confluence, falls intern bevorzugt). Die Dokumentation ist versioniert und versionierte Stände sind online zugänglich.
    2. Die Dokumentation ist logisch strukturiert und umfasst mindestens folgende Bereiche:
        * **Einführung:** Vision, Ziele, Architekturüberblick, Kernkonzepte des EAF (DDD, CQRS, ES mit Axon im ACCI Kontext), Technologie-Stack.
        * **Erste Schritte:** Setup der Entwicklungsumgebung, Erstellen eines ersten Projekts/Moduls mit dem EAF, "Hello World"-Beispiel.
        * **Detaillierte Anleitungen (How-To Guides):** Für jedes EAF-Modul (`eaf-core`, `eaf-iam`, `eaf-multitenancy`, `eaf-licensing`, `eaf-i18n`, `eaf-plugin-system`, `eaf-observability`) werden dessen Konfiguration, Nutzung und Erweiterungsmöglichkeiten praxisnah erklärt.
        * **API-Referenzen:** Generierte KDoc/JavaDoc-Dokumentation für alle öffentlichen APIs des EAF, idealerweise direkt im Portal verlinkt oder eingebettet.
        * **Tutorials:** Schritt-für-Schritt-Anleitungen für gängige, komplexere Anwendungsfälle (z.B. Implementierung eines vollständigen CQRS-Flows, Konfiguration eines externen Auth-Providers).
        * **Best Practices & Design Patterns:** Empfehlungen für den Entwurf von Anwendungen auf Basis des EAF.
        * **Troubleshooting & FAQ:** Lösungen für häufige Probleme und Antworten auf oft gestellte Fragen.
        * **Beitrag zur EAF-Entwicklung (Contribution Guide):** Falls interne Entwickler zum EAF beitragen sollen (Coding Standards, PR-Prozess etc.).
    3. Die Dokumentation wird parallel zur EAF-Entwicklung erstellt und aktuell gehalten. Jedes neue Feature oder jede API-Änderung wird dokumentiert, bevor das Feature als "Done" gilt.
    4. Die Dokumentation ist klar und präzise formuliert, verwendet konsistente Terminologie (ein Glossar wird gepflegt) und richtet sich auch an Entwickler, die möglicherweise neu im Kotlin/Java-Ökosystem oder mit den spezifischen Frameworks (Spring Boot, Axon) sind. Code-Beispiele sind korrekt, lauffähig und folgen den EAF-Konventionen.
    5. Das Dokumentationsportal bietet eine gute Suchfunktionalität.
    6. Ein Mechanismus für Entwickler, Feedback zur Dokumentation zu geben oder Verbesserungen vorzuschlagen, ist etabliert.

**Story 10.5: `app-example-module` - Demonstration von EAF Best Practices (`app-example-module` - Demonstrating EAF Best Practices)**

* **Als** Entwickler, der neu im ACCI EAF ist, **möchte ich** ein voll funktionsfähiges, aber überschaubares Beispiel-Anwendungsmodul (`app-example-module`) haben, das mit dem EAF gebaut wurde, **damit** ich Best Practices in Aktion sehen und es als Ausgangspunkt, Lernressource oder Referenz für eigene Entwicklungen verwenden kann.
* **Akzeptanzkriterien (ACs):**
    1. Das Modul `app-example-module` ist als eigenständiges Gradle-Unterprojekt im Monorepo erstellt und aktuell gehalten.
    2. Es demonstriert die Integration und Nutzung der wichtigsten EAF-Kernmodule: `eaf-core` (Implementierung eines CQRS/ES-basierten Domänenmodells), `eaf-iam` (z.B. gesicherte Endpunkte, programmatische Rechteprüfung), `eaf-multitenancy` (alle Operationen erfolgen im Kontext eines Mandanten), `eaf-i18n` (mehrsprachige Benutzeroberflächentexte, falls UI vorhanden), `eaf-observability` (Logging, Metriken, Health Checks).
    3. Das Beispiel beinhaltet ein einfaches, aber nicht-triviales Domänenmodell (z.B. eine kleine "To-Do-Verwaltung" oder "Mini-Bestellsystem") mit mindestens 1-2 Aggregaten, einigen Commands, Events und Queries.
    4. Es zeigt klar, wie Commands, Events, Aggregate, Projektionen (Read Models) und Query Handler unter Verwendung des Axon Frameworks im Kontext des ACCI EAF strukturiert und implementiert werden.
    5. Es hat eine einfache UI (z.B. einige Webseiten mit Thymeleaf/Spring MVC oder eine simple React-Anwendung, die die APIs des Beispielmoduls nutzt) oder zumindest über klar definierte REST-API-Endpunkte, um mit seinen Features zu interagieren und die Funktionsweise zu demonstrieren.
    6. Das Beispielprojekt enthält eigene Unit- und Integrationstests, die zeigen, wie EAF-basierte Anwendungen getestet werden können (insbesondere CQRS/ES-Komponenten).
    7. Der Code des Beispielmoduls ist gut strukturiert, kommentiert und folgt den im EAF etablierten Coding Conventions und Best Practices.
    8. Das Beispielmodul ist ausführlich in der EAF-Entwicklerdokumentation (Story 10.4) beschrieben, inklusive einer Anleitung, wie es ausgeführt und exploriert werden kann.

**Story 10.6: Basis EAF Command-Line Interface (CLI) Werkzeug (`eaf-cli`) (Basic EAF Command-Line Interface (CLI) Tool (`eaf-cli`))**

* **Als** EAF-Entwickler **möchte ich** ein grundlegendes Kommandozeilen-Interface (CLI) Werkzeug vom EAF bereitgestellt bekommen, **damit** ich gängige Entwicklungs-, Diagnose- oder administrative Aufgaben im Zusammenhang mit dem EAF und darauf basierenden Anwendungen effizienter von der Konsole aus durchführen kann.
* **Akzeptanzkriterien (ACs):**
    1. Das Modul `eaf-cli` ist als eigenständiges Gradle-Unterprojekt erstellt und verwendet ein etabliertes CLI-Framework für Java/Kotlin (z.B. PicoCLI, Spring Shell).
    2. Mindestens 1-2 nützliche initiale Befehle für das MVP sind implementiert, z.B.:
        * `eaf-cli project scaffold --name <module-name>`: Generiert eine Grundstruktur für ein neues EAF-Anwendungsmodul basierend auf einem Template.
        * `eaf-cli diagnostic check-env`: Überprüft grundlegende Umgebungsvoraussetzungen für die EAF-Entwicklung (z.B. JDK-Version, Gradle-Verfügbarkeit).
        * `eaf-cli version`: Zeigt die Version des EAF und seiner Kernkomponenten an.
    3. Das CLI-Werkzeug ist als ausführbares JAR oder natives Image (z.B. mit GraalVM, falls im Rahmen von ppc64le für CLI Tools machbar und sinnvoll) paketiert, um eine einfache Verteilung und Nutzung durch Entwickler zu ermöglichen.
    4. Das CLI-Werkzeug ist gut dokumentiert, inklusive Installationsanweisungen und detaillierter Beschreibung aller Befehle und deren Optionen. Jeder Befehl unterstützt einen `--help`-Parameter.
    5. Das CLI gibt klare, informative Erfolgs- und Fehlermeldungen aus. Fehler werden mit entsprechenden Exit-Codes signalisiert.
    6. Das CLI geht sicher mit etwaigen Konfigurationsdateien oder benötigten Credentials um (falls zutreffend für bestimmte Befehle).

**Story 10.7: Implementierung eines dedizierten Audit-Logging-Mechanismus (Implement Dedicated Audit Logging Mechanism)**

* **Als** EAF-Entwickler **möchte ich**, dass das EAF einen dedizierten, persistenten Audit-Logging-Mechanismus bereitstellt, der vom Anwendungs-Logging oder dem Event-Store getrennt ist, **damit** kritische administrative, sicherheitsrelevante und lizenzbezogene Ereignisse sicher, nachvollziehbar und für Compliance- sowie Analysezwecke revisionssicher aufgezeichnet werden können.
* **Akzeptanzkriterien (ACs):**
    1. Eine dedizierte Audit-Logging-Komponente oder ein Service ist innerhalb von `eaf-core` oder `eaf-observability` implementiert. Dieser Service ist von der Standard-Anwendungslogging-Infrastruktur (Story 10.1) getrennt.
    2. Audit-Ereignisse sind als strukturierte Objekte mit einem klar definierten Schema definiert. Mindestattribute für jedes Audit-Event sind: Zeitstempel (mit Millisekunden und Zeitzone), Ereignis-ID (eindeutig), auslösender Akteur (Benutzer-ID, Service-Account-ID, Systemprozess), Quell-IP-Adresse (falls zutreffend), Aktion/Ereignistyp (z.B. `TENANT_CREATED`, `USER_LOGIN_FAILED`, `ROLE_ASSIGNED`, `LICENSE_ACTIVATED`), Zielressource (z.B. Mandanten-ID, Benutzer-ID, Rollenname), Ergebnis der Aktion (`SUCCESS`, `FAILURE`), und ggf. zusätzliche Details (z.B. geänderte Felder bei einem Update, Fehlerursache).
    3. Audit-Logs werden in einem sicheren, persistenten Speicher geschrieben. Für MVP kann dies eine dedizierte PostgreSQL-Tabelle (`audit_log`) sein, die vor unautorisiertem Zugriff und Modifikation geschützt ist. Die Tabelle ist für Abfragen optimiert (z.B. Indizes auf Zeitstempel, Akteur, Aktion, Zielressource). Alternativ oder ergänzend kann die Ausgabe in ein schreibgeschütztes Logfile-Format erfolgen, das an externe SIEM-Systeme weitergeleitet werden kann.
    4. Der Mechanismus zur Erzeugung und Übermittlung von Audit-Ereignissen aus den relevanten EAF-Modulen (z.B. `eaf-iam` für Login-Events, `eaf-multitenancy` für Mandanten-Lifecycle-Events, `eaf-licensing` für Lizenzaktivitäten, Control Plane Backend für administrative Aktionen) an den Audit-Logging-Service ist klar definiert und implementiert (z.B. über einen asynchronen Event-Bus oder direkte Service-Aufrufe).
    5. Klare Richtlinien und eine einfache API werden bereitgestellt, damit auch EAF-basierte Anwendungen eigene, anwendungsspezifische, aber sicherheitsrelevante Ereignisse in dieses zentrale, vom EAF verwaltete Audit-Log schreiben können.
    6. Das Design berücksichtigt Aspekte der Integrität und Nachweisbarkeit des Audit-Logs (z.B. keine Lösch- oder Update-Möglichkeit für bestehende Einträge durch normale Anwendungslogik; ggf. Hash-Verkettung oder digitale Signaturen für Log-Batches als zukünftige Erweiterung).
    7. Der Zugriff auf das Audit-Log (für Lesezwecke durch berechtigte Auditoren oder Sicherheitspersonal) ist über definierte Schnittstellen oder Werkzeuge möglich (z.B. spezifische API-Endpunkte in der Control Plane, direkter DB-Zugriff mit restricted permissions).
    8. Die Konfiguration der Audit-Log-Speicherung (z.B. Datenbankverbindung, Log-Level für Audit-Events) und Aufbewahrungsrichtlinien (Retention Policies) sind dokumentiert (die Implementierung der Retention selbst kann außerhalb des MVP liegen).
    9. Das Audit-Logging darf die Performance der Kernanwendung nicht signifikant beeinträchtigen (z.B. durch asynchrone Verarbeitung, Batching).
