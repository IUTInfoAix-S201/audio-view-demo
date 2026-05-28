# VigieChiro Audio View - Démo

Petite application JavaFX 25 qui **consomme le composant [`audio-view`](https://github.com/IUTInfoAix-S201/audio-view)**
(sonogramme + spectrogramme) récupéré comme dépendance Maven via [JitPack](https://jitpack.io).

Elle sert de démonstration et d'exemple d'intégration pour la SAE 2.01 : on insère le composant en
FXML, on lui donne un fichier WAV, et on observe ses propriétés. Le calcul FFT, le rendu et la
lecture audio restent internes au composant (boîte noire).

## Lancer la démo

```bash
mvn javafx:run
```

Au premier lancement, Maven télécharge `audio-view` depuis JitPack. Cliquez sur **« Ouvrir un
fichier WAV... »** pour charger une séquence : le sonogramme et le spectrogramme s'affichent, la
barre d'outils du composant permet la lecture et les zooms temps / fréquence.

> Sans écran (Linux headless, CI), lancez derrière un serveur X virtuel :
> `xvfb-run -a mvn javafx:run`.

## Comment l'intégration fonctionne

- **Dépendance** (`pom.xml`) : dépôt `jitpack.io` + `com.github.IUTInfoAix-S201:audio-view:v1.0.0`.
  Le `groupId` est imposé par JitPack (`com.github.<organisation>`), l'`artifactId` est le nom du
  dépôt.
- **Insertion FXML** (`DemoView.fxml`) : `<AudioView fx:id="audioView"/>` après l'import
  `<?import fr.iutaix.vigiechiro.audio.AudioView?>`.
- **Câblage** (`DemoController.java`) : `audioView.setAudioFile(...)` pour la source, et écoute de
  `currentTimeProperty()` / `durationProperty()` pour se synchroniser avec le reste de l'IHM.

## Pile technique

Java 25, JavaFX 25 via `javafx-maven-plugin` (gère les natifs par OS), `audio-view` via JitPack.
