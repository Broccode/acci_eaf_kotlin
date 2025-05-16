# ACCI EAF (Axians Competence Center Infrastructure Enterprise Application Framework) Product Requirements Document (PRD)

## Goal, Objective and Context

* **Ziel des EAF:** Interne Nutzung zur Beschleunigung der Entwicklung und Standardisierung von Enterprise-Softwareprodukten, die externen Kunden bereitgestellt werden. Die Entwicklung mit dem ACCI EAF soll die Ablösung des veralteten, nicht mehr erweiterbaren und performancemäßig limitierten internen "DCA"-Frameworks ermöglichen.
* **Fokus & Wertversprechen:**
  * **Für das ACCI-Entwicklungsteam:** Signifikante Zeit- und Kostenersparnis bei der Entwicklung neuer Produkte und Features, verbesserte Wartbarkeit und Testbarkeit durch moderne Technologien und Architekturen, sowie eine verbesserte Developer Experience. "Befreiung" von den Limitierungen des Altsystems.
  * **Für Endkunden von ACCI-Produkten:** Erheblicher Zugewinn an modernen Features (z.B. Mandantenfähigkeit, erweiterte Sicherheit, flexibles Lizenzmanagement, Internationalisierung), verbesserte Performance und eine modernere User Experience der auf dem EAF basierenden Produkte.
* **Kontext:** Das ACCI EAF wird vom Axians Competence Center Infrastructure Team entwickelt und primär für Softwareprodukte im Enterprise-Segment eingesetzt, die auf IBM Power Architecture (ppc64le) bei Kunden betrieben werden, oft in Umgebungen ohne direkten Internetzugriff.

## [OPTIONAL: For Simplified PM-to-Development Workflow Only] Core Technical Decisions & Application Structure

Dieser Abschnitt dokumentiert die grundlegenden technischen Entscheidungen und die geplante Anwendungsstruktur für das ACCI EAF.

**1. Kern-Technologie-Stack:**

* **Programmiersprache/Plattform:** Kotlin (laufend auf der Java Virtual Machine - JVM)
* **Kern-Framework (Anwendungschicht):** Spring Boot
* **Architektur-Framework (für DDD, CQRS, ES):** Axon Framework
* **Datenbank (primär für Read Models, Zustandsdaten und als Event Store):** PostgreSQL
* **Build-Werkzeug:** Gradle

**2. Repository-Struktur:**

* **Ansatz:** Monorepo
  * Alle Module und die zugehörige Build-Logik des ACCI EAF werden in einem einzigen Git-Repository verwaltet.

**3. Anwendungsstruktur (Module & Verantwortlichkeiten):**

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

## Functional Requirements (MVP)

Die funktionalen Anforderungen für das MVP des ACCI EAF sind in 10 Epics strukturiert. Jedes Epic mit seinen detaillierten User Stories und Akzeptanzkriterien wurde zuvor erarbeitet und genehmigt. (Der vollständige Text der 10 Epics mit allen überarbeiteten User Stories und Akzeptanzkriterien, die Randfälle, Fehlerbehandlung etc. berücksichtigen, wird hier eingefügt oder referenziert. Aus Gründen der Lesbarkeit in dieser Konversation wird hier nur eine Zusammenfassung der Epic-Titel gegeben, der detaillierte Inhalt wurde bereits als "genehmigt" markiert.)

**Epic-Übersicht:**

1. **Epic 1: EAF Grundgerüst & Kerninfrastruktur**
2. **Epic 2: Kernimplementierung Mandantenfähigkeit**
3. **Epic 3: Core Identity & Access Management (IAM) - Lokale Benutzer & RBAC**
4. **Epic 4: Control Plane UI - Phase 1 (Mandanten- & Basis-Benutzerverwaltung)**
5. **Epic 5: Kernmechanismus Lizenzmanagement**
6. **Epic 6: Internationalisierung (i18n) - Kernfunktionalität & Control Plane Integration**
7. **Epic 7: Fundament des Plugin-Systems**
8. **Epic 8: Erweitertes IAM - Externe Authentifizierungs-Provider**
9. **Epic 9: Erweitertes Lizenzmanagement - Hardware-Bindung & Online-Aktivierung**
10. **Epic 10: EAF Observability & Optimierung der Developer Experience (DX)**

*(Anmerkung: Im finalen Dokument würden hier die vollständigen, zuvor detailliert ausgearbeiteten und genehmigten User Stories für jedes Epic stehen.)*

## Non Functional Requirements (MVP)

Die folgenden Nicht-Funktionalen Anforderungen (NFRs) sind für das MVP des ACCI EAF definiert:

**1. Performance & Scalability**

