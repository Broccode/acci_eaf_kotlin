# Code Formatting mit Spotless

Das ACCI EAF Projekt verwendet [Spotless](https://github.com/diffplug/spotless) zur automatischen Formatierung von Kotlin-Code und -Skripten.

## Integration

Spotless ist in den Gradle-Build integriert und verwendet [ktlint](https://github.com/pinterest/ktlint) als Formatierungsengine für Kotlin.

## Formatierungsregeln

Die Formatierungsrichtlinien sind in der [.editorconfig](./.editorconfig) Datei im Wurzelverzeichnis des Projekts definiert. Hier sind einige der wichtigsten Regeln:

- Einrückung: 4 Leerzeichen
- Maximale Zeilenlänge: 150 Zeichen
- Keine Wildcard-Imports (z.B. `import org.junit.jupiter.api.*`)
- Keine Kommentare innerhalb von Argumentlisten

## Verwendung

### Code formatieren

Um den gesamten Code zu formatieren:

```bash
./gradlew spotlessApply
```

Um nur den Code eines Untermoduls zu formatieren:

```bash
./gradlew :modul-name:spotlessApply
```

### Formatierungsprüfung

Um zu überprüfen, ob der Code korrekt formatiert ist:

```bash
./gradlew spotlessCheck
```

Diese Prüfung ist Teil des normalen `check`-Tasks und wird automatisch ausgeführt, wenn Sie `./gradlew check` oder `./gradlew build` ausführen.

## Häufige Probleme und Lösungen

### Wildcard-Imports

Wildcard-Imports (z.B. `import org.junit.jupiter.api.*`) sind in diesem Projekt nicht erlaubt. Ersetzen Sie sie durch explizite Imports für jede benötigte Klasse oder Funktion.

### Kommentare in Argumentlisten

Kommentare innerhalb von Argumentlisten sind nur erlaubt, wenn sie auf einer separaten Zeile stehen:

```kotlin
// Erlaubt
function(
    // Kommentar für Parameter
    parameter1,
    parameter2
)

// Nicht erlaubt
function(parameter1, /* Inline-Kommentar */ parameter2)
```

### Zeilenlänge überschritten

Die maximale Zeilenlänge beträgt 150 Zeichen. Lange Zeilen sollten umgebrochen werden:

```kotlin
// Zu lang
val result = veryLongFunctionName(veryLongParameter1, veryLongParameter2, veryLongParameter3, veryLongParameter4, veryLongParameter5)

// Besser
val result = veryLongFunctionName(
    veryLongParameter1, 
    veryLongParameter2, 
    veryLongParameter3, 
    veryLongParameter4, 
    veryLongParameter5
)
```

### Fehlerbehebung bei Ktlint-Problemen

Bei spezifischen Ktlint-Formattierungsproblemen hilft es manchmal, die problematische Regel in der `.editorconfig` zu konfigurieren oder zu deaktivieren. In diesem Projekt haben wir z.B. `ktlint_standard_multiline-expression-wrapping = disabled` gesetzt.

## CI-Pipeline

Die GitHub CI-Pipeline führt automatisch `spotlessCheck` aus und schlägt fehl, wenn der Code nicht korrekt formatiert ist. So wird sichergestellt, dass alle Code-Änderungen den Formatierungsrichtlinien entsprechen.

## IDE-Integration

Für die beste Entwicklungserfahrung empfehlen wir, die Ktlint-Plugins für Ihre IDE zu installieren:

- **IntelliJ IDEA**: Ktlint Plugin aus dem Marketplace installieren und konfigurieren
- **VS Code**: Ktlint Extension installieren

Diese Plugins formatieren den Code automatisch während der Bearbeitung, was die Notwendigkeit nachträglicher Korrekturen reduziert.
