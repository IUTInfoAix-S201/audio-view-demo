package fr.iutaix.vigiechiro.demo;

import fr.iutaix.vigiechiro.audio.AudioView;
import java.io.File;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;

/**
 * Contrôleur de la démonstration. Il câble le bouton d'ouverture de fichier et observe les
 * propriétés publiques d'{@link AudioView} pour mettre à jour l'affichage du temps : c'est tout ce
 * qu'un consommateur a besoin de connaître du composant (le reste est une boîte noire).
 */
public class DemoController {

  @FXML private AudioView audioView;
  @FXML private Label fichierLabel;
  @FXML private Label tempsLabel;

  @FXML
  private void initialize() {
    // Démonstration de l'API observable : on réagit aux propriétés exposées par le composant.
    audioView.currentTimeProperty().addListener((obs, ancien, nouveau) -> majTemps());
    audioView.durationProperty().addListener((obs, ancien, nouveau) -> majTemps());
    majTemps();
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
      audioView.setAudioFile(fichier.toPath());
      fichierLabel.setText(fichier.getName());
    }
  }

  private void majTemps() {
    tempsLabel.setText(
        String.format("%.2f / %.2f s", audioView.getCurrentTime(), audioView.getDuration()));
  }
}