* **NFR 1a (Automatisierte Benchmarks):** Es muss eine automatisierte Benchmark-Suite entwickelt und gewartet werden, um die Performance-Entwicklung kritischer EAF-Operationen zu verfolgen.
* **NFR 1b (Performance auf ppc64le):** Das EAF muss eine akzeptable und konsistente Performance seiner Kernfunktionen auf der ppc64le-Zielplattform demonstrieren.
* **NFR 1c (Performance-Ziele API):** Kritische API-Endpunkte der Control Plane sollen eine durchschnittliche Antwortzeit von **< 500ms** und eine P95-Antwortzeit von **< 1000ms** unter definierter Basislast (z.B. 10 gleichzeitige administrative Benutzer) aufweisen.
* **NFR 1d (Performance-Ziele EAF-Kern):** Kernmechanismen des EAF (z.B. Mandantenkontext-Auflösung, Lizenzvalidierung) sollen einen Overhead von **< 10ms** pro Operation anstreben.
* **NFR 1e (Performance-Ziele CQRS/ES):** Das EAF muss initial **mindestens 50 Commands/Events pro Sekunde** auf der Ziel-ppc64le-Hardware (Baseline TBD) verarbeiten können.
* **NFR 1f (Skalierbarkeitsziele Nutzer/Mandanten):** Architektur ausgelegt für mind. **100 Mandanten** mit je bis zu **1.000 Benutzern**. MVP-Test mit kleinerem Set (z.B. 5 Mandanten, 100 Benutzer je).
* **NFR 1g (Skalierbarkeitsziele Event Store):** Event Store (PostgreSQL) soll initial mind. **1 Million Events pro Monat** für Pilotanwendungen verarbeiten können; Strategien für Archivierung/Snapshotting konzeptionell berücksichtigt.
* **NFR 1h (Lasttestplan):** Initialer Lasttestplan für Control Plane APIs und EAF-Durchsatzmechanismen muss vor erstem Pilotprojekt-Livegang definiert und durchgeführt werden.
* **NFR 1i (Lasttestfokus):** Lasttests identifizieren Engpässe, verifizieren Antwortzeiten unter Last und bestimmen initiale Kapazitätsgrenzen.
* **NFR 1j (Stresstest-Überlegungen):** Grundlegende Stresstests werden durchgeführt (Ziel: graceful degradation).

**2. Security & Compliance**

* **NFR 2a (Compliance-Unterstützung):** EAF muss Anwendungen bei der Einhaltung von GDPR und ISO 27001 unterstützen.
* **NFR 2b (Vorbereitung für Zertifizierungen):** Architektur muss SOC2-Zertifizierung erleichtern.
* **NFR 2c (FIPS-Unterstützung):** EAF muss Verwendung von FIPS 140-2/3 validierten kryptographischen Modulen unterstützen.
* **NFR 2d (OWASP Top 10):** Aktive Adressierung der Risiken A01-A09 der OWASP Top 10.
* **NFR 2e (Bedrohungsanalyse):** Übergeordnete Bedrohungsanalyse (z.B. STRIDE) für Schlüsselkomponenten während Designphase.
* **NFR 2f (Penetrationstests):** Interne Security Reviews; externe Penetrationstests vor breiter Nutzung oder für Major-Releases geplant.

**3. Reliability & Availability (für EAF-eigene Komponenten)**

* **NFR 3a (Unterstützung für HA/DR):** EAF muss Design von HA/DR-fähigen Anwendungen ermöglichen.
* **NFR 3b (Robustheit und Wiederanlaufverhalten):** EAF-Komponenten müssen resilient sein und saubere Neustarts nach Abstürzen ermöglichen.
* **NFR 3c (Verfügbarkeitsziele):** Control Plane UI/API und Online License Activation Server sollen für MVP **99,5% Verfügbarkeit während Geschäftszeiten** anstreben.
* **NFR 3d (MTTR):** Für kritische EAF-Komponenten wird eine MTTR von **< 1 Stunde während der Geschäftszeiten** für das MVP angestrebt. MTBF-Ziele TBD.
* **NFR 3e (Backup & Recovery EAF-Dienste):** Strategien und Anforderungen (RPO/RTO) für Backup und Recovery der EAF-eigenen zustandsbehafteten Komponenten sind **To Be Defined (TBD)**.
* **NFR 3f (Disaster Recovery EAF-Dienste):** Eine spezifische DR-Strategie für die EAF-eigenen gehosteten Komponenten ist **To Be Defined (TBD)**.

**4. Maintainability & Extensibility**

