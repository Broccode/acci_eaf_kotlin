#!/usr/bin/env bash
# translate-pass-1.sh: First-pass translation helper for docs/ACCI-EAF-Frontend.md
set -euo pipefail

DOC="docs/ACCI-EAF-Frontend.md"
TMP="${DOC}.tmp"
cp "$DOC" "$TMP"

# 1. Replace common German modal words with English equivalents
#   - MUST, SHOULD, CAN, e.g.
sed -i.bak \
  -e 's/\bMUSS\b/MUST/g' \
  -e 's/\bSOLLTEN\b/SHOULD/g' \
  -e 's/\bKann\b/CAN/gI' \
  -e 's/\bz\\.B\\./e.g./g' \
"$TMP"

# 2. Insert TODO marker above any line containing German-specific characters (heuristic: umlauts or ß)
# Using BSD sed: insert requires a backslash and newline after the 'i' and '-i' needs an empty suffix
sed -i '' \
  -e '/[äöüßÄÖÜ]/i\
<!-- TODO: translate -->' \
"$TMP"

# Overwrite original
mv "$TMP" "$DOC"
echo "translate-pass-1 applied to $DOC (backup at ${DOC}.tmp.bak)" 