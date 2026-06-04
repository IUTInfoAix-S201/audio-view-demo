package fr.nedjar.vigiechiro.demo;

import fr.nedjar.vigiechiro.audio.AudioView;
import java.io.File;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.FileChooser;

/**
 * Contrôleur de la démonstration. Il câble le bouton d'ouverture de fichier et observe les
 * propriétés publiques d'{@link AudioView} pour alimenter la barre d'état : c'est tout ce qu'un
 * consommateur a besoin de connaître du composant (le reste est une boîte noire).
 */
public class DemoController {

  @FXML private AudioView audioView;
  @FXML private Label fichierLabel;
  @FXML private Label etatLabel;
  @FXML private Button themeBouton;
  @FXML private CheckBox normaliserCase;
  @FXML private CheckBox boucleCase;
  @FXML private CheckBox daltonienCase;
  @FXML private Label gainLabel;

  @FXML
  private void initialize() {
    // Les WAV VigieChiro sont ralentis x10 : on demande au composant d'afficher les unites reelles
    // (frequence x10, temps /10) sur ses axes.
    audioView.setTimeExpansionFactor(10);

    // Démonstration de l'API observable : la barre d'état reflète les propriétés du composant.
    audioView.currentTimeProperty().addListener((obs, ancien, nouveau) -> majEtat());
    audioView.durationProperty().addListener((obs, ancien, nouveau) -> majEtat());
    audioView.playingProperty().addListener((obs, ancien, nouveau) -> majEtat());
    audioView.audioFileProperty().addListener((obs, ancien, nouveau) -> majEtat());

    // Préférences observables d'AudioView exposées en cases à cocher : liaison bidirectionnelle
    // (selectedProperty <-> BooleanProperty du composant). Illustre l'API publique récente :
    // normalisation peak du volume à la lecture (#32), lecture en boucle, palette
    // daltonien-friendly.
    normaliserCase.selectedProperty().bindBidirectional(audioView.normalisationProperty());
    boucleCase.selectedProperty().bindBidirectional(audioView.loopProperty());
    daltonienCase.selectedProperty().bindBidirectional(audioView.colorblindFriendlyProperty());

    // Retour visuel de la normalisation peak : affiche le gain réellement appliqué (en dB), sinon
    // rien. La normalisation agit sur l'audio (audible), pas sur les tracés.
    gainLabel
        .textProperty()
        .bind(
            Bindings.createStringBinding(
                () ->
                    audioView.isNormalisation()
                        ? String.format("Normalisé : %+.1f dB", audioView.getNormalisationGainDb())
                        : "",
                audioView.normalisationProperty(),
                audioView.normalisationGainDbProperty()));

    majEtat();
  }

  @FXML
  private void basculerTheme() {
    boolean clair = !audioView.isLightTheme();
    audioView.setLightTheme(clair);
    themeBouton.setText(clair ? "Thème sombre" : "Thème clair");
  }

  @FXML
  private void ouvrir() {
    FileChooser chooser = new FileChooser();
    chooser.setTitle("Choisir un fichier WAV");
    chooser
        .getExtensionFilters()
        .add(new FileChooser.ExtensionFilter("Fichiers WAV", "*.wav", "*.WAV"));
    File fichier = chooser.showOpenDialog(audioView.getScene().getWindow());
    if (fichier != null) {
      charger(fichier);
    }
  }

  // Glisser-deposer : alternative robuste au dialogue natif (aucun dialogue GTK,
  // donc immunise contre le gel intermittent du FileChooser sous Wayland).
  @FXML
  private void surGlisser(DragEvent e) {
    if (e.getDragboard().hasFiles()) {
      e.acceptTransferModes(TransferMode.COPY);
    }
    e.consume();
  }

  @FXML
  private void surDepot(DragEvent e) {
    Dragboard presse = e.getDragboard();
    boolean ok = presse.hasFiles() && !presse.getFiles().isEmpty();
    if (ok) {
      charger(presse.getFiles().get(0));
    }
    e.setDropCompleted(ok);
    e.consume();
  }

  private void charger(File fichier) {
    audioView.setAudioFile(fichier.toPath());
    fichierLabel.setText(fichier.getName());
  }

  private void majEtat() {
    if (audioView.getAudioFile() == null) {
      etatLabel.setText("Aucun fichier chargé");
      return;
    }
    double f = audioView.getTimeExpansionFactor();
    String lecture = audioView.isPlaying() ? "Lecture en cours" : "En pause";
    etatLabel.setText(
        String.format(
            "%s - %.2f / %.2f s",
            lecture, audioView.getCurrentTime() / f, audioView.getDuration() / f));
  }
}
