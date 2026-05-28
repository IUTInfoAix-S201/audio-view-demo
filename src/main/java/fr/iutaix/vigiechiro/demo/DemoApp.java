package fr.iutaix.vigiechiro.demo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Point d'entrée de la démonstration : charge la vue FXML qui insère le composant {@code AudioView}
 * (récupéré comme dépendance JitPack) et l'affiche dans une fenêtre.
 *
 * <p>Lancement : {@code mvn javafx:run}.
 */
public class DemoApp extends Application {

  @Override
  public void start(Stage stage) throws Exception {
    Parent racine = FXMLLoader.load(getClass().getResource("DemoView.fxml"));
    stage.setTitle("Démo VigieChiro - AudioView (sonogramme + spectrogramme)");
    stage.setScene(new Scene(racine, 900, 600));
    stage.show();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