* **NFR 4a (Testabdeckung):** Kernmodule des EAF streben 100% Unit-Testabdeckung für kritische Logik an; hohe Abdeckung (>80%) für neue Geschäftslogik.
* **NFR 4b (Coding Standards):** EAF Codebase muss den zu erstellenden/referenzierenden **"ACCI Kotlin Coding Standards v1.0"** folgen. Einhaltung wird in CI-Pipeline geprüft.
* **NFR 4c (Update-Fähigkeit im Monorepo):** Updates von EAF-Modulen für Anwendungen im selben Monorepo sollen einfach sein.

**5. Usability & Accessibility (for Devs using EAF)**

* **NFR 5a (Umfassende Entwicklerdokumentation):** Siehe Story 10.4.
* **NFR 5b (Inhalt der Dokumentation):** Siehe Story 10.4.
* **NFR 5c (API-Design):** EAF-APIs und Erweiterungspunkte müssen auf Klarheit, einfache Benutzbarkeit und Auffindbarkeit optimiert sein.

**6. Offline Capability**

* **NFR 6a (Kernaspekte):** EAF muss Offline-Lizenzaktivierung/-validierung unterstützen. Deployment von EAF-Anwendungen in Air-Gapped-Umgebungen muss möglich sein.

**7. Auditability / Traceability**

* **NFR 7a (Dediziertes Audit-Log):** Siehe Story 10.7.
* **NFR 7b (Inhalt des Audit-Logs):** Siehe Story 10.7.
* **NFR 7c (Sicherheit und Zugänglichkeit der Audit-Logs):** Siehe Story 10.7.

**8. ppc64le-specific requirements**

* **NFR 8a (Optimierung für ppc64le):** EAF muss für zuverlässige und effiziente Ausführung auf ppc64le VMs optimiert sein.

**9. Operational Requirements (Ergänzend)**

* **NFR (Konfigurationsmanagement):** Konfigurationen über externalisierte Dateien (z.B. `application.yml`) mit Spring Profilen. Sensible Daten über Umgebungsvariablen oder sicher gemountete Dateien injiziert (nicht in Git).
* **NFR (SBOM Generierung):** Für jedes offizielle EAF-Release und darauf basierende Produkte muss eine SBOM (z.B. CycloneDX, SPDX) generiert werden. CI/CD-Pipeline beinhaltet automatisierten Schritt dafür.
* **NFR (SBOM Review):** Ein Prozess zur kontinuierlichen Überprüfung der SBOMs auf Lizenzen und bekannte Schwachstellen von Drittkomponenten (z.B. mit OWASP Dependency Track) ist etabliert.

## User Interaction and Design Goals

Dieser Abschnitt beschreibt die übergeordneten Ziele für die Benutzerinteraktion und das Design der Control Plane UI, die für die Verwaltung von Mandanten, Lizenzen und mandantenspezifischen i18n-Texten des ACCI EAF vorgesehen ist.

* **Gesamteindruck und Nutzererlebnis (Overall Vision & Experience):**
  * Die Benutzeroberfläche soll einen **professionellen und funktionalen** Eindruck vermitteln.
  * Als Referenz und Inspiration für den Stil und die Funktionalität dient **React-Admin**. Dies impliziert eine datenorientierte, übersichtliche und effiziente Bedienung.
  * Die Technologiepräferenz für das Frontend dieser Control Plane UI ist damit React.
* **Wichtige Interaktionsmuster (Key Interaction Paradigms):**
  * Tabellarische Ansichten (Data Grids) für Listen mit integrierten Such-, Filter- und Sortierfunktionen.
  * Formularbasierte Eingabemasken für das Erstellen und Bearbeiten von Einträgen.
  * Detailansichten zur Anzeige aller relevanten Informationen.
* **Kernansichten / -masken (Core Screens/Views - konzeptionell für MVP):**
  * Login-Maske für Administratoren der Control Plane.
  * Mandanten-Verwaltungsansicht (CRUD).
  * Lizenz-Verwaltungsansicht (CRUD – primär für ACCI-Team).
  * i18n-Text-Verwaltungsansicht für Mandanten.
  * Benutzer-Verwaltungsansicht (lokale Benutzer) innerhalb eines Mandanten (CRUD, Status, Passwort-Reset).
  * RBAC-Zuweisungsansicht (Rollen Benutzern zuweisen) innerhalb eines Mandanten.
  * Service-Account-Verwaltungsansicht innerhalb eines Mandanten (CRUD, Credentials, Expiration).
  * Authentifizierungs-Provider-Konfigurationsansicht für Mandanten (LDAP, OIDC, SAML).
