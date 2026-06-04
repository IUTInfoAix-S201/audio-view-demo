# VigieChiro Audio View - Démo

Petite application JavaFX 25 qui **consomme le composant [`audio-view`](https://github.com/IUTInfoAix-S201/audio-view)**
(sonogramme + spectrogramme) récupéré comme dépendance Maven depuis
[Maven Central](https://central.sonatype.com/artifact/fr.nedjar.vigiechiro/audio-view).

Elle sert de démonstration et d'exemple d'intégration pour la SAE 2.01 : on insère le composant en
FXML, on lui donne un fichier WAV, et on observe ses propriétés. Le calcul FFT, le rendu et la
lecture audio restent internes au composant (boîte noire).

## Lancer la démo

```bash
mvn javafx:run
```

Au premier lancement, Maven télécharge `audio-view` depuis Maven Central. Pour charger une séquence :

- **glissez‑déposez un fichier WAV sur la fenêtre** (voie recommandée), ou
- cliquez sur **« Ouvrir un fichier WAV... »**.

Le sonogramme et le spectrogramme s'affichent, la barre d'outils du composant permet la lecture et
les zooms temps / fréquence. Le bouton **« Thème clair »** (en haut à droite) bascule le composant
entre thème sombre et clair (démonstration de l'API `setLightTheme`).

> Sans écran (Linux headless, CI), lancez derrière un serveur X virtuel :
> `xvfb-run -a mvn javafx:run`.

> **Linux / Wayland — le bouton « Ouvrir » se fige ?** Le dialogue natif GTK du `FileChooser` gèle
> par intermittence (et le JVM reste coincé, bloquant les lancements suivants). C'est un souci connu
> de JavaFX sur Linux, indépendant du composant. Le **glisser‑déposer ci‑dessus n'utilise aucun
> dialogue GTK** : c'est le moyen fiable de charger un fichier. Si un lancement reste bloqué, tuez le
> JVM resté coincé : `pkill -9 -f 'fr.nedjar.vigiechiro.demo'`.

## Comment l'intégration fonctionne

- **Dépendance** (`pom.xml`) : `fr.nedjar.vigiechiro:audio-view:1.10.1` (propriété
  `audio.view.version`) résolue depuis Maven Central — plus de dépôt explicite à déclarer. Le
  composant étant en FXML, la démo apporte aussi le module `javafx-fxml`. La voie JitPack
  (`com.github.IUTInfoAix-S201:audio-view`) reste disponible pour les versions antérieures à 1.10.1.
- **Insertion FXML** (`DemoView.fxml`) : `<AudioView fx:id="audioView"/>` après l'import
  `<?import fr.nedjar.vigiechiro.audio.AudioView?>`.
- **Câblage** (`DemoController.java`) : `audioView.setAudioFile(...)` pour la source, écoute de
  `currentTimeProperty()` / `durationProperty()` pour se synchroniser, et `setLightTheme(...)` pour le
  bouton de thème.

## Pile technique

Java 25, JavaFX 25 via `javafx-maven-plugin` (gère les natifs par OS), `audio-view` via JitPack.
