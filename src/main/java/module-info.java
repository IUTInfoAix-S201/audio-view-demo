/**
 * Application d'exemple consommant le composant {@code audio-view}.
 *
 * <p>Module ouvert (open) pour que JavaFX FXML puisse injecter le contrôleur par réflexion sans
 * déclarer d'{@code opens} paquet par paquet.
 */
open module fr.iutaix.vigiechiro.demo {
  requires javafx.controls;
  requires javafx.fxml;
  requires fr.iutaix.vigiechiro.audio;
}
