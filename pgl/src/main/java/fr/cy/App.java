package fr.cy;

import fr.cy.controller.MainController;
import fr.cy.model.simulation.Simulation;
import fr.cy.util.ScenarioBuilder;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Objects;

/**
 * Entry point (View).
 * Initialize  backend (Simulation), JavaFX stage,
 * charge MainController and apply the CSS theme.
 */
public class App extends Application {

    /**
     * Constructs the JavaFX application entry point.
     */
    public App() {
    }

    @Override
    public void start(Stage primaryStage) {
    	
    	// 1. Adding the Icon

        Image icon = new Image(getClass().getResourceAsStream("/icons/icon_5.png"));
        primaryStage.getIcons().add(icon);
        
        // Setting up the simulation

        Simulation simulation = ScenarioBuilder.setupSimplePipelineTest();

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
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    /**
     * Application entry point that launches the JavaFX runtime.
     *
     * @param args command-line arguments passed to the application
     */
    public static void main(String[] args) {
        launch(args);
    }
}

