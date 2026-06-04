#!/usr/bin/env bash
# Régénère l'aperçu du README d'audio-view par rendu HORS-ÉCRAN de l'AudioView.
#
# S'appuie sur fr.nedjar.vigiechiro.demo.CaptureApercu (profil Maven "capture") : on charge un WAV,
# on attend le signal `ready` du composant, puis on écrit un PNG (encodeur maison, sans dépendance
# supplémentaire). JavaFX 25 n'ayant pas de plateforme Headless, on passe par xvfb-run — aucun écran
# requis (CI, conteneur, session Wayland).
#
# Prérequis : JDK 25 (le java par défaut), Maven, xvfb (paquet xvfb / xorg-x11-server-Xvfb).
#
# Usage :
#   scripts/capture-apercu.sh <chemin-WAV> [sortie.png]
#
# Par défaut, écrit dans ../audio-view/docs/apercu.png (audio-view en dépôt voisin) — c'est l'image
# référencée par le README du composant.
set -euo pipefail

RACINE="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$RACINE"

WAV="${1:?Usage: scripts/capture-apercu.sh <chemin-WAV> [sortie.png]}"
OUT="${2:-../audio-view/docs/apercu.png}"

WAV_ABS="$(realpath "$WAV")"
OUT_ABS="$(realpath -m "$OUT")"
mkdir -p "$(dirname "$OUT_ABS")"

echo "Rendu hors-écran de $WAV_ABS → $OUT_ABS"
xvfb-run -a mvn -q -Pcapture javafx:run \
  -Dcapture.wav="$WAV_ABS" \
  -Dcapture.out="$OUT_ABS"

echo "Aperçu écrit : $OUT_ABS"
