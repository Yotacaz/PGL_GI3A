package fr.cy.view;

import fr.cy.model.fire.Fire;
import fr.cy.model.graph.element.GraphElement;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.Optional;

public class DialogHelper {

    // On ajoute 'Node parentNode' dans les paramètres
    public static Optional<Fire> showFireDialog(GraphElement element, javafx.scene.Node parentNode) {
        Dialog<Fire> dialog = new Dialog<>();
        dialog.setTitle("Déclenchement Incendie");
        dialog.setHeaderText("Propriétés de l'incendie (#" + element.getId() + ")");

        DialogPane dialogPane = dialog.getDialogPane();

        // 🌟 L'ASTUCE MAGIQUE EST ICI 🌟
        // On récupère exactement le même CSS que celui utilisé par l'application
        // principale
        if (parentNode != null && parentNode.getScene() != null) {
            dialog.initOwner(parentNode.getScene().getWindow()); // Centre la boîte de dialogue
            dialogPane.getStylesheets().addAll(parentNode.getScene().getStylesheets()); // Copie le CSS
        }

        ButtonType igniteBtnType = new ButtonType("🔥 Enflammer", ButtonBar.ButtonData.OK_DONE);
        dialogPane.getButtonTypes().addAll(igniteBtnType, ButtonType.CANCEL);

        // 2. Styliser les boutons avec tes classes CSS
        Button igniteBtn = (Button) dialogPane.lookupButton(igniteBtnType);
        igniteBtn.getStyleClass().addAll("action-btn", "danger-btn");

        Button cancelBtn = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
        cancelBtn.getStyleClass().add("action-btn");

        // 3. Création de la grille
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20, 20, 10, 10));

        Spinner<Double> intensitySpinner = new Spinner<>(0.1, 5.0, 1.0, 0.1);
        Spinner<Double> spreadSpinner = new Spinner<>(0.00, 1.0, 0.05, 0.01);
        intensitySpinner.setEditable(true);
        spreadSpinner.setEditable(true);

        grid.add(new Label("Intensité :"), 0, 0);
        grid.add(intensitySpinner, 1, 0);
        grid.add(new Label("Propagation :"), 0, 1);
        grid.add(spreadSpinner, 1, 1);

        dialogPane.setContent(grid);

        // 4. Conversion
        dialog.setResultConverter(btn -> {
            if (btn == igniteBtnType) {
                return new Fire(0, intensitySpinner.getValue(), spreadSpinner.getValue());
            }
            return null;
        });

        return dialog.showAndWait();
    }

    public static Optional<Integer> showRandomGraphDialog(javafx.scene.Node parentNode) {
        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("Génération de Masse");
        dialog.setHeaderText("Ajouter des nœuds aléatoires au réseau");

        DialogPane dialogPane = dialog.getDialogPane();
        if (parentNode != null && parentNode.getScene() != null) {
            dialog.initOwner(parentNode.getScene().getWindow());
            dialogPane.getStylesheets().addAll(parentNode.getScene().getStylesheets());
        }

        ButtonType generateBtnType = new ButtonType("Générer", ButtonBar.ButtonData.OK_DONE);
        dialogPane.getButtonTypes().addAll(generateBtnType, ButtonType.CANCEL);

        ((Button) dialogPane.lookupButton(generateBtnType)).getStyleClass().addAll("action-btn", "play-btn"); // Bouton
                                                                                                              // bleu
        ((Button) dialogPane.lookupButton(ButtonType.CANCEL)).getStyleClass().add("action-btn");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20, 20, 10, 10));

        Spinner<Integer> countSpinner = new Spinner<>(5, 1000, 20, 5); // Entre 5 et 1000, defaut 20
        countSpinner.setEditable(true);

        grid.add(new Label("Nombre de nœuds à créer :"), 0, 0);
        grid.add(countSpinner, 1, 0);

        dialogPane.setContent(grid);

        dialog.setResultConverter(btn -> btn == generateBtnType ? countSpinner.getValue() : null);
        return dialog.showAndWait();
    }

    public static Optional<Integer> showAgentCountDialog(javafx.scene.Node parentNode, String title,
            String headerText) {
        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(headerText);

        DialogPane dialogPane = dialog.getDialogPane();
        if (parentNode != null && parentNode.getScene() != null) {
            dialog.initOwner(parentNode.getScene().getWindow());
            dialogPane.getStylesheets().addAll(parentNode.getScene().getStylesheets());
        }

        ButtonType generateBtnType = new ButtonType("Générer", ButtonBar.ButtonData.OK_DONE);
        dialogPane.getButtonTypes().addAll(generateBtnType, ButtonType.CANCEL);

        ((javafx.scene.control.Button) dialogPane.lookupButton(generateBtnType)).getStyleClass().addAll("action-btn",
                "play-btn");
        ((javafx.scene.control.Button) dialogPane.lookupButton(ButtonType.CANCEL)).getStyleClass().add("action-btn");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20, 20, 10, 10));

        // Par défaut 10 agents, modifiable
        Spinner<Integer> countSpinner = new Spinner<>(1, 1000, 10, 1);
        countSpinner.setEditable(true);

        grid.add(new Label("Nombre d'agents :"), 0, 0);
        grid.add(countSpinner, 1, 0);

        dialogPane.setContent(grid);

        dialog.setResultConverter(btn -> btn == generateBtnType ? countSpinner.getValue() : null);
        return dialog.showAndWait();
    }

    // 1. Petite classe pour stocker les choix de l'utilisateur
    public static class EdgeParams {
        public final double width;
        public final boolean directed;

        public EdgeParams(double width, boolean directed) {
            this.width = width;
            this.directed = directed;
        }
    }

    public static class NodeParams {
        public final double capacity;
        public final boolean isExit;

        public NodeParams(double capacity, boolean isExit) {
            this.capacity = capacity;
            this.isExit = isExit;
        }
    }

    // 2. La méthode pour afficher la boîte de dialogue
    public static Optional<EdgeParams> showEdgeCreationDialog(fr.cy.model.graph.element.Node start,
            fr.cy.model.graph.element.Node end, javafx.scene.Node parentNode) {
        Dialog<EdgeParams> dialog = new Dialog<>();
        dialog.setTitle("Nouvelle Arête");
        dialog.setHeaderText("Connexion du Nœud #" + start.getId() + " vers #" + end.getId());

        DialogPane dialogPane = dialog.getDialogPane();
        if (parentNode != null && parentNode.getScene() != null) {
            dialog.initOwner(parentNode.getScene().getWindow());
            dialogPane.getStylesheets().addAll(parentNode.getScene().getStylesheets());
        }

        ButtonType createBtnType = new ButtonType("Créer l'arête", ButtonBar.ButtonData.OK_DONE);
        dialogPane.getButtonTypes().addAll(createBtnType, ButtonType.CANCEL);

        ((javafx.scene.control.Button) dialogPane.lookupButton(createBtnType)).getStyleClass().addAll("action-btn",
                "play-btn");
        ((javafx.scene.control.Button) dialogPane.lookupButton(ButtonType.CANCEL)).getStyleClass().add("action-btn");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20, 20, 10, 10));

        // Spinner pour la largeur (de 0.5 à 10.0, défaut à 2.0)
        Spinner<Double> widthSpinner = new Spinner<>(0.5, 10.0, 2.0, 0.5);
        widthSpinner.setEditable(true);

        // Case à cocher pour le sens unique
        CheckBox directedBox = new javafx.scene.control.CheckBox("Arête orientée (Sens unique)");

        grid.add(new Label("Largeur :"), 0, 0);
        grid.add(widthSpinner, 1, 0);
        grid.add(directedBox, 0, 1, 2, 1);

        dialogPane.setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == createBtnType) {
                return new EdgeParams(widthSpinner.getValue(), directedBox.isSelected());
            }
            return null;
        });

        return dialog.showAndWait();
    }

    // La méthode pour afficher la boîte de dialogue du Nœud
    public static Optional<NodeParams> showNodeCreationDialog(javafx.scene.Node parentNode) {
        Dialog<NodeParams> dialog = new Dialog<>();
        dialog.setTitle("Nouveau Nœud");
        dialog.setHeaderText("Définir la zone (Capacité en m²)");

        DialogPane dialogPane = dialog.getDialogPane();
        if (parentNode != null && parentNode.getScene() != null) {
            dialog.initOwner(parentNode.getScene().getWindow());
            dialogPane.getStylesheets().addAll(parentNode.getScene().getStylesheets());
        }

        ButtonType createBtnType = new ButtonType("Créer le Nœud", ButtonBar.ButtonData.OK_DONE);
        dialogPane.getButtonTypes().addAll(createBtnType, ButtonType.CANCEL);

        // Application du style de tes boutons
        ((javafx.scene.control.Button) dialogPane.lookupButton(createBtnType)).getStyleClass().addAll("action-btn",
                "play-btn");
        ((javafx.scene.control.Button) dialogPane.lookupButton(ButtonType.CANCEL)).getStyleClass().add("action-btn");

        CheckBox exitBox = new CheckBox("Sortie d'évacuation");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20, 20, 10, 10));

        // Spinner pour la capacité (de 10 à 5000 m², avec 50 par défaut)
        Spinner<Double> capacitySpinner = new Spinner<>(1, 5000.0, 5.0, 2.0);
        capacitySpinner.setEditable(true);

        grid.add(new Label("Capacité (m²) :"), 0, 0);
        grid.add(capacitySpinner, 1, 0);
        grid.add(exitBox, 0, 1, 2, 1);

        dialogPane.setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == createBtnType) {
                return new NodeParams(capacitySpinner.getValue(), exitBox.isSelected());
            }
            return null;
        });

        return dialog.showAndWait();
    }
}