* **Barrierefreiheitsziele (Accessibility Aspirations):**
  * Für das MVP sind keine spezifischen, über die Standardfunktionalität des gewählten UI-Frameworks hinausgehenden Barrierefreiheitsanforderungen definiert (Nutzerwunsch: "ist nicht notwendig").
* **Zielgeräte / -plattformen (Target Devices/Platforms):**
  * Primärer Fokus: **Desktop-Webbrowser**.
  * Grundlegende responsive Darstellung für Tablets (Anzeige von Kerninformationen) ist wünschenswert.
* **Branding / Style Guide-Vorgaben:**
  * Die Control Plane UI soll **neutral** gestaltet sein.
* **User Feedback & Iteration:**
  * **Initiales MVP:** Internes "Dogfooding" & direktes Feedback vom ACCI-Team.
  * **Vor-Pilot-/Pilotphase:** Usability-Tests mit "Proxy-Usern" (internes Personal in Kunden-Admin-Rollen) oder frühen Pilotkunden.
  * **Iterationsprozess:** Gesammeltes Feedback wird vom PM konsolidiert, priorisiert und mit dem Entwicklungsteam in nachfolgenden Zyklen umgesetzt.

## Out of Scope / Future Considerations

* **Out of Scope für MVP (explizit durch Nutzerentscheidung oder als TBD markiert):**
  * Detaillierte Ausarbeitung einer vollständigen "Out of Scope"-Liste (Nutzerwunsch: "lässt sich zum jetzigen Zeitpunkt nicht sagen").
  * Unterstützung für Right-to-Left (RTL) Sprachen (Nutzerwunsch: "Nicht geplant").
  * Spezifische Schulungsbedarfe und formale Trainingspläne für Entwickler oder Support-Teams (Nutzerwunsch: "Klären wir nach dem MVP").
  * Detaillierter Support- und Wartungsprozess für das EAF (Nutzerwunsch: "Klären wir nach dem MVP").
  * Feste Release-Frequenz für das EAF (Nutzerwunsch: "Klären wir nach dem MVP").
  * Detaillierte VM-Spezifikationen für EAF-Komponenten (Nutzerwunsch: "Klären wir nach dem MVP").
  * Backup/Recovery & DR-Strategien für EAF-eigene Dienste (Nutzerwunsch: "als TBD markieren").
  * Datenmigration aus dem Altsystem "DCA" (Nutzerwunsch: "nicht für den MVP").
  * Umfassende UI für Rollen- und Permission-Definition im RBAC-Modul der Control Plane (MVP fokussiert auf Zuweisung bestehender Rollen).
  * "Passwort vergessen"-Flow für die Control Plane UI (MVP fokussiert auf manuellen Admin-Prozess).
* **Future Considerations (Potenzielle zukünftige Erweiterungen):**
  * Vollumfängliche ABAC-Implementierung.
  * Erweiterte Features des Online License Activation Servers.
  * Dynamisches Laden/Entladen von Plugins zur Laufzeit (über ServiceLoader hinausgehend, z.B. mit OSGi oder ähnlichen Technologien).
  * Unterstützung weiterer Datenbanken für den EAF-Betrieb.
  * Integrierte Feedback-Mechanismen und UI-Analytics für die Control Plane.
  * Automatisierte Self-Service-Funktionen für Mandanten-Admins in der Control Plane, die über das MVP hinausgehen.

## Technical Assumptions

1. **Zielplattform:** Das ACCI EAF und alle darauf basierenden Anwendungen müssen für die **IBM Power Architecture (ppc64le)** entwickelt und optimiert werden.
2. **Betriebsumgebung (MVP-Fokus):**
    * Das **primäre Ziel-Deployment** für das ACCI EAF und darauf basierende Anwendungen im Rahmen des MVP erfolgt auf **Virtuellen Maschinen (VMs)**.
    * Das EAF und die darauf basierenden Anwendungen **müssen für den Betrieb in Umgebungen ohne direkten Internetzugriff (air-gapped environments)** konzipiert und lauffähig sein.
    * Die Nutzung von **Public-Cloud-Plattformen oder Kubernetes-basierten Umgebungen ist für das MVP und die initiale Kernarchitektur des EAF nicht im Fokus**. Das EAF soll so gestaltet werden, dass es ohne Abhängigkeiten zu spezifischen Cloud-Diensten oder Kubernetes als Laufzeitumgebung auskommt.
    * Obwohl nicht der primäre Fokus, sollte das Design des EAF, wo immer praktikabel und ohne Kompromisse an die primären Ziele, keine unnötigen Hürden für eine *eventuelle spätere* Anpassung oder den Betrieb von *einzelnen, darauf basierenden Produkten* in Cloud- oder Kubernetes-Umgebungen aufbauen.
