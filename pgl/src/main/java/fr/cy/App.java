package fr.cy;

import fr.cy.controller.MainController;
import fr.cy.model.simulation.Simulation;
import fr.cy.util.ScenarioBuilder;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

/**
 * Point d'entrée de l'application (View).
 * Initialise le backend (Simulation), le JavaFX stage,
 * charge le MainController et applique le thème CSS.
 */
public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        Simulation simulation = ScenarioBuilder.buildDemoScenario();

        MainController mainController = new MainController(simulation);

        // 3. Création de la scène principale (1200x800 par défaut)
        Scene scene = new Scene(mainController.getRoot(), 1200, 800);

        // 4. Chargement du fichier CSS
        try {
            String cssPath = Objects.requireNonNull(getClass().getResource("/fr/cy/style.css")).toExternalForm();
            scene.getStylesheets().add(cssPath);
        } catch (NullPointerException e) {
            System.err.println("Fichier style.css introuvable dans le dossier resources ! " + e.getMessage());
        }

        primaryStage.setTitle("Simulation d'évacuation d'incendie - PGL");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}