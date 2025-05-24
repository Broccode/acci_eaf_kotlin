# ACCI EAF Control Plane UI

Eine React-basierte Benutzeroberfläche für die Administration der ACCI EAF (Enterprise Application Framework) Control Plane.

## Übersicht

Diese Anwendung bietet eine webbasierte Administrationsoberfläche für die Verwaltung von:

- Tenants und Multi-Tenancy
- Benutzern und Rollen (RBAC)
- Service Accounts
- Lizenzen und Systemkonfiguration

## Tech Stack

- **Framework**: React 19.1 mit TypeScript
- **Build Tool**: Vite 6.3.5
- **UI Library**: React-Admin 5.8.1 mit Material-UI
- **Styling**: Material-UI Components und Emotion
- **Linting**: ESLint mit TypeScript Support
- **Formatting**: Prettier

## Getting Started

### Voraussetzungen

- Node.js 18+
- npm oder yarn

### Installation

```bash
cd controlplane-ui
npm install
```

### Entwicklung

```bash
# Entwicklungsserver starten
npm run dev

# Linting ausführen
npm run lint

# Automatisches Linting mit Fixes
npm run lint:fix

# Code formatieren
npm run format

# Format-Check ohne Änderungen
npm run format:check
```

### Build

```bash
# Production Build erstellen
npm run build

# Build Preview
npm run preview
```

## Projektstruktur

```
src/
├── components/          # Wiederverwendbare Komponenten
│   ├── ErrorBoundary.tsx
│   └── index.ts
├── pages/              # Seiten-Komponenten
│   ├── Dashboard.tsx
│   └── LoginPage.tsx
├── services/           # API Services und Provider
│   ├── authProvider.ts
│   └── dataProvider.ts
├── types/              # TypeScript Type-Definitionen
│   └── index.ts
├── utils/              # Utility-Funktionen
├── App.tsx             # Haupt-App-Komponente
├── main.tsx           # React DOM Entry Point
└── index.css          # Globale Styles
```

## Features

### Authentifizierung

- Sichere Login-Seite mit Benutzername/Passwort
- JWT-Token basierte Session-Verwaltung
- Persistente Anmeldung über Page Reloads
- Automatic Logout bei invaliden/abgelaufenen Tokens

### Dashboard

- Übersichtliche Darstellung von System-Metriken
- Responsive Design für Desktop und Tablet
- Aktivitäts-Log und Systemstatus

### Sicherheit

- CSRF-Schutz implementiert
- XSS-Prävention durch sichere Token-Speicherung
- Client-seitige Eingabevalidierung
- Rate-Limiting bei fehlgeschlagenen Login-Versuchen

### Error Handling

- Globales Error Boundary für unbehandelte Exceptions
- Benutzerfreundliche Fehlermeldungen
- Development/Production unterschiedliche Error-Details

## Konfiguration

### Umgebungsvariablen

Die Anwendung unterstützt folgende Umgebungsvariablen:

```bash
# Backend API URL (Standard: http://localhost:8080)
VITE_API_BASE_URL=http://localhost:8080

# Environment (development/production)
NODE_ENV=development
```

### API-Integration

Die Anwendung kommuniziert mit folgenden Backend-Services:

- **Authentication API**: `/api/auth/login` (Story 3.3)
- **Tenant Management**: `/api/tenants/*` (zukünftige Stories)
- **User Management**: `/api/users/*` (zukünftige Stories)

## Demo-Credentials

Für die MVP-Phase sind folgende Demo-Zugangsdaten konfiguriert:

- **Benutzername**: `admin`
- **Passwort**: `admin123`

## Responsive Design

Die Anwendung ist optimiert für:

- **Desktop**: Chrome, Firefox, Edge (aktuelle Versionen)
- **Tablet**: Grundlegende Funktionalität ohne kritische Anzeigefehler
- **Mobile**: Minimale Unterstützung (nicht primärer Fokus in MVP)

## Browser-Unterstützung

- Chrome 100+
- Firefox 100+
- Safari 15+
- Edge 100+

## Lizenz

ACCI Proprietary - Alle Rechte vorbehalten

## Contributing

Siehe Projekt-Dokumentation in `docs/` für Coding Standards und Contribution Guidelines.

## Support

Bei Fragen oder Problemen wenden Sie sich an das ACCI EAF-Team.