3. **Kern-Technologie-Stack:** Kotlin (JVM), Spring Boot, Axon Framework, PostgreSQL, Gradle.
4. **Architekturstil:** Modularer Monolith.
5. **Zentrale Architekturmuster:** Hexagonale Architektur, DDD, CQRS, Event Sourcing.
6. **Repository-Struktur:** Monorepo.
7. **Frontend-Technologie (für Control Plane UI):** React (orientiert am Stil von React-Admin).
8. **Integrationspunkte (MVP):** Externe Auth-Provider (LDAP, OIDC, SAML), Online License Activation Server, SMTP-Server für E-Mail-Versand.

## Success Metrics & KPIs (MVP)

(Ziele innerhalb der ersten 6 Monate nach MVP-Bereitstellung)

**1. EAF-Adoption und -Nutzung:**

* **KPI 1a:** Mindestens **1-2 signifikante interne Pilotprojekte/-module** nutzen den ACCI EAF aktiv.
* **KPI 1b:** Pilotprojekte nutzen mindestens **60-70%** der für sie relevanten, verfügbaren EAF-Kernmodule.
**2. Entwicklerproduktivität und -zufriedenheit:**
* **KPI 2a:** Onboarding-Zeit für neue Entwickler (Kotlin/Java-Kenntnisse) für eine Standardaufgabe mit EAF: **< 2-3 Arbeitstage**.
* **KPI 2b:** Qualitative Umfrage bei Kern-EAF-Nutzern ergibt >75% Zustimmung zur Verbesserung gegenüber DCA und Produktivitätssteigerung.
* **KPI 2c:** Entwickler berichten über signifikante (>40-50% geschätzt) Reduktion von Boilerplate-Code.
**3. Produktqualität & Modernisierung:**
* **KPI 3a:** Pilotprojekte implementieren erfolgreich mindestens ein modernes EAF-Kernfeature, das mit DCA schwer/unmöglich war.
* **KPI 3b:** Keine kritischen Fehler in genutzten EAF-Kernmodulen in den ersten 3 Monaten der Pilotnutzung.
**Zeitrahmen für die Zielerreichung (dieser initialen KPIs):** 6 Monate nach MVP-Bereitstellung.

## Risks and Mitigation (Product Risks)

*(Dieser Abschnitt ist im Template vorgesehen, wurde aber in unserer Checkliste nicht explizit unter "Product Risks" behandelt. Die Risikobewertung in der Tiefenrecherche bezog sich auf Technologiestacks. Wir könnten hier Produktrisiken ergänzen, z.B. Adoptionsrisiko, Komplexität des EAF, Abhängigkeit von Schlüsselpersonen.)*

* **Risiko 1: Geringe Adoption des EAF durch interne Produktteams.**
  * *Mitigation:* Enge Zusammenarbeit mit Pilotprojekten, exzellente Dokumentation und DX (Epic 10), frühzeitiges Aufzeigen von Vorteilen.
* **Risiko 2: Hohe Komplexität des EAF überfordert Entwickler.**
  * *Mitigation:* Starke Modularisierung, klare APIs, umfassende Beispiele und Tutorials (Epic 10), schrittweise Einführung von Konzepten.
* **Risiko 3: Abhängigkeit von wenigen Schlüsselpersonen mit tiefem EAF/Axon-Wissen.**
  * *Mitigation:* Wissensverteilung im Team, Paarprogrammierung, umfassende Dokumentation, Schulungsinitiativen (Post-MVP).
* **Risiko 4: Performance auf ppc64le entspricht nicht den Erwartungen.**
  * *Mitigation:* Frühe Benchmarks (NFR 1a), kontinuierliches Performance-Monitoring, Fokus auf ppc64le-Optimierungen im gewählten Stack.
* **Risiko 5: Unterschätzung des Aufwands für die Entwicklung und Wartung des EAF selbst.**
  * *Mitigation:* Realistische Planung, Priorisierung des MVP-Scopes, iteratives Vorgehen, dediziertes EAF-Team.

## Dependencies

* Verfügbarkeit eines SMTP-Servers für E-Mail-Versand.
* Verfügbarkeit von Testsystemen für externe Authentifizierungs-Provider (LDAP, OIDC, SAML) während der Entwicklung von Epic 8.
* Interne Ressourcen (Entwicklerteam Michael, Majlinda, Lirika; QA/Doku Anita; PM Christian) für Entwicklung und Pilotierung.
* Bereitstellung und Wartung der ppc64le VM-Infrastruktur für Entwicklung, Test und Betrieb der EAF-Komponenten.

## Stakeholders

