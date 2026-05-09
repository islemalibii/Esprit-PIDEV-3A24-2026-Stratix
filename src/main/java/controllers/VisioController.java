package controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import com.teamdev.jxbrowser.browser.Browser;
import com.teamdev.jxbrowser.engine.Engine;
import com.teamdev.jxbrowser.engine.EngineOptions;
import com.teamdev.jxbrowser.view.javafx.BrowserView;

import models.UserRole;
import models.Utilisateur;

import java.net.URL;
import java.util.ResourceBundle;

import static com.teamdev.jxbrowser.engine.RenderingMode.HARDWARE_ACCELERATED;

public class VisioController implements Initializable {

    @FXML private Label roomIdLabel;
    @FXML private Button btnStartCall;
    @FXML private StackPane browserContainer;
    @FXML private VBox placeholderContainer;

    private Engine engine;
    private Browser browser;
    private BrowserView browserView;

    private String roomId;
    private String roomUrl;
    private Utilisateur currentUser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            System.out.println("=== INITIALISATION VISIO ===");

            currentUser = UserRole.getInstance().getUser();

            if (currentUser != null) {
                System.out.println(" Utilisateur connecté: " + currentUser.getPrenom() + " " + currentUser.getNom());
            } else {
                System.out.println("Aucun utilisateur connecté - mode anonyme");
                currentUser = createDefaultUser();
            }

            // Générer la salle
            roomId = "stratiX-" + System.currentTimeMillis();
            roomUrl = "https://talk.brave.com/" + roomId;

            if (roomIdLabel != null) {
                roomIdLabel.setText(roomId);
            }

            // Initialiser JxBrowser
            initJxBrowser();

            // Configurer le bouton
            if (btnStartCall != null) {
                btnStartCall.setOnAction(e -> startCall());
            }

            System.out.println("=== INITIALISATION TERMINÉE ===");

        } catch (Exception e) {
            System.err.println("ERREUR: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", e.getMessage());
        }
    }

    // Créer un utilisateur par défaut si pas connecté
    private Utilisateur createDefaultUser() {
        Utilisateur defaultUser = new Utilisateur();
        defaultUser.setPrenom("Utilisateur");
        defaultUser.setNom("stratiX");
        defaultUser.setEmail("user@stratix.com");
        return defaultUser;
    }

    private void initJxBrowser() {
        try {
            String licenseKey = "3GC4U6A4A0557QOIO8RFQOYKSXGB8SR0IKIICG62J5V73S37S4I50OT476EI4DBXKFA1NKY0FQA0FSOXRQO6Q3CXI4N32VC3CA3OAJQAYIHBKKCBJW7FDOZKIDCBHBBE2YAM5JKFPM3QCZFM";

            System.out.println("Initialisation de JxBrowser...");

            EngineOptions options = EngineOptions.newBuilder(HARDWARE_ACCELERATED)
                    .licenseKey(licenseKey)
                    .addSwitch("--use-fake-ui-for-media-stream")
                    .addSwitch("--enable-media-stream")
                    .addSwitch("--autoplay-policy=no-user-gesture-required")
                    .build();

            engine = Engine.newInstance(options);
            browser = engine.newBrowser();

            if (placeholderContainer != null) {
                placeholderContainer.setVisible(false);
                placeholderContainer.setManaged(false);
            }

            browserView = BrowserView.newInstance(browser);

            if (browserContainer != null) {
                browserContainer.getChildren().add(browserView);
                browserView.prefWidthProperty().bind(browserContainer.widthProperty());
                browserView.prefHeightProperty().bind(browserContainer.heightProperty());
            }

            System.out.println("JxBrowser initialisé");

        } catch (Exception e) {
            System.err.println(" Erreur JxBrowser: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur JxBrowser",
                    "Impossible d'initialiser JxBrowser. Vérifie ta clé de licence.");
        }
    }
    @FXML
    private void startCall() {
        try {
            if (browser == null) {
                showAlert("Erreur", "JxBrowser n'est pas initialisé");
                return;
            }

            String displayName = currentUser.getPrenom() + " " + currentUser.getNom();
            String url = "https://talk.brave.com/" + roomId;

            System.out.println(" Démarrage visio pour: " + displayName);
            System.out.println("URL: " + url);

            browser.navigation().loadUrl(url);

            // Script ULTIME pour Brave Talk
            browser.mainFrame().ifPresent(frame -> {
                String jsScript =
                        "var maxAttempts = 20;" +
                                "var attempt = 0;" +
                                "function fillAndJoin() {" +
                                "  attempt++;" +
                                "  console.log('Tentative ' + attempt);" +
                                "  " +
                                "  // 1. Trouver le champ de nom (selecteurs multiples)" +
                                "  var nameField = document.querySelector('input[type=\"text\"]') || " +
                                "                  document.querySelector('input[placeholder*=\"name\" i]') || " +
                                "                  document.querySelector('input[placeholder*=\"nom\" i]') || " +
                                "                  document.querySelector('input[class*=\"name\"]') || " +
                                "                  document.querySelector('input[id*=\"name\"]');" +
                                "  " +
                                "  if (nameField) {" +
                                "    // 2. Remplir le nom" +
                                "    nameField.value = '" + displayName + "';" +
                                "    nameField.dispatchEvent(new Event('input', { bubbles: true }));" +
                                "    nameField.dispatchEvent(new Event('change', { bubbles: true }));" +
                                "    console.log('✅ Nom inséré: " + displayName + "');" +
                                "    " +
                                "    // 3. Chercher le bouton Join (différents textes possibles)" +
                                "    setTimeout(function() {" +
                                "      var buttons = document.querySelectorAll('button');" +
                                "      var joinButton = null;" +
                                "      for (var i = 0; i < buttons.length; i++) {" +
                                "        var text = buttons[i].innerText.toLowerCase();" +
                                "        if (text.includes('join') || text.includes('rejoindre') || " +
                                "            text.includes('enter') || text.includes('entrer')) {" +
                                "          joinButton = buttons[i];" +
                                "          break;" +
                                "        }" +
                                "      }" +
                                "      " +
                                "      if (joinButton) {" +
                                "        joinButton.click();" +
                                "        console.log('✅ Bouton cliqué');" +
                                "        " +
                                "        // 4. Une fois dans la salle, devenir hôte" +
                                "        setTimeout(function() {" +
                                "          console.log(' Vous êtes maintenant dans la salle');" +
                                "        }, 3000);" +
                                "      }" +
                                "    }, 1000);" +
                                "  } else if (attempt < maxAttempts) {" +
                                "    setTimeout(fillAndJoin, 1000);" +
                                "  }" +
                                "}" +
                                "setTimeout(fillAndJoin, 3000);" +
                                "setTimeout(fillAndJoin, 5000);" +
                                "setTimeout(fillAndJoin, 7000);" +
                                "setTimeout(fillAndJoin, 10000);";

                frame.executeJavaScript(jsScript);
            });

            btnStartCall.setDisable(true);

        } catch (Exception e) {
            showAlert("Erreur", "Impossible de démarrer: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void copyLink() {
        javafx.scene.input.Clipboard clipboard =
                javafx.scene.input.Clipboard.getSystemClipboard();
        javafx.scene.input.ClipboardContent content =
                new javafx.scene.input.ClipboardContent();
        content.putString("https://talk.brave.com/" + roomId);
        clipboard.setContent(content);
        showAlert("✅ Lien copié", "https://talk.brave.com/" + roomId);
    }

    @FXML
    private void closeWindow() {
        if (browser != null) browser.close();
        if (engine != null) engine.close();

        Stage stage = (Stage) btnStartCall.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}