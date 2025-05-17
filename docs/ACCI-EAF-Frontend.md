# ACCI EAF Control Plane UI Frontend Architecture Document

> **Translation in progress — remaining German passages are marked with `<!-- TODO: translate -->`. Please convert them to English incrementally.**

## Table of Contents

- [Introduction](https://www.google.com/search?q=%23introduction)
- [Overall Frontend Philosophy & Patterns](https://www.google.com/search?q=%23overall-frontend-philosophy--patterns)
- [Detailed Frontend Directory Structure](https://www.google.com/search?q=%23detailed-frontend-directory-structure)
- [Component Breakdown & Implementation Details](https://www.google.com/search?q=%23component-breakdown--implementation-details)
  - [Component Naming & Organization](https://www.google.com/search?q=%23component-naming--organization)
  - [Template for Component Specification](https://www.google.com/search?q=%23template-for-component-specification)
- [State Management In-Depth](https://www.google.com/search?q=%23state-management-in-depth)
  - [Store Structure / Slices (for Context & Potential Zustand)](https://www.google.com/search?q=%23store-structure--slices-for-context--potential-zustand)
  - [Key Selectors (for Context & Potential Zustand)](https://www.google.com/search?q=%23key-selectors-for-context--potential-zustand)
  - [Key Actions / Reducers / Thunks (for Context & Potential Zustand)](https://www.google.com/search?q=%23key-actions--reducers--thunks-for-context--potential-zustand)
- [API Interaction Layer](https://www.google.com/search?q=%23api-interaction-layer)
  - [Client/Service Structure](https://www.google.com/search?q=%23clientservice-structure)
  - [Error Handling & Retries (Frontend)](https://www.google.com/search?q=%23error-handling--retries-frontend)
- [Routing Strategy](https://www.google.com/search?q=%23routing-strategy)
  - [Route Definitions](https://www.google.com/search?q=%23route-definitions)
  - [Route Guards / Protection](https://www.google.com/search?q=%23route-guards--protection)
- [Build, Bundling, and Deployment](https://www.google.com/search?q=%23build-bundling-and-deployment)
  - [Build Process & Scripts](https://www.google.com/search?q=%23build-process--scripts)
  - [Key Bundling Optimizations](https://www.google.com/search?q=%23key-bundling-optimizations)
  - [Deployment to CDN/Hosting](https://www.google.com/search?q=%23deployment-to-cdnhosting)
- [Frontend Testing Strategy](https://www.google.com/search?q=%23frontend-testing-strategy)
  - [Component Testing](https://www.google.com/search?q=%23component-testing)
  - [Feature/Flow Testing (UI Integration)](https://www.google.com/search?q=%23featureflow-testing-ui-integration)
  - [End-to-End UI Testing Tools & Scope](https://www.google.com/search?q=%23end-to-end-ui-testing-tools--scope)
- [Accessibility (AX) Implementation Details](https://www.google.com/search?q=%23accessibility-ax-implementation-details)
- [Performance Considerations](https://www.google.com/search?q=%23performance-considerations)
- [Internationalization (i18n) and Localization (l10n) Strategy](https://www.google.com/search?q=%23internationalization-i18n-and-localization-l10n-strategy)
- [Feature Flag Management](https://www.google.com/search?q=%23feature-flag-management)
- [Frontend Security Considerations](https://www.google.com/search?q=%23frontend-security-considerations)
- [Browser Support and Progressive Enhancement](https://www.google.com/search?q=%23browser-support-and-progressive-enhancement)
- [Change Log](https://www.google.com/search?q=%23change-log)

## Introduction

This document details the technical architecture specifically for the frontend of the ACCI EAF Control Plane UI. It complements the main ACCI EAF Architecture Document and the UI/UX guidelines derived from the ACCI EAF Product Requirements Document (PRD). This document details the frontend architecture and **builds upon the foundational decisions** (e.g., overall tech stack, CI/CD, primary testing tools) defined in the main ACCI EAF Architecture Document (`docs/ACCI-EAF-Architecture.md`). **Frontend-specific elaborations or deviations from general patterns must be explicitly noted here.** The goal is to provide a clear blueprint for frontend development, ensuring consistency, maintainability, and alignment with the overall system design and user experience goals.

- **Link to Main Architecture Document (REQUIRED):** `docs/ACCI-EAF-Architecture.md`
- **Link to UI/UX Specification (REQUIRED if exists):** `docs/ACCI-EAF-PRD.md` (insbesondere der Abschnitt "User Interaction and Design Goals")
- **Link to Primary Design Files (Figma, Sketch, etc.) (REQUIRED if exists):** Noch nicht spezifiziert - React-Admin dient als funktionale und stilistische Inspiration. Falls spezifische Design-Dateien existieren oder erstellt werden, hier verlinken.
- **Link to Deployed Storybook / Component Showcase (if applicable):** Noch nicht zutreffend

## Overall Frontend Philosophy & Patterns

This section describes the core architectural decisions and patterns chosen for the ACCI EAF Control Plane UI frontend. This aligns with the "Definitive Tech Stack Selections" in the main architecture document and considers implications from the overall system architecture.

- **Framework & Core Libraries:**
  - **React:** v19.1
  - **React-Admin:** v5.8.1
        These are derived from the 'Definitive Tech Stack Selections' in the main Architecture Document. This section elaborates on *how* these choices are applied specifically to the frontend.
- **Component Architecture:**
  - Primary reliance on **React-Admin's component suite** (e.g., `<List>`, `<Datagrid>`, `<Edit>`, `<SimpleForm>`, `<TextInput>`) for standard CRUD operations and UI structures.
  - Custom components will be developed for functionalities or UI elements not adequately covered by React-Admin.
  - A clear distinction will be made between **Presentational (Dumb) Components** (primarily concerned with UI) and **Container (Smart) Components** (managing logic, state, and data fetching, often leveraging React-Admin hooks or custom hooks).
  - Reusable UI elements (e.g., custom buttons, specialized input fields beyond React-Admin's scope) will be developed and potentially managed in a local component library or Storybook.
- **State Management Strategy:**
  - Primarily leverage **React-Admin's built-in state management capabilities (Ra-Store)**, which internally uses React Context and Redux principles for resource management, UI state, and optimistic updates.
  - For global state not directly tied to React-Admin resources (e.g., global UI settings, notifications not handled by React-Admin, complex cross-feature state), **React Context API** will be the first choice.
  - If more complex global state scenarios arise that are not well-suited for React Context, **Zustand** might be considered as a lightweight alternative to a full Redux setup, due to its simplicity and hook-based API. This will be evaluated if specific needs arise.
        Referenced from main Architecture Document and detailed further in "State Management In-Depth" section.
- **Data Flow:**
  - Unidirectional data flow as facilitated by React and React-Admin.
  - React-Admin's **Data Provider** will be the primary interface for data fetching, caching, and mutations with the `eaf-controlplane-api`.
  - UI components will receive data via props (passed down from React-Admin resource controllers or custom container components) or access it via React-Admin's hooks (e.g., `useListController`, `useEditController`) or custom hooks consuming Context or other state management solutions.
- **Styling Approach:** **React-Admin Theming & Customization (likely Material UI based) + CSS Modules**.
  - Configuration File(s): Theming object for React-Admin, potentially `*.module.css` files.
  - Key conventions:
    - Leverage React-Admin's theming capabilities to achieve the "neutral" branding requirement from the PRD. This involves customizing the default theme (often based on Material UI).
    - For custom components or significant overrides not manageable via the theme, **CSS Modules** will be used. CSS Module files (e.g., `MyComponent.module.css`) will be co-located with their respective components to ensure local scope and avoid style conflicts.
    - Global styles, if absolutely necessary, will be minimal and managed in a central file imported at the application root.
- **Key Design Patterns Used:**
  - **React Hooks:** Extensively used for state, side effects, and custom logic encapsulation.
  - **React-Admin specific patterns:** Data Providers, Resource Controllers, Custom Routes, Authentication Providers.
  - **Higher-Order Components (HOCs):** As used by React-Admin or for custom cross-cutting concerns if necessary, though hooks are generally preferred.
  - **Service Pattern for API calls:** While React-Admin's Data Provider abstracts most direct API calls, any direct or complex interactions outside its scope would be encapsulated in service modules.
  - **Provider Pattern:** For React Context usage.

## Detailed Frontend Directory Structure

<!-- TODO: translate -->
Die Frontend-Anwendung für das ACCI EAF Control Plane UI befindet sich im Verzeichnis `controlplane-ui/` des Monorepos. Als Build-Tool wird Vite verwendet. Die folgende ASCII-Diagramm repräsentiert die empfohlene Ordnerstruktur innerhalb von `controlplane-ui/`:

```plaintext
controlplane-ui/
├── public/                     # Static assets served directly by the web server (z.B. favicons, manifest.json).
│   └── locales/                # Localization files for i18n when loaded statically.
│       ├── en.json
│       └── de.json
├── src/                        # Main application source code.
│   ├── App.tsx                 # Root component of the application; setup of React-Admin, Router, ThemeProvider, etc.
│   ├── main.tsx                # Application entry point; renders the App component.
│   ├── vite-env.d.ts           # Typdefinitionen für Vite-Umgebungsvariablen.
│   │
│   ├── assets/                 # Statische Assets, die in Komponenten importiert werden (Bilder, Schriftarten etc.).
│   │   └── logo.svg
│   │
│   ├── components/             # Global wiederverwendbare UI-Komponenten, die nicht spezifisch für eine Ressource sind.
│   │   ├── common/             # Allgemeine, atomare UI-Elemente (z.B. benutzerdefinierter Button, spezielle Badges).
│   │   │   └── BrandedHeader.tsx
│   │   └── layout/             # Komponenten für das Seitenlayout (z.B. benutzerdefinierte Menüs, erweiterte AppBar-Funktionen).
│   │       └── CustomMenu.tsx
│   │
│   ├── features/               # Module für jede Haupt-React-Admin-Ressource oder benutzerdefinierte Features (z.B. Mandanten, Benutzer).
│   │   ├── tenants/            # Beispiel: Feature-Modul für "Mandanten".
│   │   │   ├── TenantList.tsx  # React-Admin List Komponente für Mandanten.
│   │   │   ├── TenantEdit.tsx  # React-Admin Edit Komponente für Mandanten.
│   │   │   ├── TenantCreate.tsx# React-Admin Create Komponente für Mandanten.
│   │   │   ├── TenantShow.tsx  # React-Admin Show Komponente (optional).
│   │   │   └── components/     # Komponenten, die spezifisch für das Mandanten-Feature sind.
│   │   │       └── TenantStatusChip.tsx
│   │   ├── users/              # Feature-Modul für "Benutzer" (ähnliche Struktur wie tenants).
│   │   ├── licenses/           # Feature-Modul für "Lizenzen" (ähnliche Struktur).
│   │   └── i18nAdmin/          # Feature-Modul für die i18n-Verwaltung im UI (falls zutreffend).
│   │       ├── LanguageList.tsx
│   │       └── TranslationEditPage.tsx
│   │
│   ├── pages/                  # Benutzerdefinierte Seiten/Ansichten, die nicht direkt an React-Admin-Ressourcen gebunden sind.
│   │   ├── DashboardPage.tsx
│   │   └── GlobalSettingsPage.tsx
│   │
│   ├── hooks/                  # Global wiederverwendbare benutzerdefinierte React Hooks.
│   │   └── useAppConfiguration.ts
│   │
│   ├── providers/              # React-Admin Provider Konfigurationen.
│   │   ├── dataProvider.ts     # Konfigurierter React-Admin Data Provider für die eaf-controlplane-api.
│   │   ├── authProvider.ts     # Konfigurierter React-Admin Auth Provider.
│   │   └── i18nProvider.ts     # Konfigurierter React-Admin i18n Provider (kann auch Sprachdateien importieren).
│   │
│   ├── router/                 # Routing-Konfiguration.
│   │   └── customRoutes.tsx    # Definitionen für benutzerdefinierte Routen außerhalb von React-Admin Ressourcen.
│   │
│   ├── store/                  # Zustandmanagement für nicht direkt React-Admin bezogene globale Zustände (z.B. mit React Context oder Zustand).
│   │   ├── themeContext.tsx
│   │   └── notificationStore.ts # Beispiel für Zustand mit Zustand
│   │
│   ├── styles/                 # Styling-bezogene Dateien.
│   │   ├── theme.ts            # React-Admin Theme-Konfigurationsobjekt (Anpassung Material UI).
│   │   └── global.css          # Minimale globale Styles (CSS Resets, Basisschriftarten, falls nicht vom Theme abgedeckt).
│   │
│   ├── types/                  # Globale TypeScript-Typdefinitionen und Interfaces.
│   │   ├── index.d.ts          # Aggregiert Typen oder globale Erweiterungen.
│   │   └── react-admin.d.ts    # Erweiterungen für React-Admin Typen falls nötig.
│   │
│   └── utils/                  # Globale Hilfsfunktionen und Konstanten.
│       ├── helpers.ts
│       └── constants.ts
│
├── tests/                      # Test-spezifische Dateien und Konfigurationen.
│   ├── setupTests.ts           # Setup-Datei für Tests (z.B. Jest/Vitest Konfiguration, globale Mocks).
│   └── e2e/                    # End-to-End Tests mit Playwright.
│       └── tenants.spec.ts
│
├── index.html                  # Haupt-HTML-Datei (Vite-Konvention).
│   ├── package.json                # Projekt-Abhängigkeiten und Skripte.
│   ├── vite.config.ts              # Vite Build-Tool Konfigurationsdatei.
│   ├── tsconfig.json               # TypeScript Compiler-Konfiguration.
│   │
│   ├── postcss.config.js           # PostCSS Konfiguration (für Autoprefixer, etc.).
│   │
│   └── README.md                   # Readme für das Frontend-Projekt.
```

### Notes on Frontend Structure

- **Einfluss von Vite:** Die Verwendung von Vite als Build-Tool beeinflusst diese Struktur wie folgt:
  - `index.html` befindet sich im Root-Verzeichnis des `controlplane-ui` Projekts.
  - Das `public/` Verzeichnis dient für statische Assets, die direkt und unverändert ausgeliefert werden.
  - `vite.config.ts` enthält die Build-, Entwicklungs-Server- und Optimierungskonfigurationen.
  - Umgebungsvariablen, die im Client-Code zugänglich sein sollen, müssen das Präfix `VITE_` tragen (z.B. `VITE_API_BASE_URL`).
  - **Modularität durch Features:** Die Strukturierung nach `features/` (oder alternativ `resources/`) fördert die Modularität und erleichtert die Verwaltung von Code, der zu spezifischen React-Admin Ressourcen oder Anwendungsbereichen gehört. Jedes Feature-Verzeichnis ist weitgehend eigenständig.
  - **Komponenten-Co-Location:** Komponenten-Tests (`*.test.tsx` oder `*.spec.tsx` für Unit- und Integrationstests mit Jest/Vitest und React Testing Library) werden direkt neben den zu testenden Dateien oder in einem `__tests__` Unterverzeichnis innerhalb des `src/` Baumes abgelegt. E2E-Tests (`tests/e2e/`) sind separat.
  - **CSS-Module:** CSS-Modul-Dateien (`*.module.css`) werden direkt neben den Komponenten abgelegt, die sie stylen, um lokale Gültigkeit und Kapselung der Styles zu gewährleisten.
  - **Provider-Kapselung:** React-Admin spezifische Provider (`dataProvider`, `authProvider`, `i18nProvider`) sind in einem eigenen `providers/` Verzeichnis gekapselt, um ihre Konfiguration zentral und übersichtlich zu halten.
  - **Strikte Einhaltung:** KI-Agenten und Entwickler MÜSSEN diese definierte Struktur strikt einhalten. Neue Dateien MÜSSEN basierend auf diesen Beschreibungen im entsprechenden Verzeichnis platziert werden.

## Component Breakdown & Implementation Details

<!-- TODO: translate -->
Dieser Abschnitt beschreibt die Konventionen und Vorlagen für die Definition von UI-Komponenten. Die detaillierte Spezifikation für die meisten Feature-spezifischen Komponenten wird im Laufe der Implementierung der User Stories entstehen. KI-Agenten und Entwickler MÜSSEN die untenstehende "Template for Component Specification" verwenden, wann immer eine neue, signifikante benutzerdefinierte Komponente identifiziert wird.

### Component Naming & Organization

<!-- TODO: translate -->
- **Component Naming Convention:** **PascalCase für Dateinamen und Komponentennamen (z.B., `TenantForm.tsx`, `UserProfileCard.tsx`)**. Alle Komponentendateien MÜSSEN dieser Konvention folgen.
- **Organization:**
  - **React-Admin Komponenten:** Der primäre Ansatz ist die Nutzung und Konfiguration der umfangreichen Komponentenbibliothek von React-Admin (z.B. `<List>`, `<Datagrid>`, `<Edit>`, `<SimpleForm>`, `<TextInput>`, `<ReferenceInput>`, etc.) für Standard-CRUD-Ansichten und -Operationen. Diese Komponenten werden direkt innerhalb der Feature-Module (z.B. `src/features/tenants/TenantList.tsx`) verwendet und über ihre Props konfiguriert.
  - **Globally Reusable Custom Components:** Eigene Komponenten, die anwendungsweit wiederverwendbar sind und nicht spezifisch für ein einzelnes Feature oder eine einzelne Ressource sind, werden in `src/components/common/` (für atomare UI-Elemente) oder `src/components/layout/` (für strukturelle Layout-Komponenten) abgelegt.
  - **Feature-Specific Custom Components:** Eigene Komponenten, die ausschließlich innerhalb eines bestimmten Features oder einer Ressource verwendet werden, werden im Unterverzeichnis `components/` des jeweiligen Feature-Moduls co-lokalisiert (z.B. `src/features/tenants/components/TenantStatusChip.tsx`).
- **Presentational vs. Container Components:** Diese Unterscheidung wird wie folgt gehandhabt:
  - React-Admin Resource-Komponenten (z.B. `<List>`, `<Edit>`) agieren oft als Container-Komponenten, die Daten-Fetching und Business-Logik über Hooks (`useListController`, `useEditController`) verwalten.
  - Benutzerdefinierte Komponenten sollten, wo sinnvoll, als Presentational Components gestaltet werden, die Daten und Callbacks über Props erhalten. Komplexere Logik oder Zustandsverwaltung kann in benutzerdefinierten Hooks gekapselt werden, die dann von "Smart" Wrapper-Komponenten oder direkt von Feature-Komponenten genutzt werden.

- **Guidelines for Creating Custom Components:**
  - Benutzerdefinierte Komponenten werden erstellt, wenn:
    - React-Admin keine passende Komponente für die benötigte UI/UX bereitstellt.
    - Eine spezifische, stark gebrandete oder interaktive UI-Anforderung nicht durch Konfiguration der Standard-React-Admin-Komponenten erreicht werden kann.
    - Komplexe, wiederverwendbare UI-Logik gekapselt werden soll, die über mehrere Teile eines Features oder der Anwendung hinweg benötigt wird.
- Diese benutzerdefinierten Komponenten MÜSSEN der "Template for Component Specification" folgen.

### Template for Component Specification

<!-- TODO: translate -->
Für jede signifikante *benutzerdefinierte* UI-Komponente, die aus der UI/UX-Spezifikation und den Design-Dateien (oder der funktionalen Notwendigkeit) identifiziert wird, MÜSSEN die folgenden Details bereitgestellt werden. Diese Vorlage ist nicht für die Standardkonfiguration von React-Admin-Komponenten gedacht, es sei denn, es wird ein komplexer Wrapper mit eigener Logik und Props erstellt. Wiederholen Sie diesen Unterabschnitt für jede solche Komponente. Der Detaillierungsgrad MUSS für einen KI-Agenten oder Entwickler ausreichend sein, um sie mit minimaler Mehrdeutigkeit zu implementieren.

#### Component: `{ComponentName}` (z.B., `AuditLogEntry`, `ComplexFilterPanel`)

<!-- TODO: translate -->
- **Purpose:** {Beschreiben Sie kurz, was diese Komponente tut und welche Rolle sie in der Benutzeroberfläche spielt. MUSS klar und prägnant sein.}
- **Source File(s):** {z.B., `src/features/auditing/components/AuditLogEntry.tsx`. MUSS der exakte Pfad sein.}
- **Visual Reference:** {Link zu einem spezifischen Figma-Frame/einer Komponente, einer Storybook-Seite oder einer detaillierten Beschreibung/Skizze, falls kein formales Design existiert. ERFORDERLICH.}
- **Props (Properties):**
    {Listen Sie jede Prop auf, die die Komponente akzeptiert. Für jede Prop MÜSSEN alle Spalten in der Tabelle ausgefüllt werden.}

    | Prop Name     | Type                                                                 | Required? | Default Value | Description                                                                                                                               |
    | :------------ | :------------------------------------------------------------------- | :-------- | :------------ | :---------------------------------------------------------------------------------------------------------------------------------------- |
    | `exampleProp` | `string`                                                             | Yes       | N/A           | Die ID der anzuzeigenden Entität. MUSS eine gültige UUID sein.                                                                             |
    | `variant`     | `'compact' \| 'full'`                                                | No        | `'full'`      | Steuert den Anzeigemodus der Komponente.                                                                                                  |
    | `{anotherProp}` | `{Spezifischer Primitivtyp, importierter Typ oder Inline-Interface/Typdefinition}` | {Yes/No}  | {Falls vorhanden} | {MUSS den Zweck der Prop und alle Einschränkungen klar angeben, z.B. 'Muss eine positive Ganzzahl sein.'}                               |

- **Internal State (if any):**
    {Beschreiben Sie jeden signifikanten internen Zustand, den die Komponente verwaltet. Listen Sie nur Zustände auf, die *nicht* von Props oder globalem Zustand abgeleitet sind. Wenn der Zustand komplex ist, überlegen Sie, ob er stattdessen von einem benutzerdefinierten Hook oder einer globalen Zustandsverwaltungslösung verwaltet werden sollte.}

    | State Variable  | Type      | Initial Value | Description                                                                |
    | :-------------- | :-------- | :------------ | :------------------------------------------------------------------------- |
    | `isLoading`     | `boolean` | `false`       | Verfolgt, ob Daten für die Komponente geladen werden.                      |
    | `{anotherState}`| `{type}`  | `{value}`     | {Beschreibung der Zustandsvariable und ihres Zwecks.}                      |

- **Key UI Elements / Structure:**
    {Stellen Sie eine Pseudo-HTML- oder JSX-ähnliche Struktur bereit, die den DOM der Komponente repräsentiert. Fügen Sie gegebenenfalls wichtige bedingte Rendering-Logik ein. **Diese Struktur diktiert die primäre Ausgabe für den KI-Agenten.**}

    ```html
    <div class="audit-log-entry {variant === 'compact' ? 'compact-styles' : 'full-styles'}">
      <span class="timestamp">{formattedTimestamp(entry.timestamp)}</span>
      <span class="user">{entry.user.name}</span>
      {variant === 'full' && <p class="details">{entry.details}</p>}
    </div>
    ```

- **Events Handled / Emitted:**
- **Handles:** {z.B., `onClick` auf einem Detail-Button (löst `onViewDetails` Prop aus).}
- **Emits:** {Wenn die Komponente benutzerdefinierte Ereignisse/Callbacks auslöst, die nicht durch Props abgedeckt sind, beschreiben Sie diese mit ihrer exakten Signatur. z.B., `onExpand: (payload: { entryId: string; isExpanded: boolean }) => void`}
- **Actions Triggered (Side Effects):**
- **State Management:** {z.B., "Dispatched `uiSlice.actions.showNotification({ message: 'Aktion ausgeführt' })` aus `src/store/notificationStore.ts`. Action Payload MUSS mit dem definierten Action Creator übereinstimmen."}
- **API Calls:** {Spezifizieren Sie, welcher Service/welche Funktion aus der "API Interaction Layer" aufgerufen wird (normalerweise nicht direkt aus reinen Presentational Components). z.B., "Ruft `auditService.fetchDetails(entryId)` aus `src/features/auditing/services/auditService.ts` auf."}
- **Styling Notes:**
    {MUSS sich auf spezifische Design-System-Komponentennamen beziehen (z.B. "Verwendet `<Button variant='primary'>` von Material UI via React-Admin Theme") ODER CSS-Modul-Klassennamen angeben, die angewendet werden sollen (z.B. "Container verwendet `styles.auditEntryContainer`. Titel verwendet `styles.entryTitle` aus `AuditLogEntry.module.css`."). Jede dynamische Styling-Logik basierend auf Props oder Zustand MUSS beschrieben werden. KI-Agent sollte die Verwendung von CSS-Modulen für benutzerdefinierte Komponenten priorisieren.}
- **Accessibility Notes:**
    {MUSS spezifische ARIA-Attribute und deren Werte auflisten (z.B., `aria-label="Audit-Log Eintrag für Aktion X"`), erforderliches Tastaturnavigationsverhalten (z.B., "Gesamte Komponente ist via Tab erreichbar und Details können per Enter/Space ein-/ausgeklappt werden, falls interaktiv."), und alle Anforderungen an das Fokusmanagement (z.B., "Wenn diese Komponente ein Modal öffnet, MUSS der Fokus darin gefangen sein. Beim Schließen des Modals kehrt der Fokus zum auslösenden Element zurück.").}

-----

*Wiederholen Sie die obige Vorlage für jede signifikante benutzerdefinierte Komponente.*

-----

## State Management In-Depth

<!-- TODO: translate -->
Dieser Abschnitt erweitert die im Abschnitt "Overall Frontend Philosophy & Patterns" dargelegte Strategie für das State Management. Die Hauptlast der Zustandsverwaltung für CRUD-Operationen und Ressourcen wird von React-Admin intern gehandhabt. Diese Sektion fokussiert sich auf die übergreifende Strategie und die Verwaltung von Zuständen, die darüber hinausgehen.

- **Chosen Solution:**

    1. **React-Admin Internal State (Ra-Store):** Die primäre Lösung für alle Zustände, die direkt mit den von React-Admin verwalteten Ressourcen (z.B. Mandanten, Benutzer, Lizenzen) zusammenhängen. Dies umfasst das Abrufen von Daten, Caching, Optimistic Updates, Listenfilter/Sortierung, Auswahlzustände und den Zustand von Bearbeitungs-/Erstellungsformularen. Entwickler interagieren hiermit hauptsächlich über React-Admin Hooks (z.B. `useListController`, `useEditController`, `useCreateController`, `useDataProvider`) und die Konfiguration der Ressourcen.
    2. **React Context API:** Für globalen Zustand, der nicht direkt an React-Admin Ressourcen gebunden ist und eine mittlere Komplexität aufweist. Anwendungsfälle sind z.B. globale UI-Einstellungen (Theme-Präferenzen, falls dynamisch und nicht über React-Admin Theme lösbar), anwendungsweite Benachrichtigungen, die nicht durch das Standard-Benachrichtigungssystem von React-Admin abgedeckt werden, oder geteilter Zustand innerhalb eines spezifischen, komplexen benutzerdefinierten Features oder einer Seite, die nicht dem React-Admin Ressourcenmodell folgt.
    3. **Zustand (Potenziell):** Falls komplexere globale Zustandsszenarien auftreten, die nicht ressourcenspezifisch sind und mit React Context schwer zu verwalten wären (z.B. sehr komplexe Benutzer-Session-Details jenseits der Basisauthentifizierung, Zustandsverwaltung für einen mehrstufigen Wizard in einem benutzerdefinierten Nicht-React-Admin-Prozess), könnte Zustand als leichtgewichtige, Hook-basierte Alternative zu einer vollständigen Redux-Installation in Betracht gezogen werden. Die Notwendigkeit hierfür wird bei Bedarf evaluiert.
<!-- TODO: translate -->
    4. **Local Component State (`useState`, `useReducer`):** Standardmäßig für UI-spezifischen, ephemeren Zustand innerhalb einzelner Komponenten (z.B. Zustand von Formulareingaben vor der Übergabe an React-Admin, Zustand von Dropdown-Menüs, UI-Schalter, die nur eine Komponente betreffen).

- **Decision Guide for State Location:**

<!-- TODO: translate -->
- **Ra-Store (React-Admin internal):** **MUSS** verwendet werden für alle Daten und UI-Zustände, die direkt mit den über `<Resource>` definierten Entitäten zusammenhängen. Dies ist der Standard und wird von React-Admin weitgehend automatisch verwaltet.
<!-- TODO: translate -->
- **React Context API (`src/store/contexts/`):** **MUSS** verwendet werden für:
  - Theming-Variablen, die nicht Teil des statischen React-Admin Themes sind.
<!-- TODO: translate -->
    - Globale anwendungsweite Konfigurationen oder Zustände, die von vielen Komponenten gelesen, aber selten geändert werden.
<!-- TODO: translate -->
    - Zustand, der primär innerhalb eines bestimmten Komponentenbaums nach unten weitergegeben wird (z.B. innerhalb eines komplexen, benutzerdefinierten Layouts oder einer mehrstufigen Formularseite, die keinem React-Admin `Resource` entspricht).
<!-- TODO: translate -->
- **Zustand (falls benötigt, in `src/store/slices/`):** Kann in Betracht gezogen werden für:
<!-- TODO: translate -->
    - Komplexe, nicht-ressourcenbezogene globale Zustände, die häufige Updates erfordern oder von vielen, nicht direkt hierarchisch verbundenen Komponenten modifiziert werden.
<!-- TODO: translate -->
    - Verwaltung von Zuständen, deren Logik von reinen UI-Komponenten entkoppelt werden soll (z.B. anwendungsweite Benachrichtigungs-Queues mit erweiterter Logik).
<!-- TODO: translate -->
- **Local Component State (`useState`, `useReducer`):** **MUSS** die Standardwahl für allen anderen UI-bezogenen Zustand sein, der nicht die Kriterien für React Context, Zustand oder Ra-Store erfüllt.

### Store Structure / Slices (for Context & Potential Zustand)

<!-- TODO: translate -->
Da Ra-Store seine Zustandsverwaltung intern kapselt, bezieht sich dieser Abschnitt primär auf die Struktur für React Context und eine potenzielle Nutzung von Zustand.

- **React Context API Structure:**

  - Kontexte werden im Verzeichnis `src/store/contexts/` definiert.
  - Jeder Kontext besteht typischerweise aus:
    - Einer Datei, die den Kontext selbst erstellt (`React.createContext()`).
    - Einem Provider-Komponenten, der den Zustand verwaltet (oft mit `useState` oder `useReducer`) und den Kontextwert bereitstellt.
    - Einem benutzerdefinierten Hook (z.B. `useThemeSettings`), um den Kontextwert einfach zu konsumieren.
<!-- TODO: translate -->
- **Beispiel: `src/store/contexts/ThemeContext.tsx`** (für eine einfache Umschaltung Hell/Dunkel-Modus zusätzlich zum RA Theme)

        ```typescript
        import React, { createContext, useContext, useState, useMemo } from 'react';

        type ThemeMode = 'light' | 'dark';
        interface ThemeContextType {
          mode: ThemeMode;
          toggleMode: () => void;
        }

        const ThemeContext = createContext<ThemeContextType | undefined>(undefined);

        export const ThemeProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
          const [mode, setMode] = useState<ThemeMode>('light'); // Default mode

          const toggleMode = () => {
            setMode((prevMode) => (prevMode === 'light' ? 'dark' : 'light'));
          };

          const value = useMemo(() => ({ mode, toggleMode }), [mode]);

          return <ThemeContext.Provider value={value}>{children}</ThemeContext.Provider>;
        };

        export const useTheme = (): ThemeContextType => {
          const context = useContext(ThemeContext);
          if (context === undefined) {
            throw new Error('useTheme must be used within a ThemeProvider');
          }
          return context;
        };
        ```

<!-- TODO: translate -->
        Der `ThemeProvider` würde dann die `App`-Komponente oder Teile davon umschließen.

<!-- TODO: translate -->
- **Zustand Store Structure (Beispielhaft, falls benötigt):**

<!-- TODO: translate -->
- Zustand-Stores würden in `src/store/slices/` abgelegt (z.B. `src/store/slices/notificationStore.ts`).
<!-- TODO: translate -->
- **Beispiel: `notificationStore.ts`** (für ein erweitertes, anwendungsweites Benachrichtigungssystem)

        ```typescript
        import { create } from 'zustand';

        interface Notification {
          id: string;
          message: string;
          type: 'info' | 'success' | 'warning' | 'error';
          autoHideDuration?: number;
        }

        interface NotificationState {
          notifications: Notification[];
          addNotification: (notification: Omit<Notification, 'id'>) => void;
          removeNotification: (id: string) => void;
          clearAllNotifications: () => void;
        }

        export const useNotificationStore = create<NotificationState>((set) => ({
          notifications: [],
          addNotification: (notification) =>
            set((state) => ({
              notifications: [...state.notifications, { ...notification, id: new Date().toISOString() }],
            })),
          removeNotification: (id) =>
            set((state) => ({
              notifications: state.notifications.filter((n) => n.id !== id),
            })),
          clearAllNotifications: () => set({ notifications: [] }),
        }));
        ```

<!-- TODO: translate -->
        Dieser Store könnte dann in UI-Komponenten über den Hook `useNotificationStore` verwendet werden.

### Key Selectors (for Context & Potential Zustand)

<!-- TODO: translate -->
- **React-Admin (Ra-Store):** Selektoren sind implizit Teil der von React-Admin bereitgestellten Hooks (z.B. `useListContext().data`, `useEditContext().record`). Es gibt keine expliziten, benutzerdefinierten Selektoren im Redux-Stil für den Zugriff auf den Ra-Store.
<!-- TODO: translate -->
- **React Context:** Der "Selektor" ist der direkte Zugriff auf die Werte, die vom Kontext-Provider bereitgestellt und über den benutzerdefinierten Hook (z.B. `const { mode } = useTheme();`) konsumiert werden.
- **Zustand:**
<!-- TODO: translate -->
- Einfache Selektoren sind Funktionen, die dem Hook übergeben werden:

        ```typescript
        const notifications = useNotificationStore((state) => state.notifications);
        const notificationCount = useNotificationStore((state) => state.notifications.length);
        ```

<!-- TODO: translate -->
- Für komplexere oder memoisierte Selektoren (um unnötige Re-Renders zu vermeiden, wenn sich nur irrelevante Teile des Stores ändern), kann `zustand/middleware` mit `subscribeWithSelector` oder eine manuelle Memoization mit `useMemo` in der Komponente in Betracht gezogen werden.

### Key Actions / Reducers / Thunks (for Context & Potential Zustand)

<!-- TODO: translate -->
- **React-Admin (Ra-Store):** Aktionen werden typischerweise durch Interaktion mit den von React-Admin bereitgestellten UI-Komponenten (z.B. Speichern eines Formulars) oder durch Aufruf von Funktionen des `dataProvider` (z.B. `dataProvider.update()`, `dataProvider.create()`) ausgelöst. React-Admin verwaltet die internen "Reducer" und "Thunks" (Sagas für Optimistic Rendering).
- **React Context:**
  - Wenn der Provider `useState` verwendet, sind "Aktionen" die `setState`-Funktionen, die vom Provider exportiert werden (z.B. `toggleMode` im `ThemeContext`-Beispiel).
<!-- TODO: translate -->
- Wenn der Provider `useReducer` verwendet, werden Aktionen an die `dispatch`-Funktion übergeben und von der Reducer-Funktion verarbeitet.
- **Zustand:**
<!-- TODO: translate -->
- "Aktionen" sind Funktionen, die innerhalb des `create` Aufrufs definiert werden und den Zustand über die `set` Funktion modifizieren (z.B. `addNotification`, `removeNotification` im `notificationStore`-Beispiel).
<!-- TODO: translate -->
- Asynchrone Aktionen können direkt innerhalb dieser Funktionen implementiert werden:

        ```typescript
<!-- TODO: translate -->
        // Beispiel für eine asynchrone Aktion in einem Zustand-Store
        // interface AppConfigState {
        //   config: AppConfig | null;
        //   isLoading: boolean;
        //   fetchAppConfig: () => Promise<void>;
        // }
        // fetchAppConfig: async () => {
        //   set({ isLoading: true });
        //   try {
<!-- TODO: translate -->
        //     const response = await apiClient.get('/app-config'); // Annahme: apiClient ist verfügbar
        //     set({ config: response.data, isLoading: false });
        //   } catch (error) {
        //     console.error("Failed to fetch app config", error);
        //     set({ isLoading: false }); // Fehlerbehandlung hier oder im UI
        //   }
        // },
        ```

## API Interaction Layer

<!-- TODO: translate -->
Dieser Abschnitt beschreibt, wie das Frontend mit der im Hauptarchitekturdokument definierten `eaf-controlplane-api` kommuniziert. Die primäre Schnittstelle hierfür ist der React-Admin `DataProvider`.

### Client/Service Structure

<!-- TODO: translate -->
- **HTTP Client Setup (für DataProvider):**

<!-- TODO: translate -->
- Der React-Admin `DataProvider` wird einen zentral konfigurierten HTTP-Client verwenden, um Anfragen an das Backend zu senden. Wir werden hierfür `Workspace` mit einer benutzerdefinierten Wrapper-Funktion oder eine leichtgewichtige Bibliothek wie `ky` in Betracht ziehen, um Interceptors und Konfigurationen zu handhaben. Falls komplexere Szenarien (wie automatische Retries, die unten diskutiert werden) breiter benötigt werden, könnte auch Axios in Erwägung gezogen werden. Die Konfiguration erfolgt in `src/providers/httpClient.ts` (oder einem ähnlichen Utility-Modul).
- **Base URL:** Wird aus einer Umgebungsvariable `VITE_API_BASE_URL` bezogen (z.B. `VITE_API_BASE_URL=/controlplane/api/v1`). Dies ist im Einklang mit der Vite-Konvention.
- **Default Headers:**
  - `Content-Type: 'application/json'`
  - `Accept: 'application/json'`
<!-- TODO: translate -->
- **Interceptors / Wrapper Logic (für den HTTP-Client des DataProviders):**
<!-- TODO: translate -->
    - **Auth Token Injection:** Der HTTP-Client wird so konfiguriert, dass er automatisch den Authentifizierungstoken (JWT) aus dem `authProvider` (z.B. via `authProvider.getIdentity()` oder einer ähnlichen Methode, die den Token sicher bereitstellt) abruft und in den `Authorization` Header jeder Anfrage einfügt (z.B. `Authorization: Bearer <token>`).
<!-- TODO: translate -->
    - **Error Normalization:** Bevor Fehler an den React-Admin `DataProvider` zurückgegeben werden, kann der HTTP-Wrapper Fehlerobjekte normalisieren, um eine konsistente Fehlerbehandlung zu gewährleisten (z.B. Extrahieren von Fehlermeldungen aus der API-Antwortstruktur).
<!-- TODO: translate -->
    - **Request/Response Logging (Entwicklung):** In Entwicklungsumgebungen können Interceptors für das Logging von Anfragen und Antworten nützlich sein.

- **React-Admin Data Provider (`src/providers/dataProvider.ts`):**

<!-- TODO: translate -->
- Dies ist die zentrale Schnittstelle für alle CRUD-Operationen und Datenabfragen für die von React-Admin verwalteten Ressourcen (Mandanten, Benutzer, Lizenzen etc.).
<!-- TODO: translate -->
- Er implementiert das `DataProvider` Interface von React-Admin, einschließlich Methoden wie `getList`, `getOne`, `getMany`, `getManyReference`, `create`, `update`, `updateMany`, `delete`, `deleteMany`.
<!-- TODO: translate -->
- **API Mapping:** Der `dataProvider` bildet die React-Admin Methodenaufrufe auf die spezifischen Endpunkte und das REST-Dialekt der `eaf-controlplane-api` ab (gemäß der OpenAPI-Spezifikation `docs/api/controlplane-v1.yml`). Dies beinhaltet:
  - Anpassung von Paginierungs-, Sortierungs- und Filterparametern an das vom Backend erwartete Format.
<!-- TODO: translate -->
    - Korrekte URL-Generierung für Ressourcen und Unterressourcen.
<!-- TODO: translate -->
- **Authentication Integration:** Der `dataProvider` arbeitet eng mit dem `authProvider` zusammen. Bei API-Fehlern, die auf Authentifizierungsprobleme hinweisen (z.B. 401 Unauthorized), wird der `authProvider` entsprechende Aktionen auslösen (z.B. Logout, Redirect zur Login-Seite).
<!-- TODO: translate -->
- **Optimistic Updates:** React-Admin unterstützt Optimistic Updates für `create`, `update` und `delete` Operationen. Der `dataProvider` wird so implementiert, dass er diese Funktion nutzt, indem er die erwartete (lokal modifizierte) Ressource direkt nach einer erfolgreichen Anfrage zurückgibt, bevor die Daten vom Server erneut abgerufen werden. Dies verbessert die wahrgenommene Performance.
- **Data Transformation:** Notwendige Transformationen zwischen dem Datenformat der API und dem vom Frontend (insbesondere von React-Admin Komponenten) erwarteten Format erfolgen innerhalb der `dataProvider`-Methoden (z.B. Umbenennung von Feldern, Anpassung von Datentypen, Aufbereitung von Referenzen).

<!-- TODO: translate -->
- **Service Definitions (für Nicht-React-Admin API-Aufrufe):**

<!-- TODO: translate -->
- Für API-Aufrufe, die nicht dem CRUD-Modell von React-Admin folgen oder spezielle Operationen darstellen (z.B. Auslösen eines Batch-Jobs, Abrufen spezifischer Dashboard-Daten, die keine "Ressource" sind), können separate Service-Module in `src/services/` erstellt werden (z.B. `src/services/dashboardService.ts`).
<!-- TODO: translate -->
- Diese Services würden dieselbe konfigurierte `httpClient`-Instanz (oder Wrapper-Funktion) verwenden, um Konsistenz bei Authentifizierung und Fehlerbehandlung zu gewährleisten.
- **Beispiel (`src/services/tenantActionsService.ts`):**

        ```typescript
        import { httpClient } from '../providers/httpClient'; // Annahme: httpClient ist exportiert

        export const tenantActionsService = {
          activateTenant: async (tenantId: string): Promise<void> => {
            await httpClient.post(`/tenants/${tenantId}/activate`);
          },
          deactivateTenant: async (tenantId: string): Promise<void> => {
            await httpClient.post(`/tenants/${tenantId}/deactivate`);
          }
        };
        ```

### Error Handling & Retries (Frontend)

- **Global Error Handling (im Kontext von React-Admin):**

<!-- TODO: translate -->
- **DataProvider-Fehler:** Der `dataProvider` MUSS Fehler (abgelehnte Promises) so zurückgeben, dass React-Admin sie verarbeiten kann. Dies bedeutet in der Regel, ein Objekt mit einem `status` Code und einem `message` (oder einem `body` mit Details) zurückzugeben. React-Admin zeigt diese Fehler standardmäßig als Benachrichtigungen an. Die Fehlermeldungen aus der API (gemäß API-Spezifikation) werden im `dataProvider` oder im `httpClient`-Wrapper extrahiert und aufbereitet, um benutzerfreundlich zu sein.
<!-- TODO: translate -->
- **AuthProvider-Fehler:** Der `authProvider` (`src/providers/authProvider.ts`) behandelt Authentifizierungs- und Autorisierungsfehler. Bei einem 401-Fehler (Token abgelaufen oder ungültig) leitet er den Benutzer typischerweise zur Login-Seite weiter. Bei einem 403-Fehler (unzureichende Berechtigungen) kann eine "Zugriff verweigert"-Seite angezeigt oder eine Benachrichtigung ausgegeben werden.
<!-- TODO: translate -->
- **Globale React Error Boundary:** Eine übergeordnete React Error Boundary (`src/components/common/GlobalErrorBoundary.tsx`) wird implementiert, um unerwartete JavaScript-Fehler in der UI abzufangen, die nicht direkt API- oder Authentifizierungsfehler sind. Diese zeigt eine benutzerfreundliche Fehlermeldung an und ermöglicht es dem Benutzer ggf., die Anwendung neu zu laden.

- **Specific Error Handling:**

<!-- TODO: translate -->
- **React-Admin Formulare:** `<SimpleForm>` und verwandte Komponenten zeigen Validierungsfehler, die vom `dataProvider` (als Antwort auf `create` oder `update` Aufrufe) zurückgegeben werden, direkt an den entsprechenden Feldern an. Die API muss dafür strukturierte Fehlermeldungen (z.B. `{ errors: { fieldName: 'Error message' } }`) zurückgeben, die der `dataProvider` entsprechend aufbereitet.
<!-- TODO: translate -->
- **Benutzerdefinierte Komponenten/Services:** Fehler, die bei Aufrufen über benutzerdefinierte Services (z.B. `tenantActionsService`) auftreten, müssen in den aufrufenden Komponenten oder Hooks explizit behandelt werden (z.B. Anzeige einer Inline-Fehlermeldung, Deaktivieren eines Buttons, Auslösen einer spezifischen Benachrichtigung über den `notificationStore` oder React-Admins `useNotify` Hook).

- **Retry Logic:**

<!-- TODO: translate -->
- Standardmäßig implementiert React-Admin keine automatische Wiederholungslogik für `dataProvider`-Aufrufe.
<!-- TODO: translate -->
- Sollte eine Wiederholungslogik für bestimmte, als instabil bekannte, idempotente GET-Anfragen (z.B. bei Netzwerkproblemen) erforderlich sein, MUSS diese im zugrundeliegenden `httpClient` implementiert werden.
  - **Konfiguration (falls implementiert, z.B. mit `axios-retry` oder manuellem Wrapper):**
    - Maximale Anzahl an Wiederholungen: z.B. 2-3 Versuche.
<!-- TODO: translate -->
      - Bedingungen für Wiederholung: Nur bei Netzwerkfehlern oder spezifischen 5xx-Serverfehlern (z.B. 503 Service Unavailable).
<!-- TODO: translate -->
      - Verzögerung zwischen Wiederholungen: Exponential Backoff (z.B. 1s, 2s, 4s).
<!-- TODO: translate -->
    - **WICHTIG:** Wiederholungslogik DARF NUR für idempotente Anfragen (GET, PUT, DELETE unter bestimmten Bedingungen) angewendet werden. Für POST-Anfragen ist sie generell ungeeignet, um doppelte Ressourcenerstellung zu vermeiden. Die Notwendigkeit und Implementierung wird pro Fall geprüft.

## Routing Strategy

<!-- TODO: translate -->
Dieser Abschnitt beschreibt, wie Navigation und Routing in der ACCI EAF Control Plane UI gehandhabt werden. React-Admin nutzt intern React Router und bietet Mechanismen zur Definition von Routen für Ressourcen sowie für benutzerdefinierte Seiten.

- **Routing Library:**
<!-- TODO: translate -->
- **React Router v6.x:** Diese Version wird typischerweise von React-Admin v5.8.1 (und neuer) intern verwendet und verwaltet. Wir nutzen die von React-Admin bereitgestellten Abstraktionen und Möglichkeiten zur Integration benutzerdefinierter Routen.
<!-- TODO: translate -->
- React-Admin generiert automatisch Routen für jede definierte `<Resource>` Komponente (z.B. `/tenants`, `/tenants/create`, `/tenants/:id`, `/tenants/:id/show`).

### Route Definitions

Die meisten Routen der Anwendung werden implizit durch die `<Resource>`-Definitionen innerhalb der `<Admin>`-Komponente erstellt. Zum Beispiel:
`<Resource name="tenants" list={TenantList} edit={TenantEdit} create={TenantCreate} />` erzeugt:

- `/tenants` (Listenansicht)
- `/tenants/create` (Erstellungsansicht)
- `/tenants/:id` (Bearbeitungsansicht, Standard bei Klick in der Liste)
- `/tenants/:id/show` (Detailansicht, falls eine `show` Komponente definiert ist)

<!-- TODO: translate -->
Zusätzlich zu diesen von React-Admin verwalteten Routen können benutzerdefinierte Routen für Seiten benötigt werden, die nicht direkt einem CRUD-Modell für eine einzelne Ressource folgen. Diese werden über die `customRoutes` Prop der `<Admin>` Komponente oder als direkte Kinder von `<Routes>` innerhalb einer benutzerdefinierten Layout-Struktur integriert. Die Definitionen für diese Routen erfolgen in `src/router/customRoutes.tsx`.

| Path Pattern                | Component/Page (`src/pages/...` oder `src/features/...`) | Protection                                  | Notes                                                                                                |
| :-------------------------- | :------------------------------------------------------- | :------------------------------------------ | :--------------------------------------------------------------------------------------------------- |
<!-- TODO: translate -->
| `/`                         | `pages/DashboardPage.tsx`                                | `Authenticated`                             | Startseite nach dem Login; zeigt Übersichts-/Dashboard-Informationen.                                  |
| `/login`                    | (React-Admin Default oder `pages/LoginPage.tsx`)         | `Public` (automatische Weiterleitung falls auth.) | Login-Seite. React-Admin leitet hierher, falls nicht authentifiziert.                                |
<!-- TODO: translate -->
| `/settings`                 | `pages/GlobalSettingsPage.tsx`                           | `Authenticated`                             | Seite für globale Anwendungseinstellungen (falls benötigt).                                         |
| `/profile`                  | `pages/UserProfilePage.tsx`                              | `Authenticated`                             | Seite zur Verwaltung des eigenen Benutzerprofils.                                                    |
<!-- TODO: translate -->
| `/i18n-administration`      | `features/i18nAdmin/TranslationManagementPage.tsx`       | `Authenticated`, `Role:[ADMIN_I18N]`        | Benutzerdefinierte Seite für die Verwaltung von Übersetzungen (gemäß Epic 6, falls nicht als RA Ressource). |
| `/access-denied`            | `pages/AccessDeniedPage.tsx`                             | `Authenticated` (technisch)                 | Seite, die angezeigt wird, wenn ein Benutzer versucht, auf eine Ressource ohne Berechtigung zuzugreifen. |
| `{weiterer benutzerdef. Pfad}` | `{KomponentenPfad}`                                      | `{Public/Authenticated/Role:[ROLLE]}`       | {Anmerkungen, Parameter, Zweck}                                                                      |

**Integration von benutzerdefinierten Routen (`src/App.tsx` und `src/router/customRoutes.tsx`):**
Benutzerdefinierte Routen werden in `src/router/customRoutes.tsx` definiert und dann in die `<Admin>` Komponente importiert.

```tsx
// src/router/customRoutes.tsx
import React from 'react';
import { Route } from 'react-router-dom';
import DashboardPage from '../pages/DashboardPage';
import GlobalSettingsPage from '../pages/GlobalSettingsPage';
// ... weitere Importe

export const customRoutes = [
  <Route key="dashboard" path="/" element={<DashboardPage />} />,
  <Route key="settings" path="/settings" element={<GlobalSettingsPage />} />,
  // ... weitere benutzerdefinierte Routen
];

// src/App.tsx (Auszug)
// ...
// import { customRoutes } from './router/customRoutes';
// ...
// const App = () => (
//   <Admin
//     dataProvider={dataProvider}
//     authProvider={authProvider}
//     i18nProvider={i18nProvider}
<!-- TODO: translate -->
//     // customRoutes können hier übergeben werden oder innerhalb eines custom Layouts
//     // <CustomRoutes> {customRoutes} </CustomRoutes> // React-Admin v3/v4 Stil
<!-- TODO: translate -->
//     // Für React-Admin mit React Router v6 ist es oft Teil eines benutzerdefinierten Layouts oder <Admin dashboard={DashboardPage}> und andere Routen als Kinder.
//     // Alternative: Ein benutzerdefiniertes Layout, das <Routes> von React Router v6 verwendet.
//     // Wir werden die customRoutes-Prop nutzen oder ein benutzerdefiniertes Layout erstellen, das diese integriert.
<!-- TODO: translate -->
//     // Für RA v5+ mit React Router v6 ist es üblich, ein <Layout> zu verwenden, das die <AdminUI> Komponente enthält
<!-- TODO: translate -->
//     // und benutzerdefinierte Routen außerhalb der <AdminUI> aber innerhalb des geschützten Bereichs platziert.
<!-- TODO: translate -->
//     // Oder man übergibt sie als Kinder von <Admin>, wenn sie das Standard-Layout nutzen sollen:
//   >
//     <Resource name="tenants" list={TenantList} edit={TenantEdit} create={TenantCreate} />
//     {/* Benutzerdefinierte Routen, die das Admin-Layout verwenden */}
//     {customRoutes.map(route => route)}
//     {/* ... andere Ressourcen */}
//   </Admin>
// );
```

<!-- TODO: translate -->
Die genaue Integration von `customRoutes` in `<Admin>` hängt von der gewünschten Layout-Struktur ab. Wenn sie das Standard-React-Admin-Layout (Sidebar, AppBar) verwenden sollen, können sie als direkte Kinder der `<Admin>`-Komponente (neben `<Resource>`-Definitionen) übergeben werden. Für ein vollständig benutzerdefiniertes Verhalten können sie in einer benutzerdefinierten Layout-Komponente platziert werden.

### Route Guards / Protection

- **Authentication Guard:**

<!-- TODO: translate -->
- Die Authentifizierung wird primär durch den `authProvider` (`src/providers/authProvider.ts`) von React-Admin gesteuert. Die Methoden `authProvider.login()`, `authProvider.logout()`, `authProvider.checkAuth()` und `authProvider.checkError()` sind hierfür zentral.
<!-- TODO: translate -->
- React-Admin leitet Benutzer automatisch zur Login-Seite (konfiguriert über die `loginPage` Prop von `<Admin>`, standardmäßig `/login`), wenn `checkAuth()` einen Fehler zurückgibt oder der Benutzer nicht authentifiziert ist und versucht, auf eine geschützte Ressource oder Seite zuzugreifen.
<!-- TODO: translate -->
- Alle `<Resource>`-Komponenten und alle benutzerdefinierten Routen, die innerhalb des `<Admin>`-Kontextes gerendert werden (siehe oben), sind automatisch durch diesen Mechanismus geschützt.

- **Authorization Guard (Role-based Access Control - RBAC):**

<!-- TODO: translate -->
- React-Admin bietet keine deklarative RBAC-Lösung für Routen "out-of-the-box". Die Autorisierung wird typischerweise auf Komponentenebene oder durch bedingtes Rendern von UI-Elementen basierend auf den Berechtigungen des Benutzers gehandhabt.
<!-- TODO: translate -->
- Der `authProvider` MUSS eine Methode `getPermissions()` implementieren (z.B. `authProvider.getPermissions()`), die die Rollen oder Berechtigungen des aktuellen Benutzers zurückgibt (z.B. `['ROLE_ADMIN', 'TENANT_MEMBER_VIEW']`).
<!-- TODO: translate -->
- **Für React-Admin Ressourcen:**
<!-- TODO: translate -->
    - Die Sichtbarkeit von `<Resource>`-Komponenten in der Navigation oder der Zugriff auf bestimmte Aktionen (Bearbeiten, Erstellen, Löschen) kann durch Abrufen der Berechtigungen im `authProvider` und entsprechende Anpassung der UI oder der Resource-Props gesteuert werden. React-Admin Enterprise Edition bietet hierfür erweiterte Komponenten.
<!-- TODO: translate -->
    - Innerhalb von Listen-, Bearbeitungs- oder Detailansichten können Aktionen oder Felder basierend auf den Berechtigungen des Benutzers ein- oder ausgeblendet werden.
<!-- TODO: translate -->
- **Für benutzerdefinierte Routen/Seiten:**
<!-- TODO: translate -->
    - Eine Möglichkeit ist die Erstellung einer Wrapper-Komponente `ProtectedRoute`, die die erforderlichen Berechtigungen als Prop entgegennimmt und prüft:

            ```tsx
            // src/router/ProtectedRoute.tsx
            import React from 'react';
            import { usePermissions } from 'react-admin';
            import { Navigate, useLocation } from 'react-router-dom';
            import LoadingPage from '../pages/LoadingPage'; // Annahme: eine Ladekomponente

            interface ProtectedRouteProps {
              children: JSX.Element;
              requiredPermissions?: string[]; // z.B. ['ROLE_ADMIN']
            }

            const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children, requiredPermissions }) => {
              const { isLoading, permissions } = usePermissions();
              const location = useLocation();

              if (isLoading) {
                return <LoadingPage />; // Oder ein anderer Ladeindikator
              }

              if (!requiredPermissions || requiredPermissions.length === 0) {
                return children; // Keine spezifischen Berechtigungen erforderlich
              }

              const hasRequiredPermissions = requiredPermissions.every(rp => permissions?.includes(rp));

              if (!hasRequiredPermissions) {
                // Benutzer zur "Zugriff verweigert" Seite oder zur Dashboard-Seite mit einer Nachricht weiterleiten
                return <Navigate to="/access-denied" state={{ from: location }} replace />;
              }

              return children;
            };

            export default ProtectedRoute;
            ```

<!-- TODO: translate -->
            Diese Komponente könnte dann in `customRoutes.tsx` verwendet werden:
            `<Route path="/admin-only" element={<ProtectedRoute requiredPermissions={['ROLE_ADMIN']}><AdminOnlyPage /></ProtectedRoute>} />`
<!-- TODO: translate -->
    - Alternativ kann die Berechtigungsprüfung direkt in der Seitenkomponente erfolgen, die dann bei Bedarf auf `/access-denied` weiterleitet oder eine entsprechende Meldung anzeigt.

## Build, Bundling, and Deployment

<!-- TODO: translate -->
Dieser Abschnitt beschreibt den Build-Prozess, Optimierungsstrategien für das Bundling und die Deployment-Details für das Frontend der ACCI EAF Control Plane UI. Diese Details ergänzen den Abschnitt "Infrastructure and Deployment Overview" im Hauptarchitekturdokument.

### Build Process & Scripts

<!-- TODO: translate -->
- **Build Tool:** **Vite** wird als primäres Build-Tool und Entwicklungsserver verwendet. Die Konfiguration erfolgt über `vite.config.ts`.

- **Key Build Scripts (aus `package.json`):**
    Die folgenden Skripte sind typischerweise in `package.json` definiert:

  - `"dev"`: `vite`
    - Startet den Vite Entwicklungsserver mit Hot Module Replacement (HMR) und schnellen Ladezeiten.
  - `"build"`: `vite build`
<!-- TODO: translate -->
    - Erstellt einen optimierten Produktions-Build der Anwendung im `dist/` Verzeichnis. Dies beinhaltet Transpilierung, Bundling, Minifizierung und Generierung von Asset-Hashes für Caching.

- `"preview"`: `vite preview`
<!-- TODO: translate -->
    - Startet einen lokalen Webserver, der den Inhalt des `dist/` Verzeichnisses ausliefert. Nützlich, um den Produktions-Build vor dem Deployment lokal zu testen.

- `"test"`: `jest` (oder `vitest run` falls auf Vitest umgestellt wird)
<!-- TODO: translate -->
    - Führt Unit- und Integrationstests aus (gemäß der Tech Stack Auswahl: Jest mit React Testing Library).

- `"test:e2e"`: `playwright test`
<!-- TODO: translate -->
    - Führt End-to-End Tests mit Playwright aus.

- `"lint"`: `eslint . --ext .js,.jsx,.ts,.tsx --fix`
<!-- TODO: translate -->
    - Überprüft den Code auf Linting-Fehler gemäß den ESLint-Regeln und versucht, diese automatisch zu korrigieren.

- `"format"`: `prettier --write "src/**/*.{js,jsx,ts,tsx,css,md}"`
<!-- TODO: translate -->
    - Formatiert den Code automatisch gemäß den Prettier-Regeln.

- **Environment Configuration Management:**

  - Vite verwendet `.env` Dateien zur Verwaltung von Umgebungsvariablen. Die Konventionen sind:
    - `.env`: Standardwerte, wird versioniert.
<!-- TODO: translate -->
    - `.env.local`: Lokale Überschreibungen, wird *nicht* versioniert (in `.gitignore`).
<!-- TODO: translate -->
    - `.env.development`: Werte spezifisch für die Entwicklungsumgebung.
<!-- TODO: translate -->
    - `.env.production`: Werte spezifisch für die Produktionsumgebung.
<!-- TODO: translate -->
- Umgebungsvariablen, die im Client-Code verfügbar sein sollen, MÜSSEN mit dem Präfix `VITE_` versehen werden (z.B. `VITE_API_BASE_URL`, `VITE_APP_TITLE`).
<!-- TODO: translate -->
- Zugriff im Code über `import.meta.env.VITE_VARIABLE_NAME`.
<!-- TODO: translate -->
- KI-Agenten und Entwickler MÜSSEN sicherstellen, dass keine sensitiven Daten oder Umgebung-spezifischen Werte fest im Code verankert werden. Alle derartigen Werte MÜSSEN über den definierten Mechanismus für Umgebungsvariablen bereitgestellt werden.

### Key Bundling Optimizations

<!-- TODO: translate -->
Vite ist standardmäßig auf Performance optimiert und implementiert viele dieser Strategien automatisch.

- **Code Splitting:**
<!-- TODO: translate -->
- Vite führt standardmäßig Code-Splitting auf Routen-Ebene durch (dynamische Imports für Seitenkomponenten).
<!-- TODO: translate -->
- Für manuelles Code-Splitting von großen, nicht kritischen Komponenten oder Bibliotheken, die nicht sofort beim ersten Laden benötigt werden, MUSS die dynamische `import()`-Syntax von JavaScript (oder `React.lazy` mit `<Suspense>`) verwendet werden. Beispiel: `const HeavyComponent = React.lazy(() => import('./components/HeavyComponent'));`
- **Tree Shaking:**
<!-- TODO: translate -->
- Wird von Vite im Produktions-Build (über Rollup) automatisch angewendet, um ungenutzten Code aus den Bundles zu entfernen. Dies setzt voraus, dass Code in ES-Modulen geschrieben ist und Seiteneffekte minimiert werden.
- **Lazy Loading (Components, Images, etc.):**
<!-- TODO: translate -->
- **Komponenten:** Wie oben beschrieben, `React.lazy` in Verbindung mit `<Suspense>` für Komponenten, die nicht sofort sichtbar oder notwendig sind.
<!-- TODO: translate -->
- **Images:** Standardmäßig das `loading="lazy"` Attribut für `<img>`-Tags verwenden, um das Laden von Bildern zu verzögern, bis sie in den Viewport gelangen. Für fortgeschrittenere Bildoptimierungen (z.B. responsive Bilder, verschiedene Formate) können spezifische Komponenten oder Bibliotheken in Betracht gezogen werden, falls erforderlich.
- **Minification & Compression:**
  - **Minification:** Vite minifiziert JavaScript/TypeScript (mit esbuild/Terser), CSS (mit Lightning CSS oder esbuild) und HTML im Produktions-Build automatisch.
<!-- TODO: translate -->
- **Compression (Gzip, Brotli):** Die Komprimierung der Assets wird typischerweise von der Hosting-Plattform oder dem CDN (z.B. Vercel, Netlify, AWS CloudFront) zur Laufzeit oder während des Deployments gehandhabt. Der Build-Prozess selbst erzeugt in der Regel keine `.gz`- oder `.br`-Dateien.

### Deployment to CDN/Hosting

<!-- TODO: translate -->
- **Target Platform:** {Dieser Wert wird aus dem Hauptarchitekturdokument (Abschnitt "Infrastructure and Deployment Overview") übernommen. Gängige Plattformen für React/Vite-Anwendungen sind Vercel, Netlify, AWS S3/CloudFront, Azure Static Web Apps. Bitte hier eintragen, sobald definitiv.}
<!-- TODO: translate -->
- **Deployment Trigger:** {Dieser Wert wird aus dem Hauptarchitekturdokument (Abschnitt CI/CD-Pipeline) übernommen. Typischerweise ein Git-Push auf den `main`- oder `production`-Branch über eine CI/CD-Pipeline (z.B. GitHub Actions, GitLab CI).}
- **Asset Caching Strategy:**
<!-- TODO: translate -->
- **Immutable Assets:** JavaScript- und CSS-Bundles, die von Vite mit Inhalts-Hashes im Dateinamen generiert werden (z.B. `app.[hash].js`), MÜSSEN mit langen `Cache-Control`-Headern ausgeliefert werden (z.B. `public, max-age=31536000, immutable`). Dies stellt sicher, dass Browser diese Dateien aggressiv zwischenspeichern.
<!-- TODO: translate -->
- **`index.html`:** Die Haupt-HTML-Datei MUSS mit kürzeren `Cache-Control`-Headern oder `Cache-Control: no-cache` / `Cache-Control: public, max-age=0, must-revalidate` ausgeliefert werden. Dies stellt sicher, dass Benutzer immer die aktuellste Version der Anwendung erhalten, die dann die versionierten Assets lädt.
<!-- TODO: translate -->
- **Andere statische Assets (im `public/` Ordner):** Die Cache-Strategie hängt von der Volatilität der Assets ab. Unveränderliche Assets können lange Cache-Zeiten haben, während sich ändernde Assets kürzere Zeiten oder Validierungsmechanismen benötigen.
- **Konfiguration:** Die Cache-Header werden typischerweise auf der Hosting-Plattform oder dem CDN konfiguriert.

## Frontend Testing Strategy

<!-- TODO: translate -->
Dieser Abschnitt baut auf der "Overall Testing Strategy" des Hauptarchitekturdokuments auf und detailliert die spezifischen Aspekte des Frontend-Testens für die ACCI EAF Control Plane UI. Die im Hauptdokument und im initialen Prompt für den Design Architect festgelegten Werkzeuge sind Jest, React Testing Library und Playwright.

- **Link to Main Overall Testing Strategy:** {Verweis auf den entsprechenden Abschnitt im Hauptarchitekturdokument `docs/ACCI-EAF-Architecture.md#overall-testing-strategy`} (Bitte genauen Link/Anker bei Bedarf anpassen).

### Component Testing

- **Scope:** Testen einzelner React-Komponenten in Isolation. Dies umfasst sowohl wiederverwendbare UI-Elemente (aus `src/components/`) als auch spezifischere Komponenten innerhalb von Feature-Modulen (z.B. `src/features/tenants/components/TenantStatusChip.tsx`).
- **Tools:**
<!-- TODO: translate -->
- **Jest (v29.7.0):** Als Test-Runner, Assertion-Bibliothek und für Mocking-Funktionalitäten.
- **React Testing Library (RTL) (v16.3.x):** Zum Rendern von Komponenten in einer Testumgebung und zur Interaktion mit ihnen auf eine Weise, die das Benutzerverhalten simuliert. Der Fokus liegt auf dem Testen des Komponentenverhaltens aus Sicht des Benutzers, nicht auf Implementierungsdetails.
- **Focus:**
<!-- TODO: translate -->
- **Rendering:** Korrekte Darstellung der Komponente mit verschiedenen Props (einschließlich Edge Cases und optionaler Props).
- **User Interactions:** Simulation von Benutzerinteraktionen wie Klicks, Eingaben, Formularabsendungen (`fireEvent` oder `@testing-library/user-event` von RTL).
<!-- TODO: translate -->
- **Event Emission / Callback Handling:** Überprüfung, ob Callbacks korrekt aufgerufen werden und ob benutzerdefinierte Events mit den richtigen Parametern ausgelöst werden.
<!-- TODO: translate -->
- **Accessibility (AX):** Grundlegende AX-Checks mit `jest-axe` können integriert werden, um sicherzustellen, dass Komponenten keine offensichtlichen WCAG-Verletzungen aufweisen (siehe auch Abschnitt "Accessibility (AX) Implementation Details").
<!-- TODO: translate -->
- **Snapshot Testing:** MUSS sparsam und mit klarer Begründung eingesetzt werden (z.B. für sehr stabile, rein präsentationale Komponenten mit komplexer, aber fester DOM-Struktur). Bevorzugt werden explizite Assertions über das Vorhandensein und den Inhalt von Elementen.
- **Location:** Testdateien (`*.test.tsx` oder `*.spec.tsx`) werden direkt neben den zu testenden Komponentendateien im `src/` Verzeichnis abgelegt (Co-Location) oder in einem `__tests__` Unterverzeichnis innerhalb des Komponentenordners.

    ```
    // Beispielstruktur:
    // src/components/common/MyButton.tsx
    // src/components/common/MyButton.test.tsx
    ```

<!-- TODO: translate -->
- **Code Coverage:** Es wird eine Code-Coverage von mindestens {z.B., 70-80%, zu definierender Schwellenwert} für Komponenten-Tests angestrebt. Coverage-Reports werden durch Jest generiert und können in die CI-Pipeline integriert werden.

### Feature/Flow Testing (UI Integration)

<!-- TODO: translate -->
- **Scope:** Testen des Zusammenspiels mehrerer Komponenten innerhalb eines Features oder eines kleinen Benutzer-Flows auf einer Seite. Dies kann das Testen eines vollständigen Formulars, die Interaktion zwischen einer Liste und einem Detailbereich oder die Navigation innerhalb eines klar abgegrenzten Feature-Moduls umfassen. API-Aufrufe und globale Zustandsänderungen werden typischerweise gemockt.
<!-- TODO: translate -->
- **Tools:** Dieselben Werkzeuge wie für Komponententests (Jest und React Testing Library). Der Aufbau der Tests wird komplexer sein, da ggf. Mock-Provider für React Router, React-Admin (`<Admin dataProvider={mockDataProvider} ...>`), Kontext-Provider oder Zustand-Stores benötigt werden.
- **Focus:**
  - Datenfluss zwischen den beteiligten Komponenten.
<!-- TODO: translate -->
- Bedingtes Rendern basierend auf Interaktionen und Zustandsänderungen.
- Korrekte Aktualisierung der UI als Reaktion auf Benutzeraktionen und gemockte Service-Antworten.
- Navigation innerhalb des getesteten Features (z.B. von einer Listenansicht zur Detailansicht eines Elements innerhalb des Mocks).
<!-- TODO: translate -->
- **Location:** Ähnlich wie Komponententests, können diese Tests neben den Hauptkomponenten eines Features oder in einem dedizierten Testverzeichnis innerhalb des Feature-Moduls liegen.

### End-to-End UI Testing Tools & Scope

<!-- TODO: translate -->
- **Tools:** **Playwright (v1.52.x)** (gemäß Tech Stack Auswahl). Playwright ermöglicht browserübergreifende Tests und bietet robuste Mechanismen zur Interaktion mit Webseiten.
<!-- TODO: translate -->
- **Scope (Frontend Focus):** Es MÜSSEN 3-5 kritische End-to-End User Journeys aus UI-Sicht abgedeckt werden. Diese testen die Anwendung als Ganzes, interagieren mit einer (potenziell gemockten oder dedizierten Test-) Backend-API und validieren den gesamten Benutzerfluss.
<!-- TODO: translate -->
    Beispiele für User Journeys:
<!-- TODO: translate -->
    1. **Benutzer-Login und Dashboard-Anzeige:** Erfolgreicher Login, Überprüfung der korrekten Weiterleitung zum Dashboard und Anzeige grundlegender Dashboard-Elemente.
<!-- TODO: translate -->
    2. **Mandantenverwaltung (Erstellen & Bearbeiten):** Anlegen eines neuen Mandanten über das Formular, Überprüfung der Anzeige in der Liste, Öffnen zum Bearbeiten, Ändern von Daten, Speichern und Validierung der Änderungen.
<!-- TODO: translate -->
    3. **Benutzerzuweisung zu einem Mandanten:** Erstellen eines Benutzers und Zuweisung zu einem existierenden Mandanten, Überprüfung der korrekten Verknüpfung.
<!-- TODO: translate -->
    4. **Lizenzaktivierung (falls UI-Flow vorhanden):** Durchlaufen des UI-Prozesses zur Aktivierung einer Lizenz für einen Mandanten.
<!-- TODO: translate -->
    5. **Navigation und grundlegende UI-Konsistenz:** Überprüfung der Hauptnavigationselemente, Erreichbarkeit wichtiger Seiten und Konsistenz von Header/Footer/Layout.

- **Test Data Management for UI:**
<!-- TODO: translate -->
- Für E2E-Tests ist eine konsistente Testdatenstrategie entscheidend. Optionen:
<!-- TODO: translate -->
    - **API Mocking Layer:** Verwendung von Werkzeugen wie Mock Service Worker (MSW) um API-Antworten im Browser während der E2E-Tests zu simulieren. Dies bietet hohe Kontrolle und Geschwindigkeit.
<!-- TODO: translate -->
    - **Dedizierte Test-Backend-Instanz:** Verwendung einer separaten Backend-Instanz, die mit einem definierten Satz von Testdaten läuft oder vor jeder Testausführung zurückgesetzt wird.
    - **Test Accounts:** Verwendung vordefinierter Test-Benutzerkonten mit spezifischen Rollen und Daten.
<!-- TODO: translate -->
- Die gewählte Strategie muss sicherstellen, dass Tests reproduzierbar sind und nicht durch veränderliche Daten fehlschlagen. Für den Start wird API Mocking mit MSW oder eine Backend-Seeding-Strategie empfohlen.
<!-- TODO: translate -->
- **Page Object Model (POM):** Zur Verbesserung der Wartbarkeit und Lesbarkeit von E2E-Tests SOLLTE das Page Object Model (oder eine ähnliche Abstraktion wie Screenplay Pattern) verwendet werden. Selektoren und Interaktionslogik für bestimmte Seiten oder wiederverwendbare UI-Bereiche werden in separaten Klassen/Modulen gekapselt.
- **Location:** E2E-Tests befinden sich im Verzeichnis `controlplane-ui/tests/e2e/`.

## Accessibility (AX) Implementation Details

<!-- TODO: translate -->
Barrierefreiheit ist ein integraler Bestandteil der Entwicklung des ACCI EAF Control Plane UI. Ziel ist es, die WCAG 2.1 AA-Konformität zu erreichen, um sicherzustellen, dass die Anwendung von möglichst vielen Menschen, einschließlich solcher mit Behinderungen, genutzt werden kann. Die UI/UX-Spezifikation (abgeleitet aus dem PRD) dient als Grundlage für die AX-Anforderungen. React-Admin bringt bereits eine gute Basis für Barrierefreiheit mit, da es oft auf Material UI basiert, das seinerseits Wert auf AX legt.

- **Semantic HTML:**

<!-- TODO: translate -->
- **Mandat:** Entwickler und KI-Agenten MÜSSEN semantisch korrekte HTML5-Elemente verwenden. `<div>` und `<span>` Elemente sind nur für Layoutzwecke oder wenn kein passenderes semantisches Element existiert, zu verwenden. Native Elemente wie `<nav>`, `<aside>`, `<main>`, `<article>`, `<section>`, `<button>`, `<input type="...">`, `<label>`, `<table>` etc. sind zu bevorzugen, da sie von assistiven Technologien (AT) besser interpretiert werden können.
- React-Admin Komponenten verwenden im Allgemeinen semantisches HTML. Bei benutzerdefinierten Komponenten ist dies besonders zu beachten.

- **ARIA Implementation (Accessible Rich Internet Applications):**

<!-- TODO: translate -->
- **Grundsatz:** ARIA-Attribute sollen nur dann verwendet werden, wenn semantisches HTML allein nicht ausreicht, um die Rolle, den Zustand oder die Eigenschaften einer Komponente für AT verständlich zu machen (Rule of ARIA: "No ARIA is better than bad ARIA").
- **React-Admin:** Viele React-Admin Komponenten (besonders Formularfelder und Interaktionselemente) bringen bereits korrekte ARIA-Attribute mit.
<!-- TODO: translate -->
- **Benutzerdefinierte Komponenten:** Für komplexe benutzerdefinierte Komponenten (z.B. benutzerdefinierte Dropdowns, Modals, Tab-Interfaces, die nicht von React-Admin oder einer zugrundeliegenden UI-Bibliothek wie Material UI bereitgestellt werden) MÜSSEN die entsprechenden ARIA-Patterns aus den WAI-ARIA Authoring Practices Guide (APG) implementiert werden. Dies beinhaltet:
  - Korrekte `role` Attribute (z.B. `role="dialog"`, `role="tablist"`, `role="tab"`, `role="tabpanel"`).
  - Zustandsattribute wie `aria-expanded`, `aria-selected`, `aria-haspopup`, `aria-disabled`, `aria-hidden`, `aria-invalid`.
<!-- TODO: translate -->
    - Eigenschaftsattribute wie `aria-label`, `aria-labelledby`, `aria-describedby` (besonders für Formularfelder ohne sichtbares Label oder mit zusätzlichen Beschreibungen).
<!-- TODO: translate -->
    - `aria-live` für dynamische Inhaltsänderungen (z.B. Benachrichtigungen, Ladezustände).
<!-- TODO: translate -->
- Alle Icons und rein visuellen Elemente, die eine Bedeutung tragen, MÜSSEN einen textuellen Alternativtext via `aria-label` oder visuell verstecktem Text haben, falls kein sichtbares Label vorhanden ist. Dekorative Icons benötigen `aria-hidden="true"`.

- **Keyboard Navigation:**

<!-- TODO: translate -->
- **Mandat:** Alle interaktiven Elemente der Anwendung MÜSSEN ausschließlich per Tastatur bedienbar sein. Dies schließt Links, Buttons, Formularfelder, Menüs und benutzerdefinierte interaktive Komponenten ein.
- **Fokusreihenfolge:** Die Fokusreihenfolge beim Navigieren mit der Tab-Taste MUSS logisch und intuitiv sein und der visuellen Anordnung der Elemente folgen.
<!-- TODO: translate -->
- **Interaktionsmuster:** Standardmäßige Tastaturinteraktionen für HTML-Elemente (z.B. Leertaste/Enter für Buttons, Pfeiltasten für Radio-Button-Gruppen) MÜSSEN funktionieren. Für benutzerdefinierte Komponenten MÜSSEN die im ARIA APG spezifizierten Tastaturinteraktionsmuster implementiert werden (z.B. Pfeiltasten für benutzerdefinierte Slider, Tabs; Escape zum Schließen von Modals).
<!-- TODO: translate -->
- **Fokusindikator:** Ein klar sichtbarer Fokusindikator MUSS für alle fokussierbaren Elemente vorhanden sein. Dieser wird typischerweise vom Browser oder der zugrundeliegenden UI-Bibliothek bereitgestellt, aber es ist sicherzustellen, dass er nicht durch benutzerdefinierte Styles unterdrückt wird.

- **Focus Management:**

<!-- TODO: translate -->
- **Modals und Dialoge:** Wenn ein Modal oder Dialog geöffnet wird, MUSS der Fokus auf das erste fokussierbare Element innerhalb des Modals oder auf das Modal selbst gesetzt werden. Der Fokus MUSS innerhalb des Modals gefangen sein (Focus Trap), bis es geschlossen wird. Beim Schließen MUSS der Fokus auf das Element zurückkehren, das das Modal ausgelöst hat.
<!-- TODO: translate -->
- **Dynamische Inhaltsänderungen:** Bei signifikanten Änderungen des Seiteninhalts (z.B. nach einer Suche, Laden neuer Datenabschnitte) oder Routenwechseln SOLLTE der Fokus an den Anfang des neuen Inhaltsbereichs oder auf eine passende Überschrift verschoben werden, um Screenreader-Nutzern die Orientierung zu erleichtern.
<!-- TODO: translate -->
- **Benachrichtigungen:** Bei `aria-live` Regionen, die Benachrichtigungen anzeigen, ist darauf zu achten, dass diese den Nutzer nicht unnötig unterbrechen.

- **Testing Tools for AX:**

  - **Browser Developer Tools:** Integrierte Accessibility-Inspektoren (z.B. in Chrome, Firefox, Edge).
<!-- TODO: translate -->
- **Axe DevTools (Browser Extension):** Für automatisierte Tests und manuelle Überprüfungen während der Entwicklung.
<!-- TODO: translate -->
- **`@axe-core/react` oder `jest-axe`:** Integration von Axe-Core in Komponententests (Jest), um grundlegende AX-Verletzungen frühzeitig zu erkennen. Tests SOLLTEN bei neuen WCAG 2.1 AA Verstößen fehlschlagen.

        ```javascript
<!-- TODO: translate -->
        // Beispiel für jest-axe in einem Komponententest
        // import { render } from '@testing-library/react';
        // import { axe, toHaveNoViolations } from 'jest-axe';
        // expect.extend(toHaveNoViolations);

        // it('should have no axe violations', async () => {
        //   const { container } = render(<MyComponent />);
        //   const results = await axe(container);
        //   expect(results).toHaveNoViolations();
        // });
        ```

<!-- TODO: translate -->
- **Lighthouse (in Chrome DevTools):** Für Audits der Gesamtseite, einschließlich Accessibility.
- **Manuelle Tests:**
<!-- TODO: translate -->
    - **Tastaturnavigation:** Vollständige Durchtestung aller interaktiven Elemente nur mit der Tastatur.
<!-- TODO: translate -->
    - **Screenreader-Tests:** Überprüfung kritischer User Journeys mit gängigen Screenreadern (z.B. NVDA für Windows, VoiceOver für macOS, TalkBack für Android).

<!-- TODO: translate -->
- **Ausrichtung an UI/UX Spezifikation:** Diese technischen Implementierungsdetails MÜSSEN die AX-Anforderungen aus der UI/UX Spezifikation (abgeleitet aus dem PRD) erfüllen, insbesondere bezüglich Kontrastverhältnissen (mindestens 4.5:1 für normalen Text, 3:1 für großen Text und UI-Komponenten), responsiven Designs und verständlicher Sprache.

## Performance Considerations

<!-- TODO: translate -->
Eine gute Performance ist entscheidend für eine positive User Experience der ACCI EAF Control Plane UI. Dieser Abschnitt beschreibt spezifische Strategien und Techniken zur Optimierung der Frontend-Performance. Viele dieser Aspekte werden durch die Wahl von Vite als Build-Tool und React-Admin als Framework bereits positiv beeinflusst, erfordern aber bewusste Anwendung und Überwachung.

- **Image Optimization:**

<!-- TODO: translate -->
- **Formate:** Moderne Bildformate wie WebP SOLLTEN bevorzugt werden, da sie bei vergleichbarer Qualität oft kleiner sind als traditionelle Formate (JPEG, PNG). Für Icons und einfache Grafiken SIND Vektorgrafiken (SVG) zu verwenden, da sie verlustfrei skalierbar und meist sehr klein sind.
<!-- TODO: translate -->
- **Responsive Images:** Für Bilder, die in verschiedenen Größen auf unterschiedlichen Viewports angezeigt werden, SOLLTE das `<picture>`-Element oder das `srcset`-Attribut des `<img>`-Tags verwendet werden, um dem Browser die Auswahl der passendsten Bildgröße zu ermöglichen.
<!-- TODO: translate -->
- **Lazy Loading:** Das `loading="lazy"` Attribut für `<img>`-Tags MUSS standardmäßig verwendet werden, um das Laden von Bildern zu verzögern, bis sie in den sichtbaren Bereich des Benutzers gelangen. Dies verbessert die initiale Ladezeit der Seite erheblich.
<!-- TODO: translate -->
- **Kompression:** Bilder MÜSSEN vor dem Hochladen optimiert und komprimiert werden (z.B. mit Tools wie ImageOptim, Squoosh oder automatisierten Build-Prozessen).
<!-- TODO: translate -->
- **Implementation Mandate:** Wo immer möglich, SOLLTEN Bilder über Komponenten verwaltet werden, die Optimierungen wie Lazy Loading und eventuell `srcset` kapseln. SVGs sind für Icons und Logos zu bevorzugen.

- **Code Splitting & Lazy Loading (reiterate from Build section if needed):**

<!-- TODO: translate -->
- **Impact:** Reduziert die Größe des initialen JavaScript-Bundles, was zu schnelleren Ladezeiten der Anwendung (Time to Interactive - TTI) führt. Vite führt automatisches Code-Splitting auf Routen-Ebene durch.
<!-- TODO: translate -->
- **Implementation Mandate:** `React.lazy` mit `<Suspense>` MUSS für Komponenten verwendet werden, die nicht sofort beim ersten Rendern benötigt werden, insbesondere für:
  - Komponenten auf Routen, die nicht die Startroute sind.
<!-- TODO: translate -->
    - Große oder komplexe Komponenten, die erst durch Benutzerinteraktion sichtbar werden (z.B. Modals, komplexe Formularabschnitte).
<!-- TODO: translate -->
    - Module oder Bibliotheken von Drittanbietern, die nur für spezifische Funktionalitäten benötigt werden.

- **Minimizing Re-renders:**

<!-- TODO: translate -->
- Unnötige Re-Renders von React-Komponenten können die Performance beeinträchtigen.
- **Implementation Mandate:**
<!-- TODO: translate -->
    - `React.memo` MUSS für funktionale Komponenten verwendet werden, die häufig mit denselben Props gerendert werden, um unnötige Neu-Renderings zu verhindern.
<!-- TODO: translate -->
    - Props sollten stabil gehalten werden. Das direkte Übergeben von neuen Objekt- oder Array-Literalen sowie Inline-Funktionen als Props in Render-Methoden SOLLTE vermieden werden, wenn dies zu unnötigen Re-Renders von Kindkomponenten führt. `useCallback` und `useMemo` Hooks sind hierfür geeignete Werkzeuge.
<!-- TODO: translate -->
    - Die Datenstruktur im Zustand (React Context, Zustand oder von React-Admin verwalteter Zustand) SOLLTE so gestaltet sein, dass Komponenten nur die Teile des Zustands abonnieren, die für sie relevant sind, um Re-Renders bei irrelevanten Zustandsänderungen zu minimieren (Zustand und React-Admin handhaben dies oft gut durch ihre Selektor/Hook-Mechanismen).
<!-- TODO: translate -->
    - Bei der Arbeit mit Listen SOLLTE immer ein stabiler `key`-Prop für jedes Listenelement verwendet werden.

- **Debouncing/Throttling:**

<!-- TODO: translate -->
- Für Event-Handler, die häufig ausgelöst werden können (z.B. bei Texteingaben in Suchfeldern, Fenstergrößenänderungen, Scroll-Events).
- **Implementation Mandate:**
<!-- TODO: translate -->
    - **Debouncing** (Auslösen der Funktion erst nach einer Pause der Events) MUSS für Aktionen wie API-Aufrufe bei Suchfeldeingaben verwendet werden.
<!-- TODO: translate -->
    - **Throttling** (Begrenzung der Ausführungshäufigkeit) KANN für Event-Handler wie Scroll- oder Resize-Listener verwendet werden, wenn diese komplexe Berechnungen durchführen.
<!-- TODO: translate -->
    - Hierfür können Utility-Funktionen (z.B. aus `lodash.debounce` / `lodash.throttle` oder benutzerdefinierte Hooks) verwendet werden. Die Wartezeiten (debounce/throttle delay) sind angemessen zu wählen (z.B. 300-500ms für Debouncing von Suchanfragen).

<!-- TODO: translate -->
- **Virtualization (für lange Listen):**

<!-- TODO: translate -->
- React-Admin's `<Datagrid>` und `<SimpleList>` rendern standardmäßig alle Elemente einer Seite. Bei sehr großen Listen (z.B. hunderte oder tausende Einträge auf einer Seite, falls Paginierung clientseitig erfolgen würde oder die Seitengröße sehr groß ist) kann dies die Performance beeinträchtigen.
<!-- TODO: translate -->
- **Implementation Mandate:** Wenn Performance-Probleme bei der Darstellung sehr langer Listen auftreten (typischerweise \>100-200 Elemente ohne serverseitige Paginierung oder mit sehr großen, clientseitig geladenen Datensätzen), MUSS der Einsatz von Virtualisierungsbibliotheken wie `react-window` oder `TanStack Virtual` in Betracht gezogen werden. Diese rendern nur die sichtbaren Elemente und verbessern so die Render-Performance und den Speicherverbrauch erheblich. React-Admin Enterprise bietet ggf. virtualisierte Komponenten an.

- **Caching Strategies (Client-Side):**

<!-- TODO: translate -->
- **HTTP Caching:** Wie im Abschnitt "Build, Bundling, and Deployment" beschrieben, ist eine korrekte HTTP-Cache-Konfiguration für Assets entscheidend.
<!-- TODO: translate -->
- **React-Admin DataProvider Caching:** React-Admin selbst implementiert eine Form von Caching für Ressourcen, um wiederholte Anfragen für bereits geladene Daten zu vermeiden. Dies wird genutzt.
<!-- TODO: translate -->
- **Service Workers:** Da PWA-Funktionalität nicht als notwendig erachtet wird, wird die Implementierung eines Service Workers für Caching-Strategien zunächst nicht verfolgt.

- **Performance Monitoring Tools:**

  - **Browser Developer Tools:**
<!-- TODO: translate -->
    - **Performance Tab:** Zur Analyse von JavaScript-Ausführungszeiten, Rendering-Performance und Identifizierung von Engpässen.
<!-- TODO: translate -->
    - **Network Tab:** Zur Analyse von Ladezeiten und Größen von Assets.
<!-- TODO: translate -->
    - **Lighthouse Audit (integriert):** Für eine Gesamtbewertung der Performance, Accessibility, Best Practices und SEO.

- **React Developer Tools (Browser Extension):** Insbesondere der Profiler, um Re-Renders von Komponenten zu identifizieren und zu analysieren.
- **WebPageTest / GTmetrix:** Externe Tools zur Analyse der Ladeperformance unter verschiedenen Bedingungen.
<!-- TODO: translate -->
- **CI-Integration:** Lighthouse-Scores oder andere Performance-Metriken KÖNNEN in die CI-Pipeline integriert werden, um Performance-Regressionen frühzeitig zu erkennen.

## Internationalization (i18n) and Localization (l10n) Strategy

<!-- TODO: translate -->
Die ACCI EAF Control Plane UI muss mehrsprachig sein, um verschiedenen Benutzergruppen gerecht zu werden. Epic 6 ("i18n Control Plane Integration") im PRD beschreibt die Notwendigkeit, dass das Control Plane selbst internationalisiert wird und die Verwaltung von Übersetzungen für die Endanwendungen des EAF ermöglicht. Dieser Abschnitt konzentriert sich auf die i18n-Implementierung des *Control Plane UI selbst*.

<!-- TODO: translate -->
- **Requirement Level:** Erforderlich. Die Basissprachen sind Deutsch (de-DE) und Englisch (en-US). Die Architektur muss so gestaltet sein, dass weitere Sprachen bei Bedarf einfach hinzugefügt werden können.

- **Chosen i18n Library/Framework:**

  - **React-Admin `i18nProvider`:** React-Admin bietet eine robuste `i18nProvider` Schnittstelle und eine Standardimplementierung (`ra-i18n-polyglot`), die auf `node-polyglot` basiert. Diese wird als Grundlage verwendet.
<!-- TODO: translate -->
- **`i18next` (Potenziell):** Für erweiterte Funktionen, die über `ra-i18n-polyglot` hinausgehen (z.B. komplexere Pluralisierungsregeln, Kontext, Namensraum-Management für sehr große Anwendungen, oder falls die Backend-API Übersetzungen in einem i18next-kompatiblen Format liefert), KÖNNTE `i18next` zusammen mit `react-i18next` und einem Adapter für React-Admin (`ra-i18n-i18next`) in Betracht gezogen werden. Für den Start wird jedoch die Standardlösung von React-Admin angestrebt, um die Komplexität gering zu halten.
- Die Konfiguration erfolgt in `src/providers/i18nProvider.ts`.

- **Translation File Structure & Format:**

<!-- TODO: translate -->
- **Format:** JSON-Dateien. Jede Datei enthält Schlüssel-Wert-Paare für eine Sprache.
<!-- TODO: translate -->
- **Struktur:** Pro Sprache eine Datei, die alle Übersetzungen für die Control Plane UI enthält. Diese Dateien werden im Verzeichnis `controlplane-ui/public/locales/` abgelegt (z.B. `controlplane-ui/public/locales/en.json`, `controlplane-ui/public/locales/de.json`).

        ```json
        // Beispiel: public/locales/en.json
        {
<!-- TODO: translate -->
          "ra": { // React-Admin interne Übersetzungen
            "action": {
              "save": "Save",
              "cancel": "Cancel"
            },
<!-- TODO: translate -->
            // ... weitere ra Schlüssel
          },
<!-- TODO: translate -->
          "app": { // Anwendungsspezifische Übersetzungen
            "dashboard": {
              "title": "Dashboard",
              "welcomeMessage": "Welcome to the EAF Control Plane!"
            },
            "tenants": {
              "title": "Tenants",
              "fields": {
                "name": "Name",
                "identifier": "Identifier"
              },
              "actions": {
                "create": "Create Tenant"
              }
            },
            "validation": {
                "required": "This field is required."
            }
<!-- TODO: translate -->
            // ... weitere App-Schlüssel
          }
        }
        ```

- Die Sprachdateien werden vom `i18nProvider` beim Start der Anwendung geladen.

- **Translation Key Naming Convention:**

<!-- TODO: translate -->
- **React-Admin Keys:** React-Admin verwendet eigene Schlüssel für Standard-UI-Elemente (z.B. `ra.action.save`). Diese können bei Bedarf überschrieben werden.
<!-- TODO: translate -->
- **Custom Application Keys:** Für anwendungsspezifische Texte wird eine hierarchische Struktur verwendet, beginnend mit `app.` gefolgt von Feature/Modul, dann Komponente/Kontext und schließlich dem spezifischen Element.
<!-- TODO: translate -->
    - Beispiel: `app.tenants.fields.name` (für das Feld "Name" in der Mandantenverwaltung).
<!-- TODO: translate -->
    - Beispiel: `app.notifications.tenantCreatedSuccess` (für eine Erfolgsmeldung).
<!-- TODO: translate -->
- Die Schlüssel MÜSSEN konsistent und gut dokumentiert sein (z.B. in einem zentralen Dokument oder direkt in den Basissprachdateien mit Kommentaren).
<!-- TODO: translate -->
- Dynamisch generierte Schlüssel sind zu vermeiden, da sie die statische Analyse und Extraktion von Schlüsseln erschweren.

- **Process for Adding New Translatable Strings:**

<!-- TODO: translate -->
- Entwickler oder KI-Agenten MÜSSEN neue Übersetzungsschlüssel zuerst der primären Entwicklungs-Sprachdatei (z.B. `en.json`) hinzufügen.
<!-- TODO: translate -->
- Im Code MÜSSEN die Übersetzungsfunktionen von React-Admin (z.B. der `useTranslate` Hook oder die `translate` HOC) verwendet werden, um Texte zu rendern.

        ```tsx
        import { useTranslate } from 'react-admin';

        const MyComponent = () => {
          const translate = useTranslate();
          return (
            <div>
              <h1>{translate('app.dashboard.title')}</h1>
              <p>{translate('app.tenants.fields.name')}</p>
            </div>
          );
        };
        ```

<!-- TODO: translate -->
- Für Texte mit dynamischen Werten (Variablen) oder HTML-Inhalten sind die entsprechenden Funktionen des i18n-Providers zu nutzen (z.B. `translate('app.welcomeMessage', { name: userName })`).

- **Handling Pluralization:**

<!-- TODO: translate -->
- `ra-i18n-polyglot` unterstützt einfache Pluralisierungsregeln über die "smart count" Funktionalität von Polyglot (z.B. `key_one: '1 item', key_other: '%{smart_count} items'`).
<!-- TODO: translate -->
- Für komplexere Pluralisierungsanforderungen, die über die Basisfunktionalität hinausgehen, müsste die Eignung von `i18next` (das ICU-Message-Format unterstützt) evaluiert werden. Für den Anfang werden die Möglichkeiten von `ra-i18n-polyglot` genutzt.

- **Date, Time, and Number Formatting:**

<!-- TODO: translate -->
- React-Admin Komponenten (wie `<DateField>`, `<NumberField>`) handhaben die Lokalisierung von Datums-, Zeit- und Zahlenformaten basierend auf der aktiven Sprache und den Locale-Optionen, die dem `i18nProvider` oder den Komponenten selbst übergeben werden.
<!-- TODO: translate -->
- Für benutzerdefinierte Komponenten, die Datums-, Zeit- oder Zahlenformate anzeigen, SOLLTE die native `Intl` API von JavaScript oder eine leichtgewichtige Bibliothek wie `date-fns` (mit Locale-Unterstützung) verwendet werden, um eine korrekte Lokalisierung sicherzustellen. Die Formatierungsoptionen sind konsistent zu halten.

<!-- TODO: translate -->
- **Default Language:** `en-US` (Englisch, USA) wird als Fallback-Sprache definiert. Wenn ein Schlüssel in der aktiven Sprache nicht gefunden wird, wird auf Englisch zurückgegriffen.

- **Language Switching Mechanism:**

<!-- TODO: translate -->
- React-Admin bietet eine `<LocalesMenuButton>` Komponente, die in die `<AppBar>` integriert werden kann, um dem Benutzer die Sprachauswahl zu ermöglichen.
<!-- TODO: translate -->
- Die gewählte Sprache wird im Anwendungszustand (von React-Admin verwaltet) und typischerweise im `localStorage` persistiert, um die Auswahl des Benutzers bei zukünftigen Besuchen beizubehalten.
<!-- TODO: translate -->
- Der `i18nProvider` wird so konfiguriert, dass er die initiale Sprache aus dem `localStorage` oder den Browser-Einstellungen des Benutzers erkennt und standardmäßig Englisch verwendet, falls keine Präferenz gefunden wird.

<!-- TODO: translate -->
- **Verwaltung der Übersetzungen durch die Control Plane UI (Epic 6):**

<!-- TODO: translate -->
- Das Control Plane UI wird selbst eine Schnittstelle zur Verwaltung der Übersetzungsdateien für die *Endanwendungen* des EAF bereitstellen. Die hier beschriebene i18n-Strategie bezieht sich auf die Mehrsprachigkeit des Control Plane UI *selbst*. Die Funktionalität zur Verwaltung externer Übersetzungsdateien (z.B. Hochladen, Bearbeiten, Herunterladen von JSON-Dateien für andere Anwendungen) wird als separates Feature innerhalb des Control Plane UI implementiert (z.B. unter `/i18n-administration` wie im Routing-Abschnitt angedeutet) und interagiert mit der `eaf-controlplane-api` für Speicherung und Abruf dieser Daten.

## Feature Flag Management

<!-- TODO: translate -->
Feature Flags (auch als Feature Toggles bekannt) ermöglichen es, neue Funktionalitäten schrittweise oder für bestimmte Benutzergruppen freizuschalten, A/B-Tests durchzuführen oder Features schnell zu deaktivieren, falls Probleme auftreten, ohne einen neuen Deploy-Vorgang starten zu müssen.

<!-- TODO: translate -->
- **Requirement Level:** Für die ACCI EAF Control Plane UI werden Feature Flags initial als **"Genutzt für spezifische Rollouts und kontrollierte Freigaben"** eingestuft. Dies bedeutet, dass nicht jedes kleine Feature einen Flag benötigt, aber für größere neue Module, riskante Änderungen oder Features, die schrittweise eingeführt werden sollen, können sie eingesetzt werden. Eine umfassende, tief integrierte Feature-Flag-Kultur ist für den Start nicht zwingend, aber die Architektur sollte die Möglichkeit vorsehen.

- **Chosen Feature Flag System/Library:**

<!-- TODO: translate -->
- Für den initialen Bedarf wird eine **einfache, Konfigurations-basierte Lösung** angestrebt. Dies könnte über Umgebungsvariablen (zur Build-Zeit) oder eine vom Backend (`eaf-controlplane-api`) bereitgestellte Konfigurations-API geschehen, die beim Start der Anwendung abgefragt wird.
<!-- TODO: translate -->
- **Option 1 (Umgebungsvariablen):** `VITE_FEATURE_NEW_DASHBOARD_ENABLED=true`. Einfach für Build-zeitliche Flags, aber nicht dynamisch zur Laufzeit änderbar ohne neuen Build/Deployment.
<!-- TODO: translate -->
- **Option 2 (Backend Configuration Service):** Die Frontend-Anwendung ruft beim Start einen Endpunkt der `eaf-controlplane-api` auf (z.B. `/controlplane/api/v1/ui-features`), der eine JSON-Struktur mit aktiven Flags zurückgibt. Dies ermöglicht dynamischere Änderungen ohne Frontend-Deployment.

        ```json
        // Beispiel Antwort von /ui-features
        {
          "newTenantDashboard": true,
          "betaUserImport": false,
          "experimentalLicenseReport": true
        }
        ```

<!-- TODO: translate -->
- **Bevorzugte Lösung:** **Option 2 (Backend Configuration Service)** wird bevorzugt, da sie mehr Flexibilität bietet. Die Konfiguration der Flags selbst (welche Flags es gibt und ihr Status) könnte Teil der administrativen Funktionen des Control Plane UI werden (Metakonfiguration).
<!-- TODO: translate -->
- Externe kommerzielle Systeme (LaunchDarkly, Flagsmith, etc.) werden derzeit nicht als notwendig erachtet, können aber bei steigender Komplexität in Zukunft evaluiert werden.

- **Accessing Flags in Code:**

  - Ein benutzerdefinierter React Hook und/oder ein Context Provider wird erstellt, um den Zugriff auf die Feature Flags zu kapseln.
  - **Beispiel (`src/hooks/useFeatureFlag.ts` und `src/store/contexts/FeatureFlagContext.tsx`):**

        ```typescript
        // src/store/contexts/FeatureFlagContext.tsx
        import React, { createContext, useContext, useEffect, useState, useMemo } from 'react';
<!-- TODO: translate -->
        // Annahme: apiClient ist für API-Aufrufe verfügbar
        // import { apiClient } from '../../providers/httpClient'; // Pfad anpassen

        interface FeatureFlags {
          [key: string]: boolean;
        }

        interface FeatureFlagContextType {
          flags: FeatureFlags;
          isLoading: boolean;
          error: Error | null;
          isFeatureEnabled: (flagName: string) => boolean;
        }

        const FeatureFlagContext = createContext<FeatureFlagContextType | undefined>(undefined);

        export const FeatureFlagProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
          const [flags, setFlags] = useState<FeatureFlags>({});
          const [isLoading, setIsLoading] = useState(true);
          const [error, setError] = useState<Error | null>(null);

          useEffect(() => {
            const fetchFlags = async () => {
              setIsLoading(true);
              try {
                // const response = await apiClient.get<FeatureFlags>('/ui-features'); // Pfad anpassen
                // setFlags(response.data);
<!-- TODO: translate -->
                // Dummy-Daten für das Beispiel:
                setFlags({
                  newTenantDashboard: true,
                  betaUserImport: false,
                });
                setError(null);
              } catch (err) {
                setError(err as Error);
                console.error('Failed to fetch feature flags', err);
                // Fallback zu Default-Flags oder leeren Flags
                setFlags({});
              } finally {
                setIsLoading(false);
              }
            };
            fetchFlags();
          }, []);

          const isFeatureEnabled = (flagName: string): boolean => {
<!-- TODO: translate -->
            return !!flags[flagName]; // Gibt false zurück, wenn der Flag nicht existiert oder false ist
          };
          
          const value = useMemo(() => ({ flags, isLoading, error, isFeatureEnabled }), [flags, isLoading, error]);

          return (
            <FeatureFlagContext.Provider value={value}>
              {children}
            </FeatureFlagContext.Provider>
          );
        };

        export const useFeatureFlag = (): ((flagName: string) => boolean) => {
          const context = useContext(FeatureFlagContext);
          if (context === undefined) {
            throw new Error('useFeatureFlag must be used within a FeatureFlagProvider');
          }
          return context.isFeatureEnabled;
        };

        export const useFeatureFlags = (): { flags: FeatureFlags; isLoading: boolean; error: Error | null } => {
          const context = useContext(FeatureFlagContext);
          if (context === undefined) {
            throw new Error('useFeatureFlags must be used within a FeatureFlagProvider');
          }
          return { flags: context.flags, isLoading: context.isLoading, error: context.error };
        };
        ```

<!-- TODO: translate -->
        Der `FeatureFlagProvider` würde die `App`-Komponente umschließen. Im Code wird dann `const isNewDashboardEnabled = useFeatureFlag()('newTenantDashboard');` verwendet.

- **Flag Naming Convention:**

<!-- TODO: translate -->
- Flags SOLLTEN prägnant und klar benannt werden, typischerweise in camelCase oder PascalCase, wenn sie als Schlüssel in einem Objekt verwendet werden.
- Beispiele: `newTenantDashboard`, `enableExperimentalReporting`, `useV2UserProfilePage`.
<!-- TODO: translate -->
- Ein Präfix wie `feature_` oder `flag_` ist optional, kann aber zur Verdeutlichung beitragen (z.B. `feature_newTenantDashboard`).

- **Code Structure for Flagged Features:**

<!-- TODO: translate -->
- **Bedingtes Rendern:** Der häufigste Anwendungsfall ist das bedingte Rendern von Komponenten oder UI-Elementen:

        ```tsx
        const isNewDashboardEnabled = useFeatureFlag()('newTenantDashboard');
        // ...
        {isNewDashboardEnabled ? <NewDashboardComponent /> : <OldDashboardComponent />}
        ```

<!-- TODO: translate -->
- **Bedingte Routen:** Ganze Routen können bedingt registriert werden.
- **Bedingte Logik:** In Funktionen oder Hooks kann Logik basierend auf Flags verzweigen.
<!-- TODO: translate -->
- **Vermeidung von Komplexität:** Es ist darauf zu achten, dass Feature Flags nicht zu einer übermäßigen Verzweigung und Komplexität im Code führen. Flags sollten an möglichst hohen Stellen in der Komponenten- oder Logikhierarchie geprüft werden.

- **Strategy for Code Cleanup (Post-Flag Retirement):**

<!-- TODO: translate -->
- **Mandat:** Sobald ein Feature Flag vollständig ausgerollt (100% der Zielgruppe) und als permanent betrachtet wird oder das Feature komplett entfernt wird, MÜSSEN der Flag selbst, die gesamte bedingte Logik und alle alten/nicht mehr genutzten Codepfade innerhalb von {z.B. 2-3 Sprints} aus der Codebasis entfernt werden.
<!-- TODO: translate -->
- Dies ist ein wichtiger Aspekt zur Vermeidung von technischen Schulden. Für jeden eingeführten Flag SOLLTE ein Ticket oder eine Aufgabe zur späteren Bereinigung erstellt werden.

- **Testing Flagged Features:**

<!-- TODO: translate -->
- **Manuelle Tests:** QA und Entwickler MÜSSEN die Möglichkeit haben, verschiedene Flag-Konfigurationen zu testen. Dies kann durch lokale Konfigurationsüberschreibungen oder eine Debug-UI geschehen, die das Umschalten von Flags im Browser ermöglicht (falls die Flags clientseitig flexibel sind).
<!-- TODO: translate -->
- **Automatisierte Tests (E2E):** Playwright-Tests SOLLTEN so konzipiert werden, dass sie mit verschiedenen Flag-Konfigurationen ausgeführt werden können, um beide Codepfade (Feature aktiviert/deaktiviert) abzudecken. Dies kann durch Setzen von Mocks für den Feature-Flag-Service oder durch Parameterisierung der Testläufe erreicht werden.

## Frontend Security Considerations

<!-- TODO: translate -->
Die Sicherheit der Frontend-Anwendung ist von entscheidender Bedeutung, um Benutzerdaten zu schützen und Angriffe abzuwehren. Dieser Abschnitt beschreibt verbindliche frontend-spezifische Sicherheitspraktiken, die das Hauptarchitekturdokument ergänzen. KI-Agenten und Entwickler MÜSSEN diese Richtlinien befolgen.

- **Cross-Site Scripting (XSS) Prevention:**

<!-- TODO: translate -->
- **Framework Reliance:** React rendert standardmäßig Daten als Text und nicht als HTML, was einen grundlegenden Schutz vor XSS bietet, wenn JSX korrekt verwendet wird. Die direkte Manipulation des DOM (z.B. über `dangerouslySetInnerHTML`) MUSS vermieden werden. Wenn es absolut unvermeidlich ist, MUSS der Inhalt vorher explizit serverseitig oder clientseitig mit einer etablierten Bibliothek wie DOMPurify (konfiguriert für strenge Filterung) bereinigt werden.
- **React-Admin:** React-Admin und die zugrundeliegenden UI-Bibliotheken (wie Material UI) sind darauf ausgelegt, XSS-Risiken zu minimieren, wenn sie wie vorgesehen verwendet werden.
<!-- TODO: translate -->
- **Content Security Policy (CSP):** Eine strenge Content Security Policy MUSS serverseitig (über HTTP-Header) implementiert werden, wie im Hauptarchitekturdokument spezifiziert. Das Frontend muss sicherstellen, dass es konform mit dieser CSP ist (z.B. keine Inline-Skripte oder -Styles ohne Nonce/Hash, Einschränkung von `script-src`, `style-src`, `connect-src` auf vertrauenswürdige Quellen).

- **Cross-Site Request Forgery (CSRF) Protection:**

<!-- TODO: translate -->
- **Mechanismus:** Da die Authentifizierung über Tokens (JWTs) im `Authorization`-Header erfolgt und nicht über session-basierte Cookies für API-Anfragen, ist das traditionelle CSRF-Risiko für API-Calls reduziert.
<!-- TODO: translate -->
- Dennoch MÜSSEN alle zustandsändernden Anfragen (POST, PUT, DELETE) sicherstellen, dass sie nicht durch Cross-Site-Scripting auf anderen Websites ausgelöst werden können, die solche Anfragen an die EAF-API initiieren. Der `Authorization`-Header-Ansatz ist hier bereits ein starker Schutz.
<!-- TODO: translate -->
- Das Backend (gemäß Hauptarchitekturdokument) SOLLTE zusätzlich Mechanismen wie SameSite-Cookie-Attribute (falls Cookies für andere Zwecke verwendet werden) und ggf. die Überprüfung des `Origin`- oder `Referer`-Headers für kritische Operationen implementieren.

- **Secure Token Storage & Handling (JWTs):**

<!-- TODO: translate -->
- **Storage Mechanism:** JWTs (Access Tokens) MÜSSEN sicher im Frontend gespeichert werden.
<!-- TODO: translate -->
    - **Bevorzugte Methode:** Speicherung im Speicher der JavaScript-Anwendung (z.B. in einem React Context oder Zustand-Store). Tokens sind dann nur für die Dauer der Browsersitzung (Tab/Fenster) gültig und nicht anfällig für XSS-Angriffe, die auf `localStorage` oder `sessionStorage` abzielen.
<!-- TODO: translate -->
    - `HttpOnly` Cookies für Refresh Tokens: Wenn ein Refresh-Token-Mechanismus verwendet wird, SOLLTE das Refresh Token in einem `HttpOnly`, `Secure`, `SameSite=Strict` (oder `Lax`) Cookie gespeichert werden, das vom Backend gesetzt wird. Das Frontend greift nicht direkt darauf zu. Der Access Token wird dann im Speicher gehalten.
<!-- TODO: translate -->
    - `localStorage` und `sessionStorage` DÜRFEN NICHT für die Speicherung von JWTs verwendet werden, da sie anfällig für XSS-Angriffe sind.
<!-- TODO: translate -->
- **Token Refresh:** Der `authProvider` und der `httpClient` (wie im API Interaction Layer beschrieben) MÜSSEN die Logik zum automatischen Aktualisieren von Access Tokens mittels Refresh Tokens (falls implementiert) sicher handhaben.

- **Third-Party Script Security:**

<!-- TODO: translate -->
- **Policy:** Die Einbindung von Skripten von Drittanbietern (z.B. für Analytics, Monitoring, falls benötigt) MUSS auf ein absolutes Minimum beschränkt und jede Quelle sorgfältig geprüft werden.
<!-- TODO: translate -->
- **Subresource Integrity (SRI):** Für alle externen Skripte und Stylesheets, die von CDNs geladen werden, MÜSSEN Subresource Integrity (SRI) Hashes verwendet werden, falls verfügbar. Dies stellt sicher, dass die geladenen Dateien nicht manipuliert wurden.

        ```html
        <script src="https{://example.com/library.js}"
                integrity="sha384-oqVuAfXRKap7fdgcCY5uykM6+R9GqQ8K/uxy9rx7HNQlGYl1kPzQho1wx4JwY8wC"
                crossorigin="anonymous"></script>
        ```

<!-- TODO: translate -->
- Skripte von Drittanbietern SOLLTEN, wenn möglich, asynchron (`async`/`defer`) geladen werden, um das Blockieren des Hauptthreads zu minimieren.

- **Client-Side Data Validation:**

<!-- TODO: translate -->
- **Purpose:** Clientseitige Validierung dient primär der Verbesserung der User Experience durch sofortiges Feedback. Sie ist KEIN Ersatz für serverseitige Validierung.
<!-- TODO: translate -->
- **Mandat:** Alle kritischen Datenvalidierungen MÜSSEN serverseitig in der `eaf-controlplane-api` erfolgen (gemäß Hauptarchitekturdokument).
<!-- TODO: translate -->
- **Implementation:** React-Admin Formulare bieten Validierungs-Props. Diese SOLLTEN genutzt werden, um grundlegende Prüfungen (z.B. Pflichtfelder, Formatprüfungen) clientseitig durchzuführen. Die Validierungsregeln sollten, wo sinnvoll, die serverseitigen Regeln widerspiegeln.

- **Preventing Clickjacking:**

<!-- TODO: translate -->
- **Mechanismus:** Die primäre Verteidigung gegen Clickjacking ist der `X-Frame-Options` HTTP-Header (z.B. `DENY` oder `SAMEORIGIN`) oder die `frame-ancestors` Direktive in der Content Security Policy. Diese Header MÜSSEN serverseitig gesetzt werden (gemäß Hauptarchitekturdokument). Das Frontend sollte sich nicht auf Frame-Busting-Skripte verlassen.

<!-- TODO: translate -->
- **API Key Exposure (für client-seitig konsumierte Dienste):**

<!-- TODO: translate -->
- **Restriction:** API-Schlüssel, die ausschließlich clientseitig verwendet werden (z.B. für einen Kartendienst, falls relevant), MÜSSEN so weit wie möglich über die Konsole des Dienstanbieters eingeschränkt werden (z.B. durch HTTP-Referrer-Beschränkungen, IP-Adressfilter oder API-spezifische Zugriffsbeschränkungen).
<!-- TODO: translate -->
- **Backend Proxy:** Für API-Schlüssel, die mehr Geheimhaltung erfordern oder sensible Operationen steuern, MUSS ein Backend-Proxy-Endpunkt in der `eaf-controlplane-api` erstellt werden. Das Frontend ruft den Proxy auf, nicht direkt den Drittanbieterdienst.

- **Secure Communication (HTTPS):**

<!-- TODO: translate -->
- **Mandat:** Die gesamte Kommunikation zwischen dem Frontend und der `eaf-controlplane-api` sowie allen anderen externen Diensten MUSS über HTTPS erfolgen. Mixed Content (HTTP-Assets auf einer HTTPS-Seite) ist verboten und wird durch die CSP verhindert.

- **Dependency Vulnerabilities:**

<!-- TODO: translate -->
- **Process:** `npm audit` (oder `yarn audit`) MUSS regelmäßig und als Teil der CI-Pipeline ausgeführt werden, um bekannte Schwachstellen in den Projekt-Abhängigkeiten zu identifizieren.
<!-- TODO: translate -->
- Schwachstellen mit hohem oder kritischem Schweregrad MÜSSEN vor einem Deployment behoben werden, entweder durch Update der Abhängigkeit oder durch Anwendung empfohlener Workarounds.
<!-- TODO: translate -->
- Tools wie Dependabot (GitHub) oder Snyk KÖNNEN zur automatischen Überwachung und Benachrichtigung über neue Schwachstellen eingesetzt werden.

- **Security Headers:**

  - Neben CSP und `X-Frame-Options` SOLLTEN weitere sicherheitsrelevante HTTP-Header serverseitig konfiguriert werden, wie z.B. `Strict-Transport-Security` (HSTS), `X-Content-Type-Options`, `Referrer-Policy`. Die Frontend-Architektur muss mit diesen Headern kompatibel sein.

## Browser Support and Progressive Enhancement

<!-- TODO: translate -->
Dieser Abschnitt definiert die Zielbrowser für die ACCI EAF Control Plane UI und beschreibt, wie sich die Anwendung in weniger leistungsfähigen oder nicht standardkonformen Umgebungen verhalten soll.

- **Target Browsers:**

<!-- TODO: translate -->
- Die ACCI EAF Control Plane UI MUSS auf den **aktuellsten zwei stabilen Versionen** der folgenden Desktop-Browser voll funktionsfähig sein und korrekt dargestellt werden:
  - Google Chrome
  - Mozilla Firefox
  - Microsoft Edge
  - Apple Safari (macOS)
<!-- TODO: translate -->
- **Internet Explorer (IE) in jeglicher Version wird NICHT unterstützt.**
<!-- TODO: translate -->
- Obwohl das PRD eine "Desktop-First"-Ausrichtung vorgibt, ist es wünschenswert, dass die Anwendung auf modernen Tablet-Browsern (aktuelle Versionen von Safari auf iPadOS und Chrome auf Android) ebenfalls gut nutzbar ist. Eine vollständige Optimierung für mobile Endgeräte ist initial nicht der primäre Fokus, aber grundlegende Responsiveness für kleinere Viewports (wie sie z.B. durch Verkleinern von Desktop-Fenstern entstehen) sollte gegeben sein.

- **Polyfill Strategy:**

<!-- TODO: translate -->
- **Mechanismus:** Vite in Verbindung mit Babel (typischerweise über `@vitejs/plugin-react`, das Babel nutzt) und PostCSS (für CSS-Präfixe) wird verwendet, um die Kompatibilität mit den Zielbrowsern sicherzustellen.
<!-- TODO: translate -->
    - `@babel/preset-env` (oder eine ähnliche Konfiguration in Vite) wird so konfiguriert, dass es auf die oben definierte Browser-Support-Matrix abzielt und notwendige JavaScript-Polyfills für ECMAScript-Features bereitstellt, die von diesen Browsern möglicherweise noch nicht vollständig unterstützt werden. Dies geschieht oft durch automatische Injektion von Polyfills aus `core-js`.
<!-- TODO: translate -->
- **Specific Polyfills:** Über die automatischen Polyfills von `core-js` hinaus werden derzeit keine spezifischen zusätzlichen Polyfills erwartet. Sollten im Laufe der Entwicklung Features implementiert werden, die auf sehr neuen Browser-APIs basieren, die nicht von `core-js` abgedeckt werden und in den Zielbrowsern fehlen, wird die Notwendigkeit spezifischer Polyfills (z.B. für `IntersectionObserver` falls nicht überall vorhanden, oder spezifische `Intl`-Features) pro Fall evaluiert und dokumentiert.

- **JavaScript Requirement & Progressive Enhancement:**

<!-- TODO: translate -->
- **Baseline:** Die Kernfunktionalität der ACCI EAF Control Plane UI **ERFORDERT aktiviertes JavaScript** im Browser. Es handelt sich um eine Single Page Application (SPA), die stark auf JavaScript für Rendering, Logik und API-Interaktionen angewiesen ist.
<!-- TODO: translate -->
- **No-JS Experience:** Es wird keine spezifische No-JS-Fallbacksicht oder -Funktionalität bereitgestellt, außer einer einfachen Meldung (z.B. via `<noscript>`-Tag in `index.html`), die den Benutzer darauf hinweist, JavaScript zu aktivieren.

        ```html
        <noscript>
<!-- TODO: translate -->
          Diese Anwendung benötigt JavaScript, um korrekt zu funktionieren. Bitte aktivieren Sie JavaScript in Ihren Browsereinstellungen.
        </noscript>
        ```

<!-- TODO: translate -->
- Obwohl Progressive Enhancement ein wünschenswertes Prinzip ist, liegt der Fokus für diese komplexe Administrationsanwendung auf einer reichen, interaktiven Benutzererfahrung, die JavaScript voraussetzt.

- **CSS Compatibility & Fallbacks:**

<!-- TODO: translate -->
- **Tooling:** PostCSS mit `autoprefixer` MUSS verwendet werden (typischerweise standardmäßig in Vite-Projekten konfiguriert), um automatisch Vendor-Präfixe für CSS-Eigenschaften hinzuzufügen, die dies für die Zielbrowser-Matrix noch benötigen. Die Browserliste für Autoprefixer wird aus den "Target Browsers" abgeleitet.
- **Feature Usage:**
<!-- TODO: translate -->
    - Moderne CSS-Features (z.B. Flexbox, Grid, Custom Properties) können und sollen verwendet werden, da sie von den Zielbrowsern gut unterstützt werden.
<!-- TODO: translate -->
    - CSS-Features, die in einem Teil der Zielbrowser-Matrix nicht unterstützt werden, DÜRFEN nur verwendet werden, wenn ein akzeptables "Graceful Degradation"-Verhalten (d.h. die Seite bleibt benutzbar und verständlich, auch wenn das Feature nicht greift) sichergestellt ist oder ein expliziter Fallback (z.B. via `@supports`-Queries) implementiert wird. Der Einsatz solcher Features ist im Team abzustimmen.

- **Accessibility Fallbacks:**

<!-- TODO: translate -->
- Wie im Abschnitt "Accessibility (AX) Implementation Details" beschrieben, ist die Verwendung von ARIA-Attributen wichtig. Es wird davon ausgegangen, dass die Zielbrowser und die von den Benutzern verwendete assistive Technologie moderne ARIA-Versionen unterstützen. Für sehr alte assistive Technologien, die möglicherweise nicht alle ARIA 1.1/1.2 Features unterstützen, wird das grundlegende semantische HTML weiterhin eine Basis-Zugänglichkeit bieten.

## Change Log

<!-- TODO: translate -->
Dieses Änderungsprotokoll dokumentiert die wesentlichen Änderungen und Weiterentwicklungen an diesem Frontend-Architekturdokument. Es dient der Nachvollziehbarkeit von Entscheidungen und dem Fortschritt.

| Change                                     | Date          | Version | Description                                                                                                | Author           |
| :----------------------------------------- | :------------ | :------ | :--------------------------------------------------------------------------------------------------------- | :--------------- |
<!-- TODO: translate -->
| Initial Draft of Frontend Architecture Doc | 16. Mai 2025  | 0.1.0   | Erste vollständige Ausarbeitung des Frontend-Architekturdokuments basierend auf dem Hauptarchitekturdokument, dem PRD und dem initialen Prompt. | Design Architect |
<!-- TODO: translate -->
| {Weitere Änderungen hier eintragen}        | {Datum}       | {Vers.} | {Beschreibung der Änderung}                                                                              | {Autor}          |