* **Christian:** Produktmanager (Primärer Ansprechpartner)
* **Michael:** Fullstack-Entwickler, Staff Engineer (Kern-Entwicklerteam EAF)
* **Majlinda:** Fullstack-Entwicklerin, Senior Engineer (Kern-Entwicklerteam EAF)
* **Lirika:** Frontend-Entwicklerin, Junior Engineer (Fokus Control Plane UI)
* **Sebastian:** Backend-Entwickler, Principal Engineer (Nutzer/Kunde des EAF für eines der ersten Produkte)
* **Anita:** QA und Dokumentation (Sicherstellung Qualität und Doku des EAF)
* **ACCI Management/Leitung:** Sponsoren, strategische Entscheidungsträger
* **Administratoren von Kundenunternehmen:** Zukünftige Nutzer der Control Plane für Mandanten-spezifische Administration
* **ACCI Lizenzmanager:** Interne Nutzer der Control Plane zur Generierung und Verwaltung von Lizenzen

## Glossary

* **ACCI EAF:** (Axians Competence Center Infrastructure Enterprise Application Framework) Das hier definierte Software-Framework, entwickelt vom Axians Competence Center Infrastructure Team.
* **API:** (Application Programming Interface) Programmierschnittstelle.
* **Axon Framework:** Ein Java-Framework zur Implementierung von CQRS, Event Sourcing und DDD.
* **Build-Logic (Gradle):** Ein spezielles Modul in Gradle-Projekten zur Zentralisierung von Build-Skript-Logik und Konventionen.
* **CI/CD:** (Continuous Integration / Continuous Deployment oder Delivery) Methoden zur Automatisierung von Software-Build-, Test- und Auslieferungsprozessen.
* **Control Plane:** Eine zentrale Verwaltungs- und Steuerungsebene (API und UI) für das ACCI EAF, z.B. für Mandanten- und Lizenzmanagement.
* **CQRS:** (Command Query Responsibility Segregation) Ein Architekturmuster, das Lese- und Schreiboperationen voneinander trennt.
* **DCA:** (Das Alte [Framework]) Das existierende, abzulösende interne Framework bei ACCI.
* **DDD:** (Domain-Driven Design) Ein Ansatz zur Softwareentwicklung, der sich auf das Geschäftsfeld (Domäne) konzentriert.
* **Definition of Done (DoD):** Eine Reihe von Kriterien, die erfüllt sein müssen, damit eine User Story als abgeschlossen gilt. (Die detaillierte DoD wurde zuvor definiert).
* **DSGVO:** (Datenschutz-Grundverordnung) Europäische Verordnung zum Datenschutz (Englisch: GDPR).
* **EAF:** (Enterprise Application Framework) Ein Rahmenwerk zur standardisierten Entwicklung von Unternehmensanwendungen.
* **ES:** (Event Sourcing) Ein Architekturmuster, bei dem alle Zustandsänderungen als eine Sequenz von Ereignissen gespeichert werden.
* **FIPS:** (Federal Information Processing Standards) US-amerikanische Standards für Informationsverarbeitung in Computersystemen.
* **Gradle:** Ein Build-Automatisierungswerkzeug, das primär im Java- und Kotlin-Ökosystem verwendet wird.
* **IAM:** (Identity & Access Management) Prozesse und Technologien zur Verwaltung digitaler Identitäten und ihrer Zugriffsrechte.
* **i18n:** (Internationalization) Der Prozess, eine Software so zu gestalten, dass sie verschiedene Sprachen und regionale Besonderheiten ohne Code-Änderungen unterstützen kann.
* **ISO 27001:** Ein internationaler Standard für Informationssicherheits-Managementsysteme.
* **JWT:** (JSON Web Token) Ein offener Standard (RFC 7519) zur sicheren Übertragung von Informationen als JSON-Objekt zwischen Parteien.
* **KPI:** (Key Performance Indicator) Schlüsselkennzahl zur Messung des Erfolgs oder Fortschritts.
* **Kotlin:** Eine moderne, statisch typisierte Programmiersprache, die auf der JVM läuft.
* **l10n:** (Localization) Der Prozess der Anpassung einer internationalisierten Software an eine spezifische Region oder Sprache.
* **LDAP:** (Lightweight Directory Access Protocol) Ein Protokoll für den Zugriff auf Verzeichnisdienste.
* **Logback:** Ein Logging-Framework für Java-Anwendungen, Nachfolger von Log4j.
* **Micrometer:** Eine herstellerneutrale Fassade für Anwendungsmetriken im JVM-Ökosystem.
* **Monorepo:** Eine Strategie in der Softwareentwicklung, bei der der Quellcode für viele verschiedene Projekte in einem einzigen Repository gespeichert wird.
* **MTTR:** (Mean Time To Recovery) Mittlere Wiederherstellungszeit nach einem Ausfall.
* **MVP:** (Minimum Viable Product) Eine erste, minimal funktionsfähige Version eines Produkts.
* **OAuth 2.0:** (Open Authorization) Ein offenes Protokoll für die Autorisierung, oft für delegierten Zugriff genutzt.
* **OIDC:** (OpenID Connect) Eine einfache Identitätsschicht auf OAuth 2.0, die es Clients ermöglicht, die Identität eines Endbenutzers basierend auf der von einem Autorisierungsserver durchgeführten Authentifizierung zu überprüfen.
* **OWASP:** (Open Web Application Security Project) Eine gemeinnützige Stiftung, die sich für die Verbesserung der Softwaresicherheit einsetzt.
* **PicoCLI:** Ein Java-Framework zur Erstellung von Kommandozeilenanwendungen.
* **Plugin-System:** Eine Architektur, die es erlaubt, die Funktionalität einer Software durch Module (Plugins) zu erweitern.
* **PostgreSQL:** Ein objektrelationales Datenbankmanagementsystem.
* **ppc64le:** (PowerPC 64-bit Little Endian) Eine spezifische Architektur von IBM Power-Prozessoren.
* **PRD:** (Product Requirements Document) Ein Dokument, das die Anforderungen an ein Produkt oder System beschreibt.
* **Prometheus:** Ein Open-Source-System zur Überwachung und Alarmierung.
* **RBAC:** (Role-Based Access Control) Ein Zugriffskontrollmodell, bei dem Berechtigungen Rollen zugewiesen werden.
* **React-Admin:** Ein Frontend-Framework zum Erstellen von Admin-Anwendungen über REST/GraphQL-APIs, basierend auf React.
* **RLS:** (Row-Level Security) Ein Datenbank-Sicherheitsmerkmal, das den Zugriff auf Datenzeilen basierend auf dem Kontext des ausführenden Benutzers einschränkt.
* **SAML:** (Security Assertion Markup Language) Ein XML-basierter offener Standard für den Austausch von Authentifizierungs- und Autorisierungsinformationen.
* **SBOM:** (Software Bill of Materials) Eine formale, maschinenlesbare Liste der Komponenten, Bibliotheken und Module, die in einer Software enthalten sind.
* **ServiceLoader (Java):** Ein Mechanismus in Java zur dynamischen Erkennung und zum Laden von Dienstanbieter-Implementierungen.
* **SLF4J:** (Simple Logging Facade for Java) Eine Abstraktionsschicht für verschiedene Logging-Frameworks.
* **SOC2:** (System and Organization Controls 2) Ein Berichtsstandard zur Dokumentation der internen Kontrollen von Dienstleistungsunternehmen hinsichtlich Sicherheit, Verfügbarkeit, Verarbeitungsintegrität, Vertraulichkeit und Datenschutz.
* **SPI:** (Service Provider Interface) Eine API, die von einem Drittanbieter implementiert oder erweitert werden soll.
* **Spring Boot:** Ein Framework zur Vereinfachung der Entwicklung von Spring-basierten Java-Anwendungen.
* **STRIDE:** Ein Modell zur Bedrohungsanalyse (Spoofing, Tampering, Repudiation, Information Disclosure, Denial of Service, Elevation of Privilege).
* **UI:** (User Interface) Benutzeroberfläche.
* **UX:** (User Experience) Nutzererlebnis.
* **VM:** (Virtual Machine) Virtuelle Maschine.

