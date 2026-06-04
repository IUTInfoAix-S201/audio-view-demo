package fr.nedjar.vigiechiro.demo;

import fr.nedjar.vigiechiro.audio.AudioView;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Outil de capture : rend un {@link AudioView} <b>hors-écran</b> avec un WAV donné et écrit un PNG.
 * Sert à régénérer l'aperçu du README d'audio-view (voir {@code scripts/capture-apercu.sh}).
 *
 * <p>Paramétrage par propriétés système : {@code -Dcapture.wav=<chemin>} (requis), {@code
 * -Dcapture.out=<chemin.png>} (défaut {@code apercu.png}), {@code -Dcapture.freqZoom=<n>} (défaut
 * {@code 5}, cadre la bande basse réellement utilisée). Le rendu attend le signal {@code ready} du
 * composant (chargement + STFT terminés). L'encodeur PNG est maison (RGBA, {@code java.util.zip})
 * pour ne dépendre ni de {@code javafx.swing} ni de {@code java.desktop}.
 *
 * <p>JavaFX 25 n'a pas de plateforme « Headless » : sur un serveur sans écran, lancer via {@code
 * xvfb-run} (cf. le script). {@code snapshot()} reste déterministe.
 */
public final class CaptureApercu extends Application {

  @Override
  public void start(Stage stage) {
    String wav = System.getProperty("capture.wav");
    if (wav == null || wav.isBlank()) {
      System.err.println("capture.wav manquant : -Dcapture.wav=<chemin du WAV>");
      Platform.exit();
      return;
    }
    String out = System.getProperty("capture.out", "apercu.png");
    double freqZoom = Double.parseDouble(System.getProperty("capture.freqZoom", "5"));

    AudioView view = new AudioView();
    view.setTimeExpansionFactor(10);
    stage.setScene(new Scene(view, 1100, 680));
    stage.show();

    view.readyProperty()
        .addListener(
            (o, was, ready) -> {
              if (Boolean.TRUE.equals(ready)) {
                view.setFrequencyZoom(freqZoom);
                // Laisse un pulse de rendu s'appliquer (canvas sono + image spectro + axes).
                PauseTransition render = new PauseTransition(Duration.millis(900));
                render.setOnFinished(e -> ecrireEtQuitter(view, Path.of(out)));
                render.play();
              }
            });

    // Garde-fou : sortie même si le chargement n'aboutit jamais (WAV illisible, pas de toolkit…).
    PauseTransition securite = new PauseTransition(Duration.seconds(30));
    securite.setOnFinished(e -> Platform.exit());
    securite.play();

    view.setAudioFile(Path.of(wav));
  }

  private static void ecrireEtQuitter(AudioView view, Path out) {
    try {
      ecrirePng(view.snapshot(null, null), out);
      System.out.println("CAPTURE_OK " + out.toAbsolutePath());
    } catch (IOException ex) {
      System.err.println("Échec de l'écriture du PNG : " + ex.getMessage());
    } finally {
      Platform.exit();
    }
  }

  // ----- Encodeur PNG minimal (RGBA 8 bits, filtre None) -----

  private static void ecrirePng(WritableImage img, Path out) throws IOException {
    int w = (int) img.getWidth();
    int h = (int) img.getHeight();
    PixelReader reader = img.getPixelReader();
    byte[] brut = new byte[h * (1 + w * 4)];
    int p = 0;
    for (int y = 0; y < h; y++) {
      brut[p++] = 0; // type de filtre : None
      for (int x = 0; x < w; x++) {
        int argb = reader.getArgb(x, y);
        brut[p++] = (byte) ((argb >> 16) & 0xFF); // R
        brut[p++] = (byte) ((argb >> 8) & 0xFF); // G
        brut[p++] = (byte) (argb & 0xFF); // B
        brut[p++] = (byte) ((argb >> 24) & 0xFF); // A
      }
    }

    byte[] compresse = deflater(brut);
    try (OutputStream os = Files.newOutputStream(out)) {
      os.write(new byte[] {(byte) 137, 80, 78, 71, 13, 10, 26, 10});
      byte[] ihdr = new byte[13];
      ecrireInt(ihdr, 0, w);
      ecrireInt(ihdr, 4, h);
      ihdr[8] = 8; // profondeur 8 bits
      ihdr[9] = 6; // type couleur RGBA
      ecrireChunk(os, "IHDR", ihdr);
      ecrireChunk(os, "IDAT", compresse);
      ecrireChunk(os, "IEND", new byte[0]);
    }
  }

  private static byte[] deflater(byte[] data) {
    Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
    try {
      deflater.setInput(data);
      deflater.finish();
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      byte[] buf = new byte[16384];
      while (!deflater.finished()) {
        out.write(buf, 0, deflater.deflate(buf));
      }
      return out.toByteArray();
    } finally {
      deflater.end();
    }
  }

  private static void ecrireInt(byte[] a, int off, int v) {
    a[off] = (byte) (v >>> 24);
    a[off + 1] = (byte) (v >>> 16);
    a[off + 2] = (byte) (v >>> 8);
    a[off + 3] = (byte) v;
  }

  private static void ecrireChunk(OutputStream os, String type, byte[] data) throws IOException {
    byte[] t = type.getBytes(StandardCharsets.US_ASCII);
    byte[] len = new byte[4];
    ecrireInt(len, 0, data.length);
    os.write(len);
    os.write(t);
    os.write(data);
    CRC32 crc = new CRC32();
    crc.update(t);
    crc.update(data);
    byte[] c = new byte[4];
    ecrireInt(c, 0, (int) crc.getValue());
    os.write(c);
  }

  public static void main(String[] args) {
    launch(args);
  }
}
