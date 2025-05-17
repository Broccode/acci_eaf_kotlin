#!/usr/bin/env bash
# translate-pass-2.sh: Second-pass translation helper for docs/ACCI-EAF-Frontend.md
set -euo pipefail

DOC="docs/ACCI-EAF-Frontend.md"
TMP="${DOC}.tmp"
cp "$DOC" "$TMP"

# Replace specific German phrases with English equivalents
sed -i '' \
  -e 's/Statische Assets, die direkt vom Webserver ausgeliefert werden/Static assets served directly by the web server/g' \
  -e 's/Sprachdateien für i18n, wenn sie statisch geladen werden/Localization files for i18n when loaded statically/g' \
  -e 's/Haupt-Quellcode der Anwendung/Main application source code/g' \
  -e 's/Hauptkomponente der Anwendung; Setup von React-Admin, Router, Theme-Provider, etc\./Root component of the application; setup of React-Admin, Router, ThemeProvider, etc\./g' \
  -e 's/Einstiegspunkt der Anwendung; rendert die App-Komponente/Application entry point; renders the App component/g' \
"$TMP"

# Overwrite original
mv "$TMP" "$DOC"
echo "translate-pass-2 applied to $DOC" 