---
**(END PM Tasks START Initial Architect Prompt)**

## Initial Architect Prompt

**To:** Solution Architect
**From:** Christian, Product Manager
**Date:** 16. Mai 2025
**Subject:** Architekturdesign für ACCI Enterprise Application Framework (ACCI EAF) - MVP

Sehr geehrte/r Lösungsarchitekt/in,

dieses Dokument dient als Grundlage für das Architekturdesign des Minimum Viable Product (MVP) für das neue **ACCI Enterprise Application Framework (ACCI EAF)**. Das Ziel dieses Frameworks ist es, die Entwicklung und Wartung unserer Enterprise-Softwareprodukte für externe Kunden signifikant zu beschleunigen, zu standardisieren und qualitativ zu verbessern, insbesondere als Ablösung für unser veraltetes "DCA"-Framework.

### Projektziele (Kurzfassung)

* Beschleunigung der Entwicklung und Standardisierung von Enterprise-Softwareprodukten.
* Reduktion von Zeit und Kosten bei der Produktentwicklung.
* Verbesserung der Wartbarkeit, Sicherheit und Performance der Endprodukte.
* Bereitstellung moderner Features wie Mandantenfähigkeit, flexibles IAM und Lizenzmanagement.

### Kernanforderungen (Zusammenfassung)

Das ACCI EAF soll als modularer Monolith entwickelt werden und folgende Kernfunktionalitäten (MVP) über dedizierte Module bereitstellen:

* Umfassende Mandantenfähigkeit (RLS, Kontextmanagement, Admin-API).
* Identitäts- & Zugriffsmanagement (IAM) (lokale Benutzer, Service-Accounts, RBAC, Konfiguration externer Provider wie LDAP/AD, OIDC, SAML2).
* Lizenzmanagement (zeitbasiert, hardwaregebunden für ppc64le CPU-Kerne, Offline-/Online-Aktivierung).
* Internationalisierung (i18n) (Laden von Übersetzungen, Formatierung, Sprachumschaltung, mandantenspezifische Anpassungen).
* Ein Plugin-System (basierend auf Java ServiceLoader).
* Observability (strukturiertes Logging, Metriken via Micrometer/Prometheus, Health Checks).
* Eine webbasierte Control Plane (Admin-UI, React-basiert) für Mandanten-, Benutzer-, Lizenz- und i18n-Verwaltung.

Detaillierte funktionale Anforderungen sind in Form von 10 Epics mit User Stories und Akzeptanzkriterien in diesem PRD dokumentiert.

Wichtige Nicht-funktionale Anforderungen umfassen:

* **Zielplattform & Betrieb:** Ausschließlich ppc64le VMs, keine Cloud/Kubernetes für MVP, Offline-/Air-Gapped-Fähigkeit.
* **Sicherheit:** Hohe Sicherheit (OWASP Top 10 A01-A09, Threat Modeling), Unterstützung für GDPR, ISO27001, SOC2, FIPS. SBOM-Generierung und -Review.
* **Performance:** Definierte Antwortzeit- und Durchsatzziele für Kernfunktionen und APIs auf ppc64le. Last- und Stresstests sind vorgesehen.
* **Zuverlässigkeit:** MTTR < 1 Stunde für kritische EAF-Komponenten (Control Plane, Lizenzserver).
* **Wartbarkeit & DX:** Hohe Codequalität (gemäß "ACCI Kotlin Coding Standards v1.0"), umfassende Dokumentation, Testabdeckung (100% für Kernlogik).
* **Auditierbarkeit:** Dediziertes Audit-Log für kritische Operationen.

### Technische Annahmen und Vorgaben

* **Programmiersprache/Plattform:** Kotlin (auf der JVM)
* **Kern-Framework:** Spring Boot
* **Architektur-Framework (DDD/CQRS/ES):** Axon Framework
* **Datenbank:** PostgreSQL (für relationale Daten und als Event Store)
* **Build-Werkzeug:** Gradle
* **Repository-Struktur:** Monorepo
* **Architekturstil:** Modularer Monolith
* **Zentrale Architekturmuster:** Hexagonale Architektur, Domain-Driven Design (DDD), Command Query Responsibility Segregation (CQRS), Event Sourcing (ES).
* **Frontend (Control Plane):** React (orientiert an React-Admin)

### Hauptaufgaben für den Architekten

1. Entwurf einer robusten, wartbaren und erweiterbaren Softwarearchitektur für das ACCI EAF und seine Kernmodule (siehe Modulstruktur im Abschnitt "Core Technical Decisions & Application Structure"), die alle funktionalen und nicht-funktionalen Anforderungen des MVP erfüllt.
2. Definition klarer Schnittstellen zwischen den EAF-Modulen und zur Control Plane API.
3. Detaillierung des Datenbankdesigns für PostgreSQL, inklusive Schemata für relationale Daten (Mandanten, Benutzer, Lizenzen etc.) und Konfiguration des Event Stores für Axon.
4. Sicherstellung, dass die Architektur die Implementierung von RLS für Mandantentrennung optimal unterstützt.
5. Konzeption der Sicherheitsarchitektur (Authentifizierung, Autorisierung, Schutz von APIs und Daten).
6. Planung der Integrationspunkte (externe Auth-Provider, SMTP, Online License Activation Server).
7. Erstellung notwendiger Architekturdiagramme (Komponenten, Sequenzen, Deployment etc.).
8. Technische Unterstützung des Entwicklungsteams während der Implementierung.

Bitte verwenden Sie dieses PRD als primäre Quelle für Ihre Architekturentscheidungen. Für Rückfragen stehe ich (Christian, PM) gerne zur Verfügung.

Mit freundlichen Grüßen,
Christian (Produktmanager)
