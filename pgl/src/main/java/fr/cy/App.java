package fr.cy;

import fr.cy.controller.MainController;
import fr.cy.model.graph.Graph;
import fr.cy.model.simulation.Simulation;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
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
    	
    	// 1. Adding the Icon

        Image icon = new Image(getClass().getResourceAsStream("/icons/icon_5.png"));
        primaryStage.getIcons().add(icon);
        
        // Setting up the simulation

        Simulation simulation = new Simulation("Sandbox", new Graph());

        MainController mainController = new MainController(simulation);

        // 3. Create the main scene (1200 * 800 by default) 
        Scene scene = new Scene(mainController.getRoot(), 1200, 800);

        // 4. Charging the CSS file
